package org.objectweb.celtix.servicemodel;

import javax.xml.namespace.QName;



public class PortInfo extends AbstractPropertiesHolder {
    BindingInfo binding;
    ServiceInfo service;
    String name;
    
    public PortInfo(String q, ServiceInfo ser) {
        name = q;
        service = ser;
    }
    
    public ServiceInfo getService() {
        return service;
    }
    
    public String getName() {
        return name;
    }
    public void setName(String n) {
        name = n;
    }
    
    public BindingInfo createBinding(QName q) {
        binding = new BindingInfo(this, q);
        return binding;
    }
    
    public void setBinding(BindingInfo b) {
        binding = b;
    }
    
    public BindingInfo getBinding() {
        return binding;
    }
}
