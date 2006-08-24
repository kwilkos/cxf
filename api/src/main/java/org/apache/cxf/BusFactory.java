package org.apache.cxf;

public interface BusFactory {
    
    Bus getDefaultBus();
    
    void setDefaultBus(Bus bus);
}
