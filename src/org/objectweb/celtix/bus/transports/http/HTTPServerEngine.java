package org.objectweb.celtix.bus.transports.http;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.xml.ws.WebServiceException;


import org.mortbay.http.HttpContext;
import org.mortbay.http.HttpHandler;
import org.mortbay.http.HttpRequest;
import org.mortbay.http.HttpResponse;
import org.mortbay.http.HttpServer;
import org.mortbay.http.SocketListener;
import org.mortbay.http.SslListener;
import org.mortbay.http.handler.AbstractHttpHandler;
import org.mortbay.util.InetAddrPort;


final class HTTPServerEngine {
    private static final long serialVersionUID = 1L;
    
    private static Map<Integer, WeakReference<HTTPServerEngine>> portMap =
        new HashMap<Integer, WeakReference<HTTPServerEngine>>();
   
    int servantCount;
    HttpServer server = new HttpServer();
    SocketListener listener;
    
    private HTTPServerEngine(String protocol, int port) {
        if ("http".equals(protocol)) {
            listener = new SocketListener(new InetAddrPort(port));
        } else if ("https".equals(protocol)) {
            listener = new SslListener(new InetAddrPort(port));
        } else {
            throw new WebServiceException("Unknown protocol: " + protocol);
        }
    }
    
    static synchronized HTTPServerEngine getForPort(String protocol, int port) {
        
        WeakReference<HTTPServerEngine> ref = portMap.get(port);
        if (ref == null || ref.get() == null) {
            ref = new WeakReference<HTTPServerEngine>(new HTTPServerEngine(protocol, port));
            portMap.put(port, ref);
        }
        return ref.get();
    }
    
    synchronized void addServant(String url, final HTTPServerTransport servant) {
        
        URL nurl = null;
        try {
            nurl = new URL(url);
        } catch (MalformedURLException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        String lpath = nurl.getPath();
        
        if (servantCount == 0) {
            server.addListener(listener);
            try {
                listener.start();
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

        HttpHandler handler = new AbstractHttpHandler() {
            {
                setName(smap);
            }
            public void handle(String pathInContext, String pathParams,
                               HttpRequest req, HttpResponse resp)
                throws IOException {
                // TODO Auto-generated method stub
                if (pathInContext.equals(getName())) {
                    servant.doService(req, resp);                    
                }
            }
            
        };
        context.addHandler(handler);
        try {
            handler.start();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        ++servantCount;
    }
    
    synchronized void removeServant(String url, HTTPServerTransport servant) throws IOException {
        URL nurl = new URL(url);
        String lpath = nurl.getPath();
        
        String contextName = "";
        String servletMap = lpath;
        int idx = lpath.lastIndexOf('/');
        if (idx > 0) {
            contextName = lpath.substring(0, idx);
            servletMap = lpath.substring(idx);
        }
        
        HttpContext context = server.getContext(contextName);
        boolean found = false;
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
        if (!found) {
            System.err.println("Not able to remove the handler");
        }
        
        --servantCount;
        if (servantCount == 0) {
            server.removeListener(listener);
        }
    }
    
    
}
