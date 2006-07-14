package org.objectweb.celtix.service;

import java.net.URL;
import java.util.Map;

import javax.xml.namespace.QName;

public interface ServiceFactory {
    
    Service create(Class clazz);

    Service create(Class clazz, Map properties);

    Service create(Class clazz,
                          String name,
                          String namespace,
                          Map properties);
    
    Service create(Class clazz, QName service, URL wsdlUrl, Map properties);
    
}
