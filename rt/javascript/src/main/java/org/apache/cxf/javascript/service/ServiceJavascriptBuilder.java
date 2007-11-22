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

import javax.xml.namespace.QName;

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

public class ServiceJavascriptBuilder extends ServiceModelVisitor {
    private static final Logger LOG = LogUtils.getL7dLogger(ServiceJavascriptBuilder.class);

    private boolean isRPC;
    private SoapBindingInfo soapBindingInfo;
    private JavascriptUtils utils;
    private NameManager nameManager;
    private StringBuilder code;
    private String currentInterfaceClassName;
    private OperationInfo currentOperation;
    private Set<OperationInfo> operationsWithNameConflicts;
    private Set<MessageInfo> inputMessagesWithNameConflicts;
    private Set<MessageInfo> outputMessagesWithNameConflicts;
    private SchemaCollection xmlSchemaCollection;
    private SchemaInfo serviceSchemaInfo;
    private XmlSchemaElement wrapperElement;
    private NamespacePrefixAccumulator prefixAccumulator;
    private BindingInfo xmlBindingInfo;
    private Map<String, OperationInfo> localOperationsNameMap;
    private Map<String, MessageInfo> localInputMessagesNameMap;
    private Map<String, MessageInfo> localOutputMessagesNameMap;

    private String opFunctionPropertyName;
    private String opFunctionGlobalName;

    public ServiceJavascriptBuilder(ServiceInfo serviceInfo, NamespacePrefixAccumulator prefixAccumulator,
                                    NameManager nameManager) {
        super(serviceInfo);
        code = new StringBuilder();
        utils = new JavascriptUtils(code);
        this.nameManager = nameManager;
        xmlSchemaCollection = serviceInfo.getXmlSchemaCollection();
        this.prefixAccumulator = prefixAccumulator;
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
        inputMessagesWithNameConflicts = new HashSet<MessageInfo>();
        outputMessagesWithNameConflicts = new HashSet<MessageInfo>();
        localOperationsNameMap = new HashMap<String, OperationInfo>();
        localInputMessagesNameMap = new HashMap<String, MessageInfo>();
        localOutputMessagesNameMap = new HashMap<String, MessageInfo>();

        code.append("function " + currentInterfaceClassName + " () {\n");
        utils.appendLine("this.jsutils = new CxfApacheOrgUtil();");
        utils.appendLine("this.synchronous = false;");
        utils.appendLine("this.url = null;");
        utils.appendLine("this.client = null;");
        utils.appendLine("this.response = null;");
        // the callback functions for a pending operation are stored in these.
        // thus, only one pending operation at a time.
        utils.appendLine("this._onsuccess = null;");
        utils.appendLine("this._onerror = null;");
        code.append("}\n\n");

        serviceSchemaInfo = serviceInfo.getSchema(serviceInfo.getTargetNamespace());
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
    
    private String getFunctionGlobalName(QName itemName, String itemType) {
        return nameManager.getJavascriptName(itemName) + "_" + itemType; 
    }
    
    
    private<T> String getFunctionPropertyName(Set<T> conflictMap, T object, QName fullName) {
        boolean needsLongName = conflictMap.contains(object);
        String functionName;
        if (needsLongName) {
            functionName = nameManager.getJavascriptName(fullName);
        } else {
            functionName = JavascriptUtils.javaScriptNameToken(fullName.getLocalPart());
        }
        return functionName;
        
    }

    // we do this at the end so we can inventory name conflicts sooner.
    @Override
    public void end(OperationInfo op) {
        // we only process the wrapped operation, not the unwrapped alternative.
        if (op.isUnwrapped()) {
            return;
        }

        if (isRPC) {
            unsupportedConstruct("RPC", op.getInterface().getName().toString());
        }
        boolean isWrapped = op.isUnwrappedCapable();
        List<String> inputParameterNames = new ArrayList<String>();
        MessageInfo inputMessage = op.getInput();
        String wrapperClassName = null;
        StringBuilder parameterList = new StringBuilder();

        List<MessagePartInfo> parts = null;

        if (inputMessage != null) {
            parts = inputMessage.getMessageParts();
            if (isWrapped) {
                wrapperClassName = setupWrapperElement(op, inputParameterNames, parts);
            }

            buildParameterList(inputParameterNames, parameterList);
        }

        MessageInfo outputMessage = op.getOutput();
        buildSuccessFunction(outputMessage);

        buildErrorFunction(); // fault part some day.

        buildOperationFunction(inputParameterNames, inputMessage, parameterList);

        createInputSerializer(inputMessage, isWrapped, inputParameterNames, wrapperClassName, parts);

        if (outputMessage != null) {
            createResponseDeserializer(outputMessage);
        }
    }

    private void buildOperationFunction(List<String> inputParameterNames,
                                        MessageInfo inputMessage, 
                                        StringBuilder parameterList) {
        code.append("function " 
                    +  opFunctionGlobalName
                    + "(successCallback, errorCallback"
                    + ((parameterList.length() > 0) ? ", " + parameterList : "") + ") {\n");
        utils.appendLine("var xml = null;");
        if (inputMessage != null) {
            utils.appendLine("var args = new Array(" + inputParameterNames.size() + ");");
            int px = 0;
            for (String param : inputParameterNames) {
                utils.appendLine("args[" + px + "] = " + param + ";");
                px++;
            }
            utils.appendLine("xml = this."
                             + getFunctionPropertyName(inputMessagesWithNameConflicts,
                                                       inputMessage, 
                                                       inputMessage.getName())
                             + "_serializeInput"
                             + "(args);");
        }
        utils.appendLine("this.client = new CxfApacheOrgClient(jsutils);");
        // we need to pass the caller's callback functions to our callback
        // functions.
        utils.appendLine("this._onsuccess = successCallback;");
        utils.appendLine("this._onerror = errorCallback;");
        utils.appendLine("this.client.onsuccess = this." 
                         + opFunctionPropertyName
                         + "_onsuccess");
        utils.appendLine("this.client.onerror = this."
                         + opFunctionPropertyName
                         + "_error");

        utils.appendLine("var requestHeaders = [];");

        if (soapBindingInfo != null) {
            String action = soapBindingInfo.getSoapAction(currentOperation);
            utils.appendLine("requestHeaders['SOAPAction'] = '" + action + "';");
        }

        // default method by passing null. Is there some place this lives in the
        // service model?
        utils.appendLine("this.client.request(this.url, xml, null, this.synchronous, requestHeaders);");

        code.append("}\n\n");
        code.append(currentInterfaceClassName + ".prototype." 
                    + opFunctionPropertyName 
                    + " = " 
                    + opFunctionGlobalName
                    + ";\n\n");
    }

    private void buildErrorFunction() {
        String errorFunctionPropertyName = opFunctionPropertyName + "_onerror";
        String errorFunctionGlobalName = opFunctionGlobalName + "_onerror";
        
        code.append("function " + errorFunctionGlobalName + "() {\n");
        utils.startIf("this._onerror");
        // Is this a good set of parameters for the error function?
        // Not if we want to process faults, it isn't. To be revisited.
        utils.appendLine("this._onerror(this.client.req.status, this.client.req.statusText);");
        utils.endBlock();
        code.append("}\n\n");
        code.append(currentInterfaceClassName + ".prototype." 
                    + errorFunctionPropertyName 
                    + " = "
                    + errorFunctionGlobalName 
                    + ";\n\n");
    }

    private void buildSuccessFunction(MessageInfo outputMessage) {
        // Here are the success and error callbacks. They have the job of
        // calling
        // the callbacks provided to the operation function with appropriate
        // parameters.
        String successFunctionGlobalName = opFunctionGlobalName + "_onsuccess"; 
        String successFunctionPropertyName = opFunctionPropertyName + "_onsuccess"; 
        code.append("function " + successFunctionGlobalName + "(responseXml) {\n");
        utils.startIf("_onsuccess");
        utils.appendLine("var responseObject = null;");
        if (outputMessage != null) {
            if (soapBindingInfo != null) { // soap
                // The following code is probably only right for basic
                // Doc/Literal/Wrapped services.
                // the top element should be the Envelope, then the Body, then
                // the actual response item.
                utils.appendLine("var element = this.jsutils.getFirstElementChild(responseXml);");
                // Go down one more from the body to the response item.
                utils.appendLine("element = this.jsutils.getFirstElementChild(responseXml);");
            } else if (xmlBindingInfo != null) {
                utils.appendLine("var element = responseXml;");
            }

            String deserializerFunctionName = outputDeserializerFunctionName(outputMessage);
            utils.appendLine("responseObject = " + deserializerFunctionName + "(this.jsutils, element);");
        }
        utils.appendLine("this._onsuccess(responseObject);");
        utils.endBlock();
        code.append("}\n\n");
        code.append(currentInterfaceClassName + ".prototype." 
                    + successFunctionPropertyName 
                    + " = "
                    + successFunctionGlobalName + ";\n\n");
    }

    private void buildParameterList(List<String> inputParameterNames, StringBuilder parameterList) {
        for (String param : inputParameterNames) {
            parameterList.append(param);
            parameterList.append(", ");
        }
        // trim last comma.
        if (parameterList.length() > 2) {
            parameterList.setLength(parameterList.length() - 2);
        }
    }

    private String outputDeserializerFunctionName(MessageInfo message) {
        return getFunctionGlobalName(message.getName(), "deserializeResponse");
    }

    private void createResponseDeserializer(MessageInfo outputMessage) {
        List<MessagePartInfo> parts = outputMessage.getMessageParts();
        if (parts.size() != 1) {
            unsupportedConstruct("MULTIPLE_OUTPUTS", outputMessage.getName().toString());
        }
        List<ElementAndNames> elements = new ArrayList<ElementAndNames>();
        String functionName = outputDeserializerFunctionName(outputMessage);
        code.append("function " + functionName + "(cxfjsutils, partElement) {\n");
        getElementsForParts(elements, parts);
        ElementAndNames element = elements.get(0);
        XmlSchemaComplexType type = (XmlSchemaComplexType)element.getElement().getSchemaType();
        assert type != null;
        String typeObjectName = nameManager.getJavascriptName(type);
        utils
            .appendLine("var returnObject = " + typeObjectName + "_deserialize (cxfjsutils, partElement);\n");
        utils.appendLine("return returnObject;");
        code.append("}\n");
    }

    private void createInputSerializer(MessageInfo msg, boolean isWrapped, List<String> inputParameterNames,
                                       String wrapperClassName, List<MessagePartInfo> parts) {
        List<ElementAndNames> elements = new ArrayList<ElementAndNames>();
        
        String serializerFunctionGlobalName = getFunctionGlobalName(msg.getName(), "serializeInput");
        String serializerFunctionPropertyName = 
            getFunctionPropertyName(inputMessagesWithNameConflicts, msg, msg.getName());

        code.append("function " + serializerFunctionGlobalName + "(args) {\n");
        getElementsForParts(elements, parts);

        // if not wrapped, the param array matches up with the parts. If
        // wrapped, the members
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

        if (soapBindingInfo != null) {
            SoapVersion soapVersion = soapBindingInfo.getSoapVersion();
            assert soapVersion.getVersion() == 1.1;
            utils.appendLine("var xml;");
            utils.appendLine("xml = cxfutils.beginSoap11Message(\"" + prefixAccumulator.getAttributes()
                             + "\");");
        } else {
            // other alternative is XML, which isn't really all here yet.
            unsupportedConstruct("XML_BINDING", currentInterfaceClassName, xmlBindingInfo.getName());
        }

        utils.setXmlStringAccumulator("xml");

        int px = 0;
        for (ElementAndNames partElement : elements) {
            utils.generateCodeToSerializeElement("cxfutils", partElement.getElement(), "args[" + px + "]",
                                                 partElement.getXmlName(), xmlSchemaCollection,
                                                 serviceSchemaInfo.getNamespaceURI(), null);
            px++;
        }

        utils.appendLine("xml = xml + cxfutils.endSoap11Message();");
        utils.appendLine("return xml;");
        code.append("}\n\n");
        code.append(currentInterfaceClassName + ".prototype."
                    + serializerFunctionPropertyName 
                    + " = "
                    + serializerFunctionGlobalName + ";\n\n");
    }

    private void getElementsForParts(List<ElementAndNames> elements, List<MessagePartInfo> parts) {
        for (MessagePartInfo mpi : parts) {
            XmlSchemaElement element;
            if (mpi.isElement()) {
                element = (XmlSchemaElement)mpi.getXmlSchema();
                if (element == null) {
                    element = XmlSchemaUtils.findElementByRefName(xmlSchemaCollection, mpi.getElementQName(),
                                                                  serviceInfo.getTargetNamespace());
                }
            } else {
                // dkulp may have fixed the problem that caused me to write this
                // code.
                // aside from the fact that in the !isElement case (rpc) we have
                // other work to do.
                LOG.severe("Missing element " + mpi.getElementQName().toString() + " in "
                           + mpi.getName().toString());
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
            String elementXmlRef = prefixAccumulator.xmlElementString(mpi.getConcreteName());

            elements.add(new ElementAndNames(element, partJavascriptVar, elementXmlRef));
        }
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
    public void begin(ServiceInfo service) {

        BindingInfo xml = null;
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
            } else if (WSDLConstants.NS_BINDING_XML.equals(bindingInfo.getBindingId())) {
                xml = bindingInfo;
            }
        }

        // For now, we use soap if its available, and XML if it isn't.\
        if (soapBindingInfo == null && xml == null) {
            unsupportedConstruct("NO_USABLE_BINDING", service.getName());
        }

        if (soapBindingInfo != null) {
            isRPC = soapBindingInfo.getStyle().equals(WSDLConstants.RPC);
        } else if (xml != null) {
            xmlBindingInfo = xml;
        }
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
        LOG.fine(getCode());
    }

    private void unsupportedConstruct(String messageKey, Object... args) {
        Message message = new Message(messageKey, LOG, args);
        throw new UnsupportedConstruct(message);
    }

    @Override
    public void begin(OperationInfo op) {
        if (op.isUnwrapped()) {
            return;
        }
        currentOperation = op;
        OperationInfo conflict = localOperationsNameMap.get(op.getName().getLocalPart());
        if (conflict != null) {
            operationsWithNameConflicts.add(conflict);
            operationsWithNameConflicts.add(op);
        }
        localOperationsNameMap.put(op.getName().getLocalPart(), op);
        opFunctionPropertyName = getFunctionPropertyName(operationsWithNameConflicts, op, op.getName());
        opFunctionGlobalName = getFunctionGlobalName(op.getName(), "op");
    }
    
    @Override
    public void begin(MessageInfo msg) {
        Map<String, MessageInfo> nameMap;
        Set<MessageInfo> conflicts;
        if (msg.getType() == MessageInfo.Type.INPUT) {
            nameMap = localInputMessagesNameMap;
            conflicts = inputMessagesWithNameConflicts;
        } else {
            nameMap = localOutputMessagesNameMap;
            conflicts = outputMessagesWithNameConflicts;

        }
        MessageInfo conflict = nameMap.get(msg.getName().getLocalPart());
        if (conflict != null) {
            conflicts.add(conflict);
            conflicts.add(msg);
        }
        nameMap.put(msg.getName().getLocalPart(), msg);
    }


}
