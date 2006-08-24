package org.apache.cxf.tools.java2wsdl.processor;

//import java.io.File;

import org.apache.cxf.tools.common.ToolConstants;

public class JavaToWSDLNoAnnoTest extends ProcessorTestBase {

    private JavaToWSDLProcessor j2wProcessor;

    public void setUp() throws Exception {
        super.setUp();
        j2wProcessor = new JavaToWSDLProcessor();
        System.setProperty("java.class.path", getClassPath());
    }

    public void tearDown() {
        super.tearDown();
        j2wProcessor = null;
    }

    
    public void testGeneratedWithElementryClass() throws Exception {
        env.put(ToolConstants.CFG_OUTPUTFILE, output.getPath() + "/doc_bare.wsdl");
        env.put(ToolConstants.CFG_CLASSNAME, "org.apache.cxf.tools.fortest.classnoanno.docbare.Stock");

        j2wProcessor.setEnvironment(env);
        j2wProcessor.process();

    }
    
    public void testGeneratedWithDocWrappedClass() throws Exception {
        env.put(ToolConstants.CFG_OUTPUTFILE, output.getPath() + "/doc_wrapped.wsdl");
        env.put(ToolConstants.CFG_CLASSNAME,
                "org.apache.cxf.tools.fortest.classnoanno.docwrapped.Stock");

        j2wProcessor.setEnvironment(env);
        j2wProcessor.process();

    }
   
    public void testGeneratedWithRPCClass() throws Exception {
        env.put(ToolConstants.CFG_OUTPUTFILE, output.getPath() + "/rpc.wsdl");
        env.put(ToolConstants.CFG_CLASSNAME, "org.apache.cxf.tools.fortest.classnoanno.rpc.Stock");

        j2wProcessor.setEnvironment(env);
        j2wProcessor.process();
    }


}
