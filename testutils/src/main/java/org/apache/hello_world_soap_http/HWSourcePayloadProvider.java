package org.apache.hello_world_soap_http;


//import java.util.logging.Logger;
import javax.xml.transform.dom.DOMSource;
import javax.xml.ws.Provider;
import javax.xml.ws.WebServiceProvider;

@WebServiceProvider(portName = "SoapPort", serviceName = "SOAPService",
                      targetNamespace = "http://apache.org/hello_world_soap_http",
                      wsdlLocation = "resources/wsdl/hello_world.wsdl")
public class HWSourcePayloadProvider implements Provider<DOMSource> {

    //private static final Logger LOG =
    //    Logger.getLogger(AnnotatedGreeterImpl.class.getName());

    private int invokeCount;

    public HWSourcePayloadProvider() {
        //Complete
    }

    public DOMSource invoke(DOMSource source) {
        invokeCount++;
        return source;
    }

    public int getInvokeCount() {
        return invokeCount;
    }
}
