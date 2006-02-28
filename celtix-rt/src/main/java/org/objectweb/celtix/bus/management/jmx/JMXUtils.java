package org.objectweb.celtix.bus.management.jmx;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;


public final class JMXUtils {
    public static final String DOMAIN_STRING = "org.objectweb.celtix";
    
    private JMXUtils() {        
    }
    
    //org.objectweb.celtix.instrumentation:type=Componnet,name=QuotedQName,bus=busIdentifier
    
    public static ObjectName getObjectName(String name) {        
        try {
            return new ObjectName(DOMAIN_STRING + ":Type=" + name);
        } catch (MalformedObjectNameException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (NullPointerException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

   

}
