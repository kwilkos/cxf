package org.objectweb.celtix.bus.handlers;


import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.ws.WebServiceException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;



public class HandlerChainConfig {

    private static final String HANDLER_NODE = "handler"; 
    private static final String HANDLER_CHAIN_NODE = "handler-chain"; 
    private static final String HANDLER_CHAIN_NAME_NODE = "handler-chain-name"; 
    private static final String HANDLER_NAME_NODE = "handler-name"; 
    private static final String HANDLER_CLASS_NODE = "handler-class"; 
    private static final String INIT_PARAM_NODE = "init-param"; 
    private static final String PARAM_NAME = "param-name"; 
    private static final String PARAM_VALUE = "param-value"; 

    private final Map<String, List<HandlerConfig>> chains = new HashMap<String, List<HandlerConfig>>(); 
    private List<HandlerConfig> first; 

    public HandlerChainConfig(InputStream in) throws IOException {
        assert in != null;
        parseConfigFile(in); 
    }

    public List<HandlerConfig> getHandlerConfig() { 
        return getHandlerConfig(null); 
    } 

    public List<HandlerConfig> getHandlerConfig(String chainName) { 
        if ("".equals(chainName) || null == chainName) { 
            return first;
        } 
        return chains.get(chainName); 
    }

    
    private void parseConfigFile(InputStream in) throws IOException {

        try {
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document doc = builder.parse(in); 

            NodeList chainNodes = doc.getFirstChild().getChildNodes(); 
            for (int i = 0; i < chainNodes.getLength(); i++) { 
                Node node = chainNodes.item(i); 
                if (HANDLER_CHAIN_NODE.equals(getNodeName(node))) {
                    parseHandlerChain((Element)node); 
                } 
            } 
        } catch (Exception ex) { 
            if (ex instanceof WebServiceException) { 
                throw (WebServiceException)ex;
            } 
            throw new WebServiceException(ex);
        } 
    }

    private void parseHandlerChain(Element el) { 
        
        String chainName = null; 
        List<HandlerConfig> chain = new ArrayList<HandlerConfig>(); 

        NodeList children = el.getChildNodes(); 
        for (int i = 0; i < children.getLength(); i++) { 
            Node n = children.item(i); 
            if (HANDLER_CHAIN_NAME_NODE.equals(getNodeName(n))) { 
                chainName = n.getFirstChild().getNodeValue();
            } else if (HANDLER_NODE.equals(getNodeName(n))) {
                chain.add(parseHandler((Element)n));
            } 
        } 

        if (first == null) { 
            first = chain;
        } 
        chains.put(chainName, chain);
    }

    private HandlerConfig parseHandler(Element el) { 

        HandlerConfig h = new HandlerConfig(); 
        
        NodeList children = el.getChildNodes(); 
        for (int i = 0; i < children.getLength(); i++) { 
            Node n = children.item(i); 
            String nodeName = getNodeName(n); 

            if (HANDLER_NAME_NODE.equals(nodeName)) {
                if (n.getFirstChild() != null) { 
                    h.setName(n.getFirstChild().getNodeValue());
                } else { 
                    throw new WebServiceException("handler name not specified"); 
                } 
            }
            
            if (HANDLER_CLASS_NODE.equals(nodeName)) {
                if (n.getFirstChild() != null) { 
                    h.setClassName(n.getFirstChild().getNodeValue());
                } else { 
                    throw new WebServiceException("handler class not specified"); 
                } 
            } 

            if (INIT_PARAM_NODE.equals(nodeName)) { 
                h.addInitParam(parseInitParam((Element)n));
            } 
        } 

//         if (null == h.getName() || "".equals(h.getName())) { 
//             throw new WebServiceException("handler name not specified"); 
//         } 
        if (null == h.getClassName() || "".equals(h.getClassName())) {
            throw new WebServiceException("handler class not specified"); 
        } 

        return h; 
    } 

    private HandlerConfig.Param parseInitParam(Element el) {
        
        String name = null; 
        String value = null; 

        NodeList children = el.getChildNodes(); 
        for (int i = 0; i < children.getLength(); i++) { 
            Node n = children.item(i); 

            if (PARAM_NAME.equals(getNodeName(n)) 
                && n.getFirstChild() != null) {
                name = n.getFirstChild().getNodeValue();
            } 
            if (PARAM_VALUE.equals(getNodeName(n))
                && n.getFirstChild() != null) { 
                value = n.getFirstChild().getNodeValue();
            }
        }

        return new HandlerConfig.Param(name, value); 
    } 

    private String getNodeName(Node node) { 
        String name = node.getNodeName(); 
        if (name.contains(":")) { 
            name = name.substring(name.indexOf(":") + 1); 
        }
        return name;
    } 

}
