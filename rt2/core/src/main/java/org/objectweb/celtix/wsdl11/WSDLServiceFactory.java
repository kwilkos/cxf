package org.objectweb.celtix.wsdl11;

import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.wsdl.Definition;
import javax.wsdl.WSDLException;
import javax.xml.namespace.QName;

import org.objectweb.celtix.Bus;
import org.objectweb.celtix.common.logging.LogUtils;
import org.objectweb.celtix.service.Service;
import org.objectweb.celtix.service.ServiceImpl;
import org.objectweb.celtix.service.factory.AbstractServiceFactoryBean;
import org.objectweb.celtix.service.model.ServiceInfo;
import org.objectweb.celtix.wsdl.WSDLManager;

public class WSDLServiceFactory extends AbstractServiceFactoryBean {
    
    private static final Logger LOG = LogUtils.getL7dLogger(WSDLServiceFactory.class);
    
    private URL wsdlUrl;
    private QName serviceName;
        
    public WSDLServiceFactory(Bus b, URL url, QName sn) {
        setBus(b);
        wsdlUrl = url;
        serviceName = sn;        
    }
    
    public Service create() {
        // use wsdl manager to parse wsdl or get cached definition
        
        Definition definition = null;
        try {
            definition = getBus().getExtension(WSDLManager.class).getDefinition(wsdlUrl);
        } catch (WSDLException ex) {
            LOG.log(Level.SEVERE, "SERVICE_CREATION_MSG", ex);
        }
        
        javax.wsdl.Service wsdlService = definition.getService(serviceName);
        ServiceInfo si = new WSDLServiceBuilder(getBus()).buildService(definition, wsdlService);
        ServiceImpl service = new ServiceImpl(si);
        
        return service;
    }
    
}
