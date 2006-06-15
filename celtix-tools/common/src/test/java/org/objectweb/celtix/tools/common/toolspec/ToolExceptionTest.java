package org.objectweb.celtix.tools.common.toolspec;

import junit.framework.TestCase;
import org.objectweb.celtix.tools.common.ToolException;
public class ToolExceptionTest extends TestCase {
    public void testMassMethod() {
        ToolException e = new ToolException("e");
        assertTrue(e.getCause() == null);
        e = new ToolException("run time exception", new RuntimeException("test run time exception"));
        assertTrue(e.getCause() instanceof RuntimeException);
        assertTrue(e.toString() != null);
    }
}
