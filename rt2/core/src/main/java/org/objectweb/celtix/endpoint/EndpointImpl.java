package org.objectweb.celtix.endpoint;

import java.util.concurrent.Executor;

import org.objectweb.celtix.bindings.Binding;
import org.objectweb.celtix.interceptors.AbstractBasicInterceptorProvider;
import org.objectweb.celtix.service.model.EndpointInfo;

public class EndpointImpl extends AbstractBasicInterceptorProvider implements Endpoint {
    
//    private static final Logger LOG = LogUtils.getL7dLogger(EndpointImpl.class);
//    private static final ResourceBundle BUNDLE = LOG.getResourceBundle();

    private Object implementor;
    private Binding binding;
    private Executor executor;
    private EndpointInfo endpointInfo;
    
    
    public EndpointImpl(Binding binding, EndpointInfo info) {
        this.binding = binding;
        endpointInfo = info;      
    }
    
    public EndpointInfo getEndpointInfo() {
        return endpointInfo;
    }
    
    public Binding getBinding() {    
        return binding;
    }
    
    public Object getImplementor() {
        return implementor;
    }
    
    public void  setImplementor(Object i) {
        implementor = i;
    }

    public Executor getExecutor() {
        return executor;
    }

    public void setExecutor(Executor e) {
        executor = e;     
    }
     
    /*
    public void start(String address) {
        // find a transport compatible with binding  and address address
        
    }

    public void stop() {
        
    }
    */

//    final void createBinding(String uri) {
//        
//        BindingFactory bf = null;
//        try {
//            bf = bus.getBindingManager().getBindingFactory(uri);
//            binding = bf.createBinding();
//        } catch (BusException ex) {
//            throw new WebServiceException(ex);
//        } catch (WSDLException ex) {
//            throw new WebServiceException(ex);
//        } catch (IOException ex) {
//            throw new WebServiceException(ex);
//        }
//        if (null == bf) {
//            Message msg = new Message("NO_BINDING_FACTORY", BUNDLE, uri);
//            throw new WebServiceException(msg.toString());
//        }
//    }

}
