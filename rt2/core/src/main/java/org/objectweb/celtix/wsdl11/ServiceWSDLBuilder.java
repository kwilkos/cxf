package org.objectweb.celtix.wsdl11;

import java.util.Collection;
import java.util.List;

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
import javax.wsdl.WSDLException;
import javax.wsdl.extensions.ElementExtensible;
import javax.wsdl.extensions.ExtensibilityElement;
import javax.wsdl.factory.WSDLFactory;

import org.objectweb.celtix.service.model.AbstractMessageContainer;
import org.objectweb.celtix.service.model.BindingFaultInfo;
import org.objectweb.celtix.service.model.BindingInfo;
import org.objectweb.celtix.service.model.BindingMessageInfo;
import org.objectweb.celtix.service.model.BindingOperationInfo;
import org.objectweb.celtix.service.model.EndpointInfo;
import org.objectweb.celtix.service.model.FaultInfo;
import org.objectweb.celtix.service.model.InterfaceInfo;
import org.objectweb.celtix.service.model.MessagePartInfo;
import org.objectweb.celtix.service.model.OperationInfo;
import org.objectweb.celtix.service.model.ServiceInfo;

public final class ServiceWSDLBuilder {
    
    private static ServiceWSDLBuilder serviceWSDLBuilder;       
    
    private ServiceWSDLBuilder() {
        
    }

    public static synchronized ServiceWSDLBuilder getServiceWSDLBuilder() {
        if (serviceWSDLBuilder == null) {
            serviceWSDLBuilder = new ServiceWSDLBuilder();
        }
        return serviceWSDLBuilder;
    }
    
    private void addExtensibiltyElements(ElementExtensible elementExtensible, 
        List<ExtensibilityElement> extensibilityElements) {
        if (extensibilityElements != null) {
            for (ExtensibilityElement element : extensibilityElements) {
                elementExtensible.addExtensibilityElement(element);
            }
        }
    }
    
    public Definition buildDefinition(ServiceInfo service) throws WSDLException {
        Definition def = null;
        try {
            def = service.getProperty(WSDLServiceBuilder.WSDL_DEFINITION, Definition.class);
        } catch (ClassCastException e) {
            //ignore
        }
        if (def == null) {
            def = WSDLFactory.newInstance().newDefinition();
            def.setQName(service.getName());
            def.setTargetNamespace(service.getTargetNamespace());
            addExtensibiltyElements(def, service.getWSDL11Extensors());
            buildPortType(def, service.getInterface());
            buildBinding(def, service.getBindings());
            buildService(def, service);
        }
        return def;
    }


    private void buildBinding(Definition def, Collection<BindingInfo> bindingInfos) {
        Binding binding = null;
        for (BindingInfo bindingInfo : bindingInfos) {
            binding = def.createBinding();
            for (PortType portType 
                    : WSDLServiceBuilder.cast(def.getPortTypes().values(), PortType.class)) {
                if (portType.getQName().equals(bindingInfo.getInterface().getName())) {
                    binding.setPortType(portType);
                    break;
                }
            }
            binding.setQName(bindingInfo.getName());
            buildBindingOperation(def, binding, bindingInfo.getOperations());
            addExtensibiltyElements(binding, bindingInfo.getWSDL11Extensors());
            def.addBinding(binding);
        }
    }

    private void buildBindingOperation(Definition def, Binding binding, 
                                       Collection<BindingOperationInfo> bindingOperationInfos) {
        BindingOperation bindingOperation = null;
        for (BindingOperationInfo bindingOperationInfo : bindingOperationInfos) {
            bindingOperation = def.createBindingOperation();
            bindingOperation.setName(bindingOperationInfo.getName().getLocalPart());
            for (Operation operation 
                    : WSDLServiceBuilder.cast(binding.getPortType().getOperations(), Operation.class)) {
                if (operation.getName().equals(bindingOperation.getName())) {
                    bindingOperation.setOperation(operation);
                    break;
                }
            }
            buildBindingInput(def, bindingOperation, bindingOperationInfo.getInput());
            buildBindingOutput(def, bindingOperation, bindingOperationInfo.getOutput());
            buildBindingFault(def, bindingOperation, bindingOperationInfo.getFaults());
            addExtensibiltyElements(bindingOperation, bindingOperationInfo.getWSDL11Extensors());
            binding.addBindingOperation(bindingOperation);
        }
    }

    private void buildBindingFault(Definition def, BindingOperation bindingOperation, 
                                   Collection<BindingFaultInfo> bindingFaultInfos) {
        BindingFault bindingFault = null;
        for (BindingFaultInfo bindingFaultInfo 
            : bindingFaultInfos) {
            bindingFault = def.createBindingFault();
            bindingFault.setName(bindingFaultInfo.getFaultInfo().getFaultName().getLocalPart());
            bindingOperation.addBindingFault(bindingFault);
            addExtensibiltyElements(bindingFault, bindingFaultInfo.getWSDL11Extensors());
        }
        
    }

    private void buildBindingInput(Definition def, BindingOperation bindingOperation, 
                                         BindingMessageInfo bindingMessageInfo) {
        BindingInput bindingInput = null;
        if (bindingMessageInfo != null) {
            bindingInput = def.createBindingInput();
            bindingInput.setName(bindingMessageInfo.getMessageInfo().getName().getLocalPart());
            bindingOperation.setBindingInput(bindingInput);
            addExtensibiltyElements(bindingInput, bindingMessageInfo.getWSDL11Extensors());
        }
    }
    
    private void buildBindingOutput(Definition def, BindingOperation bindingOperation, 
                                   BindingMessageInfo bindingMessageInfo) {
        BindingOutput bindingOutput = null;
        if (bindingMessageInfo != null) {
            bindingOutput = def.createBindingOutput();
            bindingOutput.setName(bindingMessageInfo.getMessageInfo().getName().getLocalPart());
            bindingOperation.setBindingOutput(bindingOutput);
            addExtensibiltyElements(bindingOutput, bindingMessageInfo.getWSDL11Extensors());
        }
    }

    private void buildService(Definition def, ServiceInfo service) {
        Service serv = def.createService();
        serv.setQName(service.getName());
        def.addService(serv);

        for (EndpointInfo ei : service.getEndpoints()) {
            Port port = def.createPort();
            port.setName(ei.getName());
            port.setBinding(def.getBinding(ei.getBinding().getName()));
            addExtensibiltyElements(port, ei.getWSDL11Extensors());
            serv.addPort(port);
        }
    }

    private void buildPortType(Definition def, InterfaceInfo intf) {
        PortType portType = null;
        try {
            portType = intf.getProperty(WSDLServiceBuilder.WSDL_PORTTYPE, PortType.class);
        } catch (ClassCastException e) {
            portType = def.createPortType();
            portType.setQName(intf.getName());
            buildPortTypeOperation(def, portType, intf.getOperations());
        }
        def.addPortType(portType);
    }


    private void buildPortTypeOperation(Definition def, 
                                        PortType portType, Collection<OperationInfo> operationInfos) {
        for (OperationInfo operationInfo : operationInfos) {
            Operation operation = null;
            try {
                operation = operationInfo.getProperty(
                    WSDLServiceBuilder.WSDL_OPERATION, Operation.class);
            } catch (ClassCastException e) {
                operation = def.createOperation();
                operation.setName(operationInfo.getName().getLocalPart());
                if (operationInfo.isOneWay()) {
                    operation.setStyle(OperationType.ONE_WAY);
                }
                Input input = def.createInput();
                input.setName(operationInfo.getInputName());
                Message message = def.createMessage();
                buildMessage(def, message, operationInfo.getInput());
                input.setMessage(message);
                operation.setInput(input);
                
                Output output = def.createOutput();
                output.setName(operationInfo.getOutputName());
                message = def.createMessage();
                buildMessage(def, message, operationInfo.getOutput());
                output.setMessage(message);
                operation.setOutput(output);
                //loop to add fault
                Collection<FaultInfo> faults = operationInfo.getFaults();
                Fault fault = null;
                for (FaultInfo faultInfo : faults) {
                    fault = def.createFault();
                    fault.setName(faultInfo.getFaultName().getLocalPart());
                    message = def.createMessage();
                    buildMessage(def, message, faultInfo);
                    fault.setMessage(message);
                    operation.addFault(fault);
                }
            }
            portType.addOperation(operation);
        }
    }


    private void buildMessage(Definition def, Message message,  AbstractMessageContainer messageContainer) {
        message.setQName(messageContainer.getName());
        List<MessagePartInfo> messageParts = messageContainer.getMessageParts();
        Part messagePart = null;
        for (MessagePartInfo messagePartInfo : messageParts) {
            messagePart = def.createPart();
            messagePart.setName(messagePartInfo.getName().getLocalPart());
            if (messagePartInfo.isElement()) {
                messagePart.setElementName(messagePartInfo.getElementQName());
            } else {
                messagePart.setTypeName(messagePartInfo.getTypeQName());
            }
            message.addPart(messagePart);
        }
    }
          
}
