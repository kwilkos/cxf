package org.apache.cxf.transports.http;

import org.mortbay.http.HttpContext;
import org.apache.cxf.endpoint.ContextInspector;

public class JettyContextInspector implements ContextInspector {
    
    public String getAddress(Object serverContext) {
        if (HttpContext.class.isAssignableFrom(serverContext.getClass())) {
            return ((HttpContext)serverContext).getContextPath();
        } else {
            return null;
        }
    }
}
