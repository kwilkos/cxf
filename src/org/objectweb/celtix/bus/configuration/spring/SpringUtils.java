package org.objectweb.celtix.bus.configuration.spring;

import org.objectweb.celtix.configuration.ConfigurationItemMetadata;

public final class SpringUtils {
    
    /**
     * prevent instantiation
     *
     */
    private SpringUtils() {       
    }
    
    public static String getGetterName(ConfigurationItemMetadata definition) {
        StringBuffer buf = new StringBuffer();
        buf.append("get");
        buf.append(definition.getName());
        if (Character.isLowerCase(buf.charAt(3))) {
            buf.setCharAt(3, Character.toUpperCase(buf.charAt(3)));
        }
        return buf.toString();        
    }
    
    public static String getSetterName(ConfigurationItemMetadata definition) {
        StringBuffer buf = new StringBuffer();
        buf.append("set");
        buf.append(definition.getName());
        if (Character.isLowerCase(buf.charAt(3))) {
            buf.setCharAt(3, Character.toUpperCase(buf.charAt(3)));
        }
        return buf.toString();        
    }
    
    public static String getMemberName(ConfigurationItemMetadata definition) {
        return definition.getName();       
    }
}
