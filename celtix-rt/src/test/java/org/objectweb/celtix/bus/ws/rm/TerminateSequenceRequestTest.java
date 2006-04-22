package org.objectweb.celtix.bus.ws.rm;

import java.util.ArrayList;

import javax.xml.ws.handler.Handler;

import junit.framework.TestCase;

import org.easymock.classextension.EasyMock;
import org.objectweb.celtix.bindings.AbstractBindingBase;
import org.objectweb.celtix.bus.handlers.HandlerChainInvoker;
import org.objectweb.celtix.context.ObjectMessageContext;
import org.objectweb.celtix.context.ObjectMessageContextImpl;
import org.objectweb.celtix.transports.Transport;
import org.objectweb.celtix.ws.rm.Identifier;
import org.objectweb.celtix.ws.rm.TerminateSequenceType;

public class TerminateSequenceRequestTest extends TestCase {
    
    public void testConstruction() {     
        
        ObjectMessageContext objectCtx = new ObjectMessageContextImpl(); 
        RMSource source = EasyMock.createMock(RMSource.class);
        AbstractBindingBase binding = EasyMock.createMock(AbstractBindingBase.class);
        Transport transport = EasyMock.createMock(Transport.class);
        HandlerChainInvoker hci = new HandlerChainInvoker(new ArrayList<Handler>());
        
        Identifier sid = RMUtils.getWSRMFactory().createIdentifier();
        sid.setValue("TerminatedSequence");
        SourceSequence seq = new SourceSequence(sid, null, null);
        
        
        binding.createObjectContext();
        EasyMock.expectLastCall().andReturn(objectCtx);
        binding.createHandlerInvoker();
        EasyMock.expectLastCall().andReturn(hci);
        
        EasyMock.replay(source);
        EasyMock.replay(binding);
        
        TerminateSequenceRequest req = new TerminateSequenceRequest(binding, transport, seq);
        assertNotNull(req);
        
        assertNotNull(TerminateSequenceRequest.createDataBindingCallback());
        assertNotNull(req.getObjectMessageContext().getMethod());
        
        Object[] params = req.getObjectMessageContext().getMessageObjects();
        assertEquals(1, params.length);
        TerminateSequenceType ts = (TerminateSequenceType)params[0];
        
        assertEquals(sid, ts.getIdentifier());
      
        EasyMock.verify(source);
        EasyMock.verify(binding);
    }
}
