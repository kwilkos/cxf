package org.objectweb.celtix.bus.ws.rm;

import java.math.BigInteger;
import java.util.Set;
import java.util.logging.Logger;

import javax.annotation.Resource;
import javax.xml.namespace.QName;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;

import org.objectweb.celtix.bindings.AbstractBindingBase;
import org.objectweb.celtix.bindings.ClientBinding;
import org.objectweb.celtix.bindings.JAXWSConstants;
import org.objectweb.celtix.bindings.ServerBinding;
import org.objectweb.celtix.bus.ws.addressing.AddressingPropertiesImpl;
import org.objectweb.celtix.bus.ws.addressing.ContextUtils;
import org.objectweb.celtix.bus.ws.addressing.VersionTransformer;
import org.objectweb.celtix.common.logging.LogUtils;
import org.objectweb.celtix.ws.rm.Identifier;


public class RMPersistenceHandler implements SOAPHandler<SOAPMessageContext> {
    
    private static final Logger LOG = LogUtils.getL7dLogger(RMPersistenceHandler.class);
    
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
        LOG.entering(getClass().getName(), "handleOutbound");
        // do nothing unless this is an application message
        if (!isApplicationMessage(context)) {
            return; 
        }
        
        // tell the source to store a copy of the message in the
        // retransmission queue
        // and schedule the next retransmission
        
        RMPropertiesImpl rmpsOut = 
            (RMPropertiesImpl)RMContextUtils.retrieveRMProperties(context, true);
        // assert null != rmpsOut;
        
        if (null == rmpsOut) {
            // handler chain traversal may have been reversed before
            // reaching RM logical handler - OK to ignore?
            return;
        }
        
        if (null == rmpsOut.getSequence()) {
            // cannot be an application message (may be a partial response)
            return;
        }
        
        BigInteger mn = rmpsOut.getSequence().getMessageNumber();
        boolean lm = null != rmpsOut.getSequence().getLastMessage();
        Identifier sid = rmpsOut.getSequence().getIdentifier();        
        
        // create a new SourceSequence object instead of retrieving the one
        // maintained by the RM source for the sequence identifier 
        // as the current/last message number properties of the latter may have 
        // changed since
        
        SourceSequence seq = new SourceSequence(sid, null, null, mn, lm);
        RMHandler handler = RMHandler.getHandler(getBinding());         
        RMMessageImpl msg = new RMMessageImpl(mn, context);

        handler.getSource().addUnacknowledged(seq, msg);
    }
    
    AbstractBindingBase getBinding() {
        if (null != clientBinding) {
            return (AbstractBindingBase)clientBinding;
        }
        return (AbstractBindingBase)serverBinding;
    }
    
    private boolean isApplicationMessage(SOAPMessageContext context) {
        boolean isApplicationMessage = true;
        AddressingPropertiesImpl maps =
            ContextUtils.retrieveMAPs(context, false, true);
        
        if (null == maps) {
            return false;
        }
      
        // ensure the appropriate version of WS-Addressing is used       
        maps.exposeAs(VersionTransformer.Names200408.WSA_NAMESPACE_NAME);

        String action = null;
        if (maps != null && null != maps.getAction()) {
            action = maps.getAction().getValue();
        }
        if (RMUtils.getRMConstants().getCreateSequenceAction().equals(action)
            || RMUtils.getRMConstants().getCreateSequenceResponseAction().equals(action)
            || RMUtils.getRMConstants().getTerminateSequenceAction().equals(action)
            || RMUtils.getRMConstants().getLastMessageAction().equals(action)
            || RMUtils.getRMConstants().getSequenceAcknowledgmentAction().equals(action)
            || RMUtils.getRMConstants().getSequenceInfoAction().equals(action)) {
            isApplicationMessage = false;
        }
        return isApplicationMessage;
    }
   
    
}
