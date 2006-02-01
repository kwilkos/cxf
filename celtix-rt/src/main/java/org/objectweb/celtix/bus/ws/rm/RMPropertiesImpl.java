package org.objectweb.celtix.bus.ws.rm;

import java.math.BigInteger;

import org.objectweb.celtix.ws.addressing.EndpointReferenceType;
import org.objectweb.celtix.ws.rm.Identifier;
import org.objectweb.celtix.ws.rm.RMProperties;

public class RMPropertiesImpl implements RMProperties {

    private Identifier sequenceIdentifier;
    private BigInteger messageNumber;
    private EndpointReferenceType acksTo;
    
    public EndpointReferenceType getAcksTo() {
        return acksTo;
    }

    public BigInteger getMessageNumber() {
        return messageNumber;
    }

    public Identifier getSequenceId() {
        return sequenceIdentifier;
    }

    public void setAcksTo(EndpointReferenceType a) {
        acksTo = a;        
    }

    public void setMessageNumber(BigInteger m) {
        messageNumber = m;
    }

    public void setSequenceId(Identifier sid) {
        sequenceIdentifier = sid;
    }
}
