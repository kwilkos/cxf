package org.objectweb.celtix.wsdl;

import java.lang.annotation.Annotation;
import java.net.URL;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.jws.WebService;
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

import org.objectweb.celtix.addressing.AttributedURIType;
import org.objectweb.celtix.addressing.EndpointReferenceType;
import org.objectweb.celtix.addressing.MetadataType;
import org.objectweb.celtix.addressing.ObjectFactory;
import org.objectweb.celtix.addressing.wsdl.ServiceNameType;

public final class EndpointReferenceUtils {

    private static final Logger LOG = Logger.getLogger(EndpointReferenceUtils.class.getName());

    private static final QName WSDL_LOCATION = new QName("http://www.w3.org/2004/08/wsdl-instance",
                                                         "wsdlLocation");
    private static final QName SEI = new QName("http://www.w3.org/2004/08/wsdl-instance", "sei");
    private static final QName SERVICE_NAME = new QName("http://www.w3.org/2004/08/wsdl", "service");
    private static final QName PORT_NAME = new QName("http://www.w3.org/2004/08/wsdl", "port");

    private EndpointReferenceUtils() {
        // Utility class - never constructed
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

        Map<QName, String> attribMap = metadata.getOtherAttributes();
        String className = attribMap.get(SEI);
        if (null != className) {
            Class sei = null;
            try {
                sei = Class.forName(className, true, manager.getClass().getClassLoader());
            } catch (ClassNotFoundException ex) {
                LOG.log(Level.SEVERE, "Could not load Webservice SEI", ex);
                return null;
            }
            return manager.getDefinition(sei);
        }

        return null;
    }

    public static Port getPort(WSDLManager manager, EndpointReferenceType ref) throws WSDLException {

        Definition def = getWSDLDefinition(manager, ref);
        assert def != null : "unable to find definition for reference " + ref;

        MetadataType metadata = ref.getMetadata();
        for (Object objMeta : metadata.getAny()) {
            Object obj = objMeta;
            if (obj instanceof Element) {
                Element el = (Element)obj;
                if ("http://www.w3.org/2005/02/addressing/wsdl".equals(el.getNamespaceURI())) {
                    try {
                        JAXBContext context = JAXBContext.newInstance(new Class[] {ObjectFactory.class});
                        Unmarshaller u = context.createUnmarshaller();
                        obj = u.unmarshal(el);
                    } catch (JAXBException jaxbex) {
                        throw new WSDLException(WSDLException.PARSER_ERROR, "Problem parsing WSDL", jaxbex);
                    }
                }
            }
            if (obj instanceof JAXBElement) {
                obj = ((JAXBElement)obj).getValue();
            }

            if (obj instanceof ServiceNameType) {
                ServiceNameType snt = (ServiceNameType)obj;
                LOG.log(Level.FINEST, "found service name ", snt.getEndpointName());
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
            if (service == null) {
                throw new WSDLException(WSDLException.OTHER_ERROR, "Cannot find service for " + serviceName);
            }
            String str = attribMap.get(PORT_NAME);
            // service.getPort(str);
            LOG.log(Level.FINE, "getting port " + str + " from service " + service.getQName());
            Port port = service.getPort(str);
            if (port == null) {
                throw new WSDLException(WSDLException.OTHER_ERROR, "unable to find port " + str);
            }
            return port;
        }
        // TODO : throw exception here
        return null;
    }

    public static String getAddress(EndpointReferenceType ref) {
        AttributedURIType a = ref.getAddress();
        if (null != a) {
            return a.getValue();
        }
        // should wsdl be parsed for an address now?
        return null;
    }

    public static void setAddress(EndpointReferenceType ref, String address) {
        AttributedURIType a = new ObjectFactory().createAttributedURIType();
        a.setValue(address);
        ref.setAddress(a);
    }

    public static EndpointReferenceType getEndpointReference(URL wsdlUrl, 
                                                             QName serviceName, 
                                                             String portName) {

        EndpointReferenceType reference = new EndpointReferenceType();
        reference.setMetadata(new MetadataType());
        Map<QName, String> attribMap = reference.getMetadata().getOtherAttributes();

        attribMap.put(WSDL_LOCATION, wsdlUrl.toString());
        attribMap.put(SERVICE_NAME, serviceName.toString());
        attribMap.put(PORT_NAME, portName);

        return reference;
    }

    public static EndpointReferenceType getEndpointReference(WSDLManager manager, Object implementor) {

        WebService ws = (WebService)implementor.getClass().getAnnotation(WebService.class);
        if (null == ws) {
            return null;
        }

        EndpointReferenceType reference = new EndpointReferenceType();
        reference.setMetadata(new MetadataType());
        Map<QName, String> attribMap = reference.getMetadata().getOtherAttributes();

        String serviceName = ws.serviceName();
        if (null == serviceName || "".equals(serviceName)) {
            serviceName = implementor.getClass().getSimpleName() + "Service";
        }

        attribMap.put(SERVICE_NAME, serviceName.toString());

        String url = ws.wsdlLocation();
        if (null != url && url.length() > 0) {
            attribMap.put(WSDL_LOCATION, url);
        } else {
            String className = ws.endpointInterface();

            if (null == className || "".equals(className)) {
                Class[] interfaces = implementor.getClass().getInterfaces();
                for (Class c : interfaces) {
                    Annotation[] as = c.getAnnotations();
                    for (Annotation a : as) {
                        if (a instanceof WebService) {
                            className = c.getName();
                            url = ((WebService)a).wsdlLocation();
                            break;
                        }
                    }
                }
                if (null == className || "".equals(className)) {
                    className = implementor.getClass().getName();
                }
            }
            if (null != url && url.length() > 0) {
                attribMap.put(WSDL_LOCATION, url);
            } else {
                attribMap.put(SEI, className);
            }
        }

        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("created endpoint reference with");
            LOG.fine("    service name: " + attribMap.get(SERVICE_NAME));
            LOG.fine("    wsdl location: " + attribMap.get(WSDL_LOCATION));
            LOG.fine("    sei class: " + attribMap.get(SEI));
        }

        return reference;
    }
}
