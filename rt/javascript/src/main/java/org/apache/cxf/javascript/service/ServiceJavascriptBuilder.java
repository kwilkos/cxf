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
import org.apache.cxf.common.xmlschema.SchemaCollection;
import org.apache.cxf.javascript.JavascriptUtils;
import org.apache.cxf.javascript.NameManager;
import org.apache.cxf.javascript.NamespacePrefixAccumulator;
import org.apache.cxf.javascript.UnsupportedConstruct;
import org.apache.cxf.javascript.XmlSchemaUtils;
import org.apache.cxf.service.ServiceModelVisitor;
import org.apache.cxf.service.model.BindingInfo;
import org.apache.cxf.service.model.FaultInfo;
import org.apache.cxf.service.model.InterfaceInfo;
import org.apache.cxf.service.model.MessageInfo;
import org.apache.cxf.service.model.MessagePartInfo;
import org.apache.cxf.service.model.OperationInfo;
import org.apache.cxf.service.model.SchemaInfo;
import org.apache.cxf.service.model.ServiceInfo;
import org.apache.cxf.transport.local.LocalTransportFactory;
import org.apache.cxf.wsdl.WSDLConstants;
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
    private SchemaCollection xmlSchemaCollection;
    private SchemaInfo serviceSchemaInfo;
    private XmlSchemaElement wrapperElement;

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
        code.append("\n// Javascript for " + intf.getName() + "\n\n");
        
        currentInterfaceClassName = nameManager.getJavascriptName(intf.getName());
        operationsWithNameConflicts = new HashSet<OperationInfo>();
        code.append("function " + currentInterfaceClassName + " () {\n");
        code.append("}\n\n");
        Map<String, OperationInfo> localNameMap = new HashMap<String, OperationInfo>();
        for (OperationInfo operation : intf.getOperations()) {
            OperationInfo conflict = localNameMap.get(operation.getName().getLocalPart());
            if (conflict != null) {
                operationsWithNameConflicts.add(conflict);
                operationsWithNameConflicts.add(operation);
            }
            localNameMap.put(operation.getName().getLocalPart(), operation);
        }
        serviceSchemaInfo = serviceInfo.getSchema(serviceInfo.getTargetNamespace());
    }

    @Override
    public void begin(MessageInfo msg) {
    }

    @Override
    public void begin(MessagePartInfo part) {
    }

    private static class ElementAndNames {
        private XmlSchemaElement element;
        private String javascriptName;
        private String xmlName;

        public ElementAndNames(XmlSchemaElement element, String javascriptName, String xmlName) {
            this.element = element;
            this.javascriptName = javascriptName;
            this.xmlName = xmlName;
        }

        public XmlSchemaElement getElement() {
            return element;
        }

        public String getXmlName() {
            return xmlName;
        }

        public String getJavascriptName() {
            return javascriptName;
        }
    }

    @Override
    public void begin(OperationInfo op) {
        assert !isRPC;
        boolean isWrapped = op.isUnwrappedCapable();
        // we only process the wrapped operation, not the unwrapped alternative.
        if (op.isUnwrapped()) {
            return;
        }

        boolean needsLongName = operationsWithNameConflicts.contains(op);
        String opFunctionName;
        String opGlobalFunctionName = nameManager.getJavascriptName(op.getName()) + "_op";
        if (needsLongName) {
            opFunctionName = opGlobalFunctionName;
        } else {
            opFunctionName = JavascriptUtils.javaScriptNameToken(op.getName().getLocalPart());
        }
        List<String> inputParameterNames = new ArrayList<String>();
        MessageInfo inputMessage = op.getInput();
        String wrapperClassName = null;
        StringBuilder parameterList = new StringBuilder();

        // the message content is a set of elements. Perhaps they come from the
        // parts,
        // or perhaps we invent them.
        List<ElementAndNames> elements = new ArrayList<ElementAndNames>();
        List<MessagePartInfo> parts = null;

        if (inputMessage != null) {
            parts = inputMessage.getMessageParts();
            if (isWrapped) {
                wrapperClassName = setupWrapperElement(op, inputParameterNames, parts);
            }

            for (String param : inputParameterNames) {
                parameterList.append(param);
                parameterList.append(", ");
            }
        }

        code.append("function " + opGlobalFunctionName + "(" + parameterList
                    + "responseCallback, errorCallback) {\n");
        if (inputMessage != null) {
            utils.appendLine("var args = new Array(" + inputParameterNames.size() + ");");
            int px = 0;
            for (String param : inputParameterNames) {
                utils.appendLine("args[" + px + "] = " + param + ";");
                px++;
            }
            utils.appendLine("var xml = this.serializeInputMessage(args);");
            // more to come ...
        }
        code.append("}\n\n");
        code.append(currentInterfaceClassName + ".prototype." + opFunctionName + " = " + opGlobalFunctionName
                    + ";\n\n");

        createInputSerializer(op, isWrapped, inputParameterNames, wrapperClassName, elements,
                              parts);
    }

    private void createInputSerializer(OperationInfo op, boolean isWrapped,
                                       List<String> inputParameterNames, String wrapperClassName,
                                       List<ElementAndNames> elements, List<MessagePartInfo> parts) {
        String serializerFunctionName = nameManager.getJavascriptName(op.getName()) + "_serializeInput";
        
        code.append("function " + serializerFunctionName + "(args) {\n");
        NamespacePrefixAccumulator prefixAccumulator = new NamespacePrefixAccumulator(serviceSchemaInfo);
        for (MessagePartInfo mpi : parts) {
            XmlSchemaElement element;
            if (mpi.isElement()) {
                element = (XmlSchemaElement)mpi.getXmlSchema();
                if (element == null) {
                    element = XmlSchemaUtils.findElementByRefName(xmlSchemaCollection, mpi.getElementQName(),
                                                                  serviceInfo.getTargetNamespace());
                }
            } else {
                // there is still an element in there, but it's not a very
                // interesting element
                element = new XmlSchemaElement();
                XmlSchemaElement dummyElement = (XmlSchemaElement)mpi.getXmlSchema();
                element.setMaxOccurs(dummyElement.getMaxOccurs());
                element.setMinOccurs(dummyElement.getMinOccurs());
                element.setNillable(dummyElement.isNillable());
                element.setSchemaType(xmlSchemaCollection.getTypeByQName(mpi.getTypeQName()));
                element.setQName(mpi.getName());
            }
            assert element != null;
            assert element.getQName() != null;
            String partJavascriptVar = JavascriptUtils.javaScriptNameToken(element.getQName().getLocalPart());
            String elementXmlRef = prefixAccumulator.xmlElementString(element);

            elements.add(new ElementAndNames(element, partJavascriptVar, elementXmlRef));
        }

        // if not wrapped, the param array matches up with the parts. If wrapped, the members
        // of it have to be packed into an object.

        if (isWrapped) {
            String partJavascriptVar = elements.get(0).getJavascriptName();
            utils.appendLine("var " + partJavascriptVar + " = new " + wrapperClassName + "();");
            int px = 0;
            for (String param : inputParameterNames) {
                utils.appendLine(partJavascriptVar + ".set" + StringUtils.capitalize(param) + "(args[" + px
                                 + "]);");
                px++;
            }
            // stick this into the array in slot 0.
            utils.appendLine("args[0] = " + partJavascriptVar + ";");
        }

        utils.appendLine("var cxfutils = new CxfApacheOrgUtil();");

        SoapVersion soapVersion = soapBindingInfo.getSoapVersion();
        assert soapVersion.getVersion() == 1.1;
        utils.appendLine("var xml;");
        utils.appendLine("xml = cxfutils.beginSoap11Message(\"" + prefixAccumulator.getAttributes() + "\");");

        utils.setXmlStringAccumulator("xml");

        int px = 0;
        for (ElementAndNames partElement : elements) {
            utils.generateCodeToSerializeElement("cxfutils",
                                                 partElement.getElement(), "args[" + px + "]",
                                                 partElement.getXmlName(), xmlSchemaCollection,
                                                 serviceSchemaInfo.getNamespaceURI(), null);
            px++;
        }

        utils.appendLine("xml = xml + cxfutils.endSoap11Message();");
        utils.appendLine("return xml;");
        code.append("}\n\n");
        code.append(currentInterfaceClassName + ".prototype.serializeInputMessage = " 
                    + serializerFunctionName
                    + ";\n\n");
    }

    private String setupWrapperElement(OperationInfo op, List<String> inputParameterNames,
                                       List<MessagePartInfo> parts) {
        String wrapperClassName;
        // expect one input part.
        assert parts.size() == 1;
        MessagePartInfo wrapperPart = parts.get(0);
        // we expect a type
        assert wrapperPart.isElement();
        wrapperElement = (XmlSchemaElement)wrapperPart.getXmlSchema();
        XmlSchemaComplexType wrapperType = (XmlSchemaComplexType)XmlSchemaUtils
            .getElementType(xmlSchemaCollection, op.getName().getNamespaceURI(), wrapperElement, null);
        wrapperClassName = nameManager.getJavascriptName(wrapperType);
        XmlSchemaSequence wrapperTypeSequence = XmlSchemaUtils.getSequence(wrapperType);
        for (int i = 0; i < wrapperTypeSequence.getItems().getCount(); i++) {
            XmlSchemaObject thing = wrapperTypeSequence.getItems().getItem(i);
            if (!(thing instanceof XmlSchemaElement)) {
                XmlSchemaUtils.unsupportedConstruct("NON_ELEMENT_CHILD", thing.getClass().getSimpleName(),
                                                    wrapperType);
            }

            XmlSchemaElement elChild = (XmlSchemaElement)thing;
            inputParameterNames.add(elChild.getName());
        }
        return wrapperClassName;
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
