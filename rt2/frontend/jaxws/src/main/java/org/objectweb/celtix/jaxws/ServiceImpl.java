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
import org.objectweb.celtix.wsdl11.WSDLServiceFactory;

public class ServiceImpl extends ServiceDelegate {
    
    private Bus bus;
    private URL wsdlURL;
    
    private Service service;
    
    public ServiceImpl(Bus b, URL url, QName name, Class cls) {
        bus = b;
        wsdlURL = url;
        
        WSDLServiceFactory sf = new WSDLServiceFactory(bus, url, name);
        service = sf.create(); 
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
    
    protected <T> T createPort(QName portName, Class<T> serviceEndpointInterface) {

        /*
        LOG.log(Level.FINE, "creating port for portName", portName);
        LOG.log(Level.FINE, "endpoint interface:", serviceEndpointInterface);

        //Assuming Annotation is Present
        javax.jws.WebService wsAnnotation = serviceEndpointInterface.getAnnotation(WebService.class);

        if (wsdlLocation == null) {
            wsdlLocation = getWsdlLocation(wsAnnotation);
        }

        if (wsdlLocation == null) {
            throw new WebServiceException("No wsdl url specified");
        }

        if (serviceName == null) {
            serviceName = getServiceName(wsAnnotation);
        }
        
        if (portName == null) {
            portName = getPortName(wsAnnotation);
            if (portName == null) {
                try {
                    Definition def = bus.getWSDLManager().getDefinition(wsdlLocation);
                    javax.wsdl.Service service = def.getService(serviceName);
                    if (service.getPorts().size() == 1) {
                        Port port = (Port)service.getPorts().values().iterator().next();
                        portName = new QName(serviceName.getNamespaceURI(), port.getName());
                    } else {
                        throw new WebServiceException("Unable to determine portName");                      
                    }
                } catch (WSDLException e) {
                    e.printStackTrace();
                }
            }
        }

        EndpointReferenceType ref = EndpointReferenceUtils.getEndpointReference(wsdlLocation,
                serviceName, portName.getLocalPart());

        Configuration portConfiguration = createPortConfiguration(portName, ref);

        EndpointInvocationHandler endpointHandler =
                new EndpointInvocationHandler(bus, ref, this, portConfiguration, serviceEndpointInterface);

        createHandlerChainForBinding(serviceEndpointInterface, portName, endpointHandler.getBinding());

        Object obj = Proxy.newProxyInstance(serviceEndpointInterface.getClassLoader(),
                                            new Class[] {serviceEndpointInterface, BindingProvider.class},
                                            endpointHandler);

        LOG.log(Level.FINE, "created proxy", obj);

        endpointList.add(portName);

        return serviceEndpointInterface.cast(obj);
        */
        
        return null;
    }

}
