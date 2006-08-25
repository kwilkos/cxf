package org.objectweb.celtix.service.model;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.xml.namespace.QName;

public class ServiceInfo extends AbstractPropertiesHolder {
    QName name;
    String targetNamespace;
    InterfaceInfo intf;
    TypeInfo typeInfo;
    Map<QName, BindingInfo> bindings = new ConcurrentHashMap<QName, BindingInfo>(2);
    Map<QName, EndpointInfo> endpoints = new ConcurrentHashMap<QName, EndpointInfo>(2);
    
    public ServiceInfo() {
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
    
    public BindingInfo getBinding(QName qn) {
        return bindings.get(qn);
    }
    public void addBinding(BindingInfo binding) {
        bindings.put(binding.getName(), binding);
    }
    public EndpointInfo getEndpoint(QName qn) {
        return endpoints.get(qn);
    }
    public void addEndpoint(EndpointInfo ep) {
        endpoints.put(ep.getName(), ep);
    }
    
    public Collection<EndpointInfo> getEndpoints() {
        return Collections.unmodifiableCollection(endpoints.values());
    }
    
    public Collection<BindingInfo> getBindings() {
        return Collections.unmodifiableCollection(bindings.values());
    }

    public TypeInfo getTypeInfo() {
        return typeInfo;
    }

    public void setTypeInfo(TypeInfo typeInfo) {
        this.typeInfo = typeInfo;
    }
}
