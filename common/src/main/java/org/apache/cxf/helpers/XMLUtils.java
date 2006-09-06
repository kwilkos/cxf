/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.cxf.helpers;

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
import org.w3c.dom.bootstrap.DOMImplementationRegistry;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSOutput;
import org.w3c.dom.ls.LSSerializer;

import org.xml.sax.SAXException;

import org.apache.cxf.common.logging.LogUtils;


public final class XMLUtils {
    
    private static final Logger LOG = LogUtils.getL7dLogger(XMLUtils.class);
    private static DocumentBuilderFactory parserFactory;
    private static TransformerFactory transformerFactory;
    private static String omitXmlDecl = "no";
    private static String charset = "utf-8";
    private static int indent = -1;
    
    static {
        parserFactory = DocumentBuilderFactory.newInstance();
        parserFactory.setNamespaceAware(true);
        
        transformerFactory = TransformerFactory.newInstance();
    }
    
    private XMLUtils() {
        
    }
    
    public static  Transformer newTransformer() throws TransformerConfigurationException {
        return transformerFactory.newTransformer();
    }

    public static DocumentBuilder getParser() throws ParserConfigurationException {
        return parserFactory.newDocumentBuilder();
    }
    
    public static Document parse(InputStream in) 
        throws ParserConfigurationException, SAXException, IOException {
        if (in == null && LOG.isLoggable(Level.FINE)) {
            LOG.fine("XMLUtils trying to parse a null inputstream");
        }
        return getParser().parse(in);
    }

    public static Document parse(String in) 
        throws ParserConfigurationException, SAXException, IOException {
        return parse(in.getBytes());
    }

    public static Document parse(byte[] in) 
        throws ParserConfigurationException, SAXException, IOException {
        if (in == null && LOG.isLoggable(Level.FINE)) {
            LOG.fine("XMLUtils trying to parse a null bytes");
        }
        return getParser().parse(new ByteArrayInputStream(in));
    }

    public static Document newDocument() throws ParserConfigurationException {
        return getParser().newDocument();
    }

    public static void setOmitXmlDecl(String value) {
        omitXmlDecl = value;        
    }
    
    public static void setCharsetEncoding(String value) {
        charset = value;
    }

    public static void setIndention(int i) {
        indent = i;
    }

    private static boolean indent() {
        return indent != -1;
    }
    
    public static void writeTo(Node node, OutputStream os) {
        try {
            Transformer it = newTransformer();
            
            it.setOutputProperty(OutputKeys.METHOD, "xml");
            if (indent()) {
                it.setOutputProperty(OutputKeys.INDENT, "yes");
                it.setOutputProperty("{http://xml.apache.org/xslt}indent-amount",
                                     Integer.toString(indent));
            }
            it.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, omitXmlDecl);
            it.setOutputProperty(OutputKeys.ENCODING, charset);
            it.transform(new DOMSource(node), new StreamResult(os));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public static String toString(Node node) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        writeTo(node, out);
        return out.toString();
    }

    public static void printDOM(Node node) {
        printDOM("", node);
    }

    public static void printDOM(String words, Node node) {
        System.out.println(words);
        System.out.println(toString(node));
    }

    public static Attr getAttribute(Element el, String attrName) {
        return el.getAttributeNode(attrName);
    }

    public static void replaceAttribute(Element element, String attr, String value) {
        if (element.hasAttribute(attr)) {
            element.removeAttribute(attr);
        }
        element.setAttribute(attr, value);
    }

    public static boolean hasAttribute(Element element, String value) {
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

    public static QName getNamespace(Map<String, String> namespaces, String str, String defaultNamespace) {
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

    public static void generateXMLFile(Element element, Writer writer) {
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

    public static Element createElementNS(Node node, QName name) {
        return createElementNS(node.getOwnerDocument(), name.getNamespaceURI(), name.getLocalPart());
    }

    public static Element createElementNS(Document root, QName name) {
        return createElementNS(root, name.getNamespaceURI(), name.getLocalPart());
    }
    
    public static Element createElementNS(Document root, String namespaceURI, String qualifiedName) {
        return root.createElementNS(namespaceURI, qualifiedName);
    }

    public static Text createTextNode(Document root, String data) {
        return root.createTextNode(data);
    }

    public static Text createTextNode(Node node, String data) {
        return createTextNode(node.getOwnerDocument(), data);
    }

    public static void removeContents(Node node) {
        NodeList list = node.getChildNodes();
        for (int i = 0; i < list.getLength(); i++) {
            Node entry = list.item(i);
            node.removeChild(entry);
        }
    }
    
    public static String writeQName(Definition def, QName qname) {
        return def.getPrefix(qname.getNamespaceURI()) + ":" + qname.getLocalPart();
    }

    public static InputStream getInputStream(Document doc) throws Exception {
        DOMImplementationRegistry registry = DOMImplementationRegistry.newInstance();
        DOMImplementationLS impl = (DOMImplementationLS)registry.getDOMImplementation("LS");
        if (impl == null) {
            System.setProperty(DOMImplementationRegistry.PROPERTY,
                "com.sun.org.apache.xerces.internal.dom.DOMImplementationSourceImpl");
            registry = DOMImplementationRegistry.newInstance();
            impl = (DOMImplementationLS)registry.getDOMImplementation("LS");
        }
        LSOutput output = impl.createLSOutput();
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        output.setByteStream(byteArrayOutputStream);
        LSSerializer writer = impl.createLSSerializer();
        writer.write(doc, output);
        byte[] buf = byteArrayOutputStream.toByteArray();
        return new ByteArrayInputStream(buf);
    }

    public static Element fetchElementByNameAttribute(Element parent, String targetName, String nameValue) {
        Element ret = null;
        NodeList nodeList = parent.getElementsByTagName(targetName);
        Node node = null;
        for (int i = 0; i < nodeList.getLength(); i++) {
            node = nodeList.item(i);
            if (node instanceof Element && ((Element)node).getAttribute("name").equals(nameValue)) {
                ret = (Element)node;
                break;
            }
        }
        return ret;
    }
}
