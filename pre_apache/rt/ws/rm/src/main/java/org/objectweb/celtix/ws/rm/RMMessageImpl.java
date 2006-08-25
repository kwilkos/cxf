package org.objectweb.celtix.ws.rm;

import java.io.InputStream;
import java.math.BigInteger;

import javax.xml.ws.handler.MessageContext;

import org.objectweb.celtix.ws.rm.persistence.RMMessage;

public class RMMessageImpl implements RMMessage {

    private BigInteger messageNumber;
    private MessageContext context;
    
    public RMMessageImpl(BigInteger mn, MessageContext ctx) {
        messageNumber = mn;
        context = ctx;
    }
    
    public RMMessageImpl(BigInteger mn, InputStream is) {
        messageNumber = mn;
        context = RMUtils.getPersistenceUtils().getContext(is);
    }
    
    public MessageContext getContext() {
        return context;
    }

    public InputStream getContextAsStream() {
        return RMUtils.getPersistenceUtils().getContextAsInputStream(this.getContext());
    }

    public BigInteger getMessageNr() {
        return messageNumber;
    }

}
