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
package org.apache.cxf.transport.http_jetty;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.security.Principal;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.cxf.Bus;
import org.apache.cxf.common.logging.LogUtils;
import org.apache.cxf.common.util.StringUtils;
import org.apache.cxf.helpers.HttpHeaderHelper;
import org.apache.cxf.message.ExchangeImpl;
import org.apache.cxf.message.Message;
import org.apache.cxf.message.MessageImpl;
import org.apache.cxf.security.SecurityContext;
import org.apache.cxf.service.model.EndpointInfo;
import org.apache.cxf.transport.ConduitInitiator;
import org.apache.cxf.transport.http.AbstractHTTPDestination;
import org.apache.cxf.transport.http.HTTPSession;
import org.apache.cxf.transport.https.SSLUtils;
import org.apache.cxf.transports.http.QueryHandler;
import org.apache.cxf.transports.http.QueryHandlerRegistry;
import org.apache.cxf.transports.http.StemMatchingQueryHandler;
import org.mortbay.jetty.HttpConnection;
import org.mortbay.jetty.Request;

public class JettyHTTPDestination extends AbstractHTTPDestination {
    
    private static final Logger LOG =
        LogUtils.getL7dLogger(JettyHTTPDestination.class);

    
    protected JettyHTTPServerEngine engine;
    protected JettyHTTPTransportFactory transportFactory;
    protected JettyHTTPServerEngineFactory serverEngineFactory;
    protected URL nurl;
    
    /**
     * This variable signifies that finalizeConfig() has been called.
     * It gets called after this object has been spring configured.
     * It is used to automatically reinitialize things when resources
     * are reset, such as setTlsServerParameters().
     */
    private boolean configFinalized;
     
    /**
     * Constructor, using Jetty server engine.
     * 
     * @param b the associated Bus
     * @param ci the associated conduit initiator
     * @param endpointInfo the endpoint info of the destination
     * @throws IOException
     */
    public JettyHTTPDestination(
            Bus                       b,
            JettyHTTPTransportFactory ci, 
            EndpointInfo              endpointInfo
    ) throws IOException {
        //Add the defualt port if the address is missing it
        super(b, ci, endpointInfo, true);
        this.transportFactory = ci;
        this.serverEngineFactory = ci.getJettyHTTPServerEngineFactory();
        nurl = new URL(endpointInfo.getAddress());
    }

    protected Logger getLogger() {
        return LOG;
    }
    
    /**
     * Post-configure retreival of server engine.
     */
    protected void retrieveEngine()
        throws GeneralSecurityException, 
               IOException {
        
        engine = 
            serverEngineFactory.retrieveJettyHTTPServerEngine(nurl.getPort());
        if (engine == null) {
            engine = serverEngineFactory.
                createJettyHTTPServerEngine(nurl.getPort(), nurl.getProtocol());
        }
        
        assert engine != null;
        
        // When configuring for "http", however, it is still possible that
        // Spring configuration has configured the port for https. 
        if (!nurl.getProtocol().equals(engine.getProtocol())) {
            throw new IllegalStateException(
                "Port " + engine.getPort() 
                + " is configured with wrong protocol \"" 
                + engine.getProtocol()
                + "\" for \"" + nurl + "\"");
        }
    }
    
    /**
     * This method is used to finalize the configuration
     * after the configuration items have been set.
     *
     */
    public void finalizeConfig() 
        throws GeneralSecurityException,
               IOException {
        
        assert !configFinalized;
        
        retrieveEngine();
        configFinalized = true;
    }
    
    /**
     * Activate receipt of incoming messages.
     */
    protected void activate() {
        LOG.log(Level.FINE, "Activating receipt of incoming messages");
        try {
            URL url = new URL(endpointInfo.getAddress());
            engine.addServant(url, 
                    new JettyHTTPHandler(this, contextMatchOnExact()));
            
        } catch (Exception e) {
            LOG.log(Level.WARNING, "URL creation failed: ", e);
        }
    }

    /**
     * Deactivate receipt of incoming messages.
     */
    protected void deactivate() {
        LOG.log(Level.FINE, "Deactivating receipt of incoming messages");
        engine.removeServant(nurl);   
    }   
     

    /**
     * @return the associated conduit initiator
     */
    protected ConduitInitiator getConduitInitiator() {
        return conduitInitiator;
    }
    
    private String getBasePath(String addr) {
        try {
            return new URL(addr).getPath();
        } catch (MalformedURLException e) {
            return null;
        }
    }

    private String removeTrailingSeparator(String addr) {
        if (addr.lastIndexOf('/') == addr.length() - 1) {
            return addr.substring(0, addr.length() - 1);
        } else {
            return addr;
        }
    }
    private synchronized String updateEndpointAddress(String addr) {
        // only update the EndpointAddress if the base path is equal
        // make sure we don't broke the get operation?parament query 
        String address = removeTrailingSeparator(endpointInfo.getAddress());
        if (getBasePath(address).equals(removeTrailingSeparator(getStem(getBasePath(addr))))) {
            endpointInfo.setAddress(addr);
        }
        return address;
    }
   
    protected void doService(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Request baseRequest = (req instanceof Request) 
            ? (Request)req : HttpConnection.getCurrentConnection().getRequest();
            
        if (getServer().isSetRedirectURL()) {
            resp.sendRedirect(getServer().getRedirectURL());
            resp.flushBuffer();
            baseRequest.setHandled(true);
            return;
        }
        QueryHandlerRegistry queryHandlerRegistry = bus.getExtension(QueryHandlerRegistry.class);
        
        if (null != req.getQueryString() && queryHandlerRegistry != null) {   
            String reqAddr = req.getRequestURL().toString();
            String requestURL =  reqAddr + "?" + req.getQueryString();
            String pathInfo = req.getPathInfo();                     
            for (QueryHandler qh : queryHandlerRegistry.getHandlers()) {
                boolean recognized =
                    qh instanceof StemMatchingQueryHandler
                    ? ((StemMatchingQueryHandler)qh).isRecognizedQuery(requestURL,
                                                                       pathInfo,
                                                                       endpointInfo,
                                                                       contextMatchOnExact())
                    : qh.isRecognizedQuery(requestURL, pathInfo, endpointInfo);
                if (recognized) {
                    //replace the endpointInfo address with request url only for get wsdl   
                    synchronized (endpointInfo) {
                        String oldAddress = updateEndpointAddress(reqAddr);   
                        resp.setContentType(qh.getResponseContentType(requestURL, pathInfo));
                        try {
                            qh.writeResponse(requestURL, pathInfo, endpointInfo, resp.getOutputStream());
                        } catch (Exception ex) {
                            LOG.log(Level.WARNING, "writeResponse failed: ", ex);
                        }
                        endpointInfo.setAddress(oldAddress);
                        resp.getOutputStream().flush();                     
                        baseRequest.setHandled(true);
                        return;    
                    }
                    
                }
            }
        }

        // REVISIT: service on executor if associated with endpoint
        serviceRequest(req, resp);
    }

    protected void serviceRequest(final HttpServletRequest req, final HttpServletResponse resp)
        throws IOException {
        Request baseRequest = (req instanceof Request) 
            ? (Request)req : HttpConnection.getCurrentConnection().getRequest();
        try {
            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine("Service http request on thread: " + Thread.currentThread());
            }

            MessageImpl inMessage = new MessageImpl();
            inMessage.setContent(InputStream.class, req.getInputStream());
            inMessage.put(HTTP_REQUEST, req);
            inMessage.put(HTTP_RESPONSE, resp);
            inMessage.put(Message.HTTP_REQUEST_METHOD, req.getMethod());
            inMessage.put(Message.PATH_INFO, req.getContextPath() + req.getPathInfo());
            inMessage.put(Message.ENCODING, HttpHeaderHelper.mapCharset(req.getCharacterEncoding()));
            inMessage.put(Message.QUERY_STRING, req.getQueryString());
            inMessage.put(Message.CONTENT_TYPE, req.getContentType());
            if (!StringUtils.isEmpty(endpointInfo.getAddress())) {
                inMessage.put(Message.BASE_PATH, new URL(endpointInfo.getAddress()).getPath());
            }
            inMessage.put(Message.FIXED_PARAMETER_ORDER, isFixedParameterOrder());
            inMessage.put(Message.ASYNC_POST_RESPONSE_DISPATCH, Boolean.TRUE);
            inMessage.put(SecurityContext.class, new SecurityContext() {
                public Principal getUserPrincipal() {
                    return req.getUserPrincipal();
                }
                public boolean isUserInRole(String role) {
                    return req.isUserInRole(role);
                }
            });
            
            setHeaders(inMessage);
            inMessage.setDestination(this);
            
            SSLUtils.propogateSecureSession(req, inMessage);

            ExchangeImpl exchange = new ExchangeImpl();
            exchange.setInMessage(inMessage);
            exchange.setSession(new HTTPSession(req));
            
            incomingObserver.onMessage(inMessage);

            resp.flushBuffer();
            baseRequest.setHandled(true);
        } finally {
            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine("Finished servicing http request on thread: " + Thread.currentThread());
            }
        }
    }

    @Override
    public void shutdown() {
        transportFactory.removeDestination(endpointInfo);
        
        super.shutdown();
    }
    
    public ServerEngine getEngine() {
        return engine;
    }
   
    private String getStem(String baseURI) {    
        return baseURI.substring(0, baseURI.lastIndexOf("/"));
    }
}
