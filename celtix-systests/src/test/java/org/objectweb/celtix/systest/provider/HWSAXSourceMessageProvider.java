package org.objectweb.celtix.systest.provider;

import java.io.InputStream;

import javax.xml.namespace.QName;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPMessage;
import javax.xml.transform.sax.SAXSource;
import javax.xml.ws.Provider;
import javax.xml.ws.Service;
import javax.xml.ws.ServiceMode;
import javax.xml.ws.WebServiceProvider;

import org.w3c.dom.Node;

import org.xml.sax.InputSource;

//The following wsdl file is used.
//wsdlLocation = "C:/CeltixSVN/trunk/celtix-testutils/src/main/resources/wsdl/hello_world_rpc_lit.wsdl"
@WebServiceProvider(portName = "SoapPortRPCLit3", serviceName = "SOAPServiceRPCLit3",
                    targetNamespace = "http://objectweb.org/hello_world_rpclit",
wsdlLocation = "/wsdl/hello_world_rpc_lit.wsdl")
@ServiceMode(value = Service.Mode.MESSAGE)
public class HWSAXSourceMessageProvider implements Provider<SAXSource> {
    
    private static QName sayHi = new QName("http://objectweb.org/hello_world_rpclit", "sayHi");
    private static QName greetMe = new QName("http://objectweb.org/hello_world_rpclit", "greetMe");
    private InputSource sayHiInputSource;
    private InputSource greetMeInputSource;
    private MessageFactory factory;

    public HWSAXSourceMessageProvider() {

        try {
            factory = MessageFactory.newInstance();
            InputStream is1 = getClass().getResourceAsStream("resources/sayHiRpcLiteralResp.xml");
            sayHiInputSource = new InputSource(is1);

            InputStream is2 = getClass().getResourceAsStream("resources/GreetMeRpcLiteralResp.xml");
            greetMeInputSource = new InputSource(is2);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public SAXSource invoke(SAXSource request) {
        SAXSource response = new SAXSource();
        try {
            SOAPMessage msg = factory.createMessage();
            msg.getSOAPPart().setContent(request);
            SOAPBody body = msg.getSOAPBody();
            Node n = body.getFirstChild();

            while (n.getNodeType() != Node.ELEMENT_NODE) {
                n = n.getNextSibling();
            }
            if (n.getLocalName().equals(sayHi.getLocalPart())) {
                response.setInputSource(sayHiInputSource);
            } else if (n.getLocalName().equals(greetMe.getLocalPart())) {
                response.setInputSource(greetMeInputSource);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return response;
    }

}
