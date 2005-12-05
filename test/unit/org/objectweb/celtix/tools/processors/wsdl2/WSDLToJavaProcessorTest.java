package org.objectweb.celtix.tools.processors.wsdl2;

import java.io.*;

import junit.framework.TestCase;
import org.objectweb.celtix.tools.common.ProcessorEnvironment;
import org.objectweb.celtix.tools.common.ToolConstants;

public class WSDLToJavaProcessorTest extends TestCase {
    
    private final ProcessorEnvironment env = new ProcessorEnvironment();
    private final WSDLToJavaProcessor processor = new WSDLToJavaProcessor();
    private final File output = new File("org/objectweb/celtix/tools/processors/wsdl2/resources");
    
    public void setUp() throws Exception {
        if (!output.exists()) {
            output.mkdir();
        }

        env.put(ToolConstants.CFG_OUTPUTDIR,
                output.getCanonicalPath());
    }

    public void tearDown() {
        output.deleteOnExit();
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
        assertEquals(files.length, 15);
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
