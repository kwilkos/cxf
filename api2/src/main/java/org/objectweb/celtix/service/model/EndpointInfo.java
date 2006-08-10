package org.objectweb.celtix.service.model;

import javax.xml.namespace.QName;

public class EndpointInfo extends AbstractPropertiesHolder {
    final String endpointType;
    ServiceInfo service;
    BindingInfo binding;
    QName name;
    
    public EndpointInfo(ServiceInfo serv, String ns) {
        endpointType = ns;
        service = serv;
    }
    public String getTransportId() {
        return endpointType;
    }    
    public InterfaceInfo getInterface() {
        return service.getInterface();
    }
    public ServiceInfo getService() {
        return service;
    }
    
    public QName getName() {
        return name;
    }
    public void setName(QName n) {
        name = n;
    }

    public BindingInfo getBinding() {
        return binding;
    }
    public void setBinding(BindingInfo b) {
        binding = b;
    }
}
