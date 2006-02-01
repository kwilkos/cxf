package org.objectweb.celtix.bus.ws.rm;

import java.math.BigInteger;

import junit.framework.TestCase;

import org.objectweb.celtix.configuration.Configuration;
import org.objectweb.celtix.ws.rm.Identifier;
import org.objectweb.celtix.ws.rm.SequenceType;

import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.classextension.EasyMock.createMock;
import static org.easymock.classextension.EasyMock.replay;
import static org.easymock.classextension.EasyMock.reset;
import static org.easymock.classextension.EasyMock.verify;

public class RMDestinationTest extends TestCase {
    
    private RMHandler handler;

    public void setUp() {
        handler = createMock(RMHandler.class);
    }

    public void testAddSequence() {
        RMDestination d = new RMDestination(handler);
        Identifier sid = d.generateSequenceIdentifier();
        Sequence seq = new Sequence(sid);
        d.addSequence(seq);
        assertSame(seq, d.getSequence(sid));
    }

    public void testGetDestinationPolicies() {
        Configuration c = createMock(Configuration.class);
        reset(handler);
        handler.getConfiguration();
        expectLastCall().andReturn(c);
        c.getObject("destinationPolicies");
        expectLastCall().andReturn(null);
        replay(handler);
        replay(c);

        RMDestination d = new RMDestination(handler);
        assertNull(d.getDestinationPolicies());
        verify(handler);
        verify(c);
    }

    public void testAcknowledge() {
        RMDestination d = new RMDestination(handler);
        Identifier sid = d.generateSequenceIdentifier();
        Sequence seq = new Sequence(sid);
        d.addSequence(seq);

        SequenceType st = RMUtils.getWSRMFactory().createSequenceType();
        st.setIdentifier(sid);
        BigInteger m = new BigInteger("3");
        st.setMessageNumber(m);
        d.acknowledge(st);
        assertTrue(seq.isAcknowledged(m));
        
        sid = d.generateSequenceIdentifier();
        st.setIdentifier(sid);
        d.acknowledge(st);
    }

}
