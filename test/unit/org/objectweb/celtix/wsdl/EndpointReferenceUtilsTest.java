package org.objectweb.celtix.wsdl;

import javax.wsdl.Definition;
import javax.wsdl.Port;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;

import junit.framework.TestCase;

import org.objectweb.celtix.Bus;
import org.objectweb.celtix.addressing.EndpointReferenceType;
import org.objectweb.celtix.addressing.ObjectFactory;

public class EndpointReferenceUtilsTest extends TestCase {

    public EndpointReferenceUtilsTest(String arg0) {
        super(arg0);
    }

    public static void main(String[] args) {
        junit.textui.TestRunner.run(EndpointReferenceUtilsTest.class);
    }

    protected void setUp() throws Exception {
        super.setUp();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }
    
    
    public void testGetWSDLDefinition() throws Exception  {
        Bus bus = Bus.init(new String[0]);
        
        JAXBContext context = JAXBContext.newInstance(new Class[] {
            ObjectFactory.class,
            org.objectweb.celtix.addressing.wsdl.ObjectFactory.class
        });
        Unmarshaller u = context.createUnmarshaller();
        
        EndpointReferenceType ref = (EndpointReferenceType) 
            ((JAXBElement<?>)u.unmarshal(
                    getClass().getResource("resources/reference1.xml"))).getValue();
        
        ref.getMetadata().getOtherAttributes().put(
            new QName("http://www.w3.org/2004/08/wsdl-instance", "wsdlLocation"),
            getClass().getResource("/org/objectweb/celtix/resources/hello_world.wsdl").toString());
        
        
        Definition def = EndpointReferenceUtils.getWSDLDefinition(bus.getWSDLManager(),
                                                 ref);
        assertNotNull("Could not load wsdl", def);
        
        
        Port port = EndpointReferenceUtils.getPort(bus.getWSDLManager(), ref);
        assertNotNull("Could not find port", port);             
    }
    public void testGetWSDLDefinitionEmbeddedWsdl() throws Exception  {
        Bus bus = Bus.init(new String[0]);
        
        JAXBContext context = JAXBContext.newInstance(new Class[] {
            ObjectFactory.class,
            org.objectweb.celtix.addressing.wsdl.ObjectFactory.class
        });
        Unmarshaller u = context.createUnmarshaller();
        
        EndpointReferenceType ref = (EndpointReferenceType) 
            ((JAXBElement<?>)u.unmarshal(
                    getClass().getResource("resources/reference2.xml"))).getValue();
        
        Definition def = EndpointReferenceUtils.getWSDLDefinition(bus.getWSDLManager(),
                                                                  ref);
        assertNotNull("Could not load wsdl", def);
                         
                         
        Port port = EndpointReferenceUtils.getPort(bus.getWSDLManager(), ref);
        assertNotNull("Could not find port", port);             
    }    

}
