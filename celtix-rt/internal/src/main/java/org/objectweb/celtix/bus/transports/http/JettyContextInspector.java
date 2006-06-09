package org.objectweb.celtix.bus.transports.http;

import org.mortbay.http.HttpContext;
import org.objectweb.celtix.endpoints.ContextInspector;

public class JettyContextInspector implements ContextInspector {
    
    public String getAddress(Object serverContext) {
        if (serverContext.getClass().isAssignableFrom(HttpContext.class)) {
            return ((HttpContext)serverContext).getContextPath();
        } else {
            return null;
        }
    }
}
