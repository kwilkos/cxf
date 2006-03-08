package org.objectweb.celtix.tools.extensions.xmlformat;

import javax.wsdl.Definition;

import org.w3c.dom.*;

import org.objectweb.celtix.tools.common.ToolConstants;
import org.objectweb.celtix.tools.utils.XMLUtil;

public class XMLFormatParser {

    public void parseElement(Definition def, XMLFormat xmlFormat, Element element) {
        Attr rootNodeAttribute = XMLUtil.getAttribute(element, ToolConstants.XMLBINDING_ROOTNODE);
        String rootNodeValue = rootNodeAttribute.getValue();
        
        if (rootNodeValue != null) {
            xmlFormat.setRootNode(XMLUtil.getNamespace(def.getNamespaces(),
                                                       rootNodeValue,
                                                       def.getTargetNamespace()));
        }
    }
}
