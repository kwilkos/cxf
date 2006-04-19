package org.objectweb.celtix.bus.ws.rm;

import java.math.BigInteger;
import java.util.Collection;
import java.util.Iterator;

import junit.framework.TestCase;

import org.easymock.classextension.IMocksControl;
import org.objectweb.celtix.ws.rm.Identifier;
import org.objectweb.celtix.ws.rm.SequenceAcknowledgement;
import org.objectweb.celtix.ws.rm.SequenceType;

import static org.easymock.classextension.EasyMock.*;

public class RMPropertiesImplTest extends TestCase {

    public void testSetSequence() {
        RMPropertiesImpl rmps = new RMPropertiesImpl();
        IMocksControl control = createNiceControl();
        
        SourceSequence seq = control.createMock(SourceSequence.class);
        Identifier sid = control.createMock(Identifier.class);
        seq.getIdentifier();
        expectLastCall().andReturn(sid);
        seq.getCurrentMessageNr();
        expectLastCall().andReturn(BigInteger.TEN);
        seq.isLastMessage();
        expectLastCall().andReturn(false);
        
        control.replay();
        rmps.setSequence(seq);
        control.verify();
        
        SequenceType s = rmps.getSequence();
        assertNotNull(s);
        assertSame(s.getIdentifier(), sid);
        assertEquals(s.getMessageNumber(), BigInteger.TEN);
        assertNull(s.getLastMessage());
        
        control.reset();
        
        seq.getIdentifier();
        expectLastCall().andReturn(sid);
        seq.getCurrentMessageNr();
        expectLastCall().andReturn(BigInteger.TEN);
        seq.isLastMessage();
        expectLastCall().andReturn(true);
        
        control.replay();
        rmps.setSequence(seq);
        control.verify();
        
        s = rmps.getSequence();
        assertNotNull(s);
        assertSame(s.getIdentifier(), sid);
        assertEquals(s.getMessageNumber(), BigInteger.TEN);
        assertNotNull(s.getLastMessage());
    }
    
    public void testAddAck() {
        RMPropertiesImpl rmps = new RMPropertiesImpl();
        IMocksControl control = createNiceControl();
        
        DestinationSequence seq = control.createMock(DestinationSequence.class);
        SequenceAcknowledgement ack = control.createMock(SequenceAcknowledgement.class);
        seq.getAcknowledgment();
        expectLastCall().andReturn(ack);
        
        DestinationSequence otherSeq = control.createMock(DestinationSequence.class);
        SequenceAcknowledgement otherAck = control.createMock(SequenceAcknowledgement.class);
        otherSeq.getAcknowledgment();
        expectLastCall().andReturn(otherAck);
        
        control.replay();
        
        rmps.addAck(seq);
        rmps.addAck(otherSeq);
        
        control.verify();
        
        Collection<SequenceAcknowledgement> acks = rmps.getAcks();
        assertEquals(2, acks.size());
        Iterator<SequenceAcknowledgement> it = acks.iterator();
        assertSame(ack, it.next());
        assertSame(otherAck, it.next());          
    }
}
