/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package be.libis.primo.enrichment;

import com.exlibris.primo.api.enrichment.plugin.EnrichmentPlugIn;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.w3c.dom.Document;

/**
 *
 * @author Mehmet Celik LIBIS/K.U.Leuven
 */
public class ScriptingEnrichment implements EnrichmentPlugIn {

    public static String PLUGIN_PARAM_FILENAME = "plugin_param_filename";
    private String script_file_name = "";
    private String script_engine_name = "jruby";

    public Document enrich(Document dcmnt, Map map) {        
        final long startTime = System.nanoTime();
        final long endTime;

        try {
            init(map);

            Document enrichedDocument = enrichViaScript(dcmnt);

            endTime = System.nanoTime();
            final long duration = endTime - startTime;
            Logger.getLogger(ScriptingEnrichment.class.getName()).log(Level.INFO,
                    "Enrichment took:{0} sec -- {1} msec",
                    new Object[]{TimeUnit.NANOSECONDS.toSeconds(duration),
                                 TimeUnit.NANOSECONDS.toMillis(duration)});

            try {
                BufferedWriter rlog = null;
                rlog = new BufferedWriter(new FileWriter("/tmp/global_enrichment.log", true));
                rlog.write(String.format("%s%s;%d\n", PNXHelper.getSourceId(dcmnt), PNXHelper.getRecordId(dcmnt), TimeUnit.NANOSECONDS.toMillis(duration)));

                rlog.flush();
                rlog.close();
            } catch (IOException ioEx) {
                Logger.getLogger(PNXHelper.class.getName()).log(Level.SEVERE, null, ioEx);
            }



            return enrichedDocument;
        } catch (Exception ex) {
            Logger.getLogger(ScriptingEnrichment.class.getName()).info(ex.getMessage());
        }

        return dcmnt;
    }

    private Document enrichViaScript(Document dcmnt) {
        final long startTime = System.nanoTime();
        final long endTime;

        Document new_dcmnt = null;

        try {           
            Map<java.lang.String, java.lang.Object> parameters = new HashMap<java.lang.String, java.lang.Object>();            
            parameters.put("xml_document", dcmnt);
            parameters.put("script_file_name", this.script_file_name);
            
            new_dcmnt = (Document) EnrichmentScriptManager.run(this.script_engine_name, this.script_file_name, parameters);
        } catch (Exception ex) {
            Logger.getLogger(ScriptingEnrichment.class.getName()).info("Bailed on 'enrichViaScript'");
            Logger.getLogger(ScriptingEnrichment.class.getName()).info(ex.getMessage());
        }
        endTime = System.nanoTime();
        final long duration = endTime - startTime;

        Logger.getLogger(ScriptingEnrichment.class.getName()).log(Level.INFO,
                    "Eval took:{0} sec -- {1} msec",
                    new Object[]{TimeUnit.NANOSECONDS.toSeconds(duration),
                                 TimeUnit.NANOSECONDS.toMillis(duration)});

        if (new_dcmnt == null) {
            return dcmnt;
        }
        return new_dcmnt;
    }

    private void init(Map map) throws Exception {        
        try {
            Properties props = new Properties();
            String propsFileName = (String) map.get(ScriptingEnrichment.PLUGIN_PARAM_FILENAME);
            FileInputStream propsFile = new FileInputStream(propsFileName);

            props.load(propsFile);
            propsFile.close();

            this.script_file_name = props.getProperty("SCRIPT_FILE");
            this.script_engine_name = props.getProperty("SCRIPT_ENGINE");

            if (this.script_engine_name == null) {
                this.script_engine_name = "jruby";
            }
        } catch (Exception ex) {
            Logger.getLogger(ScriptingEnrichment.class.getName()).info(ex.getMessage());
        }
    }
}
