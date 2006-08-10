package org.objectweb.celtix.transports.http;


import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.ws.BindingProvider;
import javax.xml.ws.handler.MessageContext;

import static javax.xml.ws.handler.MessageContext.HTTP_REQUEST_HEADERS;
import static javax.xml.ws.handler.MessageContext.HTTP_RESPONSE_CODE;
import static javax.xml.ws.handler.MessageContext.HTTP_RESPONSE_HEADERS;

import junit.framework.TestCase;

import org.easymock.classextension.EasyMock;
import org.easymock.classextension.IMocksControl;
import org.mortbay.http.HttpRequest;
import org.mortbay.http.HttpResponse;
import org.mortbay.http.handler.AbstractHttpHandler;
import org.objectweb.celtix.Bus;
import org.objectweb.celtix.common.util.Base64Utility;
import org.objectweb.celtix.helpers.CastUtils;
import org.objectweb.celtix.message.Exchange;
import org.objectweb.celtix.message.Message;
import org.objectweb.celtix.message.MessageImpl;
import org.objectweb.celtix.messaging.Conduit;
import org.objectweb.celtix.messaging.MessageObserver;
import org.objectweb.celtix.transports.http.configuration.HTTPServerPolicy;
import org.objectweb.celtix.ws.addressing.EndpointReferenceType;
import org.objectweb.celtix.wsdl.EndpointReferenceUtils;


public class JettyHTTPDestinationTest extends TestCase {
    private static final String NOWHERE = "http://nada.nothing.nowhere.null/";
    private static final String PAYLOAD = "message payload";
    private static final String QUERY = "?name";
    private static final String ANON_ADDR =
        "http://www.w3.org/2005/08/addressing/anonymous";
    private static final String AUTH_HEADER = "Authorization";
    private static final String USER = "copernicus";
    private static final String PASSWD = "epicycles";
    private static final String BASIC_AUTH =
        "Basic " + Base64Utility.encode((USER + ":" + PASSWD).getBytes());   
    private static final String CHALLENGE_HEADER = "WWW-Authenticate";
    private static final String BASIC_CHALLENGE = "Basic realm=terra";
    private static final String DIGEST_CHALLENGE = "Digest realm=luna";
    private static final String CUSTOM_CHALLENGE = "Custom realm=sol";
    private Bus bus;
    private EndpointReferenceType address;
    private ServerEngine engine;
    private HTTPDestinationConfiguration config;
    private HTTPServerPolicy policy;
    private JettyHTTPDestination destination;
    private TestHttpRequest request;
    private TestHttpResponse response;
    private Message inMessage;
    private MessageObserver observer;
    private InputStream is;
    private OutputStream os;
    private IMocksControl control;

    
    public void setUp() throws Exception {
        control = EasyMock.createNiceControl();
    }

    public void tearDown() {
        control.verify();
        control = null;
        bus = null;
        address = null;
        engine = null;
        config = null;
        request = null;
        response = null;
        inMessage = null;
        is = null;
        os = null;
        destination = null;
    }
    
    public void testGetAddress() throws Exception {
        destination = setUpDestination();
        EndpointReferenceType ref = destination.getAddress();
        assertNotNull("unexpected null address", ref);
        assertEquals("unexpected address",
                     EndpointReferenceUtils.getAddress(ref),
                     EndpointReferenceUtils.getAddress(address));
    }
    
    public void testRemoveServant() throws Exception {
        destination = setUpDestination();
        setUpRemoveServant();
        destination.setMessageObserver(null);
    }

    public void testDoServiceRedirectURL() throws Exception {
        destination = setUpDestination(false);
        setUpDoService(true);
        destination.doService(request, response);

        assertEquals("unexpected sendRedirect calls",
                     1,
                     response.getSendRedirectCallCount());
        assertEquals("unexpected commit calls",
                     1,
                     response.getCommitCallCount());
        assertEquals("unexpected setHandled calls",
                     1,
                     request.getHandledCallCount());
    }

    public void testDoService() throws Exception {
        destination = setUpDestination(false);
        setUpDoService(false);
        destination.doService(request, response);
        verifyDoService();
    }
    
    public void testGetAnonBackChannel() throws Exception {
        destination = setUpDestination(false);
        setUpDoService(false);
        destination.doService(request, response);
        EndpointReferenceType replyTo = getEPR(ANON_ADDR);
        Conduit backChannel = destination.getBackChannel(inMessage, replyTo);
        
        assertNotNull("expected back channel", backChannel);
        assertNull("unexpected backchannel-backchannel",
                   backChannel.getBackChannel());
        assertSame("unexpected target",
                   replyTo,
                   backChannel.getTarget());
    }
    
    public void testGetBackChannelSend() throws Exception {
        destination = setUpDestination(false);
        setUpDoService(false, true);
        destination.doService(request, response);
        EndpointReferenceType replyTo = getEPR(ANON_ADDR);
        Conduit backChannel = destination.getBackChannel(inMessage, replyTo);
        Message outMessage = setUpOutMessage();
        backChannel.send(outMessage);
        verifyBackChannelSend(outMessage, 200);
    }

    public void testGetBackChannelSendFault() throws Exception {
        destination = setUpDestination(false);
        setUpDoService(false, true);
        destination.doService(request, response);
        EndpointReferenceType replyTo = getEPR(ANON_ADDR);
        Conduit backChannel = destination.getBackChannel(inMessage, replyTo);
        Message outMessage = setUpOutMessage();
        backChannel.send(outMessage);
        verifyBackChannelSend(outMessage, 500);
    }

    private JettyHTTPDestination setUpDestination()
        throws Exception {
        return setUpDestination(false);
    };
    
    private JettyHTTPDestination setUpDestination(boolean contextMatchOnStem)
        throws Exception {
        address = getEPR("foo/bar");
        bus = control.createMock(Bus.class);
        engine = control.createMock(ServerEngine.class);
        config = control.createMock(HTTPDestinationConfiguration.class);
        config.getAddress();
        EasyMock.expectLastCall().andReturn(NOWHERE + "bar/foo").times(2);
       
        config.contextMatchOnStem();
        EasyMock.expectLastCall().andReturn(contextMatchOnStem);
        engine.addServant(EasyMock.eq(NOWHERE + "bar/foo"),
                          EasyMock.isA(AbstractHttpHandler.class));
        
        policy = new HTTPServerPolicy();   
        control.replay();
        
        JettyHTTPDestination dest =
            new JettyHTTPDestination(bus, address, engine, config);
        observer = new MessageObserver() {
            public void onMessage(Message m) {
                inMessage = m;
            }
        };
        dest.setMessageObserver(observer);
        return dest;
    }
    
    private void setUpRemoveServant() throws Exception {
        control.verify();
        control.reset();
        engine.removeServant(EasyMock.eq(new URL(NOWHERE + "bar/foo")));
        EasyMock.expectLastCall();
        control.replay();
    }
    
    private void setUpDoService(boolean setRedirectURL) throws Exception {
        setUpDoService(setRedirectURL, false);
    }

    private void setUpDoService(boolean setRedirectURL,
                                boolean sendResponse) throws Exception {
        control.verify();
        control.reset();

        is = EasyMock.createMock(InputStream.class);
        os = EasyMock.createMock(OutputStream.class);
        
        // EasyMock does not seem able to properly mock calls to HttpRequest
        // or HttpResponse - expectations set seem to be ignored.
        // Hence we use hand-crafted sub-classes instead of mocks.
        //
        //request = EasyMock.createMock(HttpRequest.class);
        //response = EasyMock.createMock(HttpResponse.class);
        request = new TestHttpRequest("POST", is, "bar/foo", QUERY);
        response = new TestHttpResponse(os);
        
        config.getPolicy();
        EasyMock.expectLastCall().andReturn(policy);

        if (setRedirectURL) {
            policy.setRedirectURL(NOWHERE + "foo/bar");
            config.getPolicy();
            EasyMock.expectLastCall().andReturn(policy);
            //response.sendRedirect(EasyMock.eq(NOWHERE + "foo/bar"));
            //EasyMock.expectLastCall();
            //response.commit();
            //EasyMock.expectLastCall();
            //request.setHandled(true);
            //EasyMock.expectLastCall();
        } else {
            //request.getMethod();
            //EasyMock.expectLastCall().andReturn("POST").times(2);
            //request.getInputStream();
            //EasyMock.expectLastCall().andReturn(is);
            //request.getPath();
            //EasyMock.expectLastCall().andReturn("bar/foo");
            //request.getQuery();
            //EasyMock.expectLastCall().andReturn(QUERY);
            //request.setHandled(true);
            //EasyMock.expectLastCall();  
            //response.commit();
            //EasyMock.expectLastCall();
            //if (sendResponse) {
            //    response.getOutputStream();
            //    EasyMock.expectLastCall().andReturn(os);
            //    response.commit();
            //    EasyMock.expectLastCall();                
            //}
        }
        control.replay();
    }
    
    private Message setUpOutMessage() {
        Message outMessage = new MessageImpl();
        outMessage.putAll(inMessage);
        return outMessage;
    }
    
    private void setUpResponseHeaders(Message outMessage) {
        Map<String, List<String>> responseHeaders =
            CastUtils.cast((Map<?, ?>)outMessage.get(HTTP_RESPONSE_HEADERS));
        assertNotNull("expected response headers", responseHeaders);
        List<String> challenges = new ArrayList<String>();
        challenges.add(BASIC_CHALLENGE);
        challenges.add(DIGEST_CHALLENGE);
        challenges.add(CUSTOM_CHALLENGE);
        responseHeaders.put(CHALLENGE_HEADER, challenges);
    }

    private void verifyDoService() throws Exception {
        assertNotNull("unexpected null message", inMessage);
        assertSame("unexpected HTTP request",
                   inMessage.get(JettyHTTPDestination.HTTP_REQUEST),
                   request);
        assertSame("unexpected HTTP response",
                   inMessage.get(JettyHTTPDestination.HTTP_RESPONSE),
                   response);
        assertEquals("unexpected method",
                     inMessage.get(MessageContext.HTTP_REQUEST_METHOD),
                     "POST");
        assertEquals("unexpected path",
                     inMessage.get(MessageContext.PATH_INFO),
                     "bar/foo");
        assertEquals("unexpected query",
                     inMessage.get(MessageContext.QUERY_STRING),
                     QUERY);
        verifyRequestHeaders();
        
        Exchange exchange = inMessage.getExchange();
        assertNotNull("expected exchange", exchange);
        assertSame("unexpected destination", 
                   destination,
                   exchange.getDestination());
        assertSame("unexpected message",
                   inMessage,
                   exchange.getInMessage());
        
        assertEquals("unexpected getMethod calls",
                     2,
                     request.getMethodCallCount());
        assertEquals("unexpected getInputStream calls",
                     1,
                     request.getInputStreamCallCount());
        assertEquals("unexpected getPath calls",
                     1,
                     request.getPathCallCount());
        assertEquals("unexpected getQuery calls",
                     1,
                     request.getQueryCallCount());
        assertEquals("unexpected setHandled calls",
                     1,
                     request.getHandledCallCount());
    }

    private void verifyRequestHeaders() throws Exception {
        Map<String, List<String>> requestHeaders =
            CastUtils.cast((Map<?, ?>)inMessage.get(HTTP_REQUEST_HEADERS));
        assertNotNull("expected request headers",
                      requestHeaders);
        assertEquals("expected getFieldNames",
                     1,
                     request.getFieldNamesCallCount());
        List<String> values = requestHeaders.get("content-type");
        assertNotNull("expected field", values);
        assertEquals("unexpected values", 2, values.size());
        assertTrue("expected value", values.contains("text/xml"));
        assertTrue("expected value", values.contains("charset=utf8"));
        values = requestHeaders.get(AUTH_HEADER);
        assertNotNull("expected field", values);
        assertEquals("unexpected values", 1, values.size());
        assertTrue("expected value", values.contains(BASIC_AUTH));
        assertEquals("expected user",
                     USER,
                     inMessage.get(BindingProvider.USERNAME_PROPERTY));
        assertEquals("expected passwd",
                     PASSWD,
                     inMessage.get(BindingProvider.PASSWORD_PROPERTY));
    }
    
    private void verifyResponseHeaders(Message outMessage) throws Exception {
        Map<String, List<String>> responseHeaders =
            CastUtils.cast((Map<?, ?>)outMessage.get(HTTP_RESPONSE_HEADERS));
        assertNotNull("expected response headers",
                      responseHeaders);
        assertEquals("expected addField",
                     3,
                     response.getAddFieldCallCount());
        Enumeration e = response.getFieldValues(CHALLENGE_HEADER);
        List<String> challenges = new ArrayList<String>();
        while (e.hasMoreElements()) {
            challenges.add((String)e.nextElement());
        }
        assertTrue("expected challenge",
                   challenges.contains(BASIC_CHALLENGE));
        assertTrue("expected challenge",
                   challenges.contains(DIGEST_CHALLENGE));
        assertTrue("expected challenge",
                   challenges.contains(CUSTOM_CHALLENGE));
    }
    
    private void verifyBackChannelSend(Message outMessage, int status) throws Exception {
        assertTrue("unexpected content formats",
                   outMessage.getContentFormats().contains(OutputStream.class));
        OutputStream responseOS = outMessage.getContent(OutputStream.class);
        assertNotNull("expected output stream", responseOS);
        assertTrue("unexpected output stream type",
                   responseOS instanceof AbstractWrappedOutputStream);
        assertEquals("expected commit",
                     1,
                     response.getCommitCallCount());
        
        outMessage.put(HTTP_RESPONSE_CODE, status);          
        responseOS.write(PAYLOAD.getBytes());
        
        setUpResponseHeaders(outMessage);
        
        OutputStream underlyingOS =
            ((AbstractWrappedOutputStream)responseOS).getOut();
        assertTrue("unexpected underlying output stream type",
                   underlyingOS instanceof ByteArrayOutputStream);
        assertEquals("expected getOutputStream",
                     0,
                     response.getOutputStreamCallCount());
        responseOS.flush();
        assertEquals("expected setStatus",
                     1,
                     response.getStatusCallCount());
        assertEquals("unexpected status",
                     status,
                     response.getStatus());
        if (status == 500) {
            assertEquals("unexpected status message",
                         "Fault+Occurred",
                         response.getReason());
        }
        verifyResponseHeaders(outMessage);
        assertEquals("expected getOutputStream",
                     1,
                     response.getOutputStreamCallCount());
        underlyingOS = ((AbstractWrappedOutputStream)responseOS).getOut();
        assertFalse("unexpected underlying output stream type: " + underlyingOS.getClass(),
                    underlyingOS instanceof ByteArrayOutputStream);

        responseOS.close();
        assertEquals("expected commit",
                     2,
                     response.getCommitCallCount());

    }

    /**
     * EasyMock does not seem able to properly mock calls to HttpRequest -
     * expectations set seem to be ignored.
     */
    private static class TestHttpRequest extends HttpRequest {
        private String method;
        private InputStream is;
        private String path;
        private String query;
        private int[] callCounts = {0, 0, 0, 0, 0, 0, 0};
        private Map<String, List<String>> fields;
        
        TestHttpRequest(String m, InputStream i, String p, String q) {
            method = m;
            is = i;
            path = p;
            query = q;
            fields = new HashMap<String, List<String>>();
            List<String> contentTypes = new ArrayList<String>();
            contentTypes.add("text/xml");
            contentTypes.add("charset=utf8");
            fields.put("content-type", contentTypes);
            List<String> auth = new ArrayList<String>();
            auth.add(BASIC_AUTH);
            fields.put(AUTH_HEADER, auth);
        }
        
        public String getMethod() {
            callCounts[0]++;
            return method;
        }
        
        int getMethodCallCount() {
            return callCounts[0];
        }
        
        public InputStream getInputStream() {
            callCounts[1]++;
            return is;
        }

        int getInputStreamCallCount() {
            return callCounts[1];
        }

        public String getPath() {
            callCounts[2]++;
            return path;
        }
        
        int getPathCallCount() {
            return callCounts[2];
        }
        
        public String getQuery() {
            callCounts[3]++;
            return query;
        }
        
        int getQueryCallCount() {
            return callCounts[3];
        }
        
        public void setHandled(boolean h) {
            callCounts[4]++;
        }
        
        int getHandledCallCount() {
            return callCounts[4];
        }
        
        public Enumeration getFieldNames() {
            callCounts[5]++;
            return Collections.enumeration(fields.keySet());
        }
        
        int getFieldNamesCallCount() {
            return callCounts[5];
        }
        
        public Enumeration getFieldValues(String f) {
            callCounts[6]++;
            return Collections.enumeration(fields.get(f));            
        }
        
        int getFieldValuesCallCount() {
            return callCounts[6];
        }

    }

    private static class TestHttpResponse extends HttpResponse {
        private OutputStream os;
        private int[] callCounts = {0, 0, 0, 0, 0};
        
        TestHttpResponse(OutputStream o) {
            os = o;
        }
        
        public void commit() {
            callCounts[0]++;
        }
        
        int getCommitCallCount() {
            return callCounts[0];
        }
        
        public OutputStream getOutputStream() {
            callCounts[1]++;
            return os;
        }

        int getOutputStreamCallCount() {
            return callCounts[1];
        }
        
        public void sendRedirect(String url) {
            callCounts[2]++;
        }
        
        int getSendRedirectCallCount() {
            return callCounts[2];
        }
        
        public void setStatus(int s) {
            super.setStatus(s);
            callCounts[3]++;
        }
        
        int getStatusCallCount() {
            return callCounts[3];
        }
        
        public void addField(String name, String value) {
            super.addField(name, value);
            callCounts[4]++;
        }
        
        int getAddFieldCallCount() {
            return callCounts[4];
        }
    }
    
    static EndpointReferenceType getEPR(String s) {
        return EndpointReferenceUtils.getEndpointReference(NOWHERE + s);
    }
}
