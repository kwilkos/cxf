package org.objectweb.celtix.wsdl11;

import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.wsdl.Definition;
import javax.wsdl.WSDLException;
import javax.xml.namespace.QName;

import org.objectweb.celtix.Bus;
import org.objectweb.celtix.common.logging.LogUtils;
import org.objectweb.celtix.service.AbstractServiceFactoryBean;
import org.objectweb.celtix.service.Service;
import org.objectweb.celtix.service.ServiceImpl;
import org.objectweb.celtix.service.model.ServiceInfo;

public class WSDLServiceFactory extends AbstractServiceFactoryBean {
    
    private static final Logger LOG = LogUtils.getL7dLogger(WSDLServiceFactory.class);
    
    private Bus bus;
    private URL wsdlUrl;
    private QName serviceName;
    
    
    public WSDLServiceFactory(Bus b, URL url, QName sn) {
        bus = b;
        wsdlUrl = url;
        serviceName = sn;        
    }
    
    public Service create() {
        // use wsdl manager to parse wsdl or get cached definition
        
        Definition definition = null;
        try {
            definition = bus.getWSDL11Manager().getDefinition(wsdlUrl);
        } catch (WSDLException ex) {
            LOG.log(Level.SEVERE, "SERVICE_CREATION_MSG", ex);
        }
        
        javax.wsdl.Service wsdlService = definition.getService(serviceName);
        ServiceInfo si = new WSDLServiceBuilder(bus).buildService(definition, wsdlService);
        return new ServiceImpl(serviceName, si);     
    }
    
}
