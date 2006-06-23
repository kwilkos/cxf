package org.objectweb.celtix.systest.rest;

import java.net.URL;

import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.stream.StreamSource;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.objectweb.celtix.systest.common.ClientServerSetupBase;
import org.objectweb.celtix.systest.common.ClientServerTestBase;

public class RestProviderTest extends ClientServerTestBase {

    private Transformer xformer;
    
    
    public static Test suite() throws Exception {
        TestSuite suite = new TestSuite(RestProviderTest.class);
        return new ClientServerSetupBase(suite) {
            public void startServers() throws Exception {
                assertTrue("server did not launch correctly", launchServer(RestProvider.class));
            }
        };
    }    

    public void setUp() throws Exception { 
        xformer = TransformerFactory.newInstance().newTransformer();
    }
         
    /** 
     * test that rest provider responds to GET requests 
     * and that the implementation has received the 
     * necessary context properties.  It will return
     * an XML doc like this: 
     * 
     * ?<?xml version="1.0" encoding="utf-8"?>
     *  <context-details>
     *      <request-method>GET</request-method>
     *      <path-info>/systest/rest</path-info>
     *      <queryString>foo=1&bar=2</queryString>
     *  </context-details>
     */
    public void testGetNoQuery() throws Exception {
        
        URL url = new URL(RestProvider.PUBLISH_ADDRESS);
        Source source = new StreamSource(url.openStream());
        DOMResult result = new DOMResult(); 
        xformer.transform(source, result);
        
        Node root = result.getNode().getFirstChild();
        assertEquals("context-details", root.getNodeName());
        
        NodeList children = root.getChildNodes();
        assertEquals("incorrect number of child nodes returned", 3, children.getLength());
        Node reqMethodNode = children.item(0);
        assertEquals("request-method", reqMethodNode.getNodeName());
        assertTrue(reqMethodNode instanceof Element);
        assertEquals(((Element)reqMethodNode).getTextContent(), "GET");
        
    }
    
}
