package org.objectweb.celtix.management.counters;

import junit.framework.TestCase;

public class CountersTest extends TestCase {
    
    public void testCounter() {
        Counter counter = new Counter("TestCounter");
        counter.reset();
        counter.increase();
        assertEquals("The Counter value is incorrcet ", 1, counter.getValue());
        counter.reset();
        assertEquals("The Counter should be rest to 0", 0, counter.getValue());
    }

}
