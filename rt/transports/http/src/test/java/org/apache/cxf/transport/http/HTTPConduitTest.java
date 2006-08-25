package org.apache.cxf.transport.http;


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

import org.apache.cxf.helpers.CastUtils;
import org.apache.cxf.message.Message;
import org.apache.cxf.message.MessageImpl;
import org.apache.cxf.service.model.EndpointInfo;
import org.apache.cxf.transport.Conduit;
import org.apache.cxf.transport.Destination;
import org.apache.cxf.transport.MessageObserver;
import org.apache.cxf.transports.http.configuration.HTTPClientPolicy;
import org.apache.cxf.ws.addressing.EndpointReferenceType;
import org.apache.cxf.wsdl.EndpointReferenceUtils;
import org.easymock.classextension.EasyMock;
import org.easymock.classextension.IMocksControl;
import org.mortbay.http.HttpHandler;
import org.mortbay.http.handler.AbstractHttpHandler;
import org.mortbay.util.MultiMap;

public class HTTPConduitTest extends TestCase {
    private static final String NOWHERE = "http://nada.nothing.nowhere.null/";
    private static final String PAYLOAD = "message payload";
    private EndpointReferenceType target;
    private EndpointInfo endpointInfo;
    private HTTPConduitConfiguration config;
    private HTTPClientPolicy policy;
    private URLConnectionFactory connectionFactory;
    private URLConnection connection;
    private Proxy proxy;
    private Message inMessage;
    private MessageObserver observer;
    private OutputStream os;
    private InputStream is;
    private TestServerEngine decoupledEngine;
    private MultiMap parameters;
    private IMocksControl control;
    
    public void setUp() throws Exception {
        control = EasyMock.createNiceControl();
    }

    public void tearDown() {
        // avoid intermittent spurious failures on EasyMock detecting finalize
        // calls by mocking up only class data members (no local variables)
        // and explicitly making available for GC post-verify
        finalVerify();
        config = null;
        policy = null;
        connectionFactory = null;
        connection = null;
        proxy = null;
        inMessage = null;
        observer = null;
        os = null;
        is = null;
        parameters = null;
        decoupledEngine = null;
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
        verifySentMessage(conduit, message);
    }
    
    public void testSendWithHeaders() throws Exception {
        HTTPConduit conduit = setUpConduit(true, false, false);
        Message message = new MessageImpl();
        setUpHeaders(message);
        conduit.send(message);
        verifySentMessage(conduit, message, true);
    }
    
    public void testSendHttpConnection() throws Exception {
        HTTPConduit conduit = setUpConduit(true, true, false);
        Message message = new MessageImpl();
        conduit.send(message);
        verifySentMessage(conduit, message);
    }

    public void testSendHttpConnectionAutoRedirect() throws Exception {
        HTTPConduit conduit = setUpConduit(true, true, false);
        Message message = new MessageImpl();
        conduit.send(message);
        verifySentMessage(conduit, message);
    }
    
    public void testSendDecoupled() throws Exception {
        HTTPConduit conduit = setUpConduit(true, false, false, true);
        Message message = new MessageImpl();
        conduit.send(message);
        verifySentMessage(conduit, message, false, true);
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
        return setUpConduit(send, httpConnection, autoRedirect, false);
    }
    
    private HTTPConduit setUpConduit(boolean send,
                                     boolean httpConnection,
                                     boolean autoRedirect,
                                     boolean decoupled) throws Exception {
        endpointInfo = control.createMock(EndpointInfo.class);
        target = getEPR("bar/foo");
        connectionFactory = control.createMock(URLConnectionFactory.class);
        config = control.createMock(HTTPConduitConfiguration.class);
        config.getAddress();
        EasyMock.expectLastCall().andReturn(NOWHERE + "bar/foo").times(2);
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
            
            policy = new HTTPClientPolicy();
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
            
            if (decoupled) {
                decoupledEngine = new TestServerEngine();
                parameters = control.createMock(MultiMap.class);
            }
        }
        
        control.replay();
        
        HTTPConduit conduit = new HTTPConduit(null, 
                                              endpointInfo,
                                              null,
                                              connectionFactory,
                                              decoupledEngine,
                                              config);
        observer = new MessageObserver() {
            public void onMessage(Message m) {
                inMessage = m;
            }
        };
        conduit.setMessageObserver(observer);
        return conduit;
    }
    
    private void verifySentMessage(Conduit conduit, Message message)
        throws IOException {
        verifySentMessage(conduit, message, false);
    }

    private void verifySentMessage(Conduit conduit,
                                   Message message,
                                   boolean expectHeaders)
        throws IOException {
        verifySentMessage(conduit, message, expectHeaders, false);
    }
    
    private void verifySentMessage(Conduit conduit,
                                   Message message,
                                   boolean expectHeaders,
                                   boolean decoupled)
        throws IOException {
        control.verify();
        control.reset();
                
        OutputStream wrappedOS = verifyRequestHeaders(message, expectHeaders);
        
        os = EasyMock.createMock(OutputStream.class);
        connection.getOutputStream();
        EasyMock.expectLastCall().andReturn(os);
        os.write(PAYLOAD.getBytes(), 0, PAYLOAD.length());
        EasyMock.expectLastCall();

        config.getPolicy();
        EasyMock.expectLastCall().andReturn(policy).times(decoupled ? 2 : 1);

        URL decoupledURL = null;
        if (decoupled) {
            decoupledURL = new URL(NOWHERE + "response");
            policy.setDecoupledEndpoint(decoupledURL.toString());
        } 
        
        os.flush();
        EasyMock.expectLastCall();
        os.flush();
        EasyMock.expectLastCall();
        os.close();
        EasyMock.expectLastCall();
        
        verifyHandleResponse(decoupled);
        
        control.replay();
        
        Destination backChannel = null;
        AbstractHttpHandler decoupledHandler = null;
        if (decoupled) {
            decoupledEngine.verifyCallCounts(new int[]{0, 0, 0});
            backChannel = conduit.getBackChannel();
            assertNotNull("expected back channel", backChannel);
            decoupledEngine.verifyCallCounts(new int[]{1, 0, 1});
            decoupledHandler = decoupledEngine.servants.get(decoupledURL);
            assertNotNull("expected servant registered", decoupledHandler);
            MessageObserver decoupledObserver =
                ((HTTPConduit.DecoupledDestination)backChannel).getMessageObserver();
            assertSame("unexpected decoupled destination",
                       observer,       
                       decoupledObserver);
        } else {
            backChannel = conduit.getBackChannel();
            assertNull("unexpected back channel", backChannel);
        }
        
        wrappedOS.flush();
        wrappedOS.flush();
        wrappedOS.close();
        
        assertNotNull("expected in message", inMessage);
        assertSame("unexpected response headers",
                   inMessage.get(HTTP_RESPONSE_HEADERS), 
                   Collections.EMPTY_MAP);
        Integer expectedResponseCode = decoupled 
                                       ? HttpURLConnection.HTTP_ACCEPTED
                                       : HttpURLConnection.HTTP_OK;
        assertEquals("unexpected response code",
                     expectedResponseCode,
                     inMessage.get(HTTP_RESPONSE_CODE));
        assertTrue("unexpected content formats",
                   inMessage.getContentFormats().contains(InputStream.class));
        assertSame("unexpected content", is, inMessage.getContent(InputStream.class));
        
        if (decoupled) {
            verifyDecoupledResponse(decoupledHandler);
        }
        
        conduit.close();
        if (decoupled) {
            decoupledEngine.verifyCallCounts(new int[]{1, 1, 2});
        }
        
        finalVerify();
    }

    private OutputStream verifyRequestHeaders(Message message, boolean expectHeaders)
        throws IOException {
        Map<String, List<String>> headers =
            CastUtils.cast((Map<?, ?>)message.get(HTTP_REQUEST_HEADERS));
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
        return wrappedOS;
    }
    
    private void verifyHandleResponse(boolean decoupled) throws IOException {
        connection.getHeaderFields();
        EasyMock.expectLastCall().andReturn(Collections.EMPTY_MAP);
        int responseCode = decoupled 
                           ? HttpURLConnection.HTTP_ACCEPTED
                           : HttpURLConnection.HTTP_OK;
        if (connection instanceof HttpURLConnection) {
            ((HttpURLConnection)connection).getResponseCode();
            EasyMock.expectLastCall().andReturn(responseCode);
            ((HttpURLConnection)connection).getErrorStream();
            EasyMock.expectLastCall().andReturn(null);
        } else {
            connection.getHeaderField(HTTP_RESPONSE_CODE);
            String responseString = Integer.toString(responseCode);
            EasyMock.expectLastCall().andReturn(responseString).times(2);
        }
        is = EasyMock.createMock(InputStream.class);
        connection.getInputStream();
        EasyMock.expectLastCall().andReturn(is);
    }
    
    private void verifyDecoupledResponse(AbstractHttpHandler decoupledHandler)
        throws IOException {
        inMessage = null;
        is = EasyMock.createMock(InputStream.class);
        os = EasyMock.createMock(OutputStream.class);
        TestHttpRequest decoupledRequest = new TestHttpRequest(is, parameters);
        TestHttpResponse decoupledResponse = new TestHttpResponse(os);
        decoupledHandler.handle("pathInContext",
                                "pathParams",
                                decoupledRequest,
                                decoupledResponse);
        assertNotNull("expected decoupled in message", inMessage);
        assertNotNull("expected response headers",
                      inMessage.get(HTTP_RESPONSE_HEADERS));
        assertEquals("unexpected response code",
                     HttpURLConnection.HTTP_OK,
                     inMessage.get(HTTP_RESPONSE_CODE));

        assertEquals("unexpected getInputStream count",
                     1,
                     decoupledRequest.getInputStreamCallCount());
        assertEquals("unexpected getParameters counts",
                     1,
                     decoupledRequest.getParametersCallCount());
        assertTrue("unexpected content formats",
                   inMessage.getContentFormats().contains(InputStream.class));
        InputStream decoupledIS = inMessage.getContent(InputStream.class);
        assertNotNull("unexpected content", decoupledIS);
        
        decoupledIS.close();
        assertEquals("unexpected setHandled count",
                     1,
                     decoupledRequest.getHandledCallCount());
        assertEquals("unexpected setHandled count",
                     1,
                     decoupledResponse.getCommitCallCount());
        
        inMessage.setContent(InputStream.class, is);

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
    
    /**
     * EasyMock does not seem able to properly mock calls to ServerEngine -
     * expectations set seem to be ignored.
     */
    private class TestServerEngine implements ServerEngine {
        private int callCounts[] = {0, 0, 0};
        private Map<URL, AbstractHttpHandler> servants =
            new HashMap<URL, AbstractHttpHandler>();
        
        public void addServant(URL url, AbstractHttpHandler handler) {
            callCounts[0]++;
            servants.put(url, handler);
        }

        public void removeServant(URL url) {
            callCounts[1]++;
            servants.remove(url);
        }

        public HttpHandler getServant(URL url) {
            callCounts[2]++;
            return servants.get(url);
        }

        void verifyCallCounts(int expectedCallCounts[]) {
            assertEquals("unexpected addServant call count",
                         expectedCallCounts[0],
                         callCounts[0]);
            assertEquals("unexpected removeServant call count",
                         expectedCallCounts[1],
                         callCounts[1]);
            assertEquals("unexpected getServant call count",
                         expectedCallCounts[2],
                         callCounts[2]);
        }
    }
}
