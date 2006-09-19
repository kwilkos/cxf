package org.apache.cxf.performance.complex_type.server;

import java.util.logging.Logger;

import org.apache.cxf.performance.complex_type.ComplexPortType;
import org.apache.cxf.performance.complex_type.types.NestedComplexTypeSeq;

@javax.jws.WebService(portName = "SoapPort", serviceName = "ComplexService",                                                                                
                      targetNamespace = "http://cxf.apache.org/performance/complex_type",
                      endpointInterface = "org.apache.cxf.performance.complex_type.ComplexPortType")

public class ServerImpl implements ComplexPortType {

    private static final Logger LOG = 
        Logger.getLogger(ServerImpl.class.getPackage().getName());
    
    public NestedComplexTypeSeq sendReceiveData(NestedComplexTypeSeq request) {
        //LOG.info("Executing operation sendReceiveData opt");
        //System.out.println("Executing operation sendReceiveData\n");
        //System.out.println("Message received: " + request + "\n");
        return request;
    }
}
    
