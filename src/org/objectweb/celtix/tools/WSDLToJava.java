package org.objectweb.celtix.tools;

import java.util.*;

import org.objectweb.celtix.tools.common.ProcessorEnvironment;
import org.objectweb.celtix.tools.common.ToolConstants;
import org.objectweb.celtix.tools.common.toolspec.ToolException;
import org.objectweb.celtix.tools.common.toolspec.ToolRunner;
import org.objectweb.celtix.tools.common.toolspec.ToolSpec;
import org.objectweb.celtix.tools.common.toolspec.parser.BadUsageException;
import org.objectweb.celtix.tools.common.toolspec.parser.CommandDocument;
import org.objectweb.celtix.tools.common.toolspec.parser.ErrorVisitor;
import org.objectweb.celtix.tools.processors.wsdl2.WSDLToJavaProcessor;

public class WSDLToJava extends AbstractCeltixToolContainer {
    static final String TOOL_NAME = "wsdltojava";
    private static String[] args;

    public WSDLToJava(ToolSpec toolspec) throws Exception {
        super(TOOL_NAME, toolspec);
    }

    private Set getArrayKeys() {
        Set<String> set = new HashSet<String>();
        set.add(ToolConstants.CFG_PORTTYPE);
        set.add(ToolConstants.CFG_PACKAGENAME);
        set.add(ToolConstants.CFG_NINCLUDE);
        set.add(ToolConstants.CFG_NEXCLUDE);
        set.add(ToolConstants.CFG_WEBSERVICE);
        return set;
    }

    public void execute(boolean exitOnFinish) {
        WSDLToJavaProcessor processor = new WSDLToJavaProcessor();
        try {
            super.execute(exitOnFinish);
            if (!hasInfoOption()) {
                ProcessorEnvironment env = new ProcessorEnvironment();
                env.setParameters(getParametersMap(getArrayKeys()));
                if (env.get(ToolConstants.CFG_OUTPUTDIR) == null) {
                    env.put(ToolConstants.CFG_OUTPUTDIR, ".");
                }

                if (!env.containsKey(ToolConstants.CFG_NIGNOREEXCLUDE)) {
                    String[] excludes = getDefaultExcludedNamespaces("wsdltojavaexclude.properties");
                    if (excludes != null) {
                        env.put(ToolConstants.CFG_NIGNOREEXCLUDE, excludes);
                    }
                } else {
                    env.remove(ToolConstants.CFG_NIGNOREEXCLUDE);
                }

                if (env.containsKey(ToolConstants.CFG_ANT)) {
                    setAntProperties(env);
                    setLibraryReferences(env);
                }

                if (isVerboseOn()) {
                    env.put(ToolConstants.CFG_VERBOSE, Boolean.TRUE);
                }
                env.put(ToolConstants.CFG_CMD_ARG, args);

                processor.setEnvironment(env);
                processor.process();
            }
        } catch (ToolException ex) {
            System.err.println("Error : " + ex.getMessage());
            if (ex.getCause() instanceof BadUsageException) {
                getInstance().printUsageException(TOOL_NAME, (BadUsageException)ex.getCause());
            }
            System.exit(1);
        } catch (Exception ex) {
            System.err.println("Error : " + ex.getMessage());
            System.err.println();
            if (isVerboseOn()) {
                ex.printStackTrace();
            }
            System.exit(1);
        }
    }

    protected void setAntProperties(ProcessorEnvironment env) {
        String installDir = System.getProperty("install.dir");
        if (installDir != null) {
            env.put(ToolConstants.CFG_INSTALL_DIR, installDir);
        } else {
            env.put(ToolConstants.CFG_INSTALL_DIR, ".");
        }
    }

    protected void setLibraryReferences(ProcessorEnvironment env) {
        Properties props = loadProperties("wsdltojavalib.properties");
        if (props != null) {
            for (Iterator keys = props.keySet().iterator(); keys.hasNext();) {
                String key = (String) keys.next();
                env.put(key, props.get(key));
            }
        }
        env.put(ToolConstants.CFG_ANT_PROP, props);
    }

    public static void main(String[] pargs) {
        args = pargs;

        try {
            ToolRunner.runTool(WSDLToJava.class,
                               WSDLToJava.class.getResourceAsStream(ToolConstants.TOOLSPECS_BASE
                                                                    + "wsdl2java.xml"),
                               false,
                               args);
        } catch (BadUsageException ex) {
            getInstance().printUsageException(TOOL_NAME, ex);
            System.exit(1);
        } catch (Exception ex) {
            System.err.println("Error : " + ex.getMessage());
            System.err.println();
            ex.printStackTrace();
            System.exit(1);
        }
    }

    public void checkParams(ErrorVisitor errors) throws ToolException {
        CommandDocument doc = super.getCommandDocument();

        if (!doc.hasParameter("wsdlurl")) {
            errors.add(new ErrorVisitor.UserError("WSDL/SCHEMA URL has to be specified"));
        }
        if (errors.getErrors().size() > 0) {
            throw new ToolException("Required parameters missing", new BadUsageException(getUsage(), errors));
        }
    }
}
