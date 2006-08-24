package org.apache.cxf.tools.util;

import org.w3c.dom.*;
import org.apache.cxf.helpers.XMLUtils;
import org.apache.cxf.tools.common.ToolConstants;

public final class JAXBUtils {
    private JAXBUtils() {
    }
    
    private static Node innerJaxbBinding(Element schema) {
        String schemaNamespace = schema.getNamespaceURI();
        Document doc = schema.getOwnerDocument();
        
        Element annotation = doc.createElementNS(schemaNamespace, "annotation");
        Element appinfo  = doc.createElementNS(schemaNamespace, "appinfo");
        annotation.appendChild(appinfo);
        Element jaxbBindings = doc.createElementNS(ToolConstants.NS_JAXB_BINDINGS, "schemaBindings");
        appinfo.appendChild(jaxbBindings);
        return jaxbBindings;
    }

    public static Node innerJaxbPackageBinding(Element schema, String packagevalue) {
        Document doc = schema.getOwnerDocument();
        XMLUtils xmlUtils = new XMLUtils();
        if (!xmlUtils.hasAttribute(schema, ToolConstants.NS_JAXB_BINDINGS)) {
            schema.setAttributeNS(ToolConstants.NS_JAXB_BINDINGS, "version", "2.0");
        }

        Node schemaBindings = innerJaxbBinding(schema);
        
        Element packagename = doc.createElementNS(ToolConstants.NS_JAXB_BINDINGS, "package");
        packagename.setAttribute("name", packagevalue);
        
        schemaBindings.appendChild(packagename);

        return schemaBindings.getParentNode().getParentNode();
    }
}
