package test.common;

import java.util.Map;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.ws.LogicalMessage;
import javax.xml.ws.ProtocolException;
import javax.xml.ws.handler.LogicalHandler;
import javax.xml.ws.handler.LogicalMessageContext;
import javax.xml.ws.handler.MessageContext;
import org.objectweb.handlers.types.AddNumbers;
import org.objectweb.handlers.types.AddNumbersResponse;



/**
 * handles addition of small numbers.
 */
public class SmallNumberHandler implements LogicalHandler<LogicalMessageContext> {


    // Implementation of javax.xml.ws.handler.Handler

    public final boolean handleMessage(LogicalMessageContext messageContext) {
        System.out.println("LogicalMessageHandler handleMessage called");

        try { 
            boolean outbound = (Boolean)messageContext.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY); 
            
            if (outbound) { 
                // get the LogicalMessage from our context
                //
                LogicalMessage msg = messageContext.getMessage();

                // check the payload, if its an AddNumbers request, we'll intervene
                //
                JAXBContext jaxbContext = JAXBContext.newInstance(AddNumbers.class); 
                Object payload = msg.getPayload(jaxbContext); 
                
                if (payload instanceof AddNumbers) { 
                    AddNumbers req = (AddNumbers)payload;

                    // now, if the arguments are small, let's do the calculation here
                    //
                    int a = req.getArg0(); 
                    int b = req.getArg1();
                    
                    if (isSmall(a) && isSmall(b)) { 
                        int answer = a + b; 
                    
                        System.out.printf("SmallNumberHandler addNumbers(%d, %d) == %d\n", a, b, answer);
                        // ok, we've done the calculation, so build the
                        // response and set it as the payload of the message 
                        AddNumbersResponse resp = new AddNumbersResponse(); 
                        resp.setReturn(answer); 
                        msg.setPayload(resp, jaxbContext); 

                        // finally, return false, indicating that request
                        // processing stops here and our answer will be 
                        // returned to the client
                        return false;
                    } 
                } 
            }
            return true;
        } catch (JAXBException ex) { 
            throw new ProtocolException(ex); 
        } 

    }

    public final boolean handleFault(LogicalMessageContext messageContext) {
        System.out.println("LogicalMessageHandler handleFault called");
        System.out.println(messageContext);
        
        return true;
    }

    public void close(MessageContext ctx) { 
        System.out.println("LogicalHandler close called");
    } 

    public void init(Map config) { 
        System.out.println("LogicalHandler init called"); 
    } 

    public void destroy() { 
        System.out.println("LogicalHandler close called"); 
    } 

    private boolean isSmall(int i) {
        return i > 0 && i <= 10;
    }
} 
