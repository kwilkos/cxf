package org.objectweb.celtix.bus.ws.rm;

import java.lang.reflect.Method;

import org.objectweb.celtix.bindings.AbstractBindingBase;
import org.objectweb.celtix.bindings.DataBindingCallback;
import org.objectweb.celtix.bindings.Request;
import org.objectweb.celtix.bus.jaxws.JAXBDataBindingCallback;
import org.objectweb.celtix.ws.rm.wsdl.SequenceAbstractPortType;

public class SequenceInfoRequest extends Request {
    
    private static final String METHOD_NAME = "sequenceInfo";
    
    public SequenceInfoRequest(AbstractBindingBase b) {
        
        super(b, b.createObjectContext());
        getObjectMessageContext().setRequestorRole(true);
        
        setAddressingProperties();
    }
    
    public DataBindingCallback createDataBindingCallback() {
        Method method  = null;
        try {
            method = SequenceAbstractPortType.class.getMethod(
                METHOD_NAME, (Class[])null);
        } catch (NoSuchMethodException ex) {
            ex.printStackTrace();
        }
        return new JAXBDataBindingCallback(method, DataBindingCallback.Mode.PARTS, null);
    }
    
    private void setAddressingProperties() {
        RMContextUtils.storeAction(getObjectMessageContext(), 
                                   RMUtils.getRMConstants().getSequenceInfoAction());    
    }
}
