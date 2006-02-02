package org.objectweb.celtix.bus.transports.http;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.mortbay.http.HttpContext;
import org.mortbay.http.HttpHandler;
import org.mortbay.http.HttpServer;
import org.mortbay.http.SocketListener;
import org.mortbay.http.SslListener;
import org.mortbay.http.handler.AbstractHttpHandler;
import org.mortbay.util.InetAddrPort;
import org.objectweb.celtix.Bus;
import org.objectweb.celtix.bus.configuration.security.SSLPolicy;
import org.objectweb.celtix.configuration.Configuration;
import org.objectweb.celtix.transports.http.configuration.HTTPListenerPolicy;


public final class JettyHTTPServerEngine {
    private static final long serialVersionUID = 1L;
    
    private static Map<Integer, JettyHTTPServerEngine> portMap =
        new HashMap<Integer, JettyHTTPServerEngine>();
   
    int servantCount;
    HttpServer server;
    SocketListener listener;
    Configuration config;
    HTTPListenerPolicy policy;
    SSLPolicy sslPolicy;
    int port;
    
    private JettyHTTPServerEngine(Bus bus, String protocol, int p) {
        port = p;
        config = createConfiguration(bus, port);
        policy = config.getObject(HTTPListenerPolicy.class, "httpListener");
        sslPolicy = config.getObject(SSLPolicy.class, "ssl");
        if (sslPolicy == null && "https".equals(protocol)) {
            sslPolicy = new SSLPolicy();
            sslPolicy.setUseSecureSockets(true);
        }
    }
    
    private Configuration createConfiguration(Bus bus, int p) {
        return new HTTPListenerConfiguration(bus, p);
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
    
    synchronized void addServant(String url, AbstractHttpHandler handler) {
        
        URL nurl = null;
        try {
            nurl = new URL(url);
        } catch (MalformedURLException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        String lpath = nurl.getPath();

        if (server == null) {
            server = new HttpServer();
            if (sslPolicy != null && sslPolicy.isUseSecureSockets()) {
                listener = new SslListener(new InetAddrPort(port));
                // TODO - set the SSL stuff
            } else {
                listener = new SocketListener(new InetAddrPort(port));
            }
            
            if (policy.isSetMinThreads()) {
                listener.setMinThreads(policy.getMinThreads());
            }
            if (policy.isSetMaxThreads()) {
                listener.setMaxThreads(policy.getMaxThreads());            
            }
            if (policy.isSetMaxIdleTimeMs()) {
                listener.setMaxIdleTimeMs(policy.getMaxIdleTimeMs().intValue());
            }
            if (policy.isSetLowResourcePersistTimeMs()) {
                listener.setLowResourcePersistTimeMs(policy.getLowResourcePersistTimeMs().intValue());
            }

            server.addListener(listener);
            try {
                server.start();
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
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

        handler.setName(smap);
        context.addHandler(handler);
        try {
            handler.start();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        ++servantCount;
    }
    
    synchronized void removeServant(String url) throws IOException {
        URL nurl = new URL(url);
        String lpath = nurl.getPath();
        
        String contextName = "";
        String servletMap = lpath;
        int idx = lpath.lastIndexOf('/');
        if (idx > 0) {
            contextName = lpath.substring(0, idx);
            servletMap = lpath.substring(idx);
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
    
    
}
