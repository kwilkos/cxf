package org.objectweb.celtix.wsdl11;

import java.net.URL;

import javax.wsdl.Definition;
import javax.xml.namespace.QName;

import org.objectweb.celtix.Bus;
import org.objectweb.celtix.service.AbstractServiceFactoryBean;
import org.objectweb.celtix.service.Service;
import org.objectweb.celtix.service.ServiceImpl;
import org.objectweb.celtix.service.model.ServiceInfo;

public class WSDLServiceFactory extends AbstractServiceFactoryBean {
    
    private Bus bus;
    // private URL wsdlUrl;
    private QName serviceName;
    
    
    public WSDLServiceFactory(Bus b, URL url, QName sn) {
        bus = b;
        // wsdlUrl = url;
        serviceName = sn;        
    }
    
    public Service create() {
        // use wsdl manager to parse wsdl or get cached definition
        Definition definition = null;
        
        javax.wsdl.Service wsdlService = definition.getService(serviceName);
        ServiceInfo si = new WSDLServiceBuilder(bus).buildService(definition, wsdlService);
        return new ServiceImpl(serviceName, si);     
    }
    
}
