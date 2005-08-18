package org.objectweb.celtix.wsdl;

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

    private EndpointReferenceUtils() {
        //Utility class - never constructed
    }


    public static Definition getWSDLDefinition(WSDLManager manager, EndpointReferenceType ref)
        throws WSDLException {
        MetadataType metadata = ref.getMetadata();
        String location = (String)metadata.getOtherAttributes().get(
                        new QName("http://www.w3.org/2004/08/wsdl-instance", "wsdlLocation"));
        
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
        throws WSDLException, JAXBException {

        Definition def = getWSDLDefinition(manager, ref);
        MetadataType metadata = ref.getMetadata();
        for (Object obj : metadata.getAny()) {
            if (obj instanceof Element) {
                Element el = (Element)obj;
                if ("http://www.w3.org/2005/02/addressing/wsdl".equals(el.getNamespaceURI())) {
                    JAXBContext context = JAXBContext.newInstance(new Class[] {ObjectFactory.class});
                    Unmarshaller u = context.createUnmarshaller();
                    obj = u.unmarshal(el);
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

        return null;
    } 

}
