package org.objectweb.celtix.tools.processors.java2;

import org.objectweb.celtix.tools.common.ProcessorEnvironment;
import org.objectweb.celtix.tools.common.ToolConstants;
import org.objectweb.celtix.tools.common.ToolTestBase;

public class JavaToWSDLProcessorTest extends ToolTestBase {

    ProcessorEnvironment env = new ProcessorEnvironment();
    JavaToWSDLProcessor processor = new JavaToWSDLProcessor();

    public void setUp() {
        super.setUp();
        env.put(ToolConstants.CFG_CLASSNAME, "org.objectweb.hello_world_soap_http.Greeter");
    }

    public void testProcess() throws Exception {
        processor.setEnvironment(env);
        processor.process();
        assertEquals(processor.getModel().getServiceName(), "GreeterSERVICE");
        assertNotNull(processor.getModel().getDefinition());
        assertNotNull(getStdOut());
    }
}
