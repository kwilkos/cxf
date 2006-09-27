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

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;


import org.apache.cxf.Bus;
import org.apache.cxf.common.logging.LogUtils;
import org.apache.cxf.common.util.Base64Utility;
import org.apache.cxf.configuration.ConfigurationProvider;
import org.apache.cxf.configuration.security.AuthorizationPolicy;
import org.apache.cxf.configuration.security.SSLClientPolicy;
import org.apache.cxf.helpers.CastUtils;
import org.apache.cxf.io.AbstractWrappedOutputStream;
import org.apache.cxf.message.Exchange;
import org.apache.cxf.message.ExchangeImpl;
import org.apache.cxf.message.Message;
import org.apache.cxf.message.MessageImpl;
import org.apache.cxf.service.model.EndpointInfo;
import org.apache.cxf.transport.Conduit;
import org.apache.cxf.transport.Destination;
import org.apache.cxf.transport.MessageObserver;
import org.apache.cxf.transport.http.conduit.HTTPConduitConfigBean;
import org.apache.cxf.transports.http.configuration.HTTPClientPolicy;
import org.apache.cxf.ws.addressing.AttributedURIType;
import org.apache.cxf.ws.addressing.EndpointReferenceType;
import org.apache.cxf.wsdl.EndpointReferenceUtils;
import org.mortbay.http.HttpRequest;
import org.mortbay.http.HttpResponse;
import org.mortbay.http.handler.AbstractHttpHandler;

/**
 * HTTP Conduit implementation.
 */
public class HTTPConduit extends HTTPConduitConfigBean implements Conduit {

    public static final String HTTP_CONNECTION = "http.connection";
    
    /**
     * REVISIT: temporary mechanism to allow decoupled response endpoint
     * to be configured, pending the maturation on the real config model
     */
    public static final String HTTP_DECOUPLED_ENDPOINT =
        "http.decoupled.endpoint";
    
    private static final Logger LOG = LogUtils.getL7dLogger(HTTPConduit.class);
    private final Bus bus;
    private final URLConnectionFactory connectionFactory;
    private URL url;
    private MessageObserver incomingObserver;
    private EndpointReferenceType target;
    
    private ServerEngine decoupledEngine;
    private URL decoupledURL;
    private DecoupledDestination decoupledDestination;
    private EndpointInfo endpointInfo;

    /**
     * Constructor
     * 
     * @param b the associated Bus
     * @param ei the endpoint info of the initiator
     * @throws IOException
     */
    public HTTPConduit(Bus b, EndpointInfo ei) throws IOException {
        this(b,
             ei,
             null);
    }

    /**
     * Constructor
     * 
     * @param b the associated Bus
     * @param ei the endpoint info of the initiator
     * @param t the endpoint reference of the target
     * @throws IOException
     */
    public HTTPConduit(Bus b, EndpointInfo ei, EndpointReferenceType t) throws IOException {
        this(b,
             ei,
             t,
             null,
             null);
    }

    /**
     * Constructor, allowing subsititution of
     * connnection factory and decoupled engine.
     * 
     * @param b the associated Bus
     * @param ei the endpoint info of the initiator
     * @param t the endpoint reference of the target
     * @param factory the URL connection factory
     * @param eng the decoupled engine
     * @throws IOException
     */
    public HTTPConduit(Bus b,
                       EndpointInfo ei,
                       EndpointReferenceType t,
                       URLConnectionFactory factory,
                       ServerEngine eng) throws IOException {
        init();
        bus = b;
        endpointInfo = ei;
        connectionFactory = factory != null
                            ? factory
                            : getDefaultConnectionFactory();
        decoupledEngine = eng;
        url = t == null
              ? new URL(getAddress())
              : new URL(t.getAddress().getValue());
        target = getTargetReference(t);
    }
    
    /**
     * Register a message observer for incoming messages.
     * 
     * @param observer the observer to notify on receipt of incoming
     */
    public void setMessageObserver(MessageObserver observer) {
        incomingObserver = observer;
        LOG.info("registering incoming observer: " + incomingObserver);
    }

    /**
     * Send an outbound message.
     * 
     * @param message the message to be sent.
     */
    public void send(Message message) throws IOException {
        Map<String, List<String>> headers = setHeaders(message);
        
        String value = (String)message.get(Message.ENDPOINT_ADDRESS);
        URL currentURL = value != null ? new URL(value) : url;
        URLConnection connection = 
            connectionFactory.createConnection(getProxy(), currentURL);
        connection.setDoOutput(true);

        if (connection instanceof HttpURLConnection) {
            HttpURLConnection hc = (HttpURLConnection)connection;
            hc.setRequestMethod("POST");
        }

        connection.setConnectTimeout((int)getClient().getConnectionTimeout());
        connection.setReadTimeout((int)getClient().getReceiveTimeout());

        connection.setUseCaches(false);
        if (connection instanceof HttpURLConnection) {
            HttpURLConnection hc = (HttpURLConnection)connection;
            if (getClient().isAutoRedirect()) {
                //cannot use chunking if autoredirect as the request will need to be
                //completely cached locally and resent to the redirect target
                hc.setInstanceFollowRedirects(true);
            } else {
                hc.setInstanceFollowRedirects(false);
                if (getClient().isAllowChunking()) {
                    hc.setChunkedStreamingMode(2048);
                }
            }
        }
        message.put(HTTP_CONNECTION, connection);
        setPolicies(message, headers);
     
        message.setContent(OutputStream.class,
                           new WrappedOutputStream(message, connection));
    }

    /**
     * @return the reference associated with the target Destination
     */    
    public EndpointReferenceType getTarget() {
        return target;
    }
    
    /**
     * Retreive the back-channel Destination.
     * 
     * @return the backchannel Destination (or null if the backchannel is
     * built-in)
     */
    public synchronized Destination getBackChannel() {
        if (decoupledDestination == null
            && getConfiguredDecoupledEndpoint() != null) {
            decoupledDestination = setUpDecoupledDestination(); 
        }
        return decoupledDestination;
    }
    
    public void close(Message msg) throws IOException {
        msg.getContent(OutputStream.class).close();        
    }

    /**
     * Close the conduit
     */
    public void close() {
        if (url != null) {
            try {
                URLConnection connect = url.openConnection();
                if (connect instanceof HttpURLConnection) {
                    ((HttpURLConnection)connect).disconnect();
                }
            } catch (IOException ex) {
                //ignore
            }
            url = null;
        }
    
        // in decoupled case, close response Destination if reference count
        // hits zero
        //
        if (decoupledURL != null && decoupledEngine != null) {
            DecoupledHandler decoupledHandler = 
                (DecoupledHandler)decoupledEngine.getServant(decoupledURL);
            if (decoupledHandler != null) {
                decoupledHandler.release();
            }
        }
    }

    /**
     * @return the encapsulated URL
     */
    protected URL getURL() {
        return url;
    }
    
    /**
     * @return default URLConnectionFactory
     */
    private URLConnectionFactory getDefaultConnectionFactory() {
        return new URLConnectionFactory() {
            public URLConnection createConnection(Proxy proxy, URL u)
                throws IOException {
                return getProxy() != null 
                        ? u.openConnection(proxy)
                        : u.openConnection();
            }
        };
    }
    
    /**
     * Get the target reference which may be constructor-provided or 
     * configured.
     * 
     * @param t the constructor-provider target
     * @return the actual target
     */
    private EndpointReferenceType getTargetReference(EndpointReferenceType t) {
        EndpointReferenceType ref = null;
        if (null == t) {
            ref = new EndpointReferenceType();
            AttributedURIType address = new AttributedURIType();
            address.setValue(getAddress());
            ref.setAddress(address);
        } else {
            ref = t;
        }
        return ref;
    }

    /**
     * Ensure an initial set of header is availbale on the outbound message.
     * 
     * @param message the outbound message
     * @return the headers
     */
    private Map<String, List<String>> setHeaders(Message message) {
        Map<String, List<String>> headers =
            CastUtils.cast((Map<?, ?>)message.get(Message.PROTOCOL_HEADERS));
        if (null == headers) {
            headers = new HashMap<String, List<String>>();
            message.put(Message.PROTOCOL_HEADERS, headers);
        }
        return headers;
    }
    
    /**
     * Flush the headers onto the output stream.
     * 
     * @param message the outbound message
     * @throws IOException
     */
    protected void flushHeaders(Message message) throws IOException {
        Map<String, List<String>> headers = 
            CastUtils.cast((Map<?, ?>)message.get(Message.PROTOCOL_HEADERS));
        URLConnection connection = (URLConnection)message.get(HTTP_CONNECTION);
        if (null != headers) {
            for (String header : headers.keySet()) {
                List<String> headerList = headers.get(header);
                for (String value : headerList) {
                    connection.addRequestProperty(header, value);
                }
            }
        }
    }
   
    /**
     * Retrieve the respons code.
     * 
     * @param connection the URLConnection
     * @return the response code
     * @throws IOException
     */
    private int getResponseCode(URLConnection connection) throws IOException {
        int responseCode = HttpURLConnection.HTTP_OK;
        if (connection instanceof HttpURLConnection) {
            HttpURLConnection hc = (HttpURLConnection)connection;
            responseCode = hc.getResponseCode();
        } else {
            if (connection.getHeaderField(Message.RESPONSE_CODE) != null) {
                responseCode =
                    Integer.parseInt(connection.getHeaderField(Message.RESPONSE_CODE));
            }
        }
        return responseCode;
    }

    /**
     * Set up the decoupled Destination if necessary.
     * 
     * @return an appropriate decoupled Destination
     */
    private DecoupledDestination setUpDecoupledDestination() {        
        EndpointReferenceType reference =
            EndpointReferenceUtils.getEndpointReference(getConfiguredDecoupledEndpoint());
        if (reference != null) {
            String decoupledAddress = reference.getAddress().getValue();
            LOG.info("creating decoupled endpoint: " + decoupledAddress);
            try {
                decoupledURL = new URL(decoupledAddress);
                if (decoupledEngine == null) {
                    decoupledEngine = 
                        JettyHTTPServerEngine.getForPort(bus, 
                                                         decoupledURL.getProtocol(),
                                                         decoupledURL.getPort());
                }
                DecoupledHandler decoupledHandler =
                    (DecoupledHandler)decoupledEngine.getServant(decoupledURL);
                if (decoupledHandler == null) {
                    decoupledHandler = new DecoupledHandler();
                    decoupledEngine.addServant(decoupledURL, decoupledHandler);
                } 
                decoupledHandler.duplicate();
            } catch (Exception e) {
                // REVISIT move message to localizable Messages.properties
                LOG.log(Level.WARNING, "decoupled endpoint creation failed: ", e);
            }
        }
        return new DecoupledDestination(reference, incomingObserver);
    }
    
    /**
     * REVISIT: temporary mechanism to allow decoupled response endpoint
     * to be configured, pending the maturation on the real config model
     */
    private String getConfiguredDecoupledEndpoint() {
        return getClient().getDecoupledEndpoint() != null
               ? getClient().getDecoupledEndpoint()
               : System.getProperty(HTTP_DECOUPLED_ENDPOINT);
    }

    /**
     * Wrapper output stream responsible for flushing headers and handling
     * the incoming HTTP-level response (not necessarily the MEP response).
     */
    private class WrappedOutputStream extends AbstractWrappedOutputStream {
        protected URLConnection connection;
        
        WrappedOutputStream(Message m, URLConnection c) {
            super(m);
            connection = c;
        }

        /**
         * Perform any actions required on stream flush (freeze headers,
         * reset output stream ... etc.)
         */
        protected void doFlush() throws IOException {
            if (!alreadyFlushed()) {
                flushHeaders(outMessage);
                resetOut(connection.getOutputStream(), true);
            }
        }

        /**
         * Perform any actions required on stream closure (handle response etc.)
         */
        protected void doClose() throws IOException {
            handleResponse();
        }
        
        protected void onWrite() throws IOException {
            
        }

        private void handleResponse() throws IOException {
            Exchange exchange = outMessage.getExchange();
            if (exchange != null && exchange.isOneWay()) {
                //oneway operation
                connection.getInputStream().close();
                return;
            }
            Message inMessage = new MessageImpl();
            inMessage.setExchange(exchange);
            InputStream in = null;
            inMessage.put(Message.PROTOCOL_HEADERS, connection.getHeaderFields());
            inMessage.put(Message.RESPONSE_CODE, getResponseCode(connection));
            if (connection instanceof HttpURLConnection) {
                HttpURLConnection hc = (HttpURLConnection)connection;
                in = hc.getErrorStream();
                if (null == in) {
                    in = connection.getInputStream();
                }
            } else {
                in = connection.getInputStream();
            }
            
            inMessage.setContent(InputStream.class, in);
            
            incomingObserver.onMessage(inMessage);
        }
    }

    /**
     * Wrapper output stream responsible for commiting incoming request 
     * containing a decoupled response.
     */
    private class WrapperInputStream extends FilterInputStream {
        HttpRequest request;
        HttpResponse response;
        boolean closed;
        
        WrapperInputStream(InputStream is,
                           HttpRequest req,
                           HttpResponse resp) {
            super(is);
            request = req;
            response = resp;
        }
        
        public void close() throws IOException {
            if (!closed) {
                closed = true;
                response.commit();
                request.setHandled(true);
            }
        }
    }
    
    /**
     * Represented decoupled response endpoint.
     */
    protected class DecoupledDestination implements Destination {
        protected MessageObserver decoupledMessageObserver;
        private EndpointReferenceType address;
        
        DecoupledDestination(EndpointReferenceType ref,
                             MessageObserver incomingObserver) {
            address = ref;
            decoupledMessageObserver = incomingObserver;
        }

        public EndpointReferenceType getAddress() {
            return address;
        }

        public Conduit getBackChannel(Message inMessage,
                                      Message partialResponse,
                                      EndpointReferenceType addr)
            throws IOException {
            // shouldn't be called on decoupled endpoint
            return null;
        }

        public void shutdown() {
            // TODO Auto-generated method stub            
        }

        public synchronized void setMessageObserver(MessageObserver observer) {
            decoupledMessageObserver = observer;
        }
        
        protected synchronized MessageObserver getMessageObserver() {
            return decoupledMessageObserver;
        }
    }

    /**
     * Handles incoming decoupled responses.
     */
    private class DecoupledHandler extends AbstractHttpHandler {
        private int refCount;
                
        synchronized void duplicate() {
            refCount++;
        }
        
        synchronized void release() {
            if (--refCount == 0) {
                decoupledEngine.removeServant(decoupledURL);
                JettyHTTPServerEngine.destroyForPort(decoupledURL.getPort());
            }
        }
        
        public void handle(String pathInContext, 
                           String pathParams,
                           HttpRequest req,
                           HttpResponse resp) throws IOException {
            InputStream responseStream = req.getInputStream();
            Message inMessage = new MessageImpl();
            // disposable exchange, swapped with real Exchange on correlation
            inMessage.setExchange(new ExchangeImpl());
            // REVISIT: how to get response headers?
            //inMessage.put(Message.PROTOCOL_HEADERS, req.getXXX());
            setHeaders(inMessage);
            inMessage.put(Message.RESPONSE_CODE, HttpURLConnection.HTTP_OK);
            InputStream is = new WrapperInputStream(responseStream, req, resp);
            inMessage.setContent(InputStream.class, is);

            try {
                decoupledDestination.getMessageObserver().onMessage(inMessage);    
            } finally {
                is.close();
            }
        }
    }    

    private void init() {
        if (!isSetClient()) {
            setClient(new HTTPClientPolicy());
        }
        if (!isSetAuthorization()) {
            setAuthorization(new AuthorizationPolicy());
        }
        if (!isSetProxyAuthorization()) {
            setProxyAuthorization(new AuthorizationPolicy());
        }
        if (!isSetSslClient()) {
            setSslClient(new SSLClientPolicy());
        }

        List <ConfigurationProvider> providers = getOverwriteProviders();
        if (null == providers) {
            providers = new ArrayList<ConfigurationProvider>();
        }
        ConfigurationProvider p = new ServiceModelHttpConfigurationProvider(endpointInfo, false);
        providers.add(p);
        setOverwriteProviders(providers);
    }

    private String getAddress() {
        return endpointInfo.getAddress();
    }

    private Proxy getProxy() {
        Proxy proxy = null;
        HTTPClientPolicy policy = getClient(); 
        if (policy.isSetProxyServer()) {
            proxy = new Proxy(Proxy.Type.valueOf(policy.getProxyServerType().toString()),
                              new InetSocketAddress(policy.getProxyServer(),
                                                    policy.getProxyServerPort()));
        }
        return proxy;
    }

    private void setPolicies(Message message, Map<String, List<String>> headers) {
        AuthorizationPolicy authPolicy = getAuthorization();
        AuthorizationPolicy newPolicy = message.get(AuthorizationPolicy.class);
        String userName = null;
        String passwd = null;
        if (null != newPolicy) {
            userName = newPolicy.getUserName();
            passwd = newPolicy.getPassword();
        }
        if (userName == null && authPolicy.isSetUserName()) {
            userName = authPolicy.getUserName();
        }
        if (userName != null) {
            if (passwd == null && authPolicy.isSetPassword()) {
                passwd = authPolicy.getPassword();
            }
            userName += ":";
            if (passwd != null) {
                userName += passwd;
            }
            userName = Base64Utility.encode(userName.getBytes());
            headers.put("Authorization",
                        Arrays.asList(new String[] {"Basic " + userName}));
        } else if (authPolicy.isSetAuthorizationType() && authPolicy.isSetAuthorization()) {
            String type = authPolicy.getAuthorizationType();
            type += " ";
            type += authPolicy.getAuthorization();
            headers.put("Authorization",
                        Arrays.asList(new String[] {type}));
        }
        AuthorizationPolicy proxyAuthPolicy = getProxyAuthorization();
        if (proxyAuthPolicy.isSetUserName()) {
            userName = proxyAuthPolicy.getUserName();
            if (userName != null) {
                passwd = "";
                if (proxyAuthPolicy.isSetPassword()) {
                    passwd = proxyAuthPolicy.getPassword();
                }
                userName += ":";
                if (passwd != null) {
                    userName += passwd;
                }
                userName = Base64Utility.encode(userName.getBytes());
                headers.put("Proxy-Authorization",
                            Arrays.asList(new String[] {"Basic " + userName}));
            } else if (proxyAuthPolicy.isSetAuthorizationType() && proxyAuthPolicy.isSetAuthorization()) {
                String type = proxyAuthPolicy.getAuthorizationType();
                type += " ";
                type += proxyAuthPolicy.getAuthorization();
                headers.put("Proxy-Authorization",
                            Arrays.asList(new String[] {type}));
            }
        }
        HTTPClientPolicy policy = getClient();
        if (policy.isSetCacheControl()) {
            headers.put("Cache-Control",
                        Arrays.asList(new String[] {policy.getCacheControl().value()}));
        }
        if (policy.isSetHost()) {
            headers.put("Host",
                        Arrays.asList(new String[] {policy.getHost()}));
        }
        if (policy.isSetConnection()) {
            headers.put("Connection",
                        Arrays.asList(new String[] {policy.getConnection().value()}));
        }
        if (policy.isSetAccept()) {
            headers.put("Accept",
                        Arrays.asList(new String[] {policy.getAccept()}));
        }
        if (policy.isSetAcceptEncoding()) {
            headers.put("Accept-Encoding",
                        Arrays.asList(new String[] {policy.getAcceptEncoding()}));
        }
        if (policy.isSetAcceptLanguage()) {
            headers.put("Accept-Language",
                        Arrays.asList(new String[] {policy.getAcceptLanguage()}));
        }
        if (policy.isSetContentType()) {
            headers.put("Content-Type",
                        Arrays.asList(new String[] {policy.getContentType()}));
        }
        if (policy.isSetCookie()) {
            headers.put("Cookie",
                        Arrays.asList(new String[] {policy.getCookie()}));
        }
        if (policy.isSetBrowserType()) {
            headers.put("BrowserType",
                        Arrays.asList(new String[] {policy.getBrowserType()}));
        }
        if (policy.isSetReferer()) {
            headers.put("Referer",
                        Arrays.asList(new String[] {policy.getReferer()}));
        }
    }
}
