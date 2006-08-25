package demo.callback.server;

import java.net.URL;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;

import org.objectweb.callback.CallbackPortType;
import org.objectweb.callback.ServerPortType;
import org.objectweb.celtix.Bus;
import org.objectweb.celtix.bus.wsdl.WSDLManagerImpl;
import org.objectweb.celtix.jaxb.JAXBUtils;
import org.objectweb.celtix.ws.addressing.EndpointReferenceType;
import org.objectweb.celtix.wsdl.EndpointReferenceUtils;
import org.objectweb.celtix.wsdl.WSDLManager;



@javax.jws.WebService(name = "ServerPortType ", serviceName = "SOAPService", 
                      targetNamespace = "http://objectweb.org/callback", 
                      wsdlLocation = "file:./wsdl/basic_callback.wsdl")
                  
public class ServerImpl implements ServerPortType  {
    
    public String registerCallback(EndpointReferenceType callback) {
        
        try {
            Bus bus = Bus.init();
            WSDLManager manager = new WSDLManagerImpl(bus);
        
            QName interfaceName = EndpointReferenceUtils.getInterfaceName(callback);
            String wsdlLocation = EndpointReferenceUtils.getWSDLLocation(callback);
            QName serviceName = EndpointReferenceUtils.getServiceName(callback);
            String portString = EndpointReferenceUtils.getPortName(callback);
            
            QName portName = new QName(serviceName.getNamespaceURI(), portString);
            
            StringBuffer seiName = new StringBuffer();
            seiName.append(JAXBUtils.namespaceURIToPackage(interfaceName.getNamespaceURI()));
            seiName.append(".");
            seiName.append(JAXBUtils.nameToIdentifier(interfaceName.getLocalPart(),
                                                      JAXBUtils.IdentifierType.INTERFACE));
            
            Class<?> sei = null;    
            try {
                sei = Class.forName(seiName.toString(), 
                                    true, manager.getClass().getClassLoader());
            } catch (ClassNotFoundException ex) {
                ex.printStackTrace();
            }
            
            URL wsdlURL = new URL(wsdlLocation);            
            
            Service service = Service.create(wsdlURL, serviceName);
            CallbackPortType port =  (CallbackPortType)service.getPort(portName, sei);

            System.out.println("Invoking on callback object");
            String resp = port.serverSayHi(System.getProperty("user.name"));
            System.out.println("Response from callback object: " + resp);
  
            bus.shutdown(true);
            
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
        
        return "registerCallback called";     
    }
    
    
}
