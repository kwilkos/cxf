package demo.hwDispatch.server;
import java.io.InputStream;

import javax.xml.namespace.QName;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.Provider;
import javax.xml.ws.Service;
import javax.xml.ws.ServiceMode;
import javax.xml.ws.WebServiceProvider;

@WebServiceProvider(portName = "SoapPort1", serviceName = "SOAPService1",
                      targetNamespace = "http://objectweb.org/hello_world_soap_http",
                      wsdlLocation = "file:./wsdl/hello_world.wsdl")
@ServiceMode(value = Service.Mode.MESSAGE)            
public class GreeterSoapMessageProvider implements Provider<SOAPMessage> {

    public GreeterSoapMessageProvider() {
        //Complete
    }
    
    public SOAPMessage invoke(SOAPMessage request) {
        SOAPMessage response = null;        
        try {
            MessageFactory factory = MessageFactory.newInstance();            
            InputStream is = getClass().getResourceAsStream("GreetMeDocLiteralResp1.xml");
            response =  factory.createMessage(null, is);
            is.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return response;
    }
}
