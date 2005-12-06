package org.objectweb.celtix.performance.complex_type.server;

import java.util.logging.Logger;
import org.objectweb.celtix.performance.complex_type.ComplexPortType;
import org.objectweb.celtix.performance.complex_type.types.NestedComplexTypeSeq;


public class ServerImpl implements ComplexPortType {

    private static final Logger LOG = 
        Logger.getLogger(ServerImpl.class.getPackage().getName());
    
    public NestedComplexTypeSeq sendReceiveData(NestedComplexTypeSeq request) {
        LOG.info("Executing operation sendReceiveData opt");
        System.out.println("Executing operation sendReceiveData\n");
        System.out.println("Message received: " + request + "\n");
        return request;
    }
}
    
