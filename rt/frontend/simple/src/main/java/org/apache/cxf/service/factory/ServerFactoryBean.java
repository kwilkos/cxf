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
package org.apache.cxf.service.factory;

import java.io.IOException;

import org.apache.cxf.BusException;
import org.apache.cxf.binding.soap.SoapBindingInfoFactoryBean;
import org.apache.cxf.endpoint.Endpoint;
import org.apache.cxf.endpoint.EndpointException;
import org.apache.cxf.endpoint.Server;
import org.apache.cxf.endpoint.ServerImpl;
import org.apache.cxf.transport.ChainInitiationObserver;

/**
 * This class helps take a {@link org.apache.cxf.service.Service} and 
 * expose as a server side endpoint.
 * If there is no Service, it can create one for you using a 
 * {@link ReflectionServiceFactoryBean}.
 * <p>
 * For most scenarios you'll want to just have the ServerFactoryBean handle everything
 * for you. In such a case, usage might look like this:
 * </p>
 * <pre>
 * ServerFactoryBean sf = new ServerFactoryBean();
 * sf.setServiceClass(MyService.class);
 * sf.setAddress("http://localhost:8080/MyService");
 * sf.create();
 * </pre>
 * <p>
 * You can also get more advanced and customize the service factory used:
 * <pre>
 * ReflectionServiceFactory serviceFactory = new ReflectionServiceFactory();
 * serviceFactory.setServiceClass(MyService.class);
 * ..
 * \/\/ Customize service factory here...
 * serviceFactory.setWrapped(false);
 * ...
 * ServerFactoryBean sf = new ServerFactoryBean();
 * sf.setServiceFactory(serviceFactory);
 * sf.setAddress("http://localhost:8080/MyService");
 * sf.create();
 * </pre>
 */
public class ServerFactoryBean extends AbstractEndpointFactory {
    private Server server;
    private boolean start = true;
    
    public ServerFactoryBean() {
        super();
        setBindingFactory(new SoapBindingInfoFactoryBean());
        setServiceFactory(new ReflectionServiceFactoryBean());
    }
    
    public Server create() {
        try {
            Endpoint ep = createEndpoint();
            server = new ServerImpl(getBus(), ep, new ChainInitiationObserver(ep, getBus()));
            
            if (start) {
                server.start();
            }
        } catch (EndpointException e) {
            throw new ServiceConstructionException(e);
        } catch (BusException e) {
            throw new ServiceConstructionException(e);
        } catch (IOException e) {
            throw new ServiceConstructionException(e);
        }
        
        return server;
    }

    public Server getServer() {
        return server;
    }

    public void setServer(Server server) {
        this.server = server;
    }

    public boolean isStart() {
        return start;
    }

    public void setStart(boolean start) {
        this.start = start;
    }    
}