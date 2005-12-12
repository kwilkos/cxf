package org.objectweb.celtix.tools.processors.java2;

import java.io.File;
import java.net.URL;

import junit.framework.TestCase;
import org.objectweb.celtix.tools.common.ProcessorEnvironment;
import org.objectweb.celtix.tools.common.ToolConstants;

public class JavaToWSDLProcessorTest extends TestCase {

    ProcessorEnvironment env = new ProcessorEnvironment();
    JavaToWSDLProcessor processor = new JavaToWSDLProcessor();
    private File output;
    
    public void setUp() {
        
        URL url = JavaToWSDLProcessorTest.class.getResource(".");
        output = new File(url.getFile());
        output = new File(output, "/resources");
        
        if (!output.exists()) {
            output.mkdir();
        }
        env.put(ToolConstants.CFG_OUTPUTFILE,
                output.getPath());
        env.put(ToolConstants.CFG_CLASSNAME, "org.objectweb.hello_world_doc_lit.Greeter");

    }

    public void tearDown() {
        output.deleteOnExit();
        output = null;
        processor = null;
        env = null;
    }


    public void testProcess() throws Exception {
        processor.setEnvironment(env);
        processor.process();
        File schemaFile = new File(output, "schema1.xsd");
        assertTrue(schemaFile.exists());
    }
}
