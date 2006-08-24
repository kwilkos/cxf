package org.apache.cxf.bus;

import java.util.HashMap;
import java.util.Map;

import org.apache.cxf.Bus;
import org.apache.cxf.BusFactory;

public class CeltixBusFactory implements BusFactory {
    
    private static Bus defaultBus;

    public synchronized Bus getDefaultBus() {
        if (null == defaultBus) {
            defaultBus = new CeltixBus();
        }
        return defaultBus;
    }

    public void setDefaultBus(Bus bus) {
        defaultBus = bus;
    }
    
    public Bus createBus() {
        return createBus(new HashMap<Class, Object>());
    }
    
    public Bus createBus(Map<Class, Object> e) {
        return createBus(e, null);
        
    }
    
    public Bus createBus(Map<Class, Object> e, Map<String, Object> properties) {
        return new CeltixBus(e, properties);
    }
    
}
