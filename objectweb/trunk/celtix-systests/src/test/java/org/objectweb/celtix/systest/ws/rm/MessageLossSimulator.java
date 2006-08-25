package org.objectweb.celtix.systest.ws.rm;

import java.math.BigInteger;
import java.util.Map;

import javax.xml.ws.handler.LogicalHandler;
import javax.xml.ws.handler.LogicalMessageContext;
import javax.xml.ws.handler.MessageContext;

import static javax.xml.ws.handler.MessageContext.MESSAGE_OUTBOUND_PROPERTY;

import org.objectweb.celtix.bus.ws.rm.RMContextUtils;
import org.objectweb.celtix.bus.ws.rm.RMUtils;
import org.objectweb.celtix.ws.rm.RMProperties;
import org.objectweb.celtix.ws.rm.SequenceAcknowledgement;
import org.objectweb.celtix.ws.rm.SequenceAcknowledgement.AcknowledgementRange;


/**
 * Simulates message loss by suppressing the acknowledgement for certain 
 * messages, before the WS-RM SOAPHandler encodes.
 */
public class MessageLossSimulator implements LogicalHandler<LogicalMessageContext> {
    
    /**
     * Discard ACK messages 2 & 4
     */
    private static final boolean[] DROP_ACKS = {false, true, false, true};  

    public void init(Map<String, Object> map) {
    }

    public void close(MessageContext context) {
    }

    public void destroy() {
    }
    
    public boolean handleMessage(LogicalMessageContext context) {
        simulateLoss(context);
        return true;
    }

    public boolean handleFault(LogicalMessageContext context) {
        return true;
    }
    
    /**
     * @return true if the current message is outbound
     */
    protected boolean isOutbound(LogicalMessageContext context) {
        Boolean outbound = (Boolean)context.get(MESSAGE_OUTBOUND_PROPERTY);
        return outbound != null && outbound.booleanValue();
    }

    private synchronized void simulateLoss(LogicalMessageContext context) {
        if (isOutbound(context)) {
            RMProperties rmps = RMContextUtils.retrieveRMProperties(context, true);
            if (rmps.getAcks() != null
                && !ResponseMisdirector.isMisdirected(context)) {
                // assume single ACK
                SequenceAcknowledgement ack = rmps.getAcks().iterator().next();
                int max = getMaxMessage(ack);
                boolean[] acked = new boolean[max];
                getAcked(ack, acked);
                if (dropAcks(acked)) {
                    ack.getAcknowledgementRange().clear();
                    updateAcks(ack, acked);
                }
            }
        }
    }

    private int getMaxMessage(SequenceAcknowledgement ack) {
        BigInteger max = BigInteger.ZERO;
        for (int i = 0; i < ack.getAcknowledgementRange().size(); i++) {
            AcknowledgementRange r = ack.getAcknowledgementRange().get(i);
            if (r.getUpper().compareTo(max) >= 0) {
                max = r.getUpper();
            }
        }
        return max.intValue();
    }

    private void getAcked(SequenceAcknowledgement ack, boolean[] acked) {
        for (int i = 0; i < ack.getAcknowledgementRange().size(); i++) {
            AcknowledgementRange r = ack.getAcknowledgementRange().get(i);
            for (BigInteger j = r.getLower();
                 j.compareTo(r.getUpper()) <= 0;
                 j = j.add(BigInteger.ONE)) {
                acked[j.intValue() - 1] = true;
            }
        }
    }
    
    private boolean dropAcks(boolean[] acked) {
        boolean dropped = false;
        for (int i = 0; i < acked.length; i++) {
            if (i < DROP_ACKS.length && DROP_ACKS[i]) {
                DROP_ACKS[i] = false;
                dropped = true;
                acked[i] = false;
            }
        }
        return dropped;
    }

    private void updateAcks(SequenceAcknowledgement ack, boolean[] acked) {
        AcknowledgementRange r = null;
        for (int i = 0; i < acked.length; i++) {
            if (acked[i]) {
                if (r == null) {
                    r = RMUtils.getWSRMFactory().createSequenceAcknowledgementAcknowledgementRange();
                    r.setLower(BigInteger.valueOf(i + 1));
                    r.setUpper(BigInteger.valueOf(i + 1));
                } else {
                    r.setUpper(BigInteger.valueOf(i + 1));
                }
            }
            
            if ((!acked[i] || i + 1 == acked.length)
                && r != null) {
                ack.getAcknowledgementRange().add(r);
                r = null;
            }
        }
    }
}
