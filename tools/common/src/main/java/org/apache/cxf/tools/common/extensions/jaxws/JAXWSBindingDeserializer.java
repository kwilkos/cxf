package org.apache.cxf.tools.common.extensions.jaxws;

import java.io.*;

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
        JAXWSBinding jaxwsBinding = (JAXWSBinding)extReg.createExtension(parentType, elementType);

        jaxwsBinding.setElementType(elementType);
        jaxwsBinding.setElement(el);
        jaxwsBinding.setDocumentBaseURI(def.getDocumentBaseURI());

        JAXWSBindingParser parser = new JAXWSBindingParser();
        parser.parseElement(jaxwsBinding, el);
        
        return jaxwsBinding;
    }
}
