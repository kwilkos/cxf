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

package org.apache.cxf.tools.java2wsdl.generator;

import javax.wsdl.Binding;
import javax.wsdl.Definition;
import javax.wsdl.Port;
import javax.wsdl.Service;
import javax.wsdl.WSDLException;
import javax.wsdl.extensions.ExtensionRegistry;
import javax.xml.namespace.QName;

import org.apache.cxf.common.util.StringUtils;
import org.apache.cxf.tools.common.ToolException;
import org.apache.cxf.tools.common.WSDLConstants;
import org.apache.cxf.tools.common.extensions.soap.SoapAddress;
import org.apache.cxf.tools.common.model.WSDLModel;
import org.apache.cxf.tools.util.SOAPBindingUtil;

public class ServiceGenerator {    
    private WSDLModel wmodel;
    private Definition definition;
    private ExtensionRegistry extensionRegistry;
    
    public ServiceGenerator(WSDLModel model) {
        this.definition = model.getDefinition();
        this.wmodel = model;
        extensionRegistry = definition.getExtensionRegistry();
        
    }
    
    public void generate() {
        generate(false);
    }
    
    private String getAddressName() {
        String contextName = wmodel.getServiceName();
        if (StringUtils.isEmpty(contextName)) {
            contextName = "changeme";
        }
        return "http://localhost:9000/" + contextName;
    }
    
    public void generate(boolean isSOAP12) {
        Service service = definition.createService();
        service.setQName(new QName(WSDLConstants.WSDL_PREFIX, wmodel.getServiceName()));
        Port port = definition.createPort();
        port.setName(wmodel.getPortName());
        Binding binding = definition.createBinding();
        String targetNameSpace = wmodel.getTargetNameSpace();
        binding.setQName(new QName(targetNameSpace, wmodel.getPortTypeName() + "Binding"));
        port.setBinding(binding);
        SoapAddress soapAddress = null;
        try {
            soapAddress = SOAPBindingUtil.createSoapAddress(extensionRegistry, isSOAP12);
            soapAddress.setLocationURI(getAddressName());
        } catch (WSDLException e) {
            throw new ToolException(e.getMessage(), e);
        }
        port.addExtensibilityElement(soapAddress);
        service.addPort(port);
        definition.addService(service);
    }
}
