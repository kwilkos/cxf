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

package org.apache.cxf.phase;

import java.util.HashSet;
import java.util.Set;

import javax.xml.stream.XMLStreamReader;

import org.apache.cxf.message.Message;

public abstract class AbstractPhaseInterceptor<T extends Message> implements PhaseInterceptor<T> {
    private String id;
    private String phase;
    private Set<String> before = new HashSet<String>();
    private Set<String> after = new HashSet<String>();

    
    public AbstractPhaseInterceptor() {
        super();
        id = getClass().getName();
    }

    public void addBefore(String i) {
        before.add(i);
    }

    public void addAfter(String i) {
        after.add(i);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.cxf.phase.PhaseInterceptor#getAfter()
     */
    public Set<String> getAfter() {
        return after;
    }

    public void setAfter(Set<String> a) {
        this.after = a;
    }

    public Set<String> getBefore() {
        return before;
    }

    public void setBefore(Set<String> b) {
        this.before = b;
    }

    public String getId() {
        return id;
    }

    public void setId(String i) {
        this.id = i;
    }

    public String getPhase() {
        return phase;
    }

    public void setPhase(String p) {
        this.phase = p;
    }

    public void handleFault(T message) {
    }
    
    public boolean isGET(T message) {
        String method = (String)message.get(Message.HTTP_REQUEST_METHOD);
        return "GET".equals(method) && message.getContent(XMLStreamReader.class) == null;
    }
}
