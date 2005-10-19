package org.objectweb.celtix.bus.bindings.soap;

import java.net.URL;

import javax.xml.namespace.QName;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPMessageContext;

import junit.framework.TestCase;

import org.objectweb.celtix.Bus;
import org.objectweb.celtix.addressing.EndpointReferenceType;
import org.objectweb.celtix.bindings.BindingFactory;
import org.objectweb.celtix.bindings.ClientBinding;
import org.objectweb.celtix.context.GenericMessageContext;
import org.objectweb.celtix.wsdl.EndpointReferenceUtils;

public class SoapBindingFactoryTest extends TestCase {

    public SoapBindingFactoryTest(String arg0) {
        super(arg0);
    }

    public static void main(String[] args) {
        junit.textui.TestRunner.run(SoapBindingFactoryTest.class);
    }
    
    protected void setUp() throws Exception {
        super.setUp();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testCreateClientBinding() throws Exception {
        Bus bus = Bus.init(new String[0]);
        BindingFactory factory = 
            bus.getBindingManager().getBindingFactory(
                "http://schemas.xmlsoap.org/wsdl/soap/");
        assertNotNull(factory);
        
        URL url = getClass().getClassLoader().getResource(".");
        URL wsdlUrl = new URL(url, "../classes-tests/wsdl/hello_world.wsdl");
        assertNotNull(wsdlUrl);
        QName serviceName = new QName("http://objectweb.org/hello_world_soap_http", "SOAPService");
        EndpointReferenceType address = EndpointReferenceUtils
            .getEndpointReference(wsdlUrl, serviceName, "SoapPort");
        
        ClientBinding clientBinding = factory.createClientBinding(address);
        assertNotNull(clientBinding);
        assertTrue(SOAPClientBinding.class.isInstance(clientBinding));
        
        SOAPClientBinding soapClientBinding = (SOAPClientBinding)clientBinding;
        MessageContext msgCtx = soapClientBinding.createBindingMessageContext(
            new GenericMessageContext());
        assertNotNull(msgCtx);
        assertTrue(SOAPMessageContext.class.isInstance(msgCtx));
        
        bus.shutdown(true);       
    }
    
}
