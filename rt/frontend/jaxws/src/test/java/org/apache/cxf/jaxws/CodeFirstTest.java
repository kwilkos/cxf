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
package org.apache.cxf.jaxws;

import java.util.Collection;

import org.w3c.dom.Node;

import org.apache.cxf.Bus;
import org.apache.cxf.jaxws.service.Hello;
import org.apache.cxf.jaxws.support.JaxWsServiceFactoryBean;
import org.apache.cxf.service.Service;
import org.apache.cxf.service.factory.ServerFactoryBean;
import org.apache.cxf.service.model.BindingInfo;
import org.apache.cxf.service.model.InterfaceInfo;
import org.apache.cxf.transport.local.LocalTransportFactory;

public class CodeFirstTest extends AbstractJaxWsTest {
    public void testModel() throws Exception {
        JaxWsServiceFactoryBean bean = new JaxWsServiceFactoryBean();

        Bus bus = getBus();
        bean.setBus(bus);
        bean.setServiceClass(Hello.class);

        Service service = bean.create();

        InterfaceInfo i = service.getServiceInfo().getInterface();
        assertEquals(1, i.getOperations().size());

        ServerFactoryBean svrFactory = new ServerFactoryBean();
        svrFactory.setBus(bus);
        svrFactory.setServiceFactory(bean);
        svrFactory.setTransportId("http://schemas.xmlsoap.org/soap/");
        svrFactory.create();
        
        Collection<BindingInfo> bindings = service.getServiceInfo().getBindings();
        assertEquals(1, bindings.size());
    }

    public void testEndpoint() throws Exception {
        Hello service = new Hello();

        EndpointImpl ep = new EndpointImpl(getBus(), service, null);
        ep.publish("http://localhost:9090/hello");

        Node res = invoke("http://localhost:9090/hello", 
                          LocalTransportFactory.TRANSPORT_ID,
                          "sayHi.xml");
        
        assertNotNull(res);

        addNamespace("h", "http://service.jaxws.cxf.apache.org");
        assertValid("//s:Body/h:sayHiResponse/h:out", res);
    }
}
