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

package org.apache.cxf.jaxrs;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.core.MultivaluedMap;

public class MetadataMap implements MultivaluedMap<String, Object> {

    private Map<String, List<Object>> m = new HashMap<String, List<Object>>();
    
    public void add(String key, Object value) {
        List<Object> data = m.get(key);
        if (data == null) {
            data = new ArrayList<Object>();    
            m.put(key, data);
        }
        data.add(value);
    }

    public Object getFirst(String key) {
        List<Object> data = m.get(key);
        return data == null ? null : data.get(0);
    }

    public void putSingle(String key, Object value) {
        List<Object> data = new ArrayList<Object>();
        data.add(value);
        m.put(key, data);
    }

    public void clear() {
        m.clear();

    }

    public boolean containsKey(Object key) {
        return m.containsKey(key);
    }

    public boolean containsValue(Object value) {
        return m.containsValue(value);
    }

    public Set<Entry<String, List<Object>>> entrySet() {
        return m.entrySet();
    }

    public List<Object> get(Object key) {
        return m.get(key);
    }

    public boolean isEmpty() {
        return m.isEmpty();
    }

    public Set<String> keySet() {
        return m.keySet();
    }

    public List<Object> put(String key, List<Object> value) {
        return m.put(key, value);
    }

    public void putAll(Map<? extends String, ? extends List<Object>> map) {
        m.putAll(map);
    }

    public List<Object> remove(Object key) {
        return m.remove(key);
    }

    public int size() {
        return m.size();
    }

    public Collection<List<Object>> values() {
        return m.values();
    }

    @Override
    public int hashCode() {
        return m.hashCode();
    }
    
    @Override
    public boolean equals(Object o) {
        return m.equals(o);
    }
}
