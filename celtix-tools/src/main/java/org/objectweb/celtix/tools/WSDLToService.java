package org.objectweb.celtix.tools;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import org.objectweb.celtix.tools.common.ProcessorEnvironment;
import org.objectweb.celtix.tools.common.ToolConstants;
import org.objectweb.celtix.tools.common.toolspec.ToolException;
import org.objectweb.celtix.tools.common.toolspec.ToolRunner;
import org.objectweb.celtix.tools.common.toolspec.ToolSpec;
import org.objectweb.celtix.tools.common.toolspec.parser.BadUsageException;
import org.objectweb.celtix.tools.common.toolspec.parser.CommandDocument;
import org.objectweb.celtix.tools.common.toolspec.parser.ErrorVisitor;
import org.objectweb.celtix.tools.processors.wsdl2.WSDLToServiceProcessor;

public class WSDLToService extends AbstractCeltixToolContainer {

    static final String TOOL_NAME = "wsdltoservice";
    private static String[] args;

    public WSDLToService(ToolSpec toolspec) throws Exception {
        super(TOOL_NAME, toolspec);
    }

    private Set getArrayKeys() {
        return new HashSet<String>();
    }
    
    public void execute(boolean exitOnFinish) {
        WSDLToServiceProcessor processor = new WSDLToServiceProcessor();
        try {
            super.execute(exitOnFinish);
            if (!hasInfoOption()) {
                ProcessorEnvironment env = new ProcessorEnvironment();
                env.setParameters(getParametersMap(getArrayKeys()));

                if (isVerboseOn()) {
                    env.put(ToolConstants.CFG_VERBOSE, Boolean.TRUE);
                }
                
                env.put(ToolConstants.CFG_CMD_ARG, args);

                validate(env);
                
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

    private void validate(ProcessorEnvironment env) throws ToolException {
        String outdir = (String) env.get(ToolConstants.CFG_OUTPUTDIR);
        if (outdir != null) {
            File dir = new File(outdir);
            if (!dir.exists()) {
                throw new ToolException("Specified direcotry [" + outdir + "] is not exist");
            }
            if (!dir.isDirectory()) {
                throw new ToolException("Specified direcotry [" + outdir + "] is not a direcotry");
            }
        }               
    }

    public static void main(String[] pargs) {
        args = pargs;
        String protocol = ""; 
        for (int i = 0; i < pargs.length; i++) {
            if (pargs[i].equals("-transport")) {
                protocol = pargs[i + 1];
                break;
            }
        }
        if ("".equals(protocol)) {
            protocol = "http";
        }
        try {
            String toolSpecFile = ToolConstants.TOOLSPECS_BASE + "wsdl2service_" + protocol + ".xml";
            ToolRunner.runTool(WSDLToService.class,
                               WSDLToService.class.getResourceAsStream(toolSpecFile),
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
            throw new ToolException("Required parameters missing", new BadUsageException(getUsage(), errors));
        }
    }
}
