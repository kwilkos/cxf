package org.objectweb.celtix.tools.processors.java2;

import java.io.File;

import org.objectweb.celtix.tools.common.ToolConstants;
import org.objectweb.celtix.tools.processors.ProcessorTestBase;

public class JavaToWSDLDocLitTest extends ProcessorTestBase {

    JavaToWSDLProcessor processor = new JavaToWSDLProcessor();

    public void setUp() throws Exception {
        super.setUp();
        env.put(ToolConstants.CFG_OUTPUTFILE, output.getPath() + "/doc_lit.wsdl");
        env.put(ToolConstants.CFG_CLASSNAME, "org.objectweb.hello_world_doc_lit.Greeter");
    }

    public void tearDown() {
        super.tearDown();
        processor = null;
    }

    public void testProcess() throws Exception {
        processor.setEnvironment(env);
        processor.process();
        File wsdlFile = new File(output, "doc_lit.wsdl");
        assertTrue(wsdlFile.exists());
        File schemaFile = new File(output, "schema1.xsd");
        assertTrue(schemaFile.exists());
    }
}
