/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package be.libis.primo.enrichment;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.script.Bindings;
import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

/**
 *
 * @author mehmetc
 */
public class EnrichmentScriptManager {

    private static ScriptEngine scriptEngine = null;
    private static String evalScriptSource = "";
    private static CompiledScript evalScript = null;
    private static Long reloadLastTriggered = 0l;
    private static final String lineSeparator = System.getProperty("line.separator");
    private static final Logger logger = Logger.getLogger(EnrichmentScriptManager.class.getName());

    private EnrichmentScriptManager() {
    }

    public static synchronized ScriptEngine getScriptEngine(String engineName) {
        final long startTime = System.nanoTime();
        final long endTime;

        try {
            if (scriptEngine == null) {
                ScriptEngineManager enrichmentScriptEngineManager = new ScriptEngineManager();

                /*
                List<ScriptEngineFactory> factories = enrichmentScriptEngineManager.getEngineFactories();
                String available_factories = "";
                for (ScriptEngineFactory factory : factories) {
                if (available_factories.length() > 0) {
                available_factories += ", ";
                }
                available_factories += factory.getEngineName();
                }
                logger.log(Level.INFO, "Available factories:{0}", available_factories);
                 */
                scriptEngine = enrichmentScriptEngineManager.getEngineByName(engineName);

                if (scriptEngine == null) {
                    logger.log(Level.INFO, "Failed to load {0} engine", engineName);
                } else {
                    if (engineName.equalsIgnoreCase("jruby")) {
                        scriptEngine.getContext().setAttribute("org.jruby.embed.localvariable.behavior", "transient", scriptEngine.getContext().ENGINE_SCOPE);
                        scriptEngine.getContext().setAttribute("org.jruby.embed.localcontext.scope", "threadsafe", scriptEngine.getContext().ENGINE_SCOPE);
                        scriptEngine.getContext().setAttribute("org.jruby.embed.compilemode", "jit", scriptEngine.getContext().ENGINE_SCOPE);
                    }
                    logger.info("Script Engine instantiated");
                }
            }
        } catch (Exception ex) {
            logger.log(Level.INFO, "getScriptEngine:{0}", ex.toString());
            Writer result = new StringWriter();
            PrintWriter printWriter = new PrintWriter(result);
            ex.printStackTrace(printWriter);
            logger.info(result.toString());
        }

        if (scriptEngine == null) {
            logger.log(Level.INFO, "Could not find {0} engine", engineName);
        }

        endTime = System.nanoTime();
        final long duration = endTime - startTime;

        Logger.getLogger(ScriptingEnrichment.class.getName()).log(Level.INFO,
                    "getScriptEngine took:{0} sec -- {1} msec",
                    new Object[]{TimeUnit.NANOSECONDS.toSeconds(duration),
                                 TimeUnit.NANOSECONDS.toMillis(duration)});

        return scriptEngine;
    }

    public static synchronized Object run(String engineName, String script, Map<String, Object> parameters) {
        Object returnValue = null;
        if (loadScript(script)) {
            try {
                ScriptEngine runtimeEngine = EnrichmentScriptManager.getScriptEngine(engineName);

                if (runtimeEngine == null) {
                    logger.info("EnrichmentScriptManager: No runtime engine");
                } else if (EnrichmentScriptManager.evalScriptSource == null || EnrichmentScriptManager.evalScriptSource.equals("")) {
                    logger.info("EnrichmentScriptManager: No script");
                } else {
                    if (EnrichmentScriptManager.evalScript == null) {
                        logger.info("Compiling script");
                        Compilable evalScriptCompiler = (Compilable) runtimeEngine;
                        EnrichmentScriptManager.evalScript = evalScriptCompiler.compile(EnrichmentScriptManager.evalScriptSource);
                    }

                    if (EnrichmentScriptManager.evalScript != null) {
                        Bindings bindings = runtimeEngine.createBindings();

                        for (Map.Entry<String, Object> entry : parameters.entrySet()) {
                            bindings.put(entry.getKey(), entry.getValue());
                        }

                        if (bindings.containsKey("xml_document")) {
                            logger.info("Running script");
                            returnValue = EnrichmentScriptManager.evalScript.eval(bindings);
                        }
                    }
                }

            } catch (Exception ex) {
                logger.info("Bailed on 'run' 1");
                logger.info(ex.toString());
            }
        }

        return returnValue;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException();
    }

    private static synchronized boolean loadScript(String scriptName) {
        boolean result = true;
        final long startTime = System.nanoTime();
        final long endTime;

        StringBuilder script = new StringBuilder();
        script = script.append("");

        try {
            File scriptFile = new File(scriptName);
            String scriptPath = scriptFile.getParent();
            File reloadFile = new File(scriptPath + "/reload.txt");
            if (EnrichmentScriptManager.evalScriptSource.length() == 0 || reloadFile.exists()) {
                if (EnrichmentScriptManager.reloadLastTriggered != reloadFile.lastModified()) {
                    EnrichmentScriptManager.reloadLastTriggered = reloadFile.lastModified();
                    EnrichmentScriptManager.evalScriptSource = "";
                    EnrichmentScriptManager.evalScript = null;
                }

                if (EnrichmentScriptManager.evalScriptSource.length() == 0) {
                    logger.info("Loading script from file");
                    BufferedReader in = new BufferedReader(new FileReader(scriptFile));
                    String str = "";
                    while ((str = in.readLine()) != null) {
                        script = script.append(str).append(lineSeparator);
                    }
                    in.close();

                    EnrichmentScriptManager.evalScriptSource = script.toString();
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(EnrichmentScriptManager.class.getName()).log(Level.SEVERE, null, ex);
            result = false;
        }

        endTime = System.nanoTime();
        final long duration = endTime - startTime;

        Logger.getLogger(ScriptingEnrichment.class.getName()).log(Level.INFO,
                    "loadscript took:{0} sec -- {1} msec",
                    new Object[]{TimeUnit.NANOSECONDS.toSeconds(duration),
                                 TimeUnit.NANOSECONDS.toMillis(duration)});

        
        return result;
    }
}
