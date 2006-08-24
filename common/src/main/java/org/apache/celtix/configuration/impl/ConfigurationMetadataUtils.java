package org.apache.cxf.configuration.impl;

import java.util.logging.Logger;

import javax.xml.namespace.QName;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import org.apache.cxf.common.i18n.Message;
import org.apache.cxf.common.logging.LogUtils;
import org.apache.cxf.configuration.ConfigurationException;

public final class ConfigurationMetadataUtils {

    private static final Logger LOG = LogUtils.getL7dLogger(ConfigurationMetadataUtils.class);
    
    /**
     * prevents instantiation
     *
     */
    private ConfigurationMetadataUtils() {
    }
    
    public static String getElementValue(Node node) {
        for (Node nd = node.getFirstChild(); nd != null; nd = nd.getNextSibling()) {
            if (Node.TEXT_NODE == nd.getNodeType()) {
                return nd.getNodeValue();
            }
        }
        return null;
    }
    
    public static QName elementAttributeToQName(Document document, Element element, String attrName) {
        return stringToQName(document, element, element.getAttribute(attrName));
    }
    
    public static QName elementValueToQName(Document document, Element element) {
        return stringToQName(document, element, getElementValue(element));
    }
    
    private static QName stringToQName(Document document, Element element, String s) {
        
        int index = s.indexOf(":");
        if (index < 0) {
            return new QName(s);
        } else if (index == 0) {
            throw new ConfigurationException(new Message("ILLEGAL_QNAME_EXC", LOG, s));
        }
        String prefix = s.substring(0, index);
        String nsAttr = "xmlns:" + prefix;
        String uri = null;
        Element el = element;
        while (null == uri || "".equals(uri)) {
            uri = el.getAttribute(nsAttr);
            if (null != uri && !"".equals(uri)) {
                break;
            }
            if (el == document.getDocumentElement()) {
                break;
            }
            el = (Element)el.getParentNode();
        }
        if (null == uri || "".equals(uri)) {
            throw new ConfigurationException(new Message("ILLEGAL_PREFIX_EXC", LOG, s));
        }
        if (index >= (s.length() - 1)) {
            throw new ConfigurationException(new Message("ILLEGAL_QNAME_EXC", LOG, s));
        }
        
        String localPart = s.substring(index + 1);
        return new QName(uri, localPart);
    }
}
