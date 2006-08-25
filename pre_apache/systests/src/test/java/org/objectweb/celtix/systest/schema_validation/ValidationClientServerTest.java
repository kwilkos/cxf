package org.objectweb.celtix.systest.schema_validation;

import java.io.Serializable;
import java.net.URL;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.ws.ProtocolException;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.objectweb.celtix.systest.common.ClientServerSetupBase;
import org.objectweb.celtix.systest.common.ClientServerTestBase;
import org.objectweb.schema_validation.SchemaValidation;
import org.objectweb.schema_validation.SchemaValidationService;
import org.objectweb.schema_validation.types.ComplexStruct;
import org.objectweb.schema_validation.types.OccuringStruct;

public class ValidationClientServerTest extends ClientServerTestBase {

    private final QName serviceName = new QName("http://objectweb.org/schema_validation",
                                                "SchemaValidationService");    
    private final QName portName = new QName("http://objectweb.org/schema_validation",
                                             "SoapPort");

    public static Test suite() throws Exception {
        TestSuite suite = new TestSuite(ValidationClientServerTest.class);
        return new ClientServerSetupBase(suite) {
            public void startServers() throws Exception {
                assertTrue("server did not launch correctly", launchServer(ValidationServer.class));
            }
            
            public void setUp() throws Exception {
                // set up configuration to enable schema validation
                URL url = getClass().getResource("celtix-config.xml"); 
                assertNotNull("cannot find test resource", url);
                configFileName = url.toString(); 
                super.setUp();
            }
        };
    }

    // TODO : Change this test so that we test the combinations of
    // client and server with schema validation enabled/disabled...
    // Only tests client side validation enabled/server side disabled.
    public void testSchemaValidation() throws Exception {
        URL wsdl = getClass().getResource("/wsdl/schema_validation.wsdl");
        assertNotNull(wsdl);
        
        SchemaValidationService service = new SchemaValidationService(wsdl, serviceName);
        assertNotNull(service);
        
        SchemaValidation validation = service.getPort(portName, SchemaValidation.class);

        ComplexStruct complexStruct = new ComplexStruct();
        complexStruct.setElem1("one");
        // Don't initialize a member of the structure.  
        // Client side validation should throw an exception.
        // complexStruct.setElem2("two");
        complexStruct.setElem3(3);
        try {       
            /*boolean result =*/ validation.setComplexStruct(complexStruct);
            fail("Set ComplexStruct hould have thrown ProtocolException");
        } catch (ProtocolException e) {
            //System.out.println(e.getMessage()); 
        }

        OccuringStruct occuringStruct = new OccuringStruct();
        // Populate the list in the wrong order.
        // Client side validation should throw an exception.
        List<Serializable> floatIntStringList = occuringStruct.getVarFloatAndVarIntAndVarString();
        floatIntStringList.add(new Integer(42));
        floatIntStringList.add(new Float(4.2f));
        floatIntStringList.add("Goofus and Gallant");
        try {       
            /*boolean result =*/ validation.setOccuringStruct(occuringStruct);
            fail("Set OccuringStruct hould have thrown ProtocolException");
        } catch (ProtocolException e) {
            //System.out.println(e.getMessage());
        }

        try {
            // The server will attempt to return an invalid ComplexStruct
            // When validation is disabled on the server side, we'll get the
            // exception while unmarshalling the invalid response.
            /*complexStruct =*/ validation.getComplexStruct("Hello");
            fail("Get ComplexStruct should have thrown ProtocolException");
        } catch (ProtocolException e) {
            //System.out.println(e.getMessage()); 
        }
        
        try {
            // The server will attempt to return an invalid OccuringStruct
            // When validation is disabled on the server side, we'll get the
            // exception while unmarshalling the invalid response.
            /*occuringStruct =*/ validation.getOccuringStruct("World");
            fail("Get OccuringStruct should have thrown ProtocolException");
        } catch (ProtocolException e) {
            //System.out.println(e.getMessage()); 
        }
    } 

    public static void main(String[] args) {
        junit.textui.TestRunner.run(ValidationClientServerTest.class);
    }
    
}
