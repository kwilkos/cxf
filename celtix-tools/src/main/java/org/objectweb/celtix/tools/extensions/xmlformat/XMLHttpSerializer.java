package org.objectweb.celtix.tools.extensions.xmlformat;

import java.io.PrintWriter;

import javax.wsdl.Definition;
import javax.wsdl.WSDLException;
import javax.wsdl.extensions.ExtensibilityElement;
import javax.wsdl.extensions.ExtensionDeserializer;
import javax.wsdl.extensions.ExtensionRegistry;
import javax.wsdl.extensions.ExtensionSerializer;
import javax.xml.namespace.QName;

import org.w3c.dom.Element;

import org.objectweb.celtix.tools.common.ToolConstants;
import org.objectweb.celtix.tools.utils.XMLParserUtil;

public class XMLHttpSerializer implements ExtensionSerializer, ExtensionDeserializer {

    public void marshall(Class parentType, QName elementType, ExtensibilityElement extension, PrintWriter pw,
                         Definition def, ExtensionRegistry extReg) throws WSDLException {

        XMLHttpAddress xmlHttpAddress = (XMLHttpAddress)extension;
        StringBuffer sb = new StringBuffer(300);
        sb.append("<" + XMLParserUtil.writeQName(def, elementType) + " ");
        if (xmlHttpAddress.getLocation() != null) {
            sb.append(ToolConstants.XMLBINDING_HTTP_LOCATION + "=\"" + xmlHttpAddress.getLocation() + "\"");
        }
        sb.append(" />");
        pw.print(sb.toString());
        pw.println();
    }

    public ExtensibilityElement unmarshall(Class parentType, QName elementType, Element el, Definition def,
                                           ExtensionRegistry extReg) throws WSDLException {

        XMLHttpAddress xmlHttpAddress = (XMLHttpAddress)extReg.createExtension(parentType, elementType);
        xmlHttpAddress.setElement(el);
        xmlHttpAddress.setElementType(elementType);
        xmlHttpAddress.setDocumentBaseURI(def.getDocumentBaseURI());
        xmlHttpAddress.setLocation(el.getAttribute(ToolConstants.XMLBINDING_HTTP_LOCATION));
        return xmlHttpAddress;
    }

}
