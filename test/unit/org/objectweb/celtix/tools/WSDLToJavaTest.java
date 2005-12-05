package org.objectweb.celtix.tools;

import org.objectweb.celtix.tools.common.ToolTestBase;

public class WSDLToJavaTest extends ToolTestBase {
    
    public void testVersionOutput() throws Exception {
        String[] args = new String[]{"-v"};
        WSDLToJava.main(args);
        assertNotNull(getStdOut());
    }

    public void testHelpOutput() {
        String[] args = new String[]{"-help"};
        WSDLToJava.main(args);
        assertNotNull(getStdOut());
    }

}
