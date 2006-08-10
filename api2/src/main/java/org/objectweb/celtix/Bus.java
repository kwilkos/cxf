package org.objectweb.celtix;

import java.util.List;

import org.objectweb.celtix.configuration.Configuration;
import org.objectweb.celtix.interceptors.InterceptorProvider;
import org.objectweb.celtix.phase.Phase;

public interface Bus extends InterceptorProvider {
    
    <T> T getExtension(Class<T> extensionType);
    
    <T> void setExtension(T extension, Class<T> extensionType);

    List<Phase> getInPhases();
   
    List<Phase> getOutPhases();
    
    Configuration getConfiguration();

}
