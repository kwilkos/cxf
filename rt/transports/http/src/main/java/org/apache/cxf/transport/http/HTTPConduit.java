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
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.cxf.Bus;
import org.apache.cxf.common.logging.LogUtils;
import org.apache.cxf.common.util.Base64Utility;
import org.apache.cxf.configuration.Configurable;
import org.apache.cxf.configuration.security.AuthorizationPolicy;
import org.apache.cxf.configuration.security.SSLClientPolicy;
import org.apache.cxf.helpers.CastUtils;
import org.apache.cxf.helpers.HttpHeaderHelper;
import org.apache.cxf.io.AbstractWrappedOutputStream;
import org.apache.cxf.message.Exchange;
import org.apache.cxf.message.ExchangeImpl;
import org.apache.cxf.message.Message;
import org.apache.cxf.message.MessageImpl;
import org.apache.cxf.service.model.EndpointInfo;
import org.apache.cxf.transport.AbstractConduit;
import org.apache.cxf.transport.Destination;
import org.apache.cxf.transport.DestinationFactory;
import org.apache.cxf.transport.DestinationFactoryManager;
import org.apache.cxf.transport.MessageObserver;
import org.apache.cxf.transports.http.configuration.HTTPClientPolicy;
import org.apache.cxf.ws.addressing.EndpointReferenceType;
import org.apache.cxf.wsdl.EndpointReferenceUtils;

import static org.apache.cxf.message.Message.DECOUPLED_CHANNEL_MESSAGE;


/**
 * HTTP Conduit implementation.
 */
public class HTTPConduit extends AbstractConduit implements Configurable {   
    public static final String HTTP_CONNECTION = "http.connection";
    private static final Logger LOG = LogUtils.getL7dLogger(HTTPConduit.class);
    
    private final Bus bus;    
    private final URLConnectionFactory alternateConnectionFactory;
    private URLConnectionFactory connectionFactory;
    private URL url;
    
    private Destination decoupledDestination;
    private MessageObserver decoupledObserver;
    private int decoupledDestinationRefCount;
    private EndpointInfo endpointInfo;
    
    // Configuration values
    private HTTPClientPolicy client;
    private AuthorizationPolicy authorization;
    private AuthorizationPolicy proxyAuthorization;
    private SSLClientPolicy sslClient;
    

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
     * @throws IOException
     */
    public HTTPConduit(Bus b,
                       EndpointInfo ei,
                       EndpointReferenceType t,
                       URLConnectionFactory factory) throws IOException {
        super(getTargetReference(ei, t, b));
        bus = b;
        endpointInfo = ei;
        alternateConnectionFactory = factory;

        initConfig();
        
        url = t == null
              ? new URL(endpointInfo.getAddress())
              : new URL(t.getAddress().getValue());
    }


    protected Logger getLogger() {
        return LOG;
    }
    
    protected void retrieveConnectionFactory() {
        connectionFactory = alternateConnectionFactory != null
                            ? alternateConnectionFactory
                            : HTTPTransportFactory.getConnectionFactory(
                                getSslClient());
    }
   
    /**
     * Send an outbound message.
     * 
     * @param message the message to be sent.
     */
    public void send(Message message) throws IOException {
        Map<String, List<String>> headers = setHeaders(message);
        URL currentURL = setupURL(message);        
        URLConnection connection = 
            connectionFactory.createConnection(getProxy(), currentURL);
        connection.setDoOutput(true);        
        //TODO using Message context to deceided HTTP send properties        
        connection.setConnectTimeout((int)getClient().getConnectionTimeout());
        connection.setReadTimeout((int)getClient().getReceiveTimeout());
        connection.setUseCaches(false);
        
        if (connection instanceof HttpURLConnection) {
            String httpRequestMethod = (String)message.get(Message.HTTP_REQUEST_METHOD);
            HttpURLConnection hc = (HttpURLConnection)connection;           
            if (null != httpRequestMethod) {
                hc.setRequestMethod(httpRequestMethod);                 
            } else {
                hc.setRequestMethod("POST");
            }
            if (getClient().isAutoRedirect()) {
                //cannot use chunking if autoredirect as the request will need to be
                //completely cached locally and resent to the redirect target
                hc.setInstanceFollowRedirects(true);
            } else {
                hc.setInstanceFollowRedirects(false);
                if (!hc.getRequestMethod().equals("GET")
                    && getClient().isAllowChunking()) {
                    hc.setChunkedStreamingMode(2048);
                }
            }
        }
        message.put(HTTP_CONNECTION, connection);
        setPolicies(message, headers);
     
        message.setContent(OutputStream.class,
                           new WrappedOutputStream(message, connection));
    }
    
    
    private URL setupURL(Message message) throws MalformedURLException {
        String value = (String)message.get(Message.ENDPOINT_ADDRESS);
        String pathInfo = (String)message.get(Message.PATH_INFO);
        String queryString = (String)message.get(Message.QUERY_STRING);
        
        String result = value != null ? value : url.toString();
        if (null != pathInfo && !result.endsWith(pathInfo)) { 
            result = result + pathInfo;
        }
        if (queryString != null) {
            result = result + "?" + queryString;
        }        
        return new URL(result);    
    }
    
    /**
     * Retreive the back-channel Destination.
     * 
     * @return the backchannel Destination (or null if the backchannel is
     * built-in)
     */
    public synchronized Destination getBackChannel() {
        if (decoupledDestination == null
            &&  getClient().getDecoupledEndpoint() != null) {
            setUpDecoupledDestination(); 
        }
        return decoupledDestination;
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
        if (decoupledDestination != null) {
            releaseDecoupledDestination();
        }
    }

    /**
     * @return the encapsulated URL
     */
    protected URL getURL() {
        return url;
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
        URLConnection connection = (URLConnection)message.get(HTTP_CONNECTION);
        String ct = (String) message.get(Message.CONTENT_TYPE);
        if (null != ct) {
            connection.setRequestProperty(HttpHeaderHelper.CONTENT_TYPE, ct);
        } else {
            connection.setRequestProperty(HttpHeaderHelper.CONTENT_TYPE, "text/xml");
        }
        
        Map<String, List<String>> headers = 
            CastUtils.cast((Map<?, ?>)message.get(Message.PROTOCOL_HEADERS));
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
     */
    private void setUpDecoupledDestination() {        
        EndpointReferenceType reference =
            EndpointReferenceUtils.getEndpointReference(
                getClient().getDecoupledEndpoint());
        if (reference != null) {
            String decoupledAddress = reference.getAddress().getValue();
            LOG.info("creating decoupled endpoint: " + decoupledAddress);
            try {
                decoupledDestination = getDestination(decoupledAddress);
                duplicateDecoupledDestination();
            } catch (Exception e) {
                // REVISIT move message to localizable Messages.properties
                LOG.log(Level.WARNING, "decoupled endpoint creation failed: ", e);
            }
        }
    }

    /**
     * @param address the address
     * @return a Destination for the address
     */
    private Destination getDestination(String address) throws IOException {
        Destination destination = null;
        DestinationFactoryManager factoryManager =
            bus.getExtension(DestinationFactoryManager.class);
        DestinationFactory factory =
            factoryManager.getDestinationFactoryForUri(address);
        if (factory != null) {
            EndpointInfo ei = new EndpointInfo();
            ei.setAddress(address);
            destination = factory.getDestination(ei);
            decoupledObserver = new InterposedMessageObserver();
            destination.setMessageObserver(decoupledObserver);
        }
        return destination;
    }
    
    /**
     * @return the decoupled observer
     */
    protected MessageObserver getDecoupledObserver() {
        return decoupledObserver;
    }
    
    private synchronized void duplicateDecoupledDestination() {
        decoupledDestinationRefCount++;
    }
    
    private synchronized void releaseDecoupledDestination() {
        if (--decoupledDestinationRefCount == 0) {
            LOG.log(Level.INFO, "shutting down decoupled destination");
            decoupledDestination.shutdown();
        }
    }
    
    /**
     * @param exchange the exchange in question
     * @return true iff the exchange indicates a oneway MEP
     */
    private boolean isOneway(Exchange exchange) {
        return exchange != null && exchange.isOneWay();
    }
    
    /**
     * @param connection the connection in question
     * @param responseCode the response code
     * @return true if a partial response is pending on the connection 
     */
    private boolean isPartialResponse(URLConnection connection,
                                      int responseCode) {
        return responseCode == HttpURLConnection.HTTP_ACCEPTED
               && connection.getContentLength() != 0;
    }

    private void initConfig() {
        //Initialize some default values for the configuration
        client = endpointInfo.getTraversedExtensor(new HTTPClientPolicy(), HTTPClientPolicy.class);
        authorization = endpointInfo.getTraversedExtensor(new AuthorizationPolicy(),
                                                          AuthorizationPolicy.class);
        proxyAuthorization = endpointInfo.getTraversedExtensor(new AuthorizationPolicy(),
                                                               AuthorizationPolicy.class);
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
            headers.put(HttpHeaderHelper.CONTENT_TYPE,
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

    public String getBeanName() {
        if (endpointInfo.getName() != null) {
            return endpointInfo.getName().toString() + ".http-conduit";
        }
        return null;
    }

    public AuthorizationPolicy getAuthorization() {
        return authorization;
    }

    public void setAuthorization(AuthorizationPolicy authorization) {
        this.authorization = authorization;
    }

    public HTTPClientPolicy getClient() {
        return client;
    }

    public void setClient(HTTPClientPolicy client) {
        this.client = client;
    }

    public AuthorizationPolicy getProxyAuthorization() {
        return proxyAuthorization;
    }

    public void setProxyAuthorization(AuthorizationPolicy proxyAuthorization) {
        this.proxyAuthorization = proxyAuthorization;
    }

    public SSLClientPolicy getSslClient() {
        return sslClient;
    }

    public void setSslClient(SSLClientPolicy sslClient) {
        this.sslClient = sslClient;
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
                if (connection instanceof HttpURLConnection) {            
                    HttpURLConnection hc = (HttpURLConnection)connection;                    
                    if (hc.getRequestMethod().equals("GET")) {
                        return;
                    }
                }
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
            int responseCode = getResponseCode(connection);
            if (isOneway(exchange)
                && !isPartialResponse(connection, responseCode)) {
                // oneway operation without partial response
                connection.getInputStream().close();
                return;
            }
            
            Message inMessage = new MessageImpl();
            inMessage.setExchange(exchange);
            InputStream in = null;
            Map<String, List<String>> headers = new HashMap<String, List<String>>();
            for (String key : connection.getHeaderFields().keySet()) {
                headers.put(HttpHeaderHelper.getHeaderKey(key), connection.getHeaderFields().get(key));
            }
            inMessage.put(Message.PROTOCOL_HEADERS, headers);
            inMessage.put(Message.RESPONSE_CODE, responseCode);
            inMessage.put(Message.CONTENT_TYPE, connection.getHeaderField(HttpHeaderHelper.CONTENT_TYPE));

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
     * Used to set appropriate message properties, exchange etc.
     * as required for an incoming decoupled response (as opposed
     * what's normally set by the Destination for an incoming
     * request).
     */
    protected class InterposedMessageObserver implements MessageObserver {
        /**
         * Called for an incoming message.
         * 
         * @param inMessage
         */
        public void onMessage(Message inMessage) {
            // disposable exchange, swapped with real Exchange on correlation
            inMessage.setExchange(new ExchangeImpl());
            inMessage.put(DECOUPLED_CHANNEL_MESSAGE, Boolean.TRUE);
            // REVISIT: how to get response headers?
            //inMessage.put(Message.PROTOCOL_HEADERS, req.getXXX());
            setHeaders(inMessage);
            inMessage.put(Message.RESPONSE_CODE, HttpURLConnection.HTTP_OK);

            // remove server-specific properties
            inMessage.remove(AbstractHTTPDestination.HTTP_REQUEST);
            inMessage.remove(AbstractHTTPDestination.HTTP_RESPONSE);
            inMessage.remove(Message.ASYNC_POST_RESPONSE_DISPATCH);

            incomingObserver.onMessage(inMessage);
        }
    }
    
}

