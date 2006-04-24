package org.objectweb.celtix.tools.utils;

import javax.xml.namespace.QName;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.objectweb.celtix.tools.common.WSDLConstants;

public class ElementLocator {

    private int line;
    private int column;

    public ElementLocator(int l, int c) {
        this.line = l;
        this.column = c;
    }

    public int getLine() {
        return line;
    }

    public int getColumn() {
        return column;
    }
    
    public static Node getNode(Document doc, QName wsdlParentNode, String parentNameValue,
                                String childNameValue) {
        NodeList parentNodeList = doc.getElementsByTagNameNS(wsdlParentNode.getNamespaceURI(), wsdlParentNode
            .getLocalPart());
        for (int i = 0; i < parentNodeList.getLength(); i++) {
            Node parentNode = parentNodeList.item(i);
            NamedNodeMap parentNodeMap = parentNode.getAttributes();
            Node parentAttrNode = parentNodeMap.getNamedItem(WSDLConstants.ATTR_NAME);
            if (parentAttrNode != null && parentAttrNode.getNodeValue().equals(parentNameValue)) {

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
