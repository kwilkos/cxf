package org.objectweb.celtix.systest.type_test.soap;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import javax.jws.WebService;
//import javax.xml.namespace.QName;
import javax.xml.ws.Endpoint;

import org.objectweb.celtix.Bus;
import org.objectweb.celtix.systest.type_test.TypeTestImpl;

public class SOAPServerImpl {

    protected Bus bus;

    public void start(String args[]) throws Exception {
        bus = Bus.init(args);

        Object implementor = new SOAPTypeTestImpl();
        String address = "http://localhost:9200/SOAPService/SOAPPort/";

        // XXX - Discard some Endpoint.publish() verbosity.
        PrintStream pout = System.out;
        System.setOut(new PrintStream(new ByteArrayOutputStream()));

        Endpoint.publish(address, implementor);

        System.setOut(pout);

        //QName name = new QName("http://objectweb.org/type_test", "SOAPService");
        //File file = new File(file,"/type_test/type_test_soap.wsdl");
        //String location = file.toURL().toString();
        //bus.registerTypeFactory(new TypeTestTypeFactory(location));
    }

    public void stop() throws Exception {
        bus.shutdown(true);
        bus = null;
    }

    public void run() throws Exception {
        bus.run();
    }

    public static void main(String args[]) throws Exception {
        SOAPServerImpl server = new SOAPServerImpl();
        server.start(args);
        server.run();
    }
    
    @WebService(serviceName = "SOAPService", portName = "SOAPPort",
                name = "TypeTestPortType",
                targetNamespace = "http://objectweb.org/type_test")
    class SOAPTypeTestImpl extends TypeTestImpl {
    }
}
