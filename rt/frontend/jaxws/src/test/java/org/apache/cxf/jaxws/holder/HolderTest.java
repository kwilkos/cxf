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
package org.apache.cxf.jaxws.holder;

import javax.xml.ws.Holder;

import org.w3c.dom.Node;

import org.apache.cxf.jaxws.AbstractJaxWsTest;
import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;
import org.apache.cxf.jaxws.JaxWsServerFactoryBean;
import org.apache.cxf.jaxws.MessageReplayObserver;
import org.apache.cxf.service.model.EndpointInfo;
import org.apache.cxf.transport.Destination;
import org.apache.cxf.transport.local.LocalTransportFactory;

public class HolderTest extends AbstractJaxWsTest {
    private final String address = "http://localhost:9000/HolderService";

    public void testClient() throws Exception {
        EndpointInfo ei = new EndpointInfo(null, "http://schemas.xmlsoap.org/soap/http");
        ei.setAddress(address);

        Destination d = localTransport.getDestination(ei);
        d.setMessageObserver(new MessageReplayObserver("/org/apache/cxf/jaxws/holder/echoResponse.xml"));

        JaxWsProxyFactoryBean factory = new JaxWsProxyFactoryBean();
        factory.getClientFactoryBean().setServiceClass(HolderService.class);
        factory.getClientFactoryBean().setBus(getBus());
        factory.getClientFactoryBean().setAddress(address);

        HolderService h = (HolderService)factory.create();
        Holder<String> holder = new Holder<String>();
        assertEquals("one", h.echo("one", "two", holder));
        assertEquals("two", holder.value);
    }

    public void testServer() throws Exception {
        JaxWsServerFactoryBean svr = new JaxWsServerFactoryBean();
        svr.setBus(getBus());
        svr.setServiceBean(new HolderServiceImpl());
        svr.setAddress(address);
        svr.create();

        addNamespace("h", "http://holder.jaxws.cxf.apache.org");

        Node response = invoke(address, LocalTransportFactory.TRANSPORT_ID, "echo.xml");
        
        assertNotNull(response);
        assertValid("//h:echoResponse/h:return[text()='one']", response);
        assertValid("//h:echoResponse/h:outS2[text()='two']", response);
        assertNoFault(response);

        response = invoke(address, LocalTransportFactory.TRANSPORT_ID, "echo2.xml");

        assertNotNull(response);
        assertNoFault(response);
        assertValid("//h:echo2Response/h:return[text()='one']", response);
        assertValid("//h:echo2Response/h:outS2[text()='two']", response);
        
        // test holder with in/out header
        response = invoke(address, LocalTransportFactory.TRANSPORT_ID, "echo3.xml");
        
        assertNotNull(response);
        assertNoFault(response);
        assertValid("//h:echo3Response/h:return[text()='one']", response);
        assertValid("//s:Header/h:header[text()='header']", response);

    }

}
