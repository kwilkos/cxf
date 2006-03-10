package org.objectweb.celtix.bus.ws.rm;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.objectweb.celtix.bus.configuration.wsrm.EndpointPolicyType;
import org.objectweb.celtix.ws.rm.Identifier;

public class RMEndpoint {

    private static final String POLICIES_PROPERTY_NAME = "policies";
    private static final String URN_UUID = "urn:uuid:";
    
    protected Map<Identifier, Sequence> map;
    private RMHandler handler;
    

    protected RMEndpoint(RMHandler h) {
        handler = h;
        map = new HashMap<Identifier, Sequence>();
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
        String sequenceID = URN_UUID + UUID.randomUUID();
        Identifier sid = RMUtils.getWSRMFactory().createIdentifier();
        sid.setValue(sequenceID);        
        return sid;
    }

    public EndpointPolicyType getPolicies() {
        return (EndpointPolicyType)handler.getConfiguration().getObject(POLICIES_PROPERTY_NAME);
    }

    /**
     * Returns the sequence with the given identifier,
     * 
     * @param id the sequence identifier.
     * @return the sequence.
     */
    public Sequence getSequence(Identifier id) {
        return map.get(id);
    }
    
    /**
     * Returns the collection of all sequences currently maintained by this source.
     * 
     * @return the collection of sequences
     */
    public Collection<Sequence> getAllSequences() {
        return map.values();
    }
    
    /**
     * Returns a collection of all sequences for which have not yet been
     * completely acknowledged.
     * 
     * @return the collection of unacknowledged sequences.
     */
    public Collection<Sequence> getAllUnacknowledgedSequences() {
        Collection<Sequence> seqs = new ArrayList<Sequence>();
        for (Sequence seq : map.values()) {
            if (!seq.allAcknowledged()) {
                seqs.add(seq);
            }
        }        
        return seqs;        
    }

    /**
     * Stores the sequence under its sequence identifier.
     * 
     * @param id the sequence identifier.
     * @param seq the sequence.
     */
    public void addSequence(Sequence seq) {
        map.put(seq.getIdentifier(), seq);
    }
}
