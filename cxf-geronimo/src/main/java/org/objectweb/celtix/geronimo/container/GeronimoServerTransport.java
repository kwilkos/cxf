package org.objectweb.celtix.geronimo.container;

import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.logging.Logger;

import javax.wsdl.WSDLException;
import javax.xml.ws.handler.MessageContext;

import org.apache.geronimo.webservices.WebServiceContainer.Request;
import org.apache.geronimo.webservices.WebServiceContainer.Response;
import org.objectweb.celtix.Bus;
import org.objectweb.celtix.context.OutputStreamMessageContext;
import org.objectweb.celtix.transports.ServerTransportCallback;
import org.objectweb.celtix.transports.http.AbstractHTTPServerTransport;
import org.objectweb.celtix.ws.addressing.EndpointReferenceType;

public class GeronimoServerTransport extends AbstractHTTPServerTransport 
                                     implements Serializable {

    private static final Logger LOG = Logger.getLogger(GeronimoServerTransport.class.getName());
    
    public GeronimoServerTransport(Bus bus, EndpointReferenceType address) throws WSDLException, IOException {
        super(bus, address);
    }
    
    public void invoke(Request request, Response response) {
        assert callback != null : "callback is null";
        
        GeronimoInputStreamMessageContext msgCtx = createInputStreamMessageContext();
        msgCtx.setRequest(request);
        msgCtx.setResponse(response);
        callback.dispatch(msgCtx, this);
    }

    public void activate(ServerTransportCallback aCallback) throws IOException {
        LOG.fine("geronimo server transport activating: " + callback);
        callback = aCallback;
    }

    public void deactivate() throws IOException {
        System.out.println("geronimo server transport deactivating");
    }

    public OutputStreamMessageContext rebase(MessageContext context, 
                                             EndpointReferenceType decoupledResponseEndpoint) 
        throws IOException {
        // TODO Auto-generated method stub
        return null;
    }

    public void postDispatch(MessageContext bindingContext, OutputStreamMessageContext context) {
        System.out.println("geronimo server transport postDispatch");
    }

    public void shutdown() {
        // TODO Auto-generated method stub
        
    }

    public OutputStreamMessageContext createOutputStreamContext(MessageContext context) throws IOException {
        return new GeronimoOutputStreamServerMessageContext(context);
    }

    public void finalPrepareOutputStreamContext(OutputStreamMessageContext context) throws IOException {
        // do nothing 
    }

    @Override
    protected void copyRequestHeaders(MessageContext ctx, Map<String, List<String>> headers) {
        assert ctx instanceof GeronimoInputStreamMessageContext; 
        GeronimoInputStreamMessageContext gctx = (GeronimoInputStreamMessageContext)ctx;
        Request req = gctx.getRequest();
        
        // no map of headers so just find all static field constants that begin with HEADER_, get
        // its value and get the corresponding header.
        for (Field field : Request.class.getFields()) {
            if (field.getName().startsWith("HEADER_")) {
                try {
                    assert field.getType().equals(String.class) : "unexpected field type";
                    String headerName = (String)field.get(null);
                    String headerValue = req.getHeader(headerName);
                    if (headerValue != null) {
                        List<String> values = headers.get(headerName);
                        if (values == null) {
                            values = new LinkedList<String>();
                            headers.put(headerName, values);
                        }
                        values.addAll(splitMultipleHeaderValues(headerValue));
                    }
                } catch (IllegalAccessException ex) {
                    // ignore 
                }
            }
        }
    }
    
    private List<String> splitMultipleHeaderValues(String value) {
        
        List<String> allValues = new LinkedList<String>(); 
        if (value.contains(",")) {
            StringTokenizer st = new StringTokenizer(value, ",");
            while (st.hasMoreTokens()) {
                allValues.add(st.nextToken().trim());
            }
            
        } else {
            allValues.add(value);
        }
        return allValues;
    }
    
    
    protected GeronimoInputStreamMessageContext createInputStreamMessageContext() {
        return new GeronimoInputStreamMessageContext();
    }

}
