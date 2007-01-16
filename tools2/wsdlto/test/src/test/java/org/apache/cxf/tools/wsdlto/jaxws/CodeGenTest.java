/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.cxf.tools.wsdlto.jaxws;

import java.io.File;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import javax.jws.Oneway;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.xml.ws.RequestWrapper;
import javax.xml.ws.ResponseWrapper;

import org.apache.cxf.tools.common.ProcessorTestBase;
import org.apache.cxf.tools.common.ToolConstants;
import org.apache.cxf.tools.util.AnnotationUtil;
import org.apache.cxf.tools.wsdlto.WSDLToJavaContainer;

public class CodeGenTest extends ProcessorTestBase {
    private WSDLToJavaContainer processor;
    private ClassLoader classLoader;

    public void setUp() throws Exception {
        super.setUp();
        File classFile = new java.io.File(output.getCanonicalPath() + "/classes");
        classFile.mkdir();
        System.setProperty("java.class.path", getClassPath() + classFile.getCanonicalPath()
                                              + File.separatorChar);
        classLoader = AnnotationUtil.getClassLoader(Thread.currentThread().getContextClassLoader());
        env.put(ToolConstants.CFG_COMPILE, ToolConstants.CFG_COMPILE);
        env.put(ToolConstants.CFG_IMPL, "impl");
        env.put(ToolConstants.CFG_OUTPUTDIR, output.getCanonicalPath());
        env.put(ToolConstants.CFG_CLASSDIR, output.getCanonicalPath() + "/classes");

        processor = new WSDLToJavaContainer("jaxws", null);

    }

    public void tearDown() {
        super.tearDown();
        processor = null;
    }

    public void testRPCLit() throws Exception {

        env.put(ToolConstants.CFG_WSDLURL, getLocation("/wsdl2java_wsdl/hello_world_rpc_lit.wsdl"));
        processor.setContext(env);
        processor.execute(false);

        assertNotNull(output);

        File org = new File(output, "org");
        assertTrue(org.exists());
        File apache = new File(org, "apache");
        assertTrue(apache.exists());
        File helloworldsoaphttp = new File(apache, "hello_world_rpclit");
        assertTrue(helloworldsoaphttp.exists());
        File types = new File(helloworldsoaphttp, "types");
        assertTrue(types.exists());
        File[] files = helloworldsoaphttp.listFiles();
        assertEquals(4, files.length);
        files = types.listFiles();
        assertEquals(files.length, 3);

        Class clz = classLoader.loadClass("org.apache.hello_world_rpclit.GreeterRPCLit");

        javax.jws.WebService ws = AnnotationUtil.getPrivClassAnnotation(clz, javax.jws.WebService.class);
        assertTrue("Webservice annotation wsdlLocation should begin with file", ws.wsdlLocation()
            .startsWith("file"));

        SOAPBinding soapBindingAnno = AnnotationUtil.getPrivClassAnnotation(clz, SOAPBinding.class);
        assertEquals("LITERAL", soapBindingAnno.use().toString());
        assertEquals("RPC", soapBindingAnno.style().toString());

        assertEquals("Generate operation error", 3, clz.getMethods().length);

        Class paraClass = classLoader.loadClass("org.apache.hello_world_rpclit.types.MyComplexStruct");
        Method method = clz.getMethod("sendReceiveData", new Class[] {paraClass});
        assertEquals("MyComplexStruct", method.getReturnType().getSimpleName());

        clz = classLoader.loadClass("org.apache.hello_world_rpclit.GreeterRPCLitImpl");
        assertNotNull(clz);
        ws = AnnotationUtil.getPrivClassAnnotation(clz, javax.jws.WebService.class);
        assertNotNull(ws);
        assertTrue("Webservice annotation wsdlLocation should begin with file", ws.wsdlLocation()
            .startsWith("file"));
        assertEquals("org.apache.hello_world_rpclit.GreeterRPCLit", ws.endpointInterface());
        assertEquals("GreeterRPCLit", ws.name());

    }

    public void testAsynMethod() throws Exception {
        env.put(ToolConstants.CFG_WSDLURL, getLocation("/wsdl2java_wsdl/hello_world_async.wsdl"));
        processor.setContext(env);
        processor.execute(true);

        assertNotNull(output);

        File org = new File(output, "org");
        assertTrue(org.exists());
        File apache = new File(org, "apache");
        assertTrue(apache.exists());
        File async = new File(apache, "hello_world_async_soap_http");
        assertTrue(async.exists());

        File[] files = async.listFiles();
        assertEquals(4, files.length);

        Class clz = classLoader.loadClass("org.apache.hello_world_async_soap_http.GreeterAsync");

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

    public void testHelloWorldSoap12() throws Exception {
        env.put(ToolConstants.CFG_WSDLURL, getLocation("/wsdl2java_wsdl/hello_world_soap12.wsdl"));
        processor.setContext(env);
        processor.execute(true);

        assertNotNull(output);

        File org = new File(output, "org");
        assertTrue(org.exists());
        File apache = new File(org, "apache");
        assertTrue(apache.exists());
        File helloworldsoaphttp = new File(apache, "hello_world_soap12_http");
        assertTrue(helloworldsoaphttp.exists());
        File types = new File(helloworldsoaphttp, "types");
        assertTrue(types.exists());
        File[] files = helloworldsoaphttp.listFiles();
        assertEquals(5, files.length);
        files = types.listFiles();
        assertEquals(7, files.length);

        Class clz = classLoader.loadClass("org.apache.hello_world_soap12_http.Greeter");
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

        assertEquals("org.apache.hello_world_soap12_http.types.SayHi", requestWrapperAnn.className());

        ResponseWrapper resposneWrapperAnn = AnnotationUtil.getPrivMethodAnnotation(method,
                                                                                    ResponseWrapper.class);

        assertEquals("sayHiResponse", resposneWrapperAnn.localName());

        WebResult webResultAnno = AnnotationUtil.getPrivMethodAnnotation(method, WebResult.class);

        assertEquals("responseType", webResultAnno.name());

        method = clz.getMethod("pingMe", new Class[] {});
        webMethodAnno = AnnotationUtil.getPrivMethodAnnotation(method, WebMethod.class);
        assertEquals(method.getName() + "()" + " Annotation : WebMethod.operationName ", "pingMe",
                     webMethodAnno.operationName());
        Class[] exceptionCls = method.getExceptionTypes();
        assertEquals(1, exceptionCls.length);
        assertEquals("org.apache.hello_world_soap12_http.PingMeFault", exceptionCls[0].getName());
    }

    public void testHelloWorld() throws Exception {
        env.put(ToolConstants.CFG_WSDLURL, getLocation("/wsdl2java_wsdl/hello_world.wsdl"));
        processor.setContext(env);
        processor.execute(true);

        assertNotNull(output);

        File org = new File(output, "org");
        assertTrue(org.exists());
        File apache = new File(org, "apache");
        assertTrue(apache.exists());
        File helloworldsoaphttp = new File(apache, "hello_world_soap_http");
        assertTrue(helloworldsoaphttp.exists());
        File types = new File(helloworldsoaphttp, "types");
        assertTrue(types.exists());
        File[] files = helloworldsoaphttp.listFiles();
        assertEquals(6, files.length);
        files = types.listFiles();
        assertEquals(17, files.length);

        Class clz = classLoader.loadClass("org.apache.hello_world_soap_http.Greeter");
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

        assertEquals("org.apache.hello_world_soap_http.types.SayHi", requestWrapperAnn.className());

        ResponseWrapper resposneWrapperAnn = AnnotationUtil.getPrivMethodAnnotation(method,
                                                                                    ResponseWrapper.class);

        assertEquals("sayHiResponse", resposneWrapperAnn.localName());

        WebResult webResultAnno = AnnotationUtil.getPrivMethodAnnotation(method, WebResult.class);

        assertEquals("responseType", webResultAnno.name());

        method = clz.getMethod("greetMe", new Class[] {String.class});
        assertEquals("String", method.getReturnType().getSimpleName());
        WebParam webParamAnn = AnnotationUtil.getWebParam(method, "requestType");
        assertEquals("http://apache.org/hello_world_soap_http/types", webParamAnn.targetNamespace());

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
        env.put(ToolConstants.CFG_WSDLURL, getLocation("/wsdl2java_wsdl/mapping-doc-literal.wsdl"));
        processor.setContext(env);
        processor.execute(true);
        assertNotNull(output);
        File org = new File(output, "org");
        assertTrue(org.exists());
        File apache = new File(org, "apache");
        assertTrue(apache.exists());
        File mapping = new File(apache, "mapping");
        assertTrue(mapping.exists());
        File[] files = mapping.listFiles();
        assertEquals(7, files.length);
        Class clz = classLoader.loadClass("org.apache.mapping.SomethingServer");
        Method method = clz.getMethod("doSomething", new Class[] {int.class, javax.xml.ws.Holder.class,
                                                                  javax.xml.ws.Holder.class});
        assertEquals("boolean", method.getReturnType().getSimpleName());
        WebParam webParamAnno = AnnotationUtil.getWebParam(method, "y");
        assertEquals("INOUT", webParamAnno.mode().name());
        webParamAnno = AnnotationUtil.getWebParam(method, "z");
        assertEquals("OUT", webParamAnno.mode().name());
    }

    public void testSchemaImport() throws Exception {
        env.put(ToolConstants.CFG_WSDLURL, getLocation("/wsdl2java_wsdl/hello_world_schema_import.wsdl"));
        
        processor.setContext(env);
        processor.execute(true);

        assertNotNull(output);

        File org = new File(output, "org");
        assertTrue(org.exists());
        File apache = new File(org, "apache");
        assertTrue(apache.exists());
        File[] files = apache.listFiles();
        assertEquals(2, files.length);
        File helloworldsoaphttp = new File(apache, "hello_world_soap_http");
        assertTrue(helloworldsoaphttp.exists());
        File types = new File(helloworldsoaphttp, "types");
        assertTrue(types.exists());
        files = helloworldsoaphttp.listFiles();
        assertEquals(1, files.length);
        files = types.listFiles();
        assertEquals(files.length, 10);
        File schemaImport = new File(apache, "schema_import");
        assertTrue(schemaImport.exists());
        files = schemaImport.listFiles();
        assertEquals(4, files.length);

        Class clz = classLoader.loadClass("org.apache.schema_import.Greeter");
        assertEquals(4, clz.getMethods().length);

        Method method = clz.getMethod("pingMe", new Class[] {});
        assertEquals("void", method.getReturnType().getSimpleName());
        assertEquals("Exception class is not generated ", 1, method.getExceptionTypes().length);

    }

    public void testExceptionNameCollision() throws Exception {
        env.put(ToolConstants.CFG_WSDLURL, getLocation("/wsdl2java_wsdl/InvoiceServer.wsdl"));

        processor.setContext(env);
        processor.execute(true);

        assertNotNull(output);

        File org = new File(output, "org");
        assertTrue(org.exists());
        File apache = new File(org, "apache");
        assertTrue(apache.exists());
        File invoiceserver = new File(apache, "invoiceserver");
        assertTrue(invoiceserver.exists());
        File invoice = new File(apache, "invoice");
        assertTrue(invoice.exists());

        File exceptionCollision = new File(invoiceserver, "NoSuchCustomerFault_Exception.java");
        assertTrue(exceptionCollision.exists());

        File[] files = invoiceserver.listFiles();
        assertEquals(13, files.length);
        files = invoice.listFiles();
        assertEquals(files.length, 9);

        Class clz = classLoader.loadClass("org.apache.invoiceserver.InvoiceServer");
        assertEquals(3, clz.getMethods().length);

        Method method = clz.getMethod("getInvoicesForCustomer", new Class[] {String.class, String.class});
        assertEquals("NoSuchCustomerFault_Exception", method.getExceptionTypes()[0].getSimpleName());

    }

    public void testAllNameCollision() throws Exception {
        env.put(ToolConstants.CFG_WSDLURL, getLocation("/wsdl2java_wsdl/hello_world_collision.wsdl"));
        env.setPackageName("org.apache");
        processor.setContext(env);
        processor.execute(true);

        assertNotNull(output);

        File org = new File(output, "org");
        assertTrue(org.exists());
        File apache = new File(org, "apache");
        assertTrue(apache.exists());

        File[] files = apache.listFiles();
        assertEquals(14, files.length);

        File typeCollision = new File(apache, "Greeter_Type.java");
        assertTrue(typeCollision.exists());
        File exceptionCollision = new File(apache, "Greeter_Exception.java");
        assertTrue(exceptionCollision.exists());
        File serviceCollision = new File(apache, "Greeter_Service.java");
        assertTrue(serviceCollision.exists());

        Class clz = classLoader.loadClass("org.apache.Greeter");
        assertTrue("SEI class Greeter modifier should be interface", clz.isInterface());

        clz = classLoader.loadClass("org.apache.Greeter_Exception");
        clz = classLoader.loadClass("org.apache.Greeter_Service");
    }

    public void testImportNameCollision() throws Exception {
        env.put(ToolConstants.CFG_WSDLURL,
                getLocation("/wsdl2java_wsdl/helloworld-portname_servicename.wsdl"));
        env.setPackageName("org.apache");
        processor.setContext(env);
        processor.execute(true);

        assertNotNull(output);

        File org = new File(output, "org");
        assertTrue(org.exists());
        File apache = new File(org, "apache");
        assertTrue(apache.exists());

        File[] files = apache.listFiles();
        assertEquals(4, files.length);

        File serviceCollision = new File(apache, "HelloWorldServiceImpl_Service.java");
        assertTrue(serviceCollision.exists());

        Class clz = classLoader.loadClass("org.apache.HelloWorldServiceImpl");
        assertTrue("SEI class HelloWorldServiceImpl modifier should be interface", clz.isInterface());

        clz = classLoader.loadClass("org.apache.HelloWorldServiceImpl_Service");
    }

    private String getLocation(String wsdlFile) {
        return this.getClass().getResource(wsdlFile).getFile();
    }
}
