package org.objectweb.celtix.servicemodel;

import javax.xml.namespace.QName;
import junit.framework.TestCase;

public class JAXWSClassServiceBuilderTest extends TestCase {
    
    
    
    public void testBasicClass() throws Exception {
        Service service = JAXWSClassServiceBuilder
                .buildService(org.objectweb.hello_world_soap_http.Greeter.class);
        
        //check to make sure only the base operations (not the async versions) got generated
        assertEquals("did not generate the correct number of methods", 
                     6, service.getOperations().size());
        
        OperationInfo op = service.getOperation("sayHi");
        assertNotNull("Did not find operation sayHi", op);
        assertFalse("sayHi should not be oneway", op.isOneWay());
        assertEquals("sayHi parameter count wrong", 0, op.getInput().size());
        assertEquals("sayHi response count wrong", 1, op.getOutput().size());
        assertEquals("sayHi fault count wrong", 0, op.getFaults().size());
        
        op = service.getOperation("greetMeOneWay");
        assertNotNull("Did not find operation greetMeOneWay", op);
        assertTrue("greetMeOneWay should be oneway", op.isOneWay());
        assertEquals("greetMeOneWay parameter count wrong", 1, op.getInput().size());

        MessagePartInfo p = op.getInput().getMessageParts().get(0);
        assertEquals(
               new QName("http://objectweb.org/hello_world_soap_http/types", 
                          "requestType"), 
            p.getName());
        
        assertNull("greetMeOneWay should not have an output", op.getOutput());
        
        op = service.getOperation("testDocLitFault");
        assertNotNull("Did not find operation testDocLitFault", op);
        assertFalse("testDocLitFault should not be oneway", op.isOneWay());
        assertEquals("testDocLitFault parameter count wrong", 1, op.getInput().size());
        assertEquals("testDocLitFault response count wrong", 0, op.getOutput().size());
        assertEquals("testDocLitFault fault count wrong", 2, op.getFaults().size());
        
        
    }

}
