package org.objectweb.celtix.bus.ws.rm;

import java.io.IOException;

import org.objectweb.celtix.context.ObjectMessageContext;
import org.objectweb.celtix.ws.rm.CreateSequenceResponseType;

public class RMService {

    private RMHandler handler;

    public RMService(RMHandler h) {
        handler = h;
    }

    public CreateSequenceResponseType createSequence(RMSource source) throws IOException {
        CreateSequenceRequest request = new CreateSequenceRequest(handler.getBinding(), source);

        CreateSequenceResponseType createSequenceResponse = null;

        if (handler.getClientBinding() != null) {
            ObjectMessageContext objectCtx = handler.getClientBinding()
                .invoke(request.getObjectMessageContext(), request.createDataBindingCallback()); 
            
            createSequenceResponse = (CreateSequenceResponseType)objectCtx.getReturn();

        } else {
            // wait for changes on the transport decoupling -
            // server transport should allow to send this out of band request
        }

        return createSequenceResponse;
    }
    
    public void terminateSequence(Sequence seq) throws IOException {
        TerminateSequenceRequest request = new TerminateSequenceRequest(handler.getBinding(), seq);
        
        if (handler.getClientBinding() != null) {
            handler.getClientBinding().invokeOneWay(request.getObjectMessageContext(), 
                                                    request.createDataBindingCallback()); 

        } else {
            // wait for changes on the transport decoupling -
            // server transport should allow to send this out of band request
        }        
    }

}
