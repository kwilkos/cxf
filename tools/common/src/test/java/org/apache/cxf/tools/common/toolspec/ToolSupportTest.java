package org.apache.cxf.tools.common.toolspec;

import junit.framework.TestCase;
import org.apache.cxf.tools.common.ToolException;
public class ToolSupportTest extends TestCase {

    
    public void testProtect() throws ToolException {
        ToolSupport toolSupport = new ToolSupport();
        
        toolSupport.performFunction();
        toolSupport.destroy();
        assertTrue(toolSupport.getContext() == null);
    }

}
