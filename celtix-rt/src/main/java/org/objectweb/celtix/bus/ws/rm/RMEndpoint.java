package org.objectweb.celtix.bus.ws.rm;

import org.objectweb.celtix.bus.ws.addressing.ContextUtils;
import org.objectweb.celtix.ws.rm.Identifier;

public class RMEndpoint {
    
    private RMHandler handler;
    

    protected RMEndpoint(RMHandler h) {
        handler = h;
    }
    
    
    public RMHandler getHandler() {
        return handler;
    }

    /**
     * Generates and returns a sequence identifier.
     * 
     * @return the sequence identifier.
     */
    public Identifier generateSequenceIdentifier() {
        String sequenceID = ContextUtils.generateUUID();
        Identifier sid = RMUtils.getWSRMFactory().createIdentifier();
        sid.setValue(sequenceID);        
        return sid;
    }

   
    
}
