package test.provider;

import javax.jws.HandlerChain;
import javax.jws.WebService;
import org.objectweb.handlers.AddNumbers;
import org.objectweb.handlers.AddNumbersFault;
import org.objectweb.handlers.types.FaultDetail;


@WebService(name = "AddNumbers", targetNamespace = "http://www.objectweb.org/handlers", 
            serviceName = "AddNumbersService",
            wsdlLocation = "/META-INF/addNumbers.wsdl")
@HandlerChain(file = "../common/demo_handlers.xml", name = "DemoHandlerChain")
public class HelloWorldProvider implements AddNumbers {


    /**
     * @param number1
     * @param number2
     * @return The sum
     * @throws AddNumbersException
     *             if any of the numbers to be added is negative.
     */
    public int addNumbers(int number1, int number2) throws AddNumbersFault {
        System.out.println("addNumbers called....." + number1 + ":" + number2);
        if (number1 < 0 || number2 < 0) {
            String message = "Negative number cant be added!";
            String detail = "Numbers: " + number1 + ", " + number2;
            FaultDetail fault = new FaultDetail();
            fault.setMessage(message);
            fault.setFaultInfo(detail);
            throw new AddNumbersFault(message, fault);
        }
        return number1 + number2;
    }

}


