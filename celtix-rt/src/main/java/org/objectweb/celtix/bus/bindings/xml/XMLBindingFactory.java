package org.objectweb.celtix.bus.bindings.xml;

import java.io.IOException;
import javax.wsdl.BindingInput;
import javax.wsdl.BindingOutput;
import javax.wsdl.WSDLException;
import javax.wsdl.extensions.ExtensionRegistry;
import javax.xml.namespace.QName;
import javax.xml.ws.Endpoint;

import org.objectweb.celtix.Bus;
import org.objectweb.celtix.bindings.BindingFactory;
import org.objectweb.celtix.bindings.ClientBinding;
import org.objectweb.celtix.bindings.ServerBinding;
import org.objectweb.celtix.bindings.ServerBindingEndpointCallback;
import org.objectweb.celtix.ws.addressing.EndpointReferenceType;


public class XMLBindingFactory implements BindingFactory {
    
    static final String NS_XML_FORMAT = "http://celtix.objectweb.org/bindings/xmlformat";
    private Bus bus;
    
    public XMLBindingFactory() {
        //Complete
    }
    
    public void init(Bus b) {
        bus = b;
        registerXMLBindingExtension(bus.getWSDLManager().getExtenstionRegistry());
    }
    
    public ClientBinding createClientBinding(EndpointReferenceType reference) 
        throws WSDLException, IOException {        
        return new XMLClientBinding(bus, reference);
    }

    public ServerBinding createServerBinding(EndpointReferenceType reference,
                                             Endpoint ep,
                                             ServerBindingEndpointCallback cbFactory)
        throws WSDLException, IOException {
        return new XMLServerBinding(bus, reference, ep, cbFactory);
    }

    private void registerXMLBindingExtension(ExtensionRegistry registry) {
        registerXMLBinding(registry, BindingInput.class);
        registerXMLBinding(registry, BindingOutput.class);
    }

    private void registerXMLBinding(ExtensionRegistry registry, Class clz) {
        registry.registerSerializer(clz,
                                    new QName(NS_XML_FORMAT, "body"),
                                    new XMLBindingSerializer());
        
        registry.registerDeserializer(clz,
                                      new QName(NS_XML_FORMAT, "body"),
                                      new XMLBindingSerializer());
        registry.mapExtensionTypes(clz,
                                   new QName(NS_XML_FORMAT, "body"),
                                   XMLBinding.class);
    }
}
