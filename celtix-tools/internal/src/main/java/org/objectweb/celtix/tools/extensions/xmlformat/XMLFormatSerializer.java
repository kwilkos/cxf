package org.objectweb.celtix.tools.extensions.xmlformat;

import java.io.PrintWriter;
import java.io.Serializable;

import javax.wsdl.Definition;
import javax.wsdl.WSDLException;
import javax.wsdl.extensions.ExtensibilityElement;
import javax.wsdl.extensions.ExtensionDeserializer;
import javax.wsdl.extensions.ExtensionRegistry;
import javax.wsdl.extensions.ExtensionSerializer;
import javax.xml.namespace.QName;

import org.w3c.dom.Element;

import org.objectweb.celtix.helpers.XMLUtils;
import org.objectweb.celtix.tools.common.ToolConstants;

public class XMLFormatSerializer implements ExtensionSerializer, ExtensionDeserializer, Serializable {

    XMLUtils xmlUtils = new XMLUtils();
    
    public void marshall(Class parentType, QName elementType, ExtensibilityElement extension, PrintWriter pw,
                         Definition def, ExtensionRegistry extReg) throws WSDLException {

        XMLFormat xmlFormat = (XMLFormat)extension;
        StringBuffer sb = new StringBuffer(300);
        sb.append("<" + xmlUtils.writeQName(def, elementType) + " ");
        if (xmlFormat.getRootNode() != null) {
            sb.append(ToolConstants.XMLBINDING_ROOTNODE + "=\""
                      + xmlUtils.writeQName(def, xmlFormat.getRootNode()) + "\"");
        }
        sb.append(" />");
        pw.print(sb.toString());
        pw.println();
    }

    public ExtensibilityElement unmarshall(Class parentType, QName elementType, Element el, Definition def,
                                           ExtensionRegistry extReg) throws WSDLException {

        XMLFormat xmlFormat = (XMLFormat)extReg.createExtension(parentType, elementType);
        xmlFormat.setElement(el);
        xmlFormat.setElementType(elementType);
        xmlFormat.setDocumentBaseURI(def.getDocumentBaseURI());
        XMLFormatParser xmlBindingParser = new XMLFormatParser();
        xmlBindingParser.parseElement(def, xmlFormat, el);
        return xmlFormat;
    }

}
