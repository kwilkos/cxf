package org.objectweb.celtix.ws.rm;

import java.lang.reflect.Method;
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
import org.objectweb.celtix.context.ObjectMessageContext;
import org.objectweb.celtix.context.ObjectMessageContextImpl;
import org.objectweb.celtix.context.OutputStreamMessageContext;
import org.objectweb.celtix.transports.Transport;
import org.objectweb.celtix.ws.addressing.AddressingProperties;
import org.objectweb.celtix.ws.addressing.ContextUtils;
import org.objectweb.celtix.ws.addressing.EndpointReferenceType;
import org.objectweb.celtix.ws.addressing.RelatesToType;
import org.objectweb.celtix.ws.addressing.VersionTransformer;

public class CreateSequenceRequestTest extends TestCase {
    
    private static final String NON_ANONYMOUS_ACKSTO_ADDRESS = "http://localhost:9999/decoupled";
    private static final Duration ONE_DAY;
    private ObjectMessageContext objectCtx;
    private RMSource source;
    private RMHandler handler;
    private ConfigurationHelper configurationHelper;
    private AbstractBindingBase binding;
    private Transport transport;
    private HandlerChainInvoker hci;
    private SourcePolicyType sp;
    private IMocksControl control;
    private EndpointReferenceType target;
    private org.objectweb.celtix.ws.addressing.v200408.EndpointReferenceType acksTo;
    private RelatesToType relatesTo;
    
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
        handler = control.createMock(RMHandler.class);
        configurationHelper = control.createMock(ConfigurationHelper.class);
        binding = control.createMock(AbstractBindingBase.class);
        transport = control.createMock(Transport.class);
        hci = new HandlerChainInvoker(new ArrayList<Handler>());
        
        sp = RMUtils.getWSRMConfFactory().createSourcePolicyType();
        
        binding.createObjectContext();
        EasyMock.expectLastCall().andReturn(objectCtx);
        binding.createHandlerInvoker();
        EasyMock.expectLastCall().andReturn(hci);
        source.getHandler();
        EasyMock.expectLastCall().andReturn(handler);
        handler.getConfigurationHelper();
        EasyMock.expectLastCall().andReturn(configurationHelper);
        configurationHelper.getSourcePolicies();
        EasyMock.expectLastCall().andReturn(sp);
        
        target = TestUtils.getEPR("target");
        acksTo = VersionTransformer.convert(TestUtils.getEPR("acksTo"));
        relatesTo = ContextUtils.WSA_OBJECT_FACTORY.createRelatesToType();
        relatesTo.setValue("related");
    }
    
    public void testStatic() {
        control.reset();
        assertNotNull(CreateSequenceRequest.createDataBindingCallback());
        Method method = CreateSequenceRequest.getMethod();
        assertNotNull("expected method", method);
        assertEquals("expected method name",
                     "createSequence",
                     method.getName());
        assertEquals("unexpected operation name",
                     "CreateSequence",
                     CreateSequenceRequest.getOperationName());        
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
                                      target,
                                      acksTo,
                                      relatesTo);
        
        verifyContext();
        assertTrue("expected related request", req.isRelatedRequestExpected());
        
        Object[] params = req.getObjectMessageContext().getMessageObjects();
        assertEquals(1, params.length);
        CreateSequenceType cs = (CreateSequenceType)params[0];
        
        assertSame(acksTo, cs.getAcksTo());
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
                                      target,
                                      acksTo,
                                      relatesTo);
        
        verifyContext();
        assertTrue("expected related request", req.isRelatedRequestExpected());
        
        Object[] params = req.getObjectMessageContext().getMessageObjects();
        assertEquals(1, params.length);
        CreateSequenceType cs = (CreateSequenceType)params[0];
        
        assertEquals(NON_ANONYMOUS_ACKSTO_ADDRESS,
                     cs.getAcksTo().getAddress().getValue());
        assertEquals(ONE_DAY, cs.getExpires().getValue());
        assertNull(cs.getOffer());
        
        control.verify();
    }
    
    private void verifyContext() {
        EndpointReferenceType to =
            (EndpointReferenceType)objectCtx.get("org.objectweb.celtix.ws.addressing.to");
        assertNotNull("unexpectedly null To", to);
        assertSame("unexpected To",
                   target,
                   to);
        
        EndpointReferenceType replyTo =
            (EndpointReferenceType)objectCtx.get("org.objectweb.celtix.ws.addressing.replyto");
        assertNotNull("unexpectedly null ReplyTo", replyTo);
        assertEquals("unexpected ReplyTo",
                     acksTo.getAddress().getValue(),
                     replyTo.getAddress().getValue());
        
        Boolean usingAddressing = 
            (Boolean)objectCtx.get("org.objectweb.celtix.ws.addressing.using");
        assertNotNull("unexpectedly null usingAddressing", usingAddressing);
        assertTrue("expected usingAddressing", usingAddressing.booleanValue());
        
        assertTrue("expected requestor", objectCtx.isRequestorRole());

        AddressingProperties outMAPs =
            (AddressingProperties)objectCtx.get("javax.xml.ws.addressing.context");
        assertNotNull("unexpectedly null MAPs", outMAPs);
        assertEquals("unexpected action", 
                     "http://schemas.xmlsoap.org/ws/2005/02/rm/CreateSequence",
                     outMAPs.getAction().getValue());
        assertSame("expected RelatesTo set to messageID",
                   relatesTo,
                   outMAPs.getRelatesTo());
        
        Boolean isOneway = 
            (Boolean)objectCtx.get(OutputStreamMessageContext.ONEWAY_MESSAGE_TF);
        assertNotNull("unexpectedly null isOneway", isOneway);
        assertFalse("unexpected isOneway", isOneway.booleanValue());
    }
}
