package org.objectweb.celtix.tools.common.toolspec;

import junit.framework.TestCase;

public class AbstractToolContainerTest extends TestCase {
    private DummyToolContainer dummyTool;

    public AbstractToolContainerTest(String name) {
        super(name);
    }

    public void setUp() throws Exception {
        String tsSource = "/org/objectweb/celtix/tools/common/toolspec/parser/resources/testtool.xml";
        ToolSpec toolspec = new ToolSpec(getClass().getResourceAsStream(tsSource), false);
        dummyTool = new DummyToolContainer(toolspec);
    }

    public void testQuietMode() {
        // catch all exception in here.
        try {
            dummyTool.setCommandLine(new String[] {"-q"});
        } catch (Exception e) {
            // caught expected exception
        }
        assertNotNull("Fail to redirect err output:", dummyTool.getErrOutputStream());
        assertNotNull("Fail to redirect output:", dummyTool.getOutOutputStream());
    }

    public void testInit() {
        try {
            dummyTool.init();
        } catch (ToolException e) {
            assertEquals("Tool specification has to be set before initializing", e.getMessage());
            return;
        }
        assertTrue(true);
    }

    public void testToolRunner() throws Exception {
        String tsSource = "/org/objectweb/celtix/tools/common/toolspec/parser/resources/testtool.xml";
        String[] args = {"-r", "wsdlurl=dfd"};
        ToolRunner.runTool(DummyToolContainer.class, getClass().getResourceAsStream(tsSource), false, args);
    }
}
