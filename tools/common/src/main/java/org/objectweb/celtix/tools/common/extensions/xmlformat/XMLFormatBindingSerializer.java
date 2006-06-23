package org.objectweb.celtix.tools.common.extensions.xmlformat;

import java.io.PrintWriter;

import javax.wsdl.Definition;
import javax.wsdl.WSDLException;
import javax.wsdl.extensions.ExtensibilityElement;
import javax.wsdl.extensions.ExtensionDeserializer;
import javax.wsdl.extensions.ExtensionRegistry;
import javax.wsdl.extensions.ExtensionSerializer;
import javax.xml.namespace.QName;

import org.w3c.dom.Element;

import org.objectweb.celtix.helpers.XMLUtils;

public class XMLFormatBindingSerializer implements ExtensionDeserializer, ExtensionSerializer {

    XMLUtils xmlUtils = new XMLUtils();
    
    public void marshall(Class parentType, QName elementType, ExtensibilityElement extension, PrintWriter pw,
                         Definition def, ExtensionRegistry extReg) throws WSDLException {

        pw.print("<" + xmlUtils.writeQName(def, elementType) + "/>");
        pw.println();
    }

    public ExtensibilityElement unmarshall(Class parentType, QName elementType, Element el, Definition def,
                                           ExtensionRegistry extReg) throws WSDLException {

        XMLFormatBinding xmlFormatBinding = (XMLFormatBinding)extReg.createExtension(parentType, elementType);
        xmlFormatBinding.setElement(el);
        xmlFormatBinding.setElementType(elementType);
        xmlFormatBinding.setDocumentBaseURI(def.getDocumentBaseURI());
        return xmlFormatBinding;
    }

}
