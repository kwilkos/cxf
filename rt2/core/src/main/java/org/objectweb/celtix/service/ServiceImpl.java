package org.objectweb.celtix.service;

import java.util.HashMap;
import java.util.List;

import javax.xml.namespace.QName;

import org.objectweb.celtix.interceptors.Interceptor;
import org.objectweb.celtix.service.model.ServiceInfo;

public class ServiceImpl extends HashMap<String, Object> implements Service {

    private ServiceInfo serviceInfo;
    private List<Interceptor> inInterceptors;
    private List<Interceptor> outInterceptors;
    private List<Interceptor> faultInterceptors;
    
    public ServiceImpl(ServiceInfo serviceInfo) {
        this.serviceInfo = serviceInfo;
    }
    
    public QName getName() {
        return serviceInfo.getName();
    }

    public ServiceInfo getServiceInfo() {
        return serviceInfo;
    }

    public List<Interceptor> getFaultInterceptors() {
        return faultInterceptors;
    }

    public void setFaultInterceptors(List<Interceptor> faultInterceptors) {
        this.faultInterceptors = faultInterceptors;
    }

    public List<Interceptor> getInInterceptors() {
        return inInterceptors;
    }

    public void setInInterceptors(List<Interceptor> inInterceptors) {
        this.inInterceptors = inInterceptors;
    }

    public List<Interceptor> getOutInterceptors() {
        return outInterceptors;
    }

    public void setOutInterceptors(List<Interceptor> outInterceptors) {
        this.outInterceptors = outInterceptors;
    }

}
