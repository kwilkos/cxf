package org.objectweb.celtix.tools.common.toolspec;

import junit.framework.TestCase;
import org.objectweb.celtix.tools.common.ToolException;
public class ToolSupportTest extends TestCase {

    
    public void testProtect() throws ToolException {
        ToolSupport toolSupport = new ToolSupport();
        
        toolSupport.performFunction();
        toolSupport.destroy();
        assertTrue(toolSupport.getContext() == null);
    }

}
