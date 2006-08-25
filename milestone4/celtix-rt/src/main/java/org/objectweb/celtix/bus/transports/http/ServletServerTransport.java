package org.objectweb.celtix.bus.transports.http;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.wsdl.Definition;
import javax.wsdl.Port;
import javax.wsdl.WSDLException;
import javax.wsdl.extensions.ExtensibilityElement;
import javax.wsdl.extensions.soap.SOAPAddress;
import javax.xml.ws.handler.MessageContext;

import org.objectweb.celtix.Bus;
import org.objectweb.celtix.context.OutputStreamMessageContext;
import org.objectweb.celtix.transports.ServerTransportCallback;
import org.objectweb.celtix.ws.addressing.EndpointReferenceType;
import org.objectweb.celtix.wsdl.EndpointReferenceUtils;

class ServletServerTransport extends AbstractHTTPServerTransport {

    CeltixServlet servlet;
    
    public ServletServerTransport(Bus b, EndpointReferenceType ref, CeltixServlet s)
        throws WSDLException, IOException {
        super(b, ref);
        servlet = s;
    }

    public void activate(ServerTransportCallback cb) throws IOException {
        callback = cb;
        servlet.addServant(nurl, this);
    }

    public void deactivate() throws IOException {
        servlet.removeServant(nurl, this);
    }

    
    protected void copyRequestHeaders(MessageContext ctx, Map<String, List<String>> headers) {
        HttpServletRequest req = (HttpServletRequest)ctx.get(HTTPServerInputStreamContext.HTTP_REQUEST);
        for (Enumeration e = req.getHeaderNames(); e.hasMoreElements();) {
            String fname = (String)e.nextElement();
            List<String> values;
            if (headers.containsKey(fname)) {
                values = headers.get(fname);
            } else {
                values = new ArrayList<String>();
                headers.put(fname, values);
            }
            for (Enumeration e2 = req.getHeaders(fname); e2.hasMoreElements();) {
                String val = (String)e2.nextElement();
                values.add(val);
            }
        }        
    }
    protected void setPolicies(MessageContext ctx, Map<String, List<String>> headers) {
        super.setPolicies(ctx, headers);
        if (policy.isSetReceiveTimeout()) {
            /*
             * @@TODO - can we set a timout?
            Object connection = req.getHttpConnection().getConnection();
            if (connection instanceof Socket) {
                Socket sock = (Socket)connection;
                try {
                    sock.setSoTimeout((int)policy.getReceiveTimeout());
                } catch (SocketException ex) {
                    LOG.log(Level.INFO, "Could not set SoTimeout", ex);
                }
            }                
             */
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
                HttpServletResponse response =
                    (HttpServletResponse)get(HTTPServerInputStreamContext.HTTP_RESPONSE);
                
                Integer i = (Integer)context.get(HTTP_RESPONSE_CODE);
                if (i != null) {
                    response.setStatus(i.intValue());
                }
                
                Map<?, ?> headers = (Map<?, ?>)super.get(HTTP_RESPONSE_HEADERS);
                if (null != headers) {
                    for (Iterator<?> iter = headers.keySet().iterator(); iter.hasNext();) {
                        String header = (String)iter.next();
                        List<?> headerList = (List<?>)headers.get(header);
                        for (Object string : headerList) {
                            response.addHeader(header, (String)string);
                        }
                    }
                }
                origOut.resetOut(new BufferedOutputStream(response.getOutputStream(), 1024));
            }
        };
    }    
    
    void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        if (policy.isSetRedirectURL()) {
            resp.sendRedirect(policy.getRedirectURL());
            return;
        }
        
        if ("GET".equals(req.getMethod())) {
            try {
                Definition def = EndpointReferenceUtils.getWSDLDefinition(bus.getWSDLManager(), reference);
                
                resp.addHeader("Content-Type", "text/xml");
                Port port = EndpointReferenceUtils.getPort(bus.getWSDLManager(), reference);
                List<?> exts = port.getExtensibilityElements();
                if (exts.size() > 0) {
                    ExtensibilityElement el = (ExtensibilityElement)exts.get(0);
                    if (el instanceof SOAPAddress) {
                        SOAPAddress add = (SOAPAddress)el;
                        add.setLocationURI(req.getRequestURL().toString());
                    }
                }
                
                
                bus.getWSDLManager().getWSDLFactory().newWSDLWriter().writeWSDL(def, resp.getOutputStream());
                resp.getOutputStream().flush();
                return;
            } catch (WSDLException ex) {
                // TODO Auto-generated catch block
                ex.printStackTrace();
            }
        }    
    }
    void doPost(final HttpServletRequest req, final HttpServletResponse resp) throws IOException {
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
    }    
}