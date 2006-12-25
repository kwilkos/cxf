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
package org.apache.cxf.tools.wsdlto.frontend.jaxws.customiztion;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import javax.wsdl.Definition;
import javax.wsdl.Operation;
import javax.wsdl.PortType;
import javax.wsdl.WSDLException;
import javax.wsdl.extensions.ExtensibilityElement;
import javax.wsdl.extensions.schema.Schema;
import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import org.apache.cxf.common.i18n.Message;
import org.apache.cxf.common.logging.LogUtils;
import org.apache.cxf.common.util.StringUtils;
import org.apache.cxf.helpers.DOMUtils;
import org.apache.cxf.tools.common.ToolConstants;
import org.apache.cxf.tools.common.ToolContext;
import org.apache.cxf.tools.common.ToolException;
import org.apache.cxf.tools.util.ProcessorUtil;
import org.apache.cxf.tools.util.StAXUtil;

public final class CustomizationParser {
    // For WSDL1.1
    private static final Logger LOG = LogUtils.getL7dLogger(CustomizationParser.class);
    private static final XPathFactory XPF = XPathFactory.newInstance();
    private static CustomizationParser parser;

    private final XPath xpath = XPF.newXPath();

    private ToolContext env;
    private final Map<URI, Element> jaxwsBindings = new HashMap<URI, Element>();
    private final Set<InputSource> jaxbBindings = new HashSet<InputSource>();
    private final Map<String, JAXWSBinding> definitionBindingMap = new HashMap<String, JAXWSBinding>();
    private final Map<QName, JAXWSBinding> portTypeBindingMap = new HashMap<QName, JAXWSBinding>();
    private final Map<QName, JAXWSBinding> opertaionBindingMap = new HashMap<QName, JAXWSBinding>();
    private final Map<QName, JAXWSBinding> partBindingMap = new HashMap<QName, JAXWSBinding>();

    private Definition definition;

    private Element handlerChains;
    private Element wsdlNode;;
    
    private Element customizedWSDLNode;

    private CustomizationParser() {

    }

    public static CustomizationParser getInstance() {
        if (parser == null) {
            parser = new CustomizationParser();
            parser.clean();
        }
        return parser;
    }

    public void clean() {
        definitionBindingMap.clear();
        portTypeBindingMap.clear();
        opertaionBindingMap.clear();
        partBindingMap.clear();
        jaxwsBindings.clear();
        jaxbBindings.clear();
    }

    public Element getHandlerChains() {
        return this.handlerChains;
    }

    public void parse(ToolContext pe) {
        this.env = pe;
        String[] bindingFiles;
        try {
            bindingFiles = (String[])env.get(ToolConstants.CFG_BINDING);
        } catch (ClassCastException e) {
            bindingFiles = new String[1];
            bindingFiles[0] = (String)env.get(ToolConstants.CFG_BINDING);
        }

        for (int i = 0; i < bindingFiles.length; i++) {
            try {
                addBinding(bindingFiles[i]);
            } catch (XMLStreamException xse) {
                Message msg = new Message("STAX_PASER_ERROR", LOG);
                throw new ToolException(msg, xse);
            }
        }

        for (URI wsdlUri : jaxwsBindings.keySet()) {
            Element element = jaxwsBindings.get(wsdlUri);
            definition = getWSDlDefinition(wsdlUri.toString());
            URI uri = null;
            try {
                uri = new URI(definition.getDocumentBaseURI());
            } catch (URISyntaxException e1) {
                //ignore
            }
            wsdlNode = getTargetNode(uri);
            buildTargetNodeMap(element, "");
        }

        buildHandlerChains();
    }

    public Element getTargetNode(URI wsdlLoc) {

        Document doc = null;
        try {
            File file = new File(wsdlLoc);
            java.io.InputStream ins = new java.io.FileInputStream(file);
            doc = DOMUtils.readXml(ins);
            
        } catch (SAXException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ParserConfigurationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        if (doc != null) {
            return doc.getDocumentElement();
        }
        return null;
    }

    private void buildHandlerChains() {
        /*
         * for (Element jaxwsBinding : jaxwsBindings) { NodeList nl =
         * jaxwsBinding.getElementsByTagNameNS(ToolConstants.HANDLER_CHAINS_URI,
         * ToolConstants.HANDLER_CHAINS); if (nl.getLength() == 0) { continue; } //
         * take the first one, anyway its 1 handler-config per customization
         * this.handlerChains = (Element)nl.item(0); return; }
         */
    }

    private void buildTargetNodeMap(Element bindings, String expression) {
        if (bindings.getAttributeNode("wsdlLocation") != null) {
            expression = "/";
        }

        JAXWSBindingParser bindingsParser = new JAXWSBindingParser(definition.getExtensionRegistry());

        if (isGlobaleBindings(bindings)) {

            try {
                JAXWSBinding jaxwsBinding = bindingsParser.parse(Definition.class, bindings, definition
                    .getTargetNamespace());
                if (definitionBindingMap.containsKey(definition.getTargetNamespace())) {
                    JAXWSBinding binding = definitionBindingMap.get(definition.getTargetNamespace());
                    mergeJaxwsBinding(binding, jaxwsBinding);
                } else {
                    definitionBindingMap.put(definition.getTargetNamespace(), jaxwsBinding);
                }

            } catch (WSDLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        }

        if (isJAXWSParameter(bindings) && bindings.getAttribute("part") != null) {
            String partExpression = "//" + bindings.getAttribute("part");
            try {
                Node node = evaluateBindingsNode(wsdlNode, partExpression);

                if ("part".equals(node.getLocalName())) {
                    JAXWSBinding jaxwsBinding = bindingsParser.parse(PortType.class, bindings,
                                                                     getTargetNamspace(node));
                    addJAXWSBindingMap(partBindingMap, node, jaxwsBinding);

                }
            } catch (WSDLException we) {
                Message msg = new Message("PARSE_BINDINGFILE_EXCEPTION", LOG);
                throw new ToolException(msg, we);
            }
        }

        if (isJAXWSBindings(bindings) && bindings.getAttributeNode("node") != null) {
            expression = expression + "/" + bindings.getAttribute("node");
            try {
                Node node = null;
                boolean nestedJaxb = nestedJaxbBinding(bindings);               
                if (nestedJaxb) {
                    customizedWSDLNode = 
                        customizedWSDLNode == null ? getNodeInDefinition(definition) : customizedWSDLNode;
                    node = evaluateBindingsNode(customizedWSDLNode, expression);
                } else {
                    node = evaluateBindingsNode(wsdlNode, expression);
                }
                if (node != null && "portType".equals(node.getLocalName()) && !nestedJaxb) {
                    JAXWSBinding jaxwsBinding = bindingsParser.parse(PortType.class, bindings,
                                                                     getTargetNamspace(node));
                    addJAXWSBindingMap(portTypeBindingMap, node, jaxwsBinding);
                }

                if (node != null && "operation".equals(node.getLocalName()) && !nestedJaxb) {
                    JAXWSBinding jaxwsBinding = bindingsParser.parse(Operation.class, bindings,
                                                                     getTargetNamspace(node));
                    addJAXWSBindingMap(opertaionBindingMap, node, jaxwsBinding);
                }

                if (node != null && nestedJaxb) {
                    // append xmlns:jaxb and jaxb:version attribute for schema
                    Node schemaNode = getSchemaNode(node);
                    Element schemaElement = (Element)schemaNode;
                    
                    String jaxbPrefix = schemaElement.lookupPrefix(ToolConstants.NS_JAXB_BINDINGS);
                    if (jaxbPrefix == null) {
                        schemaElement.setAttribute("xmlns:jaxb", ToolConstants.NS_JAXB_BINDINGS);
                        schemaElement.setAttribute("jaxb:version", "2.0");
                    }

                    // append jaxb appinfo for value node
                    Element annoElement = node.getOwnerDocument().createElementNS(ToolConstants.SCHEMA_URI,
                                                                                  "annotation");
                    Element appinfoEle = node.getOwnerDocument().createElementNS(ToolConstants.SCHEMA_URI,
                                                                                 "appinfo");

                    annoElement.appendChild(appinfoEle);

                    NodeList jxbBinds = getJaxbBindingNode((Element)bindings);

                    for (int l = 0; l < jxbBinds.getLength(); l++) {
                        Node jaxbBindingNode = jxbBinds.item(l);
                        Node cloneNode = ProcessorUtil.cloneNode(node.getOwnerDocument(), jaxbBindingNode,
                                                                 true);
                        appinfoEle.appendChild(cloneNode);
                    }

                    if (node.getChildNodes().getLength() > 0) {
                        node.insertBefore(annoElement, node.getChildNodes().item(0));
                    } else {
                        node.appendChild(annoElement);
                    }
                }

            } catch (WSDLException we) {
                Message msg = new Message("PARSE_BINDINGFILE_EXCEPTION", LOG);
                throw new ToolException(msg, we);
            }
        }

        Element[] children = getChildElements(bindings, ToolConstants.NS_JAXWS_BINDINGS);
        for (int i = 0; i < children.length; i++) {
            buildTargetNodeMap(children[i], expression);
        }
    }
    @SuppressWarnings("unchecked")
    private Element getNodeInDefinition(Definition def) {
        List<ExtensibilityElement> extList = def.getTypes().getExtensibilityElements();
        for (ExtensibilityElement ele : extList) {
            if (ele instanceof Schema) {
                Schema schema = (Schema)ele;
                Element element = schema.getElement();
                Element target = element.getOwnerDocument().getDocumentElement();
                return target;
            }
        }
        return null;
    }
    
    private Node getSchemaNode(Node node) {
        if (!"schema".equals(node.getLocalName())) {
            while (node.getParentNode() != null) {
                node = node.getParentNode();

                if ("schema".equals(node.getLocalName())) {
                    return node;
                }
            }
            return null;
        }
        return node;
    }

    @SuppressWarnings("unchecked")
    public Definition getWSDlDefinition(String baseUrl) {
        Definition def = (Definition)env.get(ToolConstants.WSDL_DEFINITION);
        if (def.getDocumentBaseURI().equals(baseUrl)) {
            return def;
        }
        List<Definition> defs = (List<Definition>)env.get(ToolConstants.IMPORTED_DEFINITION);
        for (Definition arg : defs) {
            if (arg.getDocumentBaseURI().equals(baseUrl)) {
                return arg;
            }
        }
        return null;
    }

    private boolean isGlobaleBindings(Element binding) {

        boolean globleNode = binding.getNamespaceURI().equals(ToolConstants.NS_JAXWS_BINDINGS)
                             && binding.getLocalName().equals("package")
                             || binding.getLocalName().equals("enableAsyncMapping")
                             || binding.getLocalName().equals("enableAdditionalSOAPHeaderMapping")
                             || binding.getLocalName().equals("enableWrapperStyle")
                             || binding.getLocalName().equals("enableMIMEContent");
        Node parentNode = binding.getParentNode();
        if (parentNode instanceof Element) {
            Element ele = (Element)parentNode;
            if (ele.getAttributeNode("wsdlLocation") != null && globleNode) {
                return true;
            }

        }
        return false;

    }

    private boolean isJAXWSParameter(Element binding) {
        boolean parameterNode = ToolConstants.NS_JAXWS_BINDINGS.equals(binding.getNamespaceURI())
                                && "parameter".equals(binding.getLocalName());
        Node parentNode = binding.getParentNode();
        if (parentNode instanceof Element) {
            Element ele = (Element)parentNode;
            if (parameterNode && ele.getAttributeNode("node") != null
                && ele.getAttribute("node").indexOf("portType") > -1) {
                return true;
            }
        }
        return false;
    }

    private Element[] getChildElements(Element parent, String nsUri) {
        List<Element> a = new ArrayList<Element>();
        NodeList children = parent.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node item = children.item(i);
            if (!(item instanceof Element)) {
                continue;
            }
            if (nsUri.equals(item.getNamespaceURI())) {
                a.add((Element)item);
            }
        }
        return (Element[])a.toArray(new Element[a.size()]);
    }

    private void addBinding(String bindingFile) throws XMLStreamException {
        String bindingLocation = ProcessorUtil.absolutize(ProcessorUtil.getFileOrURLName(bindingFile));

        InputSource is = new InputSource(bindingLocation);
        XMLStreamReader reader = StAXUtil.createFreshXMLStreamReader(is);

        StAXUtil.toStartTag(reader);

        if (isValidJaxwsBindingFile(bindingFile, reader)) {
            Element root = parse(is);
            String wsdlLocation = root.getAttribute("wsdlLocation");
            URI wsdlURI = null;
            try {
                wsdlURI = new URI(wsdlLocation);
            } catch (URISyntaxException e) {
                Message msg = new Message("JAXWSBINDINGS_WSDLLOC_ERROR", LOG, new Object[] {wsdlLocation});
                throw new ToolException(msg);
            }

            if (!wsdlURI.isAbsolute()) {
                try {
                    URI baseURI = new URI(bindingLocation);
                    wsdlURI = baseURI.resolve(wsdlURI);
                } catch (URISyntaxException e) {
                    // ignore
                }

            }

            if (getWSDlDefinition(wsdlURI.toString()) != null) {
                jaxwsBindings.put(wsdlURI, root);
            } else {
                String wsdl = (String)env.get(ToolConstants.CFG_WSDLURL);
                Message msg = new Message("NOT_POINTTO_URL", LOG, new Object[] {bindingLocation, wsdl});
                throw new ToolException(msg);
            }

        } else if (isValidJaxbBindingFile(reader)) {
            jaxbBindings.add(is);
        } else {
            Message msg = new Message("UNKNOWN_BINDING_FILE", LOG, bindingFile);
            throw new ToolException(msg);
        }
    }

    private boolean isValidJaxbBindingFile(XMLStreamReader reader) {
        if (ToolConstants.JAXB_BINDINGS.equals(reader.getName())) {
            return true;
        }
        return false;
    }

    private boolean isValidJaxwsBindingFile(String bindingLocation, XMLStreamReader reader) {
        if (ToolConstants.JAXWS_BINDINGS.equals(reader.getName())) {
            String wsdlLocation = reader.getAttributeValue(null, "wsdlLocation");
            if (!StringUtils.isEmpty(wsdlLocation)) {
                return true;
            }
        }
        return false;

    }

    private Element parse(InputSource source) {
        try {
            DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
            builderFactory.setNamespaceAware(true);
            builderFactory.setValidating(false);
            DocumentBuilder builder = builderFactory.newDocumentBuilder();
            builder.setErrorHandler(new ErrorHandler() {
                public void error(SAXParseException e) throws SAXParseException {
                    throw e;
                }

                public void fatalError(SAXParseException e) throws SAXParseException {
                    throw e;
                }

                public void warning(SAXParseException err) throws SAXParseException {
                    // do nothing
                }
            });

            // builder.setEntityResolver(new NullEntityResolver());
            return builder.parse(source).getDocumentElement();
        } catch (ParserConfigurationException e) {
            throw new ToolException("parsing.parserConfigException", e);
        } catch (FactoryConfigurationError e) {
            throw new ToolException("parsing.factoryConfigException", e);
        } catch (SAXException e) {
            throw new ToolException("parsing.saxException", e);
        } catch (IOException e) {
            throw new ToolException("parsing.saxException", e);
        }
    }

    private Node evaluateBindingsNode(Node targetNode, String expression) throws WSDLException {
        Node node = evaluateXPathNode(targetNode, expression, new javax.xml.namespace.NamespaceContext() {
            public String getNamespaceURI(String prefix) {
                return definition.getNamespace(prefix);
            }

            public String getPrefix(String nsURI) {
                throw new UnsupportedOperationException();
            }

            public Iterator getPrefixes(String namespaceURI) {
                throw new UnsupportedOperationException();
            }
        });
        return node;
    }

    private Node evaluateXPathNode(Node target, String expression, NamespaceContext namespaceContext) {
        NodeList nlst;
        try {
            xpath.setNamespaceContext(namespaceContext);
            nlst = (NodeList)xpath.evaluate(expression, target, XPathConstants.NODESET);
        } catch (XPathExpressionException e) {
            e.printStackTrace();
            return null; // abort processing this <jaxb:bindings>
        }

        if (nlst.getLength() == 0) {
            // error("internalizer.XPathEvaluatesToNoTarget", new
            // Object[]{expression});
            return null; // abort
        }

        if (nlst.getLength() != 1) {
            // error("internalizer.XPathEvaulatesToTooManyTargets", new
            // Object[]{expression, nlst.getLength()});
            return null; // abort
        }

        Node rnode = nlst.item(0);
        if (!(rnode instanceof Element)) {
            // error("internalizer.XPathEvaluatesToNonElement", new
            // Object[]{expression});
            return null; // abort
        }
        return (Element)rnode;
    }

    private void mergeJaxwsBinding(JAXWSBinding old, JAXWSBinding present) {
        if (present.isEnableAsyncMapping()) {
            old.setEnableAsyncMapping(present.isEnableAsyncMapping());
        }
        if (present.isEnableMime()) {
            old.setEnableMime(present.isEnableMime());
        }
        if (present.isEnableWrapperStyle()) {
            old.setEnableWrapperStyle(present.isEnableWrapperStyle());
        }
        if (present.getMethodName() != null) {
            old.setMethodName(present.getMethodName());
        }
        if (present.getPackage() != null) {
            old.setPackage(present.getPackage());
        }
        if (present.getJaxwsPara() != null) {
            old.setJaxwsPara(present.getJaxwsPara());
        }
    }

    private void addJAXWSBindingMap(Map<QName, JAXWSBinding> bindingMap, 
                                    Node node, JAXWSBinding jaxwsBinding) {
        if (bindingMap.containsKey(getQName(node))) {
            JAXWSBinding binding = bindingMap.get(getQName(node));
            mergeJaxwsBinding(binding, jaxwsBinding);
        } else {
            bindingMap.put(getQName(node), jaxwsBinding);
        }
    }

    private String getTargetNamspace(Node node) {
        Document doc = ((Element)node).getOwnerDocument();
        if (!StringUtils.isEmpty(doc.getDocumentElement().getAttribute("targetNamespace"))) {
            return doc.getDocumentElement().getAttribute("targetNamespace");
        }
        return node.getBaseURI();
    }

    private QName getQName(Node node) {
        String ns = getTargetNamspace(node);
        Element ele = (Element)node;
        return new QName(ns, ele.getAttribute("name"));
    }

    private boolean isJAXWSBindings(Node bindings) {
        return ToolConstants.NS_JAXWS_BINDINGS.equals(bindings.getNamespaceURI())
               && "bindings".equals(bindings.getLocalName());
    }

    private boolean nestedJaxbBinding(Element bindings) {
        NodeList nodeList = bindings.getElementsByTagNameNS(ToolConstants.NS_JAXB_BINDINGS, "bindings");
        if (nodeList.getLength() == 1) {
            return true;
        }
        return false;
    }

    private NodeList getJaxbBindingNode(Element bindings) {
        NodeList nodeList = bindings.getElementsByTagNameNS(ToolConstants.NS_JAXB_BINDINGS, "bindings");
        return nodeList.item(0).getChildNodes();       
    }


    public Map<String, JAXWSBinding> getDefinitionBindingMap() {
        return this.definitionBindingMap;
    }

    public Map<QName, JAXWSBinding> getPortTypeBindingMap() {
        return this.portTypeBindingMap;
    }

    public Map<QName, JAXWSBinding> getOperationBindingMap() {
        return this.opertaionBindingMap;
    }

    public Map<QName, JAXWSBinding> getPartBindingMap() {
        return this.partBindingMap;
    }
    
    public Element getCustomizedWSDLElement() {
        if (this.customizedWSDLNode == null) {
            customizedWSDLNode = this.wsdlNode;
        }
        return customizedWSDLNode;
        
    }
    
}
