package org.objectweb.celtix.ws.rm;

import org.objectweb.celtix.ws.addressing.ContextUtils;

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
