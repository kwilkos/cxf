package org.objectweb.celtix.tools;

import java.io.*;
import java.net.URL;

import org.objectweb.celtix.tools.common.ToolTestBase;

public class WSDLToJavaTest extends ToolTestBase {

    private File output;
    
    public void setUp() {
        super.setUp();
        try {
            URL url = WSDLToJavaTest.class.getResource(".");
            output = new File(url.getFile());
            output = new File(output, "/resources");
            
            if (!output.exists()) {
                output.mkdir();
            }
        } catch (Exception e) {
            // complete
        }
    }

    public void tearDown() {
        output.deleteOnExit();
        output = null;
    }

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

    public void testBadUsage() {
        String[] args = new String[]{"-bad"};
        WSDLToJava.main(args);
        assertNotNull(getStdOut());
    }

    public void testWSDLToJava() throws Exception {
        String[] args = new String[]{"-ant", "-V", "-d", output.getCanonicalPath(), wsdlLocation.getFile()};
        WSDLToJava.main(args);
        assertNotNull(getStdOut());
    }
}
