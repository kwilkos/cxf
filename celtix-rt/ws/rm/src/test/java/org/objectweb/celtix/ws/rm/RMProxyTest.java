package org.objectweb.celtix.ws.rm;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;

import javax.xml.ws.handler.MessageContext;

import junit.framework.TestCase;

import org.easymock.classextension.EasyMock;
import org.easymock.classextension.IMocksControl;
import org.objectweb.celtix.bindings.AbstractClientBinding;
import org.objectweb.celtix.bindings.ClientBinding;
import org.objectweb.celtix.bindings.DataBindingCallback;
import org.objectweb.celtix.bindings.JAXWSConstants;
import org.objectweb.celtix.bindings.Request;
import org.objectweb.celtix.bindings.Response;
import org.objectweb.celtix.bindings.ResponseCorrelator;
import org.objectweb.celtix.bus.configuration.wsrm.SourcePolicyType;
import org.objectweb.celtix.context.ObjectMessageContext;
import org.objectweb.celtix.context.ObjectMessageContextImpl;
import org.objectweb.celtix.handlers.HandlerInvoker;
import org.objectweb.celtix.transports.ClientTransport;
import org.objectweb.celtix.transports.Transport;
import org.objectweb.celtix.ws.addressing.AddressingProperties;
import org.objectweb.celtix.ws.addressing.AttributedURIType;
import org.objectweb.celtix.ws.addressing.VersionTransformer;
import org.objectweb.celtix.ws.addressing.v200408.EndpointReferenceType;
import org.objectweb.celtix.ws.rm.persistence.RMDestinationSequence;
import org.objectweb.celtix.ws.rm.wsdl.SequenceFault;

import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.isA;
import static org.easymock.EasyMock.isNull;
import static org.easymock.classextension.EasyMock.expect;

public class RMProxyTest extends TestCase {
    
    private IMocksControl control;
    private RMHandler handler;
    private RMProxy proxy;
    
    public void setUp() {
        control = EasyMock.createNiceControl();
        handler = control.createMock(RMHandler.class);
        proxy = new RMProxy(handler);
    }
    
    public void tearDown() {
         
    }
    
    public void testGetTransport() throws IOException {
        expect(handler.getClientBinding()).andReturn(null);
        Transport t = control.createMock(Transport.class);
        expect(handler.getTransport()).andReturn(t);
        control.replay();
        assertSame(t, proxy.getTransport());
        control.verify();
        
        control.reset();
        AbstractClientBinding acb = control.createMock(AbstractClientBinding.class);
        expect(handler.getClientBinding()).andReturn(acb).times(2);
        try {
            expect(acb.getTransport()).andThrow(new IOException());
        } catch (IOException ex) {
            fail("IOEXception not handled");
        }
        control.replay();
        assertNull(proxy.getTransport());
        control.verify();
        
        control.reset();
        acb = control.createMock(AbstractClientBinding.class);
        expect(handler.getClientBinding()).andReturn(acb).times(2); 
        ClientTransport ct = control.createMock(ClientTransport.class);
        expect(acb.getTransport()).andReturn(ct);
        control.replay();
        assertSame(ct, proxy.getTransport());  
        control.verify();
    }
    
    public void testCanSend2004() {
        EndpointReferenceType anonymous = RMUtils.createReference(Names.WSA_ANONYMOUS_ADDRESS);
        EndpointReferenceType nonAnonymous = RMUtils.createReference("http://localhost:9999/decoupled");
        expect(handler.getClientBinding()).andReturn(null).times(2);
        control.replay();
        assertTrue(proxy.canSend(nonAnonymous));
        assertTrue(!proxy.canSend(anonymous));
        control.verify();

        control.reset();
        ClientBinding cb = control.createMock(ClientBinding.class);
        expect(handler.getClientBinding()).andReturn(cb).times(2);
        control.replay();
        assertTrue(proxy.canSend(nonAnonymous));
        assertTrue(proxy.canSend(anonymous));
        control.verify();     
    }
    
    public void testCanSend2005() throws IOException {
        EndpointReferenceType anonymous2004 = RMUtils.createReference(Names.WSA_ANONYMOUS_ADDRESS);
        EndpointReferenceType nonAnonymous2004 = RMUtils.createReference("http://localhost:9999/decoupled");
        org.objectweb.celtix.ws.addressing.EndpointReferenceType anonymous =
            VersionTransformer.convert(anonymous2004);
        org.objectweb.celtix.ws.addressing.EndpointReferenceType nonAnonymous =
            VersionTransformer.convert(nonAnonymous2004);
        expect(handler.getClientBinding()).andReturn(null).times(2);
        control.replay();
        assertTrue(proxy.canSend(nonAnonymous));
        assertTrue(!proxy.canSend(anonymous));
        control.verify();

        control.reset();
        AbstractClientBinding acb = control.createMock(AbstractClientBinding.class);
        expect(handler.getClientBinding()).andReturn(acb).times(2);
        expect(acb.getTransport()).andReturn(null);
        control.replay();
        assertTrue(!proxy.canSend(nonAnonymous));
        assertTrue(!proxy.canSend(anonymous));
        control.verify(); 
        
        control.reset();
        acb = control.createMock(AbstractClientBinding.class);
        expect(handler.getClientBinding()).andReturn(acb).times(4);
        ClientTransport t = control.createMock(ClientTransport.class);
        expect(acb.getTransport()).andReturn(t).times(2);
        control.replay();
        assertTrue(proxy.canSend(nonAnonymous));
        assertTrue(proxy.canSend(anonymous));
        control.verify();         
    }
    
    public void testOfferedIdentifier() {
        OfferType o = control.createMock(OfferType.class);
        Identifier oid = control.createMock(Identifier.class);
        expect(o.getIdentifier()).andReturn(oid);
        control.replay();
        assertNull("Unexpected offered identifier", proxy.getOfferedIdentifier());
        proxy.setOfferedIdentifier(null);
        assertNull("Unexpected offered identifier", proxy.getOfferedIdentifier());
        proxy.setOfferedIdentifier(o);
        assertNotNull("No offered identifier", proxy.getOfferedIdentifier());
        control.verify();       
    }
    
    public void testAcknowledge() throws IOException, NoSuchMethodException {
        RMDestinationSequence ds = control.createMock(RMDestinationSequence.class);
        EndpointReferenceType anonymous = RMUtils.createReference(Names.WSA_ANONYMOUS_ADDRESS);
        expect(ds.getAcksTo()).andReturn(anonymous);
        control.replay();
        proxy.acknowledge(ds);
        control.verify();
        
        control.reset();
        Method m = RMProxy.class.getDeclaredMethod("send", new Class[] {Request.class,
                                                                        DataBindingCallback.class});
        proxy = control.createMock(RMProxy.class, new Method[] {m});
        proxy.setHandler(handler);
        EndpointReferenceType nonAnonymous = RMUtils.createReference("http://localhost:9999/decoupled");
        expect(ds.getAcksTo()).andReturn(nonAnonymous).times(4);
        AbstractClientBinding acb = control.createMock(AbstractClientBinding.class);
        expect(handler.getClientBinding()).andReturn(acb);
        expect(handler.getBinding()).andReturn(acb);
        ClientTransport ct = control.createMock(ClientTransport.class);
        expect(handler.getTransport()).andReturn(ct);
        ObjectMessageContext octx = new ObjectMessageContextImpl();
        expect(acb.createObjectContext()).andReturn(octx);
        expect(acb.createHandlerInvoker()).andReturn(null);
        proxy.send(isA(Request.class), (DataBindingCallback)(isNull()));
        expectLastCall();     
        
        control.replay();
        proxy.acknowledge(ds);
        control.verify();
    }
    
    public void testLastMessage() throws IOException, NoSuchMethodException {
        Method m = RMProxy.class.getDeclaredMethod("send", new Class[] {Request.class,
                                                                        DataBindingCallback.class});
        proxy = control.createMock(RMProxy.class, new Method[] {m});
        proxy.setHandler(handler);
        SourceSequence ss = control.createMock(SourceSequence.class);
        EndpointReferenceType nonAnonymous2004 = RMUtils.createReference("http://localhost:9999/decoupled");
        org.objectweb.celtix.ws.addressing.EndpointReferenceType nonAnonymous =
            VersionTransformer.convert(nonAnonymous2004);
        expect(ss.getTarget()).andReturn(nonAnonymous).times(2);
        AbstractClientBinding acb = control.createMock(AbstractClientBinding.class);
        expect(handler.getClientBinding()).andReturn(acb).times(4);
        ClientTransport ct = control.createMock(ClientTransport.class);
        expect(acb.getTransport()).andReturn(ct).times(2);   
        expect(handler.getBinding()).andReturn(acb);        
        ObjectMessageContext octx = new ObjectMessageContextImpl();
        expect(acb.createObjectContext()).andReturn(octx);
        expect(acb.createHandlerInvoker()).andReturn(null);
        ss.nextAndLastMessageNumber();
        expectLastCall();
        proxy.send(isA(Request.class), (DataBindingCallback)(isNull()));
        expectLastCall();
        control.replay();
        proxy.lastMessage(ss);
        control.verify();
    }
    
    public void testAcknowledgment() throws IOException, NoSuchMethodException  {        
        Method m = RMProxy.class.getDeclaredMethod("send", new Class[] {Request.class,
                                                                        DataBindingCallback.class});
        proxy = control.createMock(RMProxy.class, new Method[] {m});
        proxy.setHandler(handler);
        Collection<SourceSequence> sss = new ArrayList<SourceSequence>();
        SourceSequence ss = control.createMock(SourceSequence.class);
        sss.add(ss);
        EndpointReferenceType nonAnonymous2004 = RMUtils.createReference("http://localhost:9999/decoupled");
        org.objectweb.celtix.ws.addressing.EndpointReferenceType nonAnonymous =
            VersionTransformer.convert(nonAnonymous2004);
        expect(ss.getTarget()).andReturn(nonAnonymous).times(2);
        AbstractClientBinding acb = control.createMock(AbstractClientBinding.class);
        expect(handler.getClientBinding()).andReturn(acb).times(4);
        ClientTransport ct = control.createMock(ClientTransport.class);
        expect(acb.getTransport()).andReturn(ct).times(2);   
        expect(handler.getBinding()).andReturn(acb);        
        ObjectMessageContext octx = new ObjectMessageContextImpl();
        expect(acb.createObjectContext()).andReturn(octx);
        expect(acb.createHandlerInvoker()).andReturn(null);
        Identifier sid = control.createMock(Identifier.class);
        expect(ss.getIdentifier()).andReturn(sid);
        proxy.send(isA(Request.class), (DataBindingCallback)(isNull()));
        expectLastCall();
        control.replay();
        proxy.requestAcknowledgment(sss);
        control.verify();    
    }
    
    public void testTerminateSequence() throws IOException, NoSuchMethodException {
        Method m = RMProxy.class.getDeclaredMethod("send", new Class[] {Request.class,
                                                                        DataBindingCallback.class});
        proxy = control.createMock(RMProxy.class, new Method[] {m});
        proxy.setHandler(handler);
        SourceSequence ss = control.createMock(SourceSequence.class);
        EndpointReferenceType nonAnonymous2004 = RMUtils.createReference("http://localhost:9999/decoupled");
        org.objectweb.celtix.ws.addressing.EndpointReferenceType nonAnonymous =
            VersionTransformer.convert(nonAnonymous2004);
        expect(ss.getTarget()).andReturn(nonAnonymous).times(2);
        AbstractClientBinding acb = control.createMock(AbstractClientBinding.class);
        expect(handler.getClientBinding()).andReturn(acb).times(4);
        ClientTransport ct = control.createMock(ClientTransport.class);
        expect(acb.getTransport()).andReturn(ct).times(2);   
        expect(handler.getBinding()).andReturn(acb);        
        ObjectMessageContext octx = new ObjectMessageContextImpl();
        expect(acb.createObjectContext()).andReturn(octx);
        expect(acb.createHandlerInvoker()).andReturn(null);
        Identifier sid = control.createMock(Identifier.class);
        expect(ss.getIdentifier()).andReturn(sid);
        RMSource s = control.createMock(RMSource.class);
        expect(handler.getSource()).andReturn(s);
        s.removeSequence(ss);
        expectLastCall();
        proxy.send(isA(Request.class), isA(DataBindingCallback.class));
        expectLastCall();
        control.replay();
        proxy.terminateSequence(ss);
        control.verify();
    }
    
    public void testCreateSequenceResponse() throws IOException, NoSuchMethodException, SequenceFault {
        Method m = RMProxy.class.getDeclaredMethod("send", new Class[] {Request.class,
                                                                        DataBindingCallback.class});
        proxy = control.createMock(RMProxy.class, new Method[] {m});
        proxy.setHandler(handler);
        AddressingProperties inMAPs = control.createMock(AddressingProperties.class);
        CreateSequenceResponseType csr = control.createMock(CreateSequenceResponseType.class);
        AbstractClientBinding acb = control.createMock(AbstractClientBinding.class);
        expect(handler.getBinding()).andReturn(acb); 
        expect(handler.getClientBinding()).andReturn(acb).times(2);
        ClientTransport ct = control.createMock(ClientTransport.class);
        expect(acb.getTransport()).andReturn(ct);
        ObjectMessageContext octx = new ObjectMessageContextImpl();
        expect(acb.createObjectContext()).andReturn(octx);
        expect(acb.createHandlerInvoker()).andReturn(null);
        EndpointReferenceType anonymous2004 = RMUtils.createReference(Names.WSA_ANONYMOUS_ADDRESS);
        org.objectweb.celtix.ws.addressing.EndpointReferenceType anonymous =
            VersionTransformer.convert(anonymous2004);
        expect(inMAPs.getReplyTo()).andReturn(anonymous);
        AttributedURIType uri = control.createMock(AttributedURIType.class);
        expect(inMAPs.getMessageID()).andReturn(uri);
        expect(uri.getValue()).andReturn("mid001");
        proxy.send(isA(Request.class), isA(DataBindingCallback.class));
        control.replay();
        proxy.createSequenceResponse(inMAPs, csr);
        control.verify();
    }
    
    public void testCreateSequence() throws IOException, NoSuchMethodException, SequenceFault {
        Method m = RMProxy.class.getDeclaredMethod("send", new Class[] {Request.class,
                                                                        DataBindingCallback.class});
        proxy = control.createMock(RMProxy.class, new Method[] {m});
        proxy.setHandler(handler);
        AbstractClientBinding acb = control.createMock(AbstractClientBinding.class);
        expect(handler.getBinding()).andReturn(acb); 
        expect(handler.getClientBinding()).andReturn(acb).times(2);
        ClientTransport ct = control.createMock(ClientTransport.class);
        expect(acb.getTransport()).andReturn(ct);
        ObjectMessageContext octx = new ObjectMessageContextImpl();
        expect(acb.createObjectContext()).andReturn(octx);
        expect(acb.createHandlerInvoker()).andReturn(null);
        RMSource s = control.createMock(RMSource.class);
        EndpointReferenceType to2004 = RMUtils.createReference("http://localhost:9000/context/port");
        org.objectweb.celtix.ws.addressing.EndpointReferenceType to =
            VersionTransformer.convert(to2004);
        EndpointReferenceType acksTo2004 = RMUtils.createReference("http://localhost:9999/decoupled");
        expect(s.getHandler()).andReturn(handler);
        ConfigurationHelper ch = control.createMock(ConfigurationHelper.class);
        expect(handler.getConfigurationHelper()).andReturn(ch);
        SourcePolicyType sp = control.createMock(SourcePolicyType.class);
        expect(ch.getSourcePolicies()).andReturn(sp);
        expect(sp.getAcksTo()).andReturn(null);
        expect(sp.getSequenceExpiration()).andReturn(null);
        expect(sp.isIncludeOffer()).andReturn(true);
        expect(sp.getOfferedSequenceExpiration()).andReturn(null);
        Identifier sid = control.createMock(Identifier.class);
        expect(s.generateSequenceIdentifier()).andReturn(sid);
        proxy.send(isA(Request.class), isA(DataBindingCallback.class));
        control.replay();
        proxy.createSequence(s, to, acksTo2004, null);
        assertNotNull(proxy.getOfferedIdentifier());
        control.verify();
    }
    
    public void testSend() throws IOException {
        Request req = control.createMock(Request.class);
        DataBindingCallback dbc = control.createMock(DataBindingCallback.class);
        expect(req.isOneway()).andReturn(false);
        AbstractClientBinding acb = control.createMock(AbstractClientBinding.class);
        expect(handler.getBinding()).andReturn(acb).times(2);
        acb.send(req, dbc);
        expectLastCall();
        expect(handler.getClientBinding()).andReturn(acb).times(2);
        ResponseCorrelator rc = control.createMock(ResponseCorrelator.class);
        expect(acb.getResponseCorrelator()).andReturn(rc);
        Response resp = control.createMock(Response.class);
        expect(rc.getResponse(req)).andReturn(resp);
        HandlerInvoker hi = control.createMock(HandlerInvoker.class);
        expect(req.getHandlerInvoker()).andReturn(hi);
        resp.setHandlerInvoker(hi);
        expectLastCall();
        MessageContext respCtx = control.createMock(MessageContext.class);
        expect(resp.getBindingMessageContext()).andReturn(respCtx);
        DataBindingCallback respDbc = control.createMock(DataBindingCallback.class);
        expect(respCtx.get(JAXWSConstants.DATABINDING_CALLBACK_PROPERTY)).andReturn(respDbc);
        resp.processLogical(respDbc);
        expectLastCall();

        control.replay();
        proxy.send(req, dbc);
        control.verify();        
    }

}
