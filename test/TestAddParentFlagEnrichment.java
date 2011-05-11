import be.libis.primo.enrichment.ScriptingEnrichment;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
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
public class TestAddParentFlagEnrichment {

    public static void main(String[] args) {
        try {
            Map<String, String> parms = new HashMap<String, String>();
            parms.put(ScriptingEnrichment.PLUGIN_PARAM_FILENAME, "/Users/mehmetc/Sources/Libis/Primo/Enrichments/data/plugin_parameters.txt");
            Document dcmnt = null;
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();

            //dcmnt = db.parse("/Users/mehmetc/Sources/Libis/Primo/Enrichments/data/pnx/harry_potter.xml");
        //    dcmnt = db.parse("/Users/mehmetc/Sources/Libis/Primo/Enrichments/data/pnx/metea_magazine_article.xml");
            dcmnt = db.parse("/Users/mehmetc/Sources/Libis/Primo/Enrichments/data/pnx/noviomagus.xml");


            Document result_dcmnt = null;
            ScriptingEnrichment apfe = new ScriptingEnrichment();
            //for(int i=0; i<2; i++) {
                result_dcmnt = apfe.enrich(dcmnt, parms);

            //}
            
            try {
                Transformer transformer;
                transformer = TransformerFactory.newInstance().newTransformer();

                StreamResult result = new StreamResult(System.out);
                DOMSource source = new DOMSource(result_dcmnt);
                transformer.transform(source, result);
            } catch (TransformerException ex) {
                Logger.getLogger(ScriptingEnrichment.class.getName()).log(Level.SEVERE, null, ex);
            }

        } catch (SAXException ex) {
            Logger.getLogger(TestAddParentFlagEnrichment.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(TestAddParentFlagEnrichment.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ParserConfigurationException ex) {
            Logger.getLogger(TestAddParentFlagEnrichment.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
}
