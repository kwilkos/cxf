package org.objectweb.celtix.jaxws;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import javax.jws.WebMethod;
import javax.xml.ws.WebServiceException;

import org.objectweb.celtix.client.Client;
import org.objectweb.celtix.common.i18n.Message;
import org.objectweb.celtix.common.logging.LogUtils;
import org.objectweb.celtix.service.model.InterfaceInfo;
import org.objectweb.celtix.service.model.OperationInfo;

public final class EndpointInvocationHandler extends BindingProviderImpl implements InvocationHandler {

    private static final Logger LOG = LogUtils.getL7dLogger(EndpointInvocationHandler.class);
    // private static final ResourceBundle BUNDLE = LOG.getResourceBundle();
    
    private Client client;
    private Map<Method, OperationInfo> infoMap = new ConcurrentHashMap<Method, OperationInfo>();
    
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        
        OperationInfo oi = getOperationInfo(proxy, method);
        
        return client.invoke(oi, args);
            
    }
    
    OperationInfo getOperationInfo(Object proxy, Method method) {
          
        OperationInfo oi = infoMap.get(method);
        
        if (null == oi) {
            WebMethod wma = method.getAnnotation(WebMethod.class);
            String operationName = null;
            if (null != wma && !"".equals(wma.operationName())) {
                operationName = wma.operationName();
            } else {
                operationName = method.getName();
            }
            InterfaceInfo ii = client.getEndpoint().getService().getServiceInfo().getInterface();
            oi = ii.getOperation(operationName);
            if (null == oi) {
                Message msg = new Message("NO_OPERATION_INFO", LOG, operationName);
                throw new WebServiceException(msg.toString());
            }
            infoMap.put(method, oi);
        }
        return oi;
    }
    
    
}

/*
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.jws.Oneway;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.validation.Schema;
import javax.xml.ws.AsyncHandler;
import javax.xml.ws.ProtocolException;
import javax.xml.ws.Response;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.spi.ServiceDelegate;

import org.objectweb.celtix.Bus;
import org.objectweb.celtix.bindings.BindingFactory;
import org.objectweb.celtix.bindings.ClientBinding;
import org.objectweb.celtix.bindings.DataBindingCallback;
import org.objectweb.celtix.common.logging.LogUtils;
import org.objectweb.celtix.configuration.Configuration;
import org.objectweb.celtix.context.ObjectMessageContext;
import org.objectweb.celtix.jaxb.JAXBDataBindingCallback;
import org.objectweb.celtix.jaxb.JAXBEncoderDecoder;
import org.objectweb.celtix.ws.addressing.EndpointReferenceType;
import org.objectweb.celtix.wsdl.EndpointReferenceUtils;

public final class EndpointInvocationHandler extends BindingProviderImpl implements InvocationHandler
{

    private static final Logger LOG = LogUtils.getL7dLogger(EndpointInvocationHandler.class);
    private static final String METHOD = EndpointInvocationHandler.class.getName() + ".METHOD";
    
    
    private final ClientBinding clientBinding;    
    private final Class<?> portTypeInterface;
    private final Bus bus;
    private JAXBContext context;
    private Schema schema;
    private final ServiceDelegate service;
    private final EndpointReferenceType endpointReference;
    
    public EndpointInvocationHandler(Bus b, EndpointReferenceType reference,
                                     ServiceDelegate s, Configuration configuration, Class<?> portSEI) {
        bus = b;
        endpointReference = reference;
        service = s;
        portTypeInterface = portSEI;
        clientBinding = createBinding(reference, configuration);
        setBinding(clientBinding.getBinding());
        try {
            context = JAXBEncoderDecoder.createJAXBContextForClass(portSEI);

            Boolean enableSchemaValidation = configuration.getObject(Boolean.class,
                "enableSchemaValidation");
            if (enableSchemaValidation != null && enableSchemaValidation.booleanValue()) {
                LOG.fine("port schema validation enabled"); 
                schema = EndpointReferenceUtils.getSchema(b.getWSDLManager(), reference);
            }
        } catch (JAXBException ex1) {
            // TODO Auto-generated catch block
            ex1.printStackTrace();
            context = null;
        }
    }

    public Object invoke(Object proxy, Method method, Object args[]) throws Exception {
        
        LOG.info("EndpointInvocationHandler: invoke");

        if (portTypeInterface.equals(method.getDeclaringClass())) {
            return invokeSEIMethod(proxy, method, args);
        }             

        try {
            return method.invoke(this, args);
        } catch (InvocationTargetException ite) {
            LOG.log(Level.SEVERE, "BINDING_PROVIDER_METHOD_EXC", method.getName());
            if (WebServiceException.class.isAssignableFrom(ite.getCause().getClass())) {
                throw (WebServiceException)ite.getCause();
            }
            throw new WebServiceException(ite.getCause());
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, "BINDING_PROVIDER_METHOD_EXC", method.getName());
            throw new WebServiceException(ex);
        } 
    }

    private Object invokeSEIMethod(Object proxy, Method method, Object parameters[])
        throws Exception {

        ObjectMessageContext objMsgContext = clientBinding.createObjectContext();
        objMsgContext.putAll(getRequestContext());
        
        
        objMsgContext.put(ObjectMessageContext.REQUEST_PROXY, proxy);
       
        objMsgContext.setMessageObjects(parameters);
        objMsgContext.put(METHOD, method);
        objMsgContext.put(ObjectMessageContext.METHOD_OBJ, method);
        // TODO: Expect EndpointReferenceUtils.getWSDLLocation to return a URL in future
        String wsdlLocation = EndpointReferenceUtils.getWSDLLocation(endpointReference); 
        if (null != wsdlLocation) {
            try {
                objMsgContext.put(ObjectMessageContext.WSDL_DESCRIPTION, 
                    new java.net.URI(wsdlLocation));
            } catch (URISyntaxException ex) {
                LOG.log(Level.WARMING, "WSDL_DESCRIPTION_URI_MSG", ex);
            }
            if (null == objMsgContext.get(ObjectMessageContext.WSDL_DESCRIPTION)) {
                try {
                    objMsgContext.put(ObjectMessageContext.WSDL_DESCRIPTION,
                                      new java.net.URL(wsdlLocation)); 
                } catch (MalformedURLException ex) {
                    LOG.log(Level.WARMING, "WSDL_DESCRIPTION_URL_MSG", ex);
                }
            }
        }
        boolean isOneway = (method.getAnnotation(Oneway.class) != null) ? true : false;
        boolean isAsync = method.getName().endsWith("Async");

       
        if (isOneway) {
            clientBinding.invokeOneWay(objMsgContext,
                                       new JAXBDataBindingCallback(method,
                                                                   DataBindingCallback.Mode.PARTS,
                                                                   context,
                                                                   schema));
        } else if (isAsync) {         
            Future<ObjectMessageContext> objMsgContextAsynch =
                clientBinding.invokeAsync(objMsgContext,
                                          new JAXBDataBindingCallback(method,
                                                                      DataBindingCallback.Mode.PARTS,
                                                                      context,
                                                                      schema)
                                                                      , service.getExecutor()); 
            
            Response<?> r = new AsyncResponse<Object>(objMsgContextAsynch, Object.class);
            if (parameters.length > 0 && parameters[parameters.length - 1] instanceof AsyncHandler) {
                // callback style
                AsyncCallbackFuture f = new AsyncCallbackFuture(r, 
                    (AsyncHandler)parameters[parameters.length - 1]);
                // service must always have an executor associated with it
                service.getExecutor().execute(f);
                return f;
                
                
            } else {
                return r;
            }

            
        } else {
            objMsgContext = clientBinding.invoke(objMsgContext,
                                                 new JAXBDataBindingCallback(method,
                                                                             DataBindingCallback.Mode.PARTS,
                                                                             context,
                                                                             schema));
        }

        populateResponseContext(objMsgContext);

        if (objMsgContext.getException() != null) {
            LOG.log(Level.INFO, "ENDPOINT_INVOCATION_FAILED", method.getName());
            if (isValidException(objMsgContext)) {
                throw (Exception)objMsgContext.getException();
            } else {                
                throw new ProtocolException(objMsgContext.getException());
            }
        }
        
        return objMsgContext.getReturn();
    }    
    
    protected ClientBinding createBinding(EndpointReferenceType ref, Configuration c) {

        ClientBinding binding = null;
        try {
            
            String bindingId = c.getString("bindingId");
            BindingFactory factory = bus.getBindingManager().getBindingFactory(bindingId);
            assert factory != null : "unable to find binding factory for " + bindingId;
            binding = factory.createClientBinding(ref);
        } catch (Exception ex) {
            throw new WebServiceException(ex);
        }
        
        binding.configureSystemHandlers(c);
        return binding;
    }
    
    private boolean isValidException(ObjectMessageContext objContext) {
        Method method = (Method)objContext.get(METHOD);
        Throwable t = objContext.getException();
        
        boolean val = ProtocolException.class.isAssignableFrom(t.getClass()) 
                   || WebServiceException.class.isAssignableFrom(t.getClass());
        
        if (!val) {
            for (Class<?> clazz : method.getExceptionTypes()) {
                if (clazz.isAssignableFrom(t.getClass())) {
                    val = true;
                    break;
                }
            }
        }
        
        return val;
    }
}
*/
