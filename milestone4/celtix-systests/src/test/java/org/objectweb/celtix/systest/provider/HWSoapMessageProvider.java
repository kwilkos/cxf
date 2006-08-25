package org.objectweb.celtix.systest.provider;
import java.io.InputStream;

import javax.xml.namespace.QName;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.Provider;
import javax.xml.ws.Service;
import javax.xml.ws.ServiceMode;
import javax.xml.ws.WebServiceProvider;

import org.w3c.dom.Node;

//The following wsdl file is used.
//wsdlLocation = "C:/CeltixSVN/trunk/celtix-testutils/src/main/resources/wsdl/hello_world_rpc_lit.wsdl"
@WebServiceProvider(portName = "SoapPortRPCLit", serviceName = "SOAPServiceRPCLit",
                      targetNamespace = "http://objectweb.org/hello_world_rpclit",
 wsdlLocation = "/wsdl/hello_world_rpc_lit.wsdl")
@ServiceMode(value = Service.Mode.MESSAGE)            
public class HWSoapMessageProvider implements Provider<SOAPMessage> {

    private static QName sayHi = new QName("http://objectweb.org/hello_world_rpclit", "sayHi");
    private static QName greetMe = new QName("http://objectweb.org/hello_world_rpclit", "greetMe");
    private SOAPMessage sayHiResponse;
    private SOAPMessage greetMeResponse;
    
    public HWSoapMessageProvider() {
       
        try {
            MessageFactory factory = MessageFactory.newInstance();            
            InputStream is = getClass().getResourceAsStream("resources/sayHiRpcLiteralResp.xml");
            sayHiResponse =  factory.createMessage(null, is);
            is.close();
            is = getClass().getResourceAsStream("resources/GreetMeRpcLiteralResp.xml");
            greetMeResponse =  factory.createMessage(null, is);
            is.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
    public SOAPMessage invoke(SOAPMessage request) {
        SOAPMessage response = null;        
        try {
            SOAPBody body = request.getSOAPBody();
            Node n = body.getFirstChild();

            while (n.getNodeType() != Node.ELEMENT_NODE) {
                n = n.getNextSibling();
            }
            if (n.getLocalName().equals(sayHi.getLocalPart())) {
                response = sayHiResponse;
            } else if (n.getLocalName().equals(greetMe.getLocalPart())) {
                response = greetMeResponse;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return response;
    }
}
