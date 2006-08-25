package org.objectweb.celtix.tools.processors.wsdl2;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.objectweb.celtix.tools.WSDLValidator;
import org.objectweb.celtix.tools.common.ToolConstants;
import org.objectweb.celtix.tools.processors.ProcessorTestBase;
import org.objectweb.celtix.tools.processors.wsdl2.validators.WSDL11Validator;

public class WSDLValidationTest extends ProcessorTestBase {

    private WSDLToJavaProcessor processor = new WSDLToJavaProcessor();

    public void setUp() throws Exception {
        super.setUp();
        env.put(ToolConstants.CFG_OUTPUTDIR, output.getCanonicalPath());
    }

    public void tearDown() {
        super.tearDown();
        processor = null;
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
            System.setErr(new PrintStream(stdErr));

            WSDLValidator.main(new String[] {"-d", getLocation("/schemas"),
                                             getLocation("/wsdl/hello_world_error_attribute.wsdl")});

            assertNotNull("validate exception should be thrown", stdErr.toString());

            assertTrue("Error should be located ", stdErr.toString().indexOf("line 53 column 56") > -1);

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

    public void testWsdlReferenceValidator() {
        try {
            env.put(ToolConstants.CFG_SCHEMA_DIR, getLocation("/schemas"));
            env.put(ToolConstants.CFG_WSDLURL, getLocation("/wsdl/hello_world_error_reference.wsdl"));
            WSDL11Validator validator = new WSDL11Validator(null, env);
            validator.isValid();         
            fail("validate exception should be thrown");
        } catch (Exception e) {
            String errMsg = e.getMessage();
            assertTrue("Part reference error should be located ", errMsg.indexOf("line 57 column 54") > -1);
            assertTrue("Part reference error should be located ", errMsg.indexOf("line 69 column 46") > -1);
            assertTrue("PortType reference should be located ", errMsg.indexOf("line 99 column 63") > -1);
            assertTrue("Binding Reference should be located ", errMsg.indexOf("line 129 column 65") > -1);
        }
    }

    
    
    
    private String getLocation(String wsdlFile) {
        return WSDLValidationTest.class.getResource(wsdlFile).getFile();
    }
}
