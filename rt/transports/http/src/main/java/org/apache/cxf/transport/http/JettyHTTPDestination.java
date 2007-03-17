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
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.cxf.Bus;
import org.apache.cxf.common.logging.LogUtils;
import org.apache.cxf.common.util.StringUtils;
import org.apache.cxf.message.Message;
import org.apache.cxf.message.MessageImpl;
import org.apache.cxf.service.model.EndpointInfo;
import org.apache.cxf.transport.ConduitInitiator;
import org.apache.cxf.transport.https.SSLUtils;
import org.apache.cxf.transports.http.QueryHandler;
import org.apache.cxf.transports.http.QueryHandlerRegistry;
import org.mortbay.jetty.HttpConnection;
import org.mortbay.jetty.Request;

public class JettyHTTPDestination extends AbstractHTTPDestination {
    
    private static final Logger LOG = LogUtils.getL7dLogger(JettyHTTPDestination.class);

    protected ServerEngine engine;
    protected ServerEngine alternateEngine;

    /**
     * Constructor, using Jetty server engine.
     * 
     * @param b the associated Bus
     * @param ci the associated conduit initiator
     * @param endpointInfo the endpoint info of the destination
     * @throws IOException
     */
    public JettyHTTPDestination(Bus b, ConduitInitiator ci, EndpointInfo endpointInfo) throws IOException {
        this(b, ci, endpointInfo, null);
    }

    /**
     * Constructor, allowing subsititution of server engine.
     * 
     * @param b the associated Bus
     * @param ci the associated conduit initiator
     * @param endpointInfo the endpoint info of the destination
     * @param eng the server engine
     * @throws IOException
     */
    public JettyHTTPDestination(Bus b, ConduitInitiator ci, EndpointInfo endpointInfo, ServerEngine eng)
        throws IOException {
        //Add the defualt port if the address is missing it
        super(b, ci, endpointInfo, true);
        alternateEngine = eng;
    }

    protected Logger getLogger() {
        return LOG;
    }
    
    /**
     * Post-configure retreival of server engine.
     */
    protected void retrieveEngine() {
        engine = alternateEngine != null
                 ? alternateEngine
                 : JettyHTTPServerEngine.getForPort(bus,
                                                    nurl.getProtocol(),
                                                    nurl.getPort(),
                                                    getSslServer());
    }
    
    /**
     * Activate receipt of incoming messages.
     */
    protected void activate() {
        LOG.log(Level.FINE, "Activating receipt of incoming messages");
        try {
            URL url = new URL(endpointInfo.getAddress());
            engine.addServant(url, new JettyHTTPHandler(this, contextMatchOnExact()));
            
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
        if (queryHandlerRegistry != null) { 
            for (QueryHandler qh : queryHandlerRegistry.getHandlers()) {
                String requestURL = req.getPathInfo() + "?" + req.getQueryString();                
                if (qh.isRecognizedQuery(requestURL, endpointInfo)) {
                    resp.setContentType(qh.getResponseContentType(requestURL));
                    qh.writeResponse(requestURL, endpointInfo, resp.getOutputStream());
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
   
}
