package org.objectweb.celtix.jbi.se.state;

public final class ServiceEngineStateFactory {

   
    
    private static ServiceEngineStateFactory factory;
    private ServiceEngineStateMachine currentState;
    
    private ServiceEngineStateMachine stop = new ServiceEngineStop();
    private ServiceEngineStateMachine start = new ServiceEngineStart();
    private ServiceEngineStateMachine shutdown = new ServiceEngineShutdown();
    
    private ServiceEngineStateFactory() {
        
    }
      
        
    public static synchronized ServiceEngineStateFactory getInstance() {
        if (factory == null) {
            factory = new ServiceEngineStateFactory();
        }
        return factory;
    }
    
    public ServiceEngineStateMachine getCurrentState() {
        return currentState;
    }
    
    public void setCurrentState(ServiceEngineStateMachine state) {
        currentState = state;
    }
    
    public ServiceEngineStateMachine getShutdownState() {
        return shutdown; 
    }
    
    public ServiceEngineStateMachine getStopState() {
        return stop; 
    }
    
    public ServiceEngineStateMachine getStartState() {
        return start; 
    }
}
