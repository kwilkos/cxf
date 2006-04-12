package org.objectweb.celtix.routing;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

import javax.annotation.Resource;
import javax.wsdl.Definition;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.handler.MessageContext;

import org.objectweb.celtix.routing.configuration.RouteType;

public class SEIImplHandler implements InvocationHandler {
    private final Definition wsdlModel;
    private final RouteType route;
    private final URL wsdlLocation;

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
    }

    @Resource
    public void setContext(WebServiceContext ctx) {
        wsCtx = ctx;
    }

    public Object invoke(Object proxy, Method method, Object[] args) {
        System.out.println(wsdlLocation.toExternalForm());
        System.out.println(route.getName());
        updateRequestContext(null);
        return null;
    }
    
    private void updateRequestContext(Map<String, Object> reqCtx) {
        MessageContext sourceMsgCtx = wsCtx.getMessageContext();
        reqCtx.put(BindingProvider.USERNAME_PROPERTY, 
                   sourceMsgCtx.get(BindingProvider.USERNAME_PROPERTY));
        reqCtx.put(BindingProvider.PASSWORD_PROPERTY, 
                   sourceMsgCtx.get(BindingProvider.PASSWORD_PROPERTY));        
    }
}
