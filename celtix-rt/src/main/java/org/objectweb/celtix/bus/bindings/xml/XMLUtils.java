package org.objectweb.celtix.bus.bindings.xml;

import java.io.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
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
    private DocumentBuilder parser;
    private final TransformerFactory transformerFactory;
    private String omitXmlDecl = "no";
    private String charset = "utf-8";
    private int indent = -1;
    
    public XMLUtils() {
        parserFactory = DocumentBuilderFactory.newInstance();
        parserFactory.setNamespaceAware(true);
        
        transformerFactory = TransformerFactory.newInstance();
    }

    public Transformer newTransformer() throws XMLBindingException {
        try {
            return transformerFactory.newTransformer();
        } catch (TransformerConfigurationException tex) {
            throw new XMLBindingException("Unable to create a JAXP transformer", tex);
        }
    }

    private DocumentBuilder getParser() {
        if (parser == null) {
            try {
                parser = parserFactory.newDocumentBuilder();
            } catch (javax.xml.parsers.ParserConfigurationException e) {
                LOG.log(Level.SEVERE, "NEW_DOCUMENT_BUILDER_EXCEPTION_MSG");
            }
        }
        return parser;
    }
    
    public Document parse(InputStream in) throws SAXException, IOException {
        if (in == null && LOG.isLoggable(Level.FINE)) {
            LOG.fine("XMLUtils trying to parse a null inputstream");
        }
        return getParser().parse(in);
    }

    public Document parse(String in) throws SAXException, IOException {
        return parse(in.getBytes());
    }

    public Document parse(byte[] in) throws SAXException, IOException {
        if (in == null && LOG.isLoggable(Level.FINE)) {
            LOG.fine("XMLUtils trying to parse a null bytes");
        }
        return getParser().parse(new ByteArrayInputStream(in));
    }

    public Document newDocument() {
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
    
    public void writeTo(Node node, OutputStream os) throws XMLBindingException {
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
            throw new XMLBindingException("xml binding exception:", e);
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

    public QName getNamespace(Map namespaces, String str) {
        String prefix = null;
        String localName = null;
        
        StringTokenizer tokenizer = new StringTokenizer(str, ":");
        if (tokenizer.countTokens() == 2) {
            prefix = tokenizer.nextToken();
            localName = tokenizer.nextToken();
        } else if (tokenizer.countTokens() == 1) {
            localName = tokenizer.nextToken();
        }

        String namespceURI = "";
        if (prefix != null) {
            namespceURI = (String) namespaces.get(prefix);
        }
        return new QName(namespceURI, localName);
    }
}
