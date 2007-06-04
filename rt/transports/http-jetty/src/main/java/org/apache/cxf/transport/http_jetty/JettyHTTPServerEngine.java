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

import org.apache.cxf.Bus;
import org.apache.cxf.configuration.jsse.TLSServerParameters;
import org.apache.cxf.configuration.jsse.spring.TLSServerParametersConfig;
import org.apache.cxf.configuration.security.SSLServerPolicy;
import org.apache.cxf.configuration.security.TLSServerParametersType;
import org.apache.cxf.transport.HttpUriMapper;
import org.apache.cxf.transports.http.configuration.HTTPListenerPolicy;
import org.mortbay.jetty.AbstractConnector;
import org.mortbay.jetty.Handler;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.handler.ContextHandler;
import org.mortbay.jetty.handler.ContextHandlerCollection;
import org.mortbay.thread.BoundedThreadPool;



public class JettyHTTPServerEngine
    implements ServerEngine {
    private static final long serialVersionUID = 1L;
   
    private int servantCount;
    private Server server;
    private AbstractConnector connector;
    private JettyConnectorFactory connectorFactory;
    private ContextHandlerCollection contexts;
    
    private HTTPListenerPolicy listener;
    private SSLServerPolicy sslServer;
    private TLSServerParametersType tlsServerParameters;
    
    /**
     * This field holds the protocol this engine is for. "http" or "https".
     */
    private final String protocol;
    
    private final int port;
    
    /**
     * This field holds the TLS ServerParameters that are programatically
     * configured. The tlsServerParamers (due to JAXB) holds the struct
     * placed by SpringConfig.
     */
    private TLSServerParameters tlsProgrammaticServerParameters;
    
    /**
     * This boolean signfies that SpringConfig is over. finalizeConfig
     * has been called.
     */
    private boolean configFinalized;
    
    /**
     * This is the Server Engine Factory. This factory caches some 
     * engines based on port numbers.
     */
    private JettyHTTPServerEngineFactory factory;
    
    JettyHTTPServerEngine(JettyHTTPServerEngineFactory fac, Bus bus,
            String proto, int p) {
        factory = fac;
        protocol = proto;
        port = p;
    }

    // TODO: remove when old SSL config is gone.
    @Deprecated
    JettyHTTPServerEngine(JettyHTTPServerEngineFactory fac, Bus bus,
            String proto, int p, SSLServerPolicy policy) {
        factory = fac;
        sslServer = policy;
        protocol = proto;
        port = p;
    }

    JettyHTTPServerEngine(JettyHTTPServerEngineFactory fac, Bus bus,
            String proto, int p, TLSServerParameters params) {
        factory = fac;
        tlsProgrammaticServerParameters = params;
        protocol = proto;
        port = p;
    }
    
    public String getBeanName() {
        return JettyHTTPServerEngine.class.getName() + "." + port;
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
                    && isSetListener()) {
                    BoundedThreadPool pool = (BoundedThreadPool)connector.getThreadPool();
                    if (getListener().isSetMinThreads()) {
                        pool.setMinThreads(getListener().getMinThreads());
                    }
                    if (getListener().isSetMaxThreads()) {
                        pool.setMaxThreads(getListener().getMaxThreads());
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
        // REVISIT: how come server can be null?
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
            System.err.println("Not able to remove the handler");
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
        // REVISIT: how come server can be null?
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
            connectorFactory = JettyHTTPTransportFactory
                    .getConnectorFactory(tlsProgrammaticServerParameters);
        // TODO: remove when old SSL Config is gone.
        } else if (isSetSslServer()) {
            connectorFactory = JettyHTTPTransportFactory
                    .getConnectorFactory(getSslServer());
        } else {
            connectorFactory = JettyHTTPTransportFactory
                    .getConnectorFactory((TLSServerParameters) null);
        }
    }
    
    /**
     * This method is called after configure on this object.
     */
    protected void finalizeConfig() throws GeneralSecurityException,
            IOException {

        // If the listener was spring configured, convert those structs
        // to real configuration with KeyManagers and TrustManagers.
        if (this.tlsProgrammaticServerParameters == null
                && isSetTlsServerParameters()) {
            tlsProgrammaticServerParameters = 
                new TLSServerParametersConfig(getTlsServerParameters());
        }
        if (!isSetListener()) {
            setListener(new HTTPListenerPolicy());
        }
        if ("https".equals(protocol)
                && tlsProgrammaticServerParameters == null 
                && !isSetSslServer()) {
            throw new RuntimeException(
                    "Protocol is \"https\" without suitable "
                            + "programmatic or spring configuration.");
        }
        retrieveListenerFactory();
        this.configFinalized = true;
    }
    
    @Deprecated
    protected void init(SSLServerPolicy sslServerPolicy) {
        if (!isSetSslServer()) {
            setSslServer(sslServerPolicy);
        }
        if (!isSetListener()) {
            setListener(new HTTPListenerPolicy());
        }
    }
    
    @Deprecated
    public void setSslServer(SSLServerPolicy policy) {
        this.sslServer = policy;
        if (this.configFinalized) {
            this.retrieveListenerFactory();
        }
    }
    /**
     * This method is called to possibly reconfigure a listener. 
     */
    protected void reconfigure(String proto, TLSServerParameters tlsParams) {
        if (!getProtocol().equals(proto)) {
            throw new RuntimeException(
                    "Cannot reconfigure an allocated server port with "
                    + "different protocol."
                    + " Port: " + port + " to Protocol " + proto);
        }
        if ("https".equals(proto)) {
            // TLS/SSL Parameters have not yet been set.
            if (tlsProgrammaticServerParameters == null) {
                if (!isSetSslServer()) {
                    try {
                        setProgrammaticTlsServerParameters(tlsParams);
                    } catch (Exception e) {
                        throw new RuntimeException(
                                "Could not initialize configuration of "
                                + getBeanName() + ".", e);
                    }
                } else {
                    throw new RuntimeException(
                        "Cannot reconfigure an allocated TLS server port. "
                        + "Port = " + port);
                }
            } else if (tlsProgrammaticServerParameters != tlsParams) {
                throw new RuntimeException(
                    "Cannot reconfigure an allocated TLS server port. "
                    + "Port = " + port);
            }
        }
        
    }

    /**
     * This method is called to possibly reconfigure a listener. 
     * @param proto
     * @param policy
     */
    @Deprecated
    protected void reconfigure(String proto, SSLServerPolicy policy) {
        if (!getProtocol().equals(proto)) {
            throw new RuntimeException(
                    "Cannot reconfigure an allocated server port with "
                    + "different protocol."
                    + " Port: " + port + " to Protocol " + proto);
        }
        if ("https".equals(proto)) {
            // TLS/SSL Parameters have not yet been set.
            if (!isSetSslServer()) {
                if (tlsProgrammaticServerParameters == null) {
                    try {
                        setSslServer(policy);
                    } catch (Exception e) {
                        throw new RuntimeException(
                                "Could not initialize configuration of "
                                + getBeanName() + ".", e);
                    }
                } else {
                    throw new RuntimeException(
                            "Cannot reconfigure an allocated TLS server port. "
                            + "Port = " + port);
                }
            } else if (getSslServer() != policy) {
                throw new RuntimeException(
                    "Cannot reconfigure an allocated TLS server port. Port = " 
                    + port);
            }
        }
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
            listener = null;
        }
    }
    
    /**
     * This method is used to programmatically set the TLSServerParameters.
     * This method must be used to dynamically configure the http-listener.
     */
    public void setProgrammaticTlsServerParameters(TLSServerParameters params) {
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
    public TLSServerParameters getProgrammaticTlsServerParameters() {
        return tlsProgrammaticServerParameters;
    } 

    /**
     * Returns the listener policy
     *  @return the listener policy.
     */
    public HTTPListenerPolicy getListener() {
        return listener;
    }

    /**
     * Sets the listener policy.
     * @param listener The listener policy to set.
     */
    public void setListener(HTTPListenerPolicy listener) {
        this.listener = listener;
    }

    /** 
     * Returns the tlsServerParameters.
     * @return the tlsServerParameters.
     */
    public TLSServerParametersType getTlsServerParameters() {
        return tlsServerParameters;
    }

    /**
     * Sets the tlsServerParameters.
     * @param tlsServerParameters The tlsServerParameters to set.
     */
    public void setTlsServerParameters(TLSServerParametersType tlsServerParameters) {
        this.tlsServerParameters = tlsServerParameters;
    }

    /** 
     * Returns the sslServer policy.
     * @return the sslServer policy.
     */
    public SSLServerPolicy getSslServer() {
        return sslServer;
    }
    
    public boolean isSetListener() {
        return null != listener;
    }
    
    public boolean isSetSslServer() {
        return null != sslServer;
    }
    
    public boolean isSetTlsServerParameters() {
        return null != tlsServerParameters; 
    }
    
    
}
