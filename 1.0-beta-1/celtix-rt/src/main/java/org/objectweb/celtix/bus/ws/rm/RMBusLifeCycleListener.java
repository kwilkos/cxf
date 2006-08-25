package org.objectweb.celtix.bus.ws.rm;

import java.io.IOException;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;


import org.objectweb.celtix.buslifecycle.BusLifeCycleListener;
import org.objectweb.celtix.common.logging.LogUtils;

public class RMBusLifeCycleListener implements BusLifeCycleListener {

    private static final Logger LOG = LogUtils.getL7dLogger(RMBusLifeCycleListener.class);
    
    private RMSource source;
    
    protected RMBusLifeCycleListener(RMSource s) {
        source = s;
    }
    public void initComplete() {      
    }

    public void postShutdown() {      
    }

    public void preShutdown() {
        terminateSequences();
    }
    
    private void terminateSequences() {
        boolean tryTerminateAllSequences = true;
        Collection<Sequence> seqs = source.getAllUnacknowledgedSequences();
        
        if (tryTerminateAllSequences && seqs.size() >= 0) {
            try {
                source.getHandler().getProxy().requestAcknowledgement(seqs); 
            } catch (IOException ex) {
                LOG.log(Level.SEVERE, "Failed to request acknowledgements.", ex); 
            }
        }
        
        for (Sequence seq : seqs) {
            if (seq.allAcknowledged()) {
                try {
                    source.getHandler().getProxy().terminateSequence(seq);
                } catch (IOException ex) {
                    LOG.log(Level.SEVERE, "Failed to terminate sequence.", ex);
                }
            }
        }        
    }

}
