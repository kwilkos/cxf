package org.objectweb.celtix.application;

import java.util.ResourceBundle;
import java.util.logging.Logger;

import org.objectweb.celtix.common.i18n.BundleUtils;
import org.objectweb.celtix.common.i18n.Message;
import org.objectweb.celtix.plugins.PluginException;


public class PluginStateMachine {
    
    private static final Logger LOG = Logger.getLogger(PluginStateMachine.class.getName());
    private static final ResourceBundle BUNDLE = BundleUtils.getBundle(PluginStateMachine.class);

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
            Message msg = new Message("INVALID_STATE_TRANSITION_EXC", BUNDLE, state, nextState);
            throw new PluginException(msg);
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
