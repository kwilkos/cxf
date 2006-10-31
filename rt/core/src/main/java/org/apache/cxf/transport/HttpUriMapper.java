package org.apache.cxf.transport;

public final class HttpUriMapper {
    
    private HttpUriMapper() {
        // Util class dont need public constructor
    }
    
    public static String getContextName(String path) {
        String contextName = "";        
        int idx = path.lastIndexOf('/');
        if (idx > 0) {
            contextName = path.substring(0, idx);          
        }
        return contextName;
    }
    
    public static String getResourceBase(String path) {
        String servletMap = path;
        int idx = path.lastIndexOf('/');
        if (idx > 0) {
            servletMap = path.substring(idx);
        }
        return servletMap;
    }
}
