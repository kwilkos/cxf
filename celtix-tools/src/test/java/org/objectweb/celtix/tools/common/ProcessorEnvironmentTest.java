package org.objectweb.celtix.tools.common;

import java.util.*;

import junit.framework.TestCase;

public class ProcessorEnvironmentTest extends TestCase {
    public void testGet() {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("k1", "v1");
        ProcessorEnvironment env = new ProcessorEnvironment();
        env.setParameters(map);
        String value = (String) env.get("k1");
        assertEquals("v1", value);
    }

    public void testPut() {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("k1", "v1");
        ProcessorEnvironment env = new ProcessorEnvironment();
        env.setParameters(map);
        env.put("k2", "v2");
        String value = (String) env.get("k2");
        assertEquals("v2", value);
    }

    public void testRemove() {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("k1", "v1");
        ProcessorEnvironment env = new ProcessorEnvironment();
        env.setParameters(map);
        env.put("k2", "v2");
        String value = (String) env.get("k2");
        assertEquals("v2", value);
        env.remove("k1");
        assertNull(env.get("k1"));
    }

    public void testContainsKey() {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("k1", "v1");
        ProcessorEnvironment env = new ProcessorEnvironment();
        env.setParameters(map);
        assertTrue(env.containsKey("k1"));
    }

}
