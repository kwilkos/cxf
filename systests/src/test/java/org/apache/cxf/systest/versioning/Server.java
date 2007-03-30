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

package org.apache.cxf.systest.versioning;

import javax.xml.ws.Endpoint;

import org.apache.cxf.jaxws.EndpointImpl;
import org.apache.cxf.testutil.common.AbstractBusTestServerBase;
import org.apache.hello_world_mixedstyle.GreeterImplMixedStyle;


public class Server extends AbstractBusTestServerBase {

    protected void run() {
        //implementor1 and implementor2 are published using local transport
        Object implementor1 = new GreeterImplMixedStyle();
        String address1 = "local://SoapContext/version1/SoapPort";
        Endpoint.publish(address1, implementor1);

        Object implementor2 = new GreeterImplMixedStyle();
        String address2 = "local://SoapContext/version2/SoapPort";
        Endpoint.publish(address2, implementor2);
        
        //A dummy service that acts as a routing mediator
        Object implementor = new GreeterImplMixedStyle();
        String address = "http://localhost:9027/SoapContext/SoapPort";
        javax.xml.ws.Endpoint jaxwsEndpoint = Endpoint.publish(address, implementor);  
        
        //Register a MediatorInInterceptor on this dummy service
        EndpointImpl jaxwsEndpointImpl = (EndpointImpl)jaxwsEndpoint;
        org.apache.cxf.endpoint.Server server = jaxwsEndpointImpl.getServer();
        org.apache.cxf.endpoint.Endpoint endpoint = server.getEndpoint();
        endpoint.getInInterceptors().add(new MediatorInInterceptor());
    }

    public static void main(String[] args) {
        try {
            Server s = new Server();
            s.start();
        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(-1);
        } finally {
            System.out.println("done!");
        }
    }
}
