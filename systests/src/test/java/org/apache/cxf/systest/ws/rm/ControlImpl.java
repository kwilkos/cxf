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


package org.apache.cxf.systest.ws.rm;

import java.util.logging.Logger;

import javax.jws.WebService;
import javax.xml.ws.Endpoint;

import org.apache.cxf.BusFactory;
import org.apache.cxf.bus.spring.SpringBusFactory;
import org.apache.cxf.greeter_control.AbstractGreeterImpl;
import org.apache.cxf.interceptor.Interceptor;
import org.apache.cxf.interceptor.LoggingInInterceptor;
import org.apache.cxf.interceptor.LoggingOutInterceptor;
import org.apache.cxf.ws.rm.RMManager;

@WebService(serviceName = "ControlService", 
            portName = "ControlPort", 
            endpointInterface = "org.apache.cxf.greeter_control.Control", 
            targetNamespace = "http://cxf.apache.org/greeter_control")
public class ControlImpl  extends org.apache.cxf.greeter_control.ControlImpl {
    
    private static final Logger LOG = Logger.getLogger(ControlImpl.class.getName());
    
    @Override
    public boolean stopGreeter() {  
        
        if (null != endpoint) {
            LOG.info("Stopping Greeter endpoint");
            endpoint.stop();
        } else {
            LOG.info("No endpoint active.");
        }
        endpoint = null;
        if (null != greeterBus) {
            RMManager manager = greeterBus.getExtension(RMManager.class);
            manager.shutdown();
            greeterBus.shutdown(true);
        }
        return true;
    }

    @Override
    public boolean startGreeter(String cfgResource) {
        SpringBusFactory bf = new SpringBusFactory();
        greeterBus = bf.createBus(cfgResource);
        BusFactory.setDefaultBus(greeterBus);
        LOG.info("Initialised bus with cfg file resource: " + cfgResource);
        
        Interceptor logIn = new LoggingInInterceptor();
        Interceptor logOut = new LoggingOutInterceptor();
        greeterBus.getInInterceptors().add(logIn);
        greeterBus.getOutInterceptors().add(logOut);
        greeterBus.getOutFaultInterceptors().add(logOut);
        
        Endpoint.publish(address, implementor);
        LOG.info("Published greeter endpoint.");
        
        if (implementor instanceof AbstractGreeterImpl) {
            ((AbstractGreeterImpl)implementor).setGreeting(null);
        }
        
        return true;        
    }
    
}
