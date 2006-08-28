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

package org.apache.cxf.jaxws.context;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.xml.ws.handler.MessageContext;

import org.apache.cxf.message.Message;

public class WrappedMessageContext implements MessageContext {

    private Message message;
    private Map<String, Scope> scopes = new HashMap<String, Scope>();

    public WrappedMessageContext(Message m) {
        message = m;
    }
    
    public Message getWrappedMessage() {
        return message;
    }
    
    public void clear() {
        message.clear();      
    }

    public boolean containsKey(Object key) {
        return message.containsKey(key);
    }

    public boolean containsValue(Object value) {
        return message.containsValue(value);
    }

    public Set<Entry<String, Object>> entrySet() {
        return message.entrySet();
    }

    public Object get(Object key) {
        return message.get(key);
    }

    public boolean isEmpty() {
        return message.isEmpty();
    }

    public Set<String> keySet() {
        return message.keySet();
    }

    public Object put(String key, Object value) {
        return message.put(key, value);
    }

    public void putAll(Map<? extends String, ? extends Object> t) {
        message.putAll(t);
    }

    public Object remove(Object key) {
        return message.remove(key);
    }

    public int size() {
        return message.size();
    }

    public Collection<Object> values() {
        return message.values();
    }

    public void setScope(String arg0, Scope arg1) {
        if (!this.containsKey(arg0)) {
            throw new IllegalArgumentException("non-existant property-" + arg0 + "is specified");    
        }
        scopes.put(arg0, arg1);        
    }

    public Scope getScope(String arg0) {
        
        if (containsKey(arg0)) {
            if (scopes.containsKey(arg0)) {
                return scopes.get(arg0);
            } else {
                return Scope.HANDLER;
            }
        }
        throw new IllegalArgumentException("non-existant property-" + arg0 + "is specified");
    }
    
    
}
