package org.objectweb.celtix.bus.ws.rm;

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
import org.objectweb.celtix.bus.bindings.TestClientTransport;
import org.objectweb.celtix.bus.bindings.TestInputStreamContext;
import org.objectweb.celtix.bus.configuration.wsrm.SourcePolicyType;
import org.objectweb.celtix.transports.Transport;
import org.objectweb.celtix.ws.addressing.EndpointReferenceType;
import org.objectweb.celtix.ws.rm.Identifier;
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

    public void testCreateSequenceOnClientNoOfferIncluded() throws Exception {
        
        TestSoapClientBinding binding = new TestSoapClientBinding(bus, epr);
        TestClientTransport ct = binding.getClientTransport();
        InputStream is = getClass().getResourceAsStream("resources/spec/CreateSequenceResponse.xml");
        TestInputStreamContext istreamCtx = new TestInputStreamContext();
        istreamCtx.setInputStream(is);
        ct.setInputStreamMessageContext(istreamCtx);
        
        IMocksControl control = EasyMock.createNiceControl();
        
        RMHandler handler = control.createMock(RMHandler.class);
        RMProxy proxy = new RMProxy(handler);
        RMSource source = control.createMock(RMSource.class);
        SourcePolicyType sp = control.createMock(SourcePolicyType.class);
        
        Identifier sid = RMUtils.getWSRMFactory().createIdentifier();
        sid.setValue("s1");
        Identifier inSid = null;        
      
        expect(handler.getBinding()).andReturn(binding); 
        expect(handler.getTransport()).andReturn(ct);  
        expect(source.getSourcePolicies()).andReturn(sp);
        expect(sp.getAcksTo()).andReturn(null);
        expect(sp.getSequenceExpiration()).andReturn(null);
        expect(sp.isIncludeOffer()).andReturn(false);
        expect(handler.getClientBinding()).andReturn(binding).times(2);
        source.addSequence(EasyMock.isA(SourceSequence.class));
        expectLastCall();
        source.setCurrent((Identifier)EasyMock.isNull(), EasyMock.isA(SourceSequence.class));
        expectLastCall();
        
        control.replay();   
        proxy.createSequence(source, RMUtils.createReference(Names.WSA_ANONYMOUS_ADDRESS), inSid);
        control.verify();
    }
    
    public void testCreateSequenceOnClientOfferAccepted() throws Exception {
        TestSoapClientBinding binding = new TestSoapClientBinding(bus, epr);
        TestClientTransport ct = binding.getClientTransport();
        InputStream is = getClass().getResourceAsStream("resources/CreateSequenceResponseOfferAccepted.xml");
        TestInputStreamContext istreamCtx = new TestInputStreamContext();
        istreamCtx.setInputStream(is);
        ct.setInputStreamMessageContext(istreamCtx);
        
        IMocksControl control = EasyMock.createNiceControl();
        
        RMHandler handler = control.createMock(RMHandler.class);
        RMProxy proxy = new RMProxy(handler);
        RMSource source = control.createMock(RMSource.class);
        SourcePolicyType sp = control.createMock(SourcePolicyType.class);
        RMDestination dest = control.createMock(RMDestination.class);
        
        Identifier sid = RMUtils.getWSRMFactory().createIdentifier();
        sid.setValue("s1");
        Identifier inSid = null;  
        Duration osd = DatatypeFactory.newInstance().newDuration("PT24H");
        assertNotNull(osd);
        Identifier offeredSid = RMUtils.getWSRMFactory().createIdentifier();
        offeredSid.setValue("s1Offer");
      
        expect(handler.getBinding()).andReturn(binding);
        expect(handler.getTransport()).andReturn(ct);
        expect(source.getSourcePolicies()).andReturn(sp);
        expect(sp.getAcksTo()).andReturn(null);
        expect(sp.getSequenceExpiration()).andReturn(null);
        expect(sp.isIncludeOffer()).andReturn(true);   
        expect(sp.getOfferedSequenceExpiration()).andReturn(null);
        expect(source.generateSequenceIdentifier()).andReturn(offeredSid);
        expect(handler.getClientBinding()).andReturn(binding).times(2);
        source.addSequence(EasyMock.isA(SourceSequence.class));
        expectLastCall();
        source.setCurrent((Identifier)EasyMock.isNull(), EasyMock.isA(SourceSequence.class));
        expectLastCall();        
        expect(source.getHandler()).andReturn(handler);
        expect(handler.getDestination()).andReturn(dest);
        dest.addSequence(isA(DestinationSequence.class));

        control.replay(); 
        proxy.createSequence(source, RMUtils.createReference(Names.WSA_ANONYMOUS_ADDRESS), inSid);
        control.verify();
    }
    
    public void testCreateSequenceOnClientOfferRejected() throws Exception {
        TestSoapClientBinding binding = new TestSoapClientBinding(bus, epr);
        TestClientTransport ct = binding.getClientTransport();
        InputStream is = getClass().getResourceAsStream("resources/CreateSequenceResponseOfferAccepted.xml");
        TestInputStreamContext istreamCtx = new TestInputStreamContext();
        istreamCtx.setInputStream(is);
        ct.setInputStreamMessageContext(istreamCtx);
        
        IMocksControl control = EasyMock.createNiceControl();
        
        RMHandler handler = control.createMock(RMHandler.class);
        RMProxy proxy = new RMProxy(handler);
        RMSource source = control.createMock(RMSource.class);
        SourcePolicyType sp = control.createMock(SourcePolicyType.class);
        RMDestination dest = control.createMock(RMDestination.class);
        
        Identifier sid = RMUtils.getWSRMFactory().createIdentifier();
        sid.setValue("s1");
        Identifier inSid = null;  
        Duration osd = DatatypeFactory.newInstance().newDuration("PT24H");
        Identifier offeredSid = RMUtils.getWSRMFactory().createIdentifier();
        offeredSid.setValue("s1Offer");

        expect(handler.getBinding()).andReturn(binding);  
        expect(source.getSourcePolicies()).andReturn(sp);
        expect(sp.getAcksTo()).andReturn(null);
        expect(sp.getSequenceExpiration()).andReturn(null);
        expect(sp.isIncludeOffer()).andReturn(true);   
        expect(sp.getOfferedSequenceExpiration()).andReturn(osd);
        expect(source.generateSequenceIdentifier()).andReturn(offeredSid);
        expect(handler.getClientBinding()).andReturn(binding).times(2);        
        expect(source.getHandler()).andReturn(handler);
        expect(handler.getDestination()).andReturn(dest);

        control.replay();  
        proxy.createSequence(source, RMUtils.createReference(Names.WSA_ANONYMOUS_ADDRESS), inSid);   
        control.verify();
    }
    
    
    public void testTerminateSequenceOnClient() throws IOException, WSDLException, SequenceFault {
        TestSoapClientBinding binding = new TestSoapClientBinding(bus, epr);
        
        IMocksControl control = EasyMock.createNiceControl();
        RMHandler handler = control.createMock(RMHandler.class);

        handler.getBinding();
        EasyMock.expectLastCall().andReturn(binding); 
        handler.getTransport();
        expectLastCall().andReturn(binding.getClientTransport());
        for (int i = 0; i < 2; i++) {
            handler.getClientBinding();
            EasyMock.expectLastCall().andReturn(binding);
        }

        control.replay();

        RMProxy proxy = new RMProxy(handler);

        Identifier sid = RMUtils.getWSRMFactory().createIdentifier();
        sid.setValue("TerminatedSequence");
        SourceSequence seq = new SourceSequence(sid, null, null);
        
        proxy.terminateSequence(seq);
        
        control.verify();
    }
    
    public void testRequestAcknowledgement() throws IOException, WSDLException, SequenceFault {
        TestSoapClientBinding binding = new TestSoapClientBinding(bus, epr);
        
        IMocksControl control = EasyMock.createNiceControl();
        RMHandler handler = control.createMock(RMHandler.class);

        handler.getBinding();
        EasyMock.expectLastCall().andReturn(binding); 
        handler.getTransport();
        expectLastCall().andReturn(binding.getClientTransport());
        for (int i = 0; i < 2; i++) {
            handler.getClientBinding();
            EasyMock.expectLastCall().andReturn(binding);
        }

        control.replay();

        RMProxy proxy = new RMProxy(handler);

        Identifier sid = RMUtils.getWSRMFactory().createIdentifier();
        sid.setValue("AckRequestedSequence");
        SourceSequence seq = new SourceSequence(sid, null, null);
        
        Collection<SourceSequence> seqs = new ArrayList<SourceSequence>();
        seqs.add(seq);
        
        proxy.requestAcknowledgment(seqs);
        
        control.verify();
    }
    
    public void testLastMessage() throws IOException, WSDLException, SequenceFault {
        TestSoapClientBinding binding = new TestSoapClientBinding(bus, epr);
        
        IMocksControl control = EasyMock.createNiceControl();
        RMHandler handler = control.createMock(RMHandler.class);
     
        handler.getBinding();
        expectLastCall().andReturn(binding); 
        handler.getTransport();
        expectLastCall().andReturn(binding.getClientTransport());
        for (int i = 0; i < 2; i++) {
            handler.getClientBinding();
            expectLastCall().andReturn(binding);
        }

        control.replay();

        RMProxy proxy = new RMProxy(handler);

        Identifier sid = RMUtils.getWSRMFactory().createIdentifier();
        sid.setValue("LastMessageSequence");
        SourceSequence seq = new SourceSequence(sid, null, null);        
        proxy.lastMessage(seq); 
        
        control.verify();
    }
    
    public void testAcknowledge() throws IOException, WSDLException, SequenceFault {
        TestSoapClientBinding binding = new TestSoapClientBinding(bus, epr);
        IMocksControl control = EasyMock.createNiceControl();
        RMHandler handler = control.createMock(RMHandler.class);
        RMDestination dest = control.createMock(RMDestination.class);
        RMAssertionType rma = control.createMock(RMAssertionType.class);
        Transport transport = control.createMock(Transport.class);

        dest.getRMAssertion();
        expectLastCall().andReturn(rma).times(2);
        rma.getAcknowledgementInterval();
        expectLastCall().andReturn(null).times(2);
        dest.getAcksPolicy();
        expectLastCall().andReturn(null).times(2);
        
        handler.getBinding();
        expectLastCall().andReturn(binding);       
        handler.getTransport();
        expectLastCall().andReturn(transport);
        for (int i = 0; i < 2; i++) {
            handler.getClientBinding();
            expectLastCall().andReturn(binding);
        }

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
    }
    
    public void testSequenceInfoOnClient() throws IOException, WSDLException, SequenceFault {
        
        TestSoapClientBinding binding = new TestSoapClientBinding(bus, epr);
        TestClientTransport ct = binding.getClientTransport();
        InputStream is = getClass().getResourceAsStream("resources/spec/SequenceInfoResponse.xml");
        TestInputStreamContext istreamCtx = new TestInputStreamContext();
        istreamCtx.setInputStream(is);
        ct.setInputStreamMessageContext(istreamCtx);
        
        IMocksControl control = EasyMock.createNiceControl();
        RMHandler handler = control.createMock(RMHandler.class);
        handler.getBinding();
        expectLastCall().andReturn(binding);
        handler.getTransport();
        expectLastCall().andReturn(ct);
        for (int i = 0; i < 2; i++) {
            handler.getClientBinding();
            expectLastCall().andReturn(binding);
        }

        control.replay();

        RMProxy service = new RMProxy(handler);

        Identifier sid = RMUtils.getWSRMFactory().createIdentifier();
        sid.setValue("TerminatedSequence");
        SourceSequence seq = new SourceSequence(sid, null, null);
        
        service.terminateSequence(seq);
        
        control.verify();
    }    
    
}
