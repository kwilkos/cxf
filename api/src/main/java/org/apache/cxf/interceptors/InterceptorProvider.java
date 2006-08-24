package org.apache.cxf.interceptors;

import java.util.List;

public interface InterceptorProvider {
    
    List<Interceptor> getInInterceptors();
    
    List<Interceptor> getOutInterceptors();
    
    List<Interceptor> getFaultInterceptors();
}
