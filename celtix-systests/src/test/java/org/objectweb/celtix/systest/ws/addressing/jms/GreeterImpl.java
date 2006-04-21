package org.objectweb.celtix.systest.ws.addressing.jms;

import javax.annotation.Resource;
import javax.jws.HandlerChain;
import javax.jws.WebService;
import javax.xml.ws.WebServiceContext;

import org.objectweb.celtix.hello_world_jms.HelloWorldPortType;
import org.objectweb.celtix.systest.ws.addressing.VerificationCache;
import org.objectweb.celtix.ws.addressing.AddressingProperties;


import static org.objectweb.celtix.ws.addressing.JAXWSAConstants.SERVER_ADDRESSING_PROPERTIES_INBOUND;


@WebService(serviceName = "JMSSOAPServiceAddressing", 
            portName = "HWJMSAddressingPort", 
            endpointInterface =  "org.objectweb.celtix.hello_world_jms.HelloWorldPortType",
            targetNamespace = "http://celtix.objectweb.org/hello_world_jms")
@HandlerChain(file = "./handlers.xml", name = "JMSMAPHandlerChain")
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

}
