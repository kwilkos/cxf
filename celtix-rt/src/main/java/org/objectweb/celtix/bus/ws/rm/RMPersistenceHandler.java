package org.objectweb.celtix.bus.ws.rm;

import java.util.Set;

import javax.annotation.Resource;
import javax.xml.namespace.QName;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;

import org.objectweb.celtix.bindings.AbstractBindingBase;
import org.objectweb.celtix.bindings.ClientBinding;
import org.objectweb.celtix.bindings.JAXWSConstants;
import org.objectweb.celtix.bindings.ServerBinding;
import org.objectweb.celtix.bus.ws.addressing.ContextUtils;
import org.objectweb.celtix.context.MessageContextWrapper;
import org.objectweb.celtix.context.ObjectMessageContext;
import org.objectweb.celtix.context.ObjectMessageContextImpl;


public class RMPersistenceHandler implements SOAPHandler<SOAPMessageContext> {
    
    @Resource(name = JAXWSConstants.CLIENT_BINDING_PROPERTY) private ClientBinding clientBinding;
    @Resource(name = JAXWSConstants.SERVER_BINDING_PROPERTY) private ServerBinding serverBinding;
    
    public Set<QName> getHeaders() {
        return null;
    }

    public void close(MessageContext arg0) {
    }

    public boolean handleFault(SOAPMessageContext context) {
        if (ContextUtils.isOutbound(context)) {
            handleOutbound(context);
        }
        return true;
    }

    public boolean handleMessage(SOAPMessageContext context) {
        if (ContextUtils.isOutbound(context)) {
            handleOutbound(context);
        }
        return true;
    }
    
    void handleOutbound(SOAPMessageContext context) {
        // tell the source to store a copy of the message in the
        // retransmission queue
        // and schedule the next retransmission
        RMHandler handler = RMHandler.getHandler(getBinding());

        MessageContext unwrapped = MessageContextWrapper.unwrap(context);
        ObjectMessageContext objCtx = null;
        if (unwrapped instanceof ObjectMessageContext) {
            objCtx = (ObjectMessageContext)unwrapped;
        } else {
            objCtx = new ObjectMessageContextImpl();
            objCtx.putAll(unwrapped);
        }
        handler.getSource().addUnacknowledged(objCtx);
    }
    
    AbstractBindingBase getBinding() {
        if (null != clientBinding) {
            return (AbstractBindingBase)clientBinding;
        }
        return (AbstractBindingBase)serverBinding;
    }
   
    
}
