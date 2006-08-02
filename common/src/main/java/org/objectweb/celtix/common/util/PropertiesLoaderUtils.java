package org.objectweb.celtix.common.util;

import java.io.*;
import java.net.URL;
import java.util.*;

import org.springframework.core.io.UrlResource;

/**
 * Replace by org.springframework.core.io.support.PropertiesLoaderUtils
 * when moving to Spring 2.0.
 *
 */
    
public final class PropertiesLoaderUtils {
    
    /**
     * Prevents instantiation.
     */
    private PropertiesLoaderUtils() {        
    }

    /**
     * Load all properties from the given class path resource, using the given
     * class loader.
     * <p>
     * Merges properties if more than one resource of the same name found in the
     * class path.
     * 
     * @param resourceName the name of the class path resource
     * @param classLoader the ClassLoader to use for loading (or
     *            <code>null</code> to use the default class loader)
     * @return the populated Properties instance
     * @throws IOException if loading failed
     */
    public static Properties loadAllProperties(String resourceName, ClassLoader classLoader)
        throws IOException {

        
        Properties properties = new Properties();
        Enumeration urls = classLoader.getResources(resourceName);

        while (urls.hasMoreElements()) {
            URL url = (URL)urls.nextElement();
            // TODO: May need a log here, instead of the system.out
            InputStream is = null;
            try {
                UrlResource ur = new UrlResource(url);
                is = ur.getInputStream();
                properties.loadFromXML(new BufferedInputStream(is));
            } finally {
                if (is != null) {
                    is.close();
                }
            }
        }
        return properties;
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
