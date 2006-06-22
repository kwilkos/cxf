package org.objectweb.celtix.bindings.xml;

import java.net.*;
import javax.xml.namespace.QName;
    
import org.objectweb.celtix.ws.addressing.EndpointReferenceType;
import org.objectweb.celtix.wsdl.EndpointReferenceUtils;

public final class TestUtils {

    public EndpointReferenceType getEndpointReference() {
        URL wsdlUrl = getClass().getResource("/wsdl/hello_world_xml_bare.wsdl");
        QName serviceName = new QName("http://objectweb.org/hello_world_xml_http/bare", "XMLService");
        return EndpointReferenceUtils.getEndpointReference(wsdlUrl, serviceName, "XMLPort");
    }

    public EndpointReferenceType getWrappedReference() {
        URL wsdlUrl = getClass().getResource("/wsdl/hello_world_xml_wrapped.wsdl");
        QName serviceName = new QName("http://objectweb.org/hello_world_xml_http/wrapped", "XMLService");
        return EndpointReferenceUtils.getEndpointReference(wsdlUrl, serviceName, "XMLPort");
    }
}
    
