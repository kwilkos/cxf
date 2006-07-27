package org.objectweb.celtix.wsdl11;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

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
import org.objectweb.celtix.BusException;
import org.objectweb.celtix.bindings.BindingFactory;
import org.objectweb.celtix.service.model.AbstractMessageContainer;
import org.objectweb.celtix.service.model.AbstractPropertiesHolder;
import org.objectweb.celtix.service.model.BindingInfo;
import org.objectweb.celtix.service.model.BindingOperationInfo;
import org.objectweb.celtix.service.model.FaultInfo;
import org.objectweb.celtix.service.model.InterfaceInfo;
import org.objectweb.celtix.service.model.MessageInfo;
import org.objectweb.celtix.service.model.MessagePartInfo;
import org.objectweb.celtix.service.model.OperationInfo;
import org.objectweb.celtix.service.model.ServiceInfo;

public class WSDLServiceBuilder {
    

       
    public static final String WSDL_DEFINITION = WSDLServiceBuilder.class.getName() + ".DEFINITION";
    public static final String WSDL_SERVICE = WSDLServiceBuilder.class.getName() + ".SERVICE";
    public static final String WSDL_PORTTYPE = WSDLServiceBuilder.class.getName() + ".WSDL_PORTTYPE";
    public static final String WSDL_PORT = WSDLServiceBuilder.class.getName() + ".PORT";
    public static final String WSDL_BINDING = WSDLServiceBuilder.class.getName() + ".BINDING";
    
    public static final String WSDL_OPERATION = WSDLServiceBuilder.class.getName() + ".OPERATION";
    public static final String WSDL_BINDING_OPERATION = WSDLServiceBuilder.class.getName()
                                                        + ".BINDING_OPERATION";
    
    private static final Logger LOG = Logger.getLogger(WSDLServiceBuilder.class.getName());
    private Bus bus;
    
    public WSDLServiceBuilder(Bus bus) {
        this.bus = bus;
    }

    // utility for dealing with the JWSDL collections that are 1.4 based.   We can 
    // kind of use a normal for loop with this
    @SuppressWarnings("unchecked")
    public static <T> Collection<T> cast(Collection<?> p, Class<T> cls) {
        return (Collection<T>)p;
    }
    @SuppressWarnings("unchecked")
    public static <T, U> Map.Entry<T, U> cast(Map.Entry<?, ?> p, Class<T> pc, Class<U> uc) {
        return (Map.Entry<T, U>)p;
    }
    
    private void copyExtensors(AbstractPropertiesHolder info, List<?> extList) {
        if (info != null) {
            for (ExtensibilityElement ext : cast(extList, ExtensibilityElement.class)) {
                info.addExtensor(ext);
            }
        }
    }

    public ServiceInfo buildService(Definition def, Service serv) {
        ServiceInfo service = new ServiceInfo();
        service.setProperty(WSDL_DEFINITION, def);
        service.setProperty(WSDL_SERVICE, serv);
        service.setTargetNamespace(def.getTargetNamespace());
        service.setName(serv.getQName());
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

    public BindingInfo buildBinding(ServiceInfo service, Binding binding) {
        BindingInfo bi = null;
        try {
            String ns = ((ExtensibilityElement)binding.getExtensibilityElements().get(0))
                .getElementType().getNamespaceURI();
        
            BindingFactory factory = bus.getBindingManager().getBindingFactory(ns);
            if (factory instanceof WSDLBindingFactory) {
                WSDLBindingFactory wFactory = (WSDLBindingFactory) factory;
                bi = wFactory.createBindingInfo(service, binding);
            }
        } catch (BusException e) {
            //ignore, we'll use a generic BindingInfo
        }
        
        if (bi == null) {
            bi = new BindingInfo(service);
            bi.setName(binding.getQName());
            copyExtensors(bi, binding.getExtensibilityElements());
            
            for (BindingOperation bop : cast(binding.getBindingOperations(), BindingOperation.class)) {
                LOG.fine("binding operation name is " + bop.getName());
                String inName = null;
                String outName = null;
                if (bop.getBindingInput() != null) {
                    inName = bop.getBindingInput().getName();
                }
                if (bop.getBindingOutput() != null) {
                    outName = bop.getBindingOutput().getName();
                }
                BindingOperationInfo bop2 = bi.buildOperation(bop.getName(), inName, outName);
                if (bop2 != null) {
                    
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
                              
            } 
        }
        
        service.addBinding(bi);
        return bi;
    }

    public void buildInterface(ServiceInfo si, PortType p) {
        InterfaceInfo inf = si.createInterface(p.getQName());
        inf.setProperty(WSDL_PORTTYPE, p);
        for (Operation op : cast(p.getOperations(), Operation.class)) {
            buildInterfaceOperation(inf, op);
        }
        
    }

    public void buildInterfaceOperation(InterfaceInfo inf, Operation op) {
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
            opInfo.setOutput(output.getName(), minfo);
            buildMessage(minfo, output.getMessage());
        }
        Map<?, ?> m = op.getFaults();
        for (Map.Entry<?, ?> rawentry : m.entrySet()) {
            Map.Entry<String, Fault> entry = cast(rawentry, String.class, Fault.class);
            FaultInfo finfo = opInfo.addFault(entry.getKey(), entry.getValue().getMessage().getQName());
            buildMessage(finfo, entry.getValue().getMessage());
        }
    }

    public void buildMessage(AbstractMessageContainer minfo, Message msg) {
        for (Part part : cast(msg.getOrderedParts(null), Part.class)) {
            MessagePartInfo pi = minfo.addMessagePart(part.getName());
            if (part.getTypeName() != null) {
                pi.setTypeQName(part.getTypeName());
                pi.setIsElement(false);
            } else {
                pi.setElementQName(part.getElementName());
                pi.setIsElement(true);
            }
        }
    }
    
       
}
