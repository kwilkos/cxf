package org.objectweb.celtix.service.model;

import javax.wsdl.extensions.soap.SOAPAddress;
import javax.xml.namespace.QName;

import org.xmlsoap.schemas.wsdl.http.AddressType;

public class EndpointInfo extends AbstractPropertiesHolder {
    final String endpointType;
    ServiceInfo service;
    BindingInfo binding;
    QName name;
    String address;
    
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
    
    public String getAddress() {
        if (null != address) {
            return address;
        }
        SOAPAddress sa = getExtensor(SOAPAddress.class);
        if (null != sa) {
            return sa.getLocationURI();
        }
        AddressType a = getExtensor(AddressType.class);
        if (null != a) {
            return a.getLocation();
        }
        return null;
    }
    public void setAddress(String a) {
        address = a;
    }
}
