package org.apache.cxf.transports.http;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.mortbay.http.HttpContext;
import org.mortbay.http.HttpHandler;
import org.mortbay.http.HttpServer;
import org.mortbay.http.SocketListener;
import org.mortbay.http.handler.AbstractHttpHandler;
import org.mortbay.util.InetAddrPort;
import org.apache.cxf.Bus;


public final class JettyHTTPServerEngine implements ServerEngine {
    private static final long serialVersionUID = 1L;
    
    private static Map<Integer, JettyHTTPServerEngine> portMap =
        new HashMap<Integer, JettyHTTPServerEngine>();
   
    private int servantCount;
    private HttpServer server;
    private SocketListener listener;
    private final HTTPListenerConfiguration config;
    private final int port;
    
    JettyHTTPServerEngine(Bus bus, String protocol, int p) {
        port = p;
        config = new HTTPListenerConfiguration(bus, protocol, port);
    }
    
    static synchronized JettyHTTPServerEngine getForPort(Bus bus, String protocol, int p) {
        JettyHTTPServerEngine ref = portMap.get(p);
        if (ref == null) {
            ref = new JettyHTTPServerEngine(bus, protocol, p);
            portMap.put(p, ref);
        }
        return ref;
    }
    
    public static synchronized void destroyForPort(int p) {
        JettyHTTPServerEngine ref = portMap.remove(p);
        if (ref != null && ref.server != null) {
            try {
                ref.listener.getServerSocket().close();
                ref.server.stop(true);
                ref.server.destroy();
                ref.server = null;
                ref.listener = null;
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            } catch (IOException ex) {
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
    public synchronized void addServant(URL url, AbstractHttpHandler handler) {

        String lpath = url.getPath();

        if (server == null) {
            server = new HttpServer();
            
            // REVISIT creare SSL listener if neccessary
            listener = new SocketListener(new InetAddrPort(port));
            
            if (config.getPolicy().isSetMinThreads()) {
                listener.setMinThreads(config.getPolicy().getMinThreads());
            }
            if (config.getPolicy().isSetMaxThreads()) {
                listener.setMaxThreads(config.getPolicy().getMaxThreads());            
            }
            if (config.getPolicy().isSetMaxIdleTimeMs()) {
                listener.setMaxIdleTimeMs(config.getPolicy().getMaxIdleTimeMs().intValue());
            }
            if (config.getPolicy().isSetLowResourcePersistTimeMs()) {
                int lowResourcePersistTime = 
                    config.getPolicy().getLowResourcePersistTimeMs().intValue();
                listener.setLowResourcePersistTimeMs(lowResourcePersistTime);
            }

            server.addListener(listener);
            try {
                server.start();
            } catch (Exception e) {
                e.printStackTrace();
                //problem starting server
                try {
                    server.stop(true);
                    server.destroy();
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }    
            }
        }

        String contextName = "";
        String servletMap = lpath;
        int idx = lpath.lastIndexOf('/');
        if (idx > 0) {
            contextName = lpath.substring(0, idx);
            servletMap = lpath.substring(idx);
        }
        final String smap = servletMap;
        
        
        HttpContext context = server.getContext(contextName);
        try {
            context.start();
        } catch (Exception e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }

        if ("".equals(smap) && "".equals(contextName)) {
            handler.setName("/");
        } else {
            handler.setName(smap);
        }
        context.addHandler(handler);
        try {
            handler.start();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        ++servantCount;
    }
    
    /**
     * Remove a previously registered servant.
     * 
     * @param url the URL the servant was registered against.
     */
    public synchronized void removeServant(URL url) {
        String lpath = url.getPath();
        
        String contextName = "";
        String servletMap = lpath;
        int idx = lpath.lastIndexOf('/');
        if (idx > 0) {
            contextName = lpath.substring(0, idx);
            servletMap = lpath.substring(idx);
        }
        if ("".equals(servletMap) && "".equals(contextName)) {
            servletMap = "/";
        }

        boolean found = false;
        // REVISIT: how come server can be null?
        if (server != null) {
            HttpContext context = server.getContext(contextName);
            for (HttpHandler handler : context.getHandlers()) {
                if (servletMap.equals(handler.getName())) {
                    try {
                        handler.stop();
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    context.removeHandler(handler);
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
    public synchronized HttpHandler getServant(URL url)  {
        String lpath = url.getPath();
        
        String contextName = "";
        String servletMap = lpath;
        int idx = lpath.lastIndexOf('/');
        if (idx > 0) {
            contextName = lpath.substring(0, idx);
            servletMap = lpath.substring(idx);
        }
        
        HttpHandler ret = null;
        // REVISIT: how come server can be null?
        if (server != null) {
            HttpContext context = server.getContext(contextName);
            for (HttpHandler handler : context.getHandlers()) {
                if (servletMap.equals(handler.getName())) {
                    ret = handler;
                    break;
                }
            }
        }
        return ret;
    }
}
