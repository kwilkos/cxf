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

package org.apache.cxf.systest.jaxws;

import java.net.URL;

import org.w3c.dom.Node;

import org.apache.cxf.Bus;
import org.apache.cxf.binding.BindingFactoryManager;
import org.apache.cxf.binding.soap.SoapBindingFactory;
import org.apache.cxf.binding.soap.SoapDestinationFactory;
import org.apache.cxf.binding.soap.SoapFault;
import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.binding.soap.interceptor.AbstractSoapInterceptor;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.jaxws.support.JaxWsServiceFactoryBean;
import org.apache.cxf.phase.Phase;
import org.apache.cxf.service.Service;
import org.apache.cxf.service.invoker.BeanInvoker;
import org.apache.cxf.test.AbstractCXFTest;
import org.apache.cxf.transport.ConduitInitiatorManager;
import org.apache.cxf.transport.DestinationFactoryManager;
import org.apache.cxf.transport.local.LocalTransportFactory;
import org.apache.hello_world_soap_http.GreeterImpl;

public class SoapFaultTest extends AbstractCXFTest {

    private Bus bus;
    private Service service;

    @Override
    public void setUp() throws Exception {
        super.setUp();

        bus = getBus();

        SoapBindingFactory bindingFactory = new SoapBindingFactory();

        bus.getExtension(BindingFactoryManager.class)
            .registerBindingFactory("http://schemas.xmlsoap.org/wsdl/soap/", bindingFactory);

        DestinationFactoryManager dfm = bus.getExtension(DestinationFactoryManager.class);
        SoapDestinationFactory soapDF = new SoapDestinationFactory(dfm);
        dfm.registerDestinationFactory("http://schemas.xmlsoap.org/wsdl/soap/", soapDF);

        LocalTransportFactory localTransport = new LocalTransportFactory();
        dfm.registerDestinationFactory("http://schemas.xmlsoap.org/soap/http", localTransport);

        ConduitInitiatorManager extension = bus.getExtension(ConduitInitiatorManager.class);
        extension.registerConduitInitiator(LocalTransportFactory.TRANSPORT_ID, localTransport);
        
        JaxWsServiceFactoryBean bean = new JaxWsServiceFactoryBean();
        URL resource = getClass().getResource("/wsdl/hello_world.wsdl");
        assertNotNull(resource);
        bean.setWsdlURL(resource);
        bean.setBus(bus);
        bean.setServiceClass(GreeterImpl.class);

        GreeterImpl greeter = new GreeterImpl();
        BeanInvoker invoker = new BeanInvoker(greeter);
        bean.setInvoker(invoker);

        service = bean.create();

        bean.activateEndpoints();
    }

    public void testInterceptorThrowingSoapFault() throws Exception {
        service.getInInterceptors().add(new FaultThrowingInterceptor());

        Node response = invoke("http://localhost:9000/SoapContext/SoapPort",
                               LocalTransportFactory.TRANSPORT_ID, "GreeterMessage.xml");

        assertNotNull(response);

        assertValid("/s:Envelope/s:Body/s:Fault/faultstring[text()='I blame Hadrian.']", response);
    }


    /**
     * We need to get the jaxws fault -> soap fault conversion working for this
     * @throws Exception
     */
    public void testWebServiceException() throws Exception {
        Node response = invoke("http://localhost:9000/SoapContext/SoapPort",
                               LocalTransportFactory.TRANSPORT_ID, "GreeterGetFaultMessage.xml");

        assertNotNull(response);

        assertValid("/s:Envelope/s:Body/s:Fault/faultstring[text()='TestBadRecordLit']", response);
        assertValid("/s:Envelope/s:Body/s:Fault/detail", response);
    }

    public class FaultThrowingInterceptor extends AbstractSoapInterceptor {
        public FaultThrowingInterceptor() {
            setPhase(Phase.USER_LOGICAL);
        }

        public void handleMessage(SoapMessage message) throws Fault {
            throw new SoapFault("I blame Hadrian.", SoapFault.SENDER);
        }

    }
}
