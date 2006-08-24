package org.apache.cxf.phase;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import junit.framework.TestCase;

import org.apache.cxf.interceptors.Interceptor;
import org.apache.cxf.message.Message;

import org.easymock.classextension.EasyMock;
import org.easymock.classextension.IMocksControl;

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
    public void testAddTwoInterceptorsSamePhase() {
        AbstractPhaseInterceptor p1 = setUpPhaseInterceptor("phase1", "p1");
        Set<String> after = new HashSet<String>();
        after.add("p1");
        AbstractPhaseInterceptor p2 = setUpPhaseInterceptor("phase1", "p2", after);
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
    
    public void testInsertionInSamePhasePass() {
        
        AbstractPhaseInterceptor p2 = setUpPhaseInterceptor("phase1", "p2");
        setUpPhaseInterceptorInvocations(p2, false, false);
        Set<String> after3 = new HashSet<String>();
        after3.add("p2");
        AbstractPhaseInterceptor p3 = setUpPhaseInterceptor("phase1", "p3", after3);
        setUpPhaseInterceptorInvocations(p3, false, false);
        InsertingPhaseInterceptor p1 = new InsertingPhaseInterceptor(chain, p3, "phase1", "p1");
        p1.addBefore("p2");
        control.replay();   
        chain.add(p1);
        chain.add(p2);
        chain.doIntercept(message);
        assertEquals(1, p1.invoked);
        assertEquals(0, p1.faultInvoked);
    }
    
    public void testWrappedInvokation() throws Exception {
        CountingPhaseInterceptor p1 = new CountingPhaseInterceptor("phase1", "p1");
        WrapperingPhaseInterceptor p2 = new WrapperingPhaseInterceptor("phase2", "p2");
        CountingPhaseInterceptor p3 = new CountingPhaseInterceptor("phase3", "p3");
        
        message.getInterceptorChain();
        EasyMock.expectLastCall().andReturn(chain).anyTimes();
        
        control.replay();   
        chain.add(p1);
        chain.add(p2);
        chain.add(p3);
        chain.doIntercept(message);
        assertEquals(1, p1.invoked);
        assertEquals(1, p2.invoked);
        assertEquals(1, p3.invoked);
    }
    
    AbstractPhaseInterceptor setUpPhaseInterceptor(String phase, 
                                                   String id) {
        return setUpPhaseInterceptor(phase, id, null);     
    }
    
    @SuppressWarnings("unchecked")
    AbstractPhaseInterceptor setUpPhaseInterceptor(String phase, 
                                                   String id,
                                                   Set<String> a) {
        AbstractPhaseInterceptor p = control.createMock(AbstractPhaseInterceptor.class);
        EasyMock.expect(p.getPhase()).andReturn(phase).anyTimes();
        EasyMock.expect(p.getId()).andReturn(id).anyTimes();
        Set<String> before = new HashSet<String>();
        Set<String> after = null == a ? new HashSet<String>() : a;
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
    public class CountingPhaseInterceptor extends AbstractPhaseInterceptor<Message> {
        int invoked;
        public CountingPhaseInterceptor(String phase, 
                                         String id) {
            setPhase(phase);
            setId(id);
        }
        public void handleMessage(Message m) {
            invoked++;
        }
    }
    public class WrapperingPhaseInterceptor extends CountingPhaseInterceptor {
        public WrapperingPhaseInterceptor(String phase, 
                                         String id) {
            super(phase, id);
        }
        public void handleMessage(Message m) {
            super.handleMessage(m);
            m.getInterceptorChain().doIntercept(m);
        }
    }
}
