package org.objectweb.celtix.test;

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

import junit.framework.TestCase;

import org.objectweb.celtix.Bus;
import org.objectweb.celtix.bus.CeltixBusFactory;
import org.objectweb.celtix.helpers.DOMUtils;
import org.objectweb.celtix.message.Message;
import org.objectweb.celtix.message.MessageImpl;
import org.objectweb.celtix.messaging.Conduit;
import org.objectweb.celtix.messaging.ConduitInitiator;
import org.objectweb.celtix.messaging.ConduitInitiatorManager;
import org.objectweb.celtix.messaging.MessageObserver;
import org.objectweb.celtix.service.model.EndpointInfo;
import org.xmlsoap.schemas.wsdl.http.AddressType;

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
        
        namespaces.put("s", "http://schemas.xmlsoap.org/soap/envelope/");
    }
    
    public Bus getBus() {
        return bus;
    }

    protected Bus createBus() {
        return new CeltixBusFactory().createBus();
    }

    protected Node invoke(String address, 
                          String transport,
                          String message) throws Exception {
        EndpointInfo ei = new EndpointInfo(null, "http://schemas.xmlsoap.org/soap/http");
        AddressType a = new AddressType();
        a.setLocation(address);
        ei.addExtensor(a);

        ConduitInitiatorManager conduitMgr = getBus().getExtension(ConduitInitiatorManager.class);
        ConduitInitiator conduitInit = conduitMgr.getConduitInitiator(transport);
        Conduit conduit = conduitInit.getConduit(ei);

        TestMessageObserver obs = new TestMessageObserver();
        conduit.setMessageObserver(obs);
        
        Message m = new MessageImpl();
        conduit.send(m);

        OutputStream os = m.getContent(OutputStream.class);
        InputStream is = getResourceAsStream(message);
        copy(is, os, 8096);

        byte[] bs = obs.getResponseStream().toByteArray();
        if (bs.length == 0) {
            throw new RuntimeException("No response was received!");
        }
        ByteArrayInputStream input = new ByteArrayInputStream(bs);
        return DOMUtils.readXml(input);
    }

    private void copy(final InputStream input, final OutputStream output, final int bufferSize)
        throws IOException {
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
                copy(message.getContent(InputStream.class), response, 1024);
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
