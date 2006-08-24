package org.objectweb.celtix.extension;

import java.util.Collection;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

public class MyService {
    @Resource
    Collection<String> activationNamespaces;
    
    @Resource
    ExtensionManagerTest extensionManagerTest;
    
    
    public MyService() {
    }
    
    public Collection<String> getActivationNamespaces() {
        return activationNamespaces;
    }
    
    @PostConstruct
    void registerMyselfAsExtension() {
        extensionManagerTest.setMyService(this);
    }
}
