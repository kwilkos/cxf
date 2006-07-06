package org.objectweb.celtix.transports.http;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.Map;

import javax.wsdl.WSDLException;

import org.objectweb.celtix.Bus;
import org.objectweb.celtix.bindings.ClientBinding;
import org.objectweb.celtix.buslifecycle.BusLifeCycleListener;
import org.objectweb.celtix.transports.ClientTransport;
import org.objectweb.celtix.transports.ServerTransport;
import org.objectweb.celtix.transports.TransportFactory;
import org.objectweb.celtix.transports.http.protocol.pipe.Handler;
import org.objectweb.celtix.transports.http.protocol.pipe.PipeHTTPServerTransport;
import org.objectweb.celtix.ws.addressing.EndpointReferenceType;
import org.objectweb.celtix.wsdl.EndpointReferenceUtils;

public class HTTPTransportFactory implements TransportFactory {
    protected Bus bus;
    /* surefire runs in a classloader that won't allow the URL 
     * class to load the Handler.   We'll need to manually add 
     * it to the list.
     */
    static {
        addHandler();
    }
    @SuppressWarnings("unchecked")
    static void addHandler() { 
        try {
            Field field = URL.class.getDeclaredField("handlers");
            field.setAccessible(true);
            Map handlers = (Map)field.get(null);
            handlers.put("pipe", new Handler());
        } catch (Exception e) {
            //ignore
        }
    }
    
    public void init(Bus b) {
        bus = b;

        String val = System.getProperty("java.protocol.handler.pkgs");
        if (val == null) {
            System.setProperty("java.protocol.handler.pkgs",
                               "org.objectweb.celtix.bus.transports.http.protocol");
        } else if (val.indexOf("org.objectweb.celtix.bus.transports.http.protocol") == -1) {
            val += "|org.objectweb.celtix.bus.transports.http.protocol";
            System.setProperty("java.protocol.handler.pkgs", val);
        }
        bus.getLifeCycleManager().registerLifeCycleListener(new ShutdownListener(this));
    }
        
    public ServerTransport createServerTransport(EndpointReferenceType address) 
        throws WSDLException, IOException {
        URL url = new URL(EndpointReferenceUtils.getAddress(address));
        if ("pipe".equals(url.getProtocol())) {
            return new PipeHTTPServerTransport(bus, address);
        }
        return new JettyHTTPServerTransport(bus, address);
    }

    public ServerTransport createTransientServerTransport(EndpointReferenceType address)
        throws WSDLException {
        
        // TODO Auto-generated method stub
        return null;
    }

    public ClientTransport createClientTransport(EndpointReferenceType ref,
                                                 ClientBinding binding)
        throws WSDLException, IOException {
        return new HTTPClientTransport(bus, ref, binding, this);
    }

    protected Bus getBus() {
        return bus;
    }
    
    
    private void shutdown() {
    }
        
    private static class ShutdownListener 
        extends WeakReference<HTTPTransportFactory> 
        implements BusLifeCycleListener {
    
        ShutdownListener(HTTPTransportFactory factory) {
            super(factory);
        }

        public void initComplete() {
            //nothing
        }

        public void preShutdown() {
            if (get() != null) {
                HTTPTransportFactory factory = get();
                factory.shutdown();
                clear();
            }
        }

        public void postShutdown() {
            //nothing
        }
    }
}
