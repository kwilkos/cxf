package org.objectweb.celtix.bus.transports.http;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.util.HashMap;

import javax.servlet.ServletContext;

import org.apache.catalina.Context;
import org.apache.catalina.Engine;
import org.apache.catalina.Host;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.startup.Embedded;




final class HTTPServerEngine {
    private static final long serialVersionUID = 1L;
    
    private static HashMap<Integer, WeakReference<HTTPServerEngine>> portMap =
        new HashMap<Integer, WeakReference<HTTPServerEngine>>();
    private static Embedded embeddedTomcat;

    Connector connector;
    int servantCount = 0;
    ServletContext context;
    
    Context ctx;
    Host host;
    String path;
    
    private HTTPServerEngine(String protocol, int port) {
        Engine engine = embeddedTomcat.createEngine();
        engine.setName("Celtix-" + port);
        path = "";
        
        try {
            File file = File.createTempFile("celtix", "http");
            file.delete();
            file.mkdirs();
            path = file.getAbsolutePath();
            file.deleteOnExit();
        } catch (IOException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        
        engine.setDefaultHost("localhost");
        host = embeddedTomcat.createHost("localhost", path);
        ctx = embeddedTomcat.createContext("", path);

        engine.addChild(host);
        host.addChild(ctx);
        
        embeddedTomcat.addEngine(engine);
        connector = embeddedTomcat.createConnector((String)null, port, protocol);
        embeddedTomcat.addConnector(connector);
    }
    public boolean getConfigured() {
        return true;
    }
    
    static synchronized HTTPServerEngine getForPort(String protocol, int port) {
        if (null == embeddedTomcat) {
            embeddedTomcat = new Embedded();
            try {
                embeddedTomcat.start();
            } catch (LifecycleException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        
        WeakReference<HTTPServerEngine> ref = portMap.get(port);
        if (ref == null || ref.get() == null) {
            ref = new WeakReference<HTTPServerEngine>(new HTTPServerEngine(protocol, port));
            portMap.put(port, ref);
        }
        return ref.get();
    }
    
    synchronized void addServant(String url, HTTPServerTransport servant) throws IOException {
        URL nurl = new URL(url);
        String lpath = nurl.getPath();
        
        if (servantCount == 0) {
            try {
                connector.start();
            } catch (LifecycleException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                e.getThrowable().printStackTrace();
            }
        }

        String contextName = "";
        String servletMap = lpath;
        int idx;
        if ((idx = lpath.lastIndexOf('/')) > 0) {
            contextName = lpath.substring(0, idx);
            servletMap = lpath.substring(idx);
        }
        
        ctx = (Context)host.findChild(contextName);
        if (ctx == null) {
            ctx = embeddedTomcat.createContext(contextName, path);
        } else {
            host.removeChild(ctx);
        }
        
        ctx.addChild(servant);
        ctx.addServletMapping(servletMap, servant.getName());
        
        host.addChild(ctx);

        ++servantCount;
    }
    
    synchronized void removeServant(String url, HTTPServerTransport servant) throws IOException {
        URL nurl = new URL(url);
        String lpath = nurl.getPath();
        
        String servletMap = lpath;
        int idx;
        if ((idx = lpath.lastIndexOf('/')) > 0) {
            servletMap = lpath.substring(idx);
        }
        
        ctx.removeServletMapping(servletMap);
        ctx.removeChild(servant);
        host.removeChild(ctx);
        if (ctx.findChildren().length > 0) {
            host.addChild(ctx);
        }
        
        --servantCount;
        if (servantCount == 0) {
            try {
                connector.stop();
            } catch (LifecycleException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }
    
    
}
