package org.objectweb.celtix.bus.ws.rm;

import java.lang.reflect.Method;
import java.util.ArrayList;

import javax.xml.ws.handler.Handler;

import junit.framework.TestCase;

import org.easymock.classextension.EasyMock;
import org.objectweb.celtix.bindings.AbstractBindingBase;
import org.objectweb.celtix.bus.handlers.HandlerChainInvoker;
import org.objectweb.celtix.context.ObjectMessageContext;
import org.objectweb.celtix.context.ObjectMessageContextImpl;
import org.objectweb.celtix.context.OutputStreamMessageContext;
import org.objectweb.celtix.transports.Transport;
import org.objectweb.celtix.ws.addressing.AddressingProperties;
import org.objectweb.celtix.ws.addressing.EndpointReferenceType;
import org.objectweb.celtix.ws.rm.Identifier;
import org.objectweb.celtix.ws.rm.TerminateSequenceType;

public class TerminateSequenceRequestTest extends TestCase {

    private ObjectMessageContext objectCtx; 
    private RMSource source;
    private AbstractBindingBase binding;
    private Transport transport;
    private Identifier sid;
    private SourceSequence seq;
    private EndpointReferenceType target;

    public void setUp() {
        objectCtx = new ObjectMessageContextImpl(); 
        source = EasyMock.createMock(RMSource.class);
        binding = EasyMock.createMock(AbstractBindingBase.class);
        transport = EasyMock.createMock(Transport.class);
        HandlerChainInvoker hci = new HandlerChainInvoker(new ArrayList<Handler>());
        
        target = TestUtils.getEPR("target");
        
        sid = RMUtils.getWSRMFactory().createIdentifier();
        sid.setValue("TerminatedSequence");
        seq = new SourceSequence(sid, null, null);
        seq.setTarget(target);
                
        binding.createObjectContext();
        EasyMock.expectLastCall().andReturn(objectCtx);
        binding.createHandlerInvoker();
        EasyMock.expectLastCall().andReturn(hci);
    }
    
    public void testStatic() {
        EasyMock.reset();
        assertNotNull(TerminateSequenceRequest.createDataBindingCallback());
        Method method = TerminateSequenceRequest.getMethod();
        assertNotNull("expected method", method);
        assertEquals("expected method name",
                     "terminateSequence",
                     method.getName());
        assertEquals("unexpected operation name",
                     "TerminateSequence",
                     TerminateSequenceRequest.getOperationName());        
    }

    public void testConstruction() {
        EasyMock.replay(source);
        EasyMock.replay(binding);
        
        TerminateSequenceRequest req =
            new TerminateSequenceRequest(binding, transport, seq);
        
        verifyContext();
        assertFalse("unexpected related request",
                    req.isRelatedRequestExpected());
                
        Object[] params = req.getObjectMessageContext().getMessageObjects();
        assertEquals(1, params.length);
        TerminateSequenceType ts = (TerminateSequenceType)params[0];
        
        assertEquals(sid, ts.getIdentifier());
      
        EasyMock.verify(source);
        EasyMock.verify(binding);
    }
    
    private void verifyContext() {
        EndpointReferenceType to =
            (EndpointReferenceType)objectCtx.get("org.objectweb.celtix.ws.addressing.to");
        assertNotNull("unexpectedly null To", to);
        assertSame("unexpected To", target, to);
        
        EndpointReferenceType replyTo =
            (EndpointReferenceType)objectCtx.get("org.objectweb.celtix.ws.addressing.replyto");
        assertNull("expected null ReplyTo", replyTo);
        
        Boolean usingAddressing = 
            (Boolean)objectCtx.get("org.objectweb.celtix.ws.addressing.using");
        assertNotNull("unexpectedly null usingAddressing", usingAddressing);
        assertTrue("expected usingAddressing", usingAddressing.booleanValue());
        
        assertTrue("expected requestor", objectCtx.isRequestorRole());
        
        AddressingProperties outMAPs =
            (AddressingProperties)objectCtx.get("javax.xml.ws.addressing.context");
        assertNotNull("unexpectedly null MAPs", outMAPs);
        assertEquals("unexpected action", 
                     "http://schemas.xmlsoap.org/ws/2005/02/rm/TerminateSequence",
                     outMAPs.getAction().getValue());
                
        Boolean isOneway = 
            (Boolean)objectCtx.get(OutputStreamMessageContext.ONEWAY_MESSAGE_TF);
        assertNotNull("unexpectedly null isOneway", isOneway);
        assertTrue("expected isOneway", isOneway.booleanValue());
    }
}
