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
        
        Sequence seq = control.createMock(Sequence.class);
        Identifier sid = control.createMock(Identifier.class);
        seq.getIdentifier();
        expectLastCall().andReturn(sid);
        seq.getCurrentMessageNumber();
        expectLastCall().andReturn(BigInteger.TEN).times(2);
        seq.getLastMessageNumber();
        expectLastCall().andReturn(null);
        
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
        seq.getCurrentMessageNumber();
        expectLastCall().andReturn(BigInteger.TEN).times(2);
        seq.getLastMessageNumber();
        expectLastCall().andReturn(BigInteger.TEN);
        
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
        
        Sequence seq = control.createMock(Sequence.class);
        SequenceAcknowledgement ack = control.createMock(SequenceAcknowledgement.class);
        seq.getAcknowledged();
        expectLastCall().andReturn(ack);
        
        Sequence otherSeq = control.createMock(Sequence.class);
        SequenceAcknowledgement otherAck = control.createMock(SequenceAcknowledgement.class);
        otherSeq.getAcknowledged();
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
