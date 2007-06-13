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
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.namespace.QName;

import org.apache.cxf.Bus;
import org.apache.cxf.common.logging.LogUtils;
import org.apache.cxf.common.util.Base64Exception;
import org.apache.cxf.common.util.Base64Utility;
import org.apache.cxf.common.util.StringUtils;
import org.apache.cxf.configuration.Configurable;
import org.apache.cxf.configuration.security.AuthorizationPolicy;
import org.apache.cxf.helpers.CastUtils;
import org.apache.cxf.helpers.HttpHeaderHelper;
import org.apache.cxf.io.AbstractWrappedOutputStream;
import org.apache.cxf.message.Message;
import org.apache.cxf.service.model.EndpointInfo;
import org.apache.cxf.transport.AbstractDestination;
import org.apache.cxf.transport.AbstractMultiplexDestination;
import org.apache.cxf.transport.Conduit;
import org.apache.cxf.transport.ConduitInitiator;
import org.apache.cxf.transport.http.policy.PolicyUtils;
import org.apache.cxf.transports.http.configuration.HTTPServerPolicy;
import org.apache.cxf.ws.addressing.EndpointReferenceType;
import org.apache.cxf.ws.policy.Assertor;
import org.apache.cxf.ws.policy.PolicyEngine;
import org.apache.cxf.wsdl.EndpointReferenceUtils;

/**
 * Common base for HTTP Destination implementations.
 */
public abstract class AbstractHTTPDestination extends AbstractMultiplexDestination implements Configurable,
    Assertor {
    
    public static final String HTTP_REQUEST = "HTTP.REQUEST";
    public static final String HTTP_RESPONSE = "HTTP.RESPONSE";
    public static final String HTTP_CONTEXT = "HTTP.CONTEXT";
    public static final String PROTOCOL_HEADERS_CONTENT_TYPE = Message.CONTENT_TYPE.toLowerCase();
    
    private static final Logger LOG = LogUtils.getL7dLogger(AbstractHTTPDestination.class);
    
    private static final long serialVersionUID = 1L;

    protected final Bus bus;
    protected final ConduitInitiator conduitInitiator;

    // Configuration values
    protected HTTPServerPolicy server;
    protected String contextMatchStrategy = "stem";
    protected boolean fixedParameterOrder;
    protected boolean multiplexWithAddress;
    
    /**
     * Constructor
     * 
     * @param b the associated Bus
     * @param ci the associated conduit initiator
     * @param ei the endpoint info of the destination 
     * @param dp ture for adding the default port if it is missing
     * @throws IOException
     */    
    public AbstractHTTPDestination(Bus b,
                                   ConduitInitiator ci,
                                   EndpointInfo ei,
                                   boolean dp)
        throws IOException {
        super(b, getTargetReference(getAddressValue(ei, dp), b), ei);  
        bus = b;
        conduitInitiator = ci;
        
        initConfig();
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
    
    protected void updateResponseHeaders(Message message) {
        Map<String, List<String>> responseHeaders =
            CastUtils.cast((Map)message.get(Message.PROTOCOL_HEADERS));
        if (responseHeaders == null) {
            responseHeaders = new HashMap<String, List<String>>();
            message.put(Message.PROTOCOL_HEADERS, responseHeaders);         
        }
        setPolicies(responseHeaders);
    }
    
    /** 
     * @param message the message under consideration
     * @return true iff the message has been marked as oneway
     */    
    protected boolean isOneWay(Message message) {
        return message.getExchange() != null && message.getExchange().isOneWay();
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
        Map<?, ?> headers = (Map<?, ?>)message.get(Message.PROTOCOL_HEADERS);
        if (null != headers) {
            
            if (!headers.containsKey(Message.CONTENT_TYPE)) {
                response.setContentType((String) message.get(Message.CONTENT_TYPE));
            }
            
            for (Iterator<?> iter = headers.keySet().iterator(); iter.hasNext();) {
                String header = (String)iter.next();
                List<?> headerList = (List<?>)headers.get(header);
                for (Object value : headerList) {
                    response.addHeader(header, (String)value);
                }
            }
        } else {
            response.setContentType((String) message.get(Message.CONTENT_TYPE));
        }
    }
    
    protected static EndpointInfo getAddressValue(EndpointInfo ei) {       
        return getAddressValue(ei, true);
    } 
    
    protected static EndpointInfo getAddressValue(EndpointInfo ei, boolean dp) {       
        if (dp) {
            String addr = StringUtils.addDefaultPortIfMissing(ei.getAddress());
            if (addr != null) {
                ei.setAddress(addr);
            }
        } 
        return ei;
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
        partialResponse.getExchange().put(EndpointReferenceType.class, decoupledTarget);
        return true;
    }

    private void initConfig() {
        PolicyEngine engine = bus.getExtension(PolicyEngine.class);
        // for a decoupled endpoint there is no service info
        if (null != engine && engine.isEnabled() 
            && null != endpointInfo.getService()) {
            server = PolicyUtils.getServer(engine, endpointInfo, this);
        }
        if (null == server) {
            server = endpointInfo.getTraversedExtensor(
                    new HTTPServerPolicy(), HTTPServerPolicy.class);
        }
    }

    void setPolicies(Map<String, List<String>> headers) {
        HTTPServerPolicy policy = server; 
        if (policy.isSetCacheControl()) {
            headers.put("Cache-Control",
                        Arrays.asList(new String[] {policy.getCacheControl().value()}));
        }
        if (policy.isSetContentLocation()) {
            headers.put("Content-Location",
                        Arrays.asList(new String[] {policy.getContentLocation()}));
        }
        if (policy.isSetContentEncoding()) {
            headers.put("Content-Encoding",
                        Arrays.asList(new String[] {policy.getContentEncoding()}));
        }
        if (policy.isSetContentType()) {
            headers.put(HttpHeaderHelper.CONTENT_TYPE,
                        Arrays.asList(new String[] {policy.getContentType()}));
        }
        if (policy.isSetServerType()) {
            headers.put("Server",
                        Arrays.asList(new String[] {policy.getServerType()}));
        }
        if (policy.isSetHonorKeepAlive() && !policy.isHonorKeepAlive()) {
            headers.put("Connection",
                        Arrays.asList(new String[] {"close"}));
        }
        
    
        
    /*
     * TODO - hook up these policies
    <xs:attribute name="SuppressClientSendErrors" type="xs:boolean" use="optional" default="false">
    <xs:attribute name="SuppressClientReceiveErrors" type="xs:boolean" use="optional" default="false">
    */
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
                if (HttpURLConnection.HTTP_INTERNAL_ERROR == i) {
                    Map<Object, Object> pHeaders = 
                        CastUtils.cast((Map)outMessage.get(Message.PROTOCOL_HEADERS));
                    if (null != pHeaders && pHeaders.containsKey(PROTOCOL_HEADERS_CONTENT_TYPE)) {
                        pHeaders.remove(PROTOCOL_HEADERS_CONTENT_TYPE);
                    }
                }
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
    public class BackChannelConduit
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
        public void prepare(Message message) throws IOException {
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
        private Message outMessage;
        
        WrappedOutputStream(Message m, HttpServletResponse resp) {
            super();
            this.outMessage = m;
            response = resp;
        }

        /**
         * Perform any actions required on stream flush (freeze headers,
         * reset output stream ... etc.)
         */
        protected void onFirstWrite() throws IOException {
            OutputStream responseStream = flushHeaders(outMessage);
            if (null != responseStream) {
                wrappedStream = responseStream;
            }            
        }

        /**
         * Perform any actions required on stream closure (handle response etc.)
         */
        public void close() throws IOException {
            if (wrappedStream == null) {
                OutputStream responseStream = flushHeaders(outMessage);
                if (null != responseStream) {
                    wrappedStream = responseStream;
                }
            }
            commitResponse();
        }

        

        private void commitResponse() {
            try {
                response.flushBuffer();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    protected boolean contextMatchOnExact() {
        return "exact".equals(contextMatchStrategy);
    }    

    public String getBeanName() {
        String beanName = null;
        if (endpointInfo.getName() != null) {
            beanName = endpointInfo.getName().toString() + ".http-destination";
        }
        return beanName;
    }

    /*
     * Implement multiplex via the address URL to avoid the need for ws-a.
     * Requires contextMatchStrategy of stem.
     * 
     * @see org.apache.cxf.transport.AbstractMultiplexDestination#getAddressWithId(java.lang.String)
     */
    public EndpointReferenceType getAddressWithId(String id) {
        EndpointReferenceType ref = null;

        if (isMultiplexWithAddress()) {
            String address = EndpointReferenceUtils.getAddress(reference);
            ref = EndpointReferenceUtils.duplicate(reference);
            if (address.endsWith("/")) {
                EndpointReferenceUtils.setAddress(ref, address + id);
            } else {
                EndpointReferenceUtils.setAddress(ref, address + "/" + id);
            }
        } else {
            ref = super.getAddressWithId(id);
        }
        return ref;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.cxf.transport.AbstractMultiplexDestination#getId(java.util.Map)
     */
    public String getId(Map context) {
        String id = null;

        if (isMultiplexWithAddress()) {
            String address = (String)context.get(Message.PATH_INFO);
            if (null != address) {
                int afterLastSlashIndex = address.lastIndexOf("/") + 1;
                if (afterLastSlashIndex > 0 
                        && afterLastSlashIndex < address.length()) {
                    id = address.substring(afterLastSlashIndex);
                }
            } else {
                getLogger().log(Level.WARNING,
                    new org.apache.cxf.common.i18n.Message(
                            "MISSING_PATH_INFO", LOG).toString());
            }
        } else {
            return super.getId(context);
        }   
        return id;
    }

    public String getContextMatchStrategy() {
        return contextMatchStrategy;
    }

    public void setContextMatchStrategy(String contextMatchStrategy) {
        this.contextMatchStrategy = contextMatchStrategy;
    }

    public boolean isFixedParameterOrder() {
        return fixedParameterOrder;
    }

    public void setFixedParameterOrder(boolean fixedParameterOrder) {
        this.fixedParameterOrder = fixedParameterOrder;
    }

    public boolean isMultiplexWithAddress() {
        return multiplexWithAddress;
    }

    public void setMultiplexWithAddress(boolean multiplexWithAddress) {
        this.multiplexWithAddress = multiplexWithAddress;
    }

    public HTTPServerPolicy getServer() {
        return server;
    }

    public void setServer(HTTPServerPolicy server) {
        this.server = server;
    }
    
    public void assertMessage(Message message) {
        PolicyUtils.assertServerPolicy(message, server); 
    }

    public boolean canAssert(QName type) {
        return PolicyUtils.HTTPSERVERPOLICY_ASSERTION_QNAME.equals(type); 
    }
    
}
