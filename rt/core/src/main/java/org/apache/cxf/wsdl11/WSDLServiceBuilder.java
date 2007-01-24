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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.wsdl.Binding;
import javax.wsdl.BindingFault;
import javax.wsdl.BindingOperation;
import javax.wsdl.Definition;
import javax.wsdl.Fault;
import javax.wsdl.Import;
import javax.wsdl.Input;
import javax.wsdl.Message;
import javax.wsdl.Operation;
import javax.wsdl.Output;
import javax.wsdl.Part;
import javax.wsdl.Port;
import javax.wsdl.PortType;
import javax.wsdl.Service;
import javax.wsdl.Types;
import javax.wsdl.extensions.ExtensibilityElement;
import javax.wsdl.extensions.UnknownExtensibilityElement;
import javax.wsdl.extensions.schema.Schema;
import javax.xml.namespace.QName;

import com.ibm.wsdl.extensions.soap.SOAPBindingImpl;

import org.apache.cxf.Bus;
import org.apache.cxf.BusException;
import org.apache.cxf.binding.BindingFactory;
import org.apache.cxf.resource.XmlSchemaURIResolver;
import org.apache.cxf.service.model.AbstractMessageContainer;
import org.apache.cxf.service.model.AbstractPropertiesHolder;
import org.apache.cxf.service.model.BindingInfo;
import org.apache.cxf.service.model.BindingMessageInfo;
import org.apache.cxf.service.model.BindingOperationInfo;
import org.apache.cxf.service.model.DescriptionInfo;
import org.apache.cxf.service.model.EndpointInfo;
import org.apache.cxf.service.model.FaultInfo;
import org.apache.cxf.service.model.InterfaceInfo;
import org.apache.cxf.service.model.MessageInfo;
import org.apache.cxf.service.model.MessagePartInfo;
import org.apache.cxf.service.model.OperationInfo;
import org.apache.cxf.service.model.SchemaInfo;
import org.apache.cxf.service.model.ServiceInfo;
import org.apache.cxf.service.model.UnwrappedOperationInfo;
import org.apache.cxf.transport.DestinationFactory;
import org.apache.cxf.transport.DestinationFactoryManager;
import org.apache.ws.commons.schema.XmlSchema;
import org.apache.ws.commons.schema.XmlSchemaCollection;
import org.apache.ws.commons.schema.XmlSchemaComplexType;
import org.apache.ws.commons.schema.XmlSchemaElement;
import org.apache.ws.commons.schema.XmlSchemaObject;
import org.apache.ws.commons.schema.XmlSchemaObjectCollection;
import org.apache.ws.commons.schema.XmlSchemaSequence;

import static org.apache.cxf.helpers.CastUtils.cast;

public class WSDLServiceBuilder {

    public static final String WSDL_SCHEMA_LIST = WSDLServiceBuilder.class.getName() + ".SCHEMA";
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

    private void copyExtensors(AbstractPropertiesHolder info, List<?> extList) {
        if (info != null) {
            for (ExtensibilityElement ext : cast(extList, ExtensibilityElement.class)) {
                info.addExtensor(ext);
            }
        }
    }
    
    @SuppressWarnings("unchecked")
    public List<ServiceInfo> buildService(Definition d) {
        DescriptionInfo description = new DescriptionInfo();
        description.setProperty(WSDL_DEFINITION, d);
        description.setName(d.getQName());
        copyExtensors(description, d.getExtensibilityElements());
       
        List<ServiceInfo> serviceList = new ArrayList<ServiceInfo>();
        for (java.util.Iterator<QName> ite = d.getServices().keySet().iterator(); ite.hasNext();) {
            QName qn = ite.next();            
            serviceList.add(buildService(d, qn, description));
        }
        return serviceList;
    }

    public ServiceInfo buildService(Definition d, QName name) {
        return buildService(d, name, null);
    }

    private ServiceInfo buildService(Definition d, QName name, DescriptionInfo description) {
        Service service = d.getService(name);
        return buildService(d, service, description);
    }

    public ServiceInfo buildService(Definition def, Service serv) {
        return buildService(def, serv, null);
    }

    private ServiceInfo buildService(Definition def, Service serv, DescriptionInfo d) {
        DescriptionInfo description = d;
        if (null == description) {
            description = new DescriptionInfo();
            description.setProperty(WSDL_DEFINITION, def);
            description.setName(def.getQName());
            copyExtensors(description, def.getExtensibilityElements());
        }
        ServiceInfo service = new ServiceInfo();
        service.setDescription(description);
        description.getDescribed().add(service);
        service.setProperty(WSDL_DEFINITION, def);
        service.setProperty(WSDL_SERVICE, serv);

        XmlSchemaCollection schemas = getSchemas(def, service);
        service.setProperty(WSDL_SCHEMA_LIST, schemas);
        service.setTargetNamespace(def.getTargetNamespace());
        service.setName(serv.getQName());
        copyExtensors(service, serv.getExtensibilityElements());

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
            buildEndpoint(service, bi, port);
        }

        return service;
    }

    private XmlSchemaCollection getSchemas(Definition def, ServiceInfo serviceInfo) {
        XmlSchemaCollection schemaCol = new XmlSchemaCollection();
        serviceInfo.setXmlSchemaCollection(schemaCol);
        
        List<Definition> defList = new ArrayList<Definition>();
        parseImports(def, defList);
        extractSchema(def, schemaCol, serviceInfo);
        for (Definition def2 : defList) {
            extractSchema(def2, schemaCol, serviceInfo);
        }
        return schemaCol;
    }

    private void parseImports(Definition def, List<Definition> defList) {
        List<Import> importList = new ArrayList<Import>();

        Collection<List<Import>> ilist = cast(def.getImports().values());
        for (List<Import> list : ilist) {
            importList.addAll(list);
        }
        for (Import impt : importList) {
            parseImports(impt.getDefinition(), defList);
            defList.add(impt.getDefinition());
        }
    }

    private void extractSchema(Definition def, XmlSchemaCollection schemaCol, ServiceInfo serviceInfo) {
        Types typesElement = def.getTypes();
        if (typesElement != null) {
            for (Object obj : typesElement.getExtensibilityElements()) {
                org.w3c.dom.Element schemaElem = null;
                if (obj instanceof Schema) {
                    Schema schema = (Schema)obj;
                    schemaElem = schema.getElement();
                } else if (obj instanceof UnknownExtensibilityElement) {
                    org.w3c.dom.Element elem = ((UnknownExtensibilityElement)obj).getElement();
                    if (elem.getLocalName().equals("schema")) {
                        schemaElem = elem;
                    }
                }
                if (schemaElem != null) {
                    for (Object prefix : def.getNamespaces().keySet()) {
                        String ns = (String)def.getNamespaces().get(prefix);
                        if (!"".equals(prefix) && !schemaElem.hasAttribute("xmlns:" + prefix)) {
                            schemaElem.setAttribute("xmlns:" + prefix, ns);
                        }
                    }
                    schemaCol.setBaseUri(def.getDocumentBaseURI());
                    schemaCol.setSchemaResolver(new XmlSchemaURIResolver());
                    XmlSchema xmlSchema = schemaCol.read(schemaElem);
                    
                    SchemaInfo schemaInfo = new SchemaInfo(serviceInfo, xmlSchema.getTargetNamespace());
                    schemaInfo.setElement(schemaElem);
                    schemaInfo.setSchema(xmlSchema);
                    serviceInfo.addSchema(schemaInfo);
                }
            }
        }

    }

    public EndpointInfo buildEndpoint(ServiceInfo service, BindingInfo bi, Port port) {
        List elements = port.getExtensibilityElements();
        String ns = null;
        if (null != elements && elements.size() > 0) {
            ns = ((ExtensibilityElement)elements.get(0)).getElementType()
                        .getNamespaceURI();
        } else { // get the transport id from bindingInfo            
            ExtensibilityElement extElem = (ExtensibilityElement)port.getBinding().
                                            getExtensibilityElements().get(0);
            if (extElem instanceof SOAPBindingImpl) {
                ns = (String)((SOAPBindingImpl)extElem).getTransportURI();                  
            }            
        }
        EndpointInfo ei = null;

        try {
            DestinationFactory factory = bus.getExtension(DestinationFactoryManager.class)
                .getDestinationFactory(ns);
            if (factory instanceof WSDLEndpointFactory) {
                WSDLEndpointFactory wFactory = (WSDLEndpointFactory)factory;
                ei = wFactory.createEndpointInfo(service, bi, port);
            }
        } catch (BusException e) {
            // do nothing
        }

        if (ei == null) {
            ei = new EndpointInfo(service, ns);
        }

        ei.setName(new QName(service.getName().getNamespaceURI(), port.getName()));
        ei.setBinding(bi);
        copyExtensors(ei, port.getExtensibilityElements());

        service.addEndpoint(ei);
        DescriptionInfo d = service.getDescription();
        if (null != d) {
            ei.setDescription(d);
            d.getDescribed().add(ei);
        }
        return ei;
    }

    public BindingInfo buildBinding(ServiceInfo service, Binding binding) {
        BindingInfo bi = null;
        StringBuffer ns = new StringBuffer(100);
        BindingFactory factory = WSDLServiceUtils.getBindingFactory(binding, bus, ns);
        if (factory instanceof WSDLBindingFactory) {
            WSDLBindingFactory wFactory = (WSDLBindingFactory)factory;
            bi = wFactory.createBindingInfo(service, binding, ns.toString());
        }
        if (bi == null) {
            bi = new BindingInfo(service, ns.toString());
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
                BindingOperationInfo bop2 = bi.buildOperation(new QName(service.getName().getNamespaceURI(),
                                                                        bop.getName()), inName, outName);
                if (bop2 != null) {

                    copyExtensors(bop2, bop.getExtensibilityElements());
                    bi.addOperation(bop2);
                    if (bop.getBindingInput() != null) {
                        copyExtensors(bop2.getInput(), bop.getBindingInput().getExtensibilityElements());
                        handleHeader(bop2.getInput());
                    }
                    if (bop.getBindingOutput() != null) {
                        copyExtensors(bop2.getOutput(), bop.getBindingOutput().getExtensibilityElements());
                        handleHeader(bop2.getOutput());
                    }
                    for (BindingFault f : cast(bop.getBindingFaults().values(), BindingFault.class)) {
                        copyExtensors(bop2.getFault(new QName(service.getTargetNamespace(),
                                                              f.getName())), 
                                      bop.getBindingFault(f.getName()).getExtensibilityElements());
                    }
                }

            }
        }

        service.addBinding(bi);
        DescriptionInfo d = service.getDescription();
        if (null != d) {
            bi.setDescription(d);
            d.getDescribed().add(bi);
        }
        return bi;
    }

    private void handleHeader(BindingMessageInfo bindingMessageInfo) {
        // mark all message part which should be in header
        List<ExtensibilityElement> extensiblilityElement = 
            bindingMessageInfo.getExtensors(ExtensibilityElement.class);
        // for non-soap binding, the extensiblilityElement could be null
        if (extensiblilityElement == null) {
            return;
        }
//        for (ExtensibilityElement element : extensiblilityElement) {
//            LOG.info("the extensibility is " + element.getClass().getName());
//            if (element instanceof SOAPHeader) {
//                LOG.info("the header is " + ((SOAPHeader)element).getPart());
//            }
//        }
    }

    public void buildInterface(ServiceInfo si, PortType p) {
        InterfaceInfo inf = si.createInterface(p.getQName());
        DescriptionInfo d = si.getDescription();
        if (null != d) {
            inf.setDescription(si.getDescription());
            d.getDescribed().add(inf);
        }
        this.copyExtensors(inf, p.getExtensibilityElements());
        inf.setProperty(WSDL_PORTTYPE, p);
        for (Operation op : cast(p.getOperations(), Operation.class)) {
            buildInterfaceOperation(inf, op);
        }

    }

    @SuppressWarnings("unchecked")
    private void buildInterfaceOperation(InterfaceInfo inf, Operation op) {
        OperationInfo opInfo = inf.addOperation(new QName(inf.getName().getNamespaceURI(), op.getName()));
        opInfo.setProperty(WSDL_OPERATION, op);
        opInfo.setParameterOrdering(op.getParameterOrdering());
        this.copyExtensors(opInfo, op.getExtensibilityElements());
        Input input = op.getInput();
        List paramOrder = op.getParameterOrdering();
        if (input != null) {
            MessageInfo minfo = opInfo.createMessage(input.getMessage().getQName());
            opInfo.setInput(input.getName(), minfo);
            buildMessage(minfo, input.getMessage(), paramOrder);
            copyExtensors(minfo, input.getExtensibilityElements());
        }
        Output output = op.getOutput();
        if (output != null) {
            MessageInfo minfo = opInfo.createMessage(output.getMessage().getQName());
            opInfo.setOutput(output.getName(), minfo);
            buildMessage(minfo, output.getMessage(), paramOrder);
            copyExtensors(minfo, output.getExtensibilityElements());
        }
        Map<?, ?> m = op.getFaults();
        for (Map.Entry<?, ?> rawentry : m.entrySet()) {
            Map.Entry<String, Fault> entry = cast(rawentry, String.class, Fault.class);
            FaultInfo finfo = opInfo.addFault(new QName(inf.getName().getNamespaceURI(), entry.getKey()),
                                              entry.getValue().getMessage().getQName());
            buildMessage(finfo, entry.getValue().getMessage(), paramOrder);
        }
        checkForWrapped(opInfo);
    }

    private void checkForWrapped(OperationInfo opInfo) {
        MessageInfo inputMessage = opInfo.getInput();
        MessageInfo outputMessage = opInfo.getOutput();
        
        boolean passedRule = true;
        // RULE No.1:
        // The operation's input and output message (if present) each contain
        // only a single part
        // input message must exist
        if (inputMessage == null || inputMessage.size() != 1
            || (outputMessage != null && outputMessage.size() > 1)) {
            passedRule = false;
        }
        
        if (!passedRule) {
            return;
        }

        XmlSchemaCollection schemas = (XmlSchemaCollection)opInfo.getInterface().getService()
            .getProperty(WSDL_SCHEMA_LIST);
        XmlSchemaElement inputEl = null;
        XmlSchemaElement outputEl = null;

        // RULE No.2:
        // The input message part refers to a global element decalration whose
        // localname
        // is equal to the operation name
        MessagePartInfo inputPart = inputMessage.getMessagePartByIndex(0);
        if (!inputPart.isElement()) {
            passedRule = false;
        } else {
            QName inputElementName = inputPart.getElementQName();
            inputEl = schemas.getElementByQName(inputElementName);
            if (inputEl == null 
                || !opInfo.getName().getLocalPart().equals(inputElementName.getLocalPart())) {
                passedRule = false;
            }
        }
        
        if (!passedRule) {
            return;
        }

        // RULE No.3:
        // The output message part refers to a global element declaration
        MessagePartInfo outputPart = null;
        if (outputMessage != null && outputMessage.size() == 1) {
            outputPart = outputMessage.getMessagePartByIndex(0);
            if (outputPart != null) {
                if (!outputPart.isElement()
                    || schemas.getElementByQName(outputPart.getElementQName()) == null) {
                    passedRule = false;
                } else {
                    outputEl = schemas.getElementByQName(outputPart.getElementQName());
                }
            }
        }
        
        if (!passedRule) {
            return;
        }

        // RULE No.4 and No5:
        // wrapper element should be pure complex type

        // Now lets see if we have any attributes...
        // This should probably look at the restricted and substitute types too.
        MessageInfo unwrappedInput = new MessageInfo(opInfo, inputMessage.getName());
        MessageInfo unwrappedOutput = null;

        XmlSchemaComplexType xsct = null;
        if (inputEl.getSchemaType() instanceof XmlSchemaComplexType) {
            xsct = (XmlSchemaComplexType)inputEl.getSchemaType();
            if (hasAttributes(xsct) || !isWrappableSequence(xsct,
                                                            inputEl.getQName().getNamespaceURI(),
                                                            unwrappedInput)) {
                passedRule = false;
            }
        } else {
            passedRule = false;
        }
        
        if (!passedRule) {
            return;
        }
        
        if (outputMessage != null) {
            unwrappedOutput = new MessageInfo(opInfo, outputMessage.getName());

            if (outputEl != null && outputEl.getSchemaType() instanceof XmlSchemaComplexType) {
                xsct = (XmlSchemaComplexType)outputEl.getSchemaType();
                if (hasAttributes(xsct) || !isWrappableSequence(xsct, 
                                                                outputEl.getQName().getNamespaceURI(),
                                                                unwrappedOutput)) {
                    passedRule = false;
                }
            } else {
                passedRule = false;
            }
        }
               
        if (!passedRule) {
            return;
        }

        // we are wrappable!!
        OperationInfo unwrapped = new UnwrappedOperationInfo(opInfo);
        opInfo.setUnwrappedOperation(unwrapped);
        unwrapped.setInput(opInfo.getInputName(), unwrappedInput);
        if (outputMessage != null) {
            unwrapped.setOutput(opInfo.getOutputName(), unwrappedOutput);
        }
    }

    private boolean hasAttributes(XmlSchemaComplexType complexType) {
        // Now lets see if we have any attributes...
        // This should probably look at the restricted and substitute types too.
        if (complexType.getAnyAttribute() != null || complexType.getAttributes().getCount() > 0) {
            return true;
        }
        return false;
    }

    private boolean isWrappableSequence(XmlSchemaComplexType type, 
                                        String namespaceURI, 
                                        MessageInfo wrapper) { 
        if (type.getParticle() instanceof XmlSchemaSequence) {
            XmlSchemaSequence seq = (XmlSchemaSequence)type.getParticle();
            XmlSchemaObjectCollection items = seq.getItems();

            for (int x = 0; x < items.getCount(); x++) {
                XmlSchemaObject o = items.getItem(x);
                if (!(o instanceof XmlSchemaElement)) {
                    return false;
                }
                XmlSchemaElement el = (XmlSchemaElement)o;

                if (el.getSchemaTypeName() != null) {
                    MessagePartInfo mpi = wrapper.addMessagePart(new QName(namespaceURI, el.getName()));
                    mpi.setTypeQName(el.getSchemaTypeName());
                    mpi.setXmlSchema(el);
                } else if (el.getRefName() != null) {
                    MessagePartInfo mpi = wrapper.addMessagePart(el.getRefName());
                    mpi.setTypeQName(el.getRefName());
                    mpi.setXmlSchema(el);
                } else {
                    // anonymous type
                    MessagePartInfo mpi = wrapper.addMessagePart(
                        new QName(namespaceURI, el.getName()));
                    mpi.setElementQName(mpi.getName());
                    mpi.setElement(true);
                    mpi.setXmlSchema(el);
                }
            }

            return true;
        } else if (type.getParticle() == null) {
            return true;
        }
        return false;
    }

    private void buildMessage(AbstractMessageContainer minfo, Message msg, List paramOrder) {
        XmlSchemaCollection schemas = (XmlSchemaCollection)minfo.getOperation().getInterface().getService()
            .getProperty(WSDL_SCHEMA_LIST);
               
        List orderedParam = msg.getOrderedParts(paramOrder);
        for (Part part : cast(orderedParam, Part.class)) {
            MessagePartInfo pi = minfo.addMessagePart(new QName(minfo.getName().getNamespaceURI(), 
                    part.getName()));
            if (part.getTypeName() != null) {
                pi.setTypeQName(part.getTypeName());
                pi.setElement(false);
                pi.setXmlSchema(schemas.getTypeByQName(part.getTypeName()));
            } else {
                pi.setElementQName(part.getElementName());
                pi.setElement(true);
                pi.setXmlSchema(schemas.getElementByQName(part.getElementName()));
            }
        }
        for (Part part : cast(msg.getParts().values(), Part.class)) {
            if (!orderedParam.contains(part)) {
                MessagePartInfo pi = minfo.addMessagePart(new QName(minfo.getName().getNamespaceURI(), 
                        part.getName()));
                if (part.getTypeName() != null) {
                    pi.setTypeQName(part.getTypeName());
                    pi.setElement(false);
                    pi.setXmlSchema(schemas.getTypeByQName(part.getTypeName()));
                } else {
                    pi.setElementQName(part.getElementName());
                    pi.setElement(true);
                    pi.setXmlSchema(schemas.getElementByQName(part.getElementName()));
                }                
            }
        }
    }    

}
