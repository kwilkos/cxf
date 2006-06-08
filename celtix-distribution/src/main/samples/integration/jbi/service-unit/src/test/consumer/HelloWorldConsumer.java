package test.consumer;

import javax.jbi.component.ComponentContext;
import javax.jbi.servicedesc.ServiceEndpoint;
import javax.xml.namespace.QName;

import org.objectweb.celtix.jbi.ServiceConsumer;
import org.objectweb.hello_world.Greeter;
import org.objectweb.hello_world.HelloWorldService;


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
                    
                HelloWorldService service = new HelloWorldService();
                System.out.println("got service");
                Greeter g = service.getSoapPort();
                System.out.println("invoking method");
                    
                String ret = g.greetMe("ffang");

                System.out.println("greetMe service says: " + ret);

                g.sayHi();
                Thread.sleep(10000);
            } while (running);
        } catch (Exception ex) { 
            ex.printStackTrace();
        } 
    } 


    protected final void waitForEndpointActivation() { 

        final QName serviceName = 
            new QName("http://objectweb.org/hello_world", "HelloWorldService");

        boolean ready = false;
        do { 
            ServiceEndpoint[] eps = ctx.getEndpointsForService(serviceName); 
            if (eps.length == 0) { 
                System.out.println("waiting for endpoints to become active");
                try { 
                    Thread.sleep(5000); 
                } catch (Exception ex) {
                    //ignore
                }
            } else {
                System.out.println("endpoints ready, pump starting");
                ready = true;
            } 
        } while(!ready && running);
    } 

}
