package org.apache.cxf.tools.common.extensions.jaxws;

import javax.wsdl.Definition;
import javax.wsdl.WSDLException;
import javax.wsdl.extensions.ExtensionRegistry;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.apache.cxf.tools.common.ToolConstants;

public class JAXWSBindingParser {

    public JAXWSBinding parse(BindingsNode bindingsNode, Definition def) throws WSDLException {
        return parse(bindingsNode.getParentType(), bindingsNode.getElement(), def);
    }
    
    public JAXWSBinding parse(Class parentType, Element element, Definition def) throws WSDLException {
        ExtensionRegistry extReg = def.getExtensionRegistry();
        JAXWSBinding jaxwsBinding = (JAXWSBinding)extReg.createExtension(parentType,
                                                                         ToolConstants.JAXWS_BINDINGS);
        
        jaxwsBinding.setElementType(ToolConstants.JAXWS_BINDINGS);
        jaxwsBinding.setElement(element);
        jaxwsBinding.setDocumentBaseURI(def.getDocumentBaseURI());

        parseElement(jaxwsBinding, element);
        return jaxwsBinding;
    }

    public void parseElement(JAXWSBinding jaxwsBinding, Element element) {
        NodeList children = element.getChildNodes();
        if (children != null) {
            for (int i = 0; i < children.getLength(); i++) {
                Node child =  children.item(i);
                if (isAsyncElement(child)) {
                    jaxwsBinding.setAsyncMapping(true);
                    jaxwsBinding.setEnableAsyncMapping(isAsync(child));
                }
                if (isMIMEElement(child)) {
                    jaxwsBinding.setSetMimeEnable(true);
                    jaxwsBinding.setEnableMime(isMIMEEnabled(child));
                }                
            }
        }
    }
    
    private Boolean isAsyncElement(Node node) {
        return "enableAsyncMapping".equals(node.getNodeName());
    }

    private Boolean isAsync(Node node) {
        return Boolean.valueOf(node.getTextContent());
    }
    
    private Boolean isMIMEElement(Node node) {
        return "enableMIMEContent".equals(node.getNodeName());
    }

    private Boolean isMIMEEnabled(Node node) {
        return Boolean.valueOf(node.getTextContent());
    }
}
