package org.objectweb.celtix.bus.wsdl;

import java.net.URL;
import junit.framework.TestCase;


public class WSDLManagerInstrumentationTest extends TestCase {
    private static final String DEFINITION_NAME =  
        "Definition: {http://objectweb.org/hello_world_soap_http}HelloWorld ";
    private static final String SERVICE_NAME1 = 
        DEFINITION_NAME
        + "Service: {http://objectweb.org/hello_world_soap_http}SOAPServiceAddressing";
    private static final String PORTTYPE_NAME = 
        DEFINITION_NAME
        + "PortType: {http://objectweb.org/hello_world_soap_http}Greeter";
    private static final String BINDING_NAME =
        DEFINITION_NAME
        + "Binding: {http://objectweb.org/hello_world_soap_http}Greeter_SOAPBinding";
    
    private static final String[] OPERATIONS = {"sayHi", "greetMe", "greetMeSometime", 
                                                "greetMeOneWay", "testDocLitFault", 
                                                "testDocLitBare"};
    
    private static final String DEFINITION = "HelloWorld";
    
    private static final String PORTTYPE = "Greeter";
    
    private WSDLManagerImpl wsdlManager;
    private WSDLManagerInstrumentation wi;
                                                
    private void loadWSDL() throws Exception {
        URL url = getClass().getResource("/wsdl/hello_world.wsdl");
        wsdlManager = new WSDLManagerImpl(null);
        wsdlManager.getDefinition(url);
        wi = new WSDLManagerInstrumentation(wsdlManager);
    }
       
    
    public void testGetServices() throws Exception {
        loadWSDL();
        
        String[] services = wi.getServices();       
        assertEquals("The Services number is not right", 4, services.length);
        assertTrue("Get wrong Service Name ", services[2].compareTo(SERVICE_NAME1) == 0);        
    }
    
    public void testGetBindings() throws Exception {
        loadWSDL();
        
        String[] bindings = wi.getBindings();        
       
        assertEquals("The bindings number is not right", 1, bindings.length);
        assertTrue("Get wrong binding Name ", bindings[0].compareTo(BINDING_NAME) == 0);  
    }
    
    public void testGetPortTypes() throws Exception {
        loadWSDL();
        
        String[] ports = wi.getPortTypes();        
        assertEquals("The bindings number is not right", 1, ports.length);
        assertTrue("Get wrong binding Name ", ports[0].compareTo(PORTTYPE_NAME) == 0);  
    }
    
    public void testGetOperation() throws Exception {
        loadWSDL();
        
        String[] operations = wi.getOperation(DEFINITION, PORTTYPE);        
        assertEquals("The operation number is not right", OPERATIONS.length, operations.length);
        for (int i = 0; i < operations.length; i++) {
            assertTrue("Get wrong operation Name ", 
                       operations[i].compareTo("Operation: " + OPERATIONS[i]) == 0);
        }
        
    }
    
    public void testGetEndpoints() throws Exception {
        
    }
    
    
    

}
