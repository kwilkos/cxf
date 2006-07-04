package org.objectweb.celtix.jca.core.resourceadapter;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

public class HandlerTest extends TestCase {
    Handler h; 
    
    public HandlerTest(String name) {
        super(name);
    }

    public void setUp() throws ClassNotFoundException { 
        h = new Handler();
    } 
    
    public void testGetStreamToThisResource() throws Exception { 
        String urlpath = HandlerTest.class.getName().replace('.', '/') + ".class";
        String urls = "resourceadapter:" + urlpath;
        URL res = new URL(null, urls, h);
        InputStream is = h.openConnection(res).getInputStream();
        assertTrue("stream is not null", is != null);
    }


    public void testGetStreamToNonExistantResourceThrows() throws Exception { 
        String path = "some gobbledy rubbish/that/does/not/exist";
        String urls = "resourceadapter:" + path;
        URL res = new URL(null, urls, h);
        try {
            h.openConnection(res).getInputStream();
            fail("expect IOException on non existant url");
        } catch (IOException ioe) {
            String msg = ioe.getMessage();
            assertTrue("Ex message has expected text, msg=" + msg, msg.indexOf(path) != -1);
        }
    }

       
    public static Test suite() {
        return new TestSuite(HandlerTest.class);
    }

    public static void main(String[] args) {
        TestRunner.main(new String[] {HandlerTest.class.getName()});
    }
}

