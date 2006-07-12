package org.objectweb.celtix.bindings.soap2;

import org.objectweb.celtix.message.Message;
import org.objectweb.celtix.servicemodel.BindingInfo;
import org.objectweb.celtix.servicemodel.OperationInfo;

public final class ServiceModelUtil {
    
    private ServiceModelUtil() {        
    }
    
    public static OperationInfo getOperation(SoapMessage soapMessage, String opName) {
        BindingInfo service = (BindingInfo)soapMessage.get(Message.SERVICE_MODEL_BINDING);        
        return service.getOperation(opName);
    }

    
}
