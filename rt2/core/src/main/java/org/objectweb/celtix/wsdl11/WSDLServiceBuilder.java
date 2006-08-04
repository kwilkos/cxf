package org.objectweb.celtix.wsdl11;

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

import org.apache.ws.commons.schema.XmlSchema;
import org.apache.ws.commons.schema.XmlSchemaCollection;
import org.apache.ws.commons.schema.XmlSchemaComplexType;
import org.apache.ws.commons.schema.XmlSchemaElement;
import org.apache.ws.commons.schema.XmlSchemaObject;
import org.apache.ws.commons.schema.XmlSchemaObjectCollection;
import org.apache.ws.commons.schema.XmlSchemaSequence;
import org.objectweb.celtix.Bus;
import org.objectweb.celtix.BusException;
import org.objectweb.celtix.bindings.BindingFactory;
import org.objectweb.celtix.service.model.AbstractMessageContainer;
import org.objectweb.celtix.service.model.AbstractPropertiesHolder;
import org.objectweb.celtix.service.model.BindingInfo;
import org.objectweb.celtix.service.model.BindingOperationInfo;
import org.objectweb.celtix.service.model.EndpointInfo;
import org.objectweb.celtix.service.model.FaultInfo;
import org.objectweb.celtix.service.model.InterfaceInfo;
import org.objectweb.celtix.service.model.MessageInfo;
import org.objectweb.celtix.service.model.MessagePartInfo;
import org.objectweb.celtix.service.model.OperationInfo;
import org.objectweb.celtix.service.model.SchemaInfo;
import org.objectweb.celtix.service.model.ServiceInfo;
import org.objectweb.celtix.service.model.TypeInfo;
import org.objectweb.celtix.service.model.UnwrappedOperationInfo;

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

    // utility for dealing with the JWSDL collections that are 1.4 based. We can
    // kind of use a normal for loop with this
    @SuppressWarnings("unchecked")
    public static <T> Collection<T> cast(Collection<?> p, Class<T> cls) {
        return (Collection<T>)p;
    }

    @SuppressWarnings("unchecked")
    public static <T, U> Map.Entry<T, U> cast(Map.Entry<?, ?> p, Class<T> pc, Class<U> uc) {
        return (Map.Entry<T, U>)p;
    }

    @SuppressWarnings("unchecked")
    static <T, U> Map<T, U> cast(Map<?, ?> p, Class<T> pc, Class<U> uc) {
        return (Map<T, U>)p;
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

        TypeInfo typeInfo = new TypeInfo(service);
        XmlSchemaCollection schemas = getSchemas(def, typeInfo);
        service.setProperty(WSDL_SCHEMA_LIST, schemas);
        service.setTypeInfo(typeInfo);
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
            buildEndpoint(service, bi, port);
        }

        return service;
    }

    private XmlSchemaCollection getSchemas(Definition def, TypeInfo typeInfo) {
        XmlSchemaCollection schemaCol = new XmlSchemaCollection();
        List<Definition> defList = new ArrayList<Definition>();
        parseImports(def, defList);
        extractSchema(def, schemaCol, typeInfo);
        for (Definition def2 : defList) {
            extractSchema(def2, schemaCol, typeInfo);
        }
        return schemaCol;
    }

    @SuppressWarnings("unchecked")
    private void parseImports(Definition def, List<Definition> defList) {
        List<Import> importList = new ArrayList<Import>();

        Collection<List<Import>> ilist = (Collection<List<Import>>)def.getImports().values();
        for (List<Import> list : ilist) {
            importList.addAll(list);
        }
        for (Import impt : importList) {
            parseImports(impt.getDefinition(), defList);
            defList.add(impt.getDefinition());
        }
    }
    
 
    private void extractSchema(Definition def, XmlSchemaCollection schemaCol, TypeInfo typeInfo) {
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
                    XmlSchema xmlSchema = schemaCol.read(schemaElem);
                    SchemaInfo schemaInfo = new SchemaInfo(typeInfo, xmlSchema.getTargetNamespace());
                    schemaInfo.setElement(schemaElem);
                    typeInfo.addSchema(schemaInfo);
                    
                }
            }
        }
        
    }

    public EndpointInfo buildEndpoint(ServiceInfo service, BindingInfo bi, Port port) {
        String ns = ((ExtensibilityElement)port.getExtensibilityElements().get(0)).getElementType()
            .getNamespaceURI();
        EndpointInfo ei = new EndpointInfo(service, ns);
        ei.setName(port.getName());
        ei.setBinding(bi);
        copyExtensors(ei, port.getExtensibilityElements());
        service.addEndpoint(ei);
        return ei;
    }

    public BindingInfo buildBinding(ServiceInfo service, Binding binding) {
        BindingInfo bi = null;
        String ns = ((ExtensibilityElement)binding.getExtensibilityElements().get(0)).getElementType()
            .getNamespaceURI();
        try {
            BindingFactory factory = bus.getBindingManager().getBindingFactory(ns);
            if (factory instanceof WSDLBindingFactory) {
                WSDLBindingFactory wFactory = (WSDLBindingFactory)factory;
                bi = wFactory.createBindingInfo(service, binding);
            }
        } catch (BusException e) {
            // ignore, we'll use a generic BindingInfo
        }

        if (bi == null) {
            bi = new BindingInfo(service, ns);
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
                    }
                    if (bop.getBindingOutput() != null) {
                        copyExtensors(bop2.getOutput(), bop.getBindingOutput().getExtensibilityElements());
                    }
                    for (BindingFault f : cast(bop.getBindingFaults().values(), BindingFault.class)) {
                        copyExtensors(bop2.getFault(f.getName()), bop.getBindingFault(f.getName())
                            .getExtensibilityElements());
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

    private void buildInterfaceOperation(InterfaceInfo inf, Operation op) {
        OperationInfo opInfo = inf.addOperation(new QName(inf.getName().getNamespaceURI(), op.getName()));
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
            FaultInfo finfo = opInfo.addFault(new QName(inf.getName().getNamespaceURI(), entry.getKey()),
                                              entry.getValue().getMessage().getQName());
            buildMessage(finfo, entry.getValue().getMessage());
        }
        checkForWrapped(opInfo);
    }

    private void checkForWrapped(OperationInfo opInfo) {
        MessageInfo inputMessage = opInfo.getInput();
        MessageInfo outputMessage = opInfo.getOutput();

        // RULE No.1:
        // The operation's input and output message (if present) each contain
        // only a single part
        // input message must exist
        if (inputMessage == null || inputMessage.size() != 1
            || (outputMessage != null && outputMessage.size() != 1)) {
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
            return;
        } else {
            QName inputElementName = inputPart.getElementQName();
            inputEl = schemas.getElementByQName(inputElementName);
            if (inputEl == null || !opInfo.getName().getLocalPart().equals(inputElementName.getLocalPart())) {
                return;
            }
        }

        // RULE No.3:
        // The output message part refers to a global element decalration
        MessagePartInfo outputPart = null;
        if (outputMessage != null && outputMessage.size() == 1) {
            outputPart = outputMessage.getMessagePartByIndex(0);
            if (outputPart != null) {
                if (!outputPart.isElement()
                    || schemas.getElementByQName(outputPart.getElementQName()) == null) {
                    return;
                }
                outputEl = schemas.getElementByQName(outputPart.getElementQName());
            }
        }

        // RULE No.4 and No5:
        // wrapper element should be pure complex type

        // Now lets see if we have any attributes...
        // This should probably look at the restricted and substitute types too.
        MessageInfo unwrappedInput = new MessageInfo(opInfo, inputMessage.getName());
        MessageInfo unwrappedOutput = null;

        XmlSchemaComplexType xsct = (XmlSchemaComplexType)inputEl.getSchemaType();
        if (inputEl.getSchemaType() instanceof XmlSchemaComplexType
            && (hasAttributes(xsct) || !isWrappableSequence(xsct, unwrappedInput))) {
            return;
        }
        if (outputMessage != null) {
            unwrappedOutput = new MessageInfo(opInfo, outputMessage.getName());
            xsct = (XmlSchemaComplexType)outputEl.getSchemaType();
            if (outputEl != null && (hasAttributes(xsct) || !isWrappableSequence(xsct, unwrappedOutput))) {
                return;
            }
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

    private boolean isWrappableSequence(XmlSchemaComplexType type, MessageInfo wrapper) {
        if (type.getParticle() instanceof XmlSchemaSequence) {
            XmlSchemaSequence seq = (XmlSchemaSequence)type.getParticle();
            XmlSchemaObjectCollection items = seq.getItems();

            for (int x = 0; x < items.getCount(); x++) {
                XmlSchemaObject o = items.getItem(x);
                if (!(o instanceof XmlSchemaElement)) {
                    return false;
                }
                XmlSchemaElement el = (XmlSchemaElement)o;
                if (el.getMaxOccurs() > 1) {
                    return false;
                }
                // If this is an anonymous complex type, mark it as unwrapped.
                // We're doing this because things like JAXB don't have support
                // for finding classes from anonymous type names.
                if (el.getSchemaTypeName() == null && el.getRefName() == null) {
                    return false;
                }

                MessagePartInfo mpi = wrapper.addMessagePart(el.getQName());
                mpi.setTypeQName(el.getSchemaTypeName());
            }

            return true;
        }
        return false;
    }

    private void buildMessage(AbstractMessageContainer minfo, Message msg) {
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
