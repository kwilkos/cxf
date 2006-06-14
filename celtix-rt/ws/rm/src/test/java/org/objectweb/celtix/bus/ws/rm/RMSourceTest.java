package org.objectweb.celtix.bus.ws.rm;

import java.io.IOException;
import java.math.BigInteger;

import junit.framework.TestCase;

import org.easymock.classextension.EasyMock;
import org.easymock.classextension.IMocksControl;
import org.objectweb.celtix.ws.rm.Identifier;
import org.objectweb.celtix.ws.rm.SequenceAcknowledgement;
import org.objectweb.celtix.ws.rm.SequenceAcknowledgement.AcknowledgementRange;
import org.objectweb.celtix.ws.rm.persistence.RMStore;
import org.objectweb.celtix.ws.rm.wsdl.SequenceFault;

import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;

public class RMSourceTest extends TestCase {

    private RMHandler handler;
    private RMSource s;
    private IMocksControl control;

    public void setUp() {
        control = EasyMock.createNiceControl();
        handler = control.createMock(RMHandler.class);
        s = new RMSource(handler);
    }

    public void testRMSourceConstructor() {
        assertNull(s.getCurrent());
    }

    public void testSequenceAccess() throws IOException, SequenceFault {
        RMStore store = control.createMock(RMStore.class);
        expect(handler.getStore()).andReturn(store).times(3);
        store.createSourceSequence(EasyMock.isA(SourceSequence.class));
        expectLastCall().times(2);
        store.removeSourceSequence(EasyMock.isA(Identifier.class));

        control.replay();
        Identifier sid = s.generateSequenceIdentifier();
        SourceSequence seq = new SourceSequence(sid);
        assertNull(s.getCurrent());
        s.addSequence(seq);
        assertNotNull(s.getSequence(sid));
        assertNull(s.getCurrent());
        SourceSequence anotherSeq = new SourceSequence(s.generateSequenceIdentifier());
        s.addSequence(anotherSeq);
        assertNull(s.getCurrent());
        assertEquals(2, s.getAllSequences().size());
        s.removeSequence(seq);
        assertEquals(1, s.getAllSequences().size());
        assertNull(s.getSequence(sid));
        control.verify();
    }

    public void testCurrent() {
        Identifier sid = s.generateSequenceIdentifier();
        SourceSequence seq = new SourceSequence(sid);
        assertNull(s.getCurrent());
        Identifier inSid = s.generateSequenceIdentifier();
        assertNull(s.getCurrent(inSid));
        s.setCurrent(seq);
        assertNotNull(s.getCurrent());
        assertSame(seq, s.getCurrent());
        assertNull(s.getCurrent(inSid));
        s.setCurrent(inSid, seq);
        assertNotNull(s.getCurrent(inSid));
        assertSame(seq, s.getCurrent(inSid));
        assertNull(s.getCurrent(sid));
    }



    public void testAwaitCurrentRequestor() throws Exception {
        if (System.getProperty("java.vendor").indexOf("IBM") > -1) {
            return;
        }
        Identifier sid = s.generateSequenceIdentifier();
        SequenceAccessor accessor1 = new SequenceAccessor(null);
        Thread t1 = new Thread(accessor1);
        t1.start();
        yield();
        assertEquals("expected blocked accessor thread",
                     Thread.State.WAITING,
                     t1.getState());

        SourceSequence seq = new SourceSequence(sid);
        s.setCurrent(seq);
        yield();
        assertTrue("unexpected blocked accessor thread",
                   Thread.State.RUNNABLE.equals(t1.getState())
                   || Thread.State.TERMINATED.equals(t1.getState()));
        assertSame("unexpected sequence",
                   seq,
                   accessor1.getSequence());
        assertSame("unexpected sequence",
                   seq,
                   s.getCurrent());

        SequenceAccessor accessor2 = new SequenceAccessor(null);
        Thread t2 = new Thread(accessor2);
        t2.start();
        yield();
        assertTrue("unexpected blocked accessor thread",
                   Thread.State.RUNNABLE.equals(t2.getState())
                   || Thread.State.TERMINATED.equals(t2.getState()));
        assertSame("unexpected sequence",
                   seq,
                   accessor2.getSequence());
        assertSame("unexpected sequence",
                   seq,
                   s.getCurrent());
    }

    public void testAwaitCurrentResponder() throws Exception {
        if (System.getProperty("java.vendor").indexOf("IBM") > -1) {
            return;
        }
        Identifier sid = s.generateSequenceIdentifier();
        SequenceAccessor accessor1 = new SequenceAccessor(sid);
        Thread t1 = new Thread(accessor1);
        t1.start();
        yield();
        assertEquals("expected blocked accessor thread",
                     Thread.State.WAITING,
                     t1.getState());

        SourceSequence seq = new SourceSequence(sid);
        s.setCurrent(sid, seq);
        yield();
        assertTrue("unexpected blocked accessor thread",
                   Thread.State.RUNNABLE.equals(t1.getState())
                   || Thread.State.TERMINATED.equals(t1.getState()));
        assertSame("unexpected sequence",
                   seq,
                   accessor1.getSequence());
        assertSame("unexpected sequence",
                   seq,
                   s.getCurrent(sid));

        SequenceAccessor accessor2 = new SequenceAccessor(sid);
        Thread t2 = new Thread(accessor2);
        t2.start();
        yield();
        assertTrue("unexpected blocked accessor thread",
                   Thread.State.RUNNABLE.equals(t2.getState())
                   || Thread.State.TERMINATED.equals(t2.getState()));
        assertSame("unexpected sequence",
                   seq,
                   accessor2.getSequence());
        assertSame("unexpected sequence",
                   seq,
                   s.getCurrent(sid));
    }



    public void testSetAcknowledgedUnknownSequence() {
        Identifier sid = s.generateSequenceIdentifier();
        SequenceAcknowledgement ack = RMUtils.getWSRMFactory().createSequenceAcknowledgement();
        ack.setIdentifier(sid);
        AcknowledgementRange range =
            RMUtils.getWSRMFactory().createSequenceAcknowledgementAcknowledgementRange();
        range.setLower(BigInteger.ONE);
        range.setUpper(BigInteger.ONE);
        ack.getAcknowledgementRange().add(range);
        s.setAcknowledged(ack);
    }

    public void testSetAcknowledged() throws NoSuchMethodException, IOException {
        Identifier sid = s.generateSequenceIdentifier();
        SourceSequence seq = new SourceSequence(sid, null, null, BigInteger.TEN, true);

        RMStore store = control.createMock(RMStore.class);
        expect(handler.getStore()).andReturn(store);
        store.createSourceSequence(EasyMock.isA(SourceSequence.class));
        expectLastCall();
        PersistenceManager pm = control.createMock(PersistenceManager.class);
        expect(handler.getPersistenceManager()).andReturn(pm);
        RetransmissionQueue rtq = control.createMock(RetransmissionQueue.class);
        expect(pm.getQueue()).andReturn(rtq);
        rtq.purgeAcknowledged(seq);
        expectLastCall();

        control.replay();

        seq.setSource(s);
        s.addSequence(seq);

        SequenceAcknowledgement ack = RMUtils.getWSRMFactory().createSequenceAcknowledgement();
        ack.setIdentifier(sid);
        AcknowledgementRange range =
            RMUtils.getWSRMFactory().createSequenceAcknowledgementAcknowledgementRange();
        range.setLower(BigInteger.ONE);
        range.setUpper(BigInteger.ONE);
        ack.getAcknowledgementRange().add(range);
        s.setAcknowledged(ack);
        assertSame(ack, seq.getAcknowledgement());

        control.verify();
    }

    public void testSetAcknowledgedLastMessageTerminationSucceeds() throws IOException {
        doTestSetAcknowledgedLastMessage(false);
    }

    public void testSetAcknowledgedLastMessageTerminationFails() throws IOException {
        doTestSetAcknowledgedLastMessage(true);
    }

    public void doTestSetAcknowledgedLastMessage(boolean doThrow) throws IOException {

        Identifier sid = s.generateSequenceIdentifier();
        SourceSequence seq = new SourceSequence(sid, null, null, BigInteger.TEN, true);

        RMStore store = control.createMock(RMStore.class);
        expect(handler.getStore()).andReturn(store);
        store.createSourceSequence(EasyMock.isA(SourceSequence.class));
        PersistenceManager pm = control.createMock(PersistenceManager.class);
        expect(handler.getPersistenceManager()).andReturn(pm);
        RetransmissionQueue rtq = control.createMock(RetransmissionQueue.class);
        expect(pm.getQueue()).andReturn(rtq);
        rtq.purgeAcknowledged(EasyMock.isA(SourceSequence.class));
        expectLastCall();
        RMProxy proxy = control.createMock(RMProxy.class);
        handler.getProxy();
        expectLastCall().andReturn(proxy);
        proxy.terminateSequence(seq);
        if (doThrow) {
            expectLastCall().andThrow(new IOException("can't terminate sequence"));
        } else {
            expectLastCall();
        }

        control.replay();

        seq.setSource(s);
        s.addSequence(seq);
        SequenceAcknowledgement ack = RMUtils.getWSRMFactory().createSequenceAcknowledgement();
        ack.setIdentifier(sid);
        AcknowledgementRange range =
            RMUtils.getWSRMFactory().createSequenceAcknowledgementAcknowledgementRange();
        range.setLower(BigInteger.ONE);
        range.setUpper(BigInteger.TEN);
        ack.getAcknowledgementRange().add(range);

        s.setAcknowledged(ack);
        assertSame(ack, seq.getAcknowledgement());

        control.verify();
    }

}
