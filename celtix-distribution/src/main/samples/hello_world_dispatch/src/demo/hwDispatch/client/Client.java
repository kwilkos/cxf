package demo.hwDispatch.client;

import java.io.File;
import java.io.InputStream;
import java.net.URL;

import javax.xml.namespace.QName;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPMessage;
import javax.xml.transform.dom.DOMSource;
import javax.xml.ws.Dispatch;
import javax.xml.ws.Service.Mode;

import org.objectweb.hello_world_soap_http.SOAPService;

public final class Client {
    
    private static final QName SERVICE_NAME = new QName("http://objectweb.org/hello_world_soap_http", 
                                                        "SOAPService");
    
    private static final QName PORT_NAME = new QName("http://objectweb.org/hello_world_soap_http", 
                                                     "SoapPort"); 
    
    private static final String WSDL_RELV_PATH = "..\\..\\..\\..\\..\\wsdl\\";

    private Client() {
    } 

    public static void main(String args[]) throws Exception {
        
        if (args.length == 0) { 
            System.out.println("please specify wsdl");
            System.exit(1); 
        }
    
        URL wsdlURL;
        File wsdlFile = new File(args[0]);
        if (wsdlFile.exists()) {
            wsdlURL = wsdlFile.toURL();
        } else {
            wsdlURL = new URL(args[0]);
        }
        
        System.out.println(wsdlURL + "\n\n");
        
        SOAPService service = new SOAPService(wsdlURL, SERVICE_NAME); 
        
        InputStream is1 =  Client.class.getResourceAsStream(WSDL_RELV_PATH + "GreetMeDocLiteralReq1.xml");
        InputStream is2 =  Client.class.getResourceAsStream(WSDL_RELV_PATH + "GreetMeDocLiteralReq2.xml");
        InputStream is3 =  Client.class.getResourceAsStream(WSDL_RELV_PATH + "GreetMeDocLiteralReq3.xml");
        
        SOAPMessage soapReq1 = MessageFactory.newInstance().createMessage(null, is1);
        SOAPMessage soapReq2 = MessageFactory.newInstance().createMessage(null, is2);
        SOAPMessage soapReq3 = MessageFactory.newInstance().createMessage(null, is3);
        DOMSource domReqMessage = new DOMSource(soapReq2.getSOAPPart());
        DOMSource domReqPayload = new DOMSource(soapReq3.getSOAPBody().extractContentAsDocument());
             
        
        Dispatch<SOAPMessage> dispSOAPMsg = service.createDispatch(PORT_NAME, 
                                                            SOAPMessage.class, Mode.MESSAGE);      
        System.out.println("Invoking server through Dispatch interface using SOAPMessage");
        SOAPMessage soapResp = dispSOAPMsg.invoke(soapReq1);        
        System.out.println("Response from server: " + soapResp.getSOAPBody().getTextContent() + "\n\n"); 
        
        
        Dispatch<DOMSource> dispDOMSrcMessage = service.createDispatch(PORT_NAME, 
                                                                 DOMSource.class, Mode.MESSAGE);
        System.out.println("Invoking server through Dispatch interface using DOMSource in MESSAGE Mode");
        DOMSource domRespMessage = dispDOMSrcMessage.invoke(domReqMessage);        
        System.out.println("Response from server: " 
                           + domRespMessage.getNode().getFirstChild().getTextContent() + "\n\n"); 
        
        Dispatch<DOMSource> dispDOMSrcPayload = service.createDispatch(PORT_NAME, 
                                                                DOMSource.class, Mode.PAYLOAD);
        System.out.println("Invoking server through Dispatch interface using DOMSource in PAYLOAD Mode");
        DOMSource domRespPayload = dispDOMSrcPayload.invoke(domReqPayload);        
        System.out.println("Response from server: " 
                           + domRespPayload.getNode().getFirstChild().getTextContent()); 
        
      
            
        System.exit(0); 
    }

}
