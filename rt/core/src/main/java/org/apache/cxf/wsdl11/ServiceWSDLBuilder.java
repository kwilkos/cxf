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

package org.apache.cxf.wsdl11;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.wsdl.Binding;
import javax.wsdl.BindingFault;
import javax.wsdl.BindingInput;
import javax.wsdl.BindingOperation;
import javax.wsdl.BindingOutput;
import javax.wsdl.Definition;
import javax.wsdl.Fault;
import javax.wsdl.Input;
import javax.wsdl.Message;
import javax.wsdl.Operation;
import javax.wsdl.OperationType;
import javax.wsdl.Output;
import javax.wsdl.Part;
import javax.wsdl.Port;
import javax.wsdl.PortType;
import javax.wsdl.Service;
import javax.wsdl.Types;
import javax.wsdl.WSDLException;
import javax.wsdl.extensions.ElementExtensible;
import javax.wsdl.extensions.ExtensibilityElement;
import javax.wsdl.factory.WSDLFactory;
import javax.xml.namespace.QName;

import com.ibm.wsdl.extensions.schema.SchemaImpl;

import org.apache.cxf.helpers.CastUtils;
import org.apache.cxf.service.model.AbstractMessageContainer;
import org.apache.cxf.service.model.AbstractPropertiesHolder;
import org.apache.cxf.service.model.BindingFaultInfo;
import org.apache.cxf.service.model.BindingInfo;
import org.apache.cxf.service.model.BindingMessageInfo;
import org.apache.cxf.service.model.BindingOperationInfo;
import org.apache.cxf.service.model.EndpointInfo;
import org.apache.cxf.service.model.FaultInfo;
import org.apache.cxf.service.model.InterfaceInfo;
import org.apache.cxf.service.model.MessagePartInfo;
import org.apache.cxf.service.model.OperationInfo;
import org.apache.cxf.service.model.SchemaInfo;
import org.apache.cxf.service.model.ServiceInfo;

public final class ServiceWSDLBuilder {
    
    private static final QName SCHEMA_QNAME = new QName("http://www.w3.org/2001/XMLSchema", "schema");
    
    private Map<String, String> prefix2ns;
    private Map<String, String> ns2prefix;
    private Definition definition;
    private List<ServiceInfo> services;
    
    public ServiceWSDLBuilder(List<ServiceInfo> services) {
        this.services = services;
        prefix2ns = new HashMap<String, String>();
        ns2prefix = new HashMap<String, String>();
    }
    public ServiceWSDLBuilder(ServiceInfo ... services) {
        this.services = Arrays.asList(services);
        prefix2ns = new HashMap<String, String>();
        ns2prefix = new HashMap<String, String>();
    }

    public Definition build() throws WSDLException {
        try {
            definition = services.get(0).getProperty(WSDLServiceBuilder.WSDL_DEFINITION, Definition.class);
        } catch (ClassCastException e) {
            //ignore
        }
        if (definition == null) {
            definition = WSDLFactory.newInstance().newDefinition();
            definition.getExtensionRegistry().registerSerializer(Types.class, 
                                                                 SCHEMA_QNAME,
                                                                 new SchemaSerializer());
                    
            addNamespace("wsdlsoap", "http://schemas.xmlsoap.org/wsdl/soap/");
            addNamespace("soap", "http://schemas.xmlsoap.org/soap/");
            addNamespace("xsd", "http://www.w3.org/2001/XMLSchema");
            
            ServiceInfo si = services.get(0);
            definition.setQName(si.getName());
            definition.setTargetNamespace(si.getTargetNamespace());
            addExtensibiltyElements(definition, getWSDL11Extensors(si));
            if (si.getSchemas() != null && si.getSchemas().size() > 0) {
                buildTypes(si.getSchemas());
            }
            for (ServiceInfo service : services) {
                buildPortType(service.getInterface());
                buildBinding(service.getBindings());
                buildService(service);
            }
        }
        return definition;
    }


    public List<ExtensibilityElement> getWSDL11Extensors(AbstractPropertiesHolder holder) {
        return holder.getExtensors(ExtensibilityElement.class);
    }
    
    protected void addExtensibiltyElements(ElementExtensible elementExtensible, 
        List<ExtensibilityElement> extensibilityElements) {
        if (extensibilityElements != null) {
            for (ExtensibilityElement element : extensibilityElements) {
                elementExtensible.addExtensibilityElement(element);
            }
        }
    }

    protected void buildTypes(Collection<SchemaInfo> schemas) {
        Types types = definition.createTypes();
        for (SchemaInfo schemaInfo : schemas) {
            SchemaImpl schemaImpl = new SchemaImpl();
            schemaImpl.setRequired(true);
            schemaImpl.setElementType(SCHEMA_QNAME);
            schemaImpl.setElement(schemaInfo.getElement());
            types.addExtensibilityElement(schemaImpl);
        }
        
        definition.setTypes(types);
    }

    protected void buildBinding(Collection<BindingInfo> bindingInfos) {
        Binding binding = null;
        for (BindingInfo bindingInfo : bindingInfos) {
            binding = definition.createBinding();
            binding.setUndefined(false);
            for (PortType portType 
                    : CastUtils.cast(definition.getPortTypes().values(), PortType.class)) {
                if (portType.getQName().equals(bindingInfo.getInterface().getName())) {
                    binding.setPortType(portType);
                    break;
                }
            }
            binding.setQName(bindingInfo.getName());
            buildBindingOperation(definition, binding, bindingInfo.getOperations());
            addExtensibiltyElements(binding, getWSDL11Extensors(bindingInfo));
            definition.addBinding(binding);
        }
    }

    protected void buildBindingOperation(Definition def, Binding binding, 
                                       Collection<BindingOperationInfo> bindingOperationInfos) {
        BindingOperation bindingOperation = null;
        for (BindingOperationInfo bindingOperationInfo : bindingOperationInfos) {
            bindingOperation = def.createBindingOperation();
            bindingOperation.setName(bindingOperationInfo.getName().getLocalPart());
            for (Operation operation 
                    : CastUtils.cast(binding.getPortType().getOperations(), Operation.class)) {
                if (operation.getName().equals(bindingOperation.getName())) {
                    bindingOperation.setOperation(operation);
                    break;
                }
            }
            buildBindingInput(def, bindingOperation, bindingOperationInfo.getInput());
            buildBindingOutput(def, bindingOperation, bindingOperationInfo.getOutput());
            buildBindingFault(def, bindingOperation, bindingOperationInfo.getFaults());
            addExtensibiltyElements(bindingOperation, getWSDL11Extensors(bindingOperationInfo));
            binding.addBindingOperation(bindingOperation);
        }
    }

    protected void buildBindingFault(Definition def, BindingOperation bindingOperation, 
                                   Collection<BindingFaultInfo> bindingFaultInfos) {
        BindingFault bindingFault = null;
        for (BindingFaultInfo bindingFaultInfo 
            : bindingFaultInfos) {
            bindingFault = def.createBindingFault();
            bindingFault.setName(bindingFaultInfo.getFaultInfo().getFaultName().getLocalPart());
            bindingOperation.addBindingFault(bindingFault);
            addExtensibiltyElements(bindingFault, getWSDL11Extensors(bindingFaultInfo));
        }
        
    }

    protected void buildBindingInput(Definition def, BindingOperation bindingOperation, 
                                         BindingMessageInfo bindingMessageInfo) {
        BindingInput bindingInput = null;
        if (bindingMessageInfo != null) {
            bindingInput = def.createBindingInput();
            bindingInput.setName(bindingMessageInfo.getMessageInfo().getName().getLocalPart());
            bindingOperation.setBindingInput(bindingInput);
            addExtensibiltyElements(bindingInput, getWSDL11Extensors(bindingMessageInfo));
        }
    }
    
    protected void buildBindingOutput(Definition def, BindingOperation bindingOperation, 
                                   BindingMessageInfo bindingMessageInfo) {
        BindingOutput bindingOutput = null;
        if (bindingMessageInfo != null) {
            bindingOutput = def.createBindingOutput();
            bindingOutput.setName(bindingMessageInfo.getMessageInfo().getName().getLocalPart());
            bindingOperation.setBindingOutput(bindingOutput);
            addExtensibiltyElements(bindingOutput, getWSDL11Extensors(bindingMessageInfo));
        }
    }

    protected void buildService(ServiceInfo serviceInfo) {
        Service serv = definition.createService();
        serv.setQName(serviceInfo.getName());
        addNamespace(serviceInfo.getName().getNamespaceURI());
        definition.addService(serv);

        for (EndpointInfo ei : serviceInfo.getEndpoints()) {
            addNamespace(ei.getTransportId());
            addNamespace(ei.getBinding().getBindingId());
            Port port = definition.createPort();
            port.setName(ei.getName().getLocalPart());
            port.setBinding(definition.getBinding(ei.getBinding().getName()));
            addExtensibiltyElements(port, getWSDL11Extensors(ei));
            serv.addPort(port);
        }
    }

    protected void buildPortType(InterfaceInfo intf) {
        PortType portType = null;
        try {
            portType = intf.getProperty(WSDLServiceBuilder.WSDL_PORTTYPE, PortType.class);
        } catch (ClassCastException e) {
            // do nothing
        }
        
        if (portType == null) {
            portType = definition.createPortType();
            portType.setQName(intf.getName());
            addNamespace(intf.getName().getNamespaceURI());
            portType.setUndefined(false);
            buildPortTypeOperation(portType, intf.getOperations());
        }

        definition.addPortType(portType);
    }

    protected void addNamespace(String namespaceURI) {
        addNamespace(getPrefix(namespaceURI), namespaceURI);
    }

    protected void addNamespace(String prefix, String namespaceURI) {
        prefix2ns.put(prefix, namespaceURI);
        ns2prefix.put(namespaceURI, prefix);
        definition.addNamespace(prefix, namespaceURI);
    }

    protected void buildPortTypeOperation(PortType portType, Collection<OperationInfo> operationInfos) {
        for (OperationInfo operationInfo : operationInfos) {
            Operation operation = null;
            try {
                operation = operationInfo.getProperty(
                    WSDLServiceBuilder.WSDL_OPERATION, Operation.class);
            } catch (ClassCastException e) {
                // do nothing
            }
            
            if (operation == null) {
                operation = definition.createOperation();
                operation.setUndefined(false);
                operation.setName(operationInfo.getName().getLocalPart());
                addNamespace(operationInfo.getName().getNamespaceURI());
                if (operationInfo.isOneWay()) {
                    operation.setStyle(OperationType.ONE_WAY);
                }
                Input input = definition.createInput();
                input.setName(operationInfo.getInputName());
                Message message = definition.createMessage();
                buildMessage(message, operationInfo.getInput());
                input.setMessage(message);
                operation.setInput(input);
                
                Output output = definition.createOutput();
                output.setName(operationInfo.getOutputName());
                message = definition.createMessage();
                buildMessage(message, operationInfo.getOutput());
                output.setMessage(message);
                operation.setOutput(output);
                //loop to add fault
                Collection<FaultInfo> faults = operationInfo.getFaults();
                Fault fault = null;
                for (FaultInfo faultInfo : faults) {
                    fault = definition.createFault();
                    fault.setName(faultInfo.getFaultName().getLocalPart());
                    message = definition.createMessage();
                    buildMessage(message, faultInfo);
                    fault.setMessage(message);
                    operation.addFault(fault);
                }
            }
            portType.addOperation(operation);
        }
    }

    protected String getPrefix(String ns) {
        String prefix = ns2prefix.get(ns);
        if (prefix == null) {
            prefix = getNewPrefix();
            ns2prefix.put(ns, prefix);
        }
        return prefix;
    }
    
    protected String getNewPrefix() {
        String prefix = "ns1";
        int i = 0;
        while (prefix2ns.get(prefix) != null) {
            i++;
            prefix = "ns" + i;
        }
        return prefix;
    }

    protected void buildMessage(Message message, AbstractMessageContainer messageContainer) {
        message.setQName(messageContainer.getName());
        message.setUndefined(false);
        definition.addMessage(message);
        
        List<MessagePartInfo> messageParts = messageContainer.getMessageParts();
        Part messagePart = null;
        for (MessagePartInfo messagePartInfo : messageParts) {
            messagePart = definition.createPart();
            messagePart.setName(messagePartInfo.getName().getLocalPart());
            if (messagePartInfo.isElement()) {
                messagePart.setElementName(messagePartInfo.getElementQName());
                addNamespace(messagePartInfo.getElementQName().getNamespaceURI());
            } else if (messagePartInfo.getTypeQName() != null) {
                messagePart.setTypeName(messagePartInfo.getTypeQName());
                addNamespace(messagePartInfo.getTypeQName().getNamespaceURI());
            }
            message.addPart(messagePart);
        }
    }
          
}
