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

import org.w3c.dom.Node;
import org.apache.cxf.Bus;
import org.apache.cxf.jaxws.support.JaxWsServiceFactoryBean;
import org.apache.cxf.mtom_xop.HelloImpl;
import org.apache.cxf.service.Service;
import org.apache.cxf.service.factory.ServerFactoryBean;
import org.apache.cxf.transport.local.LocalTransportFactory;



public class HolderTest extends AbstractJaxWsTest {
    public void testInvocation() throws Exception {
        JaxWsServiceFactoryBean bean = new JaxWsServiceFactoryBean();

        Bus bus = getBus();
        bean.setBus(bus);
        bean.setServiceClass(HelloImpl.class);
        
        Service service = bean.create();
        service.setInvoker(new JAXWSMethodInvoker(new HelloImpl()));
        
        ServerFactoryBean svr = new ServerFactoryBean();
        svr.setBus(bus);
        svr.setServiceFactory(bean);
        svr.create();

        Node response = invoke("http://localhost:9036/mime-test",
                               LocalTransportFactory.TRANSPORT_ID, 
                               "echoData.xml");
        addNamespace("h", "http://cxf.apache.org/mime");        
        
        assertValid("//h:data", response);        
        assertValid("/s:Envelope/s:Body", response); 
        assertNotNull(response);
        assertNoFault(response);
    }
}
