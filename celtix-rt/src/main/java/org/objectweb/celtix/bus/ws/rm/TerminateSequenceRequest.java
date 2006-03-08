package org.objectweb.celtix.bus.ws.rm;

import java.lang.reflect.Method;

import org.objectweb.celtix.bindings.AbstractBindingBase;
import org.objectweb.celtix.bindings.DataBindingCallback;
import org.objectweb.celtix.bindings.Request;
import org.objectweb.celtix.bus.jaxws.JAXBDataBindingCallback;
import org.objectweb.celtix.ws.rm.TerminateSequenceType;
import org.objectweb.celtix.ws.rm.wsdl.SequenceAbstractPortType;

public class TerminateSequenceRequest extends Request {
    private static final String METHOD_NAME = "terminateSequence";
    
    public TerminateSequenceRequest(AbstractBindingBase b, Sequence seq) {
        
        super(b, b.createObjectContext());
        getObjectMessageContext().setRequestorRole(true);
        getObjectMessageContext().setMethod(getMethod());       
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
    
    public static DataBindingCallback createDataBindingCallback() {
        Method method  = getMethod();
        return new JAXBDataBindingCallback(method, DataBindingCallback.Mode.PARTS, null);
    }
    
    private void setMessageParameters(Sequence seq) {
        
        TerminateSequenceType ts = RMUtils.getWSRMFactory().createTerminateSequenceType();
        ts.setIdentifier(seq.getIdentifier());
        
        getObjectMessageContext().setMessageObjects(new Object[] {ts});
    }
}
