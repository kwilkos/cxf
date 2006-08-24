package org.apache.cxf.service;

import javax.xml.namespace.QName;

public interface ServiceManager {
    
    void register(Service service);
    
    void unregister(Service service);
    
    Service getService(QName name);
    
}
