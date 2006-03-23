package org.objectweb.celtix.bus.ws.rm;

import javax.xml.ws.handler.MessageContext;

import org.objectweb.celtix.ws.rm.JAXWSRMConstants;
import org.objectweb.celtix.ws.rm.RMProperties;

/**
 * Holder for utility methods relating to contexts.
 */

public final class RMContextUtils {

    /**
     * Prevents instantiation.
     */
    private RMContextUtils() {
    }
    
    public static RMProperties retrieveRMProperties(MessageContext context, boolean outbound) {
        return (RMProperties)context.get(getRMPropertiesKey(outbound));
    }
    
    public static void storeRMProperties(MessageContext context, RMProperties rmps, boolean outbound) {
        String key = getRMPropertiesKey(outbound);
        context.put(key, rmps);
        context.setScope(key, MessageContext.Scope.HANDLER);
    }
    
    private static String getRMPropertiesKey(boolean outbound) {
        return outbound ? JAXWSRMConstants.RM_PROPERTIES_OUTBOUND : JAXWSRMConstants.RM_PROPERTIES_INBOUND;
    }
    
    /*
    public static void storeSequence(MessageContext context, Sequence seq) {
        SequenceType st = RMUtils.getWSRMFactory().createSequenceType();
        st.setIdentifier(seq.getIdentifier());
        st.setMessageNumber(seq.getCurrentMessageNumber());   
        if (seq.getCurrentMessageNumber().equals(seq.getLastMessageNumber())) {
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
    
    public static void removeSequence(MessageContext context) {
        context.remove(SEQUENCE_PROPERTY);
    }
    
    public static void storeAcknowledgments(MessageContext context, 
                                            Collection<SequenceAcknowledgement> acks) {
        context.put(ACKS_PROPERTY, acks);
        context.setScope(ACKS_PROPERTY, MessageContext.Scope.HANDLER);
    }
    
    public static void storeAcknowledgment(MessageContext context, 
                                            Sequence seq) {
        Collection<SequenceAcknowledgement> acks = retrieveAcknowledgments(context);
        if (null == acks) {
            acks = new ArrayList<SequenceAcknowledgement>();
            context.put(ACKS_PROPERTY, acks);
            context.setScope(ACKS_PROPERTY, MessageContext.Scope.HANDLER);
        }
        SequenceAcknowledgement ack = seq.getAcknowledged();
        acks.add(ack);
        seq.acknowledgmentSent();
    }
    
    @SuppressWarnings("unchecked")
    public static Collection<SequenceAcknowledgement> retrieveAcknowledgments(MessageContext context) {
        return (Collection<SequenceAcknowledgement>)context.get(ACKS_PROPERTY); 
    }
    
    public static void removeAcknowledgments(MessageContext context) {
        context.remove(ACKS_PROPERTY);
    }
    
    public static void storeAcksRequested(MessageContext context, Collection<AckRequestedType> requested) {
        context.put(ACKS_REQUESTED_PROPERTY, requested);
        context.setScope(ACKS_REQUESTED_PROPERTY, MessageContext.Scope.HANDLER); 
    }

    @SuppressWarnings("unchecked")
    public static Collection<AckRequestedType> retrieveAcksRequested(MessageContext context) {
        return (Collection<AckRequestedType>)context.get(ACKS_REQUESTED_PROPERTY); 
    }
    
    public static void removeAcksRequested(MessageContext context) {
        context.remove(ACKS_REQUESTED_PROPERTY);
    }
    
    public static void removeRMProperties(MessageContext context) {
        removeSequence(context);
        removeAcknowledgments(context);
        removeAcksRequested(context);
    }
    */
}
