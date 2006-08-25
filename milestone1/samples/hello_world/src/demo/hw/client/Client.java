package demo.hw.client;

import javax.xml.ws.ServiceFactory;
import javax.xml.ws.Service;
import javax.xml.namespace.QName;
import java.net.MalformedURLException;
import java.io.File;
import javax.xml.ws.WebServiceException;
import org.objectweb.celtix.Bus;
import org.objectweb.celtix.BusException;
import org.objectweb.hello_world_soap_http.Greeter;
import java.rmi.RemoteException;

public class Client {

      static QName serviceName = new QName("http://objectweb.org/hello_world_soap_http", 
                                           "SOAPService");

      static QName portName = new QName("http://objectweb.org/hello_world_soap_http", 
                                        "SoapPort");
      static Bus bus; 

	public static void main(String[] args) {
		
            
		try { 
			if (args.length == 0) {
				System.err.println("please provide wsdl");
				System.exit(0);
			}
			File wsdl = new File(args[0]);
			
                        bus = Bus.init();
			ServiceFactory factory = ServiceFactory.newInstance();
			System.out.println("service factory: " + factory);

			
			Service service = factory.createService(wsdl.toURL(), serviceName);
                        Greeter greeter = (Greeter) service.getPort(portName, Greeter.class);
                        String greeting = greeter.greetMe("blah");
                        System.out.println("response from service: " +  greeting);
                        
		} catch(Exception ex) {
                    System.out.println("error during remote invocation: " );
                    ex.printStackTrace();
		} finally { 
                    if (bus != null) { 
                        try {
                            bus.shutdown(true);
                        } catch(BusException busex) {
                        }
                    }
                }
        }
}

