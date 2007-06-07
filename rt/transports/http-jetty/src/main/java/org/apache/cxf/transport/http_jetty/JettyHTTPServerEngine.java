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
import java.net.URL;
import java.security.GeneralSecurityException;
import java.util.logging.Logger;

import org.apache.cxf.Bus;
import org.apache.cxf.common.logging.LogUtils;
import org.apache.cxf.configuration.jsse.TLSServerParameters;
import org.apache.cxf.transport.HttpUriMapper;
import org.apache.cxf.transport.https_jetty.JettySslConnectorFactory;
import org.mortbay.jetty.AbstractConnector;
import org.mortbay.jetty.Handler;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.handler.ContextHandler;
import org.mortbay.jetty.handler.ContextHandlerCollection;
import org.mortbay.jetty.nio.SelectChannelConnector;
import org.mortbay.thread.BoundedThreadPool;


/**
 * This class is the Jetty HTTP Server Engine that is configured to
 * work off of a designated port. The port will be enabled for 
 * "http" or "https" depending upon its successful configuration.
 */
public class JettyHTTPServerEngine
    implements ServerEngine {
    private static final long serialVersionUID = 1L;
    
    private static final Logger LOG =
        LogUtils.getL7dLogger(JettyHTTPServerEngine.class);
   

    /**
     * The bus.
     */
    private final Bus bus;
    
    /**
     * This is the Jetty HTTP Server Engine Factory. This factory caches some 
     * engines based on port numbers.
     */
    private final JettyHTTPServerEngineFactory factory;
    
    
    /**
     * This is the network port for which this engine is allocated.
     */
    private final int port;
    
    /**
     * This field holds the protocol for which this engine is 
     * enabled, i.e. "http" or "https".
     */
    private String protocol;
    
    private int servantCount;
    private Server server;
    private AbstractConnector connector;
    private JettyConnectorFactory connectorFactory;
    private ContextHandlerCollection contexts;
    
    /**
     * This field holds the TLS ServerParameters that are programatically
     * configured. The tlsServerParamers (due to JAXB) holds the struct
     * placed by SpringConfig.
     */
    private TLSServerParameters tlsProgrammaticServerParameters;
    
    /**
     * This field hold the threading parameters for this particular engine.
     */
    private ThreadingParameters threadingParameters;
    
    /**
     * This boolean signfies that SpringConfig is over. finalizeConfig
     * has been called.
     */
    private boolean configFinalized;
    
    /**
     * This constructor is solely called by the JettyHTTPServerEngineFactory.
     */
    JettyHTTPServerEngine(
        JettyHTTPServerEngineFactory fac, 
        Bus bus,
        int port) {
        this.bus     = bus;
        this.factory = fac;
        this.port    = port;
    }
    
    /**
     * The bus.
     */
    public Bus getBus() {
        return bus;
    }
    
    /**
     * Returns the protocol "http" or "https" for which this engine
     * was configured.
     */
    public String getProtocol() {
        return protocol;
    }
    
    /**
     * Returns the port number for which this server engine was configured.
     * @return
     */
    public int getPort() {
        return port;
    }
    
    /**
     * This method will shut down the server engine and
     * remove it from the factory's cache. 
     */
    public void shutdown() {
        factory.destroyForPort(port);
    }
    
    /**
     * Register a servant.
     * 
     * @param url the URL associated with the servant
     * @param handler notified on incoming HTTP requests
     */
    public synchronized void addServant(URL url, JettyHTTPHandler handler) {
        if (server == null) {
            server = new Server();
            
            connector = connectorFactory.createConnector(port);
            //REVISITION for setup the connector's threadPool
            /*
            if (getListener().isSetMaxIdleTimeMs()) {
                listener.setMaxIdleTimeMs(getListener().getMaxIdleTimeMs().intValue());
            }
            if (getListener().isSetLowResourcePersistTimeMs()) {
                int lowResourcePersistTime = 
                    getListener().getLowResourcePersistTimeMs().intValue();
                listener.setLowResourcePersistTimeMs(lowResourcePersistTime);
            }*/

            server.addConnector(connector);
            contexts = new ContextHandlerCollection();
            server.addHandler(contexts);
            try {
                server.start();
                if (connector.getThreadPool() instanceof BoundedThreadPool
                    && isSetThreadingParameters()) {
                    BoundedThreadPool pool = (BoundedThreadPool)connector.getThreadPool();
                    if (getThreadingParameters().isSetMinThreads()) {
                        pool.setMinThreads(getThreadingParameters().getMinThreads());
                    }
                    if (getThreadingParameters().isSetMaxThreads()) {
                        pool.setMaxThreads(getThreadingParameters().getMaxThreads());
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                //problem starting server
                try {                    
                    server.stop();
                    server.destroy();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }    
            }
        }
        
        String contextName = HttpUriMapper.getContextName(url.getPath());
        final String smap = HttpUriMapper.getResourceBase(url.getPath());
        ContextHandler context = new ContextHandler();
        context.setContextPath(contextName);
        context.setHandler(handler);
        contexts.addHandler(context);
        if (contexts.isStarted()) {           
            try {                
                context.start();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        
        handler.setName(smap);        
        ++servantCount;
    }
    
    /**
     * Remove a previously registered servant.
     * 
     * @param url the URL the servant was registered against.
     */
    public synchronized void removeServant(URL url) {        
        
        String contextName = HttpUriMapper.getContextName(url.getPath());
        //final String smap = HttpUriMapper.getResourceBase(url.getPath());

        boolean found = false;
        // REVISIT:After a stop(), the server is null, and therefore this 
        // operation shouldn't find a handler, but then below, why do we
        // print a message to Std Error?
        if (server != null) {
            for (Handler handler : contexts.getChildHandlersByClass(ContextHandler.class)) {
                ContextHandler contextHandler = null;
                if (handler instanceof ContextHandler) {
                    contextHandler = (ContextHandler) handler;
                    if (contextName.equals(contextHandler.getContextPath())) {
                        try {
                            contexts.removeHandler(handler);
                            handler.stop();
                            handler.destroy();
                        } catch (Exception e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                        /*for (Handler httpHandler
                            : contextHandler.getChildHandlersByClass(JettyHTTPHandler.class)) {
                            if (smap.equals(((JettyHTTPHandler)httpHandler).getName())) {
                                contexts.removeHandler(httpHandler);
                                try {
                                    handler.stop();                               
                                    handler.destroy();
                                } catch (Exception e) {
                                    // TODO Auto-generated catch block
                                    e.printStackTrace();
                                }    
                            }
                        
                        }*/
                    }
                    found = true;
                }
            }
        }
        if (!found) {
            LOG.warning("Not able to remove the handler");
        }
        
        --servantCount;
        /* Bug in Jetty, we cannot do this.  If we restart later, data goes off
         * someplace unknown
        if (servantCount == 0) {
            server.removeListener(listener);
        }
        */
    }

    /**
     * Get a registered servant.
     * 
     * @param url the associated URL
     * @return the HttpHandler if registered
     */
    public synchronized Handler getServant(URL url)  {
        String contextName = HttpUriMapper.getContextName(url.getPath());
        //final String smap = HttpUriMapper.getResourceBase(url.getPath());
        
        Handler ret = null;
        // After a stop(), the server is null, and therefore this 
        // operation should return null.
        if (server != null) {           
            for (Handler handler : server.getChildHandlersByClass(ContextHandler.class)) {
                ContextHandler contextHandler = null;
                if (handler instanceof ContextHandler) {
                    contextHandler = (ContextHandler) handler;
                    if (contextName.equals(contextHandler.getContextPath())) {           
                        ret = contextHandler.getHandler();
                        break;
                    }
                }
            }    
        }
        return ret;
    }

    protected void retrieveListenerFactory() {
        if (tlsProgrammaticServerParameters != null) {
            connectorFactory = 
                getHTTPSConnectorFactory(tlsProgrammaticServerParameters);
            protocol = "https";
        } else {
            connectorFactory = getHTTPConnectorFactory();
            protocol = "http";
        }
        LOG.fine("Configured port " + port + " for \"" + protocol + "\".");
    }

    /**
     * This method creates a connector factory. If there are TLS parameters
     * then it creates a TLS enabled one.
     */
    protected JettyConnectorFactory getHTTPConnectorFactory() {
        return new JettyConnectorFactory() {                     
            public AbstractConnector createConnector(int porto) {
                SelectChannelConnector result = 
                    new SelectChannelConnector();
                
                // Regardless the port has to equal the one
                // we are configured for.
                assert porto == port;
                
                //SocketConnector result = new SocketConnector();
                result.setPort(porto);
                return result;
            }
        };
    }
    
    /**
     * This method creates a connector factory enabled with the JSSE
     */
    protected JettyConnectorFactory getHTTPSConnectorFactory(
            TLSServerParameters tlsParams
    ) {
        return new JettySslConnectorFactory(tlsParams);
    }
    
    /**
     * This method is called after configure on this object.
     */
    protected void finalizeConfig() 
        throws GeneralSecurityException,
               IOException {
        retrieveListenerFactory();
        this.configFinalized = true;
    }
    
    /**
     * This method is called by the ServerEngine Factory to destroy the 
     * listener.
     *
     */
    protected void stop() throws Exception {
        if (server != null) {
            connector.close();
            server.stop();
            server.destroy();
            server   = null;
        }
    }
    
    /**
     * This method is used to programmatically set the TLSServerParameters.
     * This method may only be called by the factory.
     */
    protected void setProgrammaticTlsServerParameters(TLSServerParameters params) {
        tlsProgrammaticServerParameters = params;
        if (this.configFinalized) {
            this.retrieveListenerFactory();
        }
    }
    
    /**
     * This method returns the programmatically set TLSServerParameters, not
     * the TLSServerParametersType, which is the JAXB generated type used 
     * in SpringConfiguration.
     * @return
     */
    protected TLSServerParameters getProgrammaticTlsServerParameters() {
        return tlsProgrammaticServerParameters;
    } 

    /**
     * This method sets the threading parameters for this particular 
     * server engine.
     * This method may only be called by the factory.
     */
    public void setThreadingParameters(ThreadingParameters params) {
        threadingParameters = params;
    }
    
    /**
     * This method returns whether the threading parameters are set.
     */
    public boolean isSetThreadingParameters() {
        return threadingParameters != null;
    }
    
    /**
     * This method returns the threading parameters that have been set.
     * This method may return null, if the threading parameters have not
     * been set.
     */
    public ThreadingParameters getThreadingParameters() {
        return threadingParameters;
    }
    
}
