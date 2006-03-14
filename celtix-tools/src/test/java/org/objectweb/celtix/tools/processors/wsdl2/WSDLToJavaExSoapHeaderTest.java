package org.objectweb.celtix.tools.processors.wsdl2;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URLClassLoader;

import javax.jws.WebParam;
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
        String[] args = new String[] {"-d", output.getCanonicalPath(), "-exsh", "true", "-compile",
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

    private String getLocation(String wsdlFile) {
        return WSDLToJavaExSoapHeaderTest.class.getResource(wsdlFile).getFile();
    }
}
