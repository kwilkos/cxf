package org.objectweb.celtix.interceptors;

import java.util.List;

public interface InterceptorProvider {
    
    List<Interceptor> getInInterceptors();
    
    List<Interceptor> getOutInterceptors();
    
    List<Interceptor> getFaultInterceptors();
}
