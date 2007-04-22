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
import org.apache.cxf.configuration.security.SSLServerPolicy;
import org.apache.cxf.configuration.spring.ConfigurerImpl;
import org.easymock.classextension.EasyMock;
import org.easymock.classextension.IMocksControl;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class JettyHTTPServerEngineTest extends Assert {

    private Bus bus;
    private IMocksControl control;
    
    @Before
    public void setUp() throws Exception {
        control = EasyMock.createNiceControl();
        bus = control.createMock(Bus.class);
        
        Configurer configurer = new ConfigurerImpl(); 
        
        bus.getExtension(Configurer.class);                        
        EasyMock.expectLastCall().andReturn(configurer).anyTimes();    
        control.replay();   
    }
    
    @Test
    public void testEngineEquality() {
        JettyHTTPServerEngine engine = JettyHTTPServerEngine.getForPort(bus, "http", 1234);
        assertTrue("Engine references for the same port should point to the same instance",
                   engine == JettyHTTPServerEngine.getForPort(bus, "http", 1234));
        assertFalse("Engine references for the different ports should point to diff instances",
                   engine == JettyHTTPServerEngine.getForPort(bus, "http", 1235));    
        JettyHTTPServerEngine.destroyForPort(1234);
        JettyHTTPServerEngine.destroyForPort(1235);
    }
    
    @Test
    public void testNoSSLServerPolicySet() {
        JettyHTTPServerEngine engine = JettyHTTPServerEngine.getForPort(bus, "http", 1234);
        assertFalse("SSLServerPolicy must not be set", engine.isSetSslServer());
        engine = JettyHTTPServerEngine.getForPort(bus, "http", 1235, null);
        assertFalse("SSLServerPolicy must not be set", engine.isSetSslServer());
        JettyHTTPServerEngine engine2 = JettyHTTPServerEngine.getForPort(bus, "http", 1234, 
                                                   new SSLServerPolicy());
        assertFalse("SSLServerPolicy must not be set for already intialized engine", 
                    engine2.isSetSslServer());
        JettyHTTPServerEngine.destroyForPort(1234);
        JettyHTTPServerEngine.destroyForPort(1235);
    }
    
    @Test
    public void testDestinationSSLServerPolicy() {
        SSLServerPolicy policy = new SSLServerPolicy();
        JettyHTTPServerEngine engine = JettyHTTPServerEngine.getForPort(bus, "http", 1234, 
                                                                        policy);
        assertTrue("SSLServerPolicy must be set", engine.getSslServer() == policy);
        JettyHTTPServerEngine engine2 = JettyHTTPServerEngine.getForPort(bus, "http", 1234, 
                                                   new SSLServerPolicy());
        assertTrue("Engine references for the same port should point to the same instance",
                   engine == engine2);
        assertTrue("SSLServerPolicy must not be set for already intialized engine", 
                    engine.getSslServer() == policy);
        
        JettyHTTPServerEngine.destroyForPort(1234);
    }

}
