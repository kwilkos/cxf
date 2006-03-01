package org.objectweb.celtix.bus.management.jmx;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.management.ObjectName;

import org.objectweb.celtix.common.logging.LogUtils;


public final class JMXUtils {
    
    public static final String DOMAIN_STRING = "org.objectweb.celtix";
    
    private static final Logger LOG = LogUtils.getL7dLogger(JMXUtils.class);
    private JMXUtils() {        
    }
    
    //org.objectweb.celtix.instrumentation:type=Componnet,name=QuotedQName,bus=busIdentifier
    //TODO should get the busIdentifier
    public static ObjectName getObjectName(String name) {        
        try {
            return new ObjectName(DOMAIN_STRING + ":Type=" + name);
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, "OBJECT_NAME_FALUE_MSG", new Object[]{name, ex});
        }
        return null;
    }

   

}
