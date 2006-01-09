package org.objectweb.celtix.tools.jaxws;

import java.io.*;
import java.net.*;
import java.util.*;

import javax.wsdl.Definition;
import javax.wsdl.Operation;
import javax.wsdl.PortType;
import javax.wsdl.WSDLException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import org.objectweb.celtix.tools.common.ProcessorEnvironment;
import org.objectweb.celtix.tools.common.ToolConstants;
import org.objectweb.celtix.tools.common.ToolException;
import org.objectweb.celtix.tools.utils.ProcessorUtil;
import org.objectweb.celtix.tools.utils.StAXUtil;
import org.objectweb.celtix.tools.utils.StringUtils;

public final class CustomizationParser {
    
    private static CustomizationParser parser;
    private ProcessorEnvironment env;
    private final Set<Element> jaxwsBindings = new HashSet<Element>();
    private Definition definition;
    private final Map<BindingsNode, JAXWSBinding>definitionExtensions;
    private final Map<BindingsNode, JAXWSBinding>portTypeExtensions;
    private final Map<BindingsNode, JAXWSBinding>operationExtensions;

    private CustomizationParser() {
        definitionExtensions = new HashMap<BindingsNode, JAXWSBinding>();
        portTypeExtensions = new HashMap<BindingsNode, JAXWSBinding>();
        operationExtensions = new HashMap<BindingsNode, JAXWSBinding>();
    }
    
    public static CustomizationParser getInstance() {
        if (parser == null) {
            parser = new CustomizationParser();
        } 
        return parser;
    }

    public void clean() {
        jaxwsBindings.clear();
        definitionExtensions.clear();
        portTypeExtensions.clear();
        operationExtensions.clear();
    }

    public void print() {
        System.err.println("## size of def:" + definitionExtensions.size());
    }

    public JAXWSBinding getDefinitionExtension() {
        if (definitionExtensions.size() > 0) {
            return definitionExtensions.values().iterator().next();
        } else {
            return null;
        }
    }

    public JAXWSBinding getPortTypeExtension(String portTypeName) {
        Collection<BindingsNode> bindingNodes = portTypeExtensions.keySet();
        JAXWSBinding jaxwsBinding = null;
        for (BindingsNode bindingNode : bindingNodes) {
            if (portTypeName.equals(bindingNode.getNodeName())) {
                jaxwsBinding = portTypeExtensions.get(bindingNode);
                break;
            }
        }
        if (jaxwsBinding == null) {
            jaxwsBinding = getDefinitionExtension();
        }
        return jaxwsBinding;
    }

    public JAXWSBinding getPortTypeOperationExtension(String portTypeName, String operationName) {
        Collection<BindingsNode> bindingNodes = operationExtensions.keySet();
        JAXWSBinding jaxwsBinding = null;
        for (BindingsNode bindingNode : bindingNodes) {
            if (matchOperation(bindingNode.getXPathExpression(), portTypeName, operationName)) {
                jaxwsBinding = operationExtensions.get(bindingNode);
                break;
            }
        }
        if (jaxwsBinding == null) {
            jaxwsBinding = getPortTypeExtension(portTypeName);
        }

        return jaxwsBinding;
    }

    private boolean matchOperation(String xpathExpression, String portTypeName, String operationName) {
        String regex = ".*" + wrapper(portTypeName) + ".*" + wrapper(operationName) + ".*";
        return xpathExpression.matches(regex);
    }
    
    public void parse(ProcessorEnvironment pe, Definition def) {
        this.env = pe;
        this.definition = def;
        String[] bindingFiles;
        try {
            bindingFiles = (String[]) env.get(ToolConstants.CFG_BINDING);
        } catch (ClassCastException e) {
            bindingFiles = new String[1];
            bindingFiles[0] = (String) env.get(ToolConstants.CFG_BINDING);
        }
        
        for (int i = 0; i < bindingFiles.length; i++) {
            try {
                addBinding(bindingFiles[i]);
            } catch (XMLStreamException xse) {
                throw new ToolException("StAX parser error, check your JAX-WS binding file(s)", xse);
            }
        }
        
        for (Element jaxwsBinding : jaxwsBindings) {
            buildTargetNodeMap(jaxwsBinding, "/");
        }
    }

    private void buildTargetNodeMap(Element bindings, String expression) {
        if (bindings.getAttributeNode("wsdlLocation") != null) {
            expression = "/";
        }

        if (isJAXWSBindings(bindings) && bindings.getAttributeNode("node") != null) {
            expression = expression + "/" + bindings.getAttribute("node");
            try {
                evaluateBindingsNode(bindings, expression);
            } catch (WSDLException we) {
                throw new ToolException("Exception during parsing external jaxws binding file(s)", we);
            }
        }

        Element[] children = getChildElements(bindings, ToolConstants.NS_JAXWS_BINDINGS);
        for (int i = 0; i < children.length; i++) {
            buildTargetNodeMap(children[i], expression);
        }
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
                a.add((Element) item);
            }
        }
        return (Element[]) a.toArray(new Element[a.size()]);
    }

    private boolean isJAXWSBindings(Node bindings) {
        return ToolConstants.NS_JAXWS_BINDINGS.equals(bindings.getNamespaceURI())
            && "bindings".equals(bindings.getLocalName());
    }

    private void addBinding(String bindingFile) throws XMLStreamException {
        String bindingLocation = ProcessorUtil.absolutize(ProcessorUtil.getFileOrURLName(bindingFile));
        
        InputSource is = new InputSource(bindingLocation);
        XMLStreamReader reader = StAXUtil.createFreshXMLStreamReader(is);

        StAXUtil.toStartTag(reader);

        if (isValidJaxwsBindingFile(bindingFile, reader)) {
            Element root = parse(is);
            jaxwsBindings.add(root);
        }
    }

    private boolean isValidJaxwsBindingFile(String bindingLocation, XMLStreamReader reader) {
        try {
            if (ToolConstants.JAXWS_BINDINGS.equals(reader.getName())) {
                String wsdlURL = (String) env.get(ToolConstants.CFG_WSDLURL);
                wsdlURL = ProcessorUtil.absolutize(ProcessorUtil.getFileOrURLName(wsdlURL));
                String wsdlLocation = reader.getAttributeValue(null, "wsdlLocation");
                if (StringUtils.isFileExist(bindingLocation) && !StringUtils.isFileAbsolute(wsdlLocation)) {
                    String basedir = new File(bindingLocation).getParent();
                    wsdlLocation = new File(basedir, wsdlLocation).getAbsolutePath();
                }
                wsdlLocation = ProcessorUtil.absolutize(ProcessorUtil.getFileOrURLName(wsdlLocation));
                    
                if (!StringUtils.getURL(wsdlURL).equals(StringUtils.getURL(wsdlLocation))) {
                    throw new ToolException("External binding file ["
                                            + wsdlLocation
                                            + "]is not point to the specified wsdl url ["
                                            + wsdlURL + "]");
                }
            }
        } catch (MalformedURLException e) {
            throw new ToolException("Can not get wsdl location:", e);
        }
        return true;
    }
    
    private Element parse(InputSource source) {
        try {
            DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
            builderFactory.setNamespaceAware(true);
            builderFactory.setValidating(false);
            DocumentBuilder builder = builderFactory.newDocumentBuilder();
            builder.setErrorHandler(new ErrorHandler() {
                    public void error(SAXParseException e)
                        throws SAXParseException {
                        throw e;
                    }

                    public void fatalError(SAXParseException e)
                        throws SAXParseException {
                        throw e;
                    }

                    public void warning(SAXParseException err)
                        throws SAXParseException {
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

    private void evaluateBindingsNode(Element bindings, String expression) throws WSDLException {
        BindingsNode bindingsNode  = evaluateXPathNode(expression);
        if (bindingsNode == null) {
            return;
        }
        bindingsNode.setElement(bindings);
        
        JAXWSBindingParser bindingsParser = new JAXWSBindingParser();
        JAXWSBinding jaxwsBinding = bindingsParser.parse(bindingsNode, this.definition);

        if (bindingsNode.getParentType().equals(Definition.class)) {
            definitionExtensions.put(bindingsNode, jaxwsBinding);
        }
        if (bindingsNode.getParentType().equals(PortType.class)) {
            portTypeExtensions.put(bindingsNode, jaxwsBinding);
        }
        if (bindingsNode.getParentType().equals(Operation.class)) {
            operationExtensions.put(bindingsNode, jaxwsBinding);
        }
    }

    private BindingsNode evaluateXPathNode(String expression) {
        String[] parts = expression.split("/");
        if (parts == null) {
            return null;
        }

        BindingsNode node = new BindingsNode();
        node.setXPathExpression(expression);
        for (int i = parts.length - 1; i > 0; i--) {
            if (parts[i].startsWith("wsdl:definitions")) {
                node.setParentType(Definition.class);
                break;
            }
            if (parts[i].startsWith("wsdl:portType")) {
                node.setParentType(PortType.class);
                node.setNodeName(getNodeName(parts[i]));
                break;
            }
            if (parts[i].startsWith("wsdl:operation")) {
                node.setParentType(Operation.class);
                node.setNodeName(getNodeName(parts[i]));
                break;
            }
        }
        return node;
    }

    private String getNodeName(String expression) {
        return StringUtils.extract(expression, "[@name='", "']");
    }

    private String wrapper(String nodeName) {
        return StringUtils.wrapper(nodeName, "[@name='", "']");
    }
}
