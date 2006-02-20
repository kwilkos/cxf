package org.objectweb.celtix.tools.processors.wsdl2;

import java.io.*;

import org.objectweb.celtix.tools.WSDLToJava;
import org.objectweb.celtix.tools.processors.ProcessorTestBase;

public class WSDLToJavaProcessorTest extends ProcessorTestBase {

    public void testHelloWorld() throws Exception {
        String[] args = new String[]{"-d", output.getCanonicalPath(),
                                     getLocation("/wsdl/hello_world.wsdl")};
        WSDLToJava.main(args);


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
        assertEquals(6, files.length);
        files = types.listFiles();
        assertEquals(files.length, 17);
    }

    public void testHelloWorldRPCLit() throws Exception {
        String[] args = new String[]{"-d", output.getCanonicalPath(),
                                     getLocation("/wsdl/hello_world_rpc_lit.wsdl")};
        WSDLToJava.main(args);

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
        assertEquals(3, files.length);
        files = types.listFiles();
        assertEquals(files.length, 3);
    }

    public void testDocLitHolder() throws Exception {
        String[] args = new String[]{"-d", output.getCanonicalPath(),
                                     getLocation("/wsdl/mapping-doc-literal.wsdl")};
        WSDLToJava.main(args);

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
        assertEquals(6, files.length);
    }

    public void testSchemaImport() throws Exception {
        String[] args = new String[]{"-d", output.getCanonicalPath(),
                                     getLocation("/wsdl/hello_world_schema_import.wsdl")};
        WSDLToJava.main(args);

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
        assertEquals(4, files.length);
        files = types.listFiles();
        assertEquals(files.length, 10);
    }
    
    public void testExceptionNameCollision() throws Exception {
        String[] args = new String[]{"-d", output.getCanonicalPath(),
                                     getLocation("/wsdl/InvoiceServer.wsdl")};
        WSDLToJava.main(args);


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
        assertEquals(12, files.length);
        files = invoice.listFiles();
        assertEquals(files.length, 9);
    }

    public void testAllNameCollision() throws Exception {
        String[] args = new String[]{"-d", output.getCanonicalPath(),
                                     "-p", "org.objectweb",
                                     getLocation("/wsdl/hello_world_collision.wsdl")};
        WSDLToJava.main(args);

        assertNotNull(output);

        File org = new File(output, "org");
        assertTrue(org.exists());
        File objectweb = new File(org, "objectweb");
        assertTrue(objectweb.exists());

        File[] files = objectweb.listFiles();
        assertEquals(13, files.length);

        File typeCollision = new File(objectweb, "Greeter_Type.java");
        assertTrue(typeCollision.exists());
        File exceptionCollision = new File(objectweb, "Greeter_Exception.java");
        assertTrue(exceptionCollision.exists());
        File serviceCollision = new File(objectweb, "Greeter_Service.java");
        assertTrue(serviceCollision.exists());
    }

    public void testHelloWorldAsync() throws Exception {
        String[] args = new String[]{"-d", output.getCanonicalPath(),
                                     "-p", "org.objectweb",
                                     getLocation("/wsdl/hello_world_async.wsdl")};
        WSDLToJava.main(args);

        assertNotNull(output);

        File org = new File(output, "org");
        assertTrue(org.exists());
        File objectweb = new File(org, "objectweb");
        assertTrue(objectweb.exists());

        File[] files = objectweb.listFiles();
        assertEquals(6, files.length);
    }

    public void testHelloWorldExternalBindingFile() throws Exception {
        String[] args = new String[]{"-d", output.getCanonicalPath(),
                                     "-p", "org.objectweb",
                                     "-b", getLocation("/wsdl/hello_world_jaxws_binding.wsdl"),
                                     getLocation("/wsdl/hello_world_jaxws_base.wsdl")};
        WSDLToJava.main(args);

        assertNotNull(output);

        File org = new File(output, "org");
        assertTrue(org.exists());
        File objectweb = new File(org, "objectweb");
        assertTrue(objectweb.exists());

        File[] files = objectweb.listFiles();
        assertEquals(6, files.length);
    }

    public void testSoapHeader() throws Exception {
        String[] args = new String[]{"-d", output.getCanonicalPath(),
                                     "-p", "org.objectweb",
                                     getLocation("/wsdl/soap_header.wsdl")};
        WSDLToJava.main(args);

        assertNotNull(output);

        File org = new File(output, "org");
        assertTrue(org.exists());
        File objectweb = new File(org, "objectweb");
        assertTrue(objectweb.exists());

        File[] files = objectweb.listFiles();
        assertEquals(11, files.length);
    }


    public void testNamespacePackageMapping1() throws Exception {
        String[] args = new String[]{"-d", output.getCanonicalPath(),
                                     "-p",
                                     "org.objectweb",
                                     "-p",
                                     "http://objectweb.org/hello_world_soap_http/types=org.objectweb.types",
                                     getLocation("/wsdl/hello_world.wsdl")};
        WSDLToJava.main(args);

        assertNotNull(output);
        
        File org = new File(output, "org");
        assertTrue(org.exists());
        File objectweb = new File(org, "objectweb");
        assertTrue(objectweb.exists());
        File types = new File(objectweb, "types");
        assertTrue(types.exists());

        File[] files = objectweb.listFiles();
        assertEquals(6, files.length);
        files = types.listFiles();
        assertEquals(17, files.length);
    }

    public void testNamespacePackageMapping2() throws Exception {
        String[] args = new String[]{"-d", output.getCanonicalPath(),
                                     "-p", "org.objectweb",
                                     "-p", "http://objectweb.org/hello_world_soap_http=com.iona",
                                     getLocation("/wsdl/hello_world.wsdl")};
        WSDLToJava.main(args);
        
        assertNotNull(output);
        
        File org = new File(output, "org");
        assertTrue(org.exists());
        File objectweb = new File(org, "objectweb");
        assertTrue(objectweb.exists());

        File[] files = objectweb.listFiles();
        assertEquals(17, files.length);
        
        File com = new File(output, "com");
        assertTrue(com.exists());
        File iona = new File(com, "iona");
        assertTrue(iona.exists());

        files = iona.listFiles();
        assertEquals(5, files.length);
    }

    public void testNamespacePackageMapping3() throws Exception {
        String[] args = new String[]{"-d", output.getCanonicalPath(),
                                     "-p", "http://objectweb.org/hello_world_soap_http=com.iona",
                                     getLocation("/wsdl/hello_world.wsdl")};
        WSDLToJava.main(args);
        
        assertNotNull(output);
        
        File org = new File(output, "org");
        assertTrue(org.exists());
        File objectweb = new File(org, "objectweb");
        assertTrue(objectweb.exists());
        File helloworldsoaphttp = new File(objectweb, "hello_world_soap_http");
        assertTrue(helloworldsoaphttp.exists());
        File types = new File(helloworldsoaphttp, "types");
        assertTrue(types.exists());
        File[] files = types.listFiles();
        assertEquals(files.length, 17);
        
        File com = new File(output, "com");
        assertTrue(com.exists());
        File iona = new File(com, "iona");
        assertTrue(iona.exists());

        files = iona.listFiles();
        assertEquals(5, files.length);
    }

    public void testExternalJaxbBinding() throws Exception {
        String[] args = new String[]{"-d", output.getCanonicalPath(),
                                     "-b", getLocation("/wsdl/hello_world_schema_import.xjb"),
                                     getLocation("/wsdl/hello_world_schema_import.wsdl")};
        WSDLToJava.main(args);
        
        assertNotNull(output);

        File org = new File(output, "org");
        assertTrue(org.exists());
        File objectweb = new File(org, "objectweb");
        assertTrue(objectweb.exists());
        File[] files = objectweb.listFiles();
        assertEquals(11, files.length);
        File helloworldsoaphttp = new File(objectweb, "hello_world_soap_http");
        assertTrue(helloworldsoaphttp.exists());
        files = helloworldsoaphttp.listFiles();
        assertEquals(3, files.length);
    }

    public void testWSAddress() throws Exception {
        String[] args = new String[]{"-d", output.getCanonicalPath(),
                                     "-p", "ws.address",
                                     "-b", getLocation("/wsdl/ws_address_binding.wsdl"),
                                     getLocation("/wsdl/hello_world_addr.wsdl")};
        WSDLToJava.main(args);

        assertNotNull(output);
        
        File ws = new File(output, "ws");
        assertTrue(ws.exists());
        File address = new File(ws, "address");
        assertTrue(address.exists());

        File[] files = address.listFiles();
        assertEquals(14, files.length);
        File handlerConfig = new File(address, "Greeter_handler.xml");
        assertTrue(handlerConfig.exists());
    }

    private String getLocation(String wsdlFile) {
        return WSDLToJavaProcessorTest.class.getResource(wsdlFile).getFile();
    }
}
