package org.objectweb.celtix.systest.jms;

import javax.jws.WebService;

import org.objectweb.celtix.hello_world_jms.BadRecordLitFault;
import org.objectweb.celtix.hello_world_jms.HelloWorldPortType;
import org.objectweb.celtix.hello_world_jms.NoSuchCodeLitFault;
import org.objectweb.celtix.hello_world_jms.types.ErrorCode;
import org.objectweb.celtix.hello_world_jms.types.NoSuchCodeLit;
import org.objectweb.celtix.hello_world_jms.types.TestRpcLitFaultResponse;



@WebService(serviceName = "HelloWorldService", 
            portName = "HelloWorldPort",
            endpointInterface = "org.objectweb.celtix.hello_world_jms.HelloWorldPortType",
            targetNamespace = "http://celtix.objectweb.org/hello_world_jms")
public class GreeterImplTwoWayJMS implements HelloWorldPortType {

    public String greetMe(String me) {
        System.out.println("Reached here :" + me);
        return "Hello " + me;
    }

    public String sayHi() {
        return "Bonjour";
    }
    
    public void greetMeOneWay(String requestType) {
        System.out.println("*********  greetMeOneWay: " + requestType);
    }
    
    public TestRpcLitFaultResponse testRpcLitFault(String faultType) 
        throws BadRecordLitFault, NoSuchCodeLitFault {
        if (faultType.equals(BadRecordLitFault.class.getSimpleName())) {
            throw new BadRecordLitFault("TestBadRecordLit", "BadRecordLitFault");
        }
        if (faultType.equals(NoSuchCodeLitFault.class.getSimpleName())) {
            ErrorCode ec = new ErrorCode();
            ec.setMajor((short)1);
            ec.setMinor((short)1);
            NoSuchCodeLit nscl = new NoSuchCodeLit();
            nscl.setCode(ec);
            throw new NoSuchCodeLitFault("TestNoSuchCodeLit", nscl);
        }
        
        return new TestRpcLitFaultResponse();
    }
    
    
}
