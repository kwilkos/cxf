package org.objectweb.celtix.jca.core.resourceadapter;

import java.util.Properties;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class UriHandlerInitTest extends TestCase {
    private static final String PROP_NAME = "java.protocol.handler.pkgs";

    private static final String PKG_ADD = "do.do";
    
    public UriHandlerInitTest(String name) {
        super(name);
    }

    public void testAppendToProp() {
        final Properties properties = System.getProperties();
        final String origVal = properties.getProperty(PROP_NAME);
        if (origVal != null) {
            try {
                assertTrue("pkg has been already been appended", origVal.indexOf(PKG_ADD) == -1);
                new UriHandlerInit(PKG_ADD);
                String newValue = properties.getProperty(PROP_NAME);
                assertTrue("pkg has been appended", newValue.indexOf(PKG_ADD) != -1);
                final int len = newValue.length();
                new UriHandlerInit(PKG_ADD);
                newValue = properties.getProperty(PROP_NAME);
                assertEquals("prop has not been appended twice, size is unchanged, newVal="
                             + newValue.length(), newValue.length(), len);

            } finally {
                if (origVal != null) {
                    properties.put(PROP_NAME, origVal);
                }
            }
        }
    }

   

    public static Test suite() {
        return new TestSuite(UriHandlerInitTest.class);
    }
   
}
