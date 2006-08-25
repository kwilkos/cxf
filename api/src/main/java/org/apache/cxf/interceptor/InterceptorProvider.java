package org.apache.cxf.interceptor;

import java.util.List;

public interface InterceptorProvider {
    
    List<Interceptor> getInInterceptors();
    
    List<Interceptor> getOutInterceptors();
    
    List<Interceptor> getFaultInterceptors();
}
