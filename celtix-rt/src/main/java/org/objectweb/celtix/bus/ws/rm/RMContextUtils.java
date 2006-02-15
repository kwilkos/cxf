package org.objectweb.celtix.bus.ws.rm;


import javax.xml.ws.handler.MessageContext;

import org.objectweb.celtix.bindings.AbstractBindingBase;
import org.objectweb.celtix.bindings.AbstractClientBinding;
import org.objectweb.celtix.bindings.AbstractServerBinding;
import org.objectweb.celtix.ws.rm.SequenceType;

import static org.objectweb.celtix.ws.rm.JAXWSRMConstants.ABSTRACT_CLIENT_BINDING_PROPERTY;
import static org.objectweb.celtix.ws.rm.JAXWSRMConstants.ABSTRACT_SERVER_BINDING_PROPERTY;
import static org.objectweb.celtix.ws.rm.JAXWSRMConstants.SEQUENCE_PROPERTIES;
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

    public static AbstractClientBinding retrieveClientBinding(MessageContext context) {
        return (AbstractClientBinding)context.get(ABSTRACT_CLIENT_BINDING_PROPERTY);
    }
    
    public static AbstractServerBinding retrieveServerBinding(MessageContext context) {
        return (AbstractServerBinding)context.get(ABSTRACT_SERVER_BINDING_PROPERTY);
    }
    
    public static AbstractBindingBase retrieveBinding(MessageContext context) {
        Object o = context.get(ABSTRACT_CLIENT_BINDING_PROPERTY);
        if (null == o) {
            o = context.get(ABSTRACT_SERVER_BINDING_PROPERTY);            
        }
        return (AbstractBindingBase)o;
    }
    
    public static void storeAction(MessageContext context, String action) {
        context.put(WSA_ACTION, action);
    }  
    
    public static String retrieveAction(MessageContext context) {
        return (String)context.get(WSA_ACTION);
    }  
    
    public static void storeSequenceProperties(MessageContext context, Sequence seq) {
        SequenceType st = RMUtils.getWSRMFactory().createSequenceType();
        st.setIdentifier(seq.getIdentifier());
        st.setMessageNumber(seq.nextMessageNumber());   
        if (seq.isLastMessage()) {
            st.setLastMessage(new SequenceType.LastMessage());
        }
        context.put(SEQUENCE_PROPERTIES, st);
    }
    
    public static SequenceType retrieveSequenceProperties(MessageContext context) {
        return (SequenceType)context.get(SEQUENCE_PROPERTIES);
    }
}
