package org.objectweb.celtix.bus.ws.rm;

import java.lang.reflect.Method;

import org.objectweb.celtix.bindings.AbstractBindingBase;
import org.objectweb.celtix.bindings.DataBindingCallback;
import org.objectweb.celtix.bindings.Request;
import org.objectweb.celtix.bus.jaxws.JAXBDataBindingCallback;
import org.objectweb.celtix.bus.ws.addressing.AddressingPropertiesImpl;
import org.objectweb.celtix.bus.ws.addressing.ContextUtils;
import org.objectweb.celtix.ws.addressing.AddressingProperties;
import org.objectweb.celtix.ws.addressing.AttributedURIType;
import org.objectweb.celtix.ws.rm.wsdl.SequenceAbstractPortType;

public class SequenceInfoRequest extends Request {
    
    private static final String METHOD_NAME = "sequenceInfo";
    
    public SequenceInfoRequest(AbstractBindingBase b) {
        
        super(b, b.createObjectContext());
        getObjectMessageContext().setRequestorRole(true);
        AddressingProperties maps = new AddressingPropertiesImpl();
        AttributedURIType actionURI =
            ContextUtils.WSA_OBJECT_FACTORY.createAttributedURIType();
        actionURI.setValue(RMUtils.getRMConstants().getSequenceInfoAction());
        maps.setAction(actionURI);
        ContextUtils.storeMAPs(maps, getObjectMessageContext(), true);
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
}
