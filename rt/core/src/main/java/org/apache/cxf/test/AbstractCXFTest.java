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

package org.apache.cxf.test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.xml.sax.SAXParseException;

import junit.framework.TestCase;

import org.apache.cxf.Bus;
import org.apache.cxf.BusException;
import org.apache.cxf.bus.cxf.CXFBusFactory;
import org.apache.cxf.helpers.DOMUtils;
import org.apache.cxf.message.Message;
import org.apache.cxf.message.MessageImpl;
import org.apache.cxf.service.model.EndpointInfo;
import org.apache.cxf.transport.Conduit;
import org.apache.cxf.transport.ConduitInitiator;
import org.apache.cxf.transport.ConduitInitiatorManager;
import org.apache.cxf.transport.MessageObserver;

/**
 * A basic test case meant for helping users unit test their services.
 */
public class AbstractCXFTest extends TestCase {
    
    private static String basedirPath;
    
    /**
     * Namespaces for the XPath expressions.
     */
    private Map<String, String> namespaces = new HashMap<String, String>();

    private Bus bus;
    
    public void setUp() throws Exception {
        bus = createBus();
        
        addNamespace("s", "http://schemas.xmlsoap.org/soap/envelope/");
        addNamespace("xsd", "http://www.w3.org/2001/XMLSchema");
        addNamespace("wsdl", "http://schemas.xmlsoap.org/wsdl/");
        addNamespace("wsdlsoap", "http://schemas.xmlsoap.org/wsdl/soap/");
        addNamespace("soap", "http://schemas.xmlsoap.org/soap/");
        
    }
    
    public Bus getBus() {
        return bus;
    }

    protected Bus createBus() throws BusException {
        return new CXFBusFactory().createBus();
    }

    protected Node invoke(String address, 
                          String transport,
                          String message) throws Exception {
        EndpointInfo ei = new EndpointInfo(null, "http://schemas.xmlsoap.org/soap/http");
        ei.setAddress(address);

        ConduitInitiatorManager conduitMgr = getBus().getExtension(ConduitInitiatorManager.class);
        ConduitInitiator conduitInit = conduitMgr.getConduitInitiator(transport);
        Conduit conduit = conduitInit.getConduit(ei);

        TestMessageObserver obs = new TestMessageObserver();
        conduit.setMessageObserver(obs);
        
        Message m = new MessageImpl();
        conduit.send(m);

        OutputStream os = m.getContent(OutputStream.class);
        InputStream is = getResourceAsStream(message);
        if (is == null) {
            throw new RuntimeException("Could not find resource " + message);
        }
        
        copy(is, os);

        byte[] bs = obs.getResponseStream().toByteArray();
        
        ByteArrayInputStream input = new ByteArrayInputStream(bs);
        try {
            return DOMUtils.readXml(input);
        } catch (SAXParseException e) {
            throw new IllegalStateException("Could not parse message:\n" 
                                            + obs.getResponseStream().toString());
        }
    }
   
    protected void copy(final InputStream input, final OutputStream output)
        throws IOException {
        int bufferSize = 1024;
        try {
            final byte[] buffer = new byte[bufferSize];

            int n = input.read(buffer);
            while (-1 != n) {
                output.write(buffer, 0, n);
                n = input.read(buffer);
            }
        } finally {
            input.close();
            output.close();
        }
    }
    
    /**
     * Assert that the following XPath query selects one or more nodes.
     * 
     * @param xpath
     * @throws Exception 
     */
    public NodeList assertValid(String xpath, Node node) throws Exception {
        return XPathAssert.assertValid(xpath, node, namespaces);
    }

    /**
     * Assert that the following XPath query selects no nodes.
     * 
     * @param xpath
     */
    public NodeList assertInvalid(String xpath, Node node) throws Exception {
        return XPathAssert.assertInvalid(xpath, node, namespaces);
    }

    /**
     * Asser that the text of the xpath node retrieved is equal to the value
     * specified.
     * 
     * @param xpath
     * @param value
     * @param node
     */
    public void assertXPathEquals(String xpath, String value, Node node) throws Exception {
        XPathAssert.assertXPathEquals(xpath, value, node, namespaces);
    }

    public void assertNoFault(Node node) throws Exception {
        XPathAssert.assertNoFault(node);
    }
    
    /**
     * Add a namespace that will be used for XPath expressions.
     * 
     * @param ns Namespace name.
     * @param uri The namespace uri.
     */
    public void addNamespace(String ns, String uri) {
        namespaces.put(ns, uri);
    }

    protected InputStream getResourceAsStream(String resource) {
        return getClass().getResourceAsStream(resource);
    }

    protected Reader getResourceAsReader(String resource) {
        return new InputStreamReader(getResourceAsStream(resource));
    }

    public File getTestFile(String relativePath) {
        return new File(getBasedir(), relativePath);
    }

    public static String getBasedir() {
        if (basedirPath != null) {
            return basedirPath;
        }

        basedirPath = System.getProperty("basedir");

        if (basedirPath == null) {
            basedirPath = new File("").getAbsolutePath();
        }

        return basedirPath;
    }
    
    class TestMessageObserver implements MessageObserver {
        ByteArrayOutputStream response = new ByteArrayOutputStream();
        boolean written;
        
        public ByteArrayOutputStream getResponseStream() throws Exception {
            synchronized (this) {
                if (!written) {
                    wait(1000000000);
                }
            }
            return response;
        }
        

        public void onMessage(Message message) {
            try {
                copy(message.getContent(InputStream.class), response);
            } catch (IOException e) {
                e.printStackTrace();
                fail();
            } finally {
                synchronized (this) {
                    written = true;
                    notifyAll();
                }
            }
        }
    }
}
