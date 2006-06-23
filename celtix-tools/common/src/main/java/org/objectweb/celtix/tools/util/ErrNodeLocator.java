package org.objectweb.celtix.tools.util;

import javax.xml.namespace.QName;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.objectweb.celtix.tools.common.WSDLConstants;

public final class ErrNodeLocator {
    private ErrNodeLocator() {

    }

    public static Node getNode(Document doc, QName wsdlParentNode, String parentNameValue,

    String childNameValue) {
        NodeList parentNodeList = doc.getElementsByTagNameNS(wsdlParentNode.getNamespaceURI(), wsdlParentNode
            .getLocalPart());

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
                            return n;
                        }
                    }
                }

            }
        }
        return null;
    }
}
