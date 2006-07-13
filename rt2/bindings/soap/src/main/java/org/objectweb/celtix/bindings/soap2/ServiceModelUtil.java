package org.objectweb.celtix.bindings.soap2;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.namespace.QName;

import org.objectweb.celtix.message.Message;
import org.objectweb.celtix.servicemodel.BindingInfo;
import org.objectweb.celtix.servicemodel.MessagePartInfo;
import org.objectweb.celtix.servicemodel.OperationInfo;

public final class ServiceModelUtil {
    
    private ServiceModelUtil() {        
    }
    
    public static OperationInfo getOperation(SoapMessage soapMessage, String opName) {
        BindingInfo service = (BindingInfo)soapMessage.get(Message.SERVICE_MODEL_BINDING);        
        return service.getOperation(opName);
    }    
    
    public static Set<QName> getHeaderQNameInOperationParam(SoapMessage soapMessage) {
        Set<QName> headers = new HashSet<QName>();        
        BindingInfo service = (BindingInfo)soapMessage.get(Message.SERVICE_MODEL_BINDING);        
        for (OperationInfo opi : service.getOperations()) {
            List<MessagePartInfo> parts = opi.getInput().getMessageParts();
            for (MessagePartInfo mpi : parts) {
                if (mpi.isHeader()) {
                    headers.add(mpi.getName());
                }
            }
            parts = opi.getOutput().getMessageParts();
            for (MessagePartInfo mpi : parts) {
                if (mpi.isHeader()) {
                    headers.add(mpi.getName());
                }
            }            
        }
        return headers;
    }
}
