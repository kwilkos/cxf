package org.objectweb.celtix.bus.transports.http;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.wsdl.WSDLException;
import javax.xml.bind.JAXBException;

import org.mortbay.http.HttpRequest;
import org.mortbay.http.HttpResponse;
import org.mortbay.http.handler.AbstractHttpHandler;
import org.objectweb.celtix.Bus;
import org.objectweb.celtix.bindings.ResponseCallback;
import org.objectweb.celtix.bus.transports.http.HTTPClientTransport.HTTPDecoupledClientInputStreamContext;
import org.objectweb.celtix.bus.transports.http.protocol.pipe.Handler;
import org.objectweb.celtix.bus.transports.http.protocol.pipe.PipeHTTPServerTransport;
import org.objectweb.celtix.buslifecycle.BusLifeCycleListener;
import org.objectweb.celtix.common.logging.LogUtils;
import org.objectweb.celtix.transports.ClientTransport;
import org.objectweb.celtix.transports.ServerTransport;
import org.objectweb.celtix.transports.TransportFactory;
import org.objectweb.celtix.transports.http.configuration.HTTPClientPolicy;
import org.objectweb.celtix.transports.http.configuration.HTTPServerPolicy;
import org.objectweb.celtix.ws.addressing.EndpointReferenceType;
import org.objectweb.celtix.wsdl.EndpointReferenceUtils;
import org.objectweb.celtix.wsdl.JAXBExtensionHelper;
import org.xmlsoap.schemas.wsdl.http.AddressType;

public class HTTPTransportFactory implements TransportFactory {
    private static final Logger LOG = LogUtils.getL7dLogger(HTTPTransportFactory.class);
    
    Bus bus;
    
    private String decoupledAddress;
    private URL decoupledURL;
    private JettyHTTPServerEngine decoupledEngine;
    private EndpointReferenceType decoupledEndpoint;
    private ResponseCallback responseCallback;
      
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
        try {
            JAXBExtensionHelper.addExtensions(bus.getWSDLManager().getExtenstionRegistry(),
                                              javax.wsdl.Port.class,
                                              HTTPClientPolicy.class);
            JAXBExtensionHelper.addExtensions(bus.getWSDLManager().getExtenstionRegistry(),
                                              javax.wsdl.Port.class,
                                              HTTPServerPolicy.class);
            JAXBExtensionHelper.addExtensions(bus.getWSDLManager().getExtenstionRegistry(),
                                              javax.wsdl.Port.class,
                                              AddressType.class);

        } catch (JAXBException e) {
            //ignore, we can continue without the extension registered
        }
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

    /**
     * @param callback used to report (potentially asynchronous) responses.
     */
    public synchronized void setResponseCallback(ResponseCallback callback) {
        responseCallback = callback;
    }
    
    /**
     * @return callback used to report (potentially asynchronous) responses.
     */
    public synchronized ResponseCallback getResponseCallback() {
        return responseCallback;
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

    public ClientTransport createClientTransport(EndpointReferenceType ref)
        throws WSDLException, IOException {
        return new HTTPClientTransport(bus, ref, this);
    }

    protected synchronized EndpointReferenceType getDecoupledEndpoint(String url) {
        if (decoupledEndpoint == null && url != null) {
            decoupledEndpoint = setUpDecoupledEndpoint(url);           
        }
        return decoupledEndpoint;
    }

    protected synchronized boolean hasDecoupledEndpoint() {
        return decoupledEndpoint != null;
    }

    protected Bus getBus() {
        return bus;
    }
    
    
    private void shutdown() {
        if (decoupledURL != null && decoupledEngine != null) {
            try {
                decoupledEngine.removeServant(decoupledAddress);
                JettyHTTPServerEngine.destroyForPort(decoupledURL.getPort());
            } catch (IOException ex) {
                //ignore
            }
        }
    }
    
    private EndpointReferenceType setUpDecoupledEndpoint(String url) {
        // REVISIT: use policy to determine decoupled endpoint
        EndpointReferenceType reference =
            EndpointReferenceUtils.getEndpointReference(url);
        if (reference != null) {
            decoupledAddress = reference.getAddress().getValue();
            LOG.info("creating decoupled endpoint: " + decoupledAddress);
            try {
                decoupledURL = new URL(decoupledAddress);
                decoupledEngine = 
                    JettyHTTPServerEngine.getForPort(bus, 
                                                     decoupledURL.getProtocol(),
                                                     decoupledURL.getPort());
                decoupledEngine.addServant(decoupledAddress, new AbstractHttpHandler() {
                    public void handle(String pathInContext, 
                                       String pathParams,
                                       HttpRequest req, 
                                       HttpResponse resp) throws IOException {
                        handleDecoupledResponse(req, resp);
                    }
                });

            } catch (Exception e) {
                // REVISIT move message to localizable Messages.properties
                LOG.log(Level.WARNING, "decoupled endpoint creation failed: ", e);
            }
        }
        return reference;
    }
    
    private void handleDecoupledResponse(HttpRequest req, HttpResponse resp) 
        throws IOException {
        HTTPDecoupledClientInputStreamContext ctx = 
            new HTTPDecoupledClientInputStreamContext(req);
    
        responseCallback.dispatch(ctx);
        resp.commit();
        req.setHandled(true);
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
