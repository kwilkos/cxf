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
import org.objectweb.celtix.ws.addressing.EndpointReferenceType;
import org.objectweb.celtix.ws.rm.Identifier;
import org.objectweb.celtix.wsdl.EndpointReferenceUtils;

public class RMServiceTest extends TestCase {

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

    public void testCreateSequenceOnClient() throws IOException, WSDLException {
        TestClientBinding binding = new TestClientBinding(bus, epr);
        TestClientTransport ct = binding.getClientTransport();
        InputStream is = getClass().getResourceAsStream("resources/spec/CreateSequenceResponse.xml");
        TestInputStreamContext istreamCtx = new TestInputStreamContext();
        istreamCtx.setInputStream(is);
        ct.setInputStreamMessageContext(istreamCtx);
        System.out.println("Preconfigured input stream context: " + istreamCtx);
        
        SourcePolicyType sp = RMUtils.getWSRMConfFactory().createSourcePolicyType();

        RMHandler handler = EasyMock.createMock(RMHandler.class);
        RMSource source = EasyMock.createMock(RMSource.class);

        handler.getBinding();
        EasyMock.expectLastCall().andReturn(binding);       
        for (int i = 0; i < 2; i++) {
            handler.getClientBinding();
            EasyMock.expectLastCall().andReturn(binding);
        }
        source.getSourcePolicies();
        EasyMock.expectLastCall().andReturn(sp);

        EasyMock.replay(handler);
        EasyMock.replay(source);

        RMService service = new RMService(handler);

        service.createSequence(source);

    }
    
    public void testTerminateSequenceOnClient() throws IOException, WSDLException {
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

        RMService service = new RMService(handler);

        Identifier sid = RMUtils.getWSRMFactory().createIdentifier();
        sid.setValue("TerminatedSequence");
        Sequence seq = new Sequence(sid, source, null);
        
        service.terminateSequence(seq);
    }
    
    
}
