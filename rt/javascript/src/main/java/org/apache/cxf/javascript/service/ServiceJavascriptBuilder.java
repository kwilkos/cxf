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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import org.apache.cxf.binding.soap.SoapBindingConstants;
import org.apache.cxf.binding.soap.SoapVersion;
import org.apache.cxf.binding.soap.model.SoapBindingInfo;
import org.apache.cxf.common.i18n.Message;
import org.apache.cxf.common.logging.LogUtils;
import org.apache.cxf.common.util.StringUtils;
import org.apache.cxf.javascript.JavascriptUtils;
import org.apache.cxf.javascript.NameManager;
import org.apache.cxf.javascript.UnsupportedConstruct;
import org.apache.cxf.javascript.XmlSchemaUtils;
import org.apache.cxf.service.ServiceModelVisitor;
import org.apache.cxf.service.model.BindingInfo;
import org.apache.cxf.service.model.FaultInfo;
import org.apache.cxf.service.model.InterfaceInfo;
import org.apache.cxf.service.model.MessageInfo;
import org.apache.cxf.service.model.MessagePartInfo;
import org.apache.cxf.service.model.OperationInfo;
import org.apache.cxf.service.model.ServiceInfo;
import org.apache.cxf.transport.local.LocalTransportFactory;
import org.apache.cxf.wsdl.WSDLConstants;
import org.apache.ws.commons.schema.XmlSchemaCollection;
import org.apache.ws.commons.schema.XmlSchemaComplexType;
import org.apache.ws.commons.schema.XmlSchemaElement;
import org.apache.ws.commons.schema.XmlSchemaObject;
import org.apache.ws.commons.schema.XmlSchemaSequence;

class ServiceJavascriptBuilder extends ServiceModelVisitor {
    private static final Logger LOG = LogUtils.getL7dLogger(ServiceJavascriptBuilder.class);

    private boolean isRPC;
    private SoapBindingInfo soapBindingInfo;
    private JavascriptUtils utils;
    private NameManager nameManager;
    private StringBuilder code;
    private String currentInterfaceClassName;
    private Set<OperationInfo> operationsWithNameConflicts;
    private XmlSchemaCollection xmlSchemaCollection;

    public ServiceJavascriptBuilder(ServiceInfo serviceInfo, NameManager nameManager) {
        super(serviceInfo);
        code = new StringBuilder();
        utils = new JavascriptUtils(code);
        this.nameManager = nameManager;
        xmlSchemaCollection = serviceInfo.getXmlSchemaCollection();
    }
    
    public String getCode() {
        return code.toString();
    }

    @Override
    public void begin(FaultInfo fault) {
    }

    @Override
    public void begin(InterfaceInfo intf) {
        currentInterfaceClassName = nameManager.getJavascriptName(intf.getName());
        operationsWithNameConflicts = new HashSet<OperationInfo>();
        utils.appendLine("function " + currentInterfaceClassName + " () {");
        utils.appendLine("}");
        Map<String, OperationInfo> localNameMap = new HashMap<String, OperationInfo>();
        for (OperationInfo operation : intf.getOperations()) {
            OperationInfo conflict = localNameMap.get(operation.getName().getLocalPart());
            if (conflict != null) {
                operationsWithNameConflicts.add(conflict);
                operationsWithNameConflicts.add(operation);
            }
            localNameMap.put(operation.getName().getLocalPart(), operation);
        }
    }

    @Override
    public void begin(MessageInfo msg) {
    }

    @Override
    public void begin(MessagePartInfo part) {
    }

    @Override
    public void begin(OperationInfo op) {
        assert !isRPC;
        boolean isWrapped = op.isUnwrappedCapable();
        // to make best use of the visitor scheme, we wait until end to
        // create the function, since the message function can participate in
        // building the argument list.
        boolean needsLongName = operationsWithNameConflicts.contains(op);
        String opFunctionName;
        if (needsLongName) {
            opFunctionName = nameManager.getJavascriptName(op.getName());
        } else {
            opFunctionName = JavascriptUtils.javaScriptNameToken(op.getName().getLocalPart());
        }
        List<String> inputParameterNames = new ArrayList<String>();
        MessageInfo inputMessage = op.getInput();
        String wrapperClassName = null;
        StringBuilder parameterList = new StringBuilder();
        XmlSchemaElement wrapperElement = null;

        if (inputMessage != null) {
            List<MessagePartInfo> parts = inputMessage.getMessageParts();
            if (isWrapped) {
                // expect one input part.
                assert parts.size() == 1;
                MessagePartInfo wrapperPart = parts.get(0);
                // we expect a type
                assert wrapperPart.isElement();
                wrapperElement = (XmlSchemaElement)wrapperPart.getXmlSchema();
                XmlSchemaComplexType wrapperType = 
                    (XmlSchemaComplexType)XmlSchemaUtils.getElementType(xmlSchemaCollection, 
                                                                        op.getName().getNamespaceURI(), 
                                                                        wrapperElement,
                                                                        null);
                wrapperClassName = nameManager.getJavascriptName(wrapperType);
                XmlSchemaSequence wrapperTypeSequence = XmlSchemaUtils.getSequence(wrapperType);
                for (int i = 0; i < wrapperTypeSequence.getItems().getCount(); i++) {
                    XmlSchemaObject thing = wrapperTypeSequence.getItems().getItem(i);
                    if (!(thing instanceof XmlSchemaElement)) {
                        XmlSchemaUtils.unsupportedConstruct("NON_ELEMENT_CHILD", thing.getClass()
                            .getSimpleName(), wrapperType);
                    }

                    XmlSchemaElement elChild = (XmlSchemaElement)thing;
                    inputParameterNames.add(elChild.getName());
                }
            }

            for (String param : inputParameterNames) {
                parameterList.append(param);
                parameterList.append(", ");
            }
        }

        // note that these functions operate in terms of async callbacks, they
        // don't
        // ever have return values. Hypothetically, I suppose that users who
        // wanted a
        // synchronous behavior might want a synchronous function (rather like
        // the Microsoft
        // wsdl.exe behavior), but I'm not going to worry about it for now.
        utils.appendLine("function " + opFunctionName + "(" + parameterList
                         + "responseCallback, errorCallback) {");
        
        // wrapped
        if (wrapperClassName != null) {
            utils.appendLine("var wrapper = new " + wrapperClassName + "();");
            for (String param : inputParameterNames) {
                utils.appendLine("wrapper.set" + StringUtils.capitalize(param) + "(" + param + ");");
            }
        }
        
        SoapVersion soapVersion = soapBindingInfo.getSoapVersion();
        assert soapVersion.getVersion() == 1.1;
        // we could have less code cooked in 

        utils.appendLine("}");
    }

    @Override
    public void end(OperationInfo op) {
    }

    @Override
    public void begin(ServiceInfo service) {
        // assume only one soap binding.
        // until further consideration.
        // hypothetically, we could generate two different JavaScript classes,
        // one for each.
        for (BindingInfo bindingInfo : service.getBindings()) {
            if (SoapBindingConstants.SOAP11_BINDING_ID.equals(bindingInfo.getBindingId())
                || SoapBindingConstants.SOAP11_BINDING_ID.equals(bindingInfo.getBindingId())) {
                SoapBindingInfo sbi = (SoapBindingInfo)bindingInfo;
                if (WSDLConstants.NS_SOAP11_HTTP_TRANSPORT.equals(sbi.getTransportURI())
                    || WSDLConstants.NS_SOAP12_HTTP_TRANSPORT.equals(sbi.getTransportURI())
                    // we may want this for testing.
                    || LocalTransportFactory.TRANSPORT_ID.equals(sbi.getTransportURI())) {
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
        throw new UnsupportedConstruct(message);
    }
}
