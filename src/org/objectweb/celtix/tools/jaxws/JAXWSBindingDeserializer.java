package org.objectweb.celtix.tools.jaxws;

import java.io.*;
import java.util.*;

import javax.wsdl.Definition;
import javax.wsdl.WSDLException;
import javax.wsdl.extensions.ExtensibilityElement;
import javax.wsdl.extensions.ExtensionDeserializer;
import javax.wsdl.extensions.ExtensionRegistry;
import javax.xml.namespace.QName;
import org.w3c.dom.*;

public class JAXWSBindingDeserializer implements ExtensionDeserializer, Serializable {

    public ExtensibilityElement unmarshall(Class parentType,
                                           QName elementType,
                                           Element el,
                                           Definition def,
                                           ExtensionRegistry extReg) throws WSDLException {
        JAXWSBinding jaxwsBinding = (JAXWSBinding) extReg.createExtension(parentType, elementType);
    
        jaxwsBinding.setElementType(elementType);
        jaxwsBinding.setElement(el);
        jaxwsBinding.setDocumentBaseURI(def.getDocumentBaseURI());

        NodeList children = el.getChildNodes();
        if (children != null) {
            for (int i = 0; i < children.getLength(); i++) {
                Node child =  children.item(i);
                if (isAsyncElement(child)) {
                    jaxwsBinding.setEnableAsyncMapping(isAsync(child));
                }
            }
        }
        return jaxwsBinding;
    }

    private Boolean isAsyncElement(Node node) {
        return "enableAsyncMapping".equals(node.getNodeName());
    }

    private Boolean isAsync(Node node) {
        return Boolean.valueOf(node.getTextContent());
    }
}
