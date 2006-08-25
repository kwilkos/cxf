package org.objectweb.celtix.bus.ws.rm;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import junit.framework.TestCase;

import org.easymock.classextension.EasyMock;
import org.easymock.classextension.IMocksControl;
import org.objectweb.celtix.bus.configuration.wsrm.SequenceTerminationPolicyType;

import static org.easymock.EasyMock.expectLastCall;

public class RMBusLifeCycleListenerTest extends TestCase {

    private IMocksControl control;
    private RMSource source;
    private SequenceTerminationPolicyType  stp;
    
    public void setUp() {
        control = EasyMock.createNiceControl();
        source = control.createMock(RMSource.class);
        stp = control.createMock(SequenceTerminationPolicyType.class);
    }
    
    public void testNoAction() {
        source.getSequenceTerminationPolicy();
        expectLastCall().andReturn(stp);
        stp.isTerminateOnShutdown();
        expectLastCall().andReturn(false);
        control.replay();

        RMBusLifeCycleListener listener = new RMBusLifeCycleListener(source);
        listener.initComplete();
        listener.preShutdown();
        listener.postShutdown();

        control.verify();
    }
    
    public void testTerminateClosedSequenceFailRequestAcknowledgement() throws IOException {

        Collection<SourceSequence> seqs = new ArrayList<SourceSequence>();
        SourceSequence seq = control.createMock(SourceSequence.class);
        seqs.add(seq);
        RMHandler handler = control.createMock(RMHandler.class);
        RMProxy proxy = control.createMock(RMProxy.class);
        
        source.getSequenceTerminationPolicy();
        expectLastCall().andReturn(stp);
        stp.isTerminateOnShutdown();
        expectLastCall().andReturn(true);
        source.getAllUnacknowledgedSequences();
        expectLastCall().andReturn(seqs);
        seq.isLastMessage();
        expectLastCall().andReturn(true);        
        source.getHandler();
        expectLastCall().andReturn(handler);
        handler.getProxy();
        expectLastCall().andReturn(proxy);
        proxy.requestAcknowledgment(seqs);
        expectLastCall().andThrow(new IOException());
        
        control.replay();

        RMBusLifeCycleListener listener = new RMBusLifeCycleListener(source);
        listener.initComplete();
        listener.preShutdown();
        listener.postShutdown();

        control.verify();
    }
    
    
    public void testTerminateOpenSequenceFailLastMessage() throws IOException {

        Collection<SourceSequence> seqs = new ArrayList<SourceSequence>();
        SourceSequence seq = control.createMock(SourceSequence.class);
        seqs.add(seq);
        RMHandler handler = control.createMock(RMHandler.class);
        RMProxy proxy = control.createMock(RMProxy.class);
        
        source.getSequenceTerminationPolicy();
        expectLastCall().andReturn(stp);
        stp.isTerminateOnShutdown();
        expectLastCall().andReturn(true);
        source.getAllUnacknowledgedSequences();
        expectLastCall().andReturn(seqs);
        seq.isLastMessage();
        expectLastCall().andReturn(false);        
        source.getHandler();
        expectLastCall().andReturn(handler);
        handler.getProxy();
        expectLastCall().andReturn(proxy);
        proxy.lastMessage(seq);
        expectLastCall().andThrow(new IOException());
        
        control.replay();

        RMBusLifeCycleListener listener = new RMBusLifeCycleListener(source);
        listener.initComplete();
        listener.preShutdown();
        listener.postShutdown();

        control.verify();
    }
    
    public void testTerminateClosedSequence() throws IOException {

        Collection<SourceSequence> seqs = new ArrayList<SourceSequence>();
        SourceSequence seq = control.createMock(SourceSequence.class);
        seqs.add(seq);
        RMHandler handler = control.createMock(RMHandler.class);
        RMProxy proxy = control.createMock(RMProxy.class);
        
        source.getSequenceTerminationPolicy();
        expectLastCall().andReturn(stp);
        stp.isTerminateOnShutdown();
        expectLastCall().andReturn(true);
        source.getAllUnacknowledgedSequences();
        expectLastCall().andReturn(seqs);
        seq.isLastMessage();
        expectLastCall().andReturn(true);        
        source.getHandler();
        expectLastCall().andReturn(handler);
        handler.getProxy();
        expectLastCall().andReturn(proxy);
        proxy.requestAcknowledgment(seqs);
        expectLastCall();
        
        control.replay();

        RMBusLifeCycleListener listener = new RMBusLifeCycleListener(source);
        listener.initComplete();
        listener.preShutdown();
        listener.postShutdown();

        control.verify();
    }
    
    public void testTerminateOpenSequence() throws IOException {

        Collection<SourceSequence> seqs = new ArrayList<SourceSequence>();
        SourceSequence seq = control.createMock(SourceSequence.class);
        seqs.add(seq);
        RMHandler handler = control.createMock(RMHandler.class);
        RMProxy proxy = control.createMock(RMProxy.class);
        
        source.getSequenceTerminationPolicy();
        expectLastCall().andReturn(stp);
        stp.isTerminateOnShutdown();
        expectLastCall().andReturn(true);
        source.getAllUnacknowledgedSequences();
        expectLastCall().andReturn(seqs);
        seq.isLastMessage();
        expectLastCall().andReturn(false);        
        source.getHandler();
        expectLastCall().andReturn(handler);
        handler.getProxy();
        expectLastCall().andReturn(proxy);
        proxy.lastMessage(seq);
        expectLastCall();
        
        control.replay();

        RMBusLifeCycleListener listener = new RMBusLifeCycleListener(source);
        listener.initComplete();
        listener.preShutdown();
        listener.postShutdown();

        control.verify();
    }
}
