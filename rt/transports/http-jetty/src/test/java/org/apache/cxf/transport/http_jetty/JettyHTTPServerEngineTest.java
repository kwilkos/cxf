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



import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

import org.apache.cxf.Bus;
import org.apache.cxf.configuration.Configurer;
import org.apache.cxf.configuration.jsse.TLSServerParameters;
import org.apache.cxf.configuration.spring.ConfigurerImpl;
import org.apache.cxf.helpers.IOUtils;
import org.easymock.classextension.EasyMock;
import org.easymock.classextension.IMocksControl;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mortbay.jetty.handler.ContextHandler;

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
    
    @Test 
    public void testaddServants() throws Exception {
        String urlStr = "http://localhost:1234/hello/test";
        JettyHTTPServerEngine engine = 
            factory.createJettyHTTPServerEngine(1234);
        JettyHTTPTestHandler handler1 = new JettyHTTPTestHandler("string1");
        JettyHTTPTestHandler handler2 = new JettyHTTPTestHandler("string2");        
        engine.addServant(new URL(urlStr), handler1);
        String response = null;
        try {
            response = getResponse(urlStr);
        } catch (Exception ex) {
            fail("Can't get the reponse from the server " + ex);
        }
        assertEquals("the jetty http handler did not take effect", response, "string1");
        
        engine.addServant(new URL(urlStr), handler2);
        try {
            response = getResponse(urlStr);
        } catch (Exception ex) {
            fail("Can't get the reponse from the server " + ex);
        }
        assertEquals("the jetty http handler did not take effect", response, "string1string2");
        
        
        // set the get request
        factory.destroyForPort(1234);       
        
    }
    
    @Test 
    public void testGetContextHandler() throws Exception {
        String urlStr = "http://localhost:1234/hello/test";
        JettyHTTPServerEngine engine = 
            factory.createJettyHTTPServerEngine(1234);
        ContextHandler contextHandler = engine.getContextHandler(new URL(urlStr));
        assertNull(contextHandler);
        JettyHTTPTestHandler handler1 = new JettyHTTPTestHandler("string1");
        JettyHTTPTestHandler handler2 = new JettyHTTPTestHandler("string2");
        engine.addServant(new URL(urlStr), handler1);
        
        contextHandler = engine.getContextHandler(new URL(urlStr));
        contextHandler.setHandler(handler2);
        contextHandler.start();
        
        String response = null;
        try {
            response = getResponse(urlStr);
        } catch (Exception ex) {
            fail("Can't get the reponse from the server " + ex);
        }
        assertEquals("the jetty http handler did not take effect", response, "string2");
        factory.destroyForPort(1234);
    }
    
    private String getResponse(String target) throws Exception {
        URL url = new URL(target);        
        
        URLConnection connection = url.openConnection();            
        
        assertTrue(connection instanceof HttpURLConnection);
        connection.connect(); 
        InputStream in = connection.getInputStream();
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        IOUtils.copy(in, buffer);
        return buffer.toString();
    }
    
}
