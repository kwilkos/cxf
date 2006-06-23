package org.objectweb.celtix.jbi.se.state;

import javax.jbi.JBIException;

import junit.framework.TestCase;

import org.objectweb.celtix.jbi.se.state.ServiceEngineStateMachine.SEOperation;

public class ServiceEngineStartTest extends TestCase {
    private ServiceEngineStateFactory stateFactory;
    private ServiceEngineStateMachine start;
    
    public void setUp() throws Exception {
        stateFactory = ServiceEngineStateFactory.getInstance();
        start = stateFactory.getStartState();
    }
    
    public void testStopOperation() throws Exception {
        start.changeState(SEOperation.stop, null);
        assertTrue(stateFactory.getCurrentState() instanceof ServiceEngineStop);
    }
    
    public void testStartOperation() throws Exception {
        try {
            start.changeState(SEOperation.start, null);
        } catch (JBIException e) {
            return;
        }
        fail();
    }
    
    public void testInitOperation() throws Exception {
        try {
            start.changeState(SEOperation.init, null);
        } catch (JBIException e) {
            return;
        }
        fail();
    }
    
    public void testShutdownOperation() throws Exception {
        try {
            start.changeState(SEOperation.shutdown, null);
        } catch (JBIException e) {
            return;
        }
        fail();
    }
}
