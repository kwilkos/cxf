package org.objectweb.celtix.tools.processors.wsdl2;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URLClassLoader;

import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.soap.SOAPBinding;
import javax.xml.ws.Holder;

import org.objectweb.celtix.tools.WSDLToJava;
import org.objectweb.celtix.tools.processors.ProcessorTestBase;
import org.objectweb.celtix.tools.utils.AnnotationUtil;

public class WSDLToJavaExSoapHeaderTest
    extends ProcessorTestBase {

    private URLClassLoader classLoader;

    public void setUp() throws Exception {
        super.setUp();
        File classFile = new java.io.File(output.getCanonicalPath());
        System.setProperty("java.class.path", getClassPath() + classFile.getCanonicalPath()
                                              + File.separatorChar);
        classLoader = AnnotationUtil.getClassLoader(Thread.currentThread().getContextClassLoader());
    }

    public void testSoapBindingDiffMessage() throws Exception {
        String[] args = new String[] {"-d", output.getCanonicalPath(), 
                                      "-exsh", "true", "-compile",
                                      getLocation("/wsdl/soapheader_test.wsdl")};
        WSDLToJava.main(args);

        Class clz = classLoader.loadClass("org.objectweb.header_test.TestHeader");        
        Method method = clz.getMethod("testHeader4", new Class[] {java.lang.String.class,
                                                                  Holder.class});
        if (method == null) {
            fail("Missing method testHeader4 of TestHeader class!");
        }
        WebParam webParamAnno = AnnotationUtil.getWebParam(method, "testHeaderMessage");
        if (webParamAnno == null) {
            fail("Missing 'inoutHeader' WebParam Annotation of method testHeader4!");
        }
        assertEquals("INOUT", webParamAnno.mode().name());
        assertEquals(true, webParamAnno.header());
        assertEquals("inoutHeader", webParamAnno.partName());
    }

    public void testSoapHeaderBinding() throws Exception {
        String[] args = new String[] {"-d", output.getCanonicalPath(), "-compile",
                                      getLocation("/wsdl/soapheader_test.wsdl")};
        WSDLToJava.main(args);

        Class clz = classLoader.loadClass("org.objectweb.header_test.TestHeader");
        Class paramClz = classLoader.loadClass("org.objectweb.header_test.types.TestHeader5");
        assertEquals(5, clz.getMethods().length);
        
        Method method = clz.getMethod("testHeader5", new Class[] {paramClz});
        if (method == null) {
            fail("Missing method testHeader5 of TestHeader class!");
        }

        SOAPBinding soapBindingAnno = AnnotationUtil.getPrivMethodAnnotation(method, SOAPBinding.class);
        assertEquals("BARE", soapBindingAnno.parameterStyle().name());

        WebResult webResultAnno = AnnotationUtil.getWebResult(method);
        if (webResultAnno == null) {
            fail("Missing 'in' WebParam Annotation of method testHeader5!");
        }        
        assertEquals(true, webResultAnno.header());
        assertEquals("outHeader", webResultAnno.partName());
        assertEquals("testHeader5", webResultAnno.name());
    }

    private String getLocation(String wsdlFile) {
        return WSDLToJavaExSoapHeaderTest.class.getResource(wsdlFile).getFile();
    }
}
