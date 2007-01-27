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

package org.apache.cxf.transport.http;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.apache.cxf.Bus;
import org.apache.cxf.configuration.Configurable;
import org.apache.cxf.configuration.Configurer;
import org.apache.cxf.configuration.security.SSLServerPolicy;
import org.apache.cxf.transport.HttpUriMapper;
import org.apache.cxf.transport.http.listener.HTTPListenerConfigBean;
import org.apache.cxf.transports.http.configuration.HTTPListenerPolicy;
import org.mortbay.jetty.Handler;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.handler.AbstractHandler;
import org.mortbay.jetty.handler.ContextHandler;
import org.mortbay.jetty.handler.ContextHandlerCollection;
import org.mortbay.jetty.nio.SelectChannelConnector;




public final class JettyHTTPServerEngine extends HTTPListenerConfigBean 
    implements ServerEngine, Configurable {
    private static final long serialVersionUID = 1L;
    
    private static Map<Integer, JettyHTTPServerEngine> portMap =
        new HashMap<Integer, JettyHTTPServerEngine>();
   
    private int servantCount;
    private Server server;
    private SelectChannelConnector connector;
    private JettyConnectorFactory connectorFactory;
    private ContextHandlerCollection contexts;
    private final int port;
    
    JettyHTTPServerEngine(Bus bus, String protocol, int p) {
        port = p;
    }
    
    public String getBeanName() {
        return JettyHTTPServerEngine.class.getName() + "." + port;
    }

    static synchronized JettyHTTPServerEngine getForPort(Bus bus, String protocol, int p) {
        return getForPort(bus, protocol, p, null);
    }

    static synchronized JettyHTTPServerEngine getForPort(Bus bus,
                                                         String protocol,
                                                         int p,
                                                         SSLServerPolicy sslServerPolicy) {
        JettyHTTPServerEngine ref = portMap.get(p);
        if (ref == null) {
            ref = new JettyHTTPServerEngine(bus, protocol, p);
            configure(bus, ref);
            ref.init(sslServerPolicy);
            ref.retrieveListenerFactory();
            portMap.put(p, ref);
        }
        return ref;
    }
    
    public static synchronized void destroyForPort(int p) {
        JettyHTTPServerEngine ref = portMap.remove(p);
        if (ref != null && ref.server != null) {
            try {
                //NEED to check again
                ref.connector.close();
                ref.server.stop();
                ref.server.destroy();
                ref.server = null;
                ref.listener = null;
            } catch (IOException ex) {
                ex.printStackTrace();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            
        }
    }
    
    /**
     * Register a servant.
     * 
     * @param url the URL associated with the servant
     * @param handler notified on incoming HTTP requests
     */
    public synchronized void addServant(URL url, AbstractHandler handler) {
        if (server == null) {
            server = new Server();            
            connector = connectorFactory.createConnector(port);
            //TODO Get the detail configuration 
            /* set up the connector's paraments
            if (getListener().isSetMinThreads()) {
                connector.getThreadPool()..setMinThreads(getListener().getMinThreads());
            }
            if (getListener().isSetMaxThreads()) {
                connector.setMaxThreads(getListener().getMaxThreads());            
            }
            if (getListener().isSetMaxIdleTimeMs()) {
                connector.setMaxIdleTimeMs(getListener().getMaxIdleTimeMs().intValue());
            }
            if (getListener().isSetLowResourcePersistTimeMs()) {
                int lowResourcePersistTime = 
                    getListener().getLowResourcePersistTimeMs().intValue();
                connector.setLowResourcePersistTimeMs(lowResourcePersistTime);
            }*/
            server.addConnector(connector);
            contexts = new ContextHandlerCollection();
            server.addHandler(contexts);
            try {
                server.start();
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
        
        ContextHandler context = new ContextHandler();
        context.setContextPath(contextName);
        context.setHandler(handler);
        contexts.addHandler(context);
        if (contexts.isStarted()) {           
            try {                
                context.start();
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        }
        
        ++servantCount;
    }
    
    /**
     * Remove a previously registered servant.
     * 
     * @param url the URL the servant was registered against.
     */
    public synchronized void removeServant(URL url) {
        String contextName = HttpUriMapper.getContextName(url.getPath());
        
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
                        found = true;
                    }
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
    
    protected static void configure(Bus bus, Object bean) {
        Configurer configurer = bus.getExtension(Configurer.class);
        if (null != configurer) {
            configurer.configureBean(bean);
        }
    }

    private void retrieveListenerFactory() {
        connectorFactory = HTTPTransportFactory.getConnectorFactory(getSslServer());
    }
    
    private void init(SSLServerPolicy sslServerPolicy) {
        if (!isSetSslServer()) {
            setSslServer(sslServerPolicy);
        }
        if (!isSetListener()) {
            setListener(new HTTPListenerPolicy());
        }
    }
}
