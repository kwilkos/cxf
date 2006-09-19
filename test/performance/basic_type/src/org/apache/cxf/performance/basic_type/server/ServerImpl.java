package org.apache.cxf.performance.basic_type.server;

import java.util.logging.Logger;

import org.apache.cxf.performance.basic_type.BasicPortType;
@javax.jws.WebService(portName = "SoapHttpPort", serviceName = "BasicService",                                                                                
                      targetNamespace = "http://cxf.apache.org/performance/basic_type",
                      endpointInterface = "org.apache.cxf.performance.basic_type.BasicPortType")

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
    
