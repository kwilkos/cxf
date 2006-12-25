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

package org.apache.cxf.transport.http;


import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletInputStream;
import javax.servlet.ServletOutputStream;

import junit.framework.TestCase;

import org.apache.cxf.Bus;
import org.apache.cxf.common.util.Base64Utility;
import org.apache.cxf.common.util.StringUtils;
import org.apache.cxf.configuration.security.AuthorizationPolicy;
import org.apache.cxf.helpers.CastUtils;
import org.apache.cxf.io.AbstractCachedOutputStream;
import org.apache.cxf.message.ExchangeImpl;
import org.apache.cxf.message.Message;
import org.apache.cxf.message.MessageImpl;
import org.apache.cxf.service.model.EndpointInfo;
import org.apache.cxf.transport.Conduit;
import org.apache.cxf.transport.ConduitInitiator;
import org.apache.cxf.transport.MessageObserver;
import org.apache.cxf.transports.http.configuration.HTTPServerPolicy;
import org.apache.cxf.ws.addressing.EndpointReferenceType;
import org.apache.cxf.wsdl.EndpointReferenceUtils;
import org.easymock.classextension.EasyMock;
import org.mortbay.jetty.HttpFields;
import org.mortbay.jetty.Request;
import org.mortbay.jetty.Response;
import org.mortbay.jetty.handler.AbstractHandler;

public class JettyHTTPDestinationTest extends TestCase {
    protected static final String AUTH_HEADER = "Authorization";
    protected static final String USER = "copernicus";
    protected static final String PASSWD = "epicycles";
    protected static final String BASIC_AUTH =
        "Basic " + Base64Utility.encode((USER + ":" + PASSWD).getBytes());   

    private static final String NOWHERE = "http://nada.nothing.nowhere.null/";
    private static final String PAYLOAD = "message payload";
    private static final String CHALLENGE_HEADER = "WWW-Authenticate";
    private static final String BASIC_CHALLENGE = "Basic realm=terra";
    private static final String DIGEST_CHALLENGE = "Digest realm=luna";
    private static final String CUSTOM_CHALLENGE = "Custom realm=sol";
    private Bus bus;
    private ConduitInitiator conduitInitiator;
    private Conduit decoupledBackChannel;
    private EndpointInfo endpointInfo;
    private EndpointReferenceType address;
    private EndpointReferenceType replyTo;
    private ServerEngine engine;
    private HTTPServerPolicy policy;
    private JettyHTTPDestination destination;
    private Request request;
    private Response response;
    private Message inMessage;
    private Message outMessage;
    private MessageObserver observer;
    private ServletInputStream is;
    private ServletOutputStream os;
    

    
    public void setUp() throws Exception {
        //control = EasyMock.createNiceControl();
    }

    public void tearDown() {
        bus = null;
        conduitInitiator = null;
        decoupledBackChannel = null;
        address = null;
        replyTo = null;
        engine = null;
        request = null;
        response = null;
        inMessage = null;
        outMessage = null;
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
                     StringUtils.addDefaultPortIfMissing(EndpointReferenceUtils.getAddress(address)));
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
    }

    public void testDoService() throws Exception {
        destination = setUpDestination(false);
        setUpDoService(false);
        destination.doService(request, response);
        verifyDoService();
    }
    
    public void testDoServiceWithHttpGET() throws Exception {
        destination = setUpDestination(false);
        setUpDoService(false,
                       false,
                       false,
                       "GET",
                       "?customerId=abc&cutomerAdd=def", 200);
        destination.doService(request, response);
        
        assertNotNull("unexpected null message", inMessage);
        assertEquals("unexpected method",
                     inMessage.get(Message.HTTP_REQUEST_METHOD),
                     "GET");
        assertEquals("unexpected path",
                     inMessage.get(Message.PATH_INFO),
                     "/bar/foo");
        assertEquals("unexpected query",
                     inMessage.get(Message.QUERY_STRING),
                     "?customerId=abc&cutomerAdd=def");

    }

    public void testGetAnonBackChannel() throws Exception {
        destination = setUpDestination(false);
        setUpDoService(false);
        destination.doService(request, response);
        setUpInMessage();
        Conduit backChannel = destination.getBackChannel(inMessage, null, null);
        
        assertNotNull("expected back channel", backChannel);
        assertNull("unexpected backchannel-backchannel",
                   backChannel.getBackChannel());
        assertEquals("unexpected target",
                     JettyHTTPDestination.ANONYMOUS_ADDRESS,
                     backChannel.getTarget().getAddress().getValue());
    }
    
    public void testGetBackChannelSend() throws Exception {
        destination = setUpDestination(false);
        setUpDoService(false, true);
        destination.doService(request, response);
        setUpInMessage();
        Conduit backChannel =
            destination.getBackChannel(inMessage, null, null);
        outMessage = setUpOutMessage();
        backChannel.send(outMessage);
        verifyBackChannelSend(backChannel, outMessage, 200);
    }

    public void testGetBackChannelSendFault() throws Exception {
        destination = setUpDestination(false);
        setUpDoService(false, true, 500);
        destination.doService(request, response);
        setUpInMessage();
        Conduit backChannel =
            destination.getBackChannel(inMessage, null, null);
        outMessage = setUpOutMessage();
        backChannel.send(outMessage);
        verifyBackChannelSend(backChannel, outMessage, 500);
    }
    
    public void testGetBackChannelSendOneway() throws Exception {
        destination = setUpDestination(false);
        setUpDoService(false, true, 500);
        destination.doService(request, response);
        setUpInMessage();
        Conduit backChannel =
            destination.getBackChannel(inMessage, null, null);
        outMessage = setUpOutMessage();
        backChannel.send(outMessage);
        verifyBackChannelSend(backChannel, outMessage, 500, true);
    }

    public void testGetBackChannelSendDecoupled() throws Exception {
        destination = setUpDestination(false);
        replyTo = getEPR(NOWHERE + "response/foo");
        setUpDoService(false, true, true, 202);
        destination.doService(request, response);
        setUpInMessage();
        
        Message partialResponse = setUpOutMessage();
        Conduit partialBackChannel =
            destination.getBackChannel(inMessage, partialResponse, replyTo);
        assertEquals("unexpected response code",
                     202,
                     partialResponse.get(Message.RESPONSE_CODE));
        partialBackChannel.send(partialResponse);
        verifyBackChannelSend(partialBackChannel, partialResponse, 202);

        outMessage = setUpOutMessage();
        Conduit fullBackChannel =
            destination.getBackChannel(inMessage, null, replyTo);
        assertSame("unexpected back channel",
                   fullBackChannel,
                   decoupledBackChannel);
        fullBackChannel.send(outMessage);
    }
    
    private JettyHTTPDestination setUpDestination()
        throws Exception {
        return setUpDestination(false);
    };
    
    private JettyHTTPDestination setUpDestination(boolean contextMatchOnStem)
        throws Exception {        
        
        policy = new HTTPServerPolicy();
        address = getEPR("bar/foo");
        bus = EasyMock.createMock(Bus.class);
        conduitInitiator = EasyMock.createMock(ConduitInitiator.class);
        endpointInfo = EasyMock.createMock(EndpointInfo.class);
        engine = EasyMock.createMock(ServerEngine.class);        
        EasyMock.expect(endpointInfo.getAddress()).andReturn(NOWHERE + "bar/foo").anyTimes();        
        endpointInfo.getExtensor(HTTPServerPolicy.class);
        EasyMock.expectLastCall().andReturn(policy).anyTimes();        
        endpointInfo.getProperty("contextMatchStrategy");
        EasyMock.expectLastCall().andReturn("stem");
        endpointInfo.getProperty("fixedParameterOrder");
        EasyMock.expectLastCall().andReturn(true);            
        engine.addServant(EasyMock.eq(new URL(NOWHERE + "bar/foo")),
                          EasyMock.isA(AbstractHandler.class));
        EasyMock.expectLastCall();
        EasyMock.replay(engine);
        EasyMock.replay(endpointInfo);
        
        JettyHTTPDestination dest = new JettyHTTPDestination(bus,
                                                             conduitInitiator,
                                                             endpointInfo,
                                                             engine);
        dest.retrieveEngine();        
        observer = new MessageObserver() {
            public void onMessage(Message m) {
                inMessage = m;
            }
        };
        dest.setMessageObserver(observer);
        return dest;
    }
    
    private void setUpRemoveServant() throws Exception {
        EasyMock.reset(engine);
        engine.removeServant(EasyMock.eq(new URL(NOWHERE + "bar/foo")));
        EasyMock.expectLastCall();
        EasyMock.replay(engine);
    }
    
    private void setUpDoService(boolean setRedirectURL) throws Exception {
        setUpDoService(setRedirectURL, false);
    }

    private void setUpDoService(boolean setRedirectURL,
                                boolean sendResponse) throws Exception {
        setUpDoService(setRedirectURL,
                       sendResponse,
                       false);
    }
    
    private void setUpDoService(boolean setRedirectURL,
                                boolean sendResponse, int status) throws Exception {
        String method = "POST";
        String query = "?name";
        setUpDoService(setRedirectURL, sendResponse, false, method, query, status);
    }
    
    private void setUpDoService(boolean setRedirectURL,
                                boolean sendResponse, boolean decoupled, int status) throws Exception {
        String method = "POST";
        String query = "?name";
        setUpDoService(setRedirectURL, sendResponse, decoupled, method, query, status);
    }

    private void setUpDoService(boolean setRedirectURL,
            boolean sendResponse,
            boolean decoupled) throws Exception {
        String method = "POST";
        String query = "?name";
        setUpDoService(setRedirectURL, sendResponse, decoupled, method, query, 200);
    }
   
    private void setUpDoService(boolean setRedirectURL,
                                boolean sendResponse,
                                boolean decoupled,
                                String method,
                                String query,
                                int status) throws Exception {
        is = EasyMock.createMock(ServletInputStream.class);
        os = EasyMock.createMock(ServletOutputStream.class);
        request = EasyMock.createMock(Request.class);
        response = EasyMock.createMock(Response.class);
       
        request.getMethod();
        EasyMock.expectLastCall().andReturn(method);
        
        if ("GET".equals(method)) {
            System.out.println("query = " + query);
            request.getRequestURI();
            EasyMock.expectLastCall().andReturn("http://localhost/" + "bar/foo" +  query);            
        } 
        
        if (setRedirectURL) {
            policy.setRedirectURL(NOWHERE + "foo/bar");
            response.sendRedirect(EasyMock.eq(NOWHERE + "foo/bar"));
            EasyMock.expectLastCall();
            response.flushBuffer();
            EasyMock.expectLastCall();
            request.setHandled(true);
            EasyMock.expectLastCall();
        } else { // method is POST 
            EasyMock.expect(request.getMethod()).andReturn(method);            
            EasyMock.expect(request.getInputStream()).andReturn(is);
            EasyMock.expect(request.getContextPath()).andReturn("/bar");
            EasyMock.expect(request.getPathInfo()).andReturn("/foo");
            EasyMock.expect(request.getQueryString()).andReturn(query);            
            EasyMock.expect(request.getContentType()).andReturn("text/xml charset=utf8");
            
            HttpFields httpFields = new HttpFields();
            httpFields.add("content-type", "text/xml");
            httpFields.add("content-type", "charset=utf8");
            httpFields.put(JettyHTTPDestinationTest.AUTH_HEADER, JettyHTTPDestinationTest.BASIC_AUTH);
            
            EasyMock.expect(request.getHeaderNames()).andReturn(httpFields.getFieldNames());
            request.getHeaders("content-type");
            EasyMock.expectLastCall().andReturn(httpFields.getValues("content-type"));
            request.getHeaders(JettyHTTPDestinationTest.AUTH_HEADER);
            EasyMock.expectLastCall().andReturn(httpFields.getValues(JettyHTTPDestinationTest.AUTH_HEADER));
                                              
            EasyMock.expect(request.getInputStream()).andReturn(is);
            request.setHandled(true);
            EasyMock.expectLastCall();  
            response.flushBuffer();
            EasyMock.expectLastCall();
            if (sendResponse) {
                response.setStatus(status);
                EasyMock.expectLastCall();
                response.setContentType("text/xml charset=utf8");
                EasyMock.expectLastCall();
                response.addHeader(EasyMock.isA(String.class), EasyMock.isA(String.class));
                EasyMock.expectLastCall().anyTimes();
                response.getOutputStream();
                EasyMock.expectLastCall().andReturn(os);
                response.getStatus();
                EasyMock.expectLastCall().andReturn(status).anyTimes();
                response.flushBuffer();
                EasyMock.expectLastCall();                
            }
        }
        
        if (decoupled) {
            decoupledBackChannel = EasyMock.createMock(Conduit.class);
            conduitInitiator.getConduit(EasyMock.isA(EndpointInfo.class),
                                        EasyMock.eq(replyTo));
            EasyMock.expectLastCall().andReturn(decoupledBackChannel);
            decoupledBackChannel.setMessageObserver(EasyMock.isA(MessageObserver.class));
            EasyMock.expectLastCall();
            decoupledBackChannel.send(EasyMock.isA(Message.class));
            EasyMock.expectLastCall();
            EasyMock.replay(conduitInitiator);
            EasyMock.replay(decoupledBackChannel);
        }
        EasyMock.replay(response);
        EasyMock.replay(request);
    }
    
    private void setUpInMessage() {
        inMessage.setExchange(new ExchangeImpl());
    }
    
    private Message setUpOutMessage() {
        Message outMsg = new MessageImpl();
        outMsg.putAll(inMessage);
        outMsg.setExchange(new ExchangeImpl());
        outMsg.put(Message.PROTOCOL_HEADERS,
                   new HashMap<String, List<String>>());
        return outMsg;
    }
    
    private void setUpResponseHeaders(Message outMsg) {
        Map<String, List<String>> responseHeaders =
            CastUtils.cast((Map<?, ?>)outMsg.get(Message.PROTOCOL_HEADERS));
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
                     inMessage.get(Message.HTTP_REQUEST_METHOD),
                     "POST");
        assertEquals("unexpected path",
                     inMessage.get(Message.PATH_INFO),
                     "/bar/foo");
        assertEquals("unexpected query",
                     inMessage.get(Message.QUERY_STRING),
                     "?name");
        verifyRequestHeaders();      
        
       
    }

    private void verifyRequestHeaders() throws Exception {
        Map<String, List<String>> requestHeaders =
            CastUtils.cast((Map<?, ?>)inMessage.get(Message.PROTOCOL_HEADERS));
        assertNotNull("expected request headers",
                      requestHeaders);        
        List<String> values = requestHeaders.get("content-type");
        assertNotNull("expected field", values);
        assertEquals("unexpected values", 2, values.size());
        assertTrue("expected value", values.contains("text/xml"));
        assertTrue("expected value", values.contains("charset=utf8"));
        values = requestHeaders.get(AUTH_HEADER);
        assertNotNull("expected field", values);
        assertEquals("unexpected values", 1, values.size());
        assertTrue("expected value", values.contains(BASIC_AUTH));
        
        AuthorizationPolicy authpolicy =
            inMessage.get(AuthorizationPolicy.class);
        assertNotNull("Expected some auth tokens", policy);
        assertEquals("expected user",
                     USER,
                     authpolicy.getUserName());
        assertEquals("expected passwd",
                     PASSWD,
                     authpolicy.getPassword());
    }
    
    private void verifyResponseHeaders(Message outMsg) throws Exception {
        Map<String, List<String>> responseHeaders =
            CastUtils.cast((Map<?, ?>)outMsg.get(Message.PROTOCOL_HEADERS));
        assertNotNull("expected response headers",
                      responseHeaders);
        /*assertEquals("expected addField",
                     3,
                     response.getAddFieldCallCount());
        Enumeration e = response.getHeaders(CHALLENGE_HEADER);
        List<String> challenges = new ArrayList<String>();
        while (e.hasMoreElements()) {
            challenges.add((String)e.nextElement());
        }
        assertTrue("expected challenge",
                   challenges.contains(BASIC_CHALLENGE));
        assertTrue("expected challenge",
                   challenges.contains(DIGEST_CHALLENGE));
        assertTrue("expected challenge",
                   challenges.contains(CUSTOM_CHALLENGE));*/
    }
    
    private void verifyBackChannelSend(Conduit backChannel,
                                       Message outMsg,
                                       int status) throws Exception {
        verifyBackChannelSend(backChannel, outMsg, status, false);
    }
    
    private void verifyBackChannelSend(Conduit backChannel,
                                       Message outMsg,
                                       int status,
                                       boolean oneway) throws Exception {
        assertTrue("unexpected back channel type",
                   backChannel instanceof JettyHTTPDestination.BackChannelConduit);
        assertTrue("unexpected content formats",
                   outMsg.getContentFormats().contains(OutputStream.class));
        OutputStream responseOS = outMsg.getContent(OutputStream.class);
        assertNotNull("expected output stream", responseOS);
        assertTrue("unexpected output stream type",
                   responseOS instanceof AbstractCachedOutputStream);        
        
        outMsg.put(Message.RESPONSE_CODE, status);          
        responseOS.write(PAYLOAD.getBytes());
        
        setUpResponseHeaders(outMsg);
        
        OutputStream underlyingOS =
            ((AbstractCachedOutputStream)responseOS).getOut();
        assertTrue("unexpected underlying output stream type",
                   underlyingOS instanceof ByteArrayOutputStream);       
        outMsg.getExchange().setOneWay(oneway);
        responseOS.flush();
      
        assertEquals("unexpected status",
                     status,
                     response.getStatus());
        
        verifyResponseHeaders(outMsg);
        
        underlyingOS = ((AbstractCachedOutputStream)responseOS).getOut();
        assertFalse("unexpected underlying output stream type: "
                    + underlyingOS.getClass(),
                    underlyingOS instanceof ByteArrayOutputStream);
       
        if (oneway) {
            assertNull("unexpected HTTP response",
                       outMsg.get(JettyHTTPDestination.HTTP_RESPONSE));
        } else {
            assertNotNull("expected HTTP response",
                           outMsg.get(JettyHTTPDestination.HTTP_RESPONSE));
            responseOS.close();            
        }
    }
    
    static EndpointReferenceType getEPR(String s) {
        return EndpointReferenceUtils.getEndpointReference(NOWHERE + s);
    }
}
