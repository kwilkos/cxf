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

public class XMLFormatBindingSerializer implements ExtensionDeserializer, ExtensionSerializer {

    public void marshall(Class parentType, QName elementType, ExtensibilityElement extension,
                         PrintWriter pw, Definition def, ExtensionRegistry extReg)
        throws WSDLException {

    }

    public ExtensibilityElement unmarshall(Class parentType, QName elementType, Element el,
                                           Definition def, ExtensionRegistry extReg)
        throws WSDLException {

        XMLFormatBinding xmlFormatBinding = (XMLFormatBinding)extReg.createExtension(parentType,
                                                                                     elementType);
        xmlFormatBinding.setElement(el);
        xmlFormatBinding.setElementType(elementType);
        xmlFormatBinding.setDocumentBaseURI(def.getDocumentBaseURI());
        return xmlFormatBinding;
    }

}
