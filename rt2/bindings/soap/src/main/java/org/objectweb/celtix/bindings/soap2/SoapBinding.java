package org.objectweb.celtix.bindings.soap2;

import java.util.ArrayList;
import java.util.List;

import org.objectweb.celtix.bindings.Binding;
import org.objectweb.celtix.interceptors.Interceptor;
import org.objectweb.celtix.message.Message;
import org.objectweb.celtix.message.MessageImpl;

public class SoapBinding implements Binding {

    private List<Interceptor> in;
    private List<Interceptor> out;
    private List<Interceptor> fault;
    
    SoapBinding() {
        in = new ArrayList<Interceptor>();
        out = new ArrayList<Interceptor>();
        fault = new ArrayList<Interceptor>();
        
        in.add(new MustUnderstandInterceptor());
        
        // ...
    }
    
    public Message createMessage() {
        return new SoapMessage(new MessageImpl());
    }

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
