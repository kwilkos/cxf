package org.objectweb.celtix.bus.ws.rm;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.bind.JAXBElement;
import javax.xml.ws.ProtocolException;
import javax.xml.ws.WebServiceException;

import org.objectweb.celtix.bindings.DataBindingCallback;
import org.objectweb.celtix.common.logging.LogUtils;
import org.objectweb.celtix.context.ObjectMessageContext;
import org.objectweb.celtix.ws.rm.CreateSequenceResponseType;
import org.objectweb.celtix.ws.rm.wsdl.SequenceFault;

public class RMProxy {

    private static final Logger LOG = LogUtils.getL7dLogger(RMProxy.class);
    private RMHandler handler;

    public RMProxy(RMHandler h) {
        handler = h;
    }

    public CreateSequenceResponseType createSequence(RMSource source) throws IOException, SequenceFault {
        CreateSequenceRequest request = new CreateSequenceRequest(handler.getBinding(), source);

        ObjectMessageContext responseCtx = invoke(request.getObjectMessageContext(), 
                                                  CreateSequenceRequest.createDataBindingCallback());
        Object result = responseCtx.getReturn();
        if (result instanceof JAXBElement) {
            result = ((JAXBElement)result).getValue();
        }
        CreateSequenceResponseType csr = (CreateSequenceResponseType)result;
        
        Sequence seq = new Sequence(csr.getIdentifier(), source, csr.getExpires());
        source.addSequence(seq);
        
        return csr;
    }
    
    public void terminateSequence(Sequence seq) throws IOException {
        TerminateSequenceRequest request = new TerminateSequenceRequest(handler.getBinding(), seq);       
        invokeOneWay(request.getObjectMessageContext(), CreateSequenceRequest.createDataBindingCallback());
    }
    
    /*
    public void acknowledge(Sequence seq) {
        SequenceInfoRequest request = new SequenceInfoRequest(handler.getBinding());
    }
    */
    
    public void sequenceInfo() throws IOException {
        SequenceInfoRequest request = new SequenceInfoRequest(handler.getBinding());
        invokeOneWay(request.getObjectMessageContext(), request.createDataBindingCallback());
    }
    
    private ObjectMessageContext invoke(ObjectMessageContext requestCtx, DataBindingCallback callback) 
        throws IOException, SequenceFault {
        ObjectMessageContext responseCtx = null;
        if (handler.getClientBinding() != null) {
            responseCtx = handler.getClientBinding().invoke(requestCtx, callback); 
            throwIfNecessary(responseCtx);
        } else {
            // wait for changes on the transport decoupling -
            // server transport should allow to send this out of band request
        } 
        return responseCtx;
    }
    
    private void invokeOneWay(ObjectMessageContext requestCtx, DataBindingCallback callback)
        throws IOException {
        if (handler.getClientBinding() != null) {
            handler.getClientBinding().invokeOneWay(requestCtx, callback);
        } else {
            // wait for changes on the transport decoupling -
            // server transport should allow to send this out of band request
        }
    }
    
    private void throwIfNecessary(ObjectMessageContext objextCtx) throws SequenceFault
    {
        Throwable t = objextCtx.getException();
        if (null != t) {
            LOG.log(Level.INFO, "RM_INVOCATION_FAILED", t);
            if (ProtocolException.class.isAssignableFrom(t.getClass())) {
                throw (ProtocolException)t;
            } else if (WebServiceException.class.isAssignableFrom(t.getClass())) {
                throw (WebServiceException)t;
            } else if (SequenceFault.class.isAssignableFrom(t.getClass())) {
                throw (SequenceFault)t;
            } else {                
                throw new ProtocolException(objextCtx.getException());
            }
        }        
    }

}
