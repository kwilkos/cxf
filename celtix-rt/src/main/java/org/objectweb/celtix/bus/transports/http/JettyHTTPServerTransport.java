package org.objectweb.celtix.bus.transports.http;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.Socket;
import java.net.SocketException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import javax.wsdl.Definition;
import javax.wsdl.WSDLException;
import javax.xml.ws.handler.MessageContext;

import org.mortbay.http.HttpRequest;
import org.mortbay.http.HttpResponse;
import org.mortbay.http.handler.AbstractHttpHandler;
import org.objectweb.celtix.Bus;
import org.objectweb.celtix.BusEvent;
import org.objectweb.celtix.BusEventListener;
import org.objectweb.celtix.BusException;
import org.objectweb.celtix.bindings.BindingContextUtils;
import org.objectweb.celtix.bus.busimpl.ComponentCreatedEvent;
import org.objectweb.celtix.bus.busimpl.ComponentRemovedEvent;
import org.objectweb.celtix.bus.configuration.ConfigurationEvent;
import org.objectweb.celtix.bus.configuration.ConfigurationEventFilter;
import org.objectweb.celtix.bus.management.counters.TransportServerCounters;
import org.objectweb.celtix.context.OutputStreamMessageContext;
import org.objectweb.celtix.transports.ServerTransportCallback;
import org.objectweb.celtix.ws.addressing.EndpointReferenceType;
import org.objectweb.celtix.wsdl.EndpointReferenceUtils;

public class JettyHTTPServerTransport extends AbstractHTTPServerTransport 
    implements BusEventListener {
    
    private static final long serialVersionUID = 1L;
    JettyHTTPServerEngine engine;
    TransportServerCounters counters;
    
    public JettyHTTPServerTransport(Bus b, EndpointReferenceType ref) throws WSDLException, IOException {
        super(b, ref);
        counters = new TransportServerCounters("JettyHTTPServerTransport");
        engine = JettyHTTPServerEngine.getForPort(bus, nurl.getProtocol(), nurl.getPort());
        //register the configuration event
        ConfigurationEventFilter configurationEventFilter = new ConfigurationEventFilter();
        try {
            bus.addListener((BusEventListener)this, configurationEventFilter);
        } catch (BusException ex) {            
            LOG.log(Level.SEVERE, "REMOVE_LISTENER_FAILURE_MSG", ex);            
        }
        
        bus.sendEvent(new ComponentCreatedEvent(this));
    }
    
    public void shutdown() {  
        try {
            bus.removeListener((BusEventListener)this);
        } catch (BusException ex) {            
            LOG.log(Level.SEVERE, "REMOVE_LISTENER_FAILURE_MSG", ex);            
        }
        bus.sendEvent(new ComponentRemovedEvent(this)); 
    }
    
    
    public synchronized void activate(ServerTransportCallback cb) throws IOException {
        callback = cb;
        engine.addServant(url, new AbstractHttpHandler() {
            public void handle(String pathInContext, String pathParams,
                               HttpRequest req, HttpResponse resp)
                throws IOException {
                if (pathInContext.equals(getName())) {
                    doService(req, resp);                    
                }
            }
        });
    }

    public void deactivate() throws IOException {
        engine.removeServant(url);
    }
    
    public OutputStreamMessageContext rebase(MessageContext context,
                                             EndpointReferenceType decoupledResponseEndpoint)
        throws IOException {
        OutputStreamMessageContext outputContext = null;
        HttpRequest request = 
            (HttpRequest)context.get(HTTPServerInputStreamContext.HTTP_REQUEST);
        HttpResponse response = 
            (HttpResponse)context.get(HTTPServerInputStreamContext.HTTP_RESPONSE);
        if (response != null) {
            outputContext = new HTTPServerRebasedOutputStreamContext(context, request, response);
            context.put(HTTPServerInputStreamContext.HTTP_RESPONSE, decoupledResponseEndpoint);
        }
        return outputContext;
    }
    
    public void postDispatch(MessageContext bindingContext, OutputStreamMessageContext context) {
        Object responseObj =
            bindingContext.get(HTTPServerInputStreamContext.HTTP_RESPONSE);
        
        if (context.isOneWay()) {
            counters.getRequestOneWay().increase();            
        }       
        counters.getRequestTotal().increase();

        if (responseObj instanceof HttpResponse) {
            HttpResponse response = (HttpResponse)responseObj;
            
            if (response.getStatus() == 500) {
                counters.getTotalError().increase();
            } 
            
            try {
                response.commit();
            } catch (IOException e) {
                e.printStackTrace();
            }
            
        } else if (responseObj instanceof URLConnection) {
            try {
                URLConnection connection = (URLConnection)responseObj;
                connection.getOutputStream().close();
                connection.getInputStream().close();
            } catch (IOException ioe) {
                LOG.log(Level.WARNING, "DECOUPLED_RESPONSE_FAILED_MSG", ioe);
            }
        }
        
    }
    
    protected void copyRequestHeaders(MessageContext ctx, Map<String, List<String>> headers) {
        HttpRequest req = (HttpRequest)ctx.get(HTTPServerInputStreamContext.HTTP_REQUEST);
        for (Enumeration e = req.getFieldNames(); e.hasMoreElements();) {
            String fname = (String)e.nextElement();
            List<String> values;
            if (headers.containsKey(fname)) {
                values = headers.get(fname);
            } else {
                values = new ArrayList<String>();
                headers.put(fname, values);
            }
            for (Enumeration e2 = req.getFieldValues(fname); e2.hasMoreElements();) {
                String val = (String)e2.nextElement();
                values.add(val);
            }
        }        
    }
    
    // REVISIT factor out to common shared with HTTPClientTransport
    protected URLConnection getConnection(URL url) throws IOException {
        URLConnection connection = url.openConnection();
        connection.setDoOutput(true);
        connection.setUseCaches(false);
        if (connection instanceof HttpURLConnection) {
            HttpURLConnection hc = (HttpURLConnection)connection;
            hc.setRequestMethod("POST");
        }
        connection.setRequestProperty("Content-Type", "text/xml");
        return connection;
    }

    protected void setPolicies(MessageContext ctx, Map<String, List<String>> headers) {
        super.setPolicies(ctx, headers);
        if (policy.isSetReceiveTimeout()) {
            HttpRequest req = (HttpRequest)ctx.get(HTTPServerInputStreamContext.HTTP_REQUEST);
            Object connection = req.getHttpConnection().getConnection();
            if (connection instanceof Socket) {
                Socket sock = (Socket)connection;
                try {
                    sock.setSoTimeout((int)policy.getReceiveTimeout());
                } catch (SocketException ex) {
                    LOG.log(Level.INFO, "Could not set SoTimeout", ex);
                }
            }                
        }
    }    

    protected void copyHeaders(MessageContext context, HttpResponse response) {
        Map<?, ?> headers = (Map<?, ?>)context.get(MessageContext.HTTP_RESPONSE_HEADERS);
        if (null != headers) {
            for (Iterator<?> iter = headers.keySet().iterator(); iter.hasNext();) {
                String header = (String)iter.next();
                List<?> headerList = (List<?>)headers.get(header);
                for (Object string : headerList) {
                    response.addField(header, (String)string);
                }
            }
        }
        
    }
    
    /**
     * @param context The associated MessageContext.
     * @return the context that will be used to obtain the OutputStream
     */
    public OutputStreamMessageContext createOutputStreamContext(MessageContext context)
        throws IOException {
        return new HTTPServerOutputStreamContext(context);
    }

    void doService(HttpRequest req, HttpResponse resp) throws IOException {
        
        if (policy.isSetRedirectURL()) {
            resp.sendRedirect(policy.getRedirectURL());
            resp.commit();
            req.setHandled(true);
            return;
        }    

        
        
        if ("GET".equals(req.getMethod()) && req.getURI().toString().toLowerCase().endsWith("?wsdl")) {
            try {
                
                Definition def = EndpointReferenceUtils.getWSDLDefinition(bus.getWSDLManager(), reference);
                resp.addField("Content-Type", "text/xml");
                bus.getWSDLManager().getWSDLFactory().newWSDLWriter().writeWSDL(def, resp.getOutputStream());
                resp.getOutputStream().flush();
                resp.commit();
                req.setHandled(true);
                return;
            } catch (WSDLException ex) {
                // TODO Auto-generated catch block
                ex.printStackTrace();
            }
        }
                
        final class Servicer implements Runnable {
            private boolean complete;
            private final HttpRequest request;
            private final HttpResponse response;
            
            Servicer(HttpRequest reqs, HttpResponse resps) {
                request = reqs;
                response = resps;
            }
            public void run() {
                try {
                    serviceRequest(request, response);                        
                } catch (IOException ex) {                        
                    // TODO handle exception
                    LOG.log(Level.SEVERE, "DISPATCH_FAILURE_MSG", ex);
                } finally {
                    complete = true;
                    synchronized (this) {
                        notifyAll();
                    }
                }
            } 
            
            public synchronized void waitForCompletion() {
                while (!complete) {
                    try {
                        wait();
                    } catch (InterruptedException ex) {
                        // ignore
                    }
                }
            }
        }
        
        if (null == callback.getExecutor()) {
            serviceRequest(req, resp);
        } else {  
            Servicer servicer = new Servicer(req, resp);
            callback.getExecutor().execute(servicer);
            servicer.waitForCompletion();
        }        
    }
    
    void serviceRequest(final HttpRequest req, final HttpResponse resp) throws IOException {
        try {
            if (LOG.isLoggable(Level.INFO)) {
                LOG.info("Service http request on thread: " + Thread.currentThread());
            }
            
            HTTPServerInputStreamContext ctx = new HTTPServerInputStreamContext(this) {
                public void initContext() throws IOException {
                    super.initContext();
                    inStream = req.getInputStream();
                    origInputStream = inStream;
                }
            };
            BindingContextUtils.storeAsyncOnewayDispatch(ctx, true);
            ctx.put(HTTPServerInputStreamContext.HTTP_REQUEST, req);
            ctx.put(HTTPServerInputStreamContext.HTTP_RESPONSE, resp);
            ctx.initContext();
            
            callback.dispatch(ctx, this);
            resp.commit();
            req.setHandled(true);
        } finally {
            if (LOG.isLoggable(Level.INFO)) {
                LOG.info("Finished servicing http request on thread: " + Thread.currentThread());
            }
        }
    }

    private class HTTPServerOutputStreamContext 
        extends AbstractHTTPServerOutputStreamContext {
        
        HTTPServerOutputStreamContext(MessageContext ctx) throws IOException {
            super(JettyHTTPServerTransport.this, ctx);
        }
        protected void flushHeaders() throws IOException {
            Object responseObj =
                get(HTTPServerInputStreamContext.HTTP_RESPONSE);
            OutputStream responseStream = null;
            if (responseObj instanceof HttpResponse) {
                // non-decoupled response
                HttpResponse response = (HttpResponse)responseObj;
            
                Integer i = (Integer)context.get(HTTP_RESPONSE_CODE);
                if (i != null) {
                    if (i.intValue() == 500) {
                        response.setStatus(i.intValue(), "Fault Occurred");
                    } else {
                        response.setStatus(i.intValue());
                    }
                } else {
                    response.setStatus(200);
                }
            
                copyHeaders(context, response);
                responseStream = response.getOutputStream();
                
                if (isOneWay()) {
                    response.commit();
                }
            } else if (responseObj instanceof EndpointReferenceType) {
                // decoupled response
                EndpointReferenceType decoupledResponseEndpoint = 
                    (EndpointReferenceType)responseObj;
                
                if (!isOneWay()) {
                    // REVISIT: use policy logic from HTTPClientTransport
                    // REVISIT: handle connection closure
                    URL url = new URL(decoupledResponseEndpoint.getAddress().getValue());
                    URLConnection connection = getConnection(url);
                    responseStream = connection.getOutputStream();
                    put(HTTPServerInputStreamContext.HTTP_RESPONSE, connection);
                }
            } else if (responseObj instanceof URLConnection) {
                // resent decoupled response
                URL url = ((URLConnection)responseObj).getURL();
                URLConnection connection = getConnection(url);
                responseStream = connection.getOutputStream();
                put(HTTPServerInputStreamContext.HTTP_RESPONSE, connection);
            } else {
                LOG.log(Level.WARNING, "UNEXPECTED_RESPONSE_TYPE_MSG", responseObj.getClass());
                throw new IOException("UNEXPECTED_RESPONSE_TYPE_MSG" + responseObj.getClass());
            }

            if (isOneWay()) {
                context.remove(HTTPServerInputStreamContext.HTTP_RESPONSE);
            } else {
                origOut.resetOut(new BufferedOutputStream(responseStream, 1024));
            }
        }
    }
    
    private class HTTPServerRebasedOutputStreamContext 
        extends AbstractHTTPServerOutputStreamContext {
    
        private HttpRequest request;
        private HttpResponse response;
        
        HTTPServerRebasedOutputStreamContext(MessageContext ctx,
                                             HttpRequest req,
                                             HttpResponse resp) throws IOException {
            super(JettyHTTPServerTransport.this, ctx);
            request = req;
            response = resp;
        }
        
        protected void flushHeaders() throws IOException {
            if (response != null) {
                copyHeaders(context, response);
                response.setStatus(HttpURLConnection.HTTP_ACCEPTED, "Accepted");
                response.commit();
                request.setHandled(true);
                origOut.resetOut(new BufferedOutputStream(response.getOutputStream(), 1024));
            }
        }
    }
    public void processEvent(BusEvent e) throws BusException {
        if (e.getID().equals(ConfigurationEvent.RECONFIGURED)) {
            String configName = (String)e.getSource();            
            reConfigure(configName);
        }
    }

    private void reConfigure(String configName) {
        if ("servicesMonitoring".equals(configName)) {
            if (bus.getConfiguration().getBoolean("servicesMonitoring")) {
                counters.resetCounters();
            } else {
                counters.stopCounters();
            }
        }
    }
}
