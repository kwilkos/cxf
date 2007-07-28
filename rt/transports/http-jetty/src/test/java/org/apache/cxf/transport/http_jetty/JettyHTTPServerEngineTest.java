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
import java.util.ArrayList;
import java.util.List;

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
import org.mortbay.jetty.Connector;
import org.mortbay.jetty.Handler;
import org.mortbay.jetty.handler.ContextHandler;
import org.mortbay.jetty.nio.SelectChannelConnector;
import org.mortbay.jetty.security.SslSocketConnector;

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
            factory.createJettyHTTPServerEngine(1234, "http");
        
        assertTrue(
            "Engine references for the same port should point to the same instance",
            engine == factory.retrieveJettyHTTPServerEngine(1234));
        
        factory.destroyForPort(1234);
    }
    
    @Test
    public void testHttpAndHttps() throws Exception {
        JettyHTTPServerEngine engine = 
            factory.createJettyHTTPServerEngine(1234, "http");
        
        assertTrue("Protocol must be http", 
                "http".equals(engine.getProtocol()));
        
        engine = new JettyHTTPServerEngine();
        engine.setPort(1235);
        engine.setTlsServerParameters(new TLSServerParameters());
        engine.finalizeConfig();
        
        List<JettyHTTPServerEngine> list = new ArrayList<JettyHTTPServerEngine>();
        list.add(engine);
        factory.setEnginesList(list);
        
        engine = factory.createJettyHTTPServerEngine(1235, "https");
        
        assertTrue("Protocol must be https", 
                "https".equals(engine.getProtocol()));
        
        
        factory.destroyForPort(1234);
        factory.destroyForPort(1235);
    }
    
    
    @Test 
    public void testSetConnector() throws Exception {
        JettyHTTPServerEngine engine = new JettyHTTPServerEngine();
        Connector conn = new SslSocketConnector();
        engine.setConnector(conn);
        engine.setPort(9000);
        try {
            engine.finalizeConfig();
            fail("We should get the connector not set with TSLServerParament exception ");
        } catch (Exception ex) {
            // expect the excepion            
        }
        
        engine = new JettyHTTPServerEngine();        
        conn = new SelectChannelConnector();
        conn.setPort(9002);
        engine.setConnector(conn);
        engine.setPort(9000);
        try {
            engine.finalizeConfig();
            fail("We should get the connector not set right port exception ");
        } catch (Exception ex) {
            // expect the excepion            
        }
        
        engine = new JettyHTTPServerEngine();
        conn = new SslSocketConnector();
        conn.setPort(9003);
        engine.setConnector(conn);
        engine.setPort(9003);
        engine.setTlsServerParameters(new TLSServerParameters());
        try {
            engine.finalizeConfig();
        } catch (Exception ex) {
            fail("we should not throw exception here");
        }
    }
    
       
    
    @Test 
    public void testaddServants() throws Exception {
        String urlStr = "http://localhost:1234/hello/test";
        JettyHTTPServerEngine engine = 
            factory.createJettyHTTPServerEngine(1234, "http");
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
    public void testSetHandlers() throws Exception {
        URL url = new URL("http://localhost:1245/hello/test");
        JettyHTTPTestHandler handler1 = new JettyHTTPTestHandler("string1");
        JettyHTTPTestHandler handler2 = new JettyHTTPTestHandler("string2");
        
        JettyHTTPServerEngine engine = new JettyHTTPServerEngine();
        engine.setPort(1245);
        
        List<Handler> handlers = new ArrayList<Handler>();
        handlers.add(handler1);
        engine.setHandlers(handlers);
        engine.finalizeConfig();
        
        engine.addServant(url, handler2);
        String response = null;
        try {
            response = getResponse(url.toString());
            assertEquals("the jetty http handler1 did not take effect", response, "string1string2");
        } catch (Exception ex) {
            fail("Can't get the reponse from the server " + ex);
        }
        engine.stop();
    }
    
    @Test 
    public void testGetContextHandler() throws Exception {
        String urlStr = "http://localhost:1234/hello/test";
        JettyHTTPServerEngine engine = 
            factory.createJettyHTTPServerEngine(1234, "http");
        ContextHandler contextHandler = engine.getContextHandler(new URL(urlStr));
        // can't find the context handler here
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
