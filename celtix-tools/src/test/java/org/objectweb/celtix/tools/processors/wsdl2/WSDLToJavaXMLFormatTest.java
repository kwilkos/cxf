package org.objectweb.celtix.tools.processors.wsdl2;

import org.objectweb.celtix.tools.common.ToolConstants;
import org.objectweb.celtix.tools.common.ToolException;
import org.objectweb.celtix.tools.processors.ProcessorTestBase;

public class WSDLToJavaXMLFormatTest
    extends ProcessorTestBase {

    public void setUp() throws Exception {
        super.setUp();
        env.put(ToolConstants.CFG_OUTPUTDIR, output.getCanonicalPath());
    }

    public void testXMLFormatRootNodeValidationFail() throws Exception {
        WSDLToJavaProcessor processor = new WSDLToJavaProcessor();
        env.put(ToolConstants.CFG_OUTPUTDIR, output.getCanonicalPath());
        env.put(ToolConstants.CFG_WSDLURL, getLocation("/wsdl/xml_format_fail.wsdl"));
        env.put(ToolConstants.CFG_VALIDATE_WSDL, ToolConstants.CFG_VALIDATE_WSDL);
        System.setProperty(ToolConstants.CELTIX_SCHEMA_DIR, getLocation("/schemas"));
        processor.setEnvironment(env);
        try {
            processor.process();
            fail("Do not catch expected tool exception for xml format binding!");
        } catch (Exception e) {
            if (!(e instanceof ToolException && e.toString()
                .indexOf("missing xml format body element") >= 0)) {
                fail("Do not catch expected tool exception for xml format binding,"
                     + " catch other unexpected exception!");
            }
        }
    }

    public void testXMLFormatRootNodeValidationPass() throws Exception {
        WSDLToJavaProcessor processor = new WSDLToJavaProcessor();
        env.put(ToolConstants.CFG_OUTPUTDIR, output.getCanonicalPath());
        env.put(ToolConstants.CFG_WSDLURL, getLocation("/wsdl/xml_format_pass.wsdl"));
        processor.setEnvironment(env);
        processor.process();
    }

    private String getLocation(String wsdlFile) {
        return WSDLToJavaXMLFormatTest.class.getResource(wsdlFile).getFile();
    }
}
