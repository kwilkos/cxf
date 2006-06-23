package org.objectweb.celtix.transports.jms;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import junit.framework.TestCase;

public class JMSInputStreamContextTest extends TestCase {
    
    static final String HELLO_WORLD_STRING = "Hello World";
    static final String ANOTHER_STRING_STRING = "Another String";

    public JMSInputStreamContextTest(String arg0) {
        super(arg0);
    }

    public static void main(String[] args) {
        junit.textui.TestRunner.run(JMSInputStreamContextTest.class);
    }

    public void testJMSInputStreamContext() throws Exception {
        
        JMSInputStreamContext inCtx = 
            new JMSInputStreamContext(
                new ByteArrayInputStream(HELLO_WORLD_STRING.getBytes())
            );
         
        InputStream ins = inCtx.getInputStream();
        byte [] retResult = new byte[HELLO_WORLD_STRING.length()];
         
        ins.read(retResult, 0 , HELLO_WORLD_STRING.length());
         
        assertTrue("Should get the same InputStream that was passed : Hello World ", 
                HELLO_WORLD_STRING.equals(new String(retResult)));
         
        InputStream insNew = new ByteArrayInputStream(ANOTHER_STRING_STRING.getBytes());
         
        inCtx.setInputStream(insNew);
         
        ins = inCtx.getInputStream();
         
        retResult = new byte[ANOTHER_STRING_STRING.length()];
        ins.read(retResult, 0, ANOTHER_STRING_STRING.length());
         
        assertTrue("Should get the same InputStream that was passed : Another String ", 
                ANOTHER_STRING_STRING.equals(new String(retResult)));
         
        inCtx.setFault(true);
         
        assertFalse(inCtx.isFault());
    }
}
