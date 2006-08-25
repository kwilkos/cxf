package org.objectweb.celtix.tools.extensions.jms;

import java.io.*;
import java.lang.reflect.*;

import javax.wsdl.Definition;
import javax.wsdl.WSDLException;
import javax.wsdl.extensions.ExtensibilityElement;
import javax.wsdl.extensions.ExtensionDeserializer;
import javax.wsdl.extensions.ExtensionRegistry;
import javax.wsdl.extensions.ExtensionSerializer;
import javax.xml.namespace.QName;
import org.w3c.dom.*;

import org.objectweb.celtix.helpers.XMLUtils;
import org.objectweb.celtix.tools.common.ToolConstants;

public class JMSAddressSerializer implements ExtensionSerializer,
                                             ExtensionDeserializer,
                                             Serializable {
    public static final long serialVersionUID = 1;
    XMLUtils xmlUtils = new XMLUtils();
    
    public void marshall(Class parentType,
                         QName elementType,
                         ExtensibilityElement extension,
                         PrintWriter pw,
                         Definition def,
                         ExtensionRegistry extReg) throws WSDLException {

        JMSAddress jmsAddress = (JMSAddress)extension;
        StringBuffer sb = new StringBuffer(300);        
        sb.append(" <" + xmlUtils.writeQName(def, elementType) + " ");
        sb.append(jmsAddress.getAttrXMLString());
        sb.append("/>");
        pw.print(sb.toString());
        pw.println();
    }

    public ExtensibilityElement unmarshall(Class parentType,
                                           QName elementType,
                                           Element el,
                                           Definition def,
                                           ExtensionRegistry extReg) throws WSDLException {

        JMSAddress jmsAddress = (JMSAddress)extReg.createExtension(parentType, elementType);
        jmsAddress.setElementType(elementType);
        jmsAddress.setElement(el);
        jmsAddress.setDocumentBaseURI(def.getDocumentBaseURI());
        
        JMSAddressParser parser = new JMSAddressParser();
        parser.parseElement(jmsAddress, el);
        
        if (jmsAddress.getAddress() == null || jmsAddress.getAddress().trim().length() == 0) {
            if (def.getNamespaces() != null) {
                jmsAddress.setAddress((String)def.getNamespaces().get("jms"));
            } else {
                jmsAddress.setAddress(ToolConstants.NS_JMS_ADDRESS);
            }
        }
        return jmsAddress;
    }
}
