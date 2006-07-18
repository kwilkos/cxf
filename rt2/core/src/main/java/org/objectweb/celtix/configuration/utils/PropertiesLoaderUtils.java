package org.objectweb.celtix.configuration.utils;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.Properties;

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
            System.out.println("processing resource: " + url);
            InputStream is = null;
            try {
                UrlResource ur = new UrlResource(url);
                is = ur.getInputStream();
                properties.load(is);
            } finally {
                if (is != null) {
                    is.close();
                }
            }
        }
        return properties;
    }

}
