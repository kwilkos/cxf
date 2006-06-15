package org.objectweb.celtix.tools.generators.spring;

import java.util.logging.Logger;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;

import org.objectweb.celtix.common.i18n.Message;
import org.objectweb.celtix.common.logging.LogUtils;
import org.objectweb.celtix.jaxb.JAXBUtils;

public final class SpringUtils {
    
    private static final Logger LOG = LogUtils.getL7dLogger(SpringUtils.class);
    
    /**
     * prevent instantiation
     *
     */
    private SpringUtils() { 
        //utility class - never constructed
    }
    
    public static String getBeanClassName(String namespaceURI) {    
        StringBuffer buf = new StringBuffer(JAXBUtils.namespaceURIToPackage(namespaceURI));
        int index = buf.lastIndexOf(".");
        String className = null;
        if (index >= 0) {
            className = buf.substring(index + 1);
        } else {
            className = buf.toString();
        }
        buf.append(".spring.");
        int len = buf.length();
        className = JAXBUtils.nameToIdentifier(className, JAXBUtils.IdentifierType.CLASS);
        buf.append(className);
        if (Character.isLowerCase(buf.charAt(len))) {
            buf.setCharAt(len, Character.toUpperCase(buf.charAt(len)));
        }
        buf.append("Bean");
        return buf.toString();
    }
    
    /**
     * Converts a stringified representation of a QName into a QName.
     * The string representation is assumed to be of the form:
     * "{" + Namespace URI + "}" + local part.  If the Namespace URI
     * <code>.equals(XMLConstants.NULL_NS_URI)</code>, the string representation
     * only consists of the local part.
     * 
     * @throws IllegalArgumentException if the string is not of this format.
     * 
     * @param str string representation of the QName.
     * @return the QName.
     */
    public static QName stringToQName(String str) {
        
        if (str == null) {
            return null;
        }
   
        if (str.startsWith("{")) {
            int index = str.lastIndexOf("}");
            if (index <= 1) {
                throw new IllegalArgumentException(new Message("ILLEGAL_QNAME_STRING_EXC", 
                                                               LOG, str).toString());
            }
            return  new QName(str.substring(1, index), 
                              (index < str.length() - 1) ? str.substring(index + 1) : "");
        } else {
            return new QName(XMLConstants.NULL_NS_URI, str);
        }    
    }
}
