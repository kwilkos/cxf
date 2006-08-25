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
import org.objectweb.celtix.ws.addressing.EndpointReferenceType;
import org.objectweb.celtix.ws.rm.CreateSequenceResponseType;
import org.objectweb.celtix.wsdl.EndpointReferenceUtils;

public class CreateSequenceResponse extends Request {
    
    private static final String METHOD_NAME = "createSequenceResponse";    
    private static final String OPERATION_NAME = "CreateSequenceResponse";
    
    public CreateSequenceResponse(AbstractBindingBase b, 
                                  Transport t,
                                  AddressingProperties inMAPs,
                                  CreateSequenceResponseType csr) {
        
        super(b, t, b.createObjectContext());
        
        EndpointReferenceType to = inMAPs.getReplyTo();
        if (to != null) {
            ContextUtils.storeTo(to, getObjectMessageContext());
            EndpointReferenceType replyTo =
                EndpointReferenceUtils.getEndpointReference(Names.WSA_ANONYMOUS_ADDRESS);
            ContextUtils.storeReplyTo(replyTo,
                                      getObjectMessageContext());
        }
        
        ContextUtils.storeUsingAddressing(true, getObjectMessageContext());

        getObjectMessageContext().setRequestorRole(true);
        
        AddressingProperties maps = new AddressingPropertiesImpl();
        AttributedURIType actionURI = ContextUtils.WSA_OBJECT_FACTORY.createAttributedURIType();
        actionURI.setValue(RMUtils.getRMConstants().getCreateSequenceResponseAction());
        maps.setAction(actionURI);
        maps.setRelatesTo(ContextUtils.getRelatesTo(inMAPs.getMessageID().getValue()));
        ContextUtils.storeMAPs(maps, getObjectMessageContext(), true, true, true, true);
        
        setMessageParameters(csr);
        
        setOneway(true);
    }
    
    public static Method getMethod() {
        Method method  = null;
        try {
            method = OutOfBandProtocolMessages.class.getMethod(
                METHOD_NAME, 
                new Class[] {CreateSequenceResponseType.class});
        } catch (NoSuchMethodException ex) {
            ex.printStackTrace();
        }
        return method;
    }

    public static String getOperationName() {
        return OPERATION_NAME;
    }
        
    public static DataBindingCallback createDataBindingCallback() {
        Method method = getMethod();
        return new JAXBDataBindingCallback(method, DataBindingCallback.Mode.PARTS, null);
    }
    
    private void setMessageParameters(CreateSequenceResponseType csr) {
        getObjectMessageContext().setMessageObjects(new Object[] {csr});
    }
}
