package org.objectweb.celtix.tools.extensions.jaxws;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.logging.Logger;

import javax.wsdl.Binding;
import javax.wsdl.BindingOperation;
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

import org.objectweb.celtix.common.i18n.Message;
import org.objectweb.celtix.common.logging.LogUtils;
import org.objectweb.celtix.common.util.StringUtils;
import org.objectweb.celtix.tools.common.ProcessorEnvironment;
import org.objectweb.celtix.tools.common.ToolConstants;
import org.objectweb.celtix.tools.common.ToolException;
import org.objectweb.celtix.tools.utils.ProcessorUtil;
import org.objectweb.celtix.tools.utils.StAXUtil;

public final class CustomizationParser {
    private static final Logger LOG = LogUtils.getL7dLogger(CustomizationParser.class);
    private static CustomizationParser parser;
    private ProcessorEnvironment env;
    private final Set<Element> jaxwsBindings = new HashSet<Element>();
    private Definition definition;
    private final Map<BindingsNode, JAXWSBinding>definitionExtensions;
    private final Map<BindingsNode, JAXWSBinding>portTypeExtensions;
    private final Map<BindingsNode, JAXWSBinding>operationExtensions;
    private Element handlerChains;

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

    public Element getHandlerChains() {
        return this.handlerChains;
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
        
        for (Element jaxwsBinding : jaxwsBindings) {
            buildTargetNodeMap(jaxwsBinding, "/");
        }
        
        buildHandlerChains();
    }

    private void buildHandlerChains() {
        for (Element jaxwsBinding : jaxwsBindings) {
            NodeList nl = jaxwsBinding.getElementsByTagNameNS(ToolConstants.HANDLER_CHAINS_URI,
                                                              ToolConstants.HANDLER_CHAINS);
            if (nl.getLength() == 0) {
                continue;
            }
            //take the first one, anyway its 1 handler-config per customization
            this.handlerChains = (Element)nl.item(0);
            return;
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
                Message msg = new Message("PARSE_BININDINGFILE_EXCEPTION", LOG);
                throw new ToolException(msg, we);
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
                a.add((Element)item);
            }
        }
        return (Element[])a.toArray(new Element[a.size()]);
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
        } else if (isValidJaxbBindingFile(reader)) {
            env.addJaxbBindingFile(bindingFile, is);
        } else {
            Message msg = new Message("UNKONW_BINDING_FILE", LOG, bindingFile);
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
        try {
            if (ToolConstants.JAXWS_BINDINGS.equals(reader.getName())) {
                String wsdlURL = (String)env.get(ToolConstants.CFG_WSDLURL);
                wsdlURL = ProcessorUtil.absolutize(ProcessorUtil.getFileOrURLName(wsdlURL));
                String wsdlLocation = reader.getAttributeValue(null, "wsdlLocation");
                if (StringUtils.isFileExist(bindingLocation) && !StringUtils.isFileAbsolute(wsdlLocation)) {
                    String basedir = new File(bindingLocation).getParent();
                    wsdlLocation = new File(basedir, wsdlLocation).getAbsolutePath();
                }
                wsdlLocation = ProcessorUtil.absolutize(ProcessorUtil.getFileOrURLName(wsdlLocation));
                    
                if (!StringUtils.getURL(wsdlURL).equals(StringUtils.getURL(wsdlLocation))) {
                    Message msg = new Message("NOT_POINTTO_URL", LOG, new Object[]{wsdlLocation, wsdlURL});
                    throw new ToolException(msg);
                }
            } else {
                return false;
            }
        } catch (MalformedURLException e) {
            Message msg = new Message("CAN_NOT_GET_WSDL_LOCATION", LOG);
            throw new ToolException(msg, e);
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

        if (bindingsNode.getParentType().equals(BindingOperation.class)) {
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
            if (parts[i].startsWith("wsdl:binding")) {
                node.setParentType(Binding.class);
                node.setNodeName(getNodeName(parts[i]));
                break;
            }
            if (parts[i].startsWith("wsdl:portType")) {
                node.setParentType(PortType.class);
                node.setNodeName(getNodeName(parts[i]));
                break;
            }
            if (parts[i].startsWith("wsdl:operation")) {
                if (i > 1 && parts[i - 1].startsWith("wsdl:binding")) {
                    node.setParentType(BindingOperation.class);
                } else if (i > 1 && parts[i - 1].startsWith("wsdl:portType")) {
                    node.setParentType(Operation.class);
                }
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
