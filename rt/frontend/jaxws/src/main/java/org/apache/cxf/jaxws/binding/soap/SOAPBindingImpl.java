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

package org.apache.cxf.jaxws.binding.soap;


import java.util.Set;

import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPFactory;
import javax.xml.ws.soap.SOAPBinding;

import org.apache.cxf.jaxws.binding.BindingImpl;
import org.apache.cxf.message.Message;
import org.apache.cxf.service.model.BindingInfo;

public class SOAPBindingImpl extends BindingImpl implements SOAPBinding {

    
    private BindingInfo soapBinding;
    
    // private SoapBinding soapBinding;

    public SOAPBindingImpl(BindingInfo sb) {
        soapBinding = sb;
        
    }
    
    public Set<String> getRoles() {
        return null;
    }

    public void setRoles(Set<String> set) {
        // TODO
    }

    public boolean isMTOMEnabled() {
        return Boolean.TRUE.equals(soapBinding.getProperty(Message.MTOM_ENABLED));
    }

    public void setMTOMEnabled(boolean flag) {
        
        soapBinding.setProperty(Message.MTOM_ENABLED, flag);
    }

    public MessageFactory getMessageFactory() {
        // TODO: get from wrapped SoapBinding
        return null;
    }  

    public SOAPFactory getSOAPFactory() {
        // TODO: get from wrapped SoapBinding
        return null;
    }
}
