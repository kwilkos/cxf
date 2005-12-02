package org.objectweb.celtix.systest.stress.concurrency;


import java.util.concurrent.atomic.AtomicInteger;

import javax.jws.WebService;

import org.objectweb.hello_world_soap_http.BadRecordLitFault;
import org.objectweb.hello_world_soap_http.Greeter;
import org.objectweb.hello_world_soap_http.NoSuchCodeLitFault;
import org.objectweb.hello_world_soap_http.types.BareDocumentResponse;
import org.objectweb.hello_world_soap_http.types.ErrorCode;
import org.objectweb.hello_world_soap_http.types.NoSuchCodeLit;

@WebService(serviceName = "SOAPService",
            portName = "SoapPort",
            name = "Greeter",
            targetNamespace = "http://objectweb.org/hello_world_soap_http")
public class GreeterImpl implements Greeter {

    protected AtomicInteger greetMeCount = new AtomicInteger(0);
    protected AtomicInteger greetMeOneWayCount = new AtomicInteger(0);
    protected AtomicInteger sayHiCount = new AtomicInteger(0);
    protected AtomicInteger docLitFaultCount = new AtomicInteger(0);
    protected AtomicInteger docLitBareCount = new AtomicInteger(0);


    public String greetMe(String me) {
        greetMeCount.incrementAndGet();
        return "Hello " + me;
    }

    public void greetMeOneWay(String me) {
        greetMeOneWayCount.incrementAndGet();
    }

    public String sayHi() {
        sayHiCount.incrementAndGet();
        return "Hiya";
    }

    public void testDocLitFault(String faultType) throws BadRecordLitFault, NoSuchCodeLitFault {
        docLitFaultCount.incrementAndGet();
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

    public BareDocumentResponse testDocLitBare(String in) {
        docLitBareCount.incrementAndGet();
        BareDocumentResponse res = new BareDocumentResponse();
        res.setCompany("Celtix");
        res.setId(1);
        return res;
    }
}
