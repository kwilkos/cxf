package org.apache.cxf.common.commands;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import junit.framework.TestCase;

public class ResultBufferedCommandTest extends TestCase {
    
    private static final String OUT = "Hello World!";
    private static final String ERR = "Please contact your administrator.";
    

    public void testStreamsEmpty() throws IOException {
        URL url = TestCommand.class.getResource("TestCommand.class");
        File file = new File(url.getFile());
        file = file.getParentFile();
        file = new File(file, "../../../../..");
        String[] cmd = new String[] {
            JavaHelper.getJavaCommand(),
            "-classpath",
            file.getCanonicalPath(),
            "org.apache.cxf.common.commands.TestCommand",
        };
        ResultBufferedCommand rbc = new ResultBufferedCommand(cmd);
        assertEquals(0, rbc.execute());
        BufferedReader br = rbc.getBufferedOutputReader();
        assertNotNull(br);
        assertNull(br.readLine());
        br.close();
        br = rbc.getBufferedErrorReader();      
        assertNotNull(br);
        assertNull(br.readLine());
        br.close();
        InputStream is = rbc.getOutput();
        assertEquals(0, is.available());
        is.close();
        is = rbc.getError();
        assertEquals(0, is.available());
        is.close();
    }
    
    public void testStreamsNotEmpty() throws IOException {
        URL url = TestCommand.class.getResource("TestCommand.class");
        File file = new File(url.getFile());
        file = file.getParentFile();
        file = new File(file, "../../../../..");
        String[] cmd = new String[] {
            JavaHelper.getJavaCommand(),
            "-classpath",
            file.getCanonicalPath(),
            "org.apache.cxf.common.commands.TestCommand",
            "-out",
            OUT,
            "-err",
            ERR,
            "-result",
            "2",          
        };
        ResultBufferedCommand rbc = new ResultBufferedCommand();
        rbc.setArgs(cmd);
        assertEquals(2, rbc.execute());
        BufferedReader br = rbc.getBufferedOutputReader();
        assertNotNull(br);
        String line = br.readLine();
        assertEquals(OUT, line);
        assertNull(br.readLine());
        br.close();
        br = rbc.getBufferedErrorReader();
        assertNotNull(br);
        line = br.readLine();
        assertEquals(ERR, line);
        assertNull(br.readLine());
        br.close();
        InputStream is = rbc.getOutput();
        assertTrue(is.available() > 0);
        is.close();
        is = rbc.getError();
        assertTrue(is.available() > 0);
        is.close();
    }

}
