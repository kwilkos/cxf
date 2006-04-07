package org.objectweb.celtix.performance.basic_type.server;

import java.util.logging.Logger;
import org.objectweb.celtix.performance.basic_type.BasicPortType;
@javax.jws.WebService(name = "BasicPortType", serviceName = "SOAPService",                                                                                
                      targetNamespace = "http://objectweb.org/performance/basic_type",
                      wsdlLocation = "file:./wsdl/basic_type.wsdl")

public class ServerImpl implements BasicPortType {

    private static final Logger LOG = 
        Logger.getLogger(ServerImpl.class.getPackage().getName());
    
    public byte[] echoBase64(byte[] inputBase64) {
        //LOG.info("Executing operation echoBase64 ");
        //System.out.println("Executing operation echoBase64");
        //System.out.println("Message received: " + inputBase64 + "\n");
        return inputBase64;
    }
    
    public String echoString(String inputString) {
        //LOG.info("Executing operation echoString");
        //System.out.println("Executing operation echoString\n");
        //System.out.println("Message received: " + inputString + "\n");
        return inputString;
    }
}
    
