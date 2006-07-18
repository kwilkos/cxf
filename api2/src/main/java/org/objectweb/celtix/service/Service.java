package org.objectweb.celtix.service;

import java.util.List;

import javax.xml.namespace.QName;

import org.objectweb.celtix.client.Client;
import org.objectweb.celtix.client.ClientFactory;
import org.objectweb.celtix.endpoint.Endpoint;
import org.objectweb.celtix.endpoint.EndpointFactory;
import org.objectweb.celtix.servicemodel.ServiceInfo;

public interface Service {
    
    QName getName();
    
    ServiceInfo getServiceInfo();
    
    List<Endpoint> getEndpoints();
    
    List<Client> getClients();
    
    ClientFactory getClientFactory();
    
    EndpointFactory getEndpointFactory();
    
    
    
}
