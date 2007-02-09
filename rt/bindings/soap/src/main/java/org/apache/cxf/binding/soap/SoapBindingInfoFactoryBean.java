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

import javax.xml.namespace.QName;

import org.apache.cxf.binding.soap.model.SoapBindingInfo;
import org.apache.cxf.binding.soap.model.SoapHeaderInfo;
import org.apache.cxf.binding.soap.model.SoapOperationInfo;
import org.apache.cxf.message.Message;
import org.apache.cxf.service.factory.AbstractBindingInfoFactoryBean;
import org.apache.cxf.service.model.BindingInfo;
import org.apache.cxf.service.model.BindingMessageInfo;
import org.apache.cxf.service.model.BindingOperationInfo;
import org.apache.cxf.service.model.MessageInfo;
import org.apache.cxf.service.model.MessagePartInfo;
import org.apache.cxf.service.model.OperationInfo;
import org.apache.cxf.service.model.ServiceInfo;

public class SoapBindingInfoFactoryBean extends AbstractBindingInfoFactoryBean {
    private SoapVersion soapVersion = Soap11.getInstance();
    private String style = "document";
    private String use;
    private String transportURI = "http://schemas.xmlsoap.org/soap/http";
    private boolean mtomEnabled;
    
    @Override
    public BindingInfo create() {
        ServiceInfo si = getServiceInfo();
        SoapBindingInfo info = new SoapBindingInfo(si, "http://schemas.xmlsoap.org/wsdl/soap/", soapVersion);
        
        info.setName(getBindingName());
        info.setStyle(getStyle());
        info.setTransportURI(getTransportURI());
        
        if (mtomEnabled) {
            info.setProperty(Message.MTOM_ENABLED, Boolean.TRUE);
        }
        
        for (OperationInfo op : si.getInterface().getOperations()) {
            SoapOperationInfo sop = new SoapOperationInfo();
            sop.setAction(getSoapAction(op));
            sop.setStyle(getStyle(op));
            
            BindingOperationInfo bop = 
                info.buildOperation(op.getName(), op.getInputName(), op.getOutputName());
            
            bop.addExtensor(sop);
            
            info.addOperation(bop);
            
            
            BindingMessageInfo bInput = bop.getInput();
            if (bInput != null) {
                MessageInfo input = null;
                BindingMessageInfo unwrappedMsg = bInput;
                if (bop.isUnwrappedCapable()) {
                    input = bop.getOperationInfo().getUnwrappedOperation().getInput();
                    unwrappedMsg = bop.getUnwrappedOperation().getInput();
                } else {
                    input = bop.getOperationInfo().getInput();
                }
                setupHeaders(bop, bInput, unwrappedMsg, input);
            }
            
            BindingMessageInfo bOutput = bop.getOutput();
            if (bOutput != null) {
                MessageInfo output = null;
                BindingMessageInfo unwrappedMsg = bOutput;
                if (bop.isUnwrappedCapable()) {
                    output = bop.getOperationInfo().getUnwrappedOperation().getOutput();
                    unwrappedMsg = bop.getUnwrappedOperation().getOutput();
                } else {
                    output = bop.getOperationInfo().getOutput();
                }
                setupHeaders(bop, bOutput, unwrappedMsg, output);
            }
        }
        
        return info;
    }

    private void setupHeaders(BindingOperationInfo op, 
                              BindingMessageInfo bMsg, 
                              BindingMessageInfo unwrappedBMsg, 
                              MessageInfo msg) {
        List<MessagePartInfo> parts = new ArrayList<MessagePartInfo>();
        for (MessagePartInfo part : msg.getMessageParts()) {
            if (isHeader(op, part)) {
                SoapHeaderInfo headerInfo = new SoapHeaderInfo();
                headerInfo.setPart(part);
                headerInfo.setUse(getUse());

                bMsg.addExtensor(headerInfo);
            } else {
                parts.add(part);
            }
        }
        unwrappedBMsg.setMessageParts(parts);
    }

    protected boolean isHeader(BindingOperationInfo op, MessagePartInfo part) {
        return false;
    }

    private String getSoapAction(OperationInfo op) {
        return "";
    }

    public String getTransportURI() {
        return transportURI;
    }

    public void setTransportURI(String transportURI) {
        this.transportURI = transportURI;
    }

    protected String getStyle() {
        return style;
    }

    protected String getStyle(OperationInfo op) {
        return getStyle();
    }

    protected QName getBindingName() {
        ServiceInfo si = getServiceInfo();
        return new QName(si.getName().getNamespaceURI(), 
                         si.getName().getLocalPart() + "SoapBinding");
    }

    public SoapVersion getSoapVersion() {
        return soapVersion;
    }

    public void setSoapVersion(SoapVersion soapVersion) {
        this.soapVersion = soapVersion;
    }

    public String getUse() {
        return use;
    }

    public void setUse(String use) {
        this.use = use;
    }

    public void setStyle(String style) {
        this.style = style;
    }

    public boolean isMtomEnabled() {
        return mtomEnabled;
    }

    public void setMtomEnabled(boolean mtomEnabled) {
        this.mtomEnabled = mtomEnabled;
    }
    
}
