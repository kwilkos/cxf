package demo.ws_addressing.client;

import java.io.File;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.Map;
import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;

import org.objectweb.celtix.ws.addressing.AddressingBuilder;
import org.objectweb.celtix.ws.addressing.AddressingProperties;
import org.objectweb.celtix.ws.addressing.AttributedURIType;
import org.objectweb.celtix.ws.addressing.ObjectFactory;
import org.objectweb.celtix.ws.addressing.RelatesToType;

import org.objectweb.hello_world_soap_http.Greeter;
import org.objectweb.hello_world_soap_http.PingMeFault;
import org.objectweb.hello_world_soap_http.SOAPService;

import static org.objectweb.celtix.ws.addressing.JAXWSAConstants.CLIENT_ADDRESSING_PROPERTIES;


public final class Client {
    
    private static final QName SERVICE_NAME = 
        new QName("http://objectweb.org/hello_world_soap_http", "SOAPService");
    private static final ObjectFactory WSA_OBJECT_FACTORY = 
        new ObjectFactory();
    private static final String USER_NAME = System.getProperty("user.name");


    private Client() {
    } 

    public static void main(String args[]) throws Exception {
        if (args.length == 0) { 
            System.out.println("please specify wsdl");
            System.exit(1); 
        }
        
        try { 
            File wsdl = new File(args[0]);
            SOAPService service = new SOAPService(wsdl.toURL(), SERVICE_NAME);
            Greeter port = service.getSoapPort();

            implicitPropagation(port);

            explicitPropagation(port);

            implicitPropagation(port);

        } catch (UndeclaredThrowableException ex) { 
            ex.getUndeclaredThrowable().printStackTrace();
        } catch (Exception ex) { 
            ex.printStackTrace();
        }  finally { 
            System.exit(0); 
        }
    }

    /**
     * A series of invocations with implicitly propogated
     * Message Addressing Properties.
     */
    private static void implicitPropagation(Greeter port) {
        System.out.println();
        System.out.println("Implicit MessageAddressingProperties propagation");
        System.out.println("------------------------------------------------");

        System.out.println("Invoking sayHi...");
        String resp = port.sayHi();
        System.out.println("Server responded with: " + resp + "\n");

        System.out.println("Invoking greetMe...");
        resp = port.greetMe(USER_NAME);
        System.out.println("Server responded with: " + resp + "\n");

        System.out.println("Invoking greetMeOneWay...");
        port.greetMeOneWay(USER_NAME);
        System.out.println("No response from server as method is OneWay\n");

        try {
            System.out.println("Invoking pingMe, expecting exception...");
            port.pingMe();
        } catch (PingMeFault ex) {
            System.out.println("Expected exception occurred: " + ex);
        }
    }

    /**
     * A series of invocations with explicitly propogated
     * Message Addressing Properties.
     */
    private static void explicitPropagation(Greeter port) {
        System.out.println();
        System.out.println("Explicit MessageAddressingProperties propagation");
        System.out.println("------------------------------------------------");

        // get Message Addressing Properties instance
        AddressingBuilder builder = AddressingBuilder.getAddressingBuilder();
        AddressingProperties maps = builder.newAddressingProperties();

        // set MessageID property
        AttributedURIType messageID = 
            WSA_OBJECT_FACTORY.createAttributedURIType();
        messageID.setValue("urn:uuid:12345");
        maps.setMessageID(messageID);

        // associate MAPs with request context
        Map<String, Object> requestContext =
            ((BindingProvider)port).getRequestContext();
        requestContext.put(CLIENT_ADDRESSING_PROPERTIES, maps);

        System.out.println("Invoking sayHi...");
        String resp = port.sayHi();
        System.out.println("Server responded with: " + resp + "\n");

        // clear the message ID to ensure a duplicate is not sent on the 
        // next invocation
        maps.setMessageID(null);

        // set the RelatesTo property to the initial message ID, so that
        // the series of invocations are explicitly related
        RelatesToType relatesTo = WSA_OBJECT_FACTORY.createRelatesToType();
        relatesTo.setValue(messageID.getValue());
        maps.setRelatesTo(relatesTo);

        System.out.println("Invoking greetMe...");
        resp = port.greetMe(USER_NAME);
        System.out.println("Server responded with: " + resp + "\n");

        System.out.println("Invoking greetMeOneWay...");
        port.greetMeOneWay(USER_NAME);
        System.out.println("No response from server as method is OneWay\n");

        // disassociate MAPs from request context
        requestContext.put(CLIENT_ADDRESSING_PROPERTIES, null);
    }
}
