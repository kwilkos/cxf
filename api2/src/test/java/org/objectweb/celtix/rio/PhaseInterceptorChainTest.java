package org.objectweb.celtix.rio;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.objectweb.celtix.rio.message.MessageImpl;
import org.objectweb.celtix.rio.phase.AbstractPhaseInterceptor;
import org.objectweb.celtix.rio.phase.Phase;
import org.objectweb.celtix.rio.phase.PhaseInterceptorChain;

public class PhaseInterceptorChainTest extends TestCase {

    public void testPhaseResolution() {
        Phase phase1 = new Phase("phase1", 1);
        Phase phase2 = new Phase("phase2", 2);
        
        List<Phase> phases = new ArrayList<Phase>();
        phases.add(phase1);
        phases.add(phase2);
        
        PhaseInterceptorChain chain = new PhaseInterceptorChain(phases);
        
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

    public void testReversePhaseResolution() {
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
    
    public void testInterphaseResolution() {
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
    
    public class Phase1Interceptor extends AbstractPhaseInterceptor {
        int invoked;
        
        public Phase1Interceptor() {
            setPhase("phase1");
        }

        public void intercept(Message message) {
            invoked++;
            message.getInterceptorChain().doIntercept(message);
        }
    }
    
    public class Phase2Interceptor extends AbstractPhaseInterceptor {
        int invoked;
        public Phase2Interceptor() {
            setPhase("phase2");
        }

        public void intercept(Message message) {
            invoked++;
        }
    }
}
