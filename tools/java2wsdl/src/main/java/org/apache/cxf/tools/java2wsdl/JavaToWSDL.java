package org.apache.cxf.tools.java2wsdl;

import java.util.HashSet;

import javax.wsdl.Definition;

import org.apache.cxf.common.i18n.Message;
import org.apache.cxf.tools.common.AbstractCXFToolContainer;
import org.apache.cxf.tools.common.ProcessorEnvironment;
import org.apache.cxf.tools.common.ToolConstants;
import org.apache.cxf.tools.common.ToolException;
import org.apache.cxf.tools.common.toolspec.ToolRunner;
import org.apache.cxf.tools.common.toolspec.ToolSpec;
import org.apache.cxf.tools.common.toolspec.parser.BadUsageException;
import org.apache.cxf.tools.common.toolspec.parser.ErrorVisitor;
import org.apache.cxf.tools.java2wsdl.processor.JavaToWSDLProcessor;

public class JavaToWSDL extends AbstractCXFToolContainer {
   
    private static final String TOOL_NAME = "java2wsdl";
    private static String[] args;
    private static Definition definition;

    public JavaToWSDL(ToolSpec toolspec) throws Exception {
        super(TOOL_NAME, toolspec);
    }

    public void execute(boolean exitOnFinish) throws ToolException {
        JavaToWSDLProcessor processor = new JavaToWSDLProcessor();
        
        try {
            super.execute(exitOnFinish);
            if (!hasInfoOption()) {
                ProcessorEnvironment env = new ProcessorEnvironment();
                env.setParameters(getParametersMap(new HashSet()));
                if (isVerboseOn()) {
                    env.put(ToolConstants.CFG_VERBOSE, Boolean.TRUE);
                }
                processor.setEnvironment(env);
                processor.process();
                definition = processor.getModel().getDefinition();
            }
        } catch (ToolException ex) {            
            if (ex.getCause() instanceof BadUsageException) {
                getInstance().printUsageException(TOOL_NAME, (BadUsageException)ex.getCause());
            }
            throw ex;
        } catch (Exception ex) {
            throw new ToolException(ex.getMessage(), ex.getCause());
        }
    }

    public static void main(String[] pargs) { 
        try {
            runTool(pargs);
        } catch (BadUsageException ex) {
            System.err.println("Error : " + ex.getMessage());
            getInstance().printUsageException(TOOL_NAME, ex);
            if (getInstance().isVerboseOn()) {
                ex.printStackTrace();
            }
        } catch (Exception ex) {
            System.err.println("Error : " + ex.getMessage());
            System.err.println();
            if (getInstance().isVerboseOn()) {
                ex.printStackTrace();
            }
        }
    }
    
    public static void runTool(String[] pargs) throws Exception {
        args = pargs;
        ToolRunner.runTool(JavaToWSDL.class, JavaToWSDL.class
                .getResourceAsStream("java2wsdl.xml"), false, args);
    }

    public void checkParams(ErrorVisitor errors) throws ToolException {
        if (errors.getErrors().size() > 0) {
            Message msg = new Message("PARAMETER_MISSSING", LOG);
            throw new ToolException(msg, new BadUsageException(getUsage(), errors));
        }
    }
    
    
    public static Definition getDefinition() {
        return definition;
    }
}
