package org.apache.cxf.helpers;

import org.w3c.dom.Node;

public final class NodeUtils {

    private NodeUtils() {
        //Complete
    }

    /**
     * Returns a first child DOM Node of type ELEMENT_NODE
     * for the specified Node.
     */
    public static Node getChildElementNode(Node xmlNode) {
        if (xmlNode == null || !xmlNode.hasChildNodes()) {
            return null;
        }
        
        xmlNode = xmlNode.getFirstChild();
        while (xmlNode != null 
               && xmlNode.getNodeType() != Node.ELEMENT_NODE) {
            xmlNode = xmlNode.getNextSibling();
        }

        return xmlNode;
    }
}

