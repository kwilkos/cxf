package org.apache.cxf;

import org.apache.cxf.configuration.Configuration;
import org.apache.cxf.interceptors.InterceptorProvider;

public interface Bus extends InterceptorProvider {
    
    <T> T getExtension(Class<T> extensionType);
    
    <T> void setExtension(T extension, Class<T> extensionType);

    Configuration getConfiguration();
    
    String getId();
    
    void shutdown(boolean wait);
    
    void run();

}
