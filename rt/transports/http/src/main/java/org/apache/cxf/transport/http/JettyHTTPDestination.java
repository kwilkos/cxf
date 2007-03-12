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
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.cxf.Bus;
import org.apache.cxf.common.logging.LogUtils;
import org.apache.cxf.common.util.StringUtils;
import org.apache.cxf.helpers.HttpHeaderHelper;
import org.apache.cxf.io.AbstractWrappedOutputStream;
import org.apache.cxf.message.Message;
import org.apache.cxf.message.MessageImpl;
import org.apache.cxf.service.model.EndpointInfo;
import org.apache.cxf.transport.AbstractDestination;
import org.apache.cxf.transport.Conduit;
import org.apache.cxf.transport.ConduitInitiator;
import org.apache.cxf.transport.https.SSLUtils;
import org.apache.cxf.transports.http.QueryHandler;
import org.apache.cxf.transports.http.QueryHandlerRegistry;
import org.mortbay.http.HttpRequest;
import org.mortbay.http.HttpResponse;
import org.mortbay.http.handler.AbstractHttpHandler;

public class JettyHTTPDestination extends AbstractHTTPDestination {
   
    public static final String HTTP_REQUEST = JettyHTTPDestination.class.getName() + ".REQUEST";
    public static final String HTTP_RESPONSE = JettyHTTPDestination.class.getName() + ".RESPONSE";
    
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
            if (contextMatchOnExact()) {
                engine.addServant(url, new AbstractHttpHandler() {
                    public void handle(String pathInContext, String pathParams, HttpRequest req,
                                       HttpResponse resp) throws IOException {
                        if (pathInContext.equals(getName())) {
                            doService(req, resp);
                        }
                    }
                });
            } else {
                engine.addServant(url, new AbstractHttpHandler() {
                    public void handle(String pathInContext, String pathParams, HttpRequest req,
                                       HttpResponse resp) throws IOException {                            
                        if (pathInContext.startsWith(getName())) {
                            doService(req, resp);
                        }
                    }
                });
            }
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
     * @param inMessage the incoming message
     * @return the inbuilt backchannel
     */
    protected Conduit getInbuiltBackChannel(Message inMessage) {
        HttpResponse response = (HttpResponse)inMessage.get(HTTP_RESPONSE);
        return new BackChannelConduit(response);
    }
   

    /**
     * @return the associated conduit initiator
     */
    protected ConduitInitiator getConduitInitiator() {
        return conduitInitiator;
    }

    /**
     * Copy the request headers into the message.
     * 
     * @param message the current message
     * @param headers the current set of headers
     */
    protected void copyRequestHeaders(Message message, Map<String, List<String>> headers) {
        HttpRequest req = (HttpRequest)message.get(HTTP_REQUEST);
        for (Enumeration e = req.getFieldNames(); e.hasMoreElements();) {
            String fname = (String)e.nextElement();
            List<String> values;
            if (headers.containsKey(fname)) {
                values = headers.get(fname);
            } else {
                values = new ArrayList<String>();
                headers.put(HttpHeaderHelper.getHeaderKey(fname), values);
            }
            for (Enumeration e2 = req.getFieldValues(fname); e2.hasMoreElements();) {
                String val = (String)e2.nextElement();
                values.add(val);
            }
        }
    }

    /**
     * Copy the response headers into the response.
     * 
     * @param message the current message
     * @param headers the current set of headers
     */
    protected void copyResponseHeaders(Message message, HttpResponse response) {
        Map<?, ?> headers = (Map<?, ?>)message.get(Message.PROTOCOL_HEADERS);
        if (null != headers) {
            
            if (!headers.containsKey(Message.CONTENT_TYPE)) {
                response.setContentType((String) message.get(Message.CONTENT_TYPE));
            }
            
            for (Iterator<?> iter = headers.keySet().iterator(); iter.hasNext();) {
                String header = (String)iter.next();
                List<?> headerList = (List<?>)headers.get(header);
                for (Object value : headerList) {
                    response.addField(header, (String)value);
                }
            }
        } else {
            response.setContentType((String) message.get(Message.CONTENT_TYPE));
        }
    }

    protected void doService(HttpRequest req, HttpResponse resp) throws IOException {
        if (getServer().isSetRedirectURL()) {
            resp.sendRedirect(getServer().getRedirectURL());
            resp.commit();
            req.setHandled(true);
            return;
        }
        QueryHandlerRegistry queryHandlerRegistry = bus.getExtension(QueryHandlerRegistry.class);
        if (queryHandlerRegistry != null) { 
            for (QueryHandler qh : queryHandlerRegistry.getHandlers()) {
                if (qh.isRecognizedQuery(req.getURI().toString(), endpointInfo)) {
                    if (resp.getField(HttpHeaderHelper.CONTENT_TYPE) == null) {
                        resp.addField(HttpHeaderHelper.CONTENT_TYPE, 
                                      qh.getResponseContentType(req.getURI().toString()));
                    }
                    qh.writeResponse(req.getURI().toString(), endpointInfo, resp.getOutputStream());
                    resp.getOutputStream().flush(); 
                    resp.commit();
                    req.setHandled(true);
                    return;
                }
            }
        }

        // REVISIT: service on executor if associated with endpoint
        serviceRequest(req, resp);
    }

    protected void serviceRequest(final HttpRequest req, final HttpResponse resp)
        throws IOException {
        try {
            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine("Service http request on thread: " + Thread.currentThread());
            }

            MessageImpl inMessage = new MessageImpl();
            inMessage.setContent(InputStream.class, req.getInputStream());
            inMessage.put(HTTP_REQUEST, req);
            inMessage.put(HTTP_RESPONSE, resp);
            inMessage.put(Message.HTTP_REQUEST_METHOD, req.getMethod());
            inMessage.put(Message.PATH_INFO, req.getPath());
            inMessage.put(Message.QUERY_STRING, req.getQuery());
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

            resp.commit();
            req.setHandled(true);
        } finally {
            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine("Finished servicing http request on thread: " + Thread.currentThread());
            }
        }
    }

    protected OutputStream flushHeaders(Message outMessage) throws IOException {
        updateResponseHeaders(outMessage);
        Object responseObj = outMessage.get(HTTP_RESPONSE);
        OutputStream responseStream = null;
        if (responseObj instanceof HttpResponse) {
            HttpResponse response = (HttpResponse)responseObj;

            Integer i = (Integer)outMessage.get(Message.RESPONSE_CODE);
            if (i != null) {
                int status = i.intValue();
                /*if (status == HttpURLConnection.HTTP_INTERNAL_ERROR) {
                    response.setStatus(status, "Fault Occurred");
                } else if (status == HttpURLConnection.HTTP_ACCEPTED) {
                    response.setStatus(status, "Accepted");
                } else {
                    response.setStatus(status);
                }*/
                response.setStatus(status);
            } else {
                response.setStatus(HttpURLConnection.HTTP_OK);
            }

            copyResponseHeaders(outMessage, response);
            responseStream = response.getOutputStream();

            if (isOneWay(outMessage)) {
                response.commit();
            }
        } else if (null != responseObj) {
            String m = (new org.apache.cxf.common.i18n.Message("UNEXPECTED_RESPONSE_TYPE_MSG",
                LOG, responseObj.getClass())).toString();
            LOG.log(Level.WARNING, m);
            throw new IOException(m);   
        } else {
            String m = (new org.apache.cxf.common.i18n.Message("NULL_RESPONSE_MSG", LOG)).toString();
            LOG.log(Level.WARNING, m);
            throw new IOException(m);            
        }

        if (isOneWay(outMessage)) {
            outMessage.remove(HTTP_RESPONSE);
        }
        return responseStream;
    }

    /**
     * Backchannel conduit.
     */
    protected class BackChannelConduit 
        extends AbstractDestination.AbstractBackChannelConduit {

        protected HttpResponse response;

        BackChannelConduit(HttpResponse resp) {
            response = resp;
        }

        /**
         * Send an outbound message, assumed to contain all the name-value
         * mappings of the corresponding input message (if any). 
         * 
         * @param message the message to be sent.
         */
        public void send(Message message) throws IOException {
            message.put(HTTP_RESPONSE, response);
            message.setContent(OutputStream.class, new WrappedOutputStream(message, response));
        }
    }

    /**
     * Wrapper stream responsible for flushing headers and committing outgoing
     * HTTP-level response.
     */
    private class WrappedOutputStream extends AbstractWrappedOutputStream {

        protected HttpResponse response;

        WrappedOutputStream(Message m, HttpResponse resp) {
            super(m);
            response = resp;
        }

        /**
         * Perform any actions required on stream flush (freeze headers,
         * reset output stream ... etc.)
         */
        protected void doFlush() throws IOException {
            if (!alreadyFlushed()) {
                OutputStream responseStream = flushHeaders(outMessage);
                if (null != responseStream) {
                    resetOut(responseStream, true);
                }
            }
        }

        /**
         * Perform any actions required on stream closure (handle response etc.)
         */
        protected void doClose() {
            commitResponse();
        }

        protected void onWrite() throws IOException {
        }

        private void commitResponse() {
            try {
                response.commit();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
