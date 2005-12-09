package org.objectweb.celtix.tools;

import java.util.HashSet;
import java.util.Set;

import org.objectweb.celtix.tools.common.ProcessorEnvironment;
import org.objectweb.celtix.tools.common.ToolConstants;
import org.objectweb.celtix.tools.common.toolspec.ToolException;
import org.objectweb.celtix.tools.common.toolspec.ToolRunner;
import org.objectweb.celtix.tools.common.toolspec.ToolSpec;
import org.objectweb.celtix.tools.common.toolspec.parser.BadUsageException;
import org.objectweb.celtix.tools.common.toolspec.parser.ErrorVisitor;
import org.objectweb.celtix.tools.processors.java2.JavaToWSDLProcessor;

public class JavaToWSDL extends AbstractCeltixToolContainer {

    static final String TOOL_NAME = "javatowsdl";
    private static String[] args;

    public JavaToWSDL(ToolSpec toolspec) throws Exception {
        super(TOOL_NAME, toolspec);
    }

    private Set getArrayKeys() {
        Set<String> set = new HashSet<String>();
        set.add(ToolConstants.CFG_OUTPUTFILE);
        set.add(ToolConstants.CFG_TNS);
        set.add(ToolConstants.CFG_SCHEMANS);
        set.add(ToolConstants.CFG_USETYPES);
        return set;
    }

    public void execute(boolean exitOnFinish) {
        JavaToWSDLProcessor processor = new JavaToWSDLProcessor();
        try {
            super.execute(exitOnFinish);
            if (!hasInfoOption()) {
                ProcessorEnvironment env = new ProcessorEnvironment();
                env.setParameters(getParametersMap(getArrayKeys()));
                if (env.get(ToolConstants.CFG_OUTPUTFILE) == null) {
                    env.put(ToolConstants.CFG_OUTPUTFILE, "./undefinedname.wsdl");
                }

                if (isVerboseOn()) {
                    env.put(ToolConstants.CFG_VERBOSE, Boolean.TRUE);
                }
                processor.setEnvironment(env);
                processor.process();
            }
        } catch (ToolException ex) {
            System.err.println("Error : " + ex.getMessage());
            if (ex.getCause() instanceof BadUsageException) {
                getInstance().printUsageException(TOOL_NAME, (BadUsageException)ex.getCause());
            }

        } catch (Exception ex) {
            System.err.println("Error : " + ex.getMessage());
            System.err.println();
            if (isVerboseOn()) {
                ex.printStackTrace();
            }

        }
    }

    public static void main(String[] pargs) {
        args = pargs;

        try {
            ToolRunner.runTool(JavaToWSDL.class, JavaToWSDL.class
                .getResourceAsStream(ToolConstants.TOOLSPECS_BASE + "java2wsdl.xml"), false, args);
        } catch (BadUsageException ex) {
            getInstance().printUsageException(TOOL_NAME, ex);

        } catch (Exception ex) {
            System.err.println("Error : " + ex.getMessage());
            System.err.println();
            ex.printStackTrace();

        }
    }

    public void checkParams(ErrorVisitor errors) throws ToolException {
        if (errors.getErrors().size() > 0) {
            throw new ToolException("Required parameters missing", new BadUsageException(getUsage(), errors));
        }
    }
}
