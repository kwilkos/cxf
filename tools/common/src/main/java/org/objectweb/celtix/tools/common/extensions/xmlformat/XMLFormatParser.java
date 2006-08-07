package org.objectweb.celtix.tools.common.extensions.xmlformat;


import javax.wsdl.Definition;

import org.w3c.dom.*;

import org.objectweb.celtix.helpers.XMLUtils;
import org.objectweb.celtix.tools.common.ToolConstants;

public class XMLFormatParser {

    private XMLUtils xmlUtils = new XMLUtils();
    
    public void parseElement(Definition def, XMLFormat xmlFormat, Element element) {
        Attr rootNodeAttribute = xmlUtils.getAttribute(element, ToolConstants.XMLBINDING_ROOTNODE);
        String rootNodeValue = rootNodeAttribute.getValue();
        
        if (rootNodeValue != null) {
            xmlFormat.setRootNode(xmlUtils.getNamespace(
                XMLUtils.cast(def.getNamespaces(), String.class, String.class),
                rootNodeValue,
                def.getTargetNamespace()));
        }
    }
    
    
}
