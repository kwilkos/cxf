package org.objectweb.celtix.tools.processors.wsdl2;

import org.objectweb.celtix.tools.common.ToolConstants;
import org.objectweb.celtix.tools.processors.ProcessorTestBase;

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
            processor.setEnvironment(env);
            processor.process();
            fail("WSDL Validation Exception Should Be Thrown");

        } catch (Exception e) {
            // do nothing
        }
    }

    private String getLocation(String wsdlFile) {
        return WSDLValidationTest.class.getResource(wsdlFile).getFile();
    }
}
