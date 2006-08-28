package org.apache.cxf.interceptor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public abstract class AbstractAttributedInterceptorProvider extends HashMap<String, Object>
    implements InterceptorProvider {

    private List<Interceptor> in = new ArrayList<Interceptor>();
    private List<Interceptor> out = new ArrayList<Interceptor>();
    private List<Interceptor> outFault  = new ArrayList<Interceptor>();
    private List<Interceptor> inFault  = new ArrayList<Interceptor>();
    
    public List<Interceptor> getOutFaultInterceptors() {
        return outFault;
    }

    public List<Interceptor> getInFaultInterceptors() {
        return inFault;
    }

    public List<Interceptor> getInInterceptors() {
        return in;
    }

    public List<Interceptor> getOutInterceptors() {
        return out;
    }
}
