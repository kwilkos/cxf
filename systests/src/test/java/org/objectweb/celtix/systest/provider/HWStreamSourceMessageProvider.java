package org.objectweb.celtix.systest.provider;

import java.io.InputStream;

import javax.xml.namespace.QName;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPMessage;
import javax.xml.transform.stream.StreamSource;
import javax.xml.ws.Provider;
import javax.xml.ws.Service;
import javax.xml.ws.ServiceMode;
import javax.xml.ws.WebServiceProvider;

import org.w3c.dom.Node;



//The following wsdl file is used.
//wsdlLocation = "C:/CeltixSVN/trunk/celtix-testutils/src/main/resources/wsdl/hello_world_rpc_lit.wsdl"
@WebServiceProvider(portName = "SoapPortRPCLit4", serviceName = "SOAPServiceRPCLit4",
                  targetNamespace = "http://objectweb.org/hello_world_rpclit",
wsdlLocation = "/wsdl/hello_world_rpc_lit.wsdl")
@ServiceMode(value = Service.Mode.MESSAGE)
public class HWStreamSourceMessageProvider implements Provider<StreamSource> {
    
    private static QName sayHi = new QName("http://objectweb.org/hello_world_rpclit", "sayHi");
    private static QName greetMe = new QName("http://objectweb.org/hello_world_rpclit", "greetMe");
    private InputStream sayHiInputStream;
    private InputStream greetMeInputStream;
    private MessageFactory factory;

    public HWStreamSourceMessageProvider() {

        try {
            factory = MessageFactory.newInstance();
            sayHiInputStream = getClass().getResourceAsStream("resources/sayHiRpcLiteralResp.xml");
            greetMeInputStream = getClass().getResourceAsStream("resources/GreetMeRpcLiteralResp.xml");

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public StreamSource invoke(StreamSource request) {
        StreamSource response = new StreamSource();
        try {
            SOAPMessage msg = factory.createMessage();
            msg.getSOAPPart().setContent(request);
            SOAPBody body = msg.getSOAPBody();
            Node n = body.getFirstChild();

            while (n.getNodeType() != Node.ELEMENT_NODE) {
                n = n.getNextSibling();
            }
            if (n.getLocalName().equals(sayHi.getLocalPart())) {
                response.setInputStream(sayHiInputStream);
            } else if (n.getLocalName().equals(greetMe.getLocalPart())) {
                response.setInputStream(greetMeInputStream);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return response;
    }

}
