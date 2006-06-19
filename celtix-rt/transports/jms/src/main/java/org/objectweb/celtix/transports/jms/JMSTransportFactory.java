package org.objectweb.celtix.transports.jms;

import java.io.IOException;

import javax.wsdl.Port;
import javax.wsdl.WSDLException;
import javax.xml.bind.JAXBException;

import org.objectweb.celtix.Bus;
import org.objectweb.celtix.bindings.ClientBinding;
import org.objectweb.celtix.transports.ClientTransport;
import org.objectweb.celtix.transports.ServerTransport;
import org.objectweb.celtix.transports.TransportFactory;
//import org.objectweb.celtix.transports.jms.JMSAddressPolicyType;
//import org.objectweb.celtix.transports.jms.JMSClientBehaviorPolicyType;
//import org.objectweb.celtix.transports.jms.JMSServerBehaviorPolicyType;
import org.objectweb.celtix.ws.addressing.EndpointReferenceType;
import org.objectweb.celtix.wsdl.JAXBExtensionHelper;

public class JMSTransportFactory implements TransportFactory {

    protected Bus theBus;

    public void init(Bus bus) {
        theBus = bus;
        
        registerExtenstion();
    }
        
    private void registerExtenstion() {
        
        try {
            JAXBExtensionHelper.addExtensions(theBus.getWSDLManager().getExtenstionRegistry(),
                                              Port.class,
                                              JMSAddressPolicyType.class);
            JAXBExtensionHelper.addExtensions(theBus.getWSDLManager().getExtenstionRegistry(),
                                              Port.class,
                                              JMSServerBehaviorPolicyType.class);
            JAXBExtensionHelper.addExtensions(theBus.getWSDLManager().getExtenstionRegistry(),
                                              Port.class,
                                              JMSClientBehaviorPolicyType.class);
        } catch (JAXBException e) {
            e.printStackTrace();
        }
        
    }

    public ServerTransport createServerTransport(EndpointReferenceType address) 
        throws WSDLException, IOException {
        return new JMSServerTransport(theBus, address);
    }
     
    public ServerTransport createTransientServerTransport(EndpointReferenceType address)
        throws WSDLException {
        return null;
    }
     
    public ClientTransport createClientTransport(EndpointReferenceType address,
                                                 ClientBinding binding) 
        throws WSDLException, IOException {
        return new JMSClientTransport(theBus, address, binding);
    }
}
