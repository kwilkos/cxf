package org.objectweb.celtix.ws.rm;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;

import javax.wsdl.WSDLException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.Duration;
import javax.xml.namespace.QName;

import junit.framework.TestCase;

import org.easymock.IMocksControl;
import org.easymock.classextension.EasyMock;
import org.objectweb.celtix.Bus;
import org.objectweb.celtix.BusException;
import org.objectweb.celtix.bus.configuration.wsrm.SourcePolicyType;
import org.objectweb.celtix.ws.addressing.AddressingProperties;
import org.objectweb.celtix.ws.addressing.AddressingPropertiesImpl;
import org.objectweb.celtix.ws.addressing.AttributedURIType;
import org.objectweb.celtix.ws.addressing.ContextUtils;
import org.objectweb.celtix.ws.addressing.EndpointReferenceType;
import org.objectweb.celtix.ws.rm.policy.RMAssertionType;
import org.objectweb.celtix.ws.rm.wsdl.SequenceFault;
import org.objectweb.celtix.wsdl.EndpointReferenceUtils;

import static org.easymock.classextension.EasyMock.*;

public class RMProxyTest extends TestCase {

    Bus bus;
    EndpointReferenceType epr;

    public void setUp() throws BusException {
        bus = Bus.init();
        URL wsdlUrl = getClass().getResource("/wsdl/hello_world.wsdl");
        QName serviceName = new QName("http://objectweb.org/hello_world_soap_http", "SOAPService");
        epr = EndpointReferenceUtils.getEndpointReference(wsdlUrl, serviceName, "SoapPort");
        
    }

    public void tearDown() throws BusException {
        bus.shutdown(true);
    }

    public void testCreateSequenceNoOfferIncluded() throws Exception {
        
        TestSoapClientBinding binding = new TestSoapClientBinding(bus, epr);
        TestClientTransport ct = binding.getClientTransport();
        
        IMocksControl control = EasyMock.createNiceControl();
        
        RMHandler handler = control.createMock(RMHandler.class);
        RMProxy proxy = new RMProxy(handler);
        RMSource source = control.createMock(RMSource.class);
        ConfigurationHelper ch = control.createMock(ConfigurationHelper.class);
        SourcePolicyType sp = control.createMock(SourcePolicyType.class);
        
        Identifier sid = RMUtils.getWSRMFactory().createIdentifier();
        sid.setValue("s1");
      
        expect(handler.getBinding()).andReturn(binding);
        expect(handler.getTransport()).andReturn(ct);
        expect(source.getHandler()).andReturn(handler);
        expect(handler.getConfigurationHelper()).andReturn(ch); 
        expect(ch.getSourcePolicies()).andReturn(sp);
        expect(sp.getAcksTo()).andReturn(null);
        expect(sp.getSequenceExpiration()).andReturn(null);
        expect(sp.isIncludeOffer()).andReturn(false);
        expect(handler.getBinding()).andReturn(binding).times(2);

        control.replay();
        proxy.createSequence(source,
                             TestUtils.getEPR("target"),
                             RMUtils.createReference(Names.WSA_ANONYMOUS_ADDRESS),
                             ContextUtils.WSA_OBJECT_FACTORY.createRelatesToType());
        control.verify();
        assertTrue("expected send",  binding.isSent());
    }
    
    public void testCreateSequenceNoExpires() throws Exception {
        TestSoapClientBinding binding = new TestSoapClientBinding(bus, epr);
        TestClientTransport ct = binding.getClientTransport();
        
        IMocksControl control = EasyMock.createNiceControl();
        
        RMHandler handler = control.createMock(RMHandler.class);
        RMProxy proxy = new RMProxy(handler);
        RMSource source = control.createMock(RMSource.class);
        ConfigurationHelper ch = control.createMock(ConfigurationHelper.class);
        SourcePolicyType sp = control.createMock(SourcePolicyType.class);
        
        Identifier sid = RMUtils.getWSRMFactory().createIdentifier();
        sid.setValue("s1");
        Duration osd = DatatypeFactory.newInstance().newDuration("PT24H");
        assertNotNull(osd);
        Identifier offeredSid = RMUtils.getWSRMFactory().createIdentifier();
        offeredSid.setValue("s1Offer");
      
        expect(handler.getBinding()).andReturn(binding);
        expect(handler.getTransport()).andReturn(ct);
        expect(source.getHandler()).andReturn(handler);
        expect(handler.getConfigurationHelper()).andReturn(ch);
        expect(ch.getSourcePolicies()).andReturn(sp);
        expect(sp.getAcksTo()).andReturn(null);
        expect(sp.getSequenceExpiration()).andReturn(null);
        expect(sp.isIncludeOffer()).andReturn(true);   
        expect(sp.getOfferedSequenceExpiration()).andReturn(null);
        expect(source.generateSequenceIdentifier()).andReturn(offeredSid);
        expect(handler.getBinding()).andReturn(binding).times(2);

        control.replay(); 
        proxy.createSequence(source,
                             TestUtils.getEPR("target"),
                             RMUtils.createReference(Names.WSA_ANONYMOUS_ADDRESS),
                             ContextUtils.WSA_OBJECT_FACTORY.createRelatesToType());
        control.verify();
        assertTrue("expected send",  binding.isSent());
    }
    
    public void testCreateSequenceExpires() throws Exception {
        TestSoapClientBinding binding = new TestSoapClientBinding(bus, epr);
        TestClientTransport ct = binding.getClientTransport();
        
        IMocksControl control = EasyMock.createNiceControl();
        
        RMHandler handler = control.createMock(RMHandler.class);
        RMProxy proxy = new RMProxy(handler);
        RMSource source = control.createMock(RMSource.class);
        ConfigurationHelper ch = control.createMock(ConfigurationHelper.class);
        SourcePolicyType sp = control.createMock(SourcePolicyType.class);
        
        Identifier sid = RMUtils.getWSRMFactory().createIdentifier();
        sid.setValue("s1");
        Duration osd = DatatypeFactory.newInstance().newDuration("PT24H");
        Identifier offeredSid = RMUtils.getWSRMFactory().createIdentifier();
        offeredSid.setValue("s1Offer");

        expect(handler.getBinding()).andReturn(binding);
        expect(handler.getTransport()).andReturn(ct);
        expect(source.getHandler()).andReturn(handler);
        expect(handler.getConfigurationHelper()).andReturn(ch);
        expect(ch.getSourcePolicies()).andReturn(sp);
        expect(sp.getAcksTo()).andReturn(null);
        expect(sp.getSequenceExpiration()).andReturn(null);
        expect(sp.isIncludeOffer()).andReturn(true);   
        expect(sp.getOfferedSequenceExpiration()).andReturn(osd);
        expect(source.generateSequenceIdentifier()).andReturn(offeredSid);
        expect(handler.getBinding()).andReturn(binding).times(2);        

        control.replay();
        proxy.createSequence(source,
                             TestUtils.getEPR("target"),
                             RMUtils.createReference(Names.WSA_ANONYMOUS_ADDRESS),
                             ContextUtils.WSA_OBJECT_FACTORY.createRelatesToType());
        control.verify();
        assertTrue("expected send",  binding.isSent());
    }
    
    public void testCreateSequenceResponse() throws Exception {
        TestSoapClientBinding binding = new TestSoapClientBinding(bus, epr);
        TestClientTransport ct = binding.getClientTransport();
        
        IMocksControl control = EasyMock.createNiceControl();
        
        RMHandler handler = control.createMock(RMHandler.class);
        RMProxy proxy = new RMProxy(handler);
        
        expect(handler.getBinding()).andReturn(binding);
        expect(handler.getTransport()).andReturn(ct);
        expect(handler.getBinding()).andReturn(binding).times(2);        

        control.replay();
        
        AddressingProperties maps = new AddressingPropertiesImpl();
        EndpointReferenceType replyTo = TestUtils.getEPR("response1");
        maps.setReplyTo(replyTo);
        AttributedURIType messageID =
            ContextUtils.WSA_OBJECT_FACTORY.createAttributedURIType();
        messageID.setValue("msg1");
        maps.setMessageID(messageID);
        
        CreateSequenceResponseType csr =
            RMUtils.getWSRMFactory().createCreateSequenceResponseType();

        proxy.createSequenceResponse(maps, csr);
        
        control.verify();
        assertTrue("expected send",  binding.isSent());
    }
    
    public void testTerminateSequence() throws IOException, WSDLException, SequenceFault {
        TestSoapClientBinding binding = new TestSoapClientBinding(bus, epr);
        
        IMocksControl control = EasyMock.createNiceControl();
        RMHandler handler = control.createMock(RMHandler.class);

        handler.getBinding();
        EasyMock.expectLastCall().andReturn(binding).times(3);
        handler.getClientBinding();
        EasyMock.expectLastCall().andReturn(binding).times(4);
        
        RMSource source = control.createMock(RMSource.class);
        handler.getSource();
        EasyMock.expectLastCall().andReturn(source);
        source.removeSequence(EasyMock.isA(SourceSequence.class));
        EasyMock.expectLastCall();
     

        control.replay();

        RMProxy proxy = new RMProxy(handler);

        Identifier sid = RMUtils.getWSRMFactory().createIdentifier();
        sid.setValue("TerminatedSequence");
        SourceSequence seq = new SourceSequence(sid, null, null);
        
        proxy.terminateSequence(seq);
        
        control.verify();
        assertTrue("expected send",  binding.isSent());
    }
    
    public void testRequestAcknowledgement() throws IOException, WSDLException, SequenceFault {
        TestSoapClientBinding binding = new TestSoapClientBinding(bus, epr);
        
        IMocksControl control = EasyMock.createNiceControl();
        RMHandler handler = control.createMock(RMHandler.class);

        handler.getBinding();
        EasyMock.expectLastCall().andReturn(binding).times(3);
        handler.getClientBinding();
        EasyMock.expectLastCall().andReturn(binding).times(4);

        control.replay();

        RMProxy proxy = new RMProxy(handler);

        Identifier sid = RMUtils.getWSRMFactory().createIdentifier();
        sid.setValue("AckRequestedSequence");
        SourceSequence seq = new SourceSequence(sid, null, null);
        seq.setTarget(TestUtils.getEPR("target"));
        
        Collection<SourceSequence> seqs = new ArrayList<SourceSequence>();
        seqs.add(seq);
        
        proxy.requestAcknowledgment(seqs);
        
        control.verify();
        assertTrue("expected send",  binding.isSent());
    }
    
    public void testLastMessage() throws IOException, WSDLException, SequenceFault {
        TestSoapClientBinding binding = new TestSoapClientBinding(bus, epr);
        
        IMocksControl control = EasyMock.createNiceControl();
        RMHandler handler = control.createMock(RMHandler.class);
     
        handler.getBinding();
        EasyMock.expectLastCall().andReturn(binding).times(3);
        handler.getClientBinding();
        EasyMock.expectLastCall().andReturn(binding).times(4);

        control.replay();

        RMProxy proxy = new RMProxy(handler);

        Identifier sid = RMUtils.getWSRMFactory().createIdentifier();
        sid.setValue("LastMessageSequence");
        SourceSequence seq = new SourceSequence(sid, null, null);        
        seq.setTarget(TestUtils.getEPR("target"));
        proxy.lastMessage(seq);
        
        control.verify();
        assertTrue("expected send",  binding.isSent());
    }
    
    public void testAcknowledge() throws IOException, WSDLException, SequenceFault {
        TestSoapClientBinding binding = new TestSoapClientBinding(bus, epr);
        IMocksControl control = EasyMock.createNiceControl();
        RMHandler handler = control.createMock(RMHandler.class);
        ConfigurationHelper ch = control.createMock(ConfigurationHelper.class);
        RMDestination dest = control.createMock(RMDestination.class);
        RMAssertionType rma = control.createMock(RMAssertionType.class);

        expect(dest.getHandler()).andReturn(handler).times(6);
        expect(handler.getConfigurationHelper()).andReturn(ch).times(4);
        expect(handler.getStore()).andReturn(null).times(2);
        expect(ch.getRMAssertion()).andReturn(rma).times(2);
        expect(rma.getAcknowledgementInterval()).andReturn(null).times(2);
        expect(ch.getAcksPolicy()).andReturn(null).times(2);
        
        handler.getBinding();
        EasyMock.expectLastCall().andReturn(binding).times(3);
        handler.getClientBinding();
        EasyMock.expectLastCall().andReturn(binding).times(2);
                                                    
        control.replay();
        
        RMProxy proxy = new RMProxy(handler);

        Identifier sid = RMUtils.getWSRMFactory().createIdentifier();
        sid.setValue("Acknowledge");
        DestinationSequence seq = new DestinationSequence(sid, 
                                    RMUtils.createReference("http://localhost:9999/decoupled"), dest);
        seq.acknowledge(BigInteger.ONE);
        seq.acknowledge(BigInteger.TEN);       
        proxy.acknowledge(seq); 
        
        control.verify();
        assertTrue("expected send",  binding.isSent());
    }
    
    public void testSequenceInfo() throws IOException, WSDLException, SequenceFault {
        
        TestSoapClientBinding binding = new TestSoapClientBinding(bus, epr);
        TestClientTransport ct = binding.getClientTransport();
        InputStream is = getClass().getResourceAsStream("resources/spec/SequenceInfoResponse.xml");
        TestInputStreamContext istreamCtx = new TestInputStreamContext();
        istreamCtx.setInputStream(is);
        ct.setInputStreamMessageContext(istreamCtx);
        
        IMocksControl control = EasyMock.createNiceControl();
        RMHandler handler = control.createMock(RMHandler.class);
        handler.getBinding();
        EasyMock.expectLastCall().andReturn(binding).times(3);
        handler.getClientBinding();
        EasyMock.expectLastCall().andReturn(binding).times(4);
        
        RMSource source = control.createMock(RMSource.class);
        handler.getSource();
        EasyMock.expectLastCall().andReturn(source);
        source.removeSequence(EasyMock.isA(SourceSequence.class));
        EasyMock.expectLastCall();

        control.replay();

        RMProxy service = new RMProxy(handler);

        Identifier sid = RMUtils.getWSRMFactory().createIdentifier();
        sid.setValue("TerminatedSequence");
        SourceSequence seq = new SourceSequence(sid, null, null);
        
        service.terminateSequence(seq);
        
        control.verify();
        assertTrue("expected send",  binding.isSent());
    }    

    public void testCanSendClient() throws IOException, WSDLException {
        TestSoapClientBinding binding = new TestSoapClientBinding(bus, epr);

        IMocksControl control = EasyMock.createNiceControl();
        RMHandler handler = control.createMock(RMHandler.class);
        handler.getClientBinding();
        EasyMock.expectLastCall().andReturn(binding).times(6);

        control.replay();

        RMProxy service = new RMProxy(handler);
        
        assertTrue("expected can send",
                   service.canSend(TestUtils.getEPR("target")));
        assertTrue("expected can send",
                   service.canSend(TestUtils.getOldEPR("target")));
        binding.discardTransport();
        assertFalse("unexpected can send",
                    service.canSend(TestUtils.getEPR("target")));
        assertTrue("expected can send",
                    service.canSend(TestUtils.getOldEPR("target")));
        
        control.verify();
    }
    
    public void testCanSendServer() throws IOException, WSDLException {
        IMocksControl control = EasyMock.createNiceControl();
        RMHandler handler = control.createMock(RMHandler.class);
        handler.getClientBinding();
        EasyMock.expectLastCall().andReturn(null).times(4);

        control.replay();

        RMProxy service = new RMProxy(handler);
                
        assertTrue("expected can send", service.canSend(TestUtils.getEPR("target")));
        String anon = org.objectweb.celtix.ws.addressing.Names.WSA_ANONYMOUS_ADDRESS;
        assertFalse("unexpected can send",
                    service.canSend(EndpointReferenceUtils.getEndpointReference(anon)));
        assertTrue("expected can send", service.canSend(TestUtils.getOldEPR("target")));
        assertFalse("unexpected can send",
                    service.canSend(RMUtils.createReference(Names.WSA_ANONYMOUS_ADDRESS)));
        
        control.verify();
    }    
    
    public void testOfferedIdentifier() throws IOException, WSDLException {
        IMocksControl control = EasyMock.createNiceControl();
        RMHandler handler = control.createMock(RMHandler.class);
        OfferType offer = control.createMock(OfferType.class);
        Identifier id = control.createMock(Identifier.class);
        offer.getIdentifier();
        EasyMock.expectLastCall().andReturn(id);

        control.replay();

        RMProxy service = new RMProxy(handler);
                
        assertNull("unexpected offered ID", service.getOfferedIdentifier());
        service.setOfferedIdentifier(offer);
        assertSame("unexpected offered ID",
                   id,
                   service.getOfferedIdentifier());
        
        control.verify();
    }    

}
