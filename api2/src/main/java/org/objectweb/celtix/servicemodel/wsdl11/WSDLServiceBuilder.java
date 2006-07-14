package org.objectweb.celtix.servicemodel.wsdl11;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.wsdl.Binding;
import javax.wsdl.BindingFault;
import javax.wsdl.BindingOperation;
import javax.wsdl.Definition;
import javax.wsdl.Fault;
import javax.wsdl.Input;
import javax.wsdl.Message;
import javax.wsdl.Operation;
import javax.wsdl.Output;
import javax.wsdl.Part;
import javax.wsdl.Port;
import javax.wsdl.PortType;
import javax.wsdl.Service;
import javax.wsdl.extensions.ExtensibilityElement;

import org.objectweb.celtix.Bus;
import org.objectweb.celtix.servicemodel.AbstractMessageContainer;
import org.objectweb.celtix.servicemodel.AbstractPropertiesHolder;
import org.objectweb.celtix.servicemodel.BindingInfo;
import org.objectweb.celtix.servicemodel.BindingOperationInfo;
import org.objectweb.celtix.servicemodel.FaultInfo;
import org.objectweb.celtix.servicemodel.InterfaceInfo;
import org.objectweb.celtix.servicemodel.MessageInfo;
import org.objectweb.celtix.servicemodel.MessagePartInfo;
import org.objectweb.celtix.servicemodel.OperationInfo;
import org.objectweb.celtix.servicemodel.ServiceInfo;

public final class WSDLServiceBuilder {
    
    public static final String WSDL_DEFINITION = WSDLServiceBuilder.class.getName() + ".DEFINITION";
    public static final String WSDL_SERVICE = WSDLServiceBuilder.class.getName() + ".SERVICE";
    public static final String WSDL_PORTTYPE = WSDLServiceBuilder.class.getName() + ".WSDL_PORTTYPE";
    public static final String WSDL_PORT = WSDLServiceBuilder.class.getName() + ".PORT";
    public static final String WSDL_BINDING = WSDLServiceBuilder.class.getName() + ".BINDING";
    
    public static final String WSDL_OPERATION = WSDLServiceBuilder.class.getName() + ".OPERATION";
    public static final String WSDL_BINDING_OPERATION = WSDLServiceBuilder.class.getName()
                                                        + ".BINDING_OPERATION";
    
    
    
    private WSDLServiceBuilder() {
        //utility class - never contructed
    }

    // utility for dealing with the JWSDL collections that are 1.4 based.   We can 
    // kind of use a normal for loop with this
    @SuppressWarnings("unchecked")
    private static <T> Collection<T> cast(Collection<?> p, Class<T> cls) {
        return (Collection<T>)p;
    }
    @SuppressWarnings("unchecked")
    private static <T, U> Map.Entry<T, U> cast(Map.Entry<?, ?> p, Class<T> pc, Class<U> uc) {
        return (Map.Entry<T, U>)p;
    }
    
    private static void copyExtensors(AbstractPropertiesHolder info, List<?> extList) {
        for (ExtensibilityElement ext : cast(extList, ExtensibilityElement.class)) {
            info.addExtensor(ext);
        }
    }

    public static ServiceInfo buildService(Bus bus, Definition def, Service serv) {
        ServiceInfo service = new ServiceInfo(bus);
        service.setProperty(WSDL_DEFINITION, def);
        service.setProperty(WSDL_SERVICE, serv);
        service.setTargetNamespace(def.getTargetNamespace());
        
        copyExtensors(service, def.getExtensibilityElements());
        
        PortType portType = null;
        for (Port port : cast(serv.getPorts().values(), Port.class)) {
            if (portType == null) {
                portType = port.getBinding().getPortType();
            } else if (port.getBinding().getPortType() != portType) {
                throw new IllegalStateException("All endpoints must share the same portType");
            }
        }
        
        buildInterface(service, portType);
        for (Port port : cast(serv.getPorts().values(), Port.class)) {
            Binding binding = port.getBinding();
            
            BindingInfo bi = service.getBinding(binding.getQName());
            if (bi == null) {
                bi = buildBinding(service, binding);
            }
            
            //build Endpoint for the binding....
            //REVISIT
        }
        
        
        return service;
    }
    
    public static BindingInfo buildBinding(ServiceInfo service, Binding binding) {
        String ns = ((ExtensibilityElement)binding.getExtensibilityElements().get(0))
            .getElementType().getNamespaceURI();
        
        BindingInfo bi = service.createBinding(binding.getQName(), ns);
        copyExtensors(bi, binding.getExtensibilityElements());
        
        for (BindingOperation bop : cast(binding.getBindingOperations(), BindingOperation.class)) {
            String inName = null;
            String outName = null;
            if (bop.getBindingInput() != null) {
                inName = bop.getBindingInput().getName();
            }
            if (bop.getBindingOutput() != null) {
                inName = bop.getBindingOutput().getName();
            }
            BindingOperationInfo bop2 = bi.buildOperation(bop.getName(), inName, outName);
            copyExtensors(bop2, bop.getExtensibilityElements());
            bi.addOperation(bop2);
            
            if (bop.getBindingInput() != null) {
                copyExtensors(bop2.getInput(), bop.getBindingInput().getExtensibilityElements());
            }
            if (bop.getBindingOutput() != null) {
                copyExtensors(bop2.getOutput(), bop.getBindingOutput().getExtensibilityElements());
            }
            for (BindingFault f : cast(bop.getBindingFaults().values(), BindingFault.class)) {
                copyExtensors(bop2.getFault(f.getName()),
                              bop.getBindingFault(f.getName()).getExtensibilityElements());
            }
        } 
        return bi;
    }

    public static void buildInterface(ServiceInfo si, PortType p) {
        InterfaceInfo inf = si.createInterface(p.getQName());
        inf.setProperty(WSDL_PORTTYPE, p);
        for (Operation op : cast(p.getOperations(), Operation.class)) {
            buildInterfaceOperation(inf, op);
        }
        
    }

    public static void buildInterfaceOperation(InterfaceInfo inf, Operation op) {
        OperationInfo opInfo = inf.addOperation(op.getName());
        opInfo.setProperty(WSDL_OPERATION, op);
        Input input = op.getInput();
        if (input != null) {
            MessageInfo minfo = opInfo.createMessage(input.getMessage().getQName());
            opInfo.setInput(input.getName(), minfo);
            buildMessage(minfo, input.getMessage());
        }
        Output output = op.getOutput();
        if (output != null) {
            MessageInfo minfo = opInfo.createMessage(output.getMessage().getQName());
            opInfo.setOutput(input.getName(), minfo);
            buildMessage(minfo, input.getMessage());
        }
        Map<?, ?> m = op.getFaults();
        for (Map.Entry<?, ?> rawentry : m.entrySet()) {
            Map.Entry<String, Fault> entry = cast(rawentry, String.class, Fault.class);
            FaultInfo finfo = opInfo.addFault(entry.getKey(), entry.getValue().getMessage().getQName());
            buildMessage(finfo, entry.getValue().getMessage());
        }
    }

    public static void buildMessage(AbstractMessageContainer minfo, Message msg) {
        for (Part part : cast(msg.getOrderedParts(null), Part.class)) {
            MessagePartInfo pi = minfo.addMessagePart(part.getName());
            if (part.getTypeName() != null) {
                pi.setTypeQName(part.getTypeName());
            } else {
                pi.setElementQName(part.getElementName());
            }
        }
    }
    
       
}
