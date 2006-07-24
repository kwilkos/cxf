package org.objectweb.celtix.client;

import org.objectweb.celtix.Bus;
import org.objectweb.celtix.bindings.Binding;
import org.objectweb.celtix.endpoint.EndpointImpl;
import org.objectweb.celtix.message.Exchange;
import org.objectweb.celtix.message.ExchangeImpl;
import org.objectweb.celtix.message.Message;
import org.objectweb.celtix.phase.PhaseInterceptorChain;
import org.objectweb.celtix.service.Service;
import org.objectweb.celtix.service.model.EndpointInfo;
import org.objectweb.celtix.service.model.OperationInfo;

public class ClientImpl extends EndpointImpl implements Client {

    private Bus bus;
    private Service service;

    protected ClientImpl(Bus bs, Service s, Binding bn, EndpointInfo e) {
        super(bn, e);
        bus = bs;
        service = s;
    }

    public Object invoke(OperationInfo oi, Object[] params) {

        Message message = getBinding().createMessage();

        message.setSource(Object[].class, params);

        message.put(Message.OPERATION_INFO, oi);
        message.put(Message.BINDING, getBinding());
        message.put(Message.REQUESTOR_ROLE, Boolean.TRUE);

        Exchange exchange = new ExchangeImpl();
        exchange.setOutMessage(message);

        PhaseInterceptorChain chain = new PhaseInterceptorChain(bus.getOutPhases());
        chain.add(bus.getOutInterceptors());
        chain.add(service.getOutInterceptors());
        chain.add(getOutInterceptors());

        // execute chain

        // create transport/channel/conduit and assign to exchange

        // correlate

        return null;

    }

}
