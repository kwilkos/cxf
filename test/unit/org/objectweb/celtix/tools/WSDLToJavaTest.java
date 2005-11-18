package org.objectweb.celtix.tools;

import org.objectweb.celtix.tools.common.ToolTestBase;

public class WSDLToJavaTest extends ToolTestBase {
    
    public void testVersionOutput() {
        String[] args = new String[]{"-v"};
        WSDLToJava.main(args);
        assertNotNull(getStdOut());
    }

    public void testHelpOutput() {
        String[] args = new String[]{"-help"};
        WSDLToJava.main(args);
        assertNotNull(getStdOut());
    }

    public void testOutputDir() {
        String[] args = new String[]{"-d", "/local", "foo.wsdl"};
        WSDLToJava.main(args);
        assertNotNull(getStdOut());
    }

    public void testAntFile() {
        String[] args = new String[]{"-ant", "foo.wsdl"};
        WSDLToJava.main(args);
        assertNotNull(getStdOut());
    }
}
