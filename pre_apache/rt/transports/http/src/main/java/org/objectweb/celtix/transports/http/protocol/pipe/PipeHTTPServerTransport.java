package org.objectweb.celtix.transports.http.protocol.pipe;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.wsdl.Definition;
import javax.wsdl.WSDLException;
import javax.xml.ws.handler.MessageContext;

import org.objectweb.celtix.Bus;
import org.objectweb.celtix.context.OutputStreamMessageContext;
import org.objectweb.celtix.transports.ServerTransportCallback;
import org.objectweb.celtix.transports.http.AbstractHTTPServerOutputStreamContext;
import org.objectweb.celtix.transports.http.AbstractHTTPServerTransport;
import org.objectweb.celtix.transports.http.HTTPServerInputStreamContext;
import org.objectweb.celtix.ws.addressing.EndpointReferenceType;
import org.objectweb.celtix.wsdl.EndpointReferenceUtils;

public class PipeHTTPServerTransport extends AbstractHTTPServerTransport {
    private static final String PIPE_RESPONSE = "PIPE_RESPONSE";
    
    public PipeHTTPServerTransport(Bus b, EndpointReferenceType ref) throws WSDLException, IOException {
        super(b, ref);
    }

    protected void copyRequestHeaders(MessageContext ctx, Map<String, List<String>> headers) {
        PipeResponse req = (PipeResponse)ctx.get(PIPE_RESPONSE);
        Map<String, List<String>> reqHeaders = req.getRequestHeaders();
        
        for (String fname : reqHeaders.keySet()) {
            List<String> values;
            if (headers.containsKey(fname)) {
                values = headers.get(fname);
            } else {
                values = new ArrayList<String>();
                headers.put(fname, values);
            }
            values.addAll(reqHeaders.get(fname));
        }        
    }

    public void activate(ServerTransportCallback cb) throws IOException {
        callback = cb;
        PipeServer.SERVERS.put(name, this);
    }

    public void deactivate() throws IOException {
        PipeServer.SERVERS.remove(name);
    }

    public void postDispatch(MessageContext bindingContext, OutputStreamMessageContext context) {
        PipeResponse resp = (PipeResponse)context.get(PIPE_RESPONSE);
        try {
            resp.getOutputStream().close();
        } catch (IOException e) {
            //ignore
            e.printStackTrace();
        }
    }

    
    public OutputStreamMessageContext createOutputStreamContext(MessageContext context) throws IOException {
        return new AbstractHTTPServerOutputStreamContext(this, context) {
            @SuppressWarnings("unchecked")
            protected void flushHeaders() throws IOException {
                PipeResponse resp = (PipeResponse)context.get(PIPE_RESPONSE);
                
                Map<String, List<String>> respHeaders = 
                    new LinkedHashMap<String, List<String>>();
                
                if (containsKey(HTTP_RESPONSE_CODE)) {
                    respHeaders.put(HTTP_RESPONSE_CODE,
                                    Arrays.asList(new String[] {get(HTTP_RESPONSE_CODE).toString()}));
                }
                Map<String, List<String>> headers 
                    = (Map<String, List<String>>)super.get(HTTP_RESPONSE_HEADERS);
                if (null != headers) {
                    for (String name : headers.keySet()) {
                        List<String> headerList = (List<String>)headers.get(name);
                        respHeaders.put(name, headerList);
                    }
                }
                OutputStream out = resp.setResponse(respHeaders);
                origOut.resetOut(new BufferedOutputStream(out, 1024));
            }
        };
    }
    
    
    void doService(PipeResponse response) throws IOException {
        
        final class Servicer implements Runnable {
            private final PipeResponse response;
            
            Servicer(PipeResponse resps) {
                response = resps;
            }
            public void run() {
                try {
                    serviceRequest(response);                        
                } catch (IOException ex) {                        
                    // TODO handle exception
                    ex.printStackTrace();
                }
            } 
        }
        
        Servicer servicer = new Servicer(response);
        if (null == callback.getExecutor()) {
            bus.getWorkQueueManager().getAutomaticWorkQueue().execute(servicer);
        } else {  
            callback.getExecutor().execute(servicer);
        }        
    }
    
    void serviceRequest(final PipeResponse response) throws IOException {
        if (!response.getURLConnection().getDoInput()) {
            try {
                Definition def = EndpointReferenceUtils.getWSDLDefinition(bus.getWSDLManager(), reference);
                Map<String, List<String>> headers = new LinkedHashMap<String, List<String>>();
                headers.put("Content-Type", Arrays.asList(new String[] {"text/xml"}));
                OutputStream out = response.setResponse(headers);
                bus.getWSDLManager().getWSDLFactory().newWSDLWriter().writeWSDL(def, out);
                out.flush();
                out.close();
                return;
            } catch (WSDLException ex) {
                // TODO Auto-generated catch block
                ex.printStackTrace();
            }
        }
                
        HTTPServerInputStreamContext ctx = new HTTPServerInputStreamContext(this) {
            public void initContext() throws IOException {
                super.initContext();
                inStream = response.getRequestInputStream();
                origInputStream = inStream;
            }
        };
        ctx.put(PIPE_RESPONSE, response);
        ctx.initContext();
        
        callback.dispatch(ctx, this);
    }
    

}
