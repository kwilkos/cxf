package org.apache.cxf.interceptors;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractBasicInterceptorProvider implements InterceptorProvider {

    private List<Interceptor> in = new ArrayList<Interceptor>();
    private List<Interceptor> out = new ArrayList<Interceptor>();
    private List<Interceptor> fault  = new ArrayList<Interceptor>();
    
    public List<Interceptor> getFaultInterceptors() {
        return fault;
    }

    public List<Interceptor> getInInterceptors() {
        return in;
    }

    public List<Interceptor> getOutInterceptors() {
        return out;
    }

    

}
