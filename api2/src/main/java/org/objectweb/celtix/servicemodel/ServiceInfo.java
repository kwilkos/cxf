package org.objectweb.celtix.servicemodel;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.xml.namespace.QName;

import org.objectweb.celtix.Bus;
import org.objectweb.celtix.BusException;
import org.objectweb.celtix.bindings.BindingFactory;


public class ServiceInfo extends AbstractPropertiesHolder {
    Bus bus;
    QName name;
    String targetNamespace;
    InterfaceInfo intf;
    Map<QName, BindingInfo> bindings = new ConcurrentHashMap<QName, BindingInfo>(2);
    Map<String, EndpointInfo> endpoints = new ConcurrentHashMap<String, EndpointInfo>(2);
    
    public ServiceInfo(Bus b) {
        bus = b;
    }
    
    public String getTargetNamespace() {
        return targetNamespace;
    }
    public void setTargetNamespace(String ns) {
        targetNamespace = ns;
    }
    
    public void setName(QName n) {
        name = n;
    }
    public QName getName() {
        return name;
    }
    
    public InterfaceInfo createInterface(QName qn) {
        intf = new InterfaceInfo(this, qn);
        return intf;
    }
    public void setInterface(InterfaceInfo inf) {
        intf = inf;
    }
    public InterfaceInfo getInterface() {
        return intf;
    }
    
    public BindingInfo createBinding(QName qn, String ns) {
        BindingInfo bi = null;
        try {
            BindingFactory factory = bus.getBindingManager().getBindingFactory(ns);
            if (factory != null) {
                bi = factory.createBindingInfo(this);
            }
        } catch (BusException e) {
            //ignore, we'll use a generic BindingInfo
        }
        if (bi == null) {
            bi = new BindingInfo(this);
        }
        bi.setName(qn);
        addBinding(bi);
        return bi;
    }
    public BindingInfo getBinding(QName qn) {
        return bindings.get(qn);
    }
    public void addBinding(BindingInfo binding) {
        bindings.put(binding.getName(), binding);
    }
    public EndpointInfo getEndpoint(String qn) {
        return endpoints.get(qn);
    }
    public void addEndpoint(EndpointInfo ep) {
        endpoints.put(ep.getName(), ep);
    }

    

}
