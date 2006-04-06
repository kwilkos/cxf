package org.objectweb.celtix.bus.ws.rm;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import junit.framework.TestCase;

import org.easymock.classextension.EasyMock;
import org.easymock.classextension.IMocksControl;

import static org.easymock.classextension.EasyMock.*;

public class RMBusLifeCycleListenerTest extends TestCase {

    public void testRMBusLifeCycleListener() throws IOException {
        IMocksControl control = EasyMock.createNiceControl();
        RMSource source = control.createMock(RMSource.class);

        Collection<Sequence> seqs = new ArrayList<Sequence>();
        Sequence seq1 = control.createMock(Sequence.class);
        seqs.add(seq1);
        Sequence seq2 = control.createMock(Sequence.class);
        seqs.add(seq2);

        source.getAllUnacknowledgedSequences();
        expectLastCall().andReturn(seqs);
        RMHandler handler = control.createMock(RMHandler.class);
        source.getHandler();
        expectLastCall().andReturn(handler);
        RMProxy proxy = control.createMock(RMProxy.class);
        handler.getProxy();
        expectLastCall().andReturn(proxy);
        proxy.requestAcknowledgment(seqs);
        expectLastCall();

        seq1.allAcknowledged();
        expectLastCall().andReturn(false);
        seq2.allAcknowledged();
        expectLastCall().andReturn(true);
        source.getHandler();
        expectLastCall().andReturn(handler);
        handler.getProxy();
        expectLastCall().andReturn(proxy);
        proxy.terminateSequence(seq2);
        expectLastCall();

        control.replay();

        RMBusLifeCycleListener listener = new RMBusLifeCycleListener(source);

        listener.initComplete();

        listener.preShutdown();

        listener.postShutdown();

        control.verify();

    }
}
