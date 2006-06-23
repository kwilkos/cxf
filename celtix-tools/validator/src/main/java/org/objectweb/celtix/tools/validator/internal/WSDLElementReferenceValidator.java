package org.objectweb.celtix.tools.validator.internal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.wsdl.Binding;
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
import javax.xml.namespace.QName;
import javax.xml.stream.Location;
import javax.xml.stream.XMLEventReader;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.apache.ws.commons.schema.XmlSchema;
import org.apache.ws.commons.schema.XmlSchemaCollection;
import org.apache.ws.commons.schema.XmlSchemaElement;
import org.apache.ws.commons.schema.XmlSchemaType;
import org.objectweb.celtix.tools.common.WSDLConstants;

public class WSDLElementReferenceValidator {
    private Map<String, XmlSchema> schemas;
    private Definition def;
    private Map<QName, List> msgPartsMap;
    private SchemaValidator schemaWSDLValidator;
    private Map<QName, QName> bindingMap;
    private Map<QName, Map> portTypes;
    private Document document;
    private Document locationDocument;
    private boolean isValid = true;
    private  XmlSchemaCollection schemaCollection;

    public WSDLElementReferenceValidator(Definition definition, SchemaValidator wsdlValidator,
                                         XMLEventReader reader) {
        def = definition;
        schemaWSDLValidator = wsdlValidator;
        schemas = wsdlValidator.getXMLSchemaMap();
        msgPartsMap = wsdlValidator.getMsgPartsMap();
        portTypes = wsdlValidator.getPortTypesMap();
        bindingMap = wsdlValidator.getBindingMap();
        document = wsdlValidator.getSchemaValidatedDoc();
        Stax2DOM stax2dom = new Stax2DOM();
        locationDocument = stax2dom.getDocument(reader);
        schemaCollection = new XmlSchemaCollection();
    }

    public boolean isValid() {
        this.validateMessages();
        this.validatePortType();
        this.vlidateBinding();
        this.validateService();
        return isValid;
    }

    private boolean validateMessages() {

        String tns = def.getTargetNamespace();

        Map messageMap = def.getMessages();

        NodeList nodeList = document.getElementsByTagNameNS(WSDLConstants.QNAME_MESSAGE.getNamespaceURI(),
                                                            WSDLConstants.QNAME_MESSAGE.getLocalPart());

        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node = nodeList.item(i);
            Node attNode = node.getAttributes().getNamedItem(WSDLConstants.ATTR_NAME);
            QName qname = new QName(tns, attNode.getNodeValue());

            List<String> partsList = new ArrayList<String>();

            msgPartsMap.put(qname, partsList);

            Message msg = (Message)messageMap.get(qname);

            Map partsMap = msg.getParts();

            Iterator ite2 = partsMap.values().iterator();
            while (ite2.hasNext()) {
                Part part = (Part)ite2.next();

                QName elementName = part.getElementName();
                QName typeName = part.getTypeName();
                if (elementName == null && typeName == null) {
                    Location loc = this.getErrNodeLocation(WSDLConstants.QNAME_MESSAGE, msg.getQName()
                        .getLocalPart(), part.getName());

                    schemaWSDLValidator
                        .addError(loc, "The part does not have a type defined. Every part must "
                                       + "specify a type from some type system. The type can "
                                       + "be specified using the built in 'element' or 'type' attributes "
                                       + "or may be specified using an extension attribute.");

                    isValid = false;

                }

                if (elementName != null && typeName != null) {
                    Location loc = this.getErrNodeLocation(WSDLConstants.QNAME_MESSAGE, msg.getQName()
                        .getLocalPart(), part.getName());
                    schemaWSDLValidator.addError(loc,
                                                 "The part has both an element and a type defined. Every "
                                                     + "part must only have an element or a type defined.");
                    isValid = false;

                }

                if (elementName != null && typeName == null) {
                    boolean valid = vlidatePartType(elementName.getNamespaceURI(),
                                                    elementName.getLocalPart(), true);
                    if (!valid) {
                        Location loc = this.getErrNodeLocation(WSDLConstants.QNAME_MESSAGE, msg.getQName()
                            .getLocalPart(), part.getName());
                        schemaWSDLValidator.addError(loc, elementName + " refefrence can not find");

                        isValid = false;
                    }

                }
                if (typeName != null && elementName == null) {

                    boolean valid = vlidatePartType(typeName.getNamespaceURI(), typeName.getLocalPart(),
                                                    false);

                    if (!valid) {
                        Location loc = this.getErrNodeLocation(WSDLConstants.QNAME_MESSAGE, msg.getQName()
                            .getLocalPart(), part.getName());
                        schemaWSDLValidator.addError(loc, "reference can not find");
                        isValid = false;
                    }

                }

                partsList.add(part.getName());

            }

        }
        return isValid;
    }

    private boolean vlidatePartType(String namespace, String name, boolean isElement) {

        boolean partvalid = false;

        if (namespace.equals(WSDLConstants.NS_XMLNS)) {
            if (isElement) {
                XmlSchemaElement  schemaEle = 
                    schemaCollection.getElementByQName(new QName(WSDLConstants.NS_XMLNS, name));    
                partvalid = schemaEle != null ? true : false;
            } else {
                XmlSchemaType schemaType = 
                    schemaCollection.getTypeByQName(new QName(WSDLConstants.NS_XMLNS, name));  
                partvalid = schemaType != null ? true : false;
            }
            
        } else {
            Iterator ite = schemas.values().iterator();
            while (ite.hasNext()) {
                XmlSchema schema = (XmlSchema)ite.next();

                if (schema != null && isElement
                    && schema.getElementByName(new QName(namespace, name)) != null) {
                    partvalid = true;
                    break;

                }
                if (schema != null && !isElement 
                    && schema.getTypeByName(new QName(namespace, name)) != null) {
                    partvalid = true;
                    break;
                }
            }
        }
        return partvalid;
    }

    @SuppressWarnings("unchecked")
    private boolean vlidateBinding() {

        NodeList nodelist = document.getElementsByTagNameNS(WSDLConstants.QNAME_BINDING.getNamespaceURI(),
                                                            WSDLConstants.QNAME_BINDING.getLocalPart());

        for (int i = 0; i < nodelist.getLength(); i++) {
            Node node = nodelist.item(i);
            Node n = node.getAttributes().getNamedItem(WSDLConstants.ATTR_NAME);
            QName bindingName = new QName(def.getTargetNamespace(), n.getNodeValue());

            Binding binding = def.getBinding(bindingName);

            QName typeName = binding.getPortType().getQName();

            if (!portTypes.containsKey(typeName)) {
                Location loc = getErrNodeLocation(WSDLConstants.QNAME_DEFINITIONS, null, bindingName
                    .getLocalPart());
                schemaWSDLValidator.addError(loc, typeName + " is not defined");
                isValid = false;
            } else {

                Map<QName, Operation> operationMap = portTypes.get(typeName);

                // OperationList
                List<QName> operationList = new ArrayList<QName>();
                operationList.addAll(operationMap.keySet());

                // bindingOperationList
                Iterator ite = binding.getBindingOperations().iterator();
                while (ite.hasNext()) {
                    BindingOperation bop = (BindingOperation)ite.next();
                    QName bopName = new QName(def.getTargetNamespace(), bop.getName());

                    if (!operationList.contains(bopName)) {

                        Location loc = this.getErrNodeLocation(WSDLConstants.QNAME_BINDING, bindingName
                            .getLocalPart(), bop.getName());
                        schemaWSDLValidator.addError(loc, "BindingOperation " + bop.getName()
                                                          + " is not defined");

                        isValid = false;

                    } else {
                        Operation op = operationMap.get(bopName);

                        if (op.getInput() == null && bop.getBindingInput() != null) {
                            Location loc = this.getErrNodeLocation(WSDLConstants.QNAME_BINDING, bindingName
                                .getLocalPart(), bop.getName());
                            schemaWSDLValidator.addError(loc, "BindingOperation " + bop.getName()
                                                              + " binding input is not defined");
                            isValid = false;
                        }

                        if (op.getInput() != null && bop.getBindingInput() == null) {
                            Location loc = this.getErrNodeLocation(WSDLConstants.QNAME_BINDING, bindingName
                                .getLocalPart(), bop.getName());
                            schemaWSDLValidator.addError(loc, "BindingOperation " + bop.getName()
                                                              + " binding input is not resolved");

                            isValid = false;
                        }

                        if (op.getOutput() == null && bop.getBindingOutput() != null) {
                            Location loc = this.getErrNodeLocation(WSDLConstants.QNAME_BINDING, bindingName
                                .getLocalPart(), bop.getName());
                            schemaWSDLValidator.addError(loc, "BindingOperation " + bop.getName()
                                                              + " binding output is not defined");
                            isValid = false;
                        }

                        if (op.getOutput() != null && bop.getBindingOutput() == null) {
                            Location loc = this.getErrNodeLocation(WSDLConstants.QNAME_BINDING, bindingName
                                .getLocalPart(), bop.getName());
                            schemaWSDLValidator.addError(loc, "BindingOperation " + bop.getName()
                                                              + " binding output is not resolved");

                            isValid = false;
                        }

                        if (op.getFaults().size() != bop.getBindingFaults().size()) {
                            Location loc = this.getErrNodeLocation(WSDLConstants.QNAME_BINDING, bindingName
                                .getLocalPart(), bop.getName());
                            schemaWSDLValidator.addError(loc, "BindingOperation " + bop.getName()
                                                              + " binding fault resolved error");
                            isValid = false;
                        }

                    }

                }
            }

            bindingMap.put(bindingName, typeName);
        }

        return isValid;
    }

    private boolean validateService() {

        Map serviceMap = def.getServices();
        Iterator ite = serviceMap.values().iterator();
        while (ite.hasNext()) {
            Service service = (Service)ite.next();
            Iterator portIte = service.getPorts().values().iterator();
            while (portIte.hasNext()) {
                Port port = (Port)portIte.next();
                Binding binding = port.getBinding();
                if (!bindingMap.containsKey(binding.getQName())) {
                    Location loc = this.getErrNodeLocation(WSDLConstants.QNAME_SERVICE, service.getQName()
                        .getLocalPart(), port.getName());
                    schemaWSDLValidator.addError(loc, " port : " + port.getName()
                                                      + " reference binding is not defined");
                    isValid = false;
                }
            }
        }

        return isValid;
    }

    private boolean validatePortType() {

        String tns = def.getTargetNamespace();
        NodeList nodeList = document.getElementsByTagNameNS(WSDLConstants.QNAME_PORT_TYPE.getNamespaceURI(),
                                                            WSDLConstants.QNAME_PORT_TYPE.getLocalPart());
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node = nodeList.item(i);
            Node attNode = node.getAttributes().getNamedItem(WSDLConstants.ATTR_NAME);
            QName qname = new QName(tns, attNode.getNodeValue());

            java.util.Map portTypeMap = def.getPortTypes();
            PortType portType = (PortType)portTypeMap.get(qname);
            Map<QName, Operation> operationMap = new HashMap<QName, Operation>();

            portTypes.put(qname, operationMap);

            // Get operations under portType
            for (Node n = node.getFirstChild(); n != null; n = n.getNextSibling()) {
                if (n.getNodeType() == Node.ELEMENT_NODE
                    && n.getLocalName().equals(WSDLConstants.QNAME_OPERATION.getLocalPart())) {
                    Node opNameNode = n.getAttributes().getNamedItem(WSDLConstants.ATTR_NAME);
                    String opName = opNameNode.getNodeValue();
                    List operations = portType.getOperations();
                    Iterator ite2 = operations.iterator();
                    while (ite2.hasNext()) {
                        Operation operation = (Operation)ite2.next();
                        if (operation.getName().equals(opName)) {

                            operationMap.put(new QName(tns, opName), operation);
                            Input input = operation.getInput();
                            if (input != null && input.getMessage() != null
                                && !msgPartsMap.containsKey(input.getMessage().getQName())) {
                                Location loc = this.getErrNodeLocation(WSDLConstants.QNAME_OPERATION,
                                                                       operation.getName(), input.getName());
                                schemaWSDLValidator.addError(loc, " input : " + input.getName()
                                                                  + " reference is not defined");
                                isValid = false;
                            }

                            Output output = operation.getOutput();
                            if (output != null && output.getMessage() != null
                                && !msgPartsMap.containsKey(output.getMessage().getQName())) {
                                Location loc = this.getErrNodeLocation(WSDLConstants.QNAME_OPERATION,
                                                                       operation.getName(), output.getName());
                                schemaWSDLValidator.addError(loc, " output : " + output.getName()
                                                                  + " reference is not defined");
                                isValid = false;
                            }

                            Map faultMap = operation.getFaults();
                            Iterator faultIte = faultMap.values().iterator();
                            while (faultIte.hasNext()) {
                                Fault fault = (Fault)faultIte.next();
                                if (fault != null && fault.getMessage() != null
                                    && !msgPartsMap.containsKey(fault.getMessage().getQName())) {
                                    Location loc = getErrNodeLocation(WSDLConstants.QNAME_OPERATION,
                                                                      operation.getName(), fault.getName());
                                    addError(loc, " fault : " 
                                             + fault.getName() + " reference is not defined");
                                    isValid = false;
                                }

                            }

                        }
                    }
                }
            }
        }

        return isValid;
    }

    private void addError(Location loc, String msg) {
        schemaWSDLValidator.addError(loc, msg);

    }

    public Location getErrNodeLocation(QName wsdlParentNode, String parentNameValue,

    String childNameValue) {
        NodeList parentNodeList = locationDocument.getElementsByTagNameNS(wsdlParentNode.getNamespaceURI(),
                                                                          wsdlParentNode.getLocalPart());

        for (int i = 0; i < parentNodeList.getLength(); i++) {
            Node parentNode = parentNodeList.item(i);
            NamedNodeMap parentNodeMap = parentNode.getAttributes();
            Node parentAttrNode = parentNodeMap.getNamedItem(WSDLConstants.ATTR_NAME);
            if (parentAttrNode != null && parentNameValue != null
                && parentAttrNode.getNodeValue().equals(parentNameValue) || parentAttrNode == null
                || parentNameValue == null) {

                for (Node n = parentNode.getFirstChild(); n != null; n = n.getNextSibling()) {
                    if (n.getNodeType() == Node.ELEMENT_NODE) {
                        NamedNodeMap map = n.getAttributes();
                        Node attrChildNode = map.getNamedItem(WSDLConstants.ATTR_NAME);
                        if (attrChildNode != null && attrChildNode.getNodeValue().equals(childNameValue)) {
                            return (Location)n.getUserData(WSDLConstants.NODE_LOCATION);
                        }
                    }
                }

            }
        }
        return null;
    }

}
