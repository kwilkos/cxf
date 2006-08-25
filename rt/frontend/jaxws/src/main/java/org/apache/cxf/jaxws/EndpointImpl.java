package org.apache.cxf.jaxws;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.concurrent.Executor;
import java.util.logging.Logger;

import javax.xml.namespace.QName;
import javax.xml.transform.Source;
import javax.xml.ws.Binding;
import javax.xml.ws.WebServiceException;

import org.apache.cxf.Bus;
import org.apache.cxf.BusException;
import org.apache.cxf.common.logging.LogUtils;
import org.apache.cxf.endpoint.ServerImpl;
import org.apache.cxf.jaxws.support.JaxWsServiceFactoryBean;
import org.apache.cxf.jaxws.support.JaxwsEndpointImpl;
import org.apache.cxf.jaxws.support.JaxwsImplementorInfo;
import org.apache.cxf.service.Service;
import org.apache.cxf.service.invoker.SimpleMethodInvoker;
import org.apache.cxf.service.model.EndpointInfo;
import org.apache.cxf.transport.ChainInitiationObserver;

public class EndpointImpl extends javax.xml.ws.Endpoint {
    
    private static final Logger LOG = LogUtils.getL7dLogger(JaxWsServiceFactoryBean.class);
    private static final ResourceBundle BUNDLE = LOG.getResourceBundle();
    
    private Bus bus;
    // private String bindingURI;
    private Object implementor;
    private ServerImpl server;
    private Service service;
    private JaxwsEndpointImpl endpoint;
    
    public EndpointImpl(Bus b, Object i, String uri) {
        bus = b;
        implementor = i;
        // bindingURI = uri;
        
        // build up the Service model
        JaxWsServiceFactoryBean serviceFactory = new JaxWsServiceFactoryBean();
        serviceFactory.setServiceClass(implementor.getClass());
        serviceFactory.setBus(bus);
        service = serviceFactory.create();
        
        // create the endpoint
        JaxwsImplementorInfo implInfo = new JaxwsImplementorInfo(implementor.getClass());
        QName endpointName = implInfo.getEndpointName();
        EndpointInfo ei = service.getServiceInfo().getEndpoint(endpointName);
        //TODO - need a jaxws specific invoker for holders and such
        service.setInvoker(new SimpleMethodInvoker(i));
        // TODO: use bindigURI
        endpoint = new JaxwsEndpointImpl(bus, service, ei);                
    }
    
    
    public Binding getBinding() {
        return endpoint.getJaxwsBinding();
    }

    public void setExecutor(Executor executor) {
        server.getEndpoint().getService().setExecutor(executor);        
    }
    
    public Executor getExecutor() {
        return server.getEndpoint().getService().getExecutor();
    }

    @Override
    public Object getImplementor() {
        return implementor;
    }

    @Override
    public List<Source> getMetadata() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Map<String, Object> getProperties() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean isPublished() {
        return server != null;
    }

    @Override
    public void publish(Object arg0) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void publish(String address) {
        doPublish(address);
    }

    public void setMetadata(List<Source> arg0) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setProperties(Map<String, Object> arg0) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void stop() {
        if (null != server) {
            server.stop();
        }
    }
    
    public ServerImpl getServer() {
        return server;
    } 
    
    protected void doPublish(String address) {
        if (null != address) {
            endpoint.getEndpointInfo().setAddress(address);
        }
        
        try {
            ChainInitiationObserver observer = new ChainInitiationObserver(endpoint, bus); 
            server = new ServerImpl(bus, endpoint, observer);
            server.start();
        } catch (BusException ex) {
            throw new WebServiceException(BUNDLE.getString("FAILED_TO_PUBLISH_ENDPOINT_EXC"), ex);
        } catch (IOException ex) {
            throw new WebServiceException(BUNDLE.getString("FAILED_TO_PUBLISH_ENDPOINT_EXC"), ex);
        }
        
    }
}
