
import be.libis.primo.enrichment.ScriptingEnrichment;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author mehmetc
 */
public class TestSimpleEnrichment {
    public static final Logger logger = Logger.getLogger(TestSimpleEnrichment.class.getName());
    private static final String filePath = "/Users/mehmetc/Sources/Libis/Primo/Research/PnxExport/records";


    public static void main(String[] args) {
        try {
            Map<String, String> parms = new HashMap<String, String>();            
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            ScriptingEnrichment apfe = new ScriptingEnrichment();


            parms.put(ScriptingEnrichment.PLUGIN_PARAM_FILENAME, "/Users/mehmetc/Sources/Libis/Primo/Enrichments/data/plugin_parameters.txt");
            File dir = new File(filePath);
            String[] files = dir.list();
            for (String file : files) {
                try {
                    logger.info(file);

                    Document dcmnt = null;
                    Document result_dcmnt = null;

                    dcmnt = db.parse(filePath + "/" + file);
                    if (dcmnt != null)
                        result_dcmnt = apfe.enrich(dcmnt, parms);
                    else {
                        throw new IOException("Unable to parse " + file);
                    }
                } catch (SAXException ex) {
                    logger.log(Level.SEVERE, null, ex);
                } catch (IOException ex) {
                    logger.log(Level.SEVERE, null, ex);
                }
            }
        } catch (ParserConfigurationException ex) {
            logger.log(Level.SEVERE, null, ex);
        }

        
    }
}
