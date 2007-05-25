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
import java.security.Principal;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.cxf.Bus;
import org.apache.cxf.common.logging.LogUtils;
import org.apache.cxf.common.util.StringUtils;
import org.apache.cxf.configuration.jsse.TLSServerParameters;
import org.apache.cxf.configuration.security.SSLServerPolicy;
import org.apache.cxf.message.Message;
import org.apache.cxf.message.MessageImpl;
import org.apache.cxf.security.SecurityContext;
import org.apache.cxf.service.model.EndpointInfo;
import org.apache.cxf.transport.ConduitInitiator;
import org.apache.cxf.transport.http.AbstractHTTPDestination;
import org.apache.cxf.transport.https.SSLUtils;
import org.apache.cxf.transports.http.QueryHandler;
import org.apache.cxf.transports.http.QueryHandlerRegistry;
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
    protected void retrieveEngine() {
        if (this.getTlsServerParameters() != null) {
            if (!"https".equals(nurl.getProtocol())) {
                throw new RuntimeException(
                        "Wrong protocol for TLS configuration: proto: " 
                        + nurl.getProtocol());
            }
            // If the previous engine was "https", we have to shut it down as
            // it cannot be reconfigured.
            if (engine != null 
                && "https".equals(engine.getProtocol())
                && nurl.getPort() == engine.getPort()) {
                engine.shutdown();
            }
            engine = serverEngineFactory.getForPort(
                                 nurl.getProtocol(),
                                 nurl.getPort(),
                                 getTlsServerParameters());
        // TODO: Remove when old SSL config is gone
        } else if (this.getSslServer() != null) {
            if (!"https".equals(nurl.getProtocol())) {
                throw new RuntimeException(
                        "Wrong protocol for TLS configuration: proto: " 
                        + nurl.getProtocol());
            }
            // If the previous engine was "https", we have to shut it down as
            // it cannot be reconfigured.
            if (engine != null 
                && "https".equals(engine.getProtocol())
                && nurl.getPort() == engine.getPort()) {
                engine.shutdown();
            }
            engine = serverEngineFactory.getForPort(nurl.getProtocol(),
                                                nurl.getPort(),
                                                getSslServer());
        } else {
            // We may still have "https", but we might still get the configuration from
            // http-listener.

            // If the previous engine was "https", we have to shut it down as
            // it cannot be reconfigured.
            if (engine != null && "https".equals(nurl.getPort())
                && "https".equals(engine.getProtocol())
                && nurl.getPort() == engine.getPort()) {
                engine.shutdown();
            }
            // This should throw an exception if TLS is not configured 
            // for http-listener and the protocol is "https".
            engine = serverEngineFactory.getForPort(nurl.getProtocol(),
                                                nurl.getPort());
        }
        assert engine != null;
    }
    
    /**
     * This method is used to finalize the configuration
     * after the configuration items have been set.
     *
     */
    public void finalizeConfig() {
        retrieveEngine();
        configFinalized = true;
    }
    
    /**
     * This method sets the SSLServerPolicy for this destination. Changing
     * the SSLServerPolicy object internally will not affect this destination.
     * This method must be called to reconfigure the Destination.
     * 
     * @param policy
     */
    @Deprecated
    @Override
    public void setSslServer(SSLServerPolicy policy) {
        super.setSslServer(policy);
        if (configFinalized) {
            deactivate();
            engine.shutdown();
            engine = null;
            retrieveEngine();
        }
    }

    /**
     * This method sets the TLS Server Parameters for this destination. 
     * Changing the TLSServerParameters object internally will not affect this 
     * destination.
     * This method must be called to reconfigure the Destination.
     * 
     * @param params
     */
    @Override
    public void setTlsServerParameters(TLSServerParameters params) {
        super.setTlsServerParameters(params);
        if (configFinalized) {
            deactivate();
            engine.shutdown();
            engine = null;
            retrieveEngine();
        }
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

    private synchronized void updateEndpointAddress(String addr) {
        // only update the EndpointAddress if the base path is equal
        // make sure we don't broke the get operation?parament query 
        String address = endpointInfo.getAddress();
        if (getBasePath(address).equals(getBasePath(addr))) {
            endpointInfo.setAddress(addr);
        }
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
            String requestURL = req.getRequestURL() + "?" + req.getQueryString();
            String pathInfo = req.getPathInfo();                     
            for (QueryHandler qh : queryHandlerRegistry.getHandlers()) {
                if (qh.isRecognizedQuery(requestURL, pathInfo, endpointInfo)) {
                    //replace the endpointInfo address with request url only for get wsdl           
                    updateEndpointAddress(req.getRequestURL().toString());   
                    resp.setContentType(qh.getResponseContentType(requestURL, pathInfo));
                    qh.writeResponse(requestURL, pathInfo, endpointInfo, resp.getOutputStream());
                    resp.getOutputStream().flush();                     
                    baseRequest.setHandled(true);
                    return;
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
        transportFactory.destinations.remove(endpointInfo.getAddress());
        
        super.shutdown();
    }
   
}
