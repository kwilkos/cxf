package org.apache.cxf.tools.common.toolspec;

import junit.framework.TestCase;
import org.apache.cxf.tools.common.ToolException;
public class ToolExceptionTest extends TestCase {
    public void testMassMethod() {
        ToolException e = new ToolException("e");
        assertTrue(e.getCause() == null);
        e = new ToolException("run time exception", new RuntimeException("test run time exception"));
        assertTrue(e.getCause() instanceof RuntimeException);
        assertTrue(e.toString() != null);
    }
}
