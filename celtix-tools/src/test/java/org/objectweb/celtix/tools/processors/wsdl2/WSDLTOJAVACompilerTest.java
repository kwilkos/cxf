package org.objectweb.celtix.tools.processors.wsdl2;
import java.io.File;

import org.objectweb.celtix.tools.WSDLToJava;
import org.objectweb.celtix.tools.processors.ProcessorTestBase;

public class WSDLTOJAVACompilerTest extends ProcessorTestBase {
    
    public void setUp() throws Exception {
        super.setUp();
        java.io.File file = new java.io.File(output.getCanonicalPath() + "/classes");
        file.mkdir();
    }

    public void testCompileGeneratedCode() throws Exception {
        String[] args = new String[]{"-compile", "-classdir", 
                                     output.getCanonicalPath() + "/classes", 
                                     "-d", output.getCanonicalPath(),
                                     getLocation("/wsdl/hello_world_rpc_lit.wsdl")};
        WSDLToJava.main(args);
        File seiClass = new File(output.getCanonicalPath() 
                                 + "/classes/org/objectweb/hello_world_rpclit/GreeterRPCLit.class");
        assertTrue("Generated code compiled fail!", seiClass.exists());
 
    }
    
    private String getLocation(String wsdlFile) {
        return WSDLTOJAVACompilerTest.class.getResource(wsdlFile).getFile();
    }
}

