package demo.hwDispatch.server;
import java.io.InputStream;

import javax.xml.namespace.QName;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPMessage;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.ws.Provider;
import javax.xml.ws.WebServiceProvider;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

@WebServiceProvider(portName = "SoapPort3", serviceName = "SOAPService3",
                    targetNamespace = "http://objectweb.org/hello_world_soap_http",
                    wsdlLocation = "file:./wsdl/hello_world.wsdl")
public class GreeterDOMSourcePayloadProvider implements Provider<DOMSource> {

    public GreeterDOMSourcePayloadProvider() {
        //Complete
    }

    public DOMSource invoke(DOMSource request) {
        DOMSource response = new DOMSource();
        try {
            System.out.println("Incoming Client Request as a DOMSource data in PAYLOAD Mode");
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            StreamResult result = new StreamResult(System.out);
            transformer.transform(request, result);
            System.out.println("\n");
            
            InputStream is = getClass().getResourceAsStream("GreetMeDocLiteralResp3.xml");
            
            SOAPMessage greetMeResponse =  MessageFactory.newInstance().createMessage(null, is);
            is.close();            
            response.setNode(greetMeResponse.getSOAPBody().extractContentAsDocument());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return response;
    }
}
