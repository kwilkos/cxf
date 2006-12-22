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

import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import junit.framework.TestCase;

import org.apache.cxf.helpers.CastUtils;
import org.apache.cxf.message.Message;
import org.apache.cxf.message.MessageImpl;
import org.apache.cxf.service.model.EndpointInfo;
import org.apache.cxf.transport.Conduit;
import org.apache.cxf.transport.Destination;
import org.apache.cxf.transport.MessageObserver;
import org.apache.cxf.ws.addressing.EndpointReferenceType;
import org.apache.cxf.wsdl.EndpointReferenceUtils;
import org.easymock.classextension.EasyMock;
import org.easymock.classextension.IMocksControl;
import org.mortbay.jetty.Handler;
import org.mortbay.jetty.Request;
import org.mortbay.jetty.handler.AbstractHandler;

public class HTTPConduitTest extends TestCase {
    private static final String NOWHERE = "http://nada.nothing.nowhere.null/";
    private static final String PAYLOAD = "message payload";
    private EndpointReferenceType target;
    private EndpointInfo endpointInfo;
    private URLConnectionFactory connectionFactory;
    private URLConnection connection;
    private Proxy proxy;
    private Message inMessage;
    private MessageObserver observer;
    private ServletOutputStream os;
    private ServletInputStream is;
    private TestServerEngine decoupledEngine;
    //private MultiMap parameters;
    private IMocksControl control;
    
    public void setUp() throws Exception {
        control = EasyMock.createNiceControl();
    }

    public void tearDown() {
        // avoid intermittent spurious failures on EasyMock detecting finalize
        // calls by mocking up only class data members (no local variables)
        // and explicitly making available for GC post-verify
        finalVerify();
        connectionFactory = null;
        connection = null;
        proxy = null;
        inMessage = null;
        observer = null;
        os = null;
        is = null;
        //parameters = null;
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
        HTTPConduit conduit = setUpConduit(true, true, true);
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
        message.put(Message.PROTOCOL_HEADERS, headers);        
        message.put(Message.USERNAME, "BJ");
        message.put(Message.PASSWORD, "value");
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
        endpointInfo.getAddress();
        EasyMock.expectLastCall().andReturn(NOWHERE + "bar/foo").times(2);
        if (send) {
            //proxy = control.createMock(Proxy.class);
            proxy =  null;
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
            
            connection.setConnectTimeout(303030);
            EasyMock.expectLastCall();
            connection.setReadTimeout(404040);
            EasyMock.expectLastCall();
            connection.setUseCaches(false);
            EasyMock.expectLastCall();
            
            

            if (httpConnection) {
                ((HttpURLConnection)connection).setInstanceFollowRedirects(autoRedirect);
                EasyMock.expectLastCall();                
                if (!autoRedirect) {
                    ((HttpURLConnection)connection).getRequestMethod();
                    EasyMock.expectLastCall().andReturn("POST");
                    ((HttpURLConnection)connection).setChunkedStreamingMode(2048);
                    EasyMock.expectLastCall();                    
                }
            }
            
            if (decoupled) {
                decoupledEngine = new TestServerEngine();
                //parameters = control.createMock(MultiMap.class);
            }            
            
        }
               
        
        control.replay();
        
        HTTPConduit conduit = new HTTPConduit(null, 
                                              endpointInfo,
                                              null,
                                              connectionFactory,
                                              decoupledEngine);
        conduit.retrieveConnectionFactory();

        if (send) {
            conduit.getClient().setConnectionTimeout(303030);
            conduit.getClient().setReceiveTimeout(404040);
            if (httpConnection) {
                conduit.getClient().setAutoRedirect(autoRedirect);
                if (!autoRedirect) {
                    conduit.getClient().setAllowChunking(true);
                } 
            }
        }

        if (decoupled) {
            URL decoupledURL = null;
            if (decoupled) {
                decoupledURL = new URL(NOWHERE + "response");
                conduit.getClient().setDecoupledEndpoint(decoupledURL.toString());
            } 
        }
       

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
        
        if (connection instanceof HttpURLConnection) {
            ((HttpURLConnection)connection).getRequestMethod();
            EasyMock.expectLastCall().andReturn("POST");
        }
        
        
        os = EasyMock.createMock(ServletOutputStream.class);
        connection.getOutputStream();
        EasyMock.expectLastCall().andReturn(os);
        os.write(PAYLOAD.getBytes(), 0, PAYLOAD.length());
        EasyMock.expectLastCall();

        URL decoupledURL = null;
        if (decoupled) {
            decoupledURL = new URL(NOWHERE + "response");
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
        AbstractHandler decoupledHandler = null;
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
        Map<?, ?> headerMap = (Map<?, ?>) inMessage.get(Message.PROTOCOL_HEADERS);
        assertEquals("unexpected response headers", headerMap.size(), 0);
        Integer expectedResponseCode = decoupled 
                                       ? HttpURLConnection.HTTP_ACCEPTED
                                       : HttpURLConnection.HTTP_OK;
        assertEquals("unexpected response code",
                     expectedResponseCode,
                     inMessage.get(Message.RESPONSE_CODE));
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
            CastUtils.cast((Map<?, ?>)message.get(Message.PROTOCOL_HEADERS));
        assertNotNull("expected request headers set", headers);
        assertTrue("expected output stream format",
                   message.getContentFormats().contains(OutputStream.class));
        OutputStream wrappedOS = message.getContent(OutputStream.class);
        assertNotNull("expected output stream", wrappedOS);
        
        wrappedOS.write(PAYLOAD.getBytes());
        
        message.put(HTTPConduit.HTTP_CONNECTION, connection);
        if (expectHeaders) {
            connection.addRequestProperty(EasyMock.eq("Authorization"),
                                          EasyMock.eq("Basic Qko6dmFsdWU="));            
            EasyMock.expectLastCall();
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
            connection.getHeaderField(Message.RESPONSE_CODE);
            String responseString = Integer.toString(responseCode);
            EasyMock.expectLastCall().andReturn(responseString).times(2);
        }
        is = EasyMock.createMock(ServletInputStream.class);
        connection.getInputStream();
        EasyMock.expectLastCall().andReturn(is);
    }
    
    private void verifyDecoupledResponse(AbstractHandler decoupledHandler)
        throws IOException {
        inMessage = null;
        is = EasyMock.createMock(ServletInputStream.class);
        os = EasyMock.createMock(ServletOutputStream.class);
        Request decoupledRequest = EasyMock.createMock(Request.class);
        decoupledRequest.getInputStream();
        EasyMock.expectLastCall().andReturn(is);
        decoupledRequest.setHandled(true);
        EasyMock.replay(decoupledRequest);
        
        HttpServletResponse decoupledResponse = EasyMock.createMock(HttpServletResponse.class);
        decoupledResponse.getCharacterEncoding();
        EasyMock.expectLastCall().andReturn("utf8");
        decoupledResponse.getContentType();
        EasyMock.expectLastCall().andReturn("test");
        decoupledResponse.flushBuffer();
        EasyMock.expectLastCall();
        EasyMock.replay(decoupledResponse);
       
        try {
            decoupledHandler.handle("pathInContext",                                
                                    decoupledRequest,
                                    decoupledResponse, Handler.REQUEST);
        } catch (ServletException e) {
            fail("There should not throw the serletException");
        }
        assertNotNull("expected decoupled in message", inMessage);
        assertNotNull("expected response headers",
                      inMessage.get(Message.PROTOCOL_HEADERS));
        assertEquals("unexpected response code",
                     HttpURLConnection.HTTP_OK,
                     inMessage.get(Message.RESPONSE_CODE));

        assertTrue("unexpected content formats",
                   inMessage.getContentFormats().contains(InputStream.class));
        InputStream decoupledIS = inMessage.getContent(InputStream.class);
        assertNotNull("unexpected content", decoupledIS);
        
        decoupledIS.close();
        
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
        private Map<URL, AbstractHandler> servants =
            new HashMap<URL, AbstractHandler>();
        
        public void addServant(URL url, AbstractHandler handler) {
            callCounts[0]++;
            servants.put(url, handler);
        }

        public void removeServant(URL url) {
            callCounts[1]++;
            servants.remove(url);
        }

        public Handler getServant(URL url) {
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
