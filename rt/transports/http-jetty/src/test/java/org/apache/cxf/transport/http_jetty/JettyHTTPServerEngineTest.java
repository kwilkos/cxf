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

package org.apache.cxf.transport.http_jetty;


import org.apache.cxf.Bus;
import org.apache.cxf.configuration.Configurer;
import org.apache.cxf.configuration.jsse.TLSServerParameters;
import org.apache.cxf.configuration.spring.ConfigurerImpl;
import org.easymock.classextension.EasyMock;
import org.easymock.classextension.IMocksControl;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class JettyHTTPServerEngineTest extends Assert {

    private Bus bus;
    private IMocksControl control;
    private JettyHTTPServerEngineFactory factory;
    
    @Before
    public void setUp() throws Exception {
        control = EasyMock.createNiceControl();
        bus = control.createMock(Bus.class);
        factory = new JettyHTTPServerEngineFactory();
        factory.setBus(bus);
        
        Configurer configurer = new ConfigurerImpl(); 
        
        bus.getExtension(Configurer.class);                        
        EasyMock.expectLastCall().andReturn(configurer).anyTimes();    
        control.replay();   
    }
    
    @Test
    public void testEngineRetrieval() throws Exception {
        JettyHTTPServerEngine engine = 
            factory.createJettyHTTPServerEngine(1234);
        
        assertTrue(
            "Engine references for the same port should point to the same instance",
            engine == factory.retrieveJettyHTTPServerEngine(1234));
        
        factory.destroyForPort(1234);
    }
    
    @Test
    public void testHttpAndHttps() throws Exception {
        JettyHTTPServerEngine engine = 
            factory.createJettyHTTPServerEngine(1234);
        
        assertTrue("Protocol must be http", 
                "http".equals(engine.getProtocol()));
        
        factory.setTLSServerParametersForPort(1235, new TLSServerParameters());
        
        engine = factory.createJettyHTTPSServerEngine(1235);
        
        assertTrue("Protocol must be https", 
                "https".equals(engine.getProtocol()));
        
        factory.removeTLSServerParametersForPort(1235);
        factory.destroyForPort(1234);
        factory.destroyForPort(1235);
    }
    
}
