package org.objectweb.celtix.systest.routing;

import javax.jws.WebService;
import org.objectweb.hello_world_doc_lit.Greeter;
import org.objectweb.hello_world_doc_lit.PingMeFault;
import org.objectweb.hello_world_doc_lit.types.FaultDetail;

@WebService(endpointInterface = "org.objectweb.hello_world_doc_lit.Greeter")
public class DocLitWrappedImpl implements Greeter {

    public String greetMe(String me) {
        return "Hello " + me;
    }

    public String sayHi() {
        return "Bonjour";
    }

    public void pingMe() throws PingMeFault {
        FaultDetail fd = new FaultDetail();
        fd.setMajor((short)2);
        fd.setMinor((short)1);
        throw new PingMeFault("Test Exception", fd);
    }

    public void greetMeOneWay(String requestType) {
        System.out.println("greetMeOneWay: " + requestType);
    }
}
