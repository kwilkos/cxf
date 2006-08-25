package demo.jms_greeter.client;

import java.io.File;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import org.objectweb.celtix.jms_greeter.JMSGreeterPortType;
import org.objectweb.celtix.jms_greeter.JMSGreeterService;
import org.objectweb.celtix.transports.jms.context.JMSMessageHeadersType;
import org.objectweb.celtix.transports.jms.context.JMSPropertyType;


public final class Client {

    private static final QName SERVICE_NAME =
        new QName("http://celtix.objectweb.org/jms_greeter", "JMSGreeterService");
    private static final QName PORT_NAME =
        new QName("http://celtix.objectweb.org/jms_greeter", "JMSGreeterPortType");

    private Client() {
    }

    public static void main(String[] args) throws Exception {

        if (args.length == 0) {
            System.out.println("please specify wsdl");
            System.exit(1);
        }

        File wsdl = new File(args[0]);

        JMSGreeterService service = new JMSGreeterService(wsdl.toURL(), SERVICE_NAME);
        JMSGreeterPortType greeter = (JMSGreeterPortType)service.getPort(PORT_NAME, JMSGreeterPortType.class);

        System.out.println("Invoking sayHi...");
        System.out.println("server responded with: " + greeter.sayHi());
        System.out.println();

        System.out.println("Invoking greetMe...");
        System.out.println("server responded with: " + greeter.greetMe(System.getProperty("user.name")));
        System.out.println();

        System.out.println("Invoking greetMeOneWay...");
        greeter.greetMeOneWay(System.getProperty("user.name"));
        System.out.println("No response from server as method is OneWay");
        System.out.println();

        // Demonstration of JMS Context usage

        InvocationHandler handler = Proxy.getInvocationHandler(greeter);

        BindingProvider  bp = null;

        if (handler instanceof BindingProvider) {
            bp = (BindingProvider)handler;
            Map<String, Object> requestContext = bp.getRequestContext();
            JMSMessageHeadersType requestHeader = new JMSMessageHeadersType();
            requestHeader.setJMSCorrelationID("JMS_QUEUE_SAMPLE_CORRELATION_ID");
            requestHeader.setJMSExpiration(3600000L);
            JMSPropertyType propType = new JMSPropertyType();
            propType.setName("Test.Prop");
            propType.setValue("mustReturn");
            requestHeader.getProperty().add(propType);
            requestContext.put("org.objectweb.celtix.jms.client.request.headers", requestHeader);
            //To override the default receive timeout.
            requestContext.put("org.objectweb.celtix.jms.client.timeout", new Long(1000));
        }

        System.out.println("Invoking sayHi with JMS Context information ...");
        System.out.println("server responded with: " + greeter.sayHi());

        if (bp != null) {
            Map<String, Object> responseContext = bp.getResponseContext();
            JMSMessageHeadersType responseHdr = (JMSMessageHeadersType)responseContext.get(
                                       "org.objectweb.celtix.jms.client.response.headers");
            if (responseHdr == null) {
                System.out.println("response Header should not be null");
                System.out.println();
                System.exit(1);
            }

            if ("JMS_QUEUE_SAMPLE_CORRELATION_ID".equals(responseHdr.getJMSCorrelationID())
                && responseHdr.getProperty() != null) {
                System.out.println("Received expected contents in response context");
            } else {
                System.out.println("Received wrong contents in response context");
                System.out.println();
                System.exit(2);
            }
        } else {
            System.out.println("Failed to get the binding provider cannot access context info.");
            System.exit(3);
        }


        System.out.println();

        System.exit(0);
    }
}
