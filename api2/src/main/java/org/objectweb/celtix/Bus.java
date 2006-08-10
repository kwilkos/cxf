package org.objectweb.celtix;

import org.objectweb.celtix.configuration.Configuration;
import org.objectweb.celtix.interceptors.InterceptorProvider;

public interface Bus extends InterceptorProvider {
    
    <T> T getExtension(Class<T> extensionType);
    
    <T> void setExtension(T extension, Class<T> extensionType);

    Configuration getConfiguration();

}
