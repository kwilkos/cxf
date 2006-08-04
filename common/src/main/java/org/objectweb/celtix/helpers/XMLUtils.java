package org.objectweb.celtix.helpers;

import java.io.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.wsdl.Definition;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.*;
import org.xml.sax.SAXException;

import org.objectweb.celtix.common.logging.LogUtils;


public class XMLUtils {
    
    private static final Logger LOG = LogUtils.getL7dLogger(XMLUtils.class);
    private final DocumentBuilderFactory parserFactory;
    private final TransformerFactory transformerFactory;
    private String omitXmlDecl = "no";
    private String charset = "utf-8";
    private int indent = -1;
    
    public XMLUtils() {
        parserFactory = DocumentBuilderFactory.newInstance();
        parserFactory.setNamespaceAware(true);
        
        transformerFactory = TransformerFactory.newInstance();
    }

    private Transformer newTransformer() throws TransformerConfigurationException {
        return transformerFactory.newTransformer();
    }

    private DocumentBuilder getParser() throws ParserConfigurationException {
        return parserFactory.newDocumentBuilder();
    }
    
    public Document parse(InputStream in) 
        throws ParserConfigurationException, SAXException, IOException {
        if (in == null && LOG.isLoggable(Level.FINE)) {
            LOG.fine("XMLUtils trying to parse a null inputstream");
        }
        return getParser().parse(in);
    }

    public Document parse(String in) 
        throws ParserConfigurationException, SAXException, IOException {
        return parse(in.getBytes());
    }

    public Document parse(byte[] in) 
        throws ParserConfigurationException, SAXException, IOException {
        if (in == null && LOG.isLoggable(Level.FINE)) {
            LOG.fine("XMLUtils trying to parse a null bytes");
        }
        return getParser().parse(new ByteArrayInputStream(in));
    }

    public Document newDocument() throws ParserConfigurationException {
        return getParser().newDocument();
    }

    public void setOmitXmlDecl(String value) {
        this.omitXmlDecl = value;        
    }
    
    public void setCharsetEncoding(String value) {
        this.charset = value;
    }

    public void setIndention(int i) {
        this.indent = i;
    }

    private boolean indent() {
        return this.indent != -1;
    }
    
    public void writeTo(Node node, OutputStream os) {
        try {
            Transformer it = newTransformer();
            
            it.setOutputProperty(OutputKeys.METHOD, "xml");
            if (indent()) {
                it.setOutputProperty(OutputKeys.INDENT, "yes");
                it.setOutputProperty("{http://xml.apache.org/xslt}indent-amount",
                                     Integer.toString(this.indent));
            }
            it.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, omitXmlDecl);
            it.setOutputProperty(OutputKeys.ENCODING, charset);
            it.transform(new DOMSource(node), new StreamResult(os));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public String toString(Node node) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        writeTo(node, out);
        return out.toString();
    }

    public void printDOM(Node node) {
        printDOM("", node);
    }

    public void printDOM(String words, Node node) {
        System.out.println(words);
        System.out.println(toString(node));
    }

    public Attr getAttribute(Element el, String attrName) {
        return el.getAttributeNode(attrName);
    }

    public void replaceAttribute(Element element, String attr, String value) {
        if (element.hasAttribute(attr)) {
            element.removeAttribute(attr);
        }
        element.setAttribute(attr, value);
    }

    public boolean hasAttribute(Element element, String value) {
        NamedNodeMap attributes = element.getAttributes();
        for (int i = 0; i < attributes.getLength(); i++) {
            Node node = attributes.item(i);
            if (value.equals(node.getNodeValue())) {
                return true;
            }
        }
        return false;
    }

    public static void printAttributes(Element element) {
        NamedNodeMap attributes = element.getAttributes();
        for (int i = 0; i < attributes.getLength(); i++) {
            Node node = attributes.item(i);
            System.err.println("## prefix=" + node.getPrefix() + " localname:"
                               + node.getLocalName() + " value=" + node.getNodeValue());
        }
    }

    public QName getNamespace(Map<String, String> namespaces, String str, String defaultNamespace) {
        String prefix = null;
        String localName = null;
        
        StringTokenizer tokenizer = new StringTokenizer(str, ":");
        if (tokenizer.countTokens() == 2) {
            prefix = tokenizer.nextToken();
            localName = tokenizer.nextToken();
        } else if (tokenizer.countTokens() == 1) {
            localName = tokenizer.nextToken();
        }

        String namespceURI = defaultNamespace;
        if (prefix != null) {
            namespceURI = (String)namespaces.get(prefix);
        }
        return new QName(namespceURI, localName);
    }

    public void generateXMLFile(Element element, Writer writer) {
        try {
            Transformer it = newTransformer();
            
            it.setOutputProperty(OutputKeys.METHOD, "xml");
            it.setOutputProperty(OutputKeys.INDENT, "yes");
            it.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
            it.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            it.transform(new DOMSource(element), new StreamResult(writer));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Element createElementNS(Node node, QName name) {
        return createElementNS(node.getOwnerDocument(), name.getNamespaceURI(), name.getLocalPart());
    }

    public Element createElementNS(Document root, QName name) {
        return createElementNS(root, name.getNamespaceURI(), name.getLocalPart());
    }
    
    public Element createElementNS(Document root, String namespaceURI, String qualifiedName) {
        return root.createElementNS(namespaceURI, qualifiedName);
    }

    public Text createTextNode(Document root, String data) {
        return root.createTextNode(data);
    }

    public Text createTextNode(Node node, String data) {
        return createTextNode(node.getOwnerDocument(), data);
    }

    public void removeContents(Node node) {
        NodeList list = node.getChildNodes();
        for (int i = 0; i < list.getLength(); i++) {
            Node entry = list.item(i);
            node.removeChild(entry);
        }
    }
    
    public String writeQName(Definition def, QName qname) {
        return def.getPrefix(qname.getNamespaceURI()) + ":" + qname.getLocalPart();
    }
}
