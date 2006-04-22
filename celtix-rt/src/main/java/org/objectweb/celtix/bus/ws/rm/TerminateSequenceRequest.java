package org.objectweb.celtix.bus.ws.rm;

import java.lang.reflect.Method;

import org.objectweb.celtix.bindings.AbstractBindingBase;
import org.objectweb.celtix.bindings.DataBindingCallback;
import org.objectweb.celtix.bindings.Request;
import org.objectweb.celtix.bus.jaxws.JAXBDataBindingCallback;
import org.objectweb.celtix.bus.ws.addressing.AddressingPropertiesImpl;
import org.objectweb.celtix.bus.ws.addressing.ContextUtils;
import org.objectweb.celtix.transports.Transport;
import org.objectweb.celtix.ws.addressing.AddressingProperties;
import org.objectweb.celtix.ws.addressing.AttributedURIType;
import org.objectweb.celtix.ws.rm.TerminateSequenceType;
import org.objectweb.celtix.ws.rm.wsdl.SequenceAbstractPortType;

public class TerminateSequenceRequest extends Request {
    
    private static final String METHOD_NAME = "terminateSequence";
    private static final String OPERATION_NAME = "TerminateSequence";
    
    public TerminateSequenceRequest(AbstractBindingBase b, Transport t, AbstractSequenceImpl seq) {
        
        super(b, t, b.createObjectContext());
        getObjectMessageContext().setRequestorRole(true);
        getObjectMessageContext().setMethod(getMethod()); 
        
        AddressingProperties maps = new AddressingPropertiesImpl();
        AttributedURIType actionURI = ContextUtils.WSA_OBJECT_FACTORY.createAttributedURIType();
        actionURI.setValue(RMUtils.getRMConstants().getTerminateSequenceAction());
        maps.setAction(actionURI);
        ContextUtils.storeMAPs(maps, getObjectMessageContext(), true, true, true, true);
        
        setMessageParameters(seq);
    }
    
    public static Method getMethod() {
        Method method  = null;
        try {
            method = SequenceAbstractPortType.class.getMethod(
                METHOD_NAME, 
                new Class[] {TerminateSequenceType.class});
        } catch (NoSuchMethodException ex) {
            ex.printStackTrace();
        }
        return method;
    }
    
    public static String getOperationName() {
        return OPERATION_NAME;
    }
    
    public static DataBindingCallback createDataBindingCallback() {
        Method method  = getMethod();
        return new JAXBDataBindingCallback(method, DataBindingCallback.Mode.PARTS, null);
    }
    
    private void setMessageParameters(AbstractSequenceImpl seq) {
        
        TerminateSequenceType ts = RMUtils.getWSRMFactory().createTerminateSequenceType();
        ts.setIdentifier(seq.getIdentifier());
        
        getObjectMessageContext().setMessageObjects(new Object[] {ts});
    }
}
