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

import javax.wsdl.Definition;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLWriter;

import org.apache.cxf.Bus;
import org.apache.cxf.common.util.StringUtils;
import org.apache.cxf.helpers.HttpHeaderHelper;
import org.apache.cxf.io.AbstractWrappedOutputStream;
import org.apache.cxf.message.Exchange;
import org.apache.cxf.message.Message;
import org.apache.cxf.message.MessageImpl;
import org.apache.cxf.service.model.EndpointInfo;
import org.apache.cxf.transport.Conduit;
import org.apache.cxf.transport.ConduitInitiator;
import org.apache.cxf.transport.Destination;
import org.apache.cxf.transport.MessageObserver;
import org.apache.cxf.ws.addressing.EndpointReferenceType;
import org.apache.cxf.wsdl.EndpointReferenceUtils;
import org.apache.cxf.wsdl11.ServiceWSDLBuilder;
import org.mortbay.http.HttpRequest;
import org.mortbay.http.HttpResponse;
import org.mortbay.http.handler.AbstractHttpHandler;

public class JettyHTTPDestination extends AbstractHTTPDestination {

    public static final String HTTP_REQUEST = JettyHTTPDestination.class.getName() + ".REQUEST";
    public static final String HTTP_RESPONSE = JettyHTTPDestination.class.getName() + ".RESPONSE";

    protected static final String ANONYMOUS_ADDRESS = "http://www.w3.org/2005/08/addressing/anonymous";

    protected ServerEngine engine;
    protected ServerEngine alternateEngine;
    protected MessageObserver incomingObserver;

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

    protected void retrieveEngine() {
        engine = alternateEngine != null
                 ? alternateEngine
                 : JettyHTTPServerEngine.getForPort(bus, nurl.getProtocol(), nurl.getPort(), sslServer);
    }

    /**
     * Register a message observer for incoming messages.
     * 
     * @param observer the observer to notify on receipt of incoming
     */
    public synchronized void setMessageObserver(MessageObserver observer) {
        if (null != observer) {
            LOG.info("registering incoming observer: " + observer);
            try {
                URL url = new URL(getAddressValue());
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
        } else {
            LOG.info("unregistering incoming observer: " + incomingObserver);
            engine.removeServant(nurl);
        }
        incomingObserver = observer;
    }

    /**
     * Retreive a back-channel Conduit, which must be policy-compatible
     * with the current Message and associated Destination. For example
     * compatible Quality of Protection must be asserted on the back-channel.
     * This would generally only be an issue if the back-channel is decoupled.
     * 
     * @param inMessage the current inbound message (null to indicate a 
     * disassociated back-channel)
     * @param partialResponse in the decoupled case, this is expected to be the
     * outbound Message to be sent over the in-built back-channel. 
     * @param address the backchannel address (null to indicate anonymous)
     * @return a suitable Conduit
     */
    public Conduit getBackChannel(Message inMessage, Message partialResponse, EndpointReferenceType address)
        throws IOException {
        HttpResponse response = (HttpResponse)inMessage.get(HTTP_RESPONSE);
        Conduit backChannel = null;
        Exchange ex = inMessage.getExchange();
        EndpointReferenceType target = address != null ? address : ex.get(EndpointReferenceType.class);
        if (target == null) {
            backChannel = new BackChannelConduit(response);
        } else {
            if (partialResponse != null) {
                // setup the outbound message to for 202 Accepted
                partialResponse.put(Message.RESPONSE_CODE, HttpURLConnection.HTTP_ACCEPTED);
                backChannel = new BackChannelConduit(response);
                ex.put(EndpointReferenceType.class, target);
            } else {
                backChannel = conduitInitiator.getConduit(endpointInfo, target);
                // ensure decoupled back channel input stream is closed
                backChannel.setMessageObserver(new MessageObserver() {
                    public void onMessage(Message m) {
                        if (m.getContentFormats().contains(InputStream.class)) {
                            InputStream is = m.getContent(InputStream.class);
                            try {
                                is.close();
                            } catch (Exception e) {
                                // ignore
                            }
                        }
                    }
                });
            }
        }
        return backChannel;
    }

    /**
     * Shutdown the Destination, i.e. stop accepting incoming messages.
     */
    public void shutdown() {
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
        response.setContentType((String) message.get(Message.CONTENT_TYPE));

        Map<?, ?> headers = (Map<?, ?>)message.get(Message.PROTOCOL_HEADERS);
        if (null != headers) {
            for (Iterator<?> iter = headers.keySet().iterator(); iter.hasNext();) {
                String header = (String)iter.next();
                List<?> headerList = (List<?>)headers.get(header);
                for (Object value : headerList) {
                    response.addField(header, (String)value);
                }
            }
        }
    }

    protected void doService(HttpRequest req, HttpResponse resp) throws IOException {
        if (getServer().isSetRedirectURL()) {
            resp.sendRedirect(getServer().getRedirectURL());
            resp.commit();
            req.setHandled(true);
            return;
        }

        if ("GET".equals(req.getMethod()) && req.getURI().toString().toLowerCase().endsWith("?wsdl")) {
            try {

                resp.addField(HttpHeaderHelper.CONTENT_TYPE, "text/xml");

                OutputStream os = resp.getOutputStream();

                WSDLWriter wsdlWriter = WSDLFactory.newInstance().newWSDLWriter();
                Definition def = new ServiceWSDLBuilder(endpointInfo.getService()).build();
                wsdlWriter.writeWSDL(def, os);
                resp.getOutputStream().flush();
                resp.commit();
                req.setHandled(true);
                return;
            } catch (Exception ex) {

                ex.printStackTrace();
            }
        }

        // REVISIT: service on executor if associated with endpoint
        serviceRequest(req, resp);
    }

    protected void serviceRequest(final HttpRequest req, final HttpResponse resp)
        throws IOException {
        try {
            if (LOG.isLoggable(Level.INFO)) {
                LOG.info("Service http request on thread: " + Thread.currentThread());
            }

            MessageImpl inMessage = new MessageImpl();
            inMessage.setContent(InputStream.class, req.getInputStream());
            inMessage.put(HTTP_REQUEST, req);
            inMessage.put(HTTP_RESPONSE, resp);
            inMessage.put(Message.HTTP_REQUEST_METHOD, req.getMethod());
            inMessage.put(Message.PATH_INFO, req.getPath());
            inMessage.put(Message.QUERY_STRING, req.getQuery());
            inMessage.put(Message.CONTENT_TYPE, req.getContentType());
            if (!StringUtils.isEmpty(getAddressValue())) {
                inMessage.put(Message.BASE_PATH, new URL(getAddressValue()).getPath());
            }
            inMessage.put(Message.FIXED_PARAMETER_ORDER, isFixedParameterOrder());
            inMessage.put(Message.ASYNC_POST_RESPONSE_DISPATCH, Boolean.TRUE); 
            
            setHeaders(inMessage);

            inMessage.setDestination(this);

            incomingObserver.onMessage(inMessage);

            resp.commit();
            req.setHandled(true);
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
        if (responseObj instanceof HttpResponse) {
            HttpResponse response = (HttpResponse)responseObj;

            Integer i = (Integer)outMessage.get(Message.RESPONSE_CODE);
            if (i != null) {
                int status = i.intValue();
                if (status == HttpURLConnection.HTTP_INTERNAL_ERROR) {
                    response.setStatus(status, "Fault Occurred");
                } else if (status == HttpURLConnection.HTTP_ACCEPTED) {
                    response.setStatus(status, "Accepted");
                } else {
                    response.setStatus(status);
                }
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
    protected class BackChannelConduit implements Conduit {

        protected HttpResponse response;
        protected EndpointReferenceType target;

        BackChannelConduit(HttpResponse resp) {
            response = resp;
            target = EndpointReferenceUtils.getEndpointReference(ANONYMOUS_ADDRESS);
        }

        public void close(Message msg) throws IOException {
            msg.getContent(OutputStream.class).close();
        }

        /**
         * Register a message observer for incoming messages.
         * 
         * @param observer the observer to notify on receipt of incoming
         */
        public void setMessageObserver(MessageObserver observer) {
            // shouldn't be called for a back channel conduit
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
        public Destination getBackChannel() {
            return null;
        }

        /**
         * Close the conduit
         */
        public void close() {
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
                response.commit();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
