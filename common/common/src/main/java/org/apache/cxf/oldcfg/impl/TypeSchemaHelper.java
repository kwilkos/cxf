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

package org.apache.cxf.oldcfg.impl;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class TypeSchemaHelper {
    
    private static Map<String, TypeSchema> map = new HashMap<String, TypeSchema>();
    
    private final boolean forceDefaults;
    
    public TypeSchemaHelper(boolean fd) {
        forceDefaults = fd;
    }
    
    public static void clearCache() {
        map.clear();
    }
    
    public TypeSchema get(String namespaceURI, String base, String location) {
        TypeSchema ts = map.get(namespaceURI);
        if (null == ts) {
            ts = new TypeSchema(namespaceURI, base, location, forceDefaults);
            map.put(namespaceURI, ts);
        }
        return ts;
    }
    
    public TypeSchema get(String namespaceURI) {
        return map.get(namespaceURI);
    }
    
    public Collection<TypeSchema> getTypeSchemas() {
        return map.values();
    }
    
    public void put(String namespaceURI, TypeSchema ts) {
        map.put(namespaceURI, ts);
    }
}
