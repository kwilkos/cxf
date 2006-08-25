package test.consumer;

import javax.jbi.component.ComponentContext;
import javax.jbi.servicedesc.ServiceEndpoint;
import javax.xml.namespace.QName;

import org.objectweb.celtix.jbi.ServiceConsumer;
import org.objectweb.handlers.AddNumbers;
import org.objectweb.handlers.AddNumbersFault;
import org.objectweb.handlers.AddNumbersService;


public class HelloWorldConsumer implements ServiceConsumer { 

    static QName serviceName = new QName("http://www.objectweb.org/handlers",
                                           "AddNumbersService");

    static QName portName = new QName("http://www.objectweb.org/handlers",
                                        "AddNumbersPort");

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
                    
                AddNumbersService service = new AddNumbersService();
                AddNumbers port = (AddNumbers)service.getAddNumbersPort();
        
                try {
                    int number1 = 10;
                    int number2 = 20;

                    System.out.printf("Invoking addNumbers(%d, %d)\n", number1, number2);
                    int result = port.addNumbers(number1, number2);
                    System.out.printf("The result of adding %d and %d is %d.\n\n", number1, number2, result);

                    number1 = 3; 
                    number2 = 5; 

                    System.out.printf("Invoking addNumbers(%d, %d)\n", number1, number2);
                    result = port.addNumbers(number1, number2);
                    System.out.printf("The result of adding %d and %d is %d.\n\n", number1, number2, result);
            
                    number1 = -10;
                    System.out.printf("Invoking addNumbers(%d, %d)\n", number1, number2);
                    result = port.addNumbers(number1, number2);
                    System.out.printf("The result of adding %d and %d is %d.\n", number1, number2, result);

                } catch (AddNumbersFault ex) {
                    System.out.printf("Caught AddNumbersFault: %s\n", ex.getFaultInfo().getMessage());
                }


                Thread.sleep(10000);
            } while (running);
        } catch (Exception ex) { 
            ex.printStackTrace();
        } 
    } 


    protected final void waitForEndpointActivation() { 

        final QName service = 
            new QName("http://www.objectweb.org/handlers", "AddNumbersService");
        boolean ready = false;
        do { 
            ServiceEndpoint[] eps = ctx.getEndpointsForService(service); 
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
