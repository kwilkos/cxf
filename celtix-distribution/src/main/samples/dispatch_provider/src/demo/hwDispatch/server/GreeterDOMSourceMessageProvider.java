package demo.hwDispatch.server;
import java.io.InputStream;

import javax.xml.namespace.QName;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPMessage;
import javax.xml.transform.dom.DOMSource;
import javax.xml.ws.Provider;
import javax.xml.ws.Service;
import javax.xml.ws.ServiceMode;
import javax.xml.ws.WebServiceProvider;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

@WebServiceProvider(portName = "SoapPort2", serviceName = "SOAPService2",
                    targetNamespace = "http://objectweb.org/hello_world_soap_http",
                    wsdlLocation = "file:./wsdl/hello_world.wsdl")
@ServiceMode(value = Service.Mode.MESSAGE)            
public class GreeterDOMSourceMessageProvider implements Provider<DOMSource> {

    public GreeterDOMSourceMessageProvider() {
        //Complete
    }

    public DOMSource invoke(DOMSource request) {
        DOMSource response = new DOMSource();
        try {
            MessageFactory factory = MessageFactory.newInstance();
            SOAPMessage soapReq = factory.createMessage();
            soapReq.getSOAPPart().setContent(request);
    
            System.out.println("Incoming Client Request as a DOMSource data in MESSAGE Mode");
            soapReq.writeTo(System.out);
            System.out.println("\n");
    
            InputStream is = getClass().getResourceAsStream("GreetMeDocLiteralResp2.xml");
            SOAPMessage greetMeResponse =  factory.createMessage(null, is);
            is.close();

            response.setNode(greetMeResponse.getSOAPPart());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return response;
    }
}
