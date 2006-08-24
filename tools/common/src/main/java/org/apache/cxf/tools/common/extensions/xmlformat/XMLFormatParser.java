package org.apache.cxf.tools.common.extensions.xmlformat;


import java.util.Map;

import javax.wsdl.Definition;

import org.w3c.dom.*;

import org.apache.cxf.helpers.CastUtils;
import org.apache.cxf.helpers.XMLUtils;
import org.apache.cxf.tools.common.ToolConstants;

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
