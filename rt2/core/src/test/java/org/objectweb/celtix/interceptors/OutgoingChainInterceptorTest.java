package org.objectweb.celtix.interceptors;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.easymock.IMocksControl;
import org.objectweb.celtix.Bus;
import org.objectweb.celtix.endpoint.Endpoint;
import org.objectweb.celtix.message.Exchange;
import org.objectweb.celtix.message.ExchangeConstants;
import org.objectweb.celtix.message.ExchangeImpl;
import org.objectweb.celtix.message.Message;
import org.objectweb.celtix.message.MessageImpl;
import org.objectweb.celtix.phase.Phase;
import org.objectweb.celtix.phase.PhaseManager;
import org.objectweb.celtix.service.ServiceImpl;

import static org.easymock.EasyMock.expect;
import static org.easymock.classextension.EasyMock.createNiceControl;

public class OutgoingChainInterceptorTest extends TestCase {
    
    public void testInterceptor() throws Exception {
        OutgoingChainInterceptor intc = new OutgoingChainInterceptor();
        
        MessageImpl m = new MessageImpl();
        Exchange exchange = new ExchangeImpl();
        m.setExchange(exchange);
        exchange.put(Message.BUS, getBus());
        exchange.put(ExchangeConstants.ENDPOINT, createEndpoint());

        intc.handleMessage(m);
    }
    
    Bus getBus() {
        IMocksControl control = createNiceControl();
        Bus bus = control.createMock(Bus.class);
        PhaseManager pm = control.createMock(PhaseManager.class);
        
        List<Phase> phases = new ArrayList<Phase>();
        phases.add(new Phase(Phase.SEND, 1000));
        expect(bus.getExtension(PhaseManager.class)).andReturn(pm);
        expect(pm.getOutPhases()).andReturn(phases);

        control.replay();
        
        return bus;
    }
    
    Endpoint createEndpoint() throws Exception {
        IMocksControl control = createNiceControl();
        Endpoint endpoint = control.createMock(Endpoint.class);

        ServiceImpl service = new ServiceImpl(null);
        expect(endpoint.getService()).andReturn(service).anyTimes();

        control.replay();
        
        return endpoint;
    }
}
