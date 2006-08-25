package org.objectweb.celtix.systest.callback;

import java.io.File;
import java.net.URL;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;

import org.objectweb.callback.CallbackPortType;
//import org.objectweb.callback.RegisterCallback;
import org.objectweb.callback.ServerPortType;
import org.objectweb.celtix.Bus;
import org.objectweb.celtix.bus.wsdl.WSDLManagerImpl;
import org.objectweb.celtix.jaxb.JAXBUtils;
import org.objectweb.celtix.ws.addressing.EndpointReferenceType;
import org.objectweb.celtix.wsdl.EndpointReferenceUtils;
import org.objectweb.celtix.wsdl.WSDLManager;

@javax.jws.WebService(serviceName = "SOAPService", 
                      targetNamespace = "http://objectweb.org/callback",
                      endpointInterface = "org.objectweb.callback.ServerPortType") 
                      //wsdlLocation = "file:./wsdl/basic_callback.wsdl")
                  
public class ServerImpl implements ServerPortType  {

    //private static final Logger LOG = 
    //    Logger.getLogger(ServerImpl.class.getPackage().getName());
    
    public String foo(String s) {
        return s;
    }
    
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
            
            URL wsdlURL = (new File(wsdlLocation)).toURL();             
            
            Service service = Service.create(wsdlURL, serviceName);
            CallbackPortType port =  (CallbackPortType)service.getPort(portName, sei);

            port.serverSayHi("Sean");
  
            bus.shutdown(true); 
            
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
        
        return "registerCallback called";     
    }
    
    
}
