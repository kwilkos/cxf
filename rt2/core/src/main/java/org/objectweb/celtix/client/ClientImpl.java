package org.objectweb.celtix.client;

import java.net.URL;
import javax.xml.namespace.QName;

import org.objectweb.celtix.Bus;
import org.objectweb.celtix.endpoint.Endpoint;
import org.objectweb.celtix.endpoint.EndpointImpl;
import org.objectweb.celtix.message.Exchange;
import org.objectweb.celtix.message.ExchangeImpl;
import org.objectweb.celtix.message.Message;
import org.objectweb.celtix.phase.PhaseInterceptorChain;
import org.objectweb.celtix.service.Service;
import org.objectweb.celtix.service.model.EndpointInfo;
import org.objectweb.celtix.service.model.OperationInfo;
import org.objectweb.celtix.wsdl11.WSDLServiceFactory;

public class ClientImpl implements Client {

    private Bus bus;
    private Endpoint endpoint;

    public ClientImpl(Bus b, URL wsdlURL, QName serviceName, String portName) {
        bus = b;
        WSDLServiceFactory sf = new WSDLServiceFactory(b, wsdlURL, serviceName);
        Service service = sf.create();
        EndpointInfo ei = service.getServiceInfo().getEndpoint(portName);
        endpoint = new EndpointImpl(bus, service, ei); 
    }

    public Endpoint getEndpoint() {
        return endpoint;
    }

    public Object invoke(OperationInfo oi, Object[] params) {

        Message message = endpoint.getBinding().createMessage();

        message.setSource(Object[].class, params);

        message.put(Message.OPERATION_INFO, oi);
        message.put(Message.BINDING, endpoint.getBinding());
        message.put(Message.REQUESTOR_ROLE, Boolean.TRUE);

        Exchange exchange = new ExchangeImpl();
        exchange.setOutMessage(message);

        PhaseInterceptorChain chain = new PhaseInterceptorChain(bus.getOutPhases());
        chain.add(bus.getOutInterceptors());
        chain.add(endpoint.getService().getOutInterceptors());
        chain.add(endpoint.getOutInterceptors());
        chain.add(endpoint.getBinding().getOutInterceptors());

        // execute chain

        // create transport/channel/conduit and assign to exchange

        // correlate

        return null;

    }

}
