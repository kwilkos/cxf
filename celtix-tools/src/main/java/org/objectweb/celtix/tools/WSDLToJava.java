package org.objectweb.celtix.tools;

import java.io.File;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;

import org.objectweb.celtix.common.i18n.Message;
import org.objectweb.celtix.tools.common.ProcessorEnvironment;
import org.objectweb.celtix.tools.common.ToolConstants;
import org.objectweb.celtix.tools.common.ToolException;
import org.objectweb.celtix.tools.common.toolspec.ToolRunner;
import org.objectweb.celtix.tools.common.toolspec.ToolSpec;
import org.objectweb.celtix.tools.common.toolspec.parser.BadUsageException;
import org.objectweb.celtix.tools.common.toolspec.parser.CommandDocument;
import org.objectweb.celtix.tools.common.toolspec.parser.ErrorVisitor;
import org.objectweb.celtix.tools.processors.wsdl2.WSDLToJavaProcessor;

public class WSDLToJava extends AbstractCeltixToolContainer {
    
    private static final String TOOL_NAME = "wsdltojava";
    private static String[] args;

    public WSDLToJava(ToolSpec toolspec) throws Exception {
        super(TOOL_NAME, toolspec);
    }

    private Set getArrayKeys() {
        Set<String> set = new HashSet<String>();
        set.add(ToolConstants.CFG_BINDING);
        set.add(ToolConstants.CFG_PACKAGENAME);
        set.add(ToolConstants.CFG_NEXCLUDE);
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

                if (env.containsKey(ToolConstants.CFG_ANT)) {
                    setAntProperties(env);
                    setLibraryReferences(env);
                }

                if (isVerboseOn()) {
                    env.put(ToolConstants.CFG_VERBOSE, Boolean.TRUE);
                }
                env.put(ToolConstants.CFG_CMD_ARG, args);

                validate(env);
                setPackageAndNamespaces(env);
                setExcludePackageAndNamespaces(env);
                
                processor.setEnvironment(env);
                processor.process();
            }
        } catch (ToolException ex) {
            System.err.println("Error : " + ex.getMessage());
            if (ex.getCause() instanceof BadUsageException) {
                getInstance().printUsageException(TOOL_NAME, (BadUsageException)ex.getCause());
            }
            System.err.println();
            if (isVerboseOn()) {
                ex.printStackTrace();
            }
        } catch (Exception ex) {
            System.err.println("Error : " + ex.getMessage());
            System.err.println();
            if (isVerboseOn()) {
                ex.printStackTrace();
            }
        }
    }

    private void setExcludePackageAndNamespaces(ProcessorEnvironment env) {
        if (env.get(ToolConstants.CFG_NEXCLUDE) != null) {
            String[] pns = (String[]) env.get(ToolConstants.CFG_NEXCLUDE);
            for (int j = 0; j < pns.length; j++) {
                int pos = pns[j].indexOf("=");
                String excludePackagename = pns[j];
                if (pos != -1) {
                    String ns = pns[j].substring(0, pos);
                    excludePackagename = pns[j].substring(pos + 1);
                    env.addExcludeNamespacePackageMap(ns, excludePackagename);
                } else {
                    env.addExcludeNamespacePackageMap(pns[j], null);
                }
            }
        }
    }
    
    private void setPackageAndNamespaces(ProcessorEnvironment env) {
        if (env.get(ToolConstants.CFG_PACKAGENAME) != null) {
            String[] pns = (String[]) env.get(ToolConstants.CFG_PACKAGENAME);
            for (int j = 0; j < pns.length; j++) {
                int pos = pns[j].indexOf("=");
                String packagename = pns[j];
                if (pos != -1) {
                    String ns = pns[j].substring(0, pos);
                    packagename = pns[j].substring(pos + 1);
                    env.addNamespacePackageMap(ns, packagename);
                } else {
                    env.setPackageName(packagename);
                }
            }
        }
    }

    private void validate(ProcessorEnvironment env) throws ToolException {
        String outdir = (String) env.get(ToolConstants.CFG_OUTPUTDIR);
        if (outdir != null) {
            File dir = new File(outdir);
            if (!dir.exists()) {
                Message msg = new Message("DIRECTORY_NOT_EXIST", LOG, outdir);
                throw new ToolException(msg);
            }
            if (!dir.isDirectory()) {
                Message msg = new Message("NOT_A_DIRECTORY", LOG, outdir);
                throw new ToolException(msg);
            }
        }

        if (env.containsKey(ToolConstants.CFG_BINDING)) {
            String[] bindings = (String[]) env.get(ToolConstants.CFG_BINDING);
            for (int i = 0; i < bindings.length; i++) {
                File binding = new File(bindings[i]);
                if (!binding.exists()) {
                    Message msg = new Message("FILE_NOT_EXIST", LOG, binding);
                    throw new ToolException(msg);
                } else if (binding.isDirectory()) {
                    Message msg = new Message("NOT_A_FILE", LOG, binding);
                    throw new ToolException(msg);
                }
            }
        }
        
        if (!env.optionSet(ToolConstants.CFG_EXTRA_SOAPHEADER)) {
            env.put(ToolConstants.CFG_EXTRA_SOAPHEADER, "false");
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
        } catch (Exception ex) {
            System.err.println("Error : " + ex.getMessage());
            System.err.println();
            ex.printStackTrace();
        }
    }

    public void checkParams(ErrorVisitor errors) throws ToolException {
        CommandDocument doc = super.getCommandDocument();

        if (!doc.hasParameter("wsdlurl")) {
            errors.add(new ErrorVisitor.UserError("WSDL/SCHEMA URL has to be specified"));
        }
        if (errors.getErrors().size() > 0) {
            Message msg = new Message("PARAMETER_MISSING", LOG);
            throw new ToolException(msg, new BadUsageException(getUsage(), errors));
        }
    }
}
