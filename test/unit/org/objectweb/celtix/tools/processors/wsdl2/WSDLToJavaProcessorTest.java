package org.objectweb.celtix.tools.processors.wsdl2;

import java.io.*;

import org.objectweb.celtix.tools.common.ToolConstants;
import org.objectweb.celtix.tools.processors.ProcessorTestBase;

public class WSDLToJavaProcessorTest extends ProcessorTestBase {
    
    private WSDLToJavaProcessor processor = new WSDLToJavaProcessor();
    
    public void setUp() throws Exception {
        super.setUp();
        env.put(ToolConstants.CFG_OUTPUTDIR,
                output.getCanonicalPath());
    }

    public void tearDown() {
        super.tearDown();
        processor = null;
    }
    
    public void testHelloWorld() throws Exception {
        env.put(ToolConstants.CFG_WSDLURL, getLocation("/wsdl/hello_world.wsdl"));
        processor.setEnvironment(env);
        processor.process();

        assertNotNull(output);

        File org = new File(output, "org");
        assertTrue(org.exists());
        File objectweb = new File(org, "objectweb");
        assertTrue(objectweb.exists());
        File helloworldsoaphttp = new File(objectweb, "hello_world_soap_http");
        assertTrue(helloworldsoaphttp.exists());
        File types = new File(helloworldsoaphttp, "types");
        assertTrue(types.exists());
        File[] files = helloworldsoaphttp.listFiles();
        assertEquals(files.length, 8);
        files = types.listFiles();
        assertEquals(files.length, 17);
    }

    public void testHelloWorldRPCLit() throws Exception {
        env.put(ToolConstants.CFG_WSDLURL, getLocation("/wsdl/hello_world_rpc_lit.wsdl"));
        processor.setEnvironment(env);
        processor.process();

        assertNotNull(output);

        File org = new File(output, "org");
        assertTrue(org.exists());
        File objectweb = new File(org, "objectweb");
        assertTrue(objectweb.exists());
        File helloworldsoaphttp = new File(objectweb, "hello_world_rpclit");
        assertTrue(helloworldsoaphttp.exists());
        File types = new File(helloworldsoaphttp, "types");
        assertTrue(types.exists());
        File[] files = helloworldsoaphttp.listFiles();
        assertEquals(files.length, 6);
        files = types.listFiles();
        assertEquals(files.length, 3);
    }

    private String getLocation(String wsdlFile) {
        return WSDLToJavaProcessorTest.class.getResource(wsdlFile).getFile();
    }
}
