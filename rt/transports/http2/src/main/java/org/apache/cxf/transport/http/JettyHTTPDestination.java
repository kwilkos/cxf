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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.wsdl.Definition;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLWriter;

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
import org.apache.cxf.transport.http.destination.HTTPDestinationConfigBean;
import org.apache.cxf.ws.addressing.EndpointReferenceType;
import org.apache.cxf.wsdl11.ServiceWSDLBuilder;
import org.mortbay.jetty.HttpConnection;
import org.mortbay.jetty.Request;
import org.mortbay.jetty.handler.AbstractHandler;


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
        super(b, ci, endpointInfo);
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
                                                    config.getSslServer());
    }

    /**
     * @return the encapsulated config bean
     */
    protected HTTPDestinationConfigBean getConfig() {
        return config;
    }
    
    /**
     * Activate receipt of incoming messages.
     */
    protected void activate() {
        LOG.log(Level.INFO, "Activating receipt of incoming messages");
        try {
            URL url = new URL(getAddressValue(endpointInfo));
            //The handler is bind with the context, 
            //we need to set the things on on context
            if (contextMatchOnExact()) {
                engine.addServant(url, new AbstractHandler() {
                    public void handle(String target, HttpServletRequest req,
                                       HttpServletResponse resp, int dispatch) throws IOException {
                        //if (target.equals(getName())) {
                        doService(req, resp);
                        //}
                    }
                });
            } else {
                engine.addServant(url, new AbstractHandler() {
                    public void handle(String target,  HttpServletRequest req,
                                       HttpServletResponse resp, int dispatch) throws IOException {
                        //if (target.startsWith(getName())) {
                        doService(req, resp);
                        //}
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
        LOG.log(Level.INFO, "Deactivating receipt of incoming messages");
        engine.removeServant(nurl);        
    }

    /**
     * @param inMessage the incoming message
     * @return the inbuilt backchannel
     */
    protected Conduit getInbuiltBackChannel(Message inMessage) {
        HttpServletResponse response = (HttpServletResponse)inMessage.get(HTTP_RESPONSE);
        return new BackChannelConduit(response);
    }

    /**
     * Mark message as a partial message.
     * 
     * @param partialResponse the partial response message
     * @param the decoupled target
     * @return true iff partial responses are supported
     */
    protected boolean markPartialResponse(Message partialResponse,
                                       EndpointReferenceType decoupledTarget) {
        // setup the outbound message to for 202 Accepted
        partialResponse.put(Message.RESPONSE_CODE, HttpURLConnection.HTTP_ACCEPTED);
        partialResponse.getExchange().put(EndpointReferenceType.class,
                                          decoupledTarget);
        return true;
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
        HttpServletRequest req = (HttpServletRequest)message.get(HTTP_REQUEST);
        //TODO how to deal with the fields        
        for (Enumeration e = req.getHeaderNames(); e.hasMoreElements();) {
            String fname = (String)e.nextElement();
            List<String> values;
            if (headers.containsKey(fname)) {
                values = headers.get(fname);
            } else {
                values = new ArrayList<String>();
                headers.put(HttpHeaderHelper.getHeaderKey(fname), values);
            }
            for (Enumeration e2 = req.getHeaders(fname); e2.hasMoreElements();) {
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
    protected void copyResponseHeaders(Message message, HttpServletResponse response) {
        response.setContentType((String) message.get(Message.CONTENT_TYPE));

        Map<?, ?> headers = (Map<?, ?>)message.get(Message.PROTOCOL_HEADERS);
        if (null != headers) {
            for (Iterator<?> iter = headers.keySet().iterator(); iter.hasNext();) {
                String header = (String)iter.next();
                List<?> headerList = (List<?>)headers.get(header);
                for (Object value : headerList) {
                    response.addHeader(header, (String)value);
                }
            }
        }
    }

    protected void doService(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Request baseRequest = (req instanceof Request) 
            ? (Request)req : HttpConnection.getCurrentConnection().getRequest();
        
        if (config.getServer().isSetRedirectURL()) {
            resp.sendRedirect(config.getServer().getRedirectURL());
            resp.flushBuffer();
            baseRequest.setHandled(true);
            return;
        }

        if ("GET".equals(req.getMethod()) && "wsdl".equalsIgnoreCase(req.getQueryString())) {
            try {

                resp.addHeader(HttpHeaderHelper.CONTENT_TYPE, "text/xml");

                OutputStream os = resp.getOutputStream();

                WSDLWriter wsdlWriter = WSDLFactory.newInstance().newWSDLWriter();
                Definition def = new ServiceWSDLBuilder(endpointInfo.getService()).build();
                wsdlWriter.writeWSDL(def, os);
                resp.getOutputStream().flush();
                resp.flushBuffer();
                baseRequest.setHandled(true);
                return;
            } catch (Exception ex) {

                ex.printStackTrace();
            }
        }

        // REVISIT: service on executor if associated with endpoint
        serviceRequest(req, resp);
    }

    protected void serviceRequest(HttpServletRequest req, HttpServletResponse resp)
        throws IOException {
        Request baseRequest = (req instanceof Request) 
            ? (Request)req : HttpConnection.getCurrentConnection().getRequest();
        try {
            if (LOG.isLoggable(Level.INFO)) {
                LOG.info("Service http request on thread: " + Thread.currentThread());
            }

            MessageImpl inMessage = new MessageImpl();            
            inMessage.put(HTTP_REQUEST, req);
            inMessage.put(HTTP_RESPONSE, resp);            
            inMessage.put(Message.HTTP_REQUEST_METHOD, req.getMethod());
            inMessage.put(Message.PATH_INFO, req.getContextPath() + req.getPathInfo());            
            inMessage.put(Message.QUERY_STRING, req.getQueryString());
            inMessage.put(Message.CONTENT_TYPE, req.getContentType());
            inMessage.setContent(InputStream.class, req.getInputStream());
            if (!StringUtils.isEmpty(getAddressValue(endpointInfo))) {
                inMessage.put(Message.BASE_PATH, new URL(getAddressValue(endpointInfo)).getPath());
            }
            inMessage.put(Message.FIXED_PARAMETER_ORDER, config.isFixedParameterOrder());
            inMessage.put(Message.ASYNC_POST_RESPONSE_DISPATCH, Boolean.TRUE); 
            
            setHeaders(inMessage);

            inMessage.setDestination(this);

            incomingObserver.onMessage(inMessage);

            resp.flushBuffer();
            baseRequest.setHandled(true);
        } finally {
            if (LOG.isLoggable(Level.INFO)) {
                LOG.info("Finished servicing http request on thread: " + Thread.currentThread());
            }
        }
    }

    protected OutputStream flushHeaders(Message outMessage) throws IOException {
        updateResponseHeaders(outMessage);
        Object responseObj = outMessage.get(HTTP_RESPONSE);
        OutputStream responseStream = null;
        if (responseObj instanceof HttpServletResponse) {
            HttpServletResponse response = (HttpServletResponse)responseObj;

            Integer i = (Integer)outMessage.get(Message.RESPONSE_CODE);
            if (i != null) { 
                //removed the error handler things
                int status = i.intValue();
                response.setStatus(status);                
            } else {
                response.setStatus(HttpURLConnection.HTTP_OK);
            }

            copyResponseHeaders(outMessage, response);
            responseStream = response.getOutputStream();

            if (isOneWay(outMessage)) {
                response.flushBuffer();
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

        protected HttpServletResponse response;

        BackChannelConduit(HttpServletResponse resp) {
            response = resp;
        }

        /**
         * Send an outbound message, assumed to contain all the name-value
         * mappings of the corresponding input message (if any). 
         * 
         * @param message the message to be sent.
         */
        @SuppressWarnings("unchecked")
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

        protected HttpServletResponse response;

        WrappedOutputStream(Message m, HttpServletResponse resp) {
            super(m);
            response = resp;
        }

        /**
         * Perform any actions required on stream flush (freeze headers,
         * reset output stream ... etc.)
         */
        protected void doFlush() throws IOException {
            OutputStream responseStream = flushHeaders(outMessage);
            if (null != responseStream && !alreadyFlushed()) {
                resetOut(responseStream, true);
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
                response.flushBuffer();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
