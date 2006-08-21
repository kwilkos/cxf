package org.objectweb.celtix.interceptors;

import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;

import junit.framework.TestCase;

import org.easymock.IMocksControl;
import org.objectweb.celtix.Bus;
import org.objectweb.celtix.bus.CeltixBus;
import org.objectweb.celtix.endpoint.Endpoint;
import org.objectweb.celtix.message.Exchange;
import org.objectweb.celtix.message.ExchangeConstants;
import org.objectweb.celtix.message.ExchangeImpl;
import org.objectweb.celtix.message.Message;
import org.objectweb.celtix.message.MessageImpl;
import org.objectweb.celtix.phase.Phase;
import org.objectweb.celtix.phase.PhaseManager;
import org.objectweb.celtix.service.Service;
import org.objectweb.celtix.service.model.BindingOperationInfo;
import org.objectweb.celtix.service.model.EndpointInfo;
import org.objectweb.celtix.wsdl11.WSDLServiceFactory;

import static org.easymock.EasyMock.expect;
import static org.easymock.classextension.EasyMock.createNiceControl;

public class OutgoingChainInterceptorTest extends TestCase {
    
    Endpoint endpoint;
    Bus bus;
    String ns = "http://objectweb.org/hello_world_soap_http";
    private BindingOperationInfo op;
    
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        
        IMocksControl control = createNiceControl();
        bus = new CeltixBus();
        PhaseManager pm = control.createMock(PhaseManager.class);
        
        List<Phase> phases = new ArrayList<Phase>();
        phases.add(new Phase(Phase.SEND, 1000));
        bus.setExtension(pm, PhaseManager.class);
        expect(pm.getOutPhases()).andReturn(phases);

        endpoint = control.createMock(Endpoint.class);

        WSDLServiceFactory factory = new WSDLServiceFactory(bus, 
            getClass().getResource("/wsdl/hello_world.wsdl"),
            new QName(ns, "SOAPService"));
            
        Service service = factory.create();
        
        EndpointInfo endpointInfo = service.getServiceInfo().getEndpoint(new QName(ns, "SoapPort"));
        op = endpointInfo.getBinding().getOperation(new QName(ns, "greetMe"));
        
        expect(endpoint.getService()).andReturn(service).anyTimes();

        control.replay();
    }

    public void testInterceptor() throws Exception {
        OutgoingChainSetupInterceptor setupIntc = new OutgoingChainSetupInterceptor();
        OutgoingChainInterceptor intc = new OutgoingChainInterceptor();
        
        MessageImpl m = new MessageImpl();
        Exchange exchange = new ExchangeImpl();
        m.setExchange(exchange);
        exchange.setOutMessage(m);
        exchange.put(Message.BUS, bus);
        exchange.put(ExchangeConstants.ENDPOINT, endpoint);
        exchange.put(BindingOperationInfo.class.getName(), op);
        setupIntc.handleMessage(m);
        intc.handleMessage(m);
    }

}
