package org.objectweb.celtix.systest.ws.addressing.jms;

import javax.annotation.Resource;
import javax.jws.WebService;
import javax.xml.ws.WebServiceContext;

import org.objectweb.celtix.hello_world_jms.BadRecordLitFault;
import org.objectweb.celtix.hello_world_jms.HelloWorldPortType;
import org.objectweb.celtix.hello_world_jms.NoSuchCodeLitFault;
import org.objectweb.celtix.hello_world_jms.types.ErrorCode;
import org.objectweb.celtix.hello_world_jms.types.NoSuchCodeLit;
import org.objectweb.celtix.hello_world_jms.types.TestRpcLitFaultResponse;
import org.objectweb.celtix.systest.ws.addressing.VerificationCache;
import org.objectweb.celtix.ws.addressing.AddressingProperties;


import static org.objectweb.celtix.ws.addressing.JAXWSAConstants.SERVER_ADDRESSING_PROPERTIES_INBOUND;


@WebService(serviceName = "JMSSOAPServiceAddressing", 
            portName = "HWJMSAddressingPort", 
            endpointInterface =  "org.objectweb.celtix.hello_world_jms.HelloWorldPortType",
            targetNamespace = "http://celtix.objectweb.org/hello_world_jms")
public class GreeterImpl implements HelloWorldPortType {
    VerificationCache verificationCache;

    /**
     * Injectable context.
     */
    @Resource
    private WebServiceContext context;


    public String greetMe(String me) {
        verifyMAPs();
        return "Hello " + me;
    }

    public void greetMeOneWay(String requestType) {   
        verifyMAPs();
    }

    public String sayHi() {
        verifyMAPs();
        return "Bonjour";
    }

    private void verifyMAPs() {
        String property = SERVER_ADDRESSING_PROPERTIES_INBOUND;
        AddressingProperties maps = (AddressingProperties)
            context.getMessageContext().get(property);
        verificationCache.put(MAPTest.verifyMAPs(maps, this));
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
