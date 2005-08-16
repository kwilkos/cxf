package org.objectweb.celtix.application;

import org.objectweb.celtix.plugins.PluginException;


public class PluginStateMachine {
    
    public enum PluginState { UNLOADED, LOADING, LOADED };

    private PluginState state;
    
    PluginStateMachine() {
        this(PluginState.UNLOADED);
    }
    
    PluginStateMachine(PluginState initialState) {
        state = initialState;        
    }
    
    PluginState getCurrentState() {
        return state;
    }
    
    synchronized void setNextState(PluginState nextState) throws PluginException {
        if ((state == PluginState.UNLOADED && nextState == PluginState.LOADING)
            || (state == PluginState.LOADING && nextState == PluginState.LOADED)
            || (state == PluginState.LOADED && nextState == PluginState.UNLOADED)) {
            state = nextState;
        } else {
            throw new PluginException("INVALID_STATE_TRANSITION", state, nextState);
        }
        notifyAll();
    }
    
    synchronized void waitForState(PluginState awaitedState) {
        while (state != awaitedState) {
            try {
                wait();
            } catch (InterruptedException ex) {
                // deliberately ignore 
            }
        }
    }
}
