package org.objectweb.celtix.tools.processors.wsdl2;

import java.io.File;
import java.lang.reflect.Method;

import javax.jws.WebMethod;

import org.objectweb.celtix.tools.common.ToolConstants;
import org.objectweb.celtix.tools.processors.ProcessorTestBase;
import org.objectweb.celtix.tools.utils.AnnotationUtil;

public class WSDLTOJAVACompilerTest extends ProcessorTestBase {
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
        //super.tearDown();
        processor = null;
    }

    public void testCompileGeneratedCode() throws Exception {
        try {
            env.put(ToolConstants.CFG_WSDLURL, getLocation("/wsdl/hello_world_rpc_lit.wsdl"));
            processor.setEnvironment(env);
            processor.process();
        } catch (Exception e) {
            e.printStackTrace();
            assertTrue("Generate and compile code error", false);
        }
    }

    public void testSupportXMLBindingBare() throws Exception {
        try {
            env.put(ToolConstants.CFG_WSDLURL, getLocation("/wsdl/xml_http_bare.wsdl"));
            processor.setEnvironment(env);
            processor.process();
        } catch (Exception e) {
            e.printStackTrace();
            assertTrue("Generate and compile code error", false);
        }
    }

    public void testSupportXMLBindingWrapped() throws Exception {
        try {
            env.put(ToolConstants.CFG_WSDLURL, getLocation("/wsdl/xml_http_wrapped.wsdl"));
            processor.setEnvironment(env);
            processor.process();
        } catch (Exception e) {
            e.printStackTrace();
            assertTrue("Generate and compile code error", false);
        }
    }

    public void testAsynMethod() throws Exception {
        try {
            env.put(ToolConstants.CFG_WSDLURL, getLocation("/wsdl/hello_world_async.wsdl"));
            processor.setEnvironment(env);
            processor.process();

            Class clz = AnnotationUtil.loadClass("org.objectweb.hello_world_async_soap_http.GreeterAsync",
                                                 Thread.currentThread().getContextClassLoader());
         
            Method method = clz.getMethod("greetMeSometime", new Class[] {java.lang.String.class});
            WebMethod webMethodAnno = AnnotationUtil.getPrivMethodAnnotation(method, WebMethod.class);
            assertEquals(method.getName() + "()"
                         + " Annotation : WebMethod.operationName "
                         , "greetMeSometime", webMethodAnno.operationName());

            Method method1 = clz.getMethod("greetMeSometimeAsync",
                                           new Class[] {java.lang.String.class,
                                                        javax.xml.ws.AsyncHandler.class});
            WebMethod webMethodAnno1 = AnnotationUtil.getPrivMethodAnnotation(method1, WebMethod.class);
            
            assertEquals(method1.getName() + "()"
                         + " Annotation : WebMethod.operationName "
                         , "greetMeSometime", webMethodAnno1.operationName());

            java.lang.reflect.Method method2 = clz.getMethod("greetMeSometimeAsync",
                                                             new Class[] {java.lang.String.class});
            WebMethod webMethodAnno2 = AnnotationUtil.getPrivMethodAnnotation(method2, WebMethod.class);
            assertEquals(method2.getName() + "()"
                         + " Annotation : WebMethod.operationName "
                         , "greetMeSometime", webMethodAnno2.operationName());

        } catch (Exception e) {
            e.printStackTrace();
            assertTrue("Generate and compile code error", false);
        }

    }

    private String getLocation(String wsdlFile) {
        return WSDLTOJAVACompilerTest.class.getResource(wsdlFile).getFile();
    }

}
