package org.objectweb.celtix.extension;

public interface ExtensionManager {
    
    <T> T getExtension(Class<T> extensionType);
    
    void activateViaNS(String namespace);
}
