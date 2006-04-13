package org.objectweb.celtix.bus.ws.rm.persistence.file;

import java.io.File;
import java.math.BigInteger;
import java.util.Collection;
import java.util.Map;

import org.objectweb.celtix.ws.rm.Identifier;
import org.objectweb.celtix.ws.rm.persistence.RMDestinationSequence;
import org.objectweb.celtix.ws.rm.persistence.RMMessage;
import org.objectweb.celtix.ws.rm.persistence.RMSourceSequence;
import org.objectweb.celtix.ws.rm.persistence.RMStore;

public class RMFileStore implements RMStore {
    public static final String FILE_STORE_DIR = 
        "org.objectweb.celtix.rm.persistence.file.dir";
    
    private File root;
    
    public void init(Map<String, String> params) {
        String dirName = params.get(FILE_STORE_DIR);
        if (null == dirName) {
            dirName = ".";
        }
        root = new File(dirName);
        assert null != root;
    }
    
    public void createSequence(RMSourceSequence seq) {
        // TODO Auto-generated method stub
        
    }

    public Collection<RMDestinationSequence> getDestinationSequences(String endpointIdentifier) {
        // TODO Auto-generated method stub
        return null;
    }

    public Collection<RMMessage> getInboundMessages(Identifier sid) {
        // TODO Auto-generated method stub
        return null;
    }

    public Collection<RMMessage> getOutboundMessages(Identifier sid) {
        // TODO Auto-generated method stub
        return null;
    }

    public Collection<RMSourceSequence> getSourceSequences(String endpointIdentifier) {
        // TODO Auto-generated method stub
        return null;
    }

    public void persistIncoming(Identifier destSequenceId, boolean lastMessage, RMMessage msg) {
        // TODO Auto-generated method stub
        
    }

    public void persistOutgoing(Identifier srcSequenceId, boolean lastMessage, RMMessage msg) {
        // TODO Auto-generated method stub
        
    }

    public void removeMessage(Identifier sid, Collection<BigInteger> messageNr) {
        // TODO Auto-generated method stub
        
    }

    public void removeSequence(Identifier seq) {
        // TODO Auto-generated method stub
        
    }
    

}
