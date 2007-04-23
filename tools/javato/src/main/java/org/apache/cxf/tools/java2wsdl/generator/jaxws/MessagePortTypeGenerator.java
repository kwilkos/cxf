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

package org.apache.cxf.tools.java2wsdl.generator.jaxws;

import java.util.Iterator;

import javax.jws.soap.SOAPBinding;
import javax.wsdl.Definition;
import javax.wsdl.Fault;
import javax.wsdl.Input;
import javax.wsdl.Message;
import javax.wsdl.Operation;
import javax.wsdl.Output;
import javax.wsdl.Part;
import javax.wsdl.PortType;
import javax.xml.namespace.QName;

import com.sun.xml.bind.api.JAXBRIContext;

import org.apache.cxf.tools.common.model.JavaMethod;
import org.apache.cxf.tools.common.model.JavaParameter;
import org.apache.cxf.tools.common.model.WSDLModel;
import org.apache.cxf.tools.common.model.WSDLParameter;

public class MessagePortTypeGenerator {
    private WSDLModel wmodel;
    private Definition definition;

    public MessagePortTypeGenerator(WSDLModel model) {
        this.definition = model.getDefinition();
        this.wmodel = model;
    }

    public void generate() {
        PortType portType = definition.createPortType();
        portType.setQName(new QName(wmodel.getTargetNameSpace(), wmodel.getPortTypeName()));
        portType.setUndefined(false);
        for (JavaMethod method : wmodel.getJavaMethods()) {
            Operation operation = definition.createOperation();
            operation.setName(method.getName());
            operation.setUndefined(false);

            Message inputMessage = null;
            inputMessage = definition.createMessage();
            inputMessage.setQName(new QName(wmodel.getTargetNameSpace(), method.getName()));
            processInputMessage(operation, method, inputMessage);

            Message outputMessage = null;
            if (!method.isOneWay()) {
                outputMessage = definition.createMessage();
                outputMessage.setQName(new QName(wmodel.getTargetNameSpace(), method.getName() + "Response"));
                processOutputMessage(operation, method, outputMessage);
            }

            generateFault(method, operation);

            portType.addOperation(operation);
            definition.addPortType(portType);
        }

    }

    private void processInputMessage(Operation operation, JavaMethod method, Message inputMessage) {
        WSDLParameter request = method.getRequest();
        String reqNS = request.getTargetNamespace();

        if (method.getSoapStyle() == SOAPBinding.Style.DOCUMENT && method.isWrapperStyle()) {
            addPartByElementName(inputMessage, request.getName(), new QName(reqNS, request.getName()));

        }

        // Doc-Lit-Bare
        if (method.getSoapStyle() == SOAPBinding.Style.DOCUMENT && !method.isWrapperStyle()) {
            for (JavaParameter jp : request.getChildren()) {
                addPartByElementName(inputMessage, jp.getPartName(), new QName(jp.getTargetNamespace(), jp
                    .getName()));
            }
        }

        // RPC

        if (method.getSoapStyle() == SOAPBinding.Style.RPC) {
            JAXBRIContext jxbcontext = wmodel.getJaxbContext();
            if (request != null && request.getChildren().size() > 0) {
                Iterator ite2 = request.getChildren().iterator();
                while (ite2.hasNext()) {
                    JavaParameter jp = (JavaParameter)ite2.next();
                    QName qname = jxbcontext.getTypeName(jp.getTypeReference());                  
                    addPartByTypeName(inputMessage, jp.getPartName(), qname);
                }

            }
        }
        
        addInputToMessage(operation, inputMessage, request.getName());
        inputMessage.setUndefined(false);
        definition.addMessage(inputMessage);

    }
    
    
    
    private void processOutputMessage(Operation operation, JavaMethod method, Message outputMessage) {
        WSDLParameter response = method.getResponse();
        String resNS = response.getTargetNamespace();

        if (method.getSoapStyle() == SOAPBinding.Style.DOCUMENT && method.isWrapperStyle()) {
            addPartByElementName(outputMessage, response.getName(), new QName(resNS, response.getName()));

        }

        // Doc-Lit-Bare
        if (method.getSoapStyle() == SOAPBinding.Style.DOCUMENT && !method.isWrapperStyle()) {
            for (JavaParameter jp : response.getChildren()) {
                addPartByElementName(outputMessage, jp.getPartName(), new QName(jp.getTargetNamespace(), jp
                    .getName()));
            }
        }

        // RPC

        if (method.getSoapStyle() == SOAPBinding.Style.RPC) {
            JAXBRIContext jxbcontext = wmodel.getJaxbContext();
            if (response != null && response.getChildren().size() > 0) {
                Iterator ite2 = response.getChildren().iterator();
                while (ite2.hasNext()) {
                    JavaParameter jp = (JavaParameter)ite2.next();
                    QName qname = jxbcontext.getTypeName(jp.getTypeReference());
                    addPartByTypeName(outputMessage, jp.getPartName(), qname);
                }

            }

        }
        
        addOutputToMessage(operation, outputMessage, response.getName());
        outputMessage.setUndefined(false);
        definition.addMessage(outputMessage);

    }
    
    
    

    private void generateFault(JavaMethod method, Operation operation) {
        for (org.apache.cxf.tools.common.model.WSDLException exception : method.getWSDLExceptions()) {
            String exceptionName = exception.getExcpetionClass().getSimpleName();
            Message msg = definition.createMessage();
            msg.setQName(new QName(wmodel.getTargetNameSpace(), exceptionName));
            Part part = definition.createPart();
            part.setName(exception.getDetailType().getSimpleName());
            part.setElementName(exception.getDetailTypeReference().tagName);
            msg.addPart(part);
            msg.setUndefined(false);
            definition.addMessage(msg);
            Fault fault = definition.createFault();
            fault.setMessage(msg);
            fault.setName(exceptionName);
            operation.addFault(fault);
        }
    }

    private void addPartByElementName(Message message, String partName, QName partElementName) {
        if (partName == null) {
            return;
        }
        Part part = definition.createPart();
        part.setName(partName);
        part.setElementName(partElementName);
        message.addPart(part);
    }

    private void addPartByTypeName(Message message, String partName, QName typeName) {
        Part part = definition.createPart();
        part.setName(partName);
        part.setTypeName(typeName);
        message.addPart(part);
    }

    private void addInputToMessage(Operation operation, Message msg, String inputName) {
        Input input = definition.createInput();
        input.setMessage(msg);
        input.setName(inputName);
        operation.setInput(input);
    }

    private void addOutputToMessage(Operation operation, Message msg, String outputName) {
        Output output = definition.createOutput();
        output.setMessage(msg);
        output.setName(outputName);
        operation.setOutput(output);
    }
}
