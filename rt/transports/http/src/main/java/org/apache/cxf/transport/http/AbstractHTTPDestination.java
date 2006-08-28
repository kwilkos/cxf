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
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.xml.ws.BindingProvider;

import static javax.xml.ws.handler.MessageContext.HTTP_REQUEST_HEADERS;
import static javax.xml.ws.handler.MessageContext.HTTP_RESPONSE_HEADERS;

import org.apache.cxf.Bus;
import org.apache.cxf.common.logging.LogUtils;
import org.apache.cxf.common.util.Base64Exception;
import org.apache.cxf.common.util.Base64Utility;
import org.apache.cxf.message.Message;
import org.apache.cxf.service.model.EndpointInfo;
import org.apache.cxf.transport.ConduitInitiator;
import org.apache.cxf.transport.Destination;
import org.apache.cxf.ws.addressing.AttributedURIType;
import org.apache.cxf.ws.addressing.EndpointReferenceType;

import static org.apache.cxf.message.Message.ONEWAY_MESSAGE;


/**
 * Common base for HTTP Destination implementations.
 */
public abstract class AbstractHTTPDestination  implements Destination {
    static final Logger LOG = LogUtils.getL7dLogger(AbstractHTTPDestination.class);
    
    private static final long serialVersionUID = 1L;        

    protected final Bus bus;
    protected final ConduitInitiator conduitInitiator;
    protected final HTTPDestinationConfiguration config;
    protected final EndpointInfo endpointInfo;
    protected final EndpointReferenceType reference;
    protected String name;
    protected URL nurl;

    /**
     * Constructor, using real configuration.
     * 
     * @param b the associated Bus
     * @param ci the associated conduit initiator
     * @param endpointInfo the endpoint info of the destination 
     * @throws IOException
     */
    public AbstractHTTPDestination(Bus b,
                                   ConduitInitiator ci,
                                   EndpointInfo endpointInfo)
        throws IOException {
        this(b,
             ci,
             endpointInfo,
             new HTTPDestinationConfiguration(b, endpointInfo));
    }

    /**
     * Constructor, allowing subsititution of configuration.
     * 
     * @param b the associated Bus
     * @param ci the associated conduit initiator
     * @param ei the endpoint info of the destination 
     * @param cfg the configuration
     * @throws IOException
     */    
    public AbstractHTTPDestination(Bus b,
                                   ConduitInitiator ci,
                                   EndpointInfo ei,
                                   HTTPDestinationConfiguration cfg)
        throws IOException {
        bus = b;
        conduitInitiator = ci;
        endpointInfo = ei;
        config = cfg;
        
        nurl = new URL(config.getAddress());
        name = nurl.getPath();

        reference = new EndpointReferenceType();
        AttributedURIType address = new AttributedURIType();
        address.setValue(config.getAddress());
        reference.setAddress(address);
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
        message.put(HTTP_REQUEST_HEADERS, requestHeaders);

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
                    message.put(BindingProvider.USERNAME_PROPERTY, username);
                    message.put(BindingProvider.PASSWORD_PROPERTY, password);
                } catch (Base64Exception ex) {
                    //ignore, we'll leave things alone.  They can try decoding it themselves
                }
            }
        }
        
        Map<String, List<String>> responseHeaders = new HashMap<String, List<String>>();
        config.setPolicies(responseHeaders);
        message.put(HTTP_RESPONSE_HEADERS, responseHeaders);         
    }
    
    /** 
     * @param message the message under consideration
     * @return true iff the message has been marked as oneway
     */    
    protected boolean isOneWay(Message message) {
        Boolean oneway = (Boolean)message.get(ONEWAY_MESSAGE);
        return oneway != null && oneway.booleanValue();
    }

    /**
     * Copy the request headers into the message.
     * 
     * @param message the current message
     * @param headers the current set of headers
     */
    protected abstract void copyRequestHeaders(Message message,
                                               Map<String, List<String>> headers);
}
