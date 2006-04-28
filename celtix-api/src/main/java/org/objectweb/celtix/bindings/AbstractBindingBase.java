package org.objectweb.celtix.bindings;

import java.io.IOException;

import javax.xml.ws.Binding;

import org.objectweb.celtix.Bus;
import org.objectweb.celtix.configuration.Configuration;
import org.objectweb.celtix.context.InputStreamMessageContext;
import org.objectweb.celtix.context.ObjectMessageContext;
import org.objectweb.celtix.context.ObjectMessageContextImpl;
import org.objectweb.celtix.context.OutputStreamMessageContext;
import org.objectweb.celtix.handlers.HandlerInvoker;
import org.objectweb.celtix.transports.ServerTransport;
import org.objectweb.celtix.transports.Transport;
import org.objectweb.celtix.ws.addressing.EndpointReferenceType;

public abstract class AbstractBindingBase implements BindingBase {

    protected final Bus bus;
    protected final EndpointReferenceType reference;
    protected Transport transport;
    
    protected AbstractBindingBase(Bus b, EndpointReferenceType r) {
        bus = b;
        reference = r;
    }
    
    public Bus getBus() {
        return bus;
    }
    
    public EndpointReferenceType getEndpointReference() {
        return reference;
    }
    
    public Binding getBinding() {
        return getBindingImpl();
    }
    
    public Transport retrieveTransport() {
        return transport;
    }

    public ObjectMessageContext createObjectContext() {
        return new ObjectMessageContextImpl();
    }

    public HandlerInvoker createHandlerInvoker() {
        return getBindingImpl().createHandlerInvoker();
    }

    public void configureSystemHandlers(Configuration endpointConfiguration) {      
        getBindingImpl().configureSystemHandlers(endpointConfiguration);
    }

    public void send(Request request, DataBindingCallback callback) 
        throws IOException {
        ObjectMessageContext objectCtx = request.getObjectMessageContext();
        BindingContextUtils.storeDataBindingCallback(objectCtx, callback);
        
        try {
            OutputStreamMessageContext ostreamCtx = request.process(null);

            if (null != ostreamCtx) {
                if (BindingContextUtils.isOnewayTransport(ostreamCtx)
                    || transport instanceof ServerTransport) {
                    // REVISIT: replace with Transport.send()
                    ostreamCtx.getOutputStream().close();
                    ostreamCtx.getCorrespondingInputStreamContext().getInputStream().close();
                } else {
                    ostreamCtx.getOutputStream().close();
                    // handle partial reponse
                    InputStreamMessageContext istreamCtx =
                        ostreamCtx.getCorrespondingInputStreamContext();
                    Response response = new Response(request);     
                    response.processProtocol(istreamCtx);
                    response.processLogical(null);
                }
            }
    
        } finally {
            request.complete();
        }
    }
    
    
    public abstract AbstractBindingImpl getBindingImpl();


}
