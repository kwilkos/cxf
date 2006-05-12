package org.objectweb.celtix.bus.ws.rm;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

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
import org.objectweb.celtix.ws.rm.AckRequestedType;
import org.objectweb.celtix.ws.rm.Identifier;
import org.objectweb.celtix.ws.rm.RMProperties;
import org.objectweb.celtix.ws.rm.persistence.RMDestinationSequence;

public class SequenceInfoRequestTest extends TestCase {

    private ObjectMessageContext objectCtx; 
    private RMSource source;
    private AbstractBindingBase binding;
    private Transport transport;
    private EndpointReferenceType target;

    public void setUp() {
        objectCtx = new ObjectMessageContextImpl(); 
        source = EasyMock.createMock(RMSource.class);
        binding = EasyMock.createMock(AbstractBindingBase.class);
        transport = EasyMock.createMock(Transport.class);
        HandlerChainInvoker hci = new HandlerChainInvoker(new ArrayList<Handler>());
        
        target = TestUtils.getEPR("target");
                        
        binding.createObjectContext();
        EasyMock.expectLastCall().andReturn(objectCtx);
        binding.createHandlerInvoker();
        EasyMock.expectLastCall().andReturn(hci);
    }
    
    public void testConstruction() {
        EasyMock.replay(source);
        EasyMock.replay(binding);
        
        SequenceInfoRequest req =
            new SequenceInfoRequest(binding, transport, target);
        
        verifyContext("http://celtix.objectweb.org/ws/rm/SequenceInfo");
        assertFalse("unexpected related request",
                    req.isRelatedRequestExpected());

        Object[] params = req.getObjectMessageContext().getMessageObjects();
        assertNull(params);
        
        EasyMock.verify(source);
        EasyMock.verify(binding);
    }

    public void testAcknowledge() {
        EasyMock.replay(source);
        EasyMock.replay(binding);
        
        SequenceInfoRequest req =
            new SequenceInfoRequest(binding, transport, target);
            
        Identifier sid = RMUtils.getWSRMFactory().createIdentifier();
        sid.setValue("Sequence1");
        org.objectweb.celtix.ws.addressing.v200408.EndpointReferenceType acksTo =
            TestUtils.getOldEPR("acks");
        RMDestinationSequence destSequence =
            new DestinationSequence(sid, acksTo, null);
        
        req.acknowledge(destSequence);
                    
        verifyContext("http://schemas.xmlsoap.org/ws/2005/02/rm/SequenceAcknowledgement",
                      acksTo.getAddress().getValue());
        assertFalse("unexpected related request",
                    req.isRelatedRequestExpected());
              
        EasyMock.verify(source);
        EasyMock.verify(binding);
    }

    public void testRequestAcknowledgement() {
        EasyMock.replay(source);
        EasyMock.replay(binding);
        
        SequenceInfoRequest req =
            new SequenceInfoRequest(binding, transport, target);
            
        Collection<SourceSequence> seqs = new ArrayList<SourceSequence>();
        Identifier sid1 = RMUtils.getWSRMFactory().createIdentifier();
        sid1.setValue("Sequence1");
        SourceSequence sourceSequence1 = new SourceSequence(sid1, null, null);
        seqs.add(sourceSequence1);
        Identifier sid2 = RMUtils.getWSRMFactory().createIdentifier();
        sid2.setValue("Sequence2");
        SourceSequence sourceSequence2 = new SourceSequence(sid2, null, null);
        seqs.add(sourceSequence2);
        
        req.requestAcknowledgement(seqs);
        
        RMProperties props =
            (RMProperties)objectCtx.get("org.objectweb.celtix.ws.rm.context.outbound");
        assertNotNull("expected RM properties", props);
        Collection<AckRequestedType> acksRequested = props.getAcksRequested();
        assertNotNull("expected acks requested", acksRequested);
        assertEquals("expected acks requested", 2, acksRequested.size());
        Iterator<AckRequestedType> i = acksRequested.iterator();
        assertTrue("expected ack requested", i.hasNext());
        assertEquals("unexpected identifier",
                     sid1.getValue(),
                     i.next().getIdentifier().getValue());
        assertTrue("expected ack requested", i.hasNext());
        assertEquals("unexpected identifier",
                     sid2.getValue(),
                     i.next().getIdentifier().getValue());
        assertFalse("unexpected ack requested", i.hasNext());
            
        verifyContext("http://celtix.objectweb.org/ws/rm/SequenceInfo");
        assertFalse("unexpected related request",
                    req.isRelatedRequestExpected());
              
        EasyMock.verify(source);
        EasyMock.verify(binding);
    }

    public void testLastMessage() {
        EasyMock.replay(source);
        EasyMock.replay(binding);
        
        SequenceInfoRequest req =
            new SequenceInfoRequest(binding, transport, target);
                
        Identifier sid = RMUtils.getWSRMFactory().createIdentifier();
        sid.setValue("Sequence1");
        SourceSequence sourceSequence = new SourceSequence(sid, null, null);

        req.lastMessage(sourceSequence);
        
        RMProperties props =
            (RMProperties)objectCtx.get("org.objectweb.celtix.ws.rm.context.outbound");
        assertNotNull("expected RM properties", props);
        assertEquals("unexpected identifier",
                     sid.getValue(),
                     props.getSequence().getIdentifier().getValue());
        assertNotNull("expected last message", props.getSequence().getLastMessage());
        
        verifyContext("http://schemas.xmlsoap.org/ws/2005/02/rm/LastMessage");
        assertFalse("unexpected related request",
                    req.isRelatedRequestExpected());
              
        EasyMock.verify(source);
        EasyMock.verify(binding);
    }

    private void verifyContext(String action) {
        verifyContext(action, null);
    }
    
    private void verifyContext(String action, String mapsTo) {
        EndpointReferenceType to =
            (EndpointReferenceType)objectCtx.get("org.objectweb.celtix.ws.addressing.to");
        assertNotNull("unexpectedly null To", to);
        assertSame("unexpected context To", target, to);
        
        EndpointReferenceType replyTo =
            (EndpointReferenceType)objectCtx.get("org.objectweb.celtix.ws.addressing.replyto");
        assertNull("expected null context ReplyTo", replyTo);
        
        Boolean usingAddressing = 
            (Boolean)objectCtx.get("org.objectweb.celtix.ws.addressing.using");
        assertNotNull("unexpectedly null usingAddressing", usingAddressing);
        assertTrue("expected usingAddressing", usingAddressing.booleanValue());
        
        assertTrue("expected requestor", objectCtx.isRequestorRole());
        
        AddressingProperties outMAPs =
            (AddressingProperties)objectCtx.get("javax.xml.ws.addressing.context");
        assertNotNull("unexpectedly null MAPs", outMAPs);
        assertEquals("unexpected action", 
                     action,
                     outMAPs.getAction().getValue());
        if (mapsTo != null) {
            assertEquals("unexpected MAPs To", 
                         mapsTo,
                         outMAPs.getTo().getValue());            
        }
                
        Boolean isOneway = 
            (Boolean)objectCtx.get(OutputStreamMessageContext.ONEWAY_MESSAGE_TF);
        assertNotNull("unexpectedly null isOneway", isOneway);
        assertTrue("expected isOneway", isOneway.booleanValue());
    }
}