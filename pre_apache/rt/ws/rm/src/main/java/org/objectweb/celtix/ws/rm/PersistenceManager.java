package org.objectweb.celtix.ws.rm;

import org.objectweb.celtix.ws.rm.persistence.RMStore;

public interface PersistenceManager {
    
    RMStore getStore();
    
    RetransmissionQueue getQueue();
}
