package org.objectweb.celtix.bus.ws.rm;

import java.io.IOException;
import java.io.InputStream;
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
import org.objectweb.celtix.ws.addressing.EndpointReferenceType;
import org.objectweb.celtix.ws.rm.Identifier;
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
      
        handler.getBinding();
        EasyMock.expectLastCall().andReturn(binding);  
        source.getSourcePolicies();
        EasyMock.expectLastCall().andReturn(sp);
        sp.getAcksTo();
        EasyMock.expectLastCall().andReturn(null);
        sp.getSequenceExpiration();
        EasyMock.expectLastCall().andReturn(null);
        sp.isIncludeOffer();
        EasyMock.expectLastCall().andReturn(false);
        handler.getClientBinding();
        EasyMock.expectLastCall().andReturn(binding).times(2);

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
      
        handler.getBinding();
        EasyMock.expectLastCall().andReturn(binding);  
        source.getSourcePolicies();
        EasyMock.expectLastCall().andReturn(sp);
        sp.getAcksTo();
        EasyMock.expectLastCall().andReturn(null);
        sp.getSequenceExpiration();
        EasyMock.expectLastCall().andReturn(null);
        sp.isIncludeOffer();
        EasyMock.expectLastCall().andReturn(true);   
        sp.getOfferedSequenceExpiration();
        EasyMock.expectLastCall().andReturn(null);
        source.generateSequenceIdentifier();
        expectLastCall().andReturn(offeredSid);
        handler.getClientBinding();
        EasyMock.expectLastCall().andReturn(binding).times(2);
        
        source.getHandler();
        expectLastCall().andReturn(handler);
        handler.getDestination();
        expectLastCall().andReturn(dest);
        dest.addSequence(isA(Sequence.class));

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
      
        handler.getBinding();
        EasyMock.expectLastCall().andReturn(binding);  
        source.getSourcePolicies();
        EasyMock.expectLastCall().andReturn(sp);
        sp.getAcksTo();
        EasyMock.expectLastCall().andReturn(null);
        sp.getSequenceExpiration();
        EasyMock.expectLastCall().andReturn(null);
        sp.isIncludeOffer();
        EasyMock.expectLastCall().andReturn(true);   
        sp.getOfferedSequenceExpiration();
        EasyMock.expectLastCall().andReturn(osd);
        source.generateSequenceIdentifier();
        expectLastCall().andReturn(offeredSid);
        handler.getClientBinding();
        EasyMock.expectLastCall().andReturn(binding).times(2);
        
        source.getHandler();
        expectLastCall().andReturn(handler);
        handler.getDestination();
        expectLastCall().andReturn(dest);

        control.replay();
        
        proxy.createSequence(source, RMUtils.createReference(Names.WSA_ANONYMOUS_ADDRESS), inSid);
        
        control.verify();
    }
    
    
    public void testTerminateSequenceOnClient() throws IOException, WSDLException, SequenceFault {
        TestSoapClientBinding binding = new TestSoapClientBinding(bus, epr);
        
        RMHandler handler = EasyMock.createMock(RMHandler.class);
        RMSource source = EasyMock.createMock(RMSource.class);

        handler.getBinding();
        EasyMock.expectLastCall().andReturn(binding);       
        for (int i = 0; i < 2; i++) {
            handler.getClientBinding();
            EasyMock.expectLastCall().andReturn(binding);
        }

        EasyMock.replay(handler);

        RMProxy proxy = new RMProxy(handler);

        Identifier sid = RMUtils.getWSRMFactory().createIdentifier();
        sid.setValue("TerminatedSequence");
        Sequence seq = new Sequence(sid, source, null);
        
        proxy.terminateSequence(seq);
    }
    
    public void testRequestAcknowledgement() throws IOException, WSDLException, SequenceFault {
        TestSoapClientBinding binding = new TestSoapClientBinding(bus, epr);
        
        RMHandler handler = EasyMock.createMock(RMHandler.class);
        RMSource source = EasyMock.createMock(RMSource.class);

        handler.getBinding();
        EasyMock.expectLastCall().andReturn(binding);       
        for (int i = 0; i < 2; i++) {
            handler.getClientBinding();
            EasyMock.expectLastCall().andReturn(binding);
        }

        EasyMock.replay(handler);

        RMProxy proxy = new RMProxy(handler);

        Identifier sid = RMUtils.getWSRMFactory().createIdentifier();
        sid.setValue("AckRequestedSequence");
        Sequence seq = new Sequence(sid, source, null);
        
        Collection<Sequence> seqs = new ArrayList<Sequence>();
        seqs.add(seq);
        
        proxy.requestAcknowledgement(seqs);
    }
    
    public void testSequenceInfoOnClient() throws IOException, WSDLException, SequenceFault {
        
        TestSoapClientBinding binding = new TestSoapClientBinding(bus, epr);

        TestClientTransport ct = binding.getClientTransport();
        InputStream is = getClass().getResourceAsStream("resources/spec/SequenceInfoResponse.xml");
        TestInputStreamContext istreamCtx = new TestInputStreamContext();
        istreamCtx.setInputStream(is);
        ct.setInputStreamMessageContext(istreamCtx);
        
        RMHandler handler = EasyMock.createMock(RMHandler.class);
        RMSource source = EasyMock.createMock(RMSource.class);

        handler.getBinding();
        EasyMock.expectLastCall().andReturn(binding);       
        for (int i = 0; i < 2; i++) {
            handler.getClientBinding();
            EasyMock.expectLastCall().andReturn(binding);
        }

        EasyMock.replay(handler);

        RMProxy service = new RMProxy(handler);

        Identifier sid = RMUtils.getWSRMFactory().createIdentifier();
        sid.setValue("TerminatedSequence");
        Sequence seq = new Sequence(sid, source, null);
        
        service.terminateSequence(seq);
    }
    
    
    
    
}
