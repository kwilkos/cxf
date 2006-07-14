package org.objectweb.celtix.jaxws;

import java.net.URL;
import java.util.Iterator;
import java.util.concurrent.Executor;

import javax.xml.bind.JAXBContext;
import javax.xml.namespace.QName;
import javax.xml.ws.Dispatch;
import javax.xml.ws.Service.Mode;
import javax.xml.ws.handler.HandlerResolver;
import javax.xml.ws.spi.ServiceDelegate;

import org.objectweb.celtix.Bus;
import org.objectweb.celtix.service.Service;

public class ServiceImpl extends ServiceDelegate {
    
    private Bus bus;
    private URL wsdlURL;
    
    private Service service;
    
    public ServiceImpl(Bus b, URL url, QName name, Class cls) {
        bus = b;
        wsdlURL = url;
        
        // parse wsdl
        
        // create service info
        
        // create service
 
    }

    @Override
    public void addPort(QName arg0, String arg1, String arg2) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public <T> Dispatch<T> createDispatch(QName arg0, Class<T> arg1, Mode arg2) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Dispatch<Object> createDispatch(QName arg0, JAXBContext arg1, Mode arg2) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Executor getExecutor() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public HandlerResolver getHandlerResolver() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public <T> T getPort(Class<T> arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public <T> T getPort(QName arg0, Class<T> arg1) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Iterator<QName> getPorts() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public QName getServiceName() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public URL getWSDLDocumentLocation() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setExecutor(Executor arg0) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setHandlerResolver(HandlerResolver arg0) {
        // TODO Auto-generated method stub
        
    }
    
    public Bus getBus() {
        return bus;
    }

    public URL getWsdlURL() {
        return wsdlURL;
    }

    public Service getService() {
        return service;
    }
}
