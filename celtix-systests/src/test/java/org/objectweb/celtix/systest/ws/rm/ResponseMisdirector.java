package org.objectweb.celtix.systest.ws.rm;

import java.util.Map;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;


import org.objectweb.celtix.ws.addressing.AddressingPropertiesImpl;
import org.objectweb.celtix.ws.addressing.ContextUtils;
import org.objectweb.celtix.ws.rm.RMContextUtils;
import org.objectweb.celtix.ws.rm.RMProperties;

import static org.objectweb.celtix.ws.addressing.JAXWSAConstants.SERVER_ADDRESSING_PROPERTIES_INBOUND;
import static org.objectweb.celtix.ws.addressing.JAXWSAConstants.SERVER_ADDRESSING_PROPERTIES_OUTBOUND;


/**
 * Arranges for responses to be misdirected by modifying the WS-A ReplyTo header
 * before the WS-A MapAggregator has a chance to rebase the ServerTransport.
 */
public class ResponseMisdirector implements SOAPHandler<SOAPMessageContext> {
    
    private static final String NOWHERE = "http://nowhere.nada.nothing.nought:5555";
    
    /**
     * Misdirect messages 2 & 4
     */
    private static final boolean[] MISDIRECTS = {false, true, false, true};
    
   
    public ResponseMisdirector() {
    }
    
    public void init(Map<String, Object> params) {
    }
    
    public Set<QName> getHeaders() {
        return null;
    }

    public boolean handleMessage(SOAPMessageContext context) {
        misdirect(context);
        return true;
    }

    public boolean handleFault(SOAPMessageContext context) {
        misdirect(context);
        return true;
    }

    public void close(MessageContext context) {
    }

    public void destroy() {
    }

    private synchronized void misdirect(SOAPMessageContext context) {
        if (!(ContextUtils.isOutbound(context) 
              || ContextUtils.isRequestor(context))) {
            RMProperties props = RMContextUtils.retrieveRMProperties(context, false);
            AddressingPropertiesImpl maps = (AddressingPropertiesImpl)
                context.get(SERVER_ADDRESSING_PROPERTIES_INBOUND);
            if (isMisdirectable(maps, props)) {
                maps.getReplyTo().getAddress().setValue(NOWHERE);
            }
        }
    }
    
    protected static boolean isMisdirected(MessageContext context) {
        boolean misdirected = false;
        AddressingPropertiesImpl maps = (AddressingPropertiesImpl)
            context.get(SERVER_ADDRESSING_PROPERTIES_OUTBOUND);
        if (maps != null && maps.getReplyTo() != null) {
            misdirected = NOWHERE.equals(maps.getReplyTo().getAddress().getValue());
        }
        return misdirected;
    }

    
    private boolean isMisdirectable(AddressingPropertiesImpl maps, RMProperties props) {
        boolean misdirect = false;
        if (maps != null
            && (!ContextUtils.isGenericAddress(maps.getReplyTo()))
            && props != null
            && props.getSequence() != null
            && props.getSequence().getMessageNumber() != null) {
            int num = props.getSequence().getMessageNumber().intValue();
            if (MISDIRECTS.length >= num && MISDIRECTS[num - 1]) {
                misdirect = true;
                MISDIRECTS[num - 1] = false;
            }
        }
        return misdirect;
    }    
}
