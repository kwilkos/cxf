package org.objectweb.celtix.bus.ws.rm;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;


import org.objectweb.celtix.bus.configuration.wsrm.SequenceTerminationPolicyType;
import org.objectweb.celtix.buslifecycle.BusLifeCycleListener;
import org.objectweb.celtix.common.i18n.Message;
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
        
        SequenceTerminationPolicyType st = source.getHandler().getConfigurationHelper()
            .getSequenceTerminationPolicy();
        if (!st.isTerminateOnShutdown()) {
            LOG.fine("No need to terminate sequences on shutdown");
            return;
        }
        
        Collection<SourceSequence> seqs = source.getAllUnacknowledgedSequences();
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("Trying to terminate " + seqs.size() + "  sequences");
        }
        
        Collection<SourceSequence> closedSeqs = new ArrayList<SourceSequence>();
        
        for (SourceSequence seq : seqs) {
            if (seq.isLastMessage()) {
                closedSeqs.add(seq);
            } else {
                try {
                    source.getHandler().getProxy().lastMessage(seq); 
                } catch (IOException ex) {
                    Message msg = new Message("LAST_MESSAGE_SEND_EXC", LOG, seq);
                    LOG.log(Level.WARNING, msg.toString(), ex); 
                }
            }
        }
        
        if (closedSeqs.size() > 0) {
            try {
                source.getHandler().getProxy().requestAcknowledgment(seqs); 
            } catch (IOException ex) {
                Message msg = new Message("ACK_REQUESTED_SEND_EXC", LOG);
                LOG.log(Level.WARNING, msg.toString(), ex);
            }
        }
    }

}
