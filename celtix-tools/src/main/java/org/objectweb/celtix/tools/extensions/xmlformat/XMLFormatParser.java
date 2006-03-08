package org.objectweb.celtix.tools.extensions.xmlformat;

import org.w3c.dom.Element;

import org.objectweb.celtix.tools.common.ToolConstants;
import org.objectweb.celtix.tools.utils.XMLUtil;

public class XMLFormatParser {

    public void parseElement(XMLFormat xmlFormat, Element element) {
        String rootNode = XMLUtil.getAttribute(element, ToolConstants.XMLBINDING_ROOTNODE);
        if (rootNode != null) {
            xmlFormat.setRootNode(rootNode);
        }
    }

}
