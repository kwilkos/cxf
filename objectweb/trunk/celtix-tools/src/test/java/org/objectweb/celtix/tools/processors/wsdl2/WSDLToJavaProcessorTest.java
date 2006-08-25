package org.objectweb.celtix.tools.processors.wsdl2;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URLClassLoader;

import javax.jws.HandlerChain;
import javax.jws.Oneway;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.xml.ws.Holder;
import javax.xml.ws.RequestWrapper;
import javax.xml.ws.ResponseWrapper;
import javax.xml.ws.WebFault;

import org.objectweb.celtix.tools.WSDLToJava;
import org.objectweb.celtix.tools.common.ToolConstants;
import org.objectweb.celtix.tools.processors.ProcessorTestBase;
import org.objectweb.celtix.tools.utils.AnnotationUtil;


public class WSDLToJavaProcessorTest extends ProcessorTestBase {
    private WSDLToJavaProcessor processor = new WSDLToJavaProcessor();
    private URLClassLoader classLoader;

    public void setUp() throws Exception {
        super.setUp();
        File classFile = new java.io.File(output.getCanonicalPath() + "/classes");
        classFile.mkdir();
        System.setProperty("java.class.path", getClassPath() + classFile.getCanonicalPath()
                                              + File.separatorChar);
        classLoader = AnnotationUtil.getClassLoader(Thread.currentThread().getContextClassLoader());
        //env.put(ToolConstants.CFG_VERBOSE, ToolConstants.CFG_VERBOSE);
        env.put(ToolConstants.CFG_COMPILE, "compile");
        env.put(ToolConstants.CFG_OUTPUTDIR, output.getCanonicalPath());
        env.put(ToolConstants.CFG_CLASSDIR, output.getCanonicalPath() + "/classes");
    }

    public void tearDown() {
        super.tearDown();
        processor = null;

    }

    public void testRPCLit() throws Exception {
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
        assertEquals(3, files.length);
        files = types.listFiles();
        assertEquals(files.length, 3);



        Class clz = classLoader.loadClass("org.objectweb.hello_world_rpclit.GreeterRPCLit");

        javax.jws.WebService ws = AnnotationUtil.getPrivClassAnnotation(clz, javax.jws.WebService.class);
        assertTrue("Webservice annotation wsdlLocation should begin with file",
                   ws.wsdlLocation().startsWith("file"));

        SOAPBinding soapBindingAnno = AnnotationUtil.getPrivClassAnnotation(clz, SOAPBinding.class);
        assertEquals("LITERAL", soapBindingAnno.use().toString());
        assertEquals("RPC", soapBindingAnno.style().toString());

        assertEquals("Generate operation error", 3, clz.getMethods().length);

        Class paraClass = classLoader.loadClass("org.objectweb.hello_world_rpclit.types.MyComplexStruct");
        Method method = clz.getMethod("sendReceiveData", new Class[] {paraClass});
        assertEquals("MyComplexStruct", method.getReturnType().getSimpleName());
    }

    public void testAsynMethod() throws Exception {
        env.put(ToolConstants.CFG_WSDLURL, getLocation("/wsdl/hello_world_async.wsdl"));
        processor.setEnvironment(env);
        processor.process();

        assertNotNull(output);

        File org = new File(output, "org");
        assertTrue(org.exists());
        File objectweb = new File(org, "objectweb");
        assertTrue(objectweb.exists());
        File async = new File(objectweb, "hello_world_async_soap_http");
        assertTrue(async.exists());

        File[] files = async.listFiles();
        assertEquals(3, files.length);

        Class clz = classLoader.loadClass("org.objectweb.hello_world_async_soap_http.GreeterAsync");


        Method method1 = clz.getMethod("greetMeSometimeAsync", new Class[] {java.lang.String.class,
                                                                            javax.xml.ws.AsyncHandler.class});
        WebMethod webMethodAnno1 = AnnotationUtil.getPrivMethodAnnotation(method1, WebMethod.class);

        assertEquals(method1.getName() + "()" + " Annotation : WebMethod.operationName ", "greetMeSometime",
                     webMethodAnno1.operationName());

        java.lang.reflect.Method method2 = clz.getMethod("greetMeSometimeAsync",
                                                         new Class[] {java.lang.String.class});
        WebMethod webMethodAnno2 = AnnotationUtil.getPrivMethodAnnotation(method2, WebMethod.class);
        assertEquals(method2.getName() + "()" + " Annotation : WebMethod.operationName ", "greetMeSometime",
                     webMethodAnno2.operationName());

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
        assertEquals(6, files.length);
        files = types.listFiles();
        assertEquals(17, files.length);

        Class clz = classLoader.loadClass("org.objectweb.hello_world_soap_http.Greeter");
        assertTrue("class " + clz.getName() + " modifier is not public", Modifier
            .isPublic(clz.getModifiers()));
        assertTrue("class " + clz.getName() + " modifier is interface", Modifier.isInterface(clz
            .getModifiers()));

        WebService webServiceAnn = AnnotationUtil.getPrivClassAnnotation(clz, WebService.class);
        assertEquals("Greeter", webServiceAnn.name());

        Method method = clz.getMethod("sayHi", new Class[] {});
        WebMethod webMethodAnno = AnnotationUtil.getPrivMethodAnnotation(method, WebMethod.class);
        assertEquals(method.getName() + "()" + " Annotation : WebMethod.operationName ", "sayHi",
                     webMethodAnno.operationName());

        RequestWrapper requestWrapperAnn = AnnotationUtil.getPrivMethodAnnotation(method,
                                                                                  RequestWrapper.class);

        assertEquals("org.objectweb.hello_world_soap_http.types.SayHi", requestWrapperAnn.className());

        ResponseWrapper resposneWrapperAnn = AnnotationUtil.getPrivMethodAnnotation(method,
                                                                                    ResponseWrapper.class);

        assertEquals("sayHiResponse", resposneWrapperAnn.localName());

        WebResult webResultAnno = AnnotationUtil.getPrivMethodAnnotation(method, WebResult.class);

        assertEquals("responseType", webResultAnno.name());

        method = clz.getMethod("greetMe", new Class[] {String.class});
        assertEquals("String", method.getReturnType().getSimpleName());
        WebParam webParamAnn = AnnotationUtil.getWebParam(method, "requestType");
        assertEquals("http://objectweb.org/hello_world_soap_http/types", webParamAnn.targetNamespace());

        method = clz.getMethod("greetMeOneWay", new Class[] {String.class});
        Oneway oneWayAnn = AnnotationUtil.getPrivMethodAnnotation(method, Oneway.class);
        assertNotNull("OneWay Annotation is not generated", oneWayAnn);
        assertEquals("void", method.getReturnType().getSimpleName());

        method = clz.getMethod("greetMeSometime", new Class[] {String.class});
        assertEquals("String", method.getReturnType().getSimpleName());

        method = clz.getMethod("testDocLitFault", new Class[] {java.lang.String.class});
        assertEquals("void", method.getReturnType().getSimpleName());
        assertEquals("Exception class is not generated ", 2, method.getExceptionTypes().length);

        method = clz.getMethod("testDocLitBare", new Class[] {java.lang.String.class});
        webResultAnno = AnnotationUtil.getPrivMethodAnnotation(method, WebResult.class);
        assertEquals("out", webResultAnno.partName());
        SOAPBinding soapBindingAnno = AnnotationUtil.getPrivMethodAnnotation(method, SOAPBinding.class);
        assertEquals("BARE", soapBindingAnno.parameterStyle().toString());
        assertEquals("BareDocumentResponse", method.getReturnType().getSimpleName());

    }



    public void testDocLitHolder() throws Exception {
        env.put(ToolConstants.CFG_WSDLURL, getLocation("/wsdl/mapping-doc-literal.wsdl"));
        processor.setEnvironment(env);
        processor.process();

        assertNotNull(output);

        File org = new File(output, "org");
        assertTrue(org.exists());
        File objectweb = new File(org, "objectweb");
        assertTrue(objectweb.exists());
        File mapping = new File(objectweb, "mapping");
        assertTrue(mapping.exists());
        File[] files = mapping.listFiles();
        assertEquals(6, files.length);

        Class clz = classLoader.loadClass("org.objectweb.mapping.SomethingServer");
        Method method = clz.getMethod("doSomething", new Class[] {int.class, javax.xml.ws.Holder.class,
                                                                  javax.xml.ws.Holder.class});
        assertEquals("boolean", method.getReturnType().getSimpleName());
        WebParam webParamAnno = AnnotationUtil.getWebParam(method, "y");
        assertEquals("INOUT", webParamAnno.mode().name());
        webParamAnno = AnnotationUtil.getWebParam(method, "z");
        assertEquals("OUT", webParamAnno.mode().name());

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
        File[] files = objectweb.listFiles();
        assertEquals(2, files.length);
        File helloworldsoaphttp = new File(objectweb, "hello_world_soap_http");
        assertTrue(helloworldsoaphttp.exists());
        File types = new File(helloworldsoaphttp, "types");
        assertTrue(types.exists());
        files = helloworldsoaphttp.listFiles();
        assertEquals(1, files.length);
        files = types.listFiles();
        assertEquals(files.length, 10);
        File schemaImport = new File(objectweb, "schema_import");
        assertTrue(schemaImport.exists());
        files = schemaImport.listFiles();
        assertEquals(3, files.length);

        Class clz = classLoader.loadClass("org.objectweb.schema_import.Greeter");
        assertEquals(4, clz.getMethods().length);

        Method method = clz.getMethod("pingMe", new Class[] {});
        assertEquals("void", method.getReturnType().getSimpleName());
        assertEquals("Exception class is not generated ", 1, method.getExceptionTypes().length);

    }

    public void testExternalJaxbBinding() throws Exception {
        String[] args = new String[] {"-d", output.getCanonicalPath(), "-b",
                                      getLocation("/wsdl/hello_world_schema_import.xjb"),
                                      getLocation("/wsdl/hello_world_schema_import.wsdl")};
        WSDLToJava.main(args);

        assertNotNull(output);

        File org = new File(output, "org");
        assertTrue(org.exists());
        File objectweb = new File(org, "objectweb");
        assertTrue(objectweb.exists());
        File[] files = objectweb.listFiles();
        assertEquals(11, files.length);
        File schemaImport = new File(objectweb, "schema_import");
        assertTrue(schemaImport.exists());
        files = schemaImport.listFiles();
        assertEquals(3, files.length);
    }

    public void testExceptionNameCollision() throws Exception {
        env.put(ToolConstants.CFG_WSDLURL, getLocation("/wsdl/InvoiceServer.wsdl"));
        processor.setEnvironment(env);
        processor.process();

        assertNotNull(output);

        File org = new File(output, "org");
        assertTrue(org.exists());
        File objectweb = new File(org, "objectweb");
        assertTrue(objectweb.exists());
        File invoiceserver = new File(objectweb, "invoiceserver");
        assertTrue(invoiceserver.exists());
        File invoice = new File(objectweb, "invoice");
        assertTrue(invoice.exists());

        File exceptionCollision = new File(invoiceserver, "NoSuchCustomerFault_Exception.java");
        assertTrue(exceptionCollision.exists());

        File[] files = invoiceserver.listFiles();
        assertEquals(12, files.length);
        files = invoice.listFiles();
        assertEquals(files.length, 9);

        Class clz = classLoader.loadClass("org.objectweb.invoiceserver.InvoiceServer");
        assertEquals(3, clz.getMethods().length);

        Method method = clz.getMethod("getInvoicesForCustomer", new Class[] {String.class, String.class});
        assertEquals("NoSuchCustomerFault_Exception", method.getExceptionTypes()[0].getSimpleName());

    }

    public void testAllNameCollision() throws Exception {
        env.put(ToolConstants.CFG_WSDLURL, getLocation("/wsdl/hello_world_collision.wsdl"));
        env.setPackageName("org.objectweb");
        processor.setEnvironment(env);
        processor.process();

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

        Class clz = classLoader.loadClass("org.objectweb.Greeter");
        assertTrue("SEI class Greeter modifier should be interface", clz.isInterface());

        clz = classLoader.loadClass("org.objectweb.Greeter_Exception");
        clz = classLoader.loadClass("org.objectweb.Greeter_Service");
    }

    public void testImportNameCollision() throws Exception {
        env.put(ToolConstants.CFG_WSDLURL, getLocation("/wsdl/helloworld-portname_servicename.wsdl"));
        env.setPackageName("org.objectweb");
        processor.setEnvironment(env);
        processor.process();

        assertNotNull(output);

        File org = new File(output, "org");
        assertTrue(org.exists());
        File objectweb = new File(org, "objectweb");
        assertTrue(objectweb.exists());

        File[] files = objectweb.listFiles();
        assertEquals(3, files.length);

        File serviceCollision = new File(objectweb, "HelloWorldServiceImpl_Service.java");
        assertTrue(serviceCollision.exists());

        Class clz = classLoader.loadClass("org.objectweb.HelloWorldServiceImpl");
        assertTrue("SEI class HelloWorldServiceImpl modifier should be interface", clz.isInterface());

        clz = classLoader.loadClass("org.objectweb.HelloWorldServiceImpl_Service");
    }

    public void testHelloWorldExternalBindingFile() throws Exception {
        env.put(ToolConstants.CFG_WSDLURL, getLocation("/wsdl/hello_world_jaxws_base.wsdl"));
        env.put(ToolConstants.CFG_BINDING, getLocation("/wsdl/hello_world_jaxws_binding.wsdl"));
        processor.setEnvironment(env);
        processor.process();

        assertNotNull(output);

        File org = new File(output, "org");
        assertTrue(org.exists());
        File objectweb = new File(org, "objectweb");
        assertTrue(objectweb.exists());

        // File[] files = objectweb.listFiles();
        // assertEquals(6, files.length);

        Class clz = classLoader.loadClass("org.objectweb.hello_world_async_soap_http.GreeterAsync");
        assertEquals(3, clz.getMethods().length);

    }

    public void testSoapHeader() throws Exception {
        env.put(ToolConstants.CFG_WSDLURL, getLocation("/wsdl/soap_header.wsdl"));
        env.setPackageName("org.objectweb");
        processor.setEnvironment(env);
        processor.process();

        assertNotNull(output);

        File org = new File(output, "org");
        assertTrue(org.exists());
        File objectweb = new File(org, "objectweb");
        assertTrue(objectweb.exists());

        File[] files = objectweb.listFiles();
        assertEquals(11, files.length);

        Class clz = classLoader.loadClass("org.objectweb.HeaderTester");
        assertEquals(3, clz.getMethods().length);

        SOAPBinding soapBindingAnno = AnnotationUtil.getPrivClassAnnotation(clz, SOAPBinding.class);
        assertEquals("BARE", soapBindingAnno.parameterStyle().name());
        assertEquals("LITERAL", soapBindingAnno.use().name());
        assertEquals("DOCUMENT", soapBindingAnno.style().name());

        Class para = classLoader.loadClass("org.objectweb.InoutHeader");

        Method method = clz.getMethod("inoutHeader", new Class[] {para, Holder.class});

        soapBindingAnno = AnnotationUtil.getPrivMethodAnnotation(method, SOAPBinding.class);
        assertEquals("BARE", soapBindingAnno.parameterStyle().name());
        WebParam webParamAnno = AnnotationUtil.getWebParam(method, "SOAPHeaderInfo");
        assertEquals("INOUT", webParamAnno.mode().name());
        assertEquals(true, webParamAnno.header());
        assertEquals("header_info", webParamAnno.partName());

    }

    public void testHolderHeader() throws Exception {
        env.put(ToolConstants.CFG_WSDLURL, getLocation("/wsdl/hello_world_holder.wsdl"));
        processor.setEnvironment(env);
        processor.process();

        Class clz = classLoader.loadClass("org.objectweb.hello_world_holder.Greeter");
        assertEquals(1, clz.getMethods().length);

        SOAPBinding soapBindingAnno = AnnotationUtil.getPrivClassAnnotation(clz, SOAPBinding.class);
        assertEquals("BARE", soapBindingAnno.parameterStyle().name());
        assertEquals("LITERAL", soapBindingAnno.use().name());
        assertEquals("DOCUMENT", soapBindingAnno.style().name());

        Class para = classLoader.loadClass("org.objectweb.hello_world_holder.types.GreetMe");
        Method method = clz.getMethod("sayHi", new Class[] {para, Holder.class});
        assertEquals("GreetMeResponse", method.getReturnType().getSimpleName());

        WebParam webParamAnno = AnnotationUtil.getWebParam(method, "greetMe");
        assertEquals(true, webParamAnno.header());

        webParamAnno = AnnotationUtil.getWebParam(method, "sayHi");
        assertEquals("INOUT", webParamAnno.mode().name());

    }

    public void testNamespacePackageMapping1() throws Exception {
        env.setPackageName("org.celtix");
        env.addNamespacePackageMap("http://objectweb.org/hello_world_soap_http/types", "org.objectweb.types");
        env.put(ToolConstants.CFG_WSDLURL, getLocation("/wsdl/hello_world.wsdl"));
        processor.setEnvironment(env);
        processor.process();

        assertNotNull(output);

        File org = new File(output, "org");
        assertTrue(org.exists());
        File objectweb = new File(org, "objectweb");
        assertTrue(objectweb.exists());
        File types = new File(objectweb, "types");
        assertTrue(types.exists());

        File[] files = objectweb.listFiles();
        assertEquals(1, files.length);
        files = types.listFiles();
        assertEquals(17, files.length);

        Class clz = classLoader.loadClass("org.celtix.Greeter");
        assertTrue("Generate " + clz.getName() + "error", clz.isInterface());
        clz = classLoader.loadClass("org.objectweb.types.GreetMe");
    }


    public void testNamespacePackageMapping2() throws Exception {
        env.setPackageName("org.celtix");
        env.addNamespacePackageMap("http://objectweb.org/hello_world_soap_http", "org.objectweb");
        env.put(ToolConstants.CFG_WSDLURL, getLocation("/wsdl/hello_world.wsdl"));
        processor.setEnvironment(env);
        processor.process();

        assertNotNull(output);

        File org = new File(output, "org");
        assertTrue(org.exists());
        File objectweb = new File(org, "objectweb");
        assertTrue(objectweb.exists());
        File celtix = new File(org, "celtix");
        assertTrue(celtix.exists());

        File[] files = objectweb.listFiles();
        assertEquals(5, files.length);
        files = celtix.listFiles();
        assertEquals(17, files.length);

        Class clz = classLoader.loadClass("org.celtix.GreetMe");
        assertTrue("Generate " + clz.getName() + "error", Modifier.isPublic(clz.getModifiers()));
        clz = classLoader.loadClass("org.objectweb.Greeter");
    }

    public void testNamespacePackageMapping3() throws Exception {
        env.addNamespacePackageMap("http://objectweb.org/hello_world_soap_http", "org.celtix");
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
        File[] files = types.listFiles();
        assertEquals(files.length, 17);

        File celtix = new File(org, "celtix");
        files = celtix.listFiles();
        assertEquals(5, files.length);

        Class clz = classLoader.loadClass("org.celtix.Greeter");
        assertTrue("Generate " + clz.getName() + "error", clz.isInterface());
    }

    public void testWSAddress() throws Exception {
        env.addNamespacePackageMap("http://objectweb.org/hello_world_soap_http", "ws.address");
        env.put(ToolConstants.CFG_BINDING, getLocation("/wsdl/ws_address_binding.wsdl"));
        env.put(ToolConstants.CFG_WSDLURL, getLocation("/wsdl/hello_world_addr.wsdl"));
        processor.setEnvironment(env);
        processor.process();

        assertNotNull(output);

        File ws = new File(output, "ws");
        assertTrue(ws.exists());
        File address = new File(ws, "address");
        assertTrue(address.exists());

        File[] files = address.listFiles();
        assertEquals(4, files.length);
        File handlerConfig = new File(address, "Greeter_handler.xml");
        assertTrue(handlerConfig.exists());

        Class clz = classLoader.loadClass("ws.address.Greeter");
        HandlerChain handlerChainAnno = AnnotationUtil.getPrivClassAnnotation(clz, HandlerChain.class);
        assertEquals("Greeter_handler.xml", handlerChainAnno.file());
        assertNotNull("Handler chain xml generate fail!", classLoader
            .findResource("ws/address/Greeter_handler.xml"));
    }

    public void testSupportXMLBindingBare() throws Exception {
        env.put(ToolConstants.CFG_WSDLURL, getLocation("/wsdl/xml_http_bare.wsdl"));
        processor.setEnvironment(env);
        processor.process();

    }

    public void testSupportXMLBindingWrapped() throws Exception {
        env.put(ToolConstants.CFG_WSDLURL, getLocation("/wsdl/xml_http_wrapped.wsdl"));
        processor.setEnvironment(env);
        processor.process();
    }

    public void testRouterWSDL() throws Exception {
        env.put(ToolConstants.CFG_WSDLURL, getLocation("/wsdl/router.wsdl"));
        processor.setEnvironment(env);
        processor.process();
    }

    public void testExcludeNSWithPackageName() throws Exception {

        String[] args = new String[] {"-d", output.getCanonicalPath(), "-nexclude",
                                      "http://objectweb.org/test/types=com.iona", "-nexclude",
                                      "http://objectweb.org/Invoice",
                                      getLocation("/wsdl/hello_world_exclude.wsdl")};
        WSDLToJava.main(args);

        assertNotNull(output);
        File com = new File(output, "com");
        assertFalse("Generated file has been excluded", com.exists());
        File iona = new File(com, "iona");
        assertFalse("Generated file has been excluded", iona.exists());

        File org = new File(output, "org");
        File objectweb = new File(org, "objectweb");
        File invoice = new File(objectweb, "Invoice");
        assertFalse("Generated file has been excluded", invoice.exists());

    }

    public void testExcludeNSWithoutPackageName() throws Exception {

        String[] args = new String[] {"-d", output.getCanonicalPath(), "-nexclude",
                                      "http://objectweb.org/test/types",
                                      getLocation("/wsdl/hello_world_exclude.wsdl")};
        WSDLToJava.main(args);

        assertNotNull(output);
        File com = new File(output, "test");
        assertFalse("Generated file has been excluded", com.exists());

    }

    public void testCommandLine() throws Exception {
        String[] args = new String[] {"-compile", "-d", output.getCanonicalPath(), "-classdir",
                                      output.getCanonicalPath() + "/classes", "-p", "org.celtix", "-p",
                                      "http://objectweb.org/hello_world_soap_http/types=org.objectweb.types",
                                      "-client", "-server", "-impl", getLocation("/wsdl/hello_world.wsdl")};
        WSDLToJava.main(args);

        Class clz = classLoader.loadClass("org.celtix.Greeter");
        assertTrue("Generate " + clz.getName() + "error", clz.isInterface());
        clz = classLoader.loadClass("org.objectweb.types.GreetMe");
    }

    public void testDefaultLoadNSMappingOFF() throws Exception {
        String[] args = new String[] {"-dns", "false", "-d", output.getCanonicalPath(),
                                      getLocation("/wsdl/basic_callback.wsdl")};

        WSDLToJava.main(args);

        assertNotNull(output);
        File org = new File(output, "org");
        assertTrue(org.exists());
        File w3 = new File(org, "w3");
        assertTrue(w3.exists());
        File p2005 = new File(w3, "_2005");
        assertTrue(p2005.exists());
        File p08 = new File(p2005, "_08");
        assertTrue(p08.exists());
        File address = new File(p08, "addressing");
        assertTrue(address.exists());

        File[] files = address.listFiles();
        assertEquals(11, files.length);
    }

    public void testDefaultLoadNSMappingON() throws Exception {
        String[] args = new String[] {"-d", output.getCanonicalPath(),
                                      getLocation("/wsdl/basic_callback.wsdl")};

        WSDLToJava.main(args);

        assertNotNull(output);
        File org = new File(output, "org");
        assertTrue(org.exists());
        File objectweb = new File(org, "objectweb");
        assertTrue(objectweb.exists());
        File celtix = new File(objectweb, "celtix");
        assertTrue(celtix.exists());
        File ws = new File(celtix, "ws");
        assertTrue(ws.exists());
        File address = new File(ws, "addressing");
        assertTrue(address.exists());

        File[] files = address.listFiles();
        assertEquals(11, files.length);
    }

    public void testWithNoService() throws Exception {
        env.put(ToolConstants.CFG_WSDLURL, getLocation("/wsdl/helloworld-noservice.wsdl"));
        processor.setEnvironment(env);
        processor.process();
        Class clz = classLoader.loadClass("org.apache.tuscany.samples.helloworldaxis.HelloWorldServiceImpl");
        Method method = clz.getMethod("getGreetings", new Class[] {java.lang.String.class});
        WebResult webResultAnno = AnnotationUtil.getPrivMethodAnnotation(method, WebResult.class);
        assertEquals("http://helloworldaxis.samples.tuscany.apache.org", webResultAnno.targetNamespace());
        assertEquals("response", webResultAnno.partName());
        assertEquals("getGreetingsResponse", webResultAnno.name());

        WebParam webParamAnno = AnnotationUtil.getWebParam(method, "getGreetings");
        assertEquals("http://helloworldaxis.samples.tuscany.apache.org", webParamAnno.targetNamespace());
        assertEquals("request", webParamAnno.partName());
        assertEquals("getGreetings", webParamAnno.name());

    }

    public void testWithNoBinding() throws Exception {
        env.put(ToolConstants.CFG_WSDLURL, getLocation("/wsdl/helloworld-nobinding.wsdl"));
        processor.setEnvironment(env);
        processor.process();
        Class clz = classLoader.loadClass("org.apache.tuscany.samples.helloworldaxis.HelloWorldServiceImpl");

        SOAPBinding soapBindingAnno = AnnotationUtil.getPrivClassAnnotation(clz, SOAPBinding.class);
        assertNull("SOAPBinding Annotation should be null", soapBindingAnno);

        Method method = clz.getMethod("getGreetings", new Class[] {java.lang.String.class});
        WebResult webResultAnno = AnnotationUtil.getPrivMethodAnnotation(method, WebResult.class);
        assertEquals("http://helloworldaxis.samples.tuscany.apache.org", webResultAnno.targetNamespace());
        assertEquals("response", webResultAnno.partName());
        assertEquals("getGreetingsResponse", webResultAnno.name());

        WebParam webParamAnno = AnnotationUtil.getWebParam(method, "getGreetings");
        assertNotNull("WebParam should be annotated", webParamAnno);

    }

    public void testVoidInOutMethod() throws Exception {
        env.put(ToolConstants.CFG_WSDLURL, getLocation("/wsdl/interoptestdoclit.wsdl"));
        processor.setEnvironment(env);
        processor.process();

        assertNotNull(output);

        File org = new File(output, "org");
        assertTrue(org.exists());
        File soapinterop = new File(org, "soapinterop");
        assertTrue(soapinterop.exists());
        File wsdlinterop = new File(soapinterop, "wsdlinteroptestdoclit");
        assertTrue(wsdlinterop.exists());
        File xsd = new File(soapinterop, "xsd");
        assertTrue(xsd.exists());
        File[] files = wsdlinterop.listFiles();
        assertEquals(3, files.length);
        files = xsd.listFiles();
        assertEquals(4, files.length);

        Class clz = classLoader
            .loadClass("org.soapinterop.wsdlinteroptestdoclit.WSDLInteropTestDocLitPortType");

        Method method = clz.getMethod("echoVoid", new Class[] {});
        WebMethod webMethodAnno = AnnotationUtil.getPrivMethodAnnotation(method, WebMethod.class);
        assertEquals(method.getName() + "()" + " Annotation : WebMethod.operationName ", "echoVoid",
                     webMethodAnno.operationName());
    }

    public void testWsdlImport() throws Exception {
        env.put(ToolConstants.CFG_WSDLURL, getLocation("/wsdl/hello_world_wsdl_import.wsdl"));
        processor.setEnvironment(env);
        processor.process();

        assertNotNull(output);

        File org = new File(output, "org");
        assertTrue(org.exists());

        File objectweb = new File(org, "objectweb");
        assertTrue(objectweb.exists());

        File helloWorld = new File(objectweb, "hello_world");
        assertTrue(helloWorld.exists());


        Class clz = classLoader.loadClass("org.objectweb.hello_world.Greeter");
        assertEquals(3, clz.getMethods().length);

        Method method = clz.getMethod("pingMe", new Class[] {});
        assertEquals("void", method.getReturnType().getSimpleName());
        assertEquals("Exception class is not generated ", 1, method.getExceptionTypes().length);
        assertEquals("org.objectweb.hello_world.messages.PingMeFault", method.getExceptionTypes()[0]
            .getCanonicalName());
    }

    public void testWebFault() throws Exception {
        env.put(ToolConstants.CFG_WSDLURL, getLocation("/wsdl/InvoiceServer-issue305570.wsdl"));
        processor.setEnvironment(env);
        processor.process();


        assertNotNull(output);

        File org = new File(output, "org");
        assertTrue(org.exists());
        File objectweb = new File(org, "objectweb");
        assertTrue(objectweb.exists());
        File invoiceserver = new File(objectweb, "invoiceserver");
        assertTrue(invoiceserver.exists());
        File invoice = new File(objectweb, "invoice");
        assertTrue(invoice.exists());

        Class clz = classLoader.loadClass("org.objectweb.invoiceserver.NoSuchCustomerFault");
        WebFault webFault = AnnotationUtil.getPrivClassAnnotation(clz, WebFault.class);
        assertEquals("WebFault annotaion name attribute error", "NoSuchCustomer", webFault.name());

    }


    public void testMultiSchemaParsing() throws Exception {
        String[] args = new String[] {"-d", output.getCanonicalPath(),
                                      getLocation("/wsdl/multi_schema.wsdl")};
        WSDLToJava.main(args);

        assertNotNull(output);
        File org = new File(output, "org");
        assertTrue(org.exists());
        File tempuri = new File(org, "tempuri");
        assertTrue(tempuri.exists());
        File header = new File(tempuri, "header");
        assertTrue(header.exists());

        File[] files = header.listFiles();
        assertEquals(3, files.length);
    }



    public void testBug305728HelloWorld() {
        try {
            String[] args = new String[] {"-compile", "-classdir", output.getCanonicalPath() + "/classes",
                                          "-d",
                                          output.getCanonicalPath(),
                                          "-nexclude",
                                          "http://www.w3.org/2005/08/addressing"
                                              + "=org.objectweb.celtix.ws.addressing",
                                          getLocation("/wsdl/bug305728/hello_world.wsdl")};
            WSDLToJava.main(args);
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }

        try {
            File file = new File(output.getCanonicalPath(),
                                 "org/objectweb/celtix/ws/addressing/EndpointReferenceType.java");

            assertFalse("Exclude java file should not be generated : " + file.getCanonicalPath(),
                        file.exists());

            file = new File(output.getCanonicalPath() + File.separator + "classes",
                            "org/objectweb/celtix/ws/addressing/EndpointReferenceType.class");
            assertFalse("Exclude class should not be generated : " + file.getCanonicalPath(), file.exists());


            file = new File(output.getCanonicalPath(),
                            "org/w3/_2005/_08/addressing/EndpointReferenceType.java");
            assertFalse("Exclude file should not be generated : " + file.getCanonicalPath(), file.exists());

            file = new File(output.getCanonicalPath() + File.separator + "classes",
                            "org/w3/_2005/_08/addressing/EndpointReferenceType.class");
            assertFalse("Exclude class should not be generated : " + file.getCanonicalPath(), file.exists());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void testBug305728HelloWorld2() {
        try {
            String[] args = new String[] {"-compile", "-classdir", output.getCanonicalPath() + "/classes",
                                          "-d",
                                          output.getCanonicalPath(),
                                          "-nexclude",
                                          "http://objectweb.org/hello_world/types",
                                          getLocation("/wsdl/bug305728/hello_world2.wsdl")};
            WSDLToJava.main(args);
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            File file = new File(output.getCanonicalPath() , "org/objectweb/hello_world/types");
            assertFalse("Exclude file should not be generated : " + file.getCanonicalPath(), file.exists());

            file = new File(output.getCanonicalPath() + File.separator + "classes",
                            "org/objectweb/hello_world/types");

            assertFalse("Exclude file should not be generated : " + file.getCanonicalPath(), file.exists());

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void testBug305729() {
        env.put(ToolConstants.CFG_WSDLURL, getLocation("/wsdl/bug305729/hello_world.wsdl"));
        processor.setEnvironment(env);
        processor.process();
        assertNotNull("Process message with no part wsdl error", output);
    }

    public void testBug305700() {
        try {
            env.put(ToolConstants.CFG_COMPILE, "compile");
            env.put(ToolConstants.CFG_OUTPUTDIR, output.getCanonicalPath());
            env.put(ToolConstants.CFG_CLASSDIR, output.getCanonicalPath() + "/classes");
            env.put(ToolConstants.CFG_CLIENT, ToolConstants.CFG_CLIENT);
            env.put(ToolConstants.CFG_WSDLURL, getLocation("/wsdl/bug305700/addNumbers.wsdl"));
            processor.setEnvironment(env);
            processor.process();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void testBug305773() {
        try {
            env.put(ToolConstants.CFG_COMPILE, "compile");
            env.put(ToolConstants.CFG_IMPL, ToolConstants.CFG_IMPL);
            env.put(ToolConstants.CFG_OUTPUTDIR, output.getCanonicalPath());
            env.put(ToolConstants.CFG_CLASSDIR, output.getCanonicalPath() + "/classes");
            env.put(ToolConstants.CFG_WSDLURL, getLocation("/wsdl/bug305773/hello_world.wsdl"));
            processor.setEnvironment(env);
            processor.process();
            Class clz = classLoader.loadClass("org.objectweb.hello_world_soap_http.GreeterImpl");


            WebService webServiceAnn = AnnotationUtil.getPrivClassAnnotation(clz, WebService.class);
            assertEquals("Greeter", webServiceAnn.name());
            assertFalse("Impl class should generate portName property value in webService annotation"
                        , webServiceAnn.portName().equals(""));
            assertFalse("Impl class should generate serviceName property value in webService annotation"
                        , webServiceAnn.serviceName().equals(""));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void testBug305772() {
        try {
            env.put(ToolConstants.CFG_COMPILE, "compile");
            env.put(ToolConstants.CFG_ANT, ToolConstants.CFG_ANT);
            env.put(ToolConstants.CFG_OUTPUTDIR, output.getCanonicalPath());
            env.put(ToolConstants.CFG_CLASSDIR, output.getCanonicalPath() + "/classes");
            // env.put(ToolConstants.CFG_CLIENT, ToolConstants.CFG_CLIENT);
            env.put(ToolConstants.CFG_WSDLURL, getLocation("/wsdl/bug305772/hello_world.wsdl"));
            processor.setEnvironment(env);
            processor.process();
            File file = new File(output.getCanonicalPath(), "build.xml");
            FileInputStream fileinput = new FileInputStream(file);
            java.io.BufferedInputStream filebuffer = new java.io.BufferedInputStream(fileinput);
            byte[] buffer = new byte[(int)file.length()];
            filebuffer.read(buffer);
            String content = new String(buffer);
            assertTrue("wsdl location should be url style in build.xml",
                       content.indexOf("param1=\"file:") > -1);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void testBug305770() {
        try {
            env.put(ToolConstants.CFG_COMPILE, "compile");
            env.put(ToolConstants.CFG_OUTPUTDIR, output.getCanonicalPath());
            env.put(ToolConstants.CFG_CLASSDIR, output.getCanonicalPath() + "/classes");
            env.put(ToolConstants.CFG_ALL, ToolConstants.CFG_ALL);
            env.put(ToolConstants.CFG_WSDLURL, getLocation("/wsdl/bug305770/Printer.wsdl"));
            processor.setEnvironment(env);
            processor.process();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void testHangingBug() throws Exception {
        env.put(ToolConstants.CFG_WSDLURL, getLocation("/wsdl/bughanging/wsdl/wsrf.wsdl"));
        processor.setEnvironment(env);
        processor.process();
    }
    
    public void testBug305924ForNestedBinding() {
        try {
            String[] args = new String[] {"-all", "-compile", "-classdir", 
                                          output.getCanonicalPath() + "/classes", 
                                          "-d",
                                          output.getCanonicalPath(),
                                          "-b",
                                          getLocation("/wsdl/bug305924/binding2.xml"),
                                          getLocation("/wsdl/bug305924/hello_world.wsdl")};
            WSDLToJava.main(args);
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }

        try {
            Class clz = 
                classLoader.loadClass("org.objectweb.hello_world_soap_http.types.CreateProcess$MyProcess");
            assertNotNull("Customization binding code should be generated", clz);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
    
    public void testBug305924ForExternalBinding() {
        try {
            String[] args = new String[] {"-all", "-compile", "-classdir", 
                                          output.getCanonicalPath() + "/classes", 
                                          "-d",
                                          output.getCanonicalPath(),
                                          "-b",
                                          getLocation("/wsdl/bug305924/binding1.xml"),
                                          getLocation("/wsdl/bug305924/hello_world.wsdl")};
            WSDLToJava.main(args);
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }

        try {
            Class clz = 
                classLoader.loadClass("org.objectweb.hello_world_soap_http.types.CreateProcess$MyProcess");
            assertNotNull("Customization binding code should be generated", clz);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }



    private String getLocation(String wsdlFile) {
        return WSDLToJavaProcessorTest.class.getResource(wsdlFile).getFile();
    }

}