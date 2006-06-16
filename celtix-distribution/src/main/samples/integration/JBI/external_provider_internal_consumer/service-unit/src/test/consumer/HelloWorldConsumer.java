package test.consumer;

import javax.jbi.component.ComponentContext;
import javax.jbi.servicedesc.ServiceEndpoint;
import javax.xml.namespace.QName;

import org.objectweb.celtix.jbi.ServiceConsumer;

import org.objectweb.hello_world_soap_jms.Greeter;
import org.objectweb.hello_world_soap_jms.PingMeFault;
import org.objectweb.hello_world_soap_jms.JMSGreeterService;


public class HelloWorldConsumer implements ServiceConsumer { 

    private volatile boolean running; 
    private ComponentContext ctx; 

    public void setComponentContext(ComponentContext cc) { 
        ctx = cc;
    } 

    public void stop() { 
        running = false;
    } 

    public void run() { 
        try { 
            Thread.currentThread().setContextClassLoader(getClass().getClassLoader());

            running = true;
            waitForEndpointActivation(); 
            do { 
                System.out.println("getting service");
                    
                JMSGreeterService service = new JMSGreeterService();
                Greeter port = service.getGreeterPort();

                String resp; 

                System.out.println("Invoking sayHi...");
                resp = port.sayHi();
                System.out.println("Server responded with: " + resp);
                System.out.println();

                System.out.println("Invoking greetMe...");
                resp = port.greetMe(System.getProperty("user.name"));
                System.out.println("Server responded with: " + resp);
                System.out.println();

                try {
                    System.out.println("Invoking pingMe, expecting exception...");
                    port.pingMe();
                } catch (PingMeFault ex) {
                    System.out.println("Expected exception: PingMeFault has occurred: " + ex.getMessage());
                }
 

                Thread.sleep(10000);
            } while (running);
        } catch (Exception ex) { 
            ex.printStackTrace();
        } 
    } 


    protected final void waitForEndpointActivation() { 

        final QName serviceName = 
            new QName("http://objectweb.org/hello_world_soap_jms", "JMSGreeterService");
        boolean ready = false;
        do { 
            ServiceEndpoint[] eps = ctx.getEndpointsForService(serviceName); 
            if (eps.length == 0) { 
                System.out.println("waiting for endpoints to become active");
                try { 
                    Thread.sleep(5000); 
                } catch (Exception ex) { 
                    //ignore it
                }
            } else {
                System.out.println("endpoints ready, pump starting");
                ready = true;
            } 
        } while(!ready && running);
    } 

}

