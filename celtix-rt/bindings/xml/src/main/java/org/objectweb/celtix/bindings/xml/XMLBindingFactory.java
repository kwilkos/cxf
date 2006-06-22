package org.objectweb.celtix.bindings.xml;

import java.io.IOException;
import javax.wsdl.BindingInput;
import javax.wsdl.BindingOutput;
import javax.wsdl.WSDLException;
import javax.xml.bind.JAXBException;

import org.objectweb.celtix.Bus;
import org.objectweb.celtix.bindings.BindingFactory;
import org.objectweb.celtix.bindings.ClientBinding;
import org.objectweb.celtix.bindings.ServerBinding;
import org.objectweb.celtix.bindings.ServerBindingEndpointCallback;
import org.objectweb.celtix.bindings.xmlformat.TBody;
import org.objectweb.celtix.ws.addressing.EndpointReferenceType;
import org.objectweb.celtix.wsdl.JAXBExtensionHelper;

public class XMLBindingFactory implements BindingFactory {
    
    private Bus bus;
    
    public XMLBindingFactory() {
        //Complete
    }
    
    public void init(Bus b) {
        bus = b;
        try {
            JAXBExtensionHelper.addExtensions(bus.getWSDLManager().getExtenstionRegistry(),
                                              BindingInput.class,
                                              TBody.class);
            JAXBExtensionHelper.addExtensions(bus.getWSDLManager().getExtenstionRegistry(),
                                              BindingOutput.class,
                                              TBody.class);
        } catch (JAXBException e) {
            //ignore, we can continue without the extension registered
        }
    }
    
    public ClientBinding createClientBinding(EndpointReferenceType reference) 
        throws WSDLException, IOException {        
        return new XMLClientBinding(bus, reference);
    }

    public ServerBinding createServerBinding(EndpointReferenceType reference,
                                             ServerBindingEndpointCallback cbFactory)
        throws WSDLException, IOException {
        return new XMLServerBinding(bus, reference, cbFactory);
    }
}
