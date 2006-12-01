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

import java.util.concurrent.Future;
import java.util.logging.Logger;

import javax.jws.WebService;
import javax.xml.ws.AsyncHandler;
import javax.xml.ws.Endpoint;
import javax.xml.ws.Response;

import org.apache.cxf.Bus;
import org.apache.cxf.bus.spring.SpringBusFactory;
import org.apache.cxf.greeter_control.Control;
import org.apache.cxf.greeter_control.types.StartGreeterResponse;
import org.apache.cxf.greeter_control.types.StopGreeterResponse;
import org.apache.cxf.ws.rm.RMManager;


@WebService(serviceName = "ControlService", 
            portName = "ControlPort", 
            endpointInterface = "org.apache.cxf.greeter_control.Control", 
            targetNamespace = "http://cxf.apache.org/greeter_control")
public class ControlImpl implements Control {
    
    private static final Logger LOG = Logger.getLogger(ControlImpl.class.getName());
    private Endpoint endpoint;
    private Bus greeterBus;
    
    public boolean startGreeter(String cfgResource) {
       
        SpringBusFactory bf = new SpringBusFactory();
        greeterBus = bf.createBus(cfgResource);
        bf.setDefaultBus(greeterBus);
        LOG.info("Initialised bus with cfg file resource: " + cfgResource);
        // greeterBus.getOutInterceptors().add(new JaxwsInterceptorRemover());
        greeterBus.getOutInterceptors().add(new OutMessageRecorder());
        greeterBus.getInInterceptors().add(new InMessageRecorder());
        
        GreeterImpl implementor = new GreeterImpl();
        String address = "http://localhost:9020/SoapContext/GreeterPort";
        endpoint = Endpoint.publish(address, implementor);
        LOG.info("Published greeter endpoint.");
        
        return true;        
    }

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
    
    public Future<?> startGreeterAsync(String requestType, AsyncHandler<StartGreeterResponse> asyncHandler) {
        // never called
        return null;
    }

    public Response<StartGreeterResponse> startGreeterAsync(String requestType) {
        // never called
        return null;
    }

    public Response<StopGreeterResponse> stopGreeterAsync() {
        // never called
        return null;
    }

    public Future<?> stopGreeterAsync(AsyncHandler<StopGreeterResponse> asyncHandler) {
        // never called
        return null;
    }
    
    
    
}
