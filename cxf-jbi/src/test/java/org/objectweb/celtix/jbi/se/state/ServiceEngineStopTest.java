package org.objectweb.celtix.jbi.se.state;

import javax.jbi.JBIException;

import junit.framework.TestCase;

import org.objectweb.celtix.jbi.se.state.ServiceEngineStateMachine.SEOperation;

public class ServiceEngineStopTest extends TestCase {
    private ServiceEngineStateFactory stateFactory;
    private ServiceEngineStateMachine stop;
    
    public void setUp() throws Exception {
        stateFactory = ServiceEngineStateFactory.getInstance();
        stop = stateFactory.getStopState();
    }
    
    public void testStartOperation() throws Exception {
        stop.changeState(SEOperation.start, null);
        assertTrue(stateFactory.getCurrentState() instanceof ServiceEngineStart);
    }
    
    public void testShutdownOperation() throws Exception {
        stop.changeState(SEOperation.shutdown, null);
        assertTrue(stateFactory.getCurrentState() instanceof ServiceEngineShutdown);
    }
    
    public void testStopOperation() throws Exception {
        try {
            stop.changeState(SEOperation.stop, null);
        } catch (JBIException e) {
            return;
        }
        fail();
    }
    
    public void testInitOperation() throws Exception {
        try {
            stop.changeState(SEOperation.init, null);
        } catch (JBIException e) {
            return;
        }
        fail();
    }
}
