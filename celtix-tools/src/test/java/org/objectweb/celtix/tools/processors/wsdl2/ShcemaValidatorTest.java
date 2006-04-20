package org.objectweb.celtix.tools.processors.wsdl2;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.objectweb.celtix.tools.SchemaValidator;
import org.objectweb.celtix.tools.processors.ProcessorTestBase;
import org.objectweb.celtix.tools.processors.wsdl2.validators.SchemaWSDLValidator;

public class ShcemaValidatorTest extends ProcessorTestBase {
    public void testCommand() {
        PrintStream oldStdErr = System.err;
        try {
            ByteArrayOutputStream stdErr = new ByteArrayOutputStream();
            System.setErr(new PrintStream(stdErr));

            SchemaValidator.main(new String[] {"-d", getLocation("/schemas"),
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
            SchemaWSDLValidator validator = new SchemaWSDLValidator(getLocation("/schemas"));
            validator.validate(getLocation("/wsdl/hello_world_error_attribute.wsdl"), null, true);
            fail("validate exception should be thrown");
        } catch (Exception e) {
            // ignore exception
        }
    }

    private String getLocation(String schemaDir) {
        return ShcemaValidatorTest.class.getResource(schemaDir).getFile();
    }

}
