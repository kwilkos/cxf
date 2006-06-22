package org.objectweb.celtix.bindings.xml;

import javax.xml.ws.Binding;

import junit.framework.TestCase;

import org.objectweb.celtix.Bus;
import org.objectweb.celtix.bindings.BindingFactory;
import org.objectweb.celtix.bindings.ClientBinding;
import org.objectweb.celtix.ws.addressing.EndpointReferenceType;

public class XMLBindingFactoryTest extends TestCase {

    private TestUtils testUtils = new TestUtils();
    
    public XMLBindingFactoryTest(String arg0) {
        super(arg0);
    }

    public static void main(String[] args) {
        junit.textui.TestRunner.run(XMLBindingFactoryTest.class);
    }

    public void setUp() {
        testUtils = new TestUtils();
    }
    
    public void testCreateClientBinding() throws Exception {
        Bus bus = Bus.init(new String[0]);
        BindingFactory factory = 
            bus.getBindingManager().getBindingFactory(
                "http://celtix.objectweb.org/bindings/xmlformat");
        assertNotNull(factory);
        
        EndpointReferenceType address = testUtils.getEndpointReference();
        
        ClientBinding clientBinding = factory.createClientBinding(address);
        assertNotNull(clientBinding);
        assertTrue(XMLClientBinding.class.isInstance(clientBinding));
        
        XMLClientBinding xmlClientBinding = (XMLClientBinding)clientBinding;
        Binding b = xmlClientBinding.getBinding();
        assertNotNull(b);
        assertTrue(XMLBindingImpl.class.isInstance(b));
               
        bus.shutdown(true);       
    }
}
