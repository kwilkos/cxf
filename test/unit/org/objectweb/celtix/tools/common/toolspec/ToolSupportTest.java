package org.objectweb.celtix.tools.common.toolspec;

import java.io.OutputStream;

import junit.framework.TestCase;

public class ToolSupportTest extends TestCase {

    
    public void testProtect() throws ToolException {
        ToolSupport toolSupport = new ToolSupport();
        toolSupport.pump(getClass().getResourceAsStream(
                                                        "/org/objectweb/celtix/tools/common/"
                                                            + "toolspec/parser/resources/testtool.xml"),
                         new DumyOutputStream());
        
        toolSupport.performFunction();
        toolSupport.destroy();
        assertTrue(toolSupport.getContext() == null);
    }

    public class DumyOutputStream extends OutputStream {
        
        public void write(byte[] b) {
        }
        
        public void write(int b) {
        }
        
    }

}
