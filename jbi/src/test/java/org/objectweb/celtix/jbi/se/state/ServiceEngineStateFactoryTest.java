package org.objectweb.celtix.jbi.se.state;

import junit.framework.TestCase;

import org.objectweb.celtix.jbi.se.state.ServiceEngineStateMachine.SEOperation;

public class ServiceEngineStateFactoryTest extends TestCase {
    private ServiceEngineStateFactory stateFactory;
    private ServiceEngineStateMachine state;
        
    public void setUp() throws Exception {
        stateFactory = ServiceEngineStateFactory.getInstance();
        state = stateFactory.getShutdownState();
    }
    
    public void testSinglton() throws Exception {
        assertSame(stateFactory, 
                   ServiceEngineStateFactory.getInstance());
    }
    
    public void testLifeCycle() throws Exception {
        stateFactory.setCurrentState(state);
        assertSame(state, stateFactory.getCurrentState());
        assertTrue(stateFactory.getCurrentState() instanceof ServiceEngineShutdown);
        state.changeState(SEOperation.init, null);
        state = stateFactory.getCurrentState();
        assertTrue(state instanceof ServiceEngineStop);
        
        state.changeState(SEOperation.start, null);
        state = stateFactory.getCurrentState();
        assertTrue(state instanceof ServiceEngineStart);
        

        state.changeState(SEOperation.stop, null);
        state = stateFactory.getCurrentState();
        assertTrue(state instanceof ServiceEngineStop);
        
        state.changeState(SEOperation.shutdown, null);
        state = stateFactory.getCurrentState();
        assertTrue(state instanceof ServiceEngineShutdown);
    }
}
