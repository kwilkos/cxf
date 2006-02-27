package org.objectweb.celtix.bus.ws.rm;

import java.util.List;

import javax.xml.ws.handler.MessageContext;

import org.objectweb.celtix.ws.rm.AckRequestedType;
import org.objectweb.celtix.ws.rm.SequenceAcknowledgement;
import org.objectweb.celtix.ws.rm.SequenceType;

import static org.objectweb.celtix.ws.rm.JAXWSRMConstants.ACKS_PROPERTY;
import static org.objectweb.celtix.ws.rm.JAXWSRMConstants.ACKS_REQUESTED_PROPERTY;
import static org.objectweb.celtix.ws.rm.JAXWSRMConstants.SEQUENCE_PROPERTY;
import static org.objectweb.celtix.ws.rm.JAXWSRMConstants.WSA_ACTION;

/**
 * Holder for utility methods relating to contexts.
 */

public final class RMContextUtils {

    /**
     * Prevents instantiation.
     */
    private RMContextUtils() {
    }

    public static void storeAction(MessageContext context, String action) {
        context.put(WSA_ACTION, action);
    }  
    
    public static String retrieveAction(MessageContext context) {
        return (String)context.get(WSA_ACTION);
    }  
    
    public static void storeSequence(MessageContext context, Sequence seq) {
        SequenceType st = RMUtils.getWSRMFactory().createSequenceType();
        st.setIdentifier(seq.getIdentifier());
        st.setMessageNumber(seq.nextMessageNumber());   
        if (seq.isLastMessage()) {
            st.setLastMessage(new SequenceType.LastMessage());
        }
        context.put(SEQUENCE_PROPERTY, st);
        context.setScope(SEQUENCE_PROPERTY, MessageContext.Scope.HANDLER);
    }
    
    public static void storeSequence(MessageContext context, SequenceType s) {
        context.put(SEQUENCE_PROPERTY, s);
        context.setScope(SEQUENCE_PROPERTY, MessageContext.Scope.HANDLER);
    }
    
    public static SequenceType retrieveSequence(MessageContext context) {
        return (SequenceType)context.get(SEQUENCE_PROPERTY);
    }
    
    public static void storeAcknowledgments(MessageContext context, List<SequenceAcknowledgement> acks) {
        context.put(ACKS_PROPERTY, acks);
        context.setScope(ACKS_PROPERTY, MessageContext.Scope.HANDLER);
    }
    
    @SuppressWarnings("unchecked")
    public static List<SequenceAcknowledgement> retrieveAcknowledgments(MessageContext context) {
        return (List<SequenceAcknowledgement>)context.get(ACKS_PROPERTY); 
    }
    
    public static void storeAcksRequested(MessageContext context, List<AckRequestedType> requested) {
        context.put(ACKS_REQUESTED_PROPERTY, requested);
        context.setScope(ACKS_REQUESTED_PROPERTY, MessageContext.Scope.HANDLER); 
    }

    @SuppressWarnings("unchecked")
    public static List<AckRequestedType> retrieveAcksRequested(MessageContext context) {
        return (List<AckRequestedType>)context.get(ACKS_REQUESTED_PROPERTY); 
    }
}
