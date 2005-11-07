package org.objectweb.celtix.tools.common.toolspec;

import junit.framework.TestCase;

public class ToolSupportTest extends TestCase {

    
    public void testProtect() throws ToolException {
        ToolSupport toolSupport = new ToolSupport();
        
        toolSupport.performFunction();
        toolSupport.destroy();
        assertTrue(toolSupport.getContext() == null);
    }

}
