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

package org.apache.cxf.message;

import java.util.HashMap;

import org.apache.cxf.transport.Conduit;
import org.apache.cxf.transport.Destination;
import org.apache.cxf.transport.Session;

public class ExchangeImpl extends HashMap<String, Object> implements Exchange {

    private Destination destination;
    private Conduit conduit;
    private boolean oneWay;
    
    private Message inMessage;
    private Message outMessage;
    private Message faultMessage;
    
    private Session session;
    
    public Destination getDestination() {
        return destination;
    }

    public Message getInMessage() {
        return inMessage;
    }

    public Conduit getConduit() {
        return conduit;
    }

    public Message getOutMessage() {
        return outMessage;
    }

    public Message getFaultMessage() {
        return faultMessage;
    }

    public void setFaultMessage(Message m) {
        this.faultMessage = m;
        m.setExchange(this);
    }

    public void setDestination(Destination d) {
        destination = d;
    }

    public void setInMessage(Message m) {
        inMessage = m;
        m.setExchange(this);
    }

    public void setConduit(Conduit c) {
        conduit = c;
    }

    public void setOutMessage(Message m) {
        outMessage = m;
        if (null != m) {
            m.setExchange(this);
        }
    }
    
    public <T> T get(Class<T> key) {
        return key.cast(get(key.getName()));
    }

    public <T> void put(Class<T> key, T value) {
        put(key.getName(), value);
    }

    public boolean isOneWay() {
        return oneWay;
    }

    public void setOneWay(boolean b) {
        oneWay = b;
    }

    public Session getSession() {
        return session;
    }

    public void setSession(Session session) {
        this.session = session;
    }
}
