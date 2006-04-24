package org.objectweb.celtix.tools.processors.wsdl2.validators;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.wsdl.Definition;
import javax.wsdl.Fault;
import javax.wsdl.Input;
import javax.wsdl.Operation;
import javax.wsdl.Output;
import javax.wsdl.PortType;
import javax.xml.namespace.QName;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.objectweb.celtix.tools.common.WSDLConstants;
import org.objectweb.celtix.tools.utils.ElementLocator;

public class PortTypeValidator {

    private Definition def;
    private Map<QName, List> msgPartsMap;
    private Map<QName, Map> portTypes;
    private Document document;

    public PortTypeValidator(Definition definition, SchemaWSDLValidator wsdlValidator) {

        this.def = definition;
        msgPartsMap = wsdlValidator.getMsgPartsMap();
        portTypes = wsdlValidator.getPortTypesMap();
        document = wsdlValidator.getSchemaValidatedDoc();

    }

    public void validatePortType() {
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
            List operations = portType.getOperations();
            Iterator ite2 = operations.iterator();
            while (ite2.hasNext()) {
                Operation operation = (Operation)ite2.next();
                operationMap.put(new QName(tns, operation.getName()), operation);

                Input input = operation.getInput();
                if (input != null && input.getMessage() != null
                    && !msgPartsMap.containsKey(input.getMessage().getQName())) {
                    Node errNode = ElementLocator.getNode(document, WSDLConstants.QNAME_OPERATION, operation
                        .getName(), input.getName());
                    ElementLocator loc = (ElementLocator)errNode.getUserData(WSDLConstants.NODE_LOCATION);
                    System.out.println("---line --- " + loc.getLine() + "--column---" + loc.getColumn());

                }

                Output output = operation.getOutput();
                if (output != null && output.getMessage() != null
                    && !msgPartsMap.containsKey(output.getMessage().getQName())) {
                    Node errNode = ElementLocator.getNode(document, WSDLConstants.QNAME_OPERATION, operation
                        .getName(), output.getName());
                    ElementLocator loc = (ElementLocator)errNode.getUserData(WSDLConstants.NODE_LOCATION);
                    System.out.println("---line --- " + loc.getLine() + "--column---" + loc.getColumn());

                }

                Map faultMap = operation.getFaults();
                Iterator faultIte = faultMap.values().iterator();
                while (faultIte.hasNext()) {
                    Fault fault = (Fault)faultIte.next();
                    if (fault != null && fault.getMessage() != null
                        && !msgPartsMap.containsKey(fault.getMessage().getQName())) {
                        Node errNode = ElementLocator.getNode(document, WSDLConstants.QNAME_OPERATION,
                                                              operation.getName(), fault.getName());
                        ElementLocator loc = (ElementLocator)errNode.getUserData(WSDLConstants.NODE_LOCATION);
                        System.out.println("---line --- " + loc.getLine() + "--column---" + loc.getColumn());

                    }

                }

            }
        }
    }

    public Map getPortTypes() {
        return portTypes;
    }

}
