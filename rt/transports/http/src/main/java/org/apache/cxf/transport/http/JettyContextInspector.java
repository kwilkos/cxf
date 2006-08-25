package org.apache.cxf.transport.http;

import org.apache.cxf.endpoint.ContextInspector;
import org.mortbay.http.HttpContext;

public class JettyContextInspector implements ContextInspector {
    
    public String getAddress(Object serverContext) {
        if (HttpContext.class.isAssignableFrom(serverContext.getClass())) {
            return ((HttpContext)serverContext).getContextPath();
        } else {
            return null;
        }
    }
}
