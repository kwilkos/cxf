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

package org.apache.cxf.ws.rm.impl;

import javax.xml.namespace.QName;

import org.apache.cxf.endpoint.Endpoint;

public class RMEndpoint {
    
    private final RMInterceptor interceptor;
    private final Endpoint endpoint;
    private Source source;
    private Destination destination;
    private Proxy proxy;
    private Servant servant;
    
    public RMEndpoint(RMInterceptor i, Endpoint e) {
        interceptor = i;
        endpoint = e;
        source = new Source(this);
        destination = new Destination(this);
        proxy = new Proxy(interceptor.getBus(), this);
        proxy.initialise();
        servant = new Servant(this);
    }
    
    public QName getName() {
        return endpoint.getEndpointInfo().getName();
    }
      
    /**
     * @return Returns the endpoint.
     */
    public Endpoint getEndpoint() {
        return endpoint;
    }

    /**
     * @return Returns the interceptor.
     */
    public RMInterceptor getInterceptor() {
        return interceptor;
    }

    /** 
     * @return Returns the destination.
     */
    public Destination getDestination() {
        return destination;
    }
    
    /**
     * @param destination The destination to set.
     */
    public void setDestination(Destination destination) {
        this.destination = destination;
    }
    
    /**
     * @return Returns the proxy.
     */
    public Proxy getProxy() {
        return proxy;
    }
    
    /**
     * @param proxy The proxy to set.
     */
    public void setProxy(Proxy p) {
        proxy = p;
    }
    
    /**
     * @return Returns the servant.
     */
    public Servant getServant() {
        return servant;
    }
    
    /**
     * @param servant The servant to set.
     */
    public void setServant(Servant s) {
        servant = s;
    }
    
    /** 
     * @return Returns the source.
     */
    public Source getSource() {
        return source;
    }
    
    /**
     * @param source The source to set.
     */
    public void setSource(Source source) {
        this.source = source;
    } 
    
    
    
}
