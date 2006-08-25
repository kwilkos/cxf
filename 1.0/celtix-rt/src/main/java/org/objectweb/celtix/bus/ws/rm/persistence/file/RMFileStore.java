package org.objectweb.celtix.bus.ws.rm.persistence.file;

import java.io.File;
import java.math.BigInteger;
import java.util.ArrayList;
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
    

    public void createDestinationSequence(RMDestinationSequence seq) {
        // TODO Auto-generated method stub
        
    }

    public void createSourceSequence(RMSourceSequence seq) {
        // TODO Auto-generated method stub
        
    }

    public Collection<RMDestinationSequence> getDestinationSequences(String endpointIdentifier) {
        return new ArrayList<RMDestinationSequence>();
    }

    public Collection<RMMessage> getMessages(Identifier sid, boolean outbound) {
        return new ArrayList<RMMessage>();
    }
    
    public Collection<RMSourceSequence> getSourceSequences(String endpointIdentifier) {
        return new ArrayList<RMSourceSequence>();
    }

    public void persistIncoming(RMDestinationSequence seq, RMMessage msg) {
        // TODO Auto-generated method stub
        
    }

    public void persistOutgoing(RMSourceSequence seq, RMMessage msg) {
        // TODO Auto-generated method stub
        
    }

    public void removeMessages(Identifier sid, Collection<BigInteger> messageNrs, boolean outbound) {
        // TODO Auto-generated method stub
        
    }

    public void removeDestinationSequence(Identifier sid) {
        // TODO Auto-generated method stub
        
    }

    public void removeSourceSequence(Identifier sid) {
        // TODO Auto-generated method stub
        
    }    

}
