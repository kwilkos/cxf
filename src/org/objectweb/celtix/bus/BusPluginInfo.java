package org.objectweb.celtix.bus;

import org.objectweb.celtix.plugins.PerBusPluginState;

public class BusPluginInfo {

    public enum BusPluginState {
        UNLOADED,
        LOADED,
        INITIALIZING,
        READY,
        SHUTTING_DOWN
    }
    private String name;
    private PerBusPluginState busState;
    private BusPluginState state;
    
    BusPluginInfo(String n) {
        name = n;
        busState = null;
        state = BusPluginState.UNLOADED;
    }

    /**
     * @return Returns the busState.
     */
    public PerBusPluginState getBusState() {
        return busState;
    }

    /**
     * @param busState The busState to set.
     */
    public void setBusState(PerBusPluginState bs) {
        busState = bs;
    }

    /**
     * @return Returns the name.
     */
    public String getName() {
        return name;
    }

    /**
     * @param name The name to set.
     */
    public void setName(String n) {
        name = n;
    }

    /**
     * @return Returns the state.
     */
    public BusPluginState getState() {
        return state;
    }

    /**
     * @param state The state to set.
     */
    public void setState(BusPluginState s) {
        this.state = s;
    }
    
    
    
}
