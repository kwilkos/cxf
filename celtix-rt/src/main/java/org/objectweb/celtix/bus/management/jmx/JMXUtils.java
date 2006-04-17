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
           org.objectweb.celtix.instrumentation:type=Bus,name=demos.jmx_runtime
       Service :
           org.objectweb.celtix.instrumentation:type=Bus.Service,Bus=demos.jmx_runtime
           name="{http://ws.celtix.objectweb.org}SOAPService"
        
       Port :
           org.objectweb.celtix.instrumentation:type=Bus.Service.Port,Bus=demos.jmx_runtime,
           name=SoapPort,Bus.Service="{http://ws.celtix.objectweb.org}SOAPService",
           
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
