package org.objectweb.celtix.bus.handlers;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.ws.WebServiceException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.objectweb.celtix.bus.jaxws.configuration.types.HandlerChainType;
import org.objectweb.celtix.configuration.ConfigurationItemMetadata;
import org.objectweb.celtix.configuration.impl.TypeSchema;
import org.objectweb.celtix.configuration.impl.TypeSchemaHelper;

public class HandlerChainDocument {
    private static final String HANDLER_CHAIN_TYPE_NAME = "handlerChainType";
    private static final String HANDLER_CONFIG_ELEM_NAME = "handler-config";
    private static final String HANDLER_CHAIN_ELEM_NAME = "handler-chain";
    private static final String HANDLER_CHAIN_NAME_ELEM_NAME = "handler-chain-name";
    private static final String HANDLER_ELEM_NAME = "handler";
    private static final String HANDLER_NAME_ELEM_NAME = "handler-name";
    private static final String HANDLER_CLASS_ELEM_NAME = "handler-class";
    private static final String INIT_PARAM_ELEM_NAME = "init=param";
    private static final String PARAM_NAME_ELEM_NAME = "param-name";
    private static final String PARAM_VALUE_ELEM_NAME = "param-value";
    private static final String JAXWS_TYPES_URI = "http://celtix.objectweb.org/bus/jaxws/configuration/types";

    private List<HandlerChainType> chains;

    HandlerChainDocument(InputStream is, boolean doValidate) {
        chains = new ArrayList<HandlerChainType>();
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(false);
            DocumentBuilder builder = dbf.newDocumentBuilder();
            Document srcDoc = builder.parse(is);
            dbf.setNamespaceAware(true);
            Document destDoc = builder.newDocument();
            transform(srcDoc, destDoc);

            NodeList chainNodes = destDoc.getFirstChild().getChildNodes();
            for (int i = 0; i < chainNodes.getLength(); i++) {
                Node node = chainNodes.item(i);
                if (Node.ELEMENT_NODE == node.getNodeType()
                    && HANDLER_CHAIN_ELEM_NAME.equals(getNodeName(node))) {

                    String location = "schemas/configuration/jaxws-types.xsd";
                    TypeSchema ts = new TypeSchemaHelper(true).get(JAXWS_TYPES_URI, null, location);

                    ConfigurationItemMetadata mdi = new ConfigurationItemMetadata() {
                        public Object getDefaultValue() {
                            return null;
                        }

                        public LifecyclePolicy getLifecyclePolicy() {
                            return LifecyclePolicy.STATIC;
                        }

                        public String getName() {
                            return "handlerChain";
                        }

                        public QName getType() {
                            return new QName(JAXWS_TYPES_URI, HANDLER_CHAIN_TYPE_NAME);
                        }

                    };

                    Object obj = ts.unmarshalDefaultValue(mdi, (Element)node, doValidate);
                    chains.add((HandlerChainType)obj);
                }
            }
        } catch (Exception ex) {
            if (ex instanceof WebServiceException) {
                throw (WebServiceException)ex;
            }
            throw new WebServiceException(ex);
        }
    }

    HandlerChainType getChain(String name) {
        if (null == name || "".equals(name)) {
            return chains.size() > 0 ? chains.get(0) : null;
        }
        for (HandlerChainType hc : chains) {
            if (name.equals(hc.getHandlerChainName())) {
                return hc;
            }
        }
        return null;
    }

    private String getNodeName(Node node) {
        String name = node.getNodeName();
        if (name.contains(":")) {
            name = name.substring(name.indexOf(":") + 1);
        }
        return name;
    }

    private void transform(Document src, Document dest) {
        Node destNode = dest.createElement(HANDLER_CONFIG_ELEM_NAME);
        dest.appendChild(destNode);
        Node srcNode = src.getFirstChild();
        createChainNodes(src, srcNode, dest, destNode);
    }

    private void createChainNodes(Document src, Node srcNode, Document dest, Node destNode) {
        NodeList nodes = srcNode.getChildNodes();
        for (int i = 0; i < nodes.getLength(); i++) {
            Node node = nodes.item(i);
            if (Node.ELEMENT_NODE == node.getNodeType()
                && HANDLER_CHAIN_ELEM_NAME.equals(getNodeName(node))) {
                Element el = dest.createElementNS(JAXWS_TYPES_URI, HANDLER_CHAIN_ELEM_NAME);
                destNode.appendChild(el);
                createLeafNode(src, node, dest, el, HANDLER_CHAIN_NAME_ELEM_NAME);
                createHandlerNodes(src, node, dest, el);
            }
        }
    }

    private void createLeafNode(Document src, Node srcNode, Document dest, Node destNode, String type) {
        NodeList nodes = srcNode.getChildNodes();
        for (int i = 0; i < nodes.getLength(); i++) {
            Node node = nodes.item(i);
            if (Node.ELEMENT_NODE == node.getNodeType() && type.equals(getNodeName(node))) {
                Element el = dest.createElementNS(JAXWS_TYPES_URI, type);
                el.setTextContent(node.getTextContent());
                destNode.appendChild(el);
                break;
            }
        }
    }

    private void createHandlerNodes(Document src, Node srcNode, Document dest, Node destNode) {
        NodeList nodes = srcNode.getChildNodes();
        for (int i = 0; i < nodes.getLength(); i++) {
            Node node = nodes.item(i);
            if (Node.ELEMENT_NODE == node.getNodeType() && HANDLER_ELEM_NAME.equals(getNodeName(node))) {
                Element el = dest.createElementNS(JAXWS_TYPES_URI, HANDLER_ELEM_NAME);
                destNode.appendChild(el);
                createLeafNode(src, node, dest, el, HANDLER_NAME_ELEM_NAME);
                createLeafNode(src, node, dest, el, HANDLER_CLASS_ELEM_NAME);
                createInitParamNodes(src, node, dest, el);
            }
        }
    }

    private void createInitParamNodes(Document src, Node srcNode, Document dest, Node destNode) {
        NodeList nodes = srcNode.getChildNodes();
        for (int i = 0; i < nodes.getLength(); i++) {
            Node node = nodes.item(i);
            if (Node.ELEMENT_NODE == node.getNodeType() && INIT_PARAM_ELEM_NAME.equals(getNodeName(node))) {
                Element el = dest.createElementNS(JAXWS_TYPES_URI, INIT_PARAM_ELEM_NAME);
                destNode.appendChild(el);
                createLeafNode(src, node, dest, el, PARAM_NAME_ELEM_NAME);
                createLeafNode(src, node, dest, el, PARAM_VALUE_ELEM_NAME);
            }
        }
    }
}
