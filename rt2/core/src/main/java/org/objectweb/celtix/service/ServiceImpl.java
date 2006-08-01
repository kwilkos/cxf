package org.objectweb.celtix.service;

import javax.xml.namespace.QName;

import org.objectweb.celtix.interceptors.AbstractAttributedInterceptorProvider;
import org.objectweb.celtix.service.model.ServiceInfo;

public class ServiceImpl extends AbstractAttributedInterceptorProvider implements Service {

    private ServiceInfo serviceInfo;
    
    public ServiceImpl(ServiceInfo si) {
        serviceInfo = si;
    }
    public QName getName() {
        return serviceInfo.getName();
    }

    public ServiceInfo getServiceInfo() {
        return serviceInfo;
    }
}
