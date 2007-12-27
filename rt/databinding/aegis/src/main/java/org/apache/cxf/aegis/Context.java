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
package org.apache.cxf.aegis;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.cxf.aegis.databinding.AegisDatabinding;
import org.apache.cxf.aegis.type.TypeMapping;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.message.Attachment;

/**
 * Holds information about the message request and response.
 * 
 * @author <a href="mailto:dan@envoisolutions.com">Dan Diephouse</a>
 * @since Feb 13, 2004
 */
public class Context implements Map<String, Object> {
    private AegisDatabinding databinding;
    private TypeMapping typeMapping;
    private Collection<Attachment> attachments;
    private Fault fault;
    private Map<String, Object> properties;
    
    public Context(AegisDatabinding databinding) {
       this(databinding, new HashMap<String, Object>());
    }

    public Context(AegisDatabinding databinding, boolean initializeProps) {
        this.databinding = databinding;
        if (initializeProps) {
            properties = new HashMap<String, Object>();
        }
    }

    public Context(AegisDatabinding databinding, Map<String, Object> properties) {
        this.databinding = databinding;
        this.properties = properties;
    }

    public TypeMapping getTypeMapping() {
        return typeMapping;
    }

    public void setTypeMapping(TypeMapping typeMapping) {
        this.typeMapping = typeMapping;
    }

    public Collection<Attachment> getAttachments() {
        return attachments;
    }

    public void setAttachments(Collection<Attachment> attachments) {
        this.attachments = attachments;
    }

    public boolean isWriteXsiTypes() {
        return databinding.isWriteXsiTypes();
    }

    public boolean isReadXsiTypes() {
        return databinding.isReadXsiTypes();
    }

    public void setFault(Fault fault) {
        this.fault = fault;
    }

    public Fault getFault() {
        return fault;
    }

    public void clear() {
    }

    public boolean containsKey(Object key) {
        return false;
    }

    public boolean containsValue(Object value) {
        return false;
    }

    public Set<java.util.Map.Entry<String, Object>> entrySet() {
        return null;
    }

    public Object get(Object key) {
        return properties.get(key);
    }

    public boolean isEmpty() {
        return properties.isEmpty();
    }

    public Set<String> keySet() {
        return properties.keySet();
    }

    public Object put(String key, Object value) {
        return properties.put(key, value);
    }

    public void putAll(Map<? extends String, ? extends Object> t) {
        properties.putAll(t);
    }

    public Object remove(Object key) {
        return properties.remove(key);
    }

    public int size() {
        return properties.size();
    }

    public Collection<Object> values() {
        return properties.values();
    }

    public void setDelegateProperties(Map<String, Object> p) {
        this.properties = p;
    }

    public AegisDatabinding getDataBinding() {
        return databinding;
    }
    
    
}
