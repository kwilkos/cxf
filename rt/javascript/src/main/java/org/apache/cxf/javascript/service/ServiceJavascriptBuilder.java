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

package org.apache.cxf.javascript.service;

import java.util.logging.Logger;

import org.apache.cxf.binding.soap.model.SoapBindingInfo;
import org.apache.cxf.common.i18n.Message;
import org.apache.cxf.common.logging.LogUtils;
import org.apache.cxf.javascript.BasicNameManager;
import org.apache.cxf.javascript.JavascriptUtils;
import org.apache.cxf.javascript.NameManager;
import org.apache.cxf.javascript.UnsupportedSchemaConstruct;
import org.apache.cxf.service.ServiceModelVisitor;
import org.apache.cxf.service.model.BindingInfo;
import org.apache.cxf.service.model.FaultInfo;
import org.apache.cxf.service.model.InterfaceInfo;
import org.apache.cxf.service.model.MessageInfo;
import org.apache.cxf.service.model.MessagePartInfo;
import org.apache.cxf.service.model.OperationInfo;
import org.apache.cxf.service.model.ServiceInfo;
import org.apache.cxf.wsdl.WSDLConstants;

public class ServiceJavascriptBuilder extends ServiceModelVisitor {
    private static final Logger LOG = LogUtils.getL7dLogger(ServiceJavascriptBuilder.class);

    private boolean isRPC;
    private boolean isWrapped;
    private SoapBindingInfo soapBindingInfo;
    private JavascriptUtils utils;
    private NameManager nameManager;
    private StringBuffer code;

    public ServiceJavascriptBuilder(ServiceInfo serviceInfo) {
        super(serviceInfo);
        code = new StringBuffer();
        utils = new JavascriptUtils(code);
        nameManager = new BasicNameManager(serviceInfo);
    }

    @Override
    public void begin(FaultInfo fault) {
    }

    @Override
    public void begin(InterfaceInfo intf) {
        utils.appendLine("function " 
                         + nameManager.getJavascriptName(intf.getName())
                         + " () {");
    }

    @Override
    public void begin(MessageInfo msg) {
    }

    @Override
    public void begin(MessagePartInfo part) {
    }

    @Override
    public void begin(OperationInfo op) {
        isWrapped = !op.isUnwrappedCapable();
    }

    @Override
    public void begin(ServiceInfo service) {
        // assume only one soap binding. 
        // until further consideration.
        // hypothetically, we could generate two different JavaScript classes, one for each.
        for (BindingInfo bindingInfo : service.getBindings()) {
            if (WSDLConstants.NS_SOAP11.equals(bindingInfo.getBindingId())
                || WSDLConstants.NS_SOAP12.equals(bindingInfo.getBindingId())) { 
                SoapBindingInfo sbi = (SoapBindingInfo)bindingInfo;
                if (WSDLConstants.NS_SOAP11_HTTP_TRANSPORT.equals(sbi.getTransportURI())
                    || WSDLConstants.NS_SOAP12_HTTP_TRANSPORT.equals(sbi.getTransportURI())) {
                    soapBindingInfo = sbi;
                    break;
                }
            }
        }
        if (soapBindingInfo == null) {
            unsupportedConstruct("NO_SOAP_BINDING", service.getName());
        }
        
        isRPC = soapBindingInfo.getStyle().equals(WSDLConstants.RPC);
    }

    @Override
    public void end(FaultInfo fault) {
    }

    @Override
    public void end(InterfaceInfo intf) {
    }

    @Override
    public void end(MessageInfo msg) {
    }

    @Override
    public void end(MessagePartInfo part) {
    }

    @Override
    public void end(ServiceInfo service) {
    }
    
    private void unsupportedConstruct(String messageKey, Object... args) {
        Message message = new Message(messageKey, LOG, args);
        throw new UnsupportedSchemaConstruct(message);
    }

    public boolean isRPC() {
        return isRPC;
    }

    public void setRPC(boolean rpc) {
        this.isRPC = rpc;
    }

    public boolean isWrapped() {
        return isWrapped;
    }

    public void setWrapped(boolean wrapped) {
        this.isWrapped = wrapped;
    }

    public JavascriptUtils getUtils() {
        return utils;
    }

    public void setUtils(JavascriptUtils utils) {
        this.utils = utils;
    }

    public NameManager getNameManager() {
        return nameManager;
    }

    public void setNameManager(NameManager nameManager) {
        this.nameManager = nameManager;
    }
}
