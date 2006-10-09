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

package org.apache.cxf.binding.soap;

import java.util.ArrayList;
import java.util.List;

import org.apache.cxf.binding.Binding;
import org.apache.cxf.interceptor.AbstractBasicInterceptorProvider;
import org.apache.cxf.interceptor.Interceptor;
import org.apache.cxf.message.Message;
import org.apache.cxf.message.MessageImpl;

public class SoapBinding extends AbstractBasicInterceptorProvider implements Binding {

    // default to support mtom, left to config to turn on this feature.
    private boolean mtomEnabled;
    
    private List<Interceptor> in;
    private List<Interceptor> out;
    private List<Interceptor> fault;
    
    private SoapVersion version;
    
    public SoapBinding() {
        this(Soap11.getInstance());
    }
    
    public SoapBinding(SoapVersion v) {
        in = new ArrayList<Interceptor>();
        out = new ArrayList<Interceptor>();
        fault = new ArrayList<Interceptor>();
        
        version = v; 
    }
    
    public Message createMessage() {
        return createMessage(new MessageImpl());
    }

    public Message createMessage(Message m) {
        SoapMessage soapMessage = new SoapMessage(m);
        soapMessage.setVersion(version);

        if (mtomEnabled) {
            m.put(Message.MTOM_ENABLED, Boolean.TRUE);
        }
        return new SoapMessage(m);
    }

    public List<Interceptor> getFaultInterceptors() {
        return fault;
    }

    public List<Interceptor> getInInterceptors() {
        return in;
    }

    public List<Interceptor> getOutInterceptors() {
        return out;
    }

    public boolean isMtomEnabled() {
        return mtomEnabled;
    }

    public void setMtomEnabled(boolean enabled) {
        mtomEnabled = enabled;
    }

}
