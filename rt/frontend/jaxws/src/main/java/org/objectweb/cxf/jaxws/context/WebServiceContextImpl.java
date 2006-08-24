package org.apache.cxf.jaxws.context;

import java.security.Principal;

import javax.xml.ws.WebServiceContext;
import javax.xml.ws.handler.MessageContext;


public class WebServiceContextImpl implements WebServiceContext {

    private static ThreadLocal<MessageContext> context = new ThreadLocal<MessageContext>();

    public WebServiceContextImpl() { 
    }

    public WebServiceContextImpl(MessageContext ctx) { 
        setMessageContext(ctx);
    } 

    // Implementation of javax.xml.ws.WebServiceContext

    public final MessageContext getMessageContext() {
        return context.get();
    }

    public final Principal getUserPrincipal() {
        return null;
    }

    public final boolean isUserInRole(final String string) {
        return false;
    }

    public static void setMessageContext(MessageContext ctx) {
        context.set(ctx);
    }

    public static void clear() {
        context.set(null);
    }
}
