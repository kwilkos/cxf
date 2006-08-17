package org.objectweb.celtix.jaxws.support;

import java.io.IOException;
import java.lang.reflect.Method;

import javax.wsdl.WSDLException;

import org.objectweb.celtix.BusException;
import org.objectweb.celtix.endpoint.ServerImpl;
import org.objectweb.celtix.interceptors.WrappedInInterceptor;
import org.objectweb.celtix.jaxb.JAXBDataReaderFactory;
import org.objectweb.celtix.jaxb.JAXBDataWriterFactory;
import org.objectweb.celtix.messaging.ChainInitiationObserver;
import org.objectweb.celtix.service.Service;
import org.objectweb.celtix.service.factory.ReflectionServiceFactoryBean;
import org.objectweb.celtix.service.model.EndpointInfo;
import org.objectweb.celtix.service.model.InterfaceInfo;
import org.objectweb.celtix.service.model.OperationInfo;

public class JaxWsServiceFactoryBean extends ReflectionServiceFactoryBean {

    public JaxWsServiceFactoryBean() {
        super();

        getServiceConfigurations().add(new JaxWsServiceConfiguration());
        setDataReaderFactory(JAXBDataReaderFactory.getInstance());
        setDataWriterFactory(JAXBDataWriterFactory.getInstance());
    }

    public void activateEndpoints() throws IOException, WSDLException, BusException {
        Service service = getService();
        
        for (EndpointInfo ei : service.getServiceInfo().getEndpoints()) {
            activateEndpoint(service, ei);
        }
    }

    public void activateEndpoint(Service service, EndpointInfo ei) 
        throws BusException, WSDLException, IOException {
        JaxwsEndpointImpl ep = new JaxwsEndpointImpl(getBus(), service, ei);
        ChainInitiationObserver observer = new ChainInitiationObserver(ep, getBus());
        
        ServerImpl server = new ServerImpl(getBus(), ep, observer);
        
        server.start();
    }

    @Override
    protected void initializeWSDLOperation(InterfaceInfo intf, OperationInfo o, Method selected) {
        super.initializeWSDLOperation(intf, o, selected);

        // TODO: Check for request/responsewrapper annotations
        o.setProperty(WrappedInInterceptor.SINGLE_WRAPPED_PART, Boolean.TRUE);
    }

    
    
}
