package org.objectweb.celtix.bus.ws.rm;

import java.util.ArrayList;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.Duration;
import javax.xml.ws.handler.Handler;

import junit.framework.TestCase;

import org.easymock.classextension.EasyMock;
import org.objectweb.celtix.bindings.AbstractBindingBase;
import org.objectweb.celtix.bus.configuration.wsrm.SourcePolicyType;
import org.objectweb.celtix.bus.handlers.HandlerChainInvoker;
import org.objectweb.celtix.context.ObjectMessageContext;
import org.objectweb.celtix.context.ObjectMessageContextImpl;
import org.objectweb.celtix.ws.rm.CreateSequenceType;
import org.objectweb.celtix.ws.rm.Identifier;
import org.objectweb.celtix.ws.rm.OfferType;

public class CreateSequenceRequestTest extends TestCase {
    
    private static final String NON_ANONYMOUS_ACKSTO_ADDRESS = "http://localhost:9999/decoupled";
    private static final Duration ONE_DAY;
    private ObjectMessageContext objectCtx;
    private RMSource source;
    private AbstractBindingBase binding;
    private HandlerChainInvoker hci;
    private SourcePolicyType sp;
    
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
        source = EasyMock.createMock(RMSource.class);
        binding = EasyMock.createMock(AbstractBindingBase.class);
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
        
        EasyMock.replay(source);
        EasyMock.replay(binding);
        
        CreateSequenceRequest req = new CreateSequenceRequest(binding, source);
        assertNotNull(req);
        
        assertNotNull(CreateSequenceRequest.createDataBindingCallback());
        assertNotNull(req.getObjectMessageContext().getMethod());
        
        Object[] params = req.getObjectMessageContext().getMessageObjects();
        assertEquals(1, params.length);
        CreateSequenceType cs = (CreateSequenceType)params[0];
        
        assertEquals(RMUtils.getAddressingConstants().getAnonymousURI(),
                     cs.getAcksTo().getAddress().getValue());
        assertNull(cs.getExpires());
        assertNull(cs.getOffer());
        
        EasyMock.verify(source);
        EasyMock.verify(binding);
    }
    
    public void testNonDefaultConstruction() {     
        Identifier offeredSid = RMUtils.getWSRMFactory().createIdentifier();
        source.offer();        
        EasyMock.expectLastCall().andReturn(offeredSid);
        EasyMock.replay(source);
        EasyMock.replay(binding);
        
        sp.setAcksTo(NON_ANONYMOUS_ACKSTO_ADDRESS);
        sp.setSequenceExpiration(ONE_DAY);
        sp.setIncludeOffer(true);
        sp.setOfferedSequenceExpiration(Sequence.PT0S);
        
        CreateSequenceRequest req = new CreateSequenceRequest(binding, source);
        assertNotNull(req);
        
        Object[] params = req.getObjectMessageContext().getMessageObjects();
        assertEquals(1, params.length);
        CreateSequenceType cs = (CreateSequenceType)params[0];
        
        assertEquals(NON_ANONYMOUS_ACKSTO_ADDRESS,
                     cs.getAcksTo().getAddress().getValue());
        assertEquals(ONE_DAY, cs.getExpires().getValue());
        OfferType offer = cs.getOffer();
        assertEquals(Sequence.PT0S, offer.getExpires().getValue());
        assertNotNull(offer.getIdentifier());
        
        EasyMock.verify(source);
        EasyMock.verify(binding);
    }
}
