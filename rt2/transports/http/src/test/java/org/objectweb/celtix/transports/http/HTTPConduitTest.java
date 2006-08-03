package org.objectweb.celtix.transports.http;


import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static javax.xml.ws.handler.MessageContext.HTTP_REQUEST_HEADERS;
import static javax.xml.ws.handler.MessageContext.HTTP_RESPONSE_CODE;
import static javax.xml.ws.handler.MessageContext.HTTP_RESPONSE_HEADERS;

import junit.framework.TestCase;

import org.easymock.classextension.EasyMock;
import org.easymock.classextension.IMocksControl;
import org.objectweb.celtix.message.Message;
import org.objectweb.celtix.message.MessageImpl;
import org.objectweb.celtix.messaging.MessageObserver;
import org.objectweb.celtix.transports.http.configuration.HTTPClientPolicy;
import org.objectweb.celtix.ws.addressing.EndpointReferenceType;
import org.objectweb.celtix.wsdl.EndpointReferenceUtils;



public class HTTPConduitTest extends TestCase {
    private static final String NOWHERE = "http://nada.nothing.nowhere.null/";
    private static final String PAYLOAD = "message payload";
    private EndpointReferenceType target;
    private HTTPConduitConfiguration config;
    private URLConnectionFactory connectionFactory;
    private URLConnection connection;
    private Proxy proxy;
    private Message inMessage;
    private MessageObserver observer;
    private OutputStream os;
    private InputStream is;
    private IMocksControl control;
    
    public void setUp() throws Exception {
        control = EasyMock.createNiceControl();
    }

    public void tearDown() {
        // avoid intermittent spurious failures on EasyMock detecting finalize calls
        // by mocking up only class data members (no local variables) and
        // explicitly making available for GC post-verify
        finalVerify();
        config = null;
        connectionFactory = null;
        connection = null;
        proxy = null;
        inMessage = null;
        observer = null;
        os = null;
        is = null;
    }

    public void testGetTarget() throws Exception {
        HTTPConduit conduit = setUpConduit(false);
        EndpointReferenceType ref = conduit.getTarget();
        assertNotNull("unexpected null target", ref);
        assertEquals("unexpected target",
                     EndpointReferenceUtils.getAddress(ref),
                     EndpointReferenceUtils.getAddress(target));
        assertEquals("unexpected URL",
                     conduit.getURL().getPath(),
                     "/bar/foo");
    }
    
    public void testSend() throws Exception {
        HTTPConduit conduit = setUpConduit(true, false, false);
        Message message = new MessageImpl();
        conduit.send(message);
        verifySentMessage(message);
    }
    
    public void testSendWithHeaders() throws Exception {
        HTTPConduit conduit = setUpConduit(true, false, false);
        Message message = new MessageImpl();
        setUpHeaders(message);
        conduit.send(message);
        verifySentMessage(message, true);
    }
    
    public void testSendHttpConnection() throws Exception {
        HTTPConduit conduit = setUpConduit(true, true, false);
        Message message = new MessageImpl();
        conduit.send(message);
        verifySentMessage(message);
    }

    public void testSendHttpConnectionAutoRedirect() throws Exception {
        HTTPConduit conduit = setUpConduit(true, true, false);
        Message message = new MessageImpl();
        conduit.send(message);
        verifySentMessage(message);
    }
    
    private void setUpHeaders(Message message) {
        Map<String, List<String>> headers = new HashMap<String, List<String>>();
        List<String> contentTypes = new ArrayList<String>();
        contentTypes.add("text/xml");
        contentTypes.add("charset=utf8");
        headers.put("content-type", contentTypes);
        message.put(HTTP_REQUEST_HEADERS, headers);
    }

    private HTTPConduit setUpConduit(boolean send) throws Exception {
        return setUpConduit(send, false, false);
    }
    
    private HTTPConduit setUpConduit(boolean send,
                                     boolean httpConnection,
                                     boolean autoRedirect) throws Exception {
        target = getEPR("foo/bar");
        connectionFactory = control.createMock(URLConnectionFactory.class);
        config = control.createMock(HTTPConduitConfiguration.class);
        config.getAddress();
        EasyMock.expectLastCall().andReturn(NOWHERE + "bar/foo");
        if (send) {
            proxy = control.createMock(Proxy.class);
            config.getProxy();
            EasyMock.expectLastCall().andReturn(proxy);
            connection =
                control.createMock(httpConnection ? HttpURLConnection.class : URLConnection.class);
            connectionFactory.createConnection(
                                      EasyMock.eq(proxy), 
                                      EasyMock.eq(new URL(NOWHERE + "bar/foo")));
            EasyMock.expectLastCall().andReturn(connection);
            connection.setDoOutput(true);
            EasyMock.expectLastCall();
            
            if (httpConnection) {
                ((HttpURLConnection)connection).setRequestMethod("POST");
            }
            
            HTTPClientPolicy policy = new HTTPClientPolicy();
            config.getPolicy();
            EasyMock.expectLastCall().andReturn(policy).times(2);
            policy.setConnectionTimeout(303030);
            connection.setConnectTimeout(303030);
            EasyMock.expectLastCall();
            policy.setReceiveTimeout(404040);
            connection.setReadTimeout(404040);
            EasyMock.expectLastCall();
            connection.setUseCaches(false);
            EasyMock.expectLastCall();

            if (httpConnection) {
                config.getPolicy();
                EasyMock.expectLastCall().andReturn(policy).times(2);
                policy.setAutoRedirect(autoRedirect);
                ((HttpURLConnection)connection).setInstanceFollowRedirects(autoRedirect);
                EasyMock.expectLastCall();
                if (!autoRedirect) {
                    policy.setAllowChunking(true);
                    ((HttpURLConnection)connection).setChunkedStreamingMode(2048);
                    EasyMock.expectLastCall();
                }
            }
        }
        control.replay();
        HTTPConduit conduit = new HTTPConduit(target, connectionFactory, config);
        observer = new MessageObserver() {
            public void onMessage(Message m) {
                inMessage = m;
            }
        };
        conduit.setMessageObserver(observer);
        return conduit;
    }
    
    private void verifySentMessage(Message message)
        throws IOException {
        verifySentMessage(message, false);
    }
    
    @SuppressWarnings("unchecked")
    private void verifySentMessage(Message message, boolean expectHeaders)
        throws IOException {
        control.verify();
        control.reset();
        
        Map<String, List<String>> headers =
            (Map<String, List<String>>)message.get(HTTP_REQUEST_HEADERS);
        assertNotNull("expected request headers set", headers);
        assertTrue("expected output stream format",
                   message.getContentFormats().contains(OutputStream.class));
        OutputStream wrappedOS = message.getContent(OutputStream.class);
        assertNotNull("expected output stream", wrappedOS);
        
        wrappedOS.write(PAYLOAD.getBytes());
        
        message.put(HTTPConduit.HTTP_CONNECTION, connection);
        if (expectHeaders) {
            connection.addRequestProperty(EasyMock.eq("content-type"),
                                          EasyMock.eq("text/xml"));
            EasyMock.expectLastCall();
            connection.addRequestProperty(EasyMock.eq("content-type"),
                                          EasyMock.eq("charset=utf8"));
            EasyMock.expectLastCall();
        }
        
        os = EasyMock.createMock(OutputStream.class);
        connection.getOutputStream();
        EasyMock.expectLastCall().andReturn(os);
        os.write(PAYLOAD.getBytes(), 0, PAYLOAD.length());
        EasyMock.expectLastCall();
        
        connection.getHeaderFields();
        EasyMock.expectLastCall().andReturn(Collections.EMPTY_MAP);
        if (connection instanceof HttpURLConnection) {
            ((HttpURLConnection)connection).getResponseCode();
            EasyMock.expectLastCall().andReturn(HttpURLConnection.HTTP_ACCEPTED);
            ((HttpURLConnection)connection).getErrorStream();
            EasyMock.expectLastCall().andReturn(null);
        } else {
            connection.getHeaderField(HTTP_RESPONSE_CODE);
            String response = Integer.toString(HttpURLConnection.HTTP_ACCEPTED);
            EasyMock.expectLastCall().andReturn(response).times(2);
        }
        is = EasyMock.createMock(InputStream.class);
        connection.getInputStream();
        EasyMock.expectLastCall().andReturn(is);
        
        control.replay();
        wrappedOS.close();
        
        assertNotNull("expected in message", inMessage);
        assertSame("unexpected response headers",
                   inMessage.get(HTTP_RESPONSE_HEADERS), 
                   Collections.EMPTY_MAP);
        assertEquals("unexpected response code",
                     inMessage.get(HTTP_RESPONSE_CODE),
                     new Integer(HttpURLConnection.HTTP_ACCEPTED));
        assertTrue("unexpected content formats",
                   inMessage.getContentFormats().contains(InputStream.class));
        assertSame("unexpected content", is, inMessage.getContent(InputStream.class));
        
        finalVerify();
    }
    
    private void finalVerify() {
        if (control != null) {
            control.verify();
            control = null;
        }
    }

    static EndpointReferenceType getEPR(String s) {
        return EndpointReferenceUtils.getEndpointReference(NOWHERE + s);
    }
}
