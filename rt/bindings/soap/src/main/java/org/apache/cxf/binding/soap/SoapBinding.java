package org.apache.cxf.binding.soap;

import java.util.ArrayList;
import java.util.List;

import org.apache.cxf.binding.Binding;
import org.apache.cxf.binding.soap.interceptor.Soap11FaultInInterceptor;
import org.apache.cxf.binding.soap.interceptor.Soap11FaultOutInterceptor;
import org.apache.cxf.interceptor.AbstractBasicInterceptorProvider;
import org.apache.cxf.interceptor.Interceptor;
import org.apache.cxf.message.Message;
import org.apache.cxf.message.MessageImpl;

public class SoapBinding extends AbstractBasicInterceptorProvider implements Binding {

    private List<Interceptor> in;
    private List<Interceptor> out;
    private List<Interceptor> fault;
    private Interceptor outFaultInterceptor;
    private Interceptor inFaultInterceptor;
    
    public SoapBinding() {
        in = new ArrayList<Interceptor>();
        out = new ArrayList<Interceptor>();
        fault = new ArrayList<Interceptor>();
        
        outFaultInterceptor = new Soap11FaultOutInterceptor();
        inFaultInterceptor = new Soap11FaultInInterceptor();
    }
    
    public Message createMessage() {
        return createMessage(new MessageImpl());
    }

    public Message createMessage(Message m) {
        return new SoapMessage(m);
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

    public Interceptor getInFaultInterceptor() {
        return inFaultInterceptor;
    }

    public Interceptor getOutFaultInterceptor() {
        return outFaultInterceptor;
    }
}
