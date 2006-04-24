package org.objectweb.celtix.tools.processors.wsdl2.validators;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.wsdl.Definition;
import javax.wsdl.Message;
import javax.wsdl.Part;
import javax.xml.namespace.QName;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.sun.org.apache.xerces.internal.xs.XSModel;

import org.objectweb.celtix.tools.common.WSDLConstants;
import org.objectweb.celtix.tools.utils.ElementLocator;

public class MessageValidator {

    private java.util.List<XSModel> schemas;
    private Definition def;
    private Map<QName, List> msgPartsMap;
    private SchemaWSDLValidator schemaWSDLValidator;

    public MessageValidator(Definition definition, SchemaWSDLValidator wsdlValidator) {
        def = definition;
        schemaWSDLValidator = wsdlValidator;
        schemas = wsdlValidator.getXSModelList();
        msgPartsMap = wsdlValidator.getMsgPartsMap();

    }

    public boolean validateMessages() {

        boolean isValid = true;

        String tns = def.getTargetNamespace();

        Map messageMap = def.getMessages();

        Document doc = schemaWSDLValidator.getSchemaValidatedDoc();

        NodeList nodeList = doc.getElementsByTagNameNS(WSDLConstants.QNAME_MESSAGE.getNamespaceURI(),
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
                    schemaWSDLValidator
                        .addError(ElementLocator.getNode(doc, WSDLConstants.QNAME_MESSAGE, msg
                            .getQName().getLocalPart(), part.getName()),
                                  "The part does not have a type defined. Every part must "
                                      + "specify a type from some type system. The type can "
                                      + "be specified using the built in 'element' or 'type' attributes "
                                      + "or may be specified using an extension attribute.");

                    isValid = false;

                }

                if (elementName != null && typeName != null) {
                    schemaWSDLValidator.addError(ElementLocator.getNode(doc, WSDLConstants.QNAME_MESSAGE, 
                                                                        msg.getQName().getLocalPart(),
                                         part.getName()),
                                                 "The part has both an element and a type defined. Every "
                                                     + "part must only have an element or a type defined.");
                    isValid = false;

                }

                if (elementName != null && typeName == null) {
                    boolean valid = vlidatePartType(elementName.getNamespaceURI(),
                                                    elementName.getLocalPart(), true);
                    if (!valid) {
                        schemaWSDLValidator.addError(ElementLocator.getNode(doc, WSDLConstants.QNAME_MESSAGE,
                                                                            msg.getQName().getLocalPart(),
                                                                            part.getName()),
                                                     elementName + " reference can not find");
                        isValid = false;
                    }

                }
                if (typeName != null && elementName == null) {

                    boolean valid = vlidatePartType(typeName.getNamespaceURI(), typeName.getLocalPart(),
                                                    false);

                    if (!valid) {
                        schemaWSDLValidator.addError(ElementLocator.getNode(doc, WSDLConstants.QNAME_MESSAGE, 
                                                                            msg.getQName().getLocalPart(),
                                             part.getName()), "reference can not find");
                        isValid = false;
                    }

                }

                partsList.add(part.getName());

            }

        }
        return isValid;

    }

    protected boolean vlidatePartType(String namespace, String name, boolean isElement) {

        boolean partvalid = false;

        if (namespace.equals(WSDLConstants.NS_XMLNS)) {
            SchemaSymbolTable table = new SchemaSymbolTable();
            if (table.containsSymbol(name)) {
                partvalid = true;
            }
        } else {
            Iterator ite = schemas.iterator();
            while (ite.hasNext()) {
                XSModel schema = (XSModel)ite.next();

                if (schema != null && isElement && schema.getElementDeclaration(name, namespace) != null) {
                    partvalid = true;
                    break;

                }
                if (schema != null && !isElement && schema.getTypeDefinition(name, namespace) != null) {
                    partvalid = true;
                    break;
                }
            }
        }
        return partvalid;
    }

}
