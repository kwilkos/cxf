package org.objectweb.celtix.configuration.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Properties;

public final class PropertiesUtils {
    
    /**
     * Prevents instantiation.
     */
    private PropertiesUtils() {        
    }
    
    /**
     * Retrieves the names of all properties that bind to the specified value.
     * 
     * @param properties the properties to search
     * @param value the property value 
     * @return the list of property names
     */
    public static Collection<String> getPropertyNames(Properties properties, String value) {
        Collection<String> names = new ArrayList<String>();
        Enumeration e = properties.propertyNames();
        while (e.hasMoreElements()) {
            String name = (String)e.nextElement();
            if (value.equals(properties.getProperty(name))) {
                names.add(name);
            }            
        }
        return names;
    }

}
