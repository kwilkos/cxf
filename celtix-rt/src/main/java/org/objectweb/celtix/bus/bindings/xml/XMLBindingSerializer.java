package org.objectweb.celtix.bus.bindings.xml;

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

public final class XMLBindingSerializer implements ExtensionSerializer,
                                                   ExtensionDeserializer,
                                                   Serializable {
    static final String NS_XML_FORMAT = "http://celtix.objectweb.org/bindings/xmlformat";
    static final String XML_ROOT_NODE = "rootNode";
    
    public void marshall(Class parentType,
                         QName elementType,
                         ExtensibilityElement extension,
                         PrintWriter pw,
                         Definition def,
                         ExtensionRegistry extReg) throws WSDLException {
        // TODO
    }

    public ExtensibilityElement unmarshall(Class parentType,
                                           QName elementType,
                                           Element el,
                                           Definition def,
                                           ExtensionRegistry extReg) throws WSDLException {
        XMLBinding xmlBinding = (XMLBinding) extReg.createExtension(parentType, elementType);
        xmlBinding.setElementType(elementType);
        xmlBinding.setElement(el);
        xmlBinding.setDocumentBaseURI(def.getDocumentBaseURI());

        XMLUtils xmlUtils = new XMLUtils();
        Attr rootNode = xmlUtils.getAttribute(el, XML_ROOT_NODE);
        String rootNodeValue = rootNode.getValue();
        if (rootNodeValue != null) {
            xmlBinding.setRootNode(xmlUtils.getNamespace(def.getNamespaces(), rootNodeValue));
        }

        return xmlBinding;
    }
}
