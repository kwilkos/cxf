package org.objectweb.celtix.phase;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import junit.framework.TestCase;

import org.easymock.classextension.EasyMock;
import org.easymock.classextension.IMocksControl;
import org.objectweb.celtix.interceptors.Interceptor;
import org.objectweb.celtix.message.Message;

public class PhaseInterceptorChainTest extends TestCase {
    
    private IMocksControl control;
    private PhaseInterceptorChain chain;
    private Message message;
    
    public void setUp() {
        
        control = EasyMock.createNiceControl();
        message = control.createMock(Message.class);
        
        Phase phase1 = new Phase("phase1", 1);
        Phase phase2 = new Phase("phase2", 2);
        Phase phase3 = new Phase("phase3", 3);
        List<Phase> phases = new ArrayList<Phase>();
        phases.add(phase1);
        phases.add(phase2);
        phases.add(phase3);
        
        chain = new PhaseInterceptorChain(phases);
    }
    
    public void tearDown() {
        control.verify();
    }
    
    public void testAddOneInterceptor() {
        AbstractPhaseInterceptor p = setUpPhaseInterceptor("phase1", "p1"); 
        control.replay();
        chain.add(p);
        Iterator<Interceptor<? extends Message>> it = chain.iterator();
        assertSame(p, it.next());
        assertTrue(!it.hasNext());
    }
    
    @SuppressWarnings("unchecked")
    public void xtestAddTwoInterceptorsSamePhase() {
        AbstractPhaseInterceptor p1 = setUpPhaseInterceptor("phase1", "p1");
        Set<String> before = new HashSet<String>();
        before.add("p1");
        AbstractPhaseInterceptor p2 = setUpPhaseInterceptor("phase1", "p2", before);
        control.replay();
        chain.add(p1);
        chain.add(p2);
        Iterator<Interceptor<? extends Message>> it = chain.iterator();
       
        assertSame("Unexpected interceptor at this position.", p1, it.next());
        assertSame("Unexpected interceptor at this position.", p2, it.next());
        assertTrue(!it.hasNext());
    }
   
    
    public void testSingleInterceptorPass() {
        AbstractPhaseInterceptor p = setUpPhaseInterceptor("phase1", "p1");
        setUpPhaseInterceptorInvocations(p, false, false); 
        control.replay();
        chain.add(p);
        chain.doIntercept(message);
    }
    
    public void testSingleInterceptorFail() {
        AbstractPhaseInterceptor p = setUpPhaseInterceptor("phase1", "p1");
        setUpPhaseInterceptorInvocations(p, true, true); 
        control.replay();
        chain.add(p);
        chain.doIntercept(message);
    }
    
    public void testTwoInterceptorsInSamePhasePass() {
        AbstractPhaseInterceptor p1 = setUpPhaseInterceptor("phase1", "p1");
        setUpPhaseInterceptorInvocations(p1, false, false);
        AbstractPhaseInterceptor p2 = setUpPhaseInterceptor("phase1", "p2");
        setUpPhaseInterceptorInvocations(p2, false, false);
        control.replay();
        chain.add(p2);
        chain.add(p1);
        chain.doIntercept(message);
    }
    
    @SuppressWarnings("unchecked")
    public void testThreeInterceptorsInSamePhaseSecondFail() {
        AbstractPhaseInterceptor p1 = setUpPhaseInterceptor("phase1", "p1");
        setUpPhaseInterceptorInvocations(p1, false, true);
        AbstractPhaseInterceptor p2 = setUpPhaseInterceptor("phase1", "p2");
        setUpPhaseInterceptorInvocations(p2, true, true);
        AbstractPhaseInterceptor p3 = setUpPhaseInterceptor("phase1", "p3");
        control.replay();
        chain.add(p3);
        chain.add(p2);
        chain.add(p1);
        chain.doIntercept(message);
    }
    
    public void testTwoInterceptorsInSamePhaseSecondFail() {
        AbstractPhaseInterceptor p1 = setUpPhaseInterceptor("phase1", "p1");
        setUpPhaseInterceptorInvocations(p1, false, true);
        AbstractPhaseInterceptor p2 = setUpPhaseInterceptor("phase1", "p2");
        setUpPhaseInterceptorInvocations(p2, true, true);
        control.replay();
        chain.add(p2);
        chain.add(p1);
        chain.doIntercept(message);
    }
    
    public void testTwoInterceptorsInDifferentPhasesPass() {
        AbstractPhaseInterceptor p1 = setUpPhaseInterceptor("phase1", "p1");
        setUpPhaseInterceptorInvocations(p1, false, false);
        AbstractPhaseInterceptor p2 = setUpPhaseInterceptor("phase2", "p2");
        setUpPhaseInterceptorInvocations(p2, false, false);
        control.replay();
        chain.add(p1);
        chain.add(p2);
        chain.doIntercept(message);
    }
    
    public void testTwoInterceptorsInDifferentPhasesSecondFail() {
        AbstractPhaseInterceptor p1 = setUpPhaseInterceptor("phase1", "p1");
        setUpPhaseInterceptorInvocations(p1, false, true);
        AbstractPhaseInterceptor p2 = setUpPhaseInterceptor("phase2", "p2");
        setUpPhaseInterceptorInvocations(p2, true, true);
        control.replay();
        chain.add(p1);
        chain.add(p2);
        chain.doIntercept(message);
    }
    
    public void testInsertionInDifferentPhasePass() {
        
        AbstractPhaseInterceptor p2 = setUpPhaseInterceptor("phase2", "p2");
        setUpPhaseInterceptorInvocations(p2, false, false);
        AbstractPhaseInterceptor p3 = setUpPhaseInterceptor("phase3", "p3");
        setUpPhaseInterceptorInvocations(p3, false, false);
        InsertingPhaseInterceptor p1 = new InsertingPhaseInterceptor(chain, p2, "phase1", "p1");
        control.replay();
        chain.add(p3);
        chain.add(p1);
        chain.doIntercept(message);
        assertEquals(1, p1.invoked);
        assertEquals(0, p1.faultInvoked);
    }
    
    AbstractPhaseInterceptor setUpPhaseInterceptor(String phase, 
                                                   String id) {
        return setUpPhaseInterceptor(phase, id, null);     
    }
    
    @SuppressWarnings("unchecked")
    AbstractPhaseInterceptor setUpPhaseInterceptor(String phase, 
                                                   String id,
                                                   Set<String> b) {
        AbstractPhaseInterceptor p = control.createMock(AbstractPhaseInterceptor.class);
        EasyMock.expect(p.getPhase()).andReturn(phase).anyTimes();
        EasyMock.expect(p.getId()).andReturn(id).anyTimes();
        Set<String> before = null == b ? new HashSet<String>() : b;
        Set<String> after = new HashSet<String>();
        EasyMock.expect(p.getBefore()).andReturn(before).anyTimes();
        EasyMock.expect(p.getAfter()).andReturn(after).anyTimes();
        
        return p;
    }
    
    @SuppressWarnings("unchecked")
    void setUpPhaseInterceptorInvocations(AbstractPhaseInterceptor p,
                                          boolean fail,
                                          boolean expectFault) {
        p.handleMessage(message);
        if (fail) {
            EasyMock.expectLastCall().andThrow(new RuntimeException());
            message.setContent(EasyMock.isA(Class.class), EasyMock.isA(Exception.class));
            EasyMock.expectLastCall();
        } else {
            EasyMock.expectLastCall();
        } 
        if (expectFault) {
            p.handleFault(message);     
            EasyMock.expectLastCall();
        } 
    }

    /*
    public void testPhaseResolution() {
 
        Phase1Interceptor i1 = new Phase1Interceptor();
        i1.setId("i1");
        Phase2Interceptor i2 = new Phase2Interceptor();
        chain.add(i1);
        chain.add(i2);
        
        MessageImpl message = new MessageImpl();
        message.setInterceptorChain(chain);
        chain.doIntercept(message);
        
        assertEquals(1, i1.invoked);
        assertEquals(1, i2.invoked);
    }

    public void dontTestReversePhaseResolution() {
        Phase phase1 = new Phase("phase1", 2);
        Phase phase2 = new Phase("phase2", 1);
        
        List<Phase> phases = new ArrayList<Phase>();
        phases.add(phase1);
        phases.add(phase2);
        
        PhaseInterceptorChain chain = new PhaseInterceptorChain(phases);
        
        Phase1Interceptor i1 = new Phase1Interceptor();
        Phase2Interceptor i2 = new Phase2Interceptor();
        chain.add(i1);
        chain.add(i2);
        
        MessageImpl message = new MessageImpl();
        message.setInterceptorChain(chain);
        chain.doIntercept(message);
        
        assertEquals(0, i1.invoked);
        assertEquals(1, i2.invoked);
    }
    
    public void dontTestInterphaseResolution() {
        Phase phase1 = new Phase("phase1", 1);
        Phase phase2 = new Phase("phase2", 2);
        
        List<Phase> phases = new ArrayList<Phase>();
        phases.add(phase1);
        phases.add(phase2);
        
        PhaseInterceptorChain chain = new PhaseInterceptorChain(phases);
        
        Phase1Interceptor i1 = new Phase1Interceptor();
        i1.setId("i1");
        i1.addAfter("i2");
        Phase2Interceptor i2 = new Phase2Interceptor();
        i2.setPhase("phase1");
        i2.setId("i2");
        i2.addBefore("i1");
        
        chain.add(i1);
        chain.add(i2);
        
        MessageImpl message = new MessageImpl();
        message.setInterceptorChain(chain);
        chain.doIntercept(message);
        
        assertEquals(0, i1.invoked);
        assertEquals(1, i2.invoked);
        
        chain.doIntercept(message);
        assertEquals(1, i1.invoked);
        assertEquals(1, i2.invoked);
    }
    
    public class Phase1Interceptor extends AbstractPhaseInterceptor<Message> {
        int invoked;
        
        public Phase1Interceptor() {
            setPhase("phase1");
        }

        public void handleMessage(Message message) {
            invoked++;
            // message.getInterceptorChain().doIntercept(message);
        }
    }
    
    public class Phase2Interceptor extends AbstractPhaseInterceptor<Message> {
        int invoked;
        public Phase2Interceptor() {
            setPhase("phase2");
        }

        public void handleMessage(Message message) {
            invoked++;
        }
    }
    */
    
    public class InsertingPhaseInterceptor extends AbstractPhaseInterceptor<Message> {
        int invoked;
        int faultInvoked;
        private final PhaseInterceptorChain insertionChain;
        private final AbstractPhaseInterceptor insertionInterceptor;
        
        
        public InsertingPhaseInterceptor(PhaseInterceptorChain c,
                                         AbstractPhaseInterceptor i, 
                                         String phase, 
                                         String id) {
            setPhase(phase);
            setId(id);
            insertionChain = c;
            insertionInterceptor = i;
        }

        public void handleMessage(Message m) {
            insertionChain.add(insertionInterceptor);
            invoked++;
        }
        
        public void handleFault(Message m) {
            faultInvoked++;
        }
    }
    
}
