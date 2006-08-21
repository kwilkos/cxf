package org.objectweb.celtix.jaxws.support;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.jws.WebService;
import javax.wsdl.WSDLException;
import javax.xml.bind.JAXBException;
import javax.xml.ws.WebServiceException;

import org.objectweb.celtix.BusException;
import org.objectweb.celtix.common.classloader.ClassLoaderUtils;
import org.objectweb.celtix.common.i18n.Message;
import org.objectweb.celtix.common.logging.LogUtils;
import org.objectweb.celtix.endpoint.ServerImpl;
import org.objectweb.celtix.interceptors.WrappedInInterceptor;
import org.objectweb.celtix.jaxb.JAXBDataBinding;
import org.objectweb.celtix.jaxws.interceptors.WrapperClassOutInterceptor;
import org.objectweb.celtix.messaging.ChainInitiationObserver;
import org.objectweb.celtix.service.Service;
import org.objectweb.celtix.service.factory.ReflectionServiceFactoryBean;
import org.objectweb.celtix.service.model.EndpointInfo;
import org.objectweb.celtix.service.model.InterfaceInfo;
import org.objectweb.celtix.service.model.OperationInfo;

public class JaxWsServiceFactoryBean extends ReflectionServiceFactoryBean {

    private static final Logger LOG = LogUtils.getL7dLogger(JaxWsServiceFactoryBean.class);
    private static final ResourceBundle BUNDLE = LOG.getResourceBundle();
    
    Class<?> seiClass;
    JAXBDataBinding dataBinding;
    
    public JaxWsServiceFactoryBean() {
        super();

        getServiceConfigurations().add(new JaxWsServiceConfiguration());
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
        Class responseWrapper = getResponseWrapper(selected);
        if (responseWrapper != null) {
            o.setProperty(WrapperClassOutInterceptor.SINGLE_WRAPPED_PART, responseWrapper);
        }
        Class<?> requestWrapper = getRequestWrapper(selected);
        if (requestWrapper != null) {
            o.setProperty(WrappedInInterceptor.SINGLE_WRAPPED_PART, Boolean.TRUE);
        }
    }

    @Override
    public void setServiceClass(Class<?> serviceClass) {
        super.setServiceClass(serviceClass);
        
        try {
            dataBinding = new JAXBDataBinding(serviceClass);
        } catch (JAXBException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        setDataReaderFactory(dataBinding.getDataReaderFactory());
        setDataWriterFactory(dataBinding.getDataWriterFactory());
        
        // update wsdl location
        
        // TODO: replace version in EndpointreferenceUtils?
        
        WebService ws = serviceClass.getAnnotation(WebService.class);
        if (null == ws) {
            // endpoint must be a provider type endpoint 
            // TODO: get wsdl location from WwebServiceProvider annotation
            // return;
        }
        
        String sei = ws.endpointInterface();
        if (null != sei && !"".equals(sei)) {
            try {
                seiClass = ClassLoaderUtils.loadClass(sei, serviceClass);
            } catch (ClassNotFoundException ex) {
                throw new WebServiceException(BUNDLE.getString("SEI_LOAD_FAILURE_MSG"), ex);
            }
            ws = seiClass.getAnnotation(WebService.class);
            if (null == ws) {
                throw new WebServiceException(BUNDLE.getString("SEI_WITHOUT_WEBSERVICE_ANNOTATION_EXC"));
            }
        }
        if (null != ws.wsdlLocation() && !"".equals(ws.wsdlLocation())) {
            
            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine("Setting wsdl location to:  " + ws.wsdlLocation());
            }
            try {
                setWsdlURL(new URL(ws.wsdlLocation()));
            } catch (MalformedURLException ex) {
                Message msg = new Message("MALFORMED_URL_IN_WEBSERVICE_ANNOTATION_EXC", 
                                          BUNDLE, ws.wsdlLocation());
                throw new WebServiceException(msg.toString(), ex);                
            }
        }
    }  
    
}
