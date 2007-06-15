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

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.apache.cxf.Bus;
import org.apache.cxf.common.logging.LogUtils;
import org.apache.cxf.configuration.jsse.TLSServerParameters;
import org.apache.cxf.transport.http_jetty.spring.JettyHTTPServerEngineFactoryConfig;

/**
 * This Bus Extension handles the configuration of network port
 * numbers for use with "http" or "https". This factory 
 * caches the JettyHTTPServerEngines so that they may be 
 * retrieved if already previously configured.
 */
public class JettyHTTPServerEngineFactory {
    private static final Logger LOG =
        LogUtils.getL7dLogger(JettyHTTPServerEngineFactory.class);
    
    private static final int FALLBACK_THREADING_PARAMS_KEY = 0;

    /**
     * This map holds references for allocated ports.
     */
    // All system tests do not shut down bus correctly,
    // or the bus does not shutdown all endpoints correctly,
    // so that these server endings are actuall shared amongst busses
    // within the same JVM.
    
    // We will keep it static until we can resolve the problems 
    // in the System tests.
    // TODO: Fix the System Tests so that they shutdown the 
    // buses that they are using and that the buses actually
    // shutdown the destinations and their server engines
    // properly. This will require a bit of lifecyle and reference
    // counting on Destinations to server engines, if they are 
    // going to be shared, but they should by no means be 
    // shared accross buses, right?
    private static Map<Integer, JettyHTTPServerEngine> portMap =
        new HashMap<Integer, JettyHTTPServerEngine>();
   
    
    /**
     * This map holds the threading parameters that are to be applied
     * to new Engines when bound to a specified port.
     */
    private Map<Integer, ThreadingParameters> threadingParametersMap =
        new TreeMap<Integer, ThreadingParameters>();
    
    /**
     * This map holds TLS Server Parameters that are to be used to
     * configure a subsequently created JettyHTTPServerEngine.
     */
    private Map<Integer, TLSServerParameters> tlsParametersMap =
        new TreeMap<Integer, TLSServerParameters>();
    
    /**
     * The bus.
     */
    private Bus bus;
    
    private JettyHTTPServerEngineFactoryConfig factorySpringConfig;
    
    public JettyHTTPServerEngineFactory() {
        // Empty
    }
    
    /**
     * This call is used to set the bus. It should only be called once.
     * @param bus
     */
    @Resource(name = "bus")
    public void setBus(Bus bus) {
        assert this.bus == null;
        this.bus = bus;
    }
    
    @Resource
    public void setConfig(JettyHTTPServerEngineFactoryConfig config) {
        factorySpringConfig = config;
    }
    
    @PostConstruct
    public void registerWithBus() {
        bus.setExtension(this, JettyHTTPServerEngineFactory.class);
    }
    
    @PostConstruct
    public void configureSpring() {
        if (factorySpringConfig != null) {
            factorySpringConfig.configureServerEngineFactory(this);
        }
    }
    
    /**
     * This call sets TLSServerParameters for a JettyHTTPServerEngine
     * that will be subsequently created. It will not alter an engine
     * that has already been created for that network port.
     * @param port       The network port number to bind to the engine.
     * @param tlsParams  The tls server parameters. Cannot be null.
     */
    public void setTLSServerParametersForPort(
        int port, 
        TLSServerParameters tlsParams) {
        if (tlsParams == null) {
            throw new IllegalArgumentException("tlsParams cannot be null");
        }
        tlsParametersMap.put(port, tlsParams);
    }

    /**
     * This call removes any TLSParameters that have been placed
     * on the port arguments. This call will not affect a server engine
     * already in existence for that port. 
     * @param port
     */
    public void removeTLSServerParametersForPort(int port) {
        tlsParametersMap.remove(port);
    }
    
    /**
     * This call sets the ThreadingParameters for a JettyHTTPServerEngine
     * that will be subsequently created. It will not alter an
     * engine that has already ben creatd for that network port.
     * @param port            The network port number to bind to the engine.
     * @param threadingParams The threading parameters. Cannot be null.
     */
    public void setThreadingParametersForPort(
        int port,
        ThreadingParameters threadingParams) {
        if (threadingParams == null) {
            throw new IllegalArgumentException(
                    "threadingParameters cannot be null");
        }
        threadingParametersMap.put(port, threadingParams);
    }
    
    /**
     * This call removes any ThreadingParameters that have been placed
     * on the port arguments. This call will not affect a server engine
     * already in existence for that port. 
     * @param port
     */
    public void removeThreadingParametersForPort(int port) {
        threadingParametersMap.remove(port);
    }
    
    /**
     * This call retrieves a previously configured JettyHTTPServerEngine for the
     * given port. If none exists, this call returns null.
     */
    synchronized JettyHTTPServerEngine retrieveJettyHTTPServerEngine(int port) {
        return portMap.get(port);
    }

    /**
     * This call creates a new JettyHTTPServerEngine initialized for "http"
     * or "https" on the given port. The determination of "http" or "https"
     * will depend on configuration of the engine's bean name.
     * 
     * If an JettyHTTPEngine already exists, or the port
     * is already in use, a BindIOException will be thrown. If the 
     * engine is being Spring configured for TLS a GeneralSecurityException
     * may be thrown.
     */
    synchronized JettyHTTPServerEngine createJettyHTTPServerEngine(int port)
        throws GeneralSecurityException, IOException {
        
        TLSServerParameters tlsParams = tlsParametersMap.get(port);

        if (tlsParams != null) {
            throw new RuntimeException("Port " 
                    + port + " is configured for TLS");
        }
        
        JettyHTTPServerEngine ref = 
            new JettyHTTPServerEngine(this, bus, port);

        ref.setProgrammaticTlsServerParameters(tlsParams);
        
        applyThreadingParameters(ref, port);
        
        ref.finalizeConfig();
        portMap.put(port, ref);
        return ref;
    }

    /**
     * This call creates a new JettyHTTPServerEngine initialized for "https"
     * on the given port. The configuration for "https"
     * will depend on configuration of the engine's bean name, then default,
     * then parameter map.
     * 
     * If an JettyHTTPEngine already exists, or the port
     * is already in use, a BindIOException will be thrown. If the 
     * engine is being Spring configured for TLS a GeneralSecurityException
     * may be thrown.
     */
    synchronized JettyHTTPServerEngine createJettyHTTPSServerEngine(int port)
        throws GeneralSecurityException, IOException {

        LOG.fine("Creating Jetty HTTP Server Engine for port " + port + ".");
        
        JettyHTTPServerEngine ref = 
            new JettyHTTPServerEngine(this, bus, port);
        
        // Configuration of the Factory 
        TLSServerParameters tlsParams = tlsParametersMap.get(port);
        if (tlsParams != null) {
            ref.setProgrammaticTlsServerParameters(tlsParams);
        } else {
            throw new RuntimeException("Port " 
                    + port + " is not configured for TLS");
        }
        
        applyThreadingParameters(ref, port);
        
        ref.finalizeConfig();
        portMap.put(port, ref);
        return ref;
    }

    /**
     * Apply the thread paramaters to the newly created engine,
     * falling back to non-port specific values if necessary.
     * 
     * @param engine the new created engine
     * @param port the listen port
     */
    protected void applyThreadingParameters(JettyHTTPServerEngine engine, int port) {
        ThreadingParameters params = threadingParametersMap.get(port);
        if (params != null) {
            engine.setThreadingParameters(params);
        } else {
            ThreadingParameters fallback =
                threadingParametersMap.get(FALLBACK_THREADING_PARAMS_KEY);
            if (fallback != null) {
                engine.setThreadingParameters(fallback);
            }
        }
    }
    
    /**
     * This method removes the Server Engine from the port map and stops it.
     */
    public synchronized void destroyForPort(int port) {
        JettyHTTPServerEngine ref = portMap.remove(port);
        if (ref != null) {
            LOG.fine("Stopping Jetty HTTP Server Engine for port " + port + ".");
            try {
                ref.stop();
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }            
        }
    }

    @PostConstruct
    public void finalizeConfig() {
        registerWithBus();
    }
    
}
