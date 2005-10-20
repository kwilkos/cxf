package org.objectweb.celtix.systest.basic;


import javax.jws.WebService;

import org.objectweb.hello_world_soap_http.BadRecordLitFault;
import org.objectweb.hello_world_soap_http.Greeter;
import org.objectweb.hello_world_soap_http.NoSuchCodeLitFault;
import org.objectweb.hello_world_soap_http.types.ErrorCode;
import org.objectweb.hello_world_soap_http.types.NoSuchCodeLit;

@WebService(serviceName = "SOAPService", portName = "SoapPort", name = "Greeter", 
            targetNamespace = "http://objectweb.org/hello_world_soap_http")
public class GreeterImpl implements Greeter {

    public String greetMe(String me) {
        return "Hello " + me;
    }

    public String sayHi() {
        return "Bonjour";
    }
    
    public void testDocLitFault(String faultType) throws BadRecordLitFault, NoSuchCodeLitFault {
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
    }
    
    public void greetMeOneWay(String requestType) {
        
    }
}
