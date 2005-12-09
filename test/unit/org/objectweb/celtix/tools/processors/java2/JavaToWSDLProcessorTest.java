package org.objectweb.celtix.tools.processors.java2;

import junit.framework.TestCase;

import org.objectweb.celtix.tools.common.ProcessorEnvironment;
import org.objectweb.celtix.tools.common.ToolConstants;

public class JavaToWSDLProcessorTest extends TestCase {

    ProcessorEnvironment env = new ProcessorEnvironment();
    JavaToWSDLProcessor processor = new JavaToWSDLProcessor();

    public void setUp() throws Exception {

        env.put(ToolConstants.CFG_CLASSNAME, "org.objectweb.hello_world_soap_http.Greeter");
    }

    public void testProcess() throws Exception {
        processor.setEnvironment(env);
        processor.process();
        assertEquals(processor.getModel().getServiceName(), "GreeterSERVICE");
        assertNotNull(processor.getModel().getDefinition());
    }

}
