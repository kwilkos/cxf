package org.objectweb.celtix.tools.common.extensions.xmlformat;


import java.util.Map;

import javax.wsdl.Definition;

import org.w3c.dom.*;

import org.objectweb.celtix.helpers.CastUtils;
import org.objectweb.celtix.helpers.XMLUtils;
import org.objectweb.celtix.tools.common.ToolConstants;

public class XMLFormatParser {

    private XMLUtils xmlUtils = new XMLUtils();
    
    public void parseElement(Definition def, XMLFormat xmlFormat, Element element) {
        Attr rootNodeAttribute = xmlUtils.getAttribute(element, ToolConstants.XMLBINDING_ROOTNODE);
        String rootNodeValue = rootNodeAttribute.getValue();
        
        if (rootNodeValue != null) {
            Map<String, String> mp = CastUtils.cast(def.getNamespaces());
            xmlFormat.setRootNode(xmlUtils.getNamespace(
                mp,
                rootNodeValue,
                def.getTargetNamespace()));
        }
    }
    
    
}
