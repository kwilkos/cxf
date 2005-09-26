package org.objectweb.celtix.application;

import java.util.logging.Logger;

import org.objectweb.celtix.plugins.PluginException;
import org.objectweb.celtix.plugins.PluginMessage;


public class PluginStateMachine {
    
    private static final Logger LOG = Logger.getLogger(PluginStateMachine.class/*.getPackage()*/.getName());
    
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
            LOG.fine("changing state from " + state + " to " + nextState);
            state = nextState;
        } else {
            throw new PluginException(new PluginMessage("INVALID_STATE_TRANSITION", state, nextState));
        }
        notifyAll();
    }
    
    synchronized void waitForState(PluginState awaitedState) {
        while (state != awaitedState) {
            LOG.fine("waiting for state so change from " + state + " to " + awaitedState);
            try {
                wait();
            } catch (InterruptedException ex) {
                // deliberately ignore 
            }
        }
    }
}
