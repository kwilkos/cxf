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

    public void testDocLitHolder() throws Exception {
        env.put(ToolConstants.CFG_WSDLURL, getLocation("/wsdl/mapping-doc-literal.wsdl"));
        processor.setEnvironment(env);
        processor.process();

        assertNotNull(output);

        File com = new File(output, "com");
        assertTrue(com.exists());
        File iona = new File(com, "iona");
        assertTrue(iona.exists());
        File artix = new File(iona, "artix");
        assertTrue(artix.exists());
        File mapping = new File(artix, "mapping");
        assertTrue(mapping.exists());
        File[] files = mapping.listFiles();
        assertEquals(files.length, 9);
    }

    public void testSchemaImport() throws Exception {
        env.put(ToolConstants.CFG_WSDLURL, getLocation("/wsdl/hello_world_schema_import.wsdl"));
        
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
        assertEquals(files.length, 7);
        files = types.listFiles();
        assertEquals(files.length, 10);
    }
    
    public void testExceptionNameCollision() throws Exception {
        env.put(ToolConstants.CFG_WSDLURL, getLocation("/wsdl/InvoiceServer.wsdl"));
        
        processor.setEnvironment(env);
        processor.process();

        assertNotNull(output);

        File org = new File(output, "org");
        assertTrue(org.exists());
        File celtix = new File(org, "celtix");
        assertTrue(celtix.exists());
        File courseware = new File(celtix, "courseware");
        assertTrue(courseware.exists());
        File invoiceserver = new File(courseware, "invoiceserver");
        assertTrue(invoiceserver.exists());
        File invoice = new File(courseware, "invoice");
        assertTrue(invoice.exists());

        File exceptionCollision = new File(invoiceserver, "NoSuchCustomerFault_Exception.java");
        assertTrue(exceptionCollision.exists());

        File[] files = invoiceserver.listFiles();
        assertEquals(files.length, 15);
        files = invoice.listFiles();
        assertEquals(files.length, 9);
    }

    public void testAllNameCollision() throws Exception {
        env.put(ToolConstants.CFG_WSDLURL, getLocation("/wsdl/hello_world_collision.wsdl"));
        env.put(ToolConstants.CFG_PACKAGENAME, "org.objectweb");
        
        processor.setEnvironment(env);
        processor.process();

        assertNotNull(output);

        File org = new File(output, "org");
        assertTrue(org.exists());
        File objectweb = new File(org, "objectweb");
        assertTrue(objectweb.exists());

        File[] files = objectweb.listFiles();
        assertEquals(files.length, 16);

        File typeCollision = new File(objectweb, "Greeter_Type.java");
        assertTrue(typeCollision.exists());
        File exceptionCollision = new File(objectweb, "Greeter_Exception.java");
        assertTrue(exceptionCollision.exists());
        File serviceCollision = new File(objectweb, "Greeter_Service.java");
        assertTrue(serviceCollision.exists());
    }

    public void testHelloWorldAsync() throws Exception {
        env.put(ToolConstants.CFG_WSDLURL, getLocation("/wsdl/hello_world_async.wsdl"));
        env.put(ToolConstants.CFG_PACKAGENAME, "org.objectweb");
        
        processor.setEnvironment(env);
        processor.process();

        assertNotNull(output);

        File org = new File(output, "org");
        assertTrue(org.exists());
        File objectweb = new File(org, "objectweb");
        assertTrue(objectweb.exists());

        File[] files = objectweb.listFiles();
        assertEquals(files.length, 9);
    }

    public void testHelloWorldExternalBindingFile() throws Exception {
        env.put(ToolConstants.CFG_WSDLURL, getLocation("/wsdl/hello_world_jaxws_base.wsdl"));
        env.put(ToolConstants.CFG_BINDING, getLocation("/wsdl/hello_world_jaxws_binding.wsdl"));
        env.put(ToolConstants.CFG_PACKAGENAME, "org.objectweb");
        
        processor.setEnvironment(env);
        processor.process();

        assertNotNull(output);

        File org = new File(output, "org");
        assertTrue(org.exists());
        File objectweb = new File(org, "objectweb");
        assertTrue(objectweb.exists());

        File[] files = objectweb.listFiles();
        assertEquals(9, files.length);
    }

    private String getLocation(String wsdlFile) {
        return WSDLToJavaProcessorTest.class.getResource(wsdlFile).getFile();
    }
}
