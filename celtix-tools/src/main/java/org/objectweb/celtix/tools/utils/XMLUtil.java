package org.objectweb.celtix.tools.utils;

import java.io.*;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.*;
import org.objectweb.celtix.tools.common.ToolConstants;
import org.objectweb.celtix.tools.common.toolspec.ToolException;

public final class XMLUtil {

    static final TransformerFactory TRANSFORMER_FACTORY = TransformerFactory.newInstance();

    private XMLUtil() {
    }

    public static Transformer newTransformer() throws ToolException {
        try {
            return TRANSFORMER_FACTORY.newTransformer();
        } catch (TransformerConfigurationException tex) {
            throw new ToolException("Unable to create a JAXP transformer", tex);
        }
    }

    public static String getAttribute(Element el, String attrName) {
        String sRet = null;
        Attr   attr = el.getAttributeNode(attrName);
        if (attr != null) {
            sRet = attr.getValue();
        }
        return sRet;
    }

    public static void replaceAttribute(Element element, String attr, String value) {
        if (element.hasAttribute(attr)) {
            element.removeAttribute(attr);
        }
        element.setAttribute(attr, value);
    }

    public static Node innerJaxbBinding(Element schema) {
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

        if (!hasAttribute(schema, ToolConstants.NS_JAXB_BINDINGS)) {
            schema.setAttributeNS(ToolConstants.NS_JAXB_BINDINGS, "version", "2.0");
        }

        Node schemaBindings = innerJaxbBinding(schema);
        
        Element packagename = doc.createElementNS(ToolConstants.NS_JAXB_BINDINGS, "package");
        packagename.setAttribute("name", packagevalue);
        
        schemaBindings.appendChild(packagename);

        return schemaBindings.getParentNode().getParentNode();
    }

    private static boolean hasAttribute(Element element, String value) {
        NamedNodeMap attributes = element.getAttributes();
        for (int i = 0; i < attributes.getLength(); i++) {
            Node node = attributes.item(i);
            if (value.equals(node.getNodeValue())) {
                return true;
            }
        }
        return false;
    }

    public static void generateXMLFile(Element element, Writer writer) throws ToolException {
        try {
            Transformer it = newTransformer();
            
            it.setOutputProperty(OutputKeys.METHOD, "xml");
            it.setOutputProperty(OutputKeys.INDENT, "yes");
            it.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
            it.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            it.transform(new DOMSource(element), new StreamResult(writer));
        } catch (Exception e) {
            throw new ToolException("generator.nestedGeneratorError", e);
        }
    }

    public static Writer getWriter(String packageName, String filename, String ext) throws ToolException {
        FileWriterUtil fw;
        Writer writer;
        
        fw = new FileWriterUtil("c:/testcase");
        try {
            writer = fw.getWriter(packageName, filename + ext);
        } catch (IOException ioe) {
            throw new ToolException("Failed to write " + packageName + "." + filename + ext, ioe);
        }
        return writer;
    }

    public static void printAttributes(Element element) {
        NamedNodeMap attributes = element.getAttributes();
        for (int i = 0; i < attributes.getLength(); i++) {
            Node node = attributes.item(i);
            System.err.println("## prefix=" + node.getPrefix() + " localname:"
                               + node.getLocalName() + " value=" + node.getNodeValue());
        }
    }
}
