package org.objectweb.celtix.routing;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.lang.reflect.UndeclaredThrowableException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Resource;
import javax.wsdl.Definition;
import javax.wsdl.Port;
import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.ProtocolException;
import javax.xml.ws.Service;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.handler.MessageContext;

import org.objectweb.celtix.common.i18n.Message;
import org.objectweb.celtix.common.logging.LogUtils;
import org.objectweb.celtix.routing.configuration.DestinationType;
import org.objectweb.celtix.routing.configuration.RouteType;

public class SEIImplHandler implements InvocationHandler {
    private static final Logger LOG = LogUtils.getL7dLogger(SEIImplHandler.class);
    protected List<Object> proxyList;
    private final Definition wsdlModel;
    private final RouteType route;
    private final URL wsdlLocation;
    private boolean doInit;
    /**
     * Injectable context.
     */
    @Resource
    private WebServiceContext wsCtx;

    public SEIImplHandler(Definition model, RouteType rt) {
        wsdlModel = model;
        route = rt;
        try {
            wsdlLocation = new URL(wsdlModel.getDocumentBaseURI());
        } catch (MalformedURLException mue) {
            throw new WebServiceException("Invalid wsdl url", mue);
        }
        doInit = true;
    }

    @Resource
    public void setContext(WebServiceContext ctx) {
        wsCtx = ctx;
    }

    public WebServiceContext getContext() {
        return wsCtx;
    }
    
    public Object invoke(Object proxy, Method method, Object[] args) throws Exception {
        if (doInit) {
            init(method.getDeclaringClass());
        }
        
        //TODO Iterate over the list for fanin/fanout
        Object clientProxy = proxyList.get(0);
        Object ret = null;
        if (Proxy.isProxyClass(clientProxy.getClass())
            && BindingProvider.class.isAssignableFrom(clientProxy.getClass())) {
            //Proxy instance inherits out of BindingProvider Interface
            //as per JAXWS spec 4.2.3
            BindingProvider bp = (BindingProvider) clientProxy;
            Exception ex = null;
            
            //Synchrnization will not be required if the client proxies are thread safe.
            synchronized (this) {
                updateRequestContext(bp.getRequestContext());
                
                InvocationHandler proxyHandler = Proxy.getInvocationHandler(clientProxy);
                try {
                    ret = proxyHandler.invoke(clientProxy, method, args);
                } catch (UndeclaredThrowableException ute) {
                    LOG.log(Level.SEVERE, "PROXY_INVOKE_UNDECLEXCEPTION", method.toString());
                    ex = new ProtocolException(new Message("PROXY_INVOKE_UNDECLEXCEPTION",
                                                           LOG,
                                                           method.toString()).toString(),
                                               ute.getCause());
                } catch (Error error) {
                    LOG.log(Level.SEVERE, "PROXY_INVOKE_ERROR", method.toString());
                    ex = new ProtocolException(new Message("PROXY_INVOKE_UNDECLEXCEPTION",
                                                           LOG,
                                                           method.toString()).toString(),
                                               error);
                } catch (Throwable t) {
                    LOG.log(Level.SEVERE, "PROXY_INVOKE_EXCEPTION", method.toString());                
                    ex = (Exception) t;
                }
                
                updateWebServiceContext(bp.getResponseContext());
            }
                
            if (null != ex) {
                throw ex;
            }
        }
        
        return ret;
    }
    
    protected synchronized void init(Class<?> seiClass) {
        if (doInit) {
            List<DestinationType> dtList = route.getDestination();
            if (null == proxyList) {
                proxyList = new ArrayList<Object>(dtList.size());
            }

            for (DestinationType dt : dtList) {
                Service dtService = createService(wsdlLocation, dt.getService());
                QName portName;
                if (dt.isSetPort()) {
                    portName = new QName(dt.getService().getNamespaceURI(), dt.getPort());
                } else {
                    javax.wsdl.Service destService = 
                        wsdlModel.getService(dt.getService());
                    String name = ((Port)destService.getPorts().values().iterator()).getName();
                    portName = new QName(dt.getService().getNamespaceURI(), name);
                }
                
                Object proxy = dtService.getPort(portName, seiClass);
                if (null == proxy) {
                    LOG.log(Level.SEVERE, 
                            "GETPORT_FAILURE", 
                            new Object[] {dt.getService(), portName.toString()});
                    throw new WebServiceException(new Message("GETPORT_FAILURE", 
                                                              LOG, 
                                                              dt.getService(),
                                                              portName).toString());
                }
                proxyList.add(proxy);
            }
            doInit = false;
        }
    }
    
    protected Service createService(URL wsdlUrl, QName serviceName) {
        //TODO Set Executor used by the Source Endpoint onto Service
        //Currently destination service uses bus workqueue.
        return Service.create(wsdlUrl, serviceName);
    }
    
    private void updateRequestContext(Map<String, Object> reqCtx) {
        if (null != getContext()) {
            MessageContext sourceMsgCtx = getContext().getMessageContext();
            reqCtx.put(BindingProvider.USERNAME_PROPERTY, 
                       sourceMsgCtx.get(BindingProvider.USERNAME_PROPERTY));
            reqCtx.put(BindingProvider.PASSWORD_PROPERTY, 
                       sourceMsgCtx.get(BindingProvider.PASSWORD_PROPERTY));
        }
    }
    
    private void updateWebServiceContext(Map<String, Object> respCtx) {
        //TODO
    }
}
