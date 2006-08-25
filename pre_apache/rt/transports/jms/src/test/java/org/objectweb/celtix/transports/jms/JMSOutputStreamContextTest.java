package org.objectweb.celtix.transports.jms;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;

import junit.framework.TestCase;

import org.objectweb.celtix.context.GenericMessageContext;

public class JMSOutputStreamContextTest extends TestCase {

    static final String ANOTHER_STRING_STRING = "Another string";
    static final String HELLO_WORLD_STRING = "Hello World";

    public JMSOutputStreamContextTest(String arg0) {
        super(arg0);
    }

    public static void main(String[] args) {
        junit.textui.TestRunner.run(JMSOutputStreamContextTest.class);
    }
    
    public void testJMSOutputStreamContext() throws Exception {
        JMSOutputStreamContext joc =
            new JMSOutputStreamContext(new GenericMessageContext());
        
        OutputStream os = joc.getOutputStream();
        
        assertNotNull(os);
        
        os.write(HELLO_WORLD_STRING.getBytes());
        
        os = joc.getOutputStream();
        
        assertTrue("Should not change the contents of output buffer : ", 
                       HELLO_WORLD_STRING.equals(os.toString()));
        
        os = new ByteArrayOutputStream();
        
        os.write(ANOTHER_STRING_STRING.getBytes());
        
        assertTrue("Should contain old contents. ",  
                HELLO_WORLD_STRING.equals(joc.getOutputStream().toString()));
        
        joc.setOutputStream(os);
        
        assertTrue("Should contain new contents. ",  
                ANOTHER_STRING_STRING.equals(joc.getOutputStream().toString()));
        
        joc.setOneWay(true);
        
        assertTrue(joc.isOneWay());
        
        joc.setOneWay(false);
        
        assertFalse(joc.isOneWay());
        
        joc.setFault(true);
        
        assertFalse(joc.isFault());
    }

}
