package org.objectweb.celtix.bus.ws.rm;

import java.util.ArrayList;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.Duration;
import javax.xml.ws.handler.Handler;

import junit.framework.TestCase;

import org.easymock.IMocksControl;
import org.easymock.classextension.EasyMock;
import org.objectweb.celtix.bindings.AbstractBindingBase;
import org.objectweb.celtix.bus.configuration.wsrm.SourcePolicyType;
import org.objectweb.celtix.bus.handlers.HandlerChainInvoker;
import org.objectweb.celtix.bus.ws.addressing.ContextUtils;
import org.objectweb.celtix.bus.ws.addressing.VersionTransformer;
import org.objectweb.celtix.context.ObjectMessageContext;
import org.objectweb.celtix.context.ObjectMessageContextImpl;
import org.objectweb.celtix.transports.Transport;
import org.objectweb.celtix.ws.rm.CreateSequenceType;
import org.objectweb.celtix.ws.rm.Identifier;
import org.objectweb.celtix.ws.rm.OfferType;
import org.objectweb.celtix.wsdl.EndpointReferenceUtils;

public class CreateSequenceRequestTest extends TestCase {
    
    private static final String NON_ANONYMOUS_ACKSTO_ADDRESS = "http://localhost:9999/decoupled";
    private static final Duration ONE_DAY;
    private ObjectMessageContext objectCtx;
    private RMSource source;
    private AbstractBindingBase binding;
    private Transport transport;
    private HandlerChainInvoker hci;
    private SourcePolicyType sp;
    private IMocksControl control;
    
    static {
        Duration d = null;
        try {
            DatatypeFactory df = DatatypeFactory.newInstance();
            d = df.newDuration("PT24H");
        } catch (DatatypeConfigurationException ex) {
            ex.printStackTrace();
        }
        ONE_DAY = d;
    }
    
    public void setUp() {
        objectCtx = new ObjectMessageContextImpl(); 
        control = EasyMock.createNiceControl();
        source = control.createMock(RMSource.class);
        binding = control.createMock(AbstractBindingBase.class);
        transport = control.createMock(Transport.class);
        hci = new HandlerChainInvoker(new ArrayList<Handler>());
        sp = RMUtils.getWSRMConfFactory().createSourcePolicyType();
        
        binding.createObjectContext();
        EasyMock.expectLastCall().andReturn(objectCtx);
        binding.createHandlerInvoker();
        EasyMock.expectLastCall().andReturn(hci);
        source.getSourcePolicies();
        EasyMock.expectLastCall().andReturn(sp);
        
    }
    
    public void testDefaultConstruction() {     
        
        Identifier offeredSid = RMUtils.getWSRMFactory().createIdentifier();
        source.generateSequenceIdentifier();        
        EasyMock.expectLastCall().andReturn(offeredSid);
        
        control.replay();    
        
        CreateSequenceRequest req =
            new CreateSequenceRequest(binding,
                                      transport,
                                      source,
                                      getEPR("to"),
                                      VersionTransformer.convert(getEPR("acksTo")),
                                      ContextUtils.WSA_OBJECT_FACTORY.createRelatesToType());
        assertNotNull(req);
        
        assertNotNull(CreateSequenceRequest.createDataBindingCallback());
        
        Object[] params = req.getObjectMessageContext().getMessageObjects();
        assertEquals(1, params.length);
        CreateSequenceType cs = (CreateSequenceType)params[0];
        
        assertEquals(VersionTransformer.convert(getEPR("acksTo")).getAddress().getValue(),
                     cs.getAcksTo().getAddress().getValue());
        assertNull(cs.getExpires());
        
        // default is to include offers
        
        OfferType offer = cs.getOffer();
        assertNotNull(offer);
        assertNull(offer.getExpires());
        assertNotNull(offer.getIdentifier());
        
        control.verify();
    }
    
    public void testNonDefaultConstruction() {     
        
        control.replay();
                
        sp.setAcksTo(NON_ANONYMOUS_ACKSTO_ADDRESS);
        sp.setSequenceExpiration(ONE_DAY);
        sp.setIncludeOffer(false);

        CreateSequenceRequest req = 
            new CreateSequenceRequest(binding,
                                      transport, 
                                      source,
                                      getEPR("to"),
                                      VersionTransformer.convert(getEPR("acksTo")),
                                      ContextUtils.WSA_OBJECT_FACTORY.createRelatesToType());
        assertNotNull(req);
        
        Object[] params = req.getObjectMessageContext().getMessageObjects();
        assertEquals(1, params.length);
        CreateSequenceType cs = (CreateSequenceType)params[0];
        
        assertEquals(NON_ANONYMOUS_ACKSTO_ADDRESS,
                     cs.getAcksTo().getAddress().getValue());
        assertEquals(ONE_DAY, cs.getExpires().getValue());
        assertNull(cs.getOffer());
        
        control.verify();
    }
    
    private org.objectweb.celtix.ws.addressing.EndpointReferenceType getEPR(String s) {
        return EndpointReferenceUtils.getEndpointReference("http://nada.nothing.nowhere.null/" + s);
    }
}
