package org.objectweb.celtix.tools.processors.wsdl2;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URL;
import java.util.Enumeration;

import org.objectweb.celtix.tools.WSDLValidator;
import org.objectweb.celtix.tools.common.ToolConstants;
import org.objectweb.celtix.tools.common.ToolException;
import org.objectweb.celtix.tools.processors.ProcessorTestBase;
import org.objectweb.celtix.tools.processors.wsdl2.validators.WSDL11Validator;

public class WSDLValidationTest extends ProcessorTestBase {

    private WSDLToJavaProcessor processor = new WSDLToJavaProcessor();
    private String origSchemaDir;
    
    public void setUp() throws Exception {
        super.setUp();
        origSchemaDir = System.getProperty(ToolConstants.CELTIX_SCHEMA_DIR);
        env.put(ToolConstants.CFG_OUTPUTDIR, output.getCanonicalPath());
    }

    public void tearDown() {
        super.tearDown();
        processor = null;
        if (origSchemaDir != null) {
            System.setProperty(ToolConstants.CELTIX_SCHEMA_DIR, origSchemaDir);
        } else {
            System.clearProperty(ToolConstants.CELTIX_SCHEMA_DIR);
        }
    }

    public void testValidateWSDL() {
        try {
            env.put(ToolConstants.CFG_WSDLURL, getLocation("/wsdl/doc_lit_bare.wsdl"));
            env.put(ToolConstants.CFG_VALIDATE_WSDL, ToolConstants.CFG_VALIDATE_WSDL);
            System.setProperty(ToolConstants.CELTIX_SCHEMA_DIR, getLocation("/schemas"));
            processor.setEnvironment(env);
            processor.process();
            fail("WSDL Validation Exception Should Be Thrown");
        } catch (Exception e) {
            // do nothing
        }
    }

    public void testMixedStyle() {
        try {
            env.put(ToolConstants.CFG_WSDLURL, getLocation("/wsdl/hello_world_mixed_style.wsdl"));
            processor.setEnvironment(env);
            processor.process();
            fail("WSDL Validation Exception Should Be Thrown");
        } catch (Exception e) {
            // do nothing
        }
    }

    public void testDocType() {
        try {
            env.put(ToolConstants.CFG_WSDLURL, getLocation("/wsdl/hello_world_doc_lit_type.wsdl"));
            env.put(ToolConstants.CFG_VALIDATE_WSDL, ToolConstants.CFG_VALIDATE_WSDL);
            System.setProperty(ToolConstants.CELTIX_SCHEMA_DIR, getLocation("/schemas"));
            processor.setEnvironment(env);            
            processor.process();
            fail("WSDL Validation Exception Should Be Thrown");
        } catch (Exception e) {
            // do nothing
        }
    }

    public void testValidationHandlerWSDL() throws Exception {
        env.put(ToolConstants.CFG_WSDLURL, getLocation("/wsdl/handler_test.wsdl"));
        processor.setEnvironment(env);
        processor.process();

    }

    
    public void testValidationHandlerWSDL2() throws Exception {
        env.put(ToolConstants.CFG_WSDLURL, getLocation("/wsdl/addNumbers.wsdl"));
        processor.setEnvironment(env);
        processor.process();

    }
     
    public void testCommand() {
        PrintStream oldStdErr = System.err;
        try {
            ByteArrayOutputStream stdErr = new ByteArrayOutputStream();
            PrintStream ps = new PrintStream(stdErr); 
            System.setErr(ps);

            WSDLValidator.main(new String[] {"-d", getLocation("/schemas"),
                                             getLocation("/wsdl/hello_world_error_attribute.wsdl")});
            ps.flush();

            assertNotNull("validate exception should be thrown", stdErr.toString());

            assertTrue("Error should be found in " + stdErr.toString(),
                       stdErr.toString().indexOf("line 53 column 56") > -1);

        } catch (Exception e) {
            // ignore
        } finally {
            System.setErr(oldStdErr);
        }
    }

    public void testValidator() {
        try {
            env.put(ToolConstants.CFG_SCHEMA_DIR, getLocation("/schemas"));
            env.put(ToolConstants.CFG_WSDLURL, getLocation("/wsdl/hello_world_error_attribute.wsdl"));
            WSDL11Validator validator = new WSDL11Validator(null, env);
            validator.isValid();
            fail("validate exception should be thrown");
        } catch (Exception e) {
            // ignore exception
        }
    }

    public void testWsdlReferenceValidator() throws Exception {
        try {
            env.put(ToolConstants.CFG_SCHEMA_DIR, getLocation("/schemas"));
            env.put(ToolConstants.CFG_WSDLURL, getLocation("/wsdl/hello_world_error_reference.wsdl"));
            WSDL11Validator validator = new WSDL11Validator(null, env);
            validator.isValid();         
            fail("validate exception should be thrown");
        } catch (ToolException e) {
            String errMsg = e.getMessage();
            assertTrue("Part reference error should be located ", errMsg.indexOf("line 57 column 54") > -1);
            assertTrue("Part reference error should be located ", errMsg.indexOf("line 69 column 46") > -1);
            assertTrue("PortType reference should be located ", errMsg.indexOf("line 99 column 63") > -1);
            assertTrue("Binding Reference should be located ", errMsg.indexOf("line 129 column 65") > -1);
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
