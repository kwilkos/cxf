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
package org.apache.cxf.service.factory;

import javax.xml.namespace.QName;

import org.apache.cxf.binding.soap.Soap11;
import org.apache.cxf.binding.soap.SoapVersion;
import org.apache.cxf.binding.soap.model.SoapBindingInfo;
import org.apache.cxf.binding.soap.model.SoapOperationInfo;
import org.apache.cxf.service.model.BindingInfo;
import org.apache.cxf.service.model.BindingOperationInfo;
import org.apache.cxf.service.model.OperationInfo;
import org.apache.cxf.service.model.ServiceInfo;

public class SoapBindingInfoFactoryBean extends AbstractBindingInfoFactoryBean {
    private SoapVersion soapVersion = Soap11.getInstance();
    private String style = "document";
    private String use;
    
    @Override
    public BindingInfo create() {
        ServiceInfo si = getServiceInfo();
        SoapBindingInfo info = new SoapBindingInfo(si, "http://schemas.xmlsoap.org/wsdl/soap/", soapVersion);
        
        info.setName(getBindingName());
        info.setStyle(getStyle());
        info.setTransportURI(getTransportURI());
        
        for (OperationInfo op : si.getInterface().getOperations()) {
            SoapOperationInfo sop = new SoapOperationInfo();
            sop.setAction(getSoapAction(op));
            sop.setStyle(getStyle(op));
            
            BindingOperationInfo bop = 
                info.buildOperation(op.getName(), op.getInputName(), op.getOutputName());
            
            bop.addExtensor(sop);
            
            info.addOperation(bop);
        }
        
        return info;
    }

    private String getSoapAction(OperationInfo op) {
        return "";
    }

    protected String getTransportURI() {
        return "http://schemas.xmlsoap.org/wsdl/soap/http";
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
    
}
