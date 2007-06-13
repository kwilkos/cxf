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

import java.util.Set;

import javax.xml.stream.XMLStreamReader;

import org.apache.cxf.common.util.SortedArraySet;
import org.apache.cxf.message.Message;

public abstract class AbstractPhaseInterceptor<T extends Message> implements PhaseInterceptor<T> {
    private final String id;
    private String phase;
    private final Set<String> before = new SortedArraySet<String>();
    private final Set<String> after = new SortedArraySet<String>();

    /**
     * @deprecated
     */
    public AbstractPhaseInterceptor() {
        this(null, null);
    }
    
    public AbstractPhaseInterceptor(String phase) {
        this(null, phase);
    }
    public AbstractPhaseInterceptor(String i, String p) {
        super();
        id = i == null ? getClass().getName() : i;
        phase = p;
    }

    public void addBefore(String i) {
        before.add(i);
    }

    public void addAfter(String i) {
        after.add(i);
    }


    public final Set<String> getAfter() {
        return after;
    }

    public final Set<String> getBefore() {
        return before;
    }

    public final String getId() {
        return id;
    }

    public final String getPhase() {
        return phase;
    }

    /**
     * @deprecated Pass the phase into the constructor.  This method will 
     * be removed shortly
     * @param p
     */
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
