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
package org.apache.cxf.transport.http_jetty.spring;

import javax.xml.namespace.QName;

import org.xml.sax.SAXParseException;

import org.apache.cxf.Bus;
import org.apache.cxf.configuration.Configurer;
import org.apache.cxf.configuration.spring.ConfigurerImpl;
import org.apache.cxf.service.model.EndpointInfo;
import org.apache.cxf.service.model.ServiceInfo;
import org.apache.cxf.test.TestApplicationContext;
import org.apache.cxf.transport.ConduitInitiator;
import org.apache.cxf.transport.ConduitInitiatorManager;
import org.apache.cxf.transport.Destination;
import org.apache.cxf.transport.DestinationFactoryManager;
import org.apache.cxf.transport.http.HTTPConduit;
import org.apache.cxf.transport.http_jetty.JettyHTTPDestination;
import org.apache.cxf.transport.http_jetty.JettyHTTPServerEngine;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.xml.XmlBeanDefinitionStoreException;


public class ApplicationContextTest extends Assert {
    
    private static final String S1 = 
        ApplicationContextTest.class.getResource("/META-INF/cxf/cxf.xml").toString();
    private static final String S2 = 
        ApplicationContextTest.class.getResource("/META-INF/cxf/cxf-extension-http.xml").toString();
    private static final String S3 = 
        ApplicationContextTest.class.getResource("/META-INF/cxf/cxf-extension-http-jetty.xml").toString();
    
    @Ignore
    @Test
    public void testInvalid() throws Exception {
        String s4 = getClass()
            .getResource("/org/apache/cxf/transport/http_jetty/spring/invalid-beans.xml").toString();
    
        try {
            new TestApplicationContext(new String[] {S1, S2, S3, s4});
            fail("Expected XmlBeanDefinitionStoreException not thrown.");
        } catch (XmlBeanDefinitionStoreException ex) {
            assertTrue(ex.getCause() instanceof SAXParseException);
        }
    }
    
    @Test
    public void testContext() throws Exception {
        String s4 = getClass()
            .getResource("/org/apache/cxf/transport/http_jetty/spring/beans.xml").toString();
        
        TestApplicationContext ctx = new TestApplicationContext(
            new String[] {S1, S2, S3, s4});
        
        ctx.refresh();
        
        ConfigurerImpl cfg = new ConfigurerImpl(ctx);
        
        ServiceInfo serviceInfo = new ServiceInfo();
        serviceInfo.setName(new QName("bla", "Service"));        
        EndpointInfo info = new EndpointInfo(serviceInfo, "");
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
        
        JettyHTTPServerEngine engine = (JettyHTTPServerEngine)jd.getEngine();
        assertEquals(111, engine.getListener().getMinThreads());
        
        ConduitInitiatorManager cim = bus.getExtension(ConduitInitiatorManager.class);
        ConduitInitiator ci = cim.getConduitInitiator("http://schemas.xmlsoap.org/soap/http");
        HTTPConduit conduit = (HTTPConduit) ci.getConduit(info);
        assertEquals(97, conduit.getClient().getConnectionTimeout());
        
        info.setName(new QName("urn:test:ns", "Bar"));
        conduit = (HTTPConduit) ci.getConduit(info);
        assertEquals(79, conduit.getClient().getConnectionTimeout());
        
    }
}
