package org.objectweb.celtix.bus;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;



/**
 * Manages the <code>Bus</code> instances in a process.
 */
final class BusManager {

    private static final MessageFormat BUS_ID_FMT = new MessageFormat("_bus_id_{0}_");
    
    private static int lastId;
    private static BusManager theInstance;
    
    private Map<String, Bus> busses;
    
    private BusManager() {
        busses = new HashMap<String, Bus>();
    }
    
    static BusManager getInstance() {
        if (null == theInstance) {
            theInstance = new BusManager();
        }
        return theInstance;
    }
    
    public Bus getBus(String[] args) {
        
        // partial initialisation of the bus configuration is
        // required in order to determine if a bus with the same
        // specification already exists
        
        BusArguments busArgs = new BusArguments(args);
        String id = busArgs.getBusId();
        Bus bus = busses.get(id);
        if (null == bus) {
            bus = new Bus(busArgs);
            busses.put(id, bus);
        }
        return bus;    
    }
    
    String getBusId(BusArguments args) {
        
        // iterate through arg list and check if id is explicitly specified,
        // otherwise generate is and add the argument
        
        String id = generateBusId();
        return id;
    }
    
    /** 
     * Determines a unique bus identifier for this process. 
     * 
     * @return String a unique <code>Bus</code> identifier for this process.
     */
    String generateBusId() {
        String tmpId;
        do {
            lastId++;
            tmpId = BUS_ID_FMT.format(new Integer(lastId));
        } while(busses.get(tmpId) == null);
        return tmpId;        
    }    
 
}
