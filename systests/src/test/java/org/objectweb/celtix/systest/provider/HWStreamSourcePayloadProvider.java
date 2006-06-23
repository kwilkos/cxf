package org.objectweb.celtix.systest.provider;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import javax.xml.namespace.QName;
import javax.xml.soap.MessageFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.ws.Provider;
import javax.xml.ws.Service;
import javax.xml.ws.ServiceMode;
import javax.xml.ws.WebServiceProvider;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSOutput;
import org.w3c.dom.ls.LSSerializer;



//The following wsdl file is used.
//wsdlLocation = "C:/CeltixSVN/trunk/celtix-testutils/src/main/resources/wsdl/hello_world_rpc_lit.wsdl"
@WebServiceProvider(portName = "SoapPortRPCLit6", serviceName = "SOAPServiceRPCLit6",
                targetNamespace = "http://objectweb.org/hello_world_rpclit",
wsdlLocation = "/wsdl/hello_world_rpc_lit.wsdl")
@ServiceMode(value = Service.Mode.PAYLOAD)
public class HWStreamSourcePayloadProvider implements Provider<StreamSource> {
    
    private static QName sayHi = new QName("http://objectweb.org/hello_world_rpclit", "sayHi");
    private static QName greetMe = new QName("http://objectweb.org/hello_world_rpclit", "greetMe");
    private InputStream sayHiInputStream;
    private InputStream greetMeInputStream;
    private MessageFactory factory;

    public HWStreamSourcePayloadProvider() {

        try {
            factory = MessageFactory.newInstance();
            InputStream is = getClass().getResourceAsStream("resources/sayHiRpcLiteralResp.xml");
            Document sayHiDocument = factory.createMessage(null, is).getSOAPBody().extractContentAsDocument();
            sayHiInputStream = getSOAPBodyStream(sayHiDocument);
            
            InputStream is2 = getClass().getResourceAsStream("resources/GreetMeRpcLiteralResp.xml");
            Document greetMeDocument = 
                factory.createMessage(null, is2).getSOAPBody().extractContentAsDocument();
            greetMeInputStream = getSOAPBodyStream(greetMeDocument);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public StreamSource invoke(StreamSource request) {
        StreamSource response = new StreamSource();
        try {
            DOMResult domResult = new DOMResult();
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.transform(request, domResult);
            Node n = domResult.getNode().getFirstChild();

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
    
    private InputStream getSOAPBodyStream(Document doc) throws Exception {
        System.setProperty(DOMImplementationRegistry.PROPERTY,
            "com.sun.org.apache.xerces.internal.dom.DOMImplementationSourceImpl");
        DOMImplementationRegistry registry = DOMImplementationRegistry.newInstance();
        DOMImplementationLS impl = (DOMImplementationLS)registry.getDOMImplementation("LS");
        LSOutput output = impl.createLSOutput();
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        output.setByteStream(byteArrayOutputStream);
        LSSerializer writer = impl.createLSSerializer();
        writer.write(doc, output);
        byte[] buf = byteArrayOutputStream.toByteArray();
        return new ByteArrayInputStream(buf);
    }

    
}
