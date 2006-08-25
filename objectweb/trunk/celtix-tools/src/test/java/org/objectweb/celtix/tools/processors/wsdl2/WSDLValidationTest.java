package org.objectweb.celtix.tools.processors.wsdl2;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;

import org.objectweb.celtix.tools.WSDLValidator;
import org.objectweb.celtix.tools.common.ToolTestBase;

public class WSDLValidationTest extends ToolTestBase {
    private String schemaDir;

    public void setUp() {
        super.setUp();
        try {
            schemaDir = getLocation("/schemas");
        } catch (IOException ex) {
            ex.printStackTrace();
        }

    }

    public void testValidateUniqueBody() {
        try {

            String[] args = new String[] {"-d", schemaDir,
                                          "-verbose", getLocation("/wsdl/doc_lit_bare.wsdl")};
            WSDLValidator.main(args);
            
            assertTrue("Non Unique Body Parts Error should be discovered",
                       getStdErr().indexOf("Non unique body part") > -1);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
 
    public void testValidateMixedStyle() {
        try {

            String[] args = new String[] {"-d", schemaDir, "-verbose",
                                          getLocation("/wsdl/hello_world_mixed_style.wsdl")};
            WSDLValidator.main(args);
            assertTrue("Mixted style Error should be discovered",
                       getStdErr().indexOf("Mixted style ,Wrong WSDL") > -1);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
   
    public void testValidateTypeElement() {
        try {

            String[] args = new String[] {"-d", schemaDir, "-verbose",
                                          getLocation("/wsdl/hello_world_doc_lit_type.wsdl")};
            WSDLValidator.main(args);
            assertTrue("Refere type element error   should be discovered",
                       getStdErr().indexOf("using the element attribute") > -1);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
   
    public void testValidateAttribute() {
        try {

            String[] args = new String[] {"-d", schemaDir, "-verbose",
                                          getLocation("/wsdl/hello_world_error_attribute.wsdl")};
            WSDLValidator.main(args);
            assertTrue("Attribute error should be discovered",
                       getStdErr().indexOf(" is not allowed to appear in element") > -1);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void testValidateReferenceError() throws Exception {

        try {

            String[] args = new String[] {"-d", schemaDir, "-verbose",
                                          getLocation("/wsdl/hello_world_error_reference.wsdl")};
            WSDLValidator.main(args);
            assertTrue("Reference error should be discovered",
                       getStdErr().indexOf("reference binding is not defined") > -1);


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void testBug305872() throws Exception {
        try {
            String[] args = new String[] {"-d", schemaDir, "-verbose",
                                          getLocation("/wsdl/bug305872/http.xsd")};
            WSDLValidator.main(args);

            assertTrue("Tools should check if this file is a wsdl file",
                       getStdErr().indexOf("is not a wsdl file") > -1);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
   


    private String getLocation(String wsdlFile) throws IOException {
        Enumeration<URL> e = WSDLValidationTest.class.getClassLoader().getResources(wsdlFile);
        while (e.hasMoreElements()) {
            URL u = e.nextElement();
            File f = new File(u.getFile());
            if (f.exists() && f.isDirectory()) {
                return f.toString();
            }
        }

        return WSDLValidationTest.class.getResource(wsdlFile).getFile();
    }
}
