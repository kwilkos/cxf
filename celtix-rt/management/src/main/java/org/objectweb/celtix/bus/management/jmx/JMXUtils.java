package org.objectweb.celtix.bus.management.jmx;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.management.ObjectName;

import org.objectweb.celtix.common.logging.LogUtils;


public final class JMXUtils {
    
    public static final String DOMAIN_STRING = "org.objectweb.celtix.instrumentation";
    
    private static final Logger LOG = LogUtils.getL7dLogger(JMXUtils.class);
    private JMXUtils() {        
    }
         
    /**
     * Bus :
           org.objectweb.celtix.instrumentation:type=Bus,name=celtix
       
       WorkQueue :
           org.objectweb.celtix.instrumentation:type=Bus.WorkQueue,Bus=celtix,name=WorkQueue
        
       WSDLManager :
           org.objectweb.celtix.instrumentation:type=Bus.WSDLManager,Bus=celtix,name=WSDLManager       
           
         
       Endpoint :
           org.objectweb.celtix.instrumentation:type=Bus.Endpoint,Bus=celtix,
           Bus.Service={http://objectweb.org/hello_world}SOAPService",Bus.Port=SoapPort, 
           name=Endpoint
        
       HTTPServerTransport:
           org.objectweb.celtix.instrumentation:type=Bus.Service.Port.HTTPServerTransport,
           Bus=celtix,Bus.Service={http://objectweb.org/hello_world}SOAPService",Bus.Port=SoapPort,
           name=HTTPServerTransport"
       
       JMSServerTransport:
           org.objectweb.celtix.instrumentation:type=Bus.Service.Port.JMSServerTransport,
           Bus=celtix,Bus.Service={http://objectweb.org/hello_world}SOAPService",Bus.Port=SoapPort,
           name=JMSServerTransport" 
       ...
           
     */
    // org.objectweb.celtix:type=Componnet,name=QuotedQName,bus=busIdentifier 
    public static ObjectName getObjectName(String type, String name, String busID) {        
        String objectName = "";
        if (type.compareTo("Bus") == 0) {
            objectName = ":type=" + type + ",name=" + busID;
        } else {
            objectName = ":type=" + type + ",Bus=" + busID + name;
        }
        try {
            return new ObjectName(DOMAIN_STRING + objectName);
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, "OBJECT_NAME_FALUE_MSG", new Object[]{name, ex});
        }
        return null;
    }

   

}
