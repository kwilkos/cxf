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
package org.apache.cxf.transport.http.spring;

import javax.xml.namespace.QName;

import junit.framework.TestCase;

import org.apache.cxf.Bus;
import org.apache.cxf.configuration.Configurer;
import org.apache.cxf.configuration.spring.ConfigurerImpl;
import org.apache.cxf.service.model.EndpointInfo;
import org.apache.cxf.transport.ConduitInitiator;
import org.apache.cxf.transport.ConduitInitiatorManager;
import org.apache.cxf.transport.Destination;
import org.apache.cxf.transport.DestinationFactoryManager;
import org.apache.cxf.transport.http.HTTPConduit;
import org.apache.cxf.transport.http.JettyHTTPDestination;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class ApplicationContextTest extends TestCase {
    public void testContext() throws Exception {
        ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext(
            new String[] {"META-INF/cxf/cxf.xml", "META-INF/cxf/cxf-extension-http.xml",
                          "/org/apache/cxf/transport/http/spring/beans.xml", });
        
        ConfigurerImpl cfg = new ConfigurerImpl(ctx);
        
        EndpointInfo info = new EndpointInfo();
        info.setName(new QName("urn:test:ns", "Foo"));
        info.setAddress("http://localhost:9000");
        
        Bus bus = (Bus) ctx.getBean("cxf");
        bus.setExtension(cfg, Configurer.class);
        
        DestinationFactoryManager dfm = bus.getExtension(DestinationFactoryManager.class);
        Destination d = 
            dfm.getDestinationFactory("http://schemas.xmlsoap.org/soap/http").getDestination(info);
        assertTrue(d instanceof JettyHTTPDestination);
        JettyHTTPDestination jd = (JettyHTTPDestination) d;
        assertEquals("foobar", jd.getServer().getContentEncoding());
        
        ConduitInitiatorManager cim = bus.getExtension(ConduitInitiatorManager.class);
        ConduitInitiator ci = cim.getConduitInitiator("http://schemas.xmlsoap.org/soap/http");
        HTTPConduit conduit = (HTTPConduit) ci.getConduit(info);
        assertEquals(97, conduit.getClient().getConnectionTimeout());
        
    }
}
