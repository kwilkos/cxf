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

package org.apache.cxf.endpoint;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.xml.namespace.QName;

import org.apache.cxf.Bus;

public class ServiceContractResolverRegistryImpl implements ServiceContractResolverRegistry {

    private Bus bus;
    private List<ServiceContactResolver> resolvers;

    /**
     * Initialize registry, and expose as Bus extension.
     */
    @PostConstruct
    public void init() {
        resolvers = new ArrayList<ServiceContactResolver>();
        if (bus != null) {
            bus.setExtension(this, ServiceContractResolverRegistry.class);
        }
    }

    public URI getContractLocation(QName qname) {
        for (ServiceContactResolver resolver : resolvers) {
            URI contact = resolver.getContractLocation(qname);
            if (null != contact) {
                return contact;
            }
        }
        return null;
    }

    public boolean isRegistered(ServiceContactResolver resolver) {
        return resolvers.contains(resolver);
    }

    public synchronized void register(ServiceContactResolver resolver) {
        resolvers.add(resolver);
        
    }

    public synchronized void unregister(ServiceContactResolver resolver) {
        resolvers.remove(resolver);
        
    }

    public void setBus(Bus bus) {
        this.bus = bus;
    }
    
    protected List<ServiceContactResolver> getResolvers() {
        return resolvers;
    }

}
