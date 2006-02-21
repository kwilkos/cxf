package org.objectweb.celtix.bus.ws.rm;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import javax.wsdl.WSDLException;
import javax.xml.namespace.QName;

import junit.framework.TestCase;

import org.easymock.classextension.EasyMock;
import org.objectweb.celtix.Bus;
import org.objectweb.celtix.BusException;
import org.objectweb.celtix.bus.configuration.wsrm.SourcePolicyType;
import org.objectweb.celtix.configuration.Configuration;
import org.objectweb.celtix.ws.addressing.EndpointReferenceType;
import org.objectweb.celtix.ws.rm.Identifier;
import org.objectweb.celtix.ws.rm.wsdl.SequenceFault;
import org.objectweb.celtix.wsdl.EndpointReferenceUtils;

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

    public void testCreateSequenceOnClient() throws IOException, WSDLException, 
        SequenceFault, NoSuchMethodException {
        
        TestClientBinding binding = new TestClientBinding(bus, epr);
        TestClientTransport ct = binding.getClientTransport();
        InputStream is = getClass().getResourceAsStream("resources/spec/CreateSequenceResponse.xml");
        TestInputStreamContext istreamCtx = new TestInputStreamContext();
        istreamCtx.setInputStream(is);
        ct.setInputStreamMessageContext(istreamCtx);

        RMHandler handler = EasyMock.createMock(RMHandler.class);
        Configuration config = EasyMock.createMock(Configuration.class);        
        RMSource source = new RMSource(handler);
        
        handler.getBinding();
        EasyMock.expectLastCall().andReturn(binding);       
        for (int i = 0; i < 2; i++) {
            handler.getClientBinding();
            EasyMock.expectLastCall().andReturn(binding);
        }
        handler.getConfiguration();
        EasyMock.expectLastCall().andReturn(config);
        config.getObject(SourcePolicyType.class, "sourcePolicies");
        EasyMock.expectLastCall().andReturn(null);
        
        EasyMock.replay(handler);
        EasyMock.replay(config);

        RMProxy proxy = new RMProxy(handler);

        proxy.createSequence(source);

    }
    
    public void testTerminateSequenceOnClient() throws IOException, WSDLException, SequenceFault {
        TestClientBinding binding = new TestClientBinding(bus, epr);
        
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
    
    public void testSequenceInfoOnClient() throws IOException, WSDLException, SequenceFault {
        
        TestClientBinding binding = new TestClientBinding(bus, epr);

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
