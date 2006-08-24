package org.apache.cxf.jaxws;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.xml.ws.Binding;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.handler.MessageContext;

public class BindingProviderImpl implements BindingProvider {
    
    private final Binding binding;
    private ThreadLocal<Map<String, Object>> requestContext;
    private Map<String, Object> responseContext;
    
    public BindingProviderImpl(Binding b) {
        binding = b;
    }
    
    public Map<String, Object> getRequestContext() {
        if (requestContext == null) {
            requestContext = new ThreadLocal<Map<String, Object>>() {
                protected synchronized Map<String, Object> initialValue() {
                    return new HashMap<String, Object>();
                }
            };
        }
        return (Map<String, Object>)requestContext.get();
    }
    
    public Map<String, Object> getResponseContext() {
        if (responseContext == null) {
            responseContext = new HashMap<String, Object>();
        }
        return responseContext;
    }

    public Binding getBinding() {
        return binding;
    }
    
    protected void populateResponseContext(MessageContext ctx) {
        
        Iterator<String> iter  = ctx.keySet().iterator();
        Map<String, Object> respCtx = getResponseContext();
        while (iter.hasNext()) {
            String obj = iter.next();
            if (MessageContext.Scope.APPLICATION.compareTo(ctx.getScope(obj)) == 0) {
                respCtx.put(obj, ctx.get(obj));
            }
        }
    }

}
