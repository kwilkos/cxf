/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.cxf.tools.common;

import java.util.*;

import junit.framework.TestCase;

public class ProcessorEnvironmentTest extends TestCase {
    public void testGet() {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("k1", "v1");
        ProcessorEnvironment env = new ProcessorEnvironment();
        env.setParameters(map);
        String value = (String)env.get("k1");
        assertEquals("v1", value);
    }

    public void testPut() {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("k1", "v1");
        ProcessorEnvironment env = new ProcessorEnvironment();
        env.setParameters(map);
        env.put("k2", "v2");
        String value = (String)env.get("k2");
        assertEquals("v2", value);
    }

    public void testRemove() {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("k1", "v1");
        ProcessorEnvironment env = new ProcessorEnvironment();
        env.setParameters(map);
        env.put("k2", "v2");
        String value = (String)env.get("k2");
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

    public void testGetDefaultValue() {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("k1", "v1");
        ProcessorEnvironment env = new ProcessorEnvironment();
        env.setParameters(map);

        String k1 = (String)env.get("k1", "v2");
        assertEquals("v1", k1);
        String k2 = (String)env.get("k2", "v2");
        assertEquals("v2", k2);
    }

    public void testOptionSet() {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("k1", "true");
        ProcessorEnvironment env = new ProcessorEnvironment();
        env.setParameters(map);

        assertTrue(env.optionSet("k1"));
        assertFalse(env.optionSet("k2"));
    }

    public void testGetBooleanValue() {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("k1", "true");
        ProcessorEnvironment env = new ProcessorEnvironment();
        env.setParameters(map);

        Boolean k1 = Boolean.valueOf((String)env.get("k1"));
        assertTrue(k1);
        Boolean k2 = Boolean.valueOf((String)env.get("k2", "true"));
        assertTrue(k2);
        Boolean k3 = Boolean.valueOf((String)env.get("k3", "yes"));
        assertFalse(k3);
    }

}
