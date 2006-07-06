package org.objectweb.celtix.bindings.soap2.utils;

import java.util.*;
import org.w3c.dom.*;

import org.jaxen.XPath;
import org.jaxen.dom.DOMXPath;

public final class XPathUtils {

    private Map<String, String> namespaces = new HashMap<String, String>();
    
    public XPathUtils() {
    }

    public void addNamespace(String prefix, String namespaceURI) {
        namespaces.put(prefix, namespaceURI);
    }
    
    public boolean isExist(Document doc, String xpathExpr) throws Exception {
        return isExist(doc, new DOMXPath(xpathExpr));
    }

    private boolean isExist(Object node, XPath xpath) throws Exception {
        for (String prefix : namespaces.keySet()) {
            xpath.addNamespace(prefix, namespaces.get(prefix));
        }
        return xpath.selectSingleNode(node) != null;
    }
}
