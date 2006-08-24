package org.apache.cxf.management.jmx;

import javax.management.ObjectName;

import junit.framework.TestCase;

public class JMXUtilsTest extends TestCase {
    static final String OBJECT_NAME = "org.apache.cxf.instrumentation:type=foo,name=boo"; 
    public void testGetObjectName() {
        ObjectName name = JMXUtils.getObjectName("foo", "boo");        
        assertTrue("The wrong object name", OBJECT_NAME.compareTo(name.toString()) == 0);
    }
}
