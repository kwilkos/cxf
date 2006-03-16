package org.objectweb.celtix.tools.utils;

import javax.wsdl.Definition;
import javax.xml.namespace.QName;


public final class XMLParserUtil {
    
    private XMLParserUtil() {
        // complete
    }
    
    public static String writeQName(Definition def, QName qname) {
        return def.getPrefix(qname.getNamespaceURI()) + ":" + qname.getLocalPart();
    }
}
