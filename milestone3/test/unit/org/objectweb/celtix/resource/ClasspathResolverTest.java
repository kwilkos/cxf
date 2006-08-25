package org.objectweb.celtix.resource;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;


public class ClasspathResolverTest extends TestCase {
    private static final String TEST_RESOURCE = "config-metadata/bus-config.xml"; 

    ClasspathResolver cpr = new ClasspathResolver(); 

    public void setUp() { 

        InputStream in = ClassLoader.getSystemResourceAsStream(TEST_RESOURCE);
        assertNotNull("cannot find " + TEST_RESOURCE + " on system classpath", 
                      in); 
    } 

    public void testResolveFromClassPath() { 

        assertNull(cpr.resolve(TEST_RESOURCE, null));
    }
 
    public void testResolveStreamFromClassPath() throws IOException { 

        InputStream in = cpr.getAsStream(TEST_RESOURCE); 
        assertNotNull(in);
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        String line = reader.readLine(); 
        assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?>", line);
    } 

    
    public static Test suite() {
        return new TestSuite(ClasspathResolverTest.class);
    }
    
    public static void main(String[] args)  {
        TestRunner.main(new String[] {ClasspathResolverTest.class.getName()});
    }
}