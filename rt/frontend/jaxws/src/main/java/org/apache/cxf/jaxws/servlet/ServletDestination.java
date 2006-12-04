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

package org.apache.cxf.jaxws.servlet;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.cxf.Bus;
import org.apache.cxf.common.util.Base64Exception;
import org.apache.cxf.common.util.Base64Utility;
import org.apache.cxf.configuration.security.AuthorizationPolicy;
import org.apache.cxf.helpers.HttpHeaderHelper;
import org.apache.cxf.io.AbstractWrappedOutputStream;
import org.apache.cxf.message.Message;
import org.apache.cxf.message.MessageImpl;
import org.apache.cxf.service.model.EndpointInfo;
import org.apache.cxf.transport.Conduit;
import org.apache.cxf.transport.ConduitInitiator;
import org.apache.cxf.transport.Destination;
import org.apache.cxf.transport.MessageObserver;
import org.apache.cxf.ws.addressing.AttributedURIType;
import org.apache.cxf.ws.addressing.EndpointReferenceType;


public class ServletDestination implements Destination {

    public static final String HTTP_REQUEST =
        "HTTP_SERVLET_REQUEST";
    public static final String HTTP_RESPONSE =
        "HTTP_SERVLET_RESPONSE"; 
    
    static final Logger LOG = Logger.getLogger(ServletDestination.class.getName());
        
    private static final long serialVersionUID = 1L;        

    protected final Bus bus;
    protected final ConduitInitiator conduitInitiator;
    protected final EndpointInfo endpointInfo;
    protected final EndpointReferenceType reference;
    protected String name;
    protected URL nurl;
    protected MessageObserver incomingObserver;
    
    
    /**
     * Constructor, allowing subsititution of configuration.
     * 
     * @param b the associated Bus
     * @param ci the associated conduit initiator
     * @param ei the endpoint info of the destination 
     * @param cfg the configuration
     * @throws IOException
     */    
    public ServletDestination(Bus b,
                              ConduitInitiator ci,
                              EndpointInfo ei)
        throws IOException {
        bus = b;
        conduitInitiator = ci;
        endpointInfo = ei;
        
        reference = new EndpointReferenceType();
        AttributedURIType add = new AttributedURIType();
        add.setValue(ei.getAddress());
        reference.setAddress(add);
    }

    /**
     * @return the reference associated with this Destination
     */    
    public EndpointReferenceType getAddress() {
        return reference;
    }

    /**
     * Cache HTTP headers in message.
     * 
     * @param message the current message
     */
    protected void setHeaders(Message message) {
        Map<String, List<String>> requestHeaders = new HashMap<String, List<String>>();
        copyRequestHeaders(message, requestHeaders);
        message.put(Message.PROTOCOL_HEADERS, requestHeaders);

        if (requestHeaders.containsKey("Authorization")) {
            List<String> authorizationLines = requestHeaders.get("Authorization"); 
            String credentials = authorizationLines.get(0);
            String authType = credentials.split(" ")[0];
            if ("Basic".equals(authType)) {
                String authEncoded = credentials.split(" ")[1];
                try {
                    String authDecoded = new String(Base64Utility.decode(authEncoded));
                    String authInfo[] = authDecoded.split(":");
                    String username = authInfo[0];
                    String password = authInfo[1];
                    
                    AuthorizationPolicy policy = new AuthorizationPolicy();
                    policy.setUserName(username);
                    policy.setPassword(password);
                    
                    message.put(AuthorizationPolicy.class, policy);
                } catch (Base64Exception ex) {
                    //ignore, we'll leave things alone.  They can try decoding it themselves
                }
            }
        }
           
    }
    
    @SuppressWarnings("unchecked")
    protected void updateResponseHeaders(Message message) {
        Map<String, List<String>> responseHeaders =
            (Map<String, List<String>>)message.get(Message.PROTOCOL_HEADERS);
        if (responseHeaders == null) {
            responseHeaders = new HashMap<String, List<String>>();
            message.put(Message.PROTOCOL_HEADERS, responseHeaders);         
        }
    }
    


    /**
     * Register a message observer for incoming messages.
     * 
     * @param observer the observer to notify on receipt of incoming
     */
    public synchronized void setMessageObserver(MessageObserver observer) {
        LOG.info("set the observer for address " + getAddress().getAddress().getValue());
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
    public Conduit getBackChannel(Message inMessage,
                                  Message partialResponse,
                                  EndpointReferenceType address) throws IOException {
        HttpServletResponse response = (HttpServletResponse)inMessage.get(HTTP_RESPONSE);
        Conduit backChannel = null;
        if (address == null) {
            backChannel = new BackChannelConduit(address, response);
        } else {
            if (partialResponse != null) {
                // setup the outbound message to for 202 Accepted
                partialResponse.put(Message.RESPONSE_CODE,
                                    HttpURLConnection.HTTP_ACCEPTED);
                backChannel = new BackChannelConduit(address, response);
            } else {
                backChannel = conduitInitiator.getConduit(endpointInfo, address);
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
        HttpServletRequest req = (HttpServletRequest)message.get(HTTP_REQUEST);
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
        String ct = (String) message.get(Message.CONTENT_TYPE);
        String enc = (String) message.get(Message.ENCODING);
        
        if (null != ct) {
            if (enc != null && ct.indexOf("charset=") == -1) {
                ct = ct + "; charset=" + enc;
            }
            response.setContentType(ct);
        } else if (enc != null) {
            response.setContentType("text/xml; charset=" + enc);
        }
    }
    
    
    
    protected void doMessage(MessageImpl inMessage) throws IOException {
        try {
            
            setHeaders(inMessage);
            
            inMessage.setDestination(this);
            
            incomingObserver.onMessage(inMessage);
            
        } finally {
            if (LOG.isLoggable(Level.INFO)) {
                LOG.info("Finished servicing http request on thread: " + Thread.currentThread());
            }
        }
        
    }

    
    protected class BackChannelConduit implements Conduit {
        
        protected HttpServletResponse response;
        protected EndpointReferenceType target;
        
        BackChannelConduit(EndpointReferenceType ref, HttpServletResponse resp) {
            response = resp;
            target = ref;
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
        public void send(Message message) throws IOException {
            message.put(HTTP_RESPONSE, response);
            message.setContent(OutputStream.class,
                               new WrappedOutputStream(message, response));
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
                LOG.severe(e.getMessage());
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
        } else {
            LOG.log(Level.WARNING, "UNEXPECTED_RESPONSE_TYPE_MSG", responseObj.getClass());
            throw new IOException("UNEXPECTED_RESPONSE_TYPE_MSG" + responseObj.getClass());
        }
    
        if (isOneWay(outMessage)) {
            outMessage.remove(HTTP_RESPONSE);
        }
        return responseStream;
    }
    
    protected boolean isOneWay(Message message) {
        return message.getExchange() != null && message.getExchange().isOneWay();
    }

    public MessageObserver getMessageObserver() {
        return this.incomingObserver;
    }

    public EndpointInfo getEndpointInfo() {
        return endpointInfo;
    }

}
