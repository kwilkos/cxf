package org.objectweb.celtix.ws.rm;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import junit.framework.TestCase;

import org.easymock.classextension.EasyMock;
import org.easymock.classextension.IMocksControl;
import org.objectweb.celtix.bus.configuration.wsrm.SequenceTerminationPolicyType;

import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;

public class RMBusLifeCycleListenerTest extends TestCase {

    private IMocksControl control;
    private RMSource source;
    private RMHandler handler;
    private ConfigurationHelper configurationHelper;
    private SequenceTerminationPolicyType  stp;
    
    public void setUp() {
        control = EasyMock.createNiceControl();
        handler = control.createMock(RMHandler.class);
        configurationHelper = control.createMock(ConfigurationHelper.class);
        source = control.createMock(RMSource.class);
        stp = control.createMock(SequenceTerminationPolicyType.class);
    }
    
    public void testNoAction() {
        expect(source.getHandler()).andReturn(handler);
        expect(handler.getConfigurationHelper()).andReturn(configurationHelper);
        expect(configurationHelper.getSequenceTerminationPolicy()).andReturn(stp);
        expect(stp.isTerminateOnShutdown()).andReturn(false);
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
        RMProxy proxy = control.createMock(RMProxy.class);
        
        expect(source.getHandler()).andReturn(handler).times(2);
        expect(handler.getConfigurationHelper()).andReturn(configurationHelper);
        expect(configurationHelper.getSequenceTerminationPolicy()).andReturn(stp);
        expect(stp.isTerminateOnShutdown()).andReturn(true);
        expect(source.getAllUnacknowledgedSequences()).andReturn(seqs);
        expect(seq.isLastMessage()).andReturn(true);        
        expect(handler.getProxy()).andReturn(proxy);
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
        RMProxy proxy = control.createMock(RMProxy.class);
        
        expect(source.getHandler()).andReturn(handler).times(2);
        expect(handler.getConfigurationHelper()).andReturn(configurationHelper);
        expect(configurationHelper.getSequenceTerminationPolicy()).andReturn(stp);
        expect(stp.isTerminateOnShutdown()).andReturn(true);
        expect(source.getAllUnacknowledgedSequences()).andReturn(seqs);
        expect(seq.isLastMessage()).andReturn(false);        
        expect(handler.getProxy()).andReturn(proxy);
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
        RMProxy proxy = control.createMock(RMProxy.class);
        
        expect(source.getHandler()).andReturn(handler).times(2);
        expect(handler.getConfigurationHelper()).andReturn(configurationHelper);
        expect(configurationHelper.getSequenceTerminationPolicy()).andReturn(stp);
        expect(stp.isTerminateOnShutdown()).andReturn(true);
        expect(source.getAllUnacknowledgedSequences()).andReturn(seqs);
        expect(seq.isLastMessage()).andReturn(true);        
        expect(handler.getProxy()).andReturn(proxy);
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
        RMProxy proxy = control.createMock(RMProxy.class);
        
        expect(source.getHandler()).andReturn(handler).times(2);
        expect(handler.getConfigurationHelper()).andReturn(configurationHelper);
        expect(configurationHelper.getSequenceTerminationPolicy()).andReturn(stp);
        expect(stp.isTerminateOnShutdown()).andReturn(true);
        expect(source.getAllUnacknowledgedSequences()).andReturn(seqs);
        expect(seq.isLastMessage()).andReturn(false);        
        expect(handler.getProxy()).andReturn(proxy);
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
