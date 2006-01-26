package org.objectweb.celtix.bus.transports.http;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
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
import org.objectweb.celtix.Bus;
import org.objectweb.celtix.context.OutputStreamMessageContext;
import org.objectweb.celtix.transports.ServerTransportCallback;
import org.objectweb.celtix.ws.addressing.EndpointReferenceType;
import org.objectweb.celtix.wsdl.EndpointReferenceUtils;

public class JettyHTTPServerTransport extends AbstractHTTPServerTransport {
    
    private static final long serialVersionUID = 1L;
    JettyHTTPServerEngine engine;
    
    public JettyHTTPServerTransport(Bus b, EndpointReferenceType ref) throws WSDLException, IOException {
        super(b, ref);
        engine = JettyHTTPServerEngine.getForPort(bus, nurl.getProtocol(), nurl.getPort());
    }
    
    public synchronized void activate(ServerTransportCallback cb) throws IOException {
        callback = cb;
        engine.addServant(url, this);
    }

    public void deactivate() throws IOException {
        engine.removeServant(url, this);
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
    public void postDispatch(MessageContext bindingContext, OutputStreamMessageContext context) {
        HttpResponse response = (HttpResponse)context.get(HTTPServerInputStreamContext.HTTP_RESPONSE);
        if (response != null) {
            try {
                response.commit();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    
    /**
     * @param context The associated MessageContext.
     * @return the context that will be used to obtain the OutputStream
     */
    public OutputStreamMessageContext createOutputStreamContext(MessageContext context)
        throws IOException {
        return new AbstractHTTPServerOutputStreamContext(this, context) {
            protected void flushHeaders() throws IOException {
                HttpResponse response = (HttpResponse)get(HTTPServerInputStreamContext.HTTP_RESPONSE);
                
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
                
                Map<?, ?> headers = (Map<?, ?>)super.get(HTTP_RESPONSE_HEADERS);
                if (null != headers) {
                    for (Iterator<?> iter = headers.keySet().iterator(); iter.hasNext();) {
                        String header = (String)iter.next();
                        List<?> headerList = (List<?>)headers.get(header);
                        for (Object string : headerList) {
                            response.addField(header, (String)string);
                        }
                    }
                }
                if (!isOneWay()) {
                    origOut.resetOut(new BufferedOutputStream(response.getOutputStream(), 1024));
                } else {
                    response.commit();
                    context.remove(HTTPServerInputStreamContext.HTTP_RESPONSE);
                }
            }
        };
    }

    void doService(HttpRequest req, HttpResponse resp) throws IOException {
        if (policy.isSetRedirectURL()) {
            resp.sendRedirect(policy.getRedirectURL());
            resp.commit();
            req.setHandled(true);
            return;
        }
        
        if ("GET".equals(req.getMethod())) {
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


}
