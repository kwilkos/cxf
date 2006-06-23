package org.objectweb.celtix.tools.wsdl2java;

import java.io.*;

import org.objectweb.celtix.tools.common.ToolTestBase;

public class WSDLToJavaTest extends ToolTestBase {

    private File output;
    
    public void setUp() {
        super.setUp();
        try {
            File file = File.createTempFile("WSDLToJavaTest", "");
            output = new File(file.getAbsolutePath() + ".dir");
            file.delete();
            
            if (!output.exists()) {
                output.mkdir();
            }
        } catch (Exception e) {
            // complete
        }
    }

    private void deleteDir(File dir) throws IOException {
        for (File f : dir.listFiles()) {
            if (f.isDirectory()) {
                deleteDir(f);
            } else {
                f.delete();
            }
        }
        dir.delete();
    }
    public void tearDown() {
        try {
            deleteDir(output);
        } catch (IOException ex) {
            //ignore
        }
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
