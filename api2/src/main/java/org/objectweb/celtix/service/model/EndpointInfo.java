package org.objectweb.celtix.service.model;

public class EndpointInfo extends AbstractPropertiesHolder {
    final String namespaceURI;
    ServiceInfo service;
    BindingInfo binding;
    String name;
    
    public EndpointInfo(ServiceInfo serv, String ns) {
        namespaceURI = ns;
        service = serv;
    }
    public String getNamespaceURI() {
        return namespaceURI;
    }    
    public InterfaceInfo getInterface() {
        return service.getInterface();
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

    public BindingInfo getBinding() {
        return binding;
    }
    public void setBinding(BindingInfo b) {
        binding = b;
    }
}
