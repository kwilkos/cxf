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

package org.apache.cxf.systest.mtom;

import javax.xml.namespace.QName;
import javax.xml.ws.soap.SOAPBinding;

import org.apache.cxf.Bus;
import org.apache.cxf.BusFactory;
import org.apache.cxf.endpoint.ServerImpl;
import org.apache.cxf.jaxws.JAXWSMethodInvoker;
import org.apache.cxf.jaxws.binding.soap.SOAPBindingImpl;
import org.apache.cxf.jaxws.support.JaxWsEndpointImpl;
import org.apache.cxf.jaxws.support.JaxWsImplementorInfo;
import org.apache.cxf.jaxws.support.JaxWsServiceFactoryBean;
import org.apache.cxf.mtom_xop.TestMtomImpl;
import org.apache.cxf.service.Service;
import org.apache.cxf.service.factory.AbstractServiceFactoryBean;
import org.apache.cxf.service.model.EndpointInfo;
import org.apache.cxf.systest.common.TestServerBase;
import org.apache.cxf.transport.ChainInitiationObserver;
import org.apache.cxf.transport.MessageObserver;

public class Server extends TestServerBase {

    protected void run() {
        Object implementor = new TestMtomImpl();
        String address = "http://localhost:9036/mime-test";
        try {
            Bus bus = BusFactory.getDefaultBus();
            JaxWsImplementorInfo implInfo = new JaxWsImplementorInfo(implementor.getClass());
            AbstractServiceFactoryBean serviceFactory = new JaxWsServiceFactoryBean(implInfo);
            serviceFactory.setBus(bus);
            Service service = serviceFactory.create();
            QName endpointName = implInfo.getEndpointName();
            EndpointInfo ei = service.getServiceInfo().getEndpoint(endpointName);
            service.setInvoker(new JAXWSMethodInvoker(implementor));
            org.apache.cxf.endpoint.EndpointImpl endpoint = new JaxWsEndpointImpl(bus, service, ei);
            SOAPBinding jaxWsSoapBinding = new SOAPBindingImpl(ei.getBinding()); 
            jaxWsSoapBinding.setMTOMEnabled(true);

            endpoint.getInInterceptors().add(new TestMultipartMessageInterceptor());
            endpoint.getOutInterceptors().add(new TestAttachmentOutInterceptor());
            
            endpoint.getEndpointInfo().setAddress(address);
            MessageObserver observer = new ChainInitiationObserver(endpoint, bus);
            ServerImpl server = new ServerImpl(bus, endpoint, observer);
            server.start();
        } catch (Exception e) {
            Thread.currentThread().interrupt();
        }
    }

    public static void main(String args[]) {
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
