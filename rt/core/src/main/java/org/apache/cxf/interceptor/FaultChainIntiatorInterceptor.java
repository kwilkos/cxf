package org.apache.cxf.interceptor;

import org.apache.cxf.Bus;
import org.apache.cxf.binding.Binding;
import org.apache.cxf.endpoint.Endpoint;
import org.apache.cxf.message.Exchange;
import org.apache.cxf.message.ExchangeImpl;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.PhaseInterceptorChain;
import org.apache.cxf.phase.PhaseManager;
import org.apache.cxf.service.Service;

public class FaultChainIntiatorInterceptor implements Interceptor<Message> {
    Endpoint endpoint;
    Bus bus;

    public FaultChainIntiatorInterceptor(Endpoint endpoint, Bus bus) {
        super();
        this.endpoint = endpoint;
        this.bus = bus;
    }

    public void handleMessage(Message m) {
        Message message = endpoint.getBinding().createMessage(m);
        Exchange exchange = new ExchangeImpl();
        exchange.setInMessage(message);
        message.setExchange(exchange);
        
        exchange.put(Endpoint.class, endpoint);
        exchange.put(Service.class, endpoint.getService());
        exchange.put(Binding.class, endpoint.getBinding());
        exchange.put(Bus.class, bus);
        exchange.setDestination(m.getDestination());
        
        // setup chain
        PhaseInterceptorChain chain = new PhaseInterceptorChain(bus.getExtension(PhaseManager.class)
            .getInPhases());
        chain.add(bus.getOutFaultInterceptors());
        chain.add(endpoint.getOutFaultInterceptors());
        chain.add(endpoint.getBinding().getOutFaultInterceptors());
        chain.add(endpoint.getService().getOutFaultInterceptors());

        chain.doIntercept(message);        
    }

    public void handleFault(Message message) {
    }
   
}
