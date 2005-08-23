package org.objectweb.celtix.wsdl;

import java.net.URL;
import java.util.Map;
import javax.wsdl.Definition;
import javax.wsdl.Port;
import javax.wsdl.Service;
import javax.wsdl.WSDLException;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;

import org.w3c.dom.Element;

import org.objectweb.celtix.addressing.EndpointReferenceType;
import org.objectweb.celtix.addressing.MetadataType;
import org.objectweb.celtix.addressing.wsdl.ObjectFactory;
import org.objectweb.celtix.addressing.wsdl.ServiceNameType;

public final class EndpointReferenceUtils {

    private static final QName WSDL_LOCATION = 
            new QName("http://www.w3.org/2004/08/wsdl-instance", "wsdlLocation");
    private static final QName SERVICE_NAME = new QName("http://www.w3.org/2004/08/wsdl", "service");
    private static final QName PORT_NAME = new QName("http://www.w3.org/2004/08/wsdl", "port");

    private EndpointReferenceUtils() {
        //Utility class - never constructed
    }


    public static Definition getWSDLDefinition(WSDLManager manager, EndpointReferenceType ref)
        throws WSDLException {
        MetadataType metadata = ref.getMetadata();
        String location = (String)metadata.getOtherAttributes().get(WSDL_LOCATION);
        
        if (null != location) {
            return manager.getDefinition(location);
        }
        for (Object obj : metadata.getAny()) {
            if (obj instanceof Element) {
                Element el = (Element)obj;
                if ("http://schemas.xmlsoap.org/wsdl/".equals(el.getNamespaceURI())
                    && "definitions".equals(el.getLocalName())) {
                    return manager.getDefinition(el);
                }
            }
        }
        return null;
    }

    public static Port getPort(WSDLManager manager, EndpointReferenceType ref)
        throws WSDLException {

        Definition def = getWSDLDefinition(manager, ref);
        MetadataType metadata = ref.getMetadata();
        for (Object obj : metadata.getAny()) {
            if (obj instanceof Element) {
                Element el = (Element)obj;
                if ("http://www.w3.org/2005/02/addressing/wsdl".equals(el.getNamespaceURI())) {
                    try {
                        JAXBContext context = JAXBContext.newInstance(new Class[] {ObjectFactory.class});
                        Unmarshaller u = context.createUnmarshaller();
                        obj = u.unmarshal(el);
                    } catch (JAXBException jaxbex) {
                        throw new WSDLException(WSDLException.PARSER_ERROR,
                                                "Problem parsing WSDL",
                                                jaxbex);
                    }
                }
            }
            if (obj instanceof JAXBElement) {
                obj = ((JAXBElement)obj).getValue();
            }

            if (obj instanceof ServiceNameType) {
                ServiceNameType snt = (ServiceNameType)obj;

                Service service = def.getService(snt.getValue());
                return service.getPort(snt.getEndpointName());
            }
        }
        if (def.getServices().size() == 1) {
            Service service = (Service)def.getServices().values().iterator().next();
            if (service.getPorts().size() == 1) {
                return (Port)service.getPorts().values().iterator().next();
            }
        }

        Map<QName, String> attribMap = metadata.getOtherAttributes();
        String value = attribMap.get(SERVICE_NAME);
        if (null != value) {
            QName serviceName = QName.valueOf(value);
            Service service = def.getService(serviceName);
            String str = attribMap.get(PORT_NAME);
            //return service.getPort(str);
            return service.getPort(str);
        }
 
        return null;
    } 

    public static EndpointReferenceType getEndpointReference(URL wsdlUrl, 
            QName serviceName, String portName) {

        EndpointReferenceType reference = new EndpointReferenceType();
        reference.setMetadata(new MetadataType());
        Map<QName, String> attribMap = reference.getMetadata().getOtherAttributes();
        
        attribMap.put(WSDL_LOCATION, wsdlUrl.toString());
        attribMap.put(SERVICE_NAME, serviceName.toString());
        attribMap.put(PORT_NAME, portName);
        
        return reference;
    }
}
