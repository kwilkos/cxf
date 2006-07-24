package org.objectweb.celtix.service;

import javax.xml.namespace.QName;

import org.objectweb.celtix.interceptors.AbstractAttributedInterceptorProvider;
import org.objectweb.celtix.service.model.ServiceInfo;

public class ServiceImpl extends AbstractAttributedInterceptorProvider implements Service {

    private QName serviceName;
    private ServiceInfo serviceInfo;
    
    public ServiceImpl(QName sn, ServiceInfo si) {
        serviceName = sn;
        serviceInfo = si;
    }
    public QName getName() {
        return serviceName;
    }

    public ServiceInfo getServiceInfo() {
        return serviceInfo;
    }
}
