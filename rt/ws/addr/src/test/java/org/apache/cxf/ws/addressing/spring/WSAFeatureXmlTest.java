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
package org.apache.cxf.ws.addressing.spring;

import org.apache.cxf.Bus;
import org.apache.cxf.BusException;
import org.apache.cxf.bus.spring.SpringBusFactory;
import org.apache.cxf.configuration.Configurer;
import org.apache.cxf.endpoint.Endpoint;
import org.apache.cxf.endpoint.Server;
import org.apache.cxf.interceptor.Interceptor;
import org.apache.cxf.jaxws.JaxWsServerFactoryBean;
import org.apache.cxf.test.AbstractCXFTest;
import org.apache.cxf.ws.addressing.MAPAggregator;
import org.apache.cxf.ws.addressing.soap.MAPCodec;
import org.apache.hello_world_soap_http.GreeterImpl;
import org.junit.Test;

public class WSAFeatureXmlTest extends AbstractCXFTest {
    
    @Override
    protected Bus createBus() throws BusException {
        return new SpringBusFactory().createBus("/org/apache/cxf/ws/addressing/spring/server.xml");
    }

    @Test
    public void testServerFactory() {
        JaxWsServerFactoryBean sf = new JaxWsServerFactoryBean();
        sf.setServiceBean(new GreeterImpl());
        sf.setAddress("http://localhost/test");
        sf.setStart(false);
        sf.setBus(getBus());
        
        Configurer c = getBus().getExtension(Configurer.class);
        c.configureBean("test", sf);
        
        Server server = sf.create();
        
        Endpoint endpoint = server.getEndpoint();
        boolean hasAg = false;
        boolean hasCodec = false;
        
        for (Interceptor i : endpoint.getInInterceptors()) {
            if (i instanceof MAPAggregator) {
                hasAg = true;
            } else if (i instanceof MAPCodec) {
                hasCodec = true;
            }
        }
        assertTrue(hasAg);
        assertTrue(hasCodec);
    }
}
