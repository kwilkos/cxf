package org.objectweb.celtix.bus.management.jmx;

import javax.management.ObjectName;

import junit.framework.TestCase;

public class JMXUtilsTest extends TestCase {
    static final String OBJECT_NAME = "org.objectweb.celtix:Type=foo,Bus=celtix"; 
    public void testGetObjectName() {
        ObjectName name = JMXUtils.getObjectName("foo", "celtix");
        
        assertTrue("The wrong object name", OBJECT_NAME.compareTo(name.toString()) == 0);
    }
}
