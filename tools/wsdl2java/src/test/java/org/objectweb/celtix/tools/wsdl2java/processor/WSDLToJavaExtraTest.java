package org.objectweb.celtix.tools.wsdl2java.processor;

import java.io.File;

import org.objectweb.celtix.tools.common.ProcessorTestBase;
import org.objectweb.celtix.tools.common.ToolConstants;

public class WSDLToJavaExtraTest extends ProcessorTestBase {
    private WSDLToJavaProcessor processor = new WSDLToJavaProcessor();

    public void setUp() throws Exception {
        super.setUp();
        File classFile = new java.io.File(output.getCanonicalPath() + "/classes");
        classFile.mkdir();
        System.setProperty("java.class.path", getClassPath() + classFile.getCanonicalPath()
                                              + File.separatorChar);
        
        env.put(ToolConstants.CFG_COMPILE, "compile");
        env.put(ToolConstants.CFG_OUTPUTDIR, output.getCanonicalPath());
        env.put(ToolConstants.CFG_CLASSDIR, output.getCanonicalPath() + "/classes");
    }

    public void tearDown() {
        super.tearDown();
        processor = null;

    }
    
    public void testAb2301() throws Exception {
        env.put(ToolConstants.CFG_WSDLURL, getLocation("/wsdl/ab2301.wsdl"));
        processor.setEnvironment(env);
        processor.process();

    }
        
    public void testAb2371() throws Exception {
        env.put(ToolConstants.CFG_WSDLURL, getLocation("/wsdl/ab2371.wsdl"));
        processor.setEnvironment(env);
        processor.process();

    }
    
    
    public void testAb2456() throws Exception {
        env.put(ToolConstants.CFG_WSDLURL, getLocation("/wsdl/ab2456.wsdl"));
        processor.setEnvironment(env);
        try {
            processor.process();
        } catch (Exception e) {
            assertEquals("Unsupported exception should be thorwn",
                         "Rpc/encoded wsdls are not supported in JAXWS 2.0", e.getMessage());
        }
    }
    
     
    private String getLocation(String wsdlFile) {
        return WSDLToJavaExtraTest.class.getResource(wsdlFile).getFile();
    }

}
