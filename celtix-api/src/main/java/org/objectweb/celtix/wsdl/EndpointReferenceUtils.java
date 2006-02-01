package org.objectweb.celtix.wsdl;

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
import javax.xml.ws.WebServiceProvider;

import org.w3c.dom.Element;

import org.objectweb.celtix.common.logging.LogUtils;
import org.objectweb.celtix.ws.addressing.AttributedURIType;
import org.objectweb.celtix.ws.addressing.EndpointReferenceType;
import org.objectweb.celtix.ws.addressing.MetadataType;
import org.objectweb.celtix.ws.addressing.ObjectFactory;
import org.objectweb.celtix.ws.addressing.wsdl.ServiceNameType;

/**
 * Provides utility methods for obtaining endpoint references, wsdl definitions, etc.
 */
public final class EndpointReferenceUtils {

    private static final Logger LOG = LogUtils.getL7dLogger(EndpointReferenceUtils.class);

    private static final QName WSDL_LOCATION = new QName("http://www.w3.org/2004/08/wsdl-instance",
                                                         "wsdlLocation");
    private static final QName SEI = new QName("http://www.w3.org/2004/08/wsdl-instance", "sei");
    private static final QName SERVICE_NAME = new QName("http://www.w3.org/2004/08/wsdl", "service");
    private static final QName PORT_NAME = new QName("http://www.w3.org/2004/08/wsdl", "port");

    private EndpointReferenceUtils() {
        // Utility class - never constructed
    }
    
    /**
     * Gets the service name of the provided endpoint reference. 
     * @param ref the endpoint reference.
     * @return the service name.
     */
    public static QName getServiceName(EndpointReferenceType ref) {
        Map<QName, String> attribMap = ref.getMetadata().getOtherAttributes();
        return QName.valueOf(attribMap.get(SERVICE_NAME));
    }
    
    /**
     * Gets the port name of the provided endpoint reference.
     * @param ref the endpoint reference.
     * @return the port name.
     */
    public static String getPortName(EndpointReferenceType ref) {
        Map<QName, String> attribMap = ref.getMetadata().getOtherAttributes();
        return attribMap.get(PORT_NAME);
    }

    /**
     * Gets the wsdl location of the provided endpoint reference.
     * @param ref the endpoint reference.
     * @return the wsdl location.
     */
    public static String getWSDLLocation(EndpointReferenceType ref) {
        Map<QName, String> attribMap = ref.getMetadata().getOtherAttributes();
        return attribMap.get(WSDL_LOCATION);
    }
    
    /**
     * Gets the WSDL definition for the provided endpoint reference.
     * @param manager - the WSDL manager 
     * @param ref - the endpoint reference
     * @return Definition the wsdl definition
     * @throws WSDLException
     */
    public static Definition getWSDLDefinition(WSDLManager manager, EndpointReferenceType ref)
        throws WSDLException {
        
        MetadataType metadata = ref.getMetadata();
        String location = metadata.getOtherAttributes().get(WSDL_LOCATION);

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
            Class<?> sei = null;
            try {
                sei = Class.forName(className, true, manager.getClass().getClassLoader());
            } catch (ClassNotFoundException ex) {
                LOG.log(Level.SEVERE, "SEI_LOAD_FAILURE_MSG", ex);
                return null;
            }
            return manager.getDefinition(sei);
        }

        return null;
    }

    /**
     * Gets the WSDL port for the provided endpoint reference.
     * @param manager - the WSDL manager 
     * @param ref - the endpoint reference
     * @return Port the wsdl port
     * @throws WSDLException
     */
    public static Port getPort(WSDLManager manager, EndpointReferenceType ref) throws WSDLException {

        Definition def = getWSDLDefinition(manager, ref);
        assert def != null : "unable to find definition for reference " + ref;

        MetadataType metadata = ref.getMetadata();
        for (Object objMeta : metadata.getAny()) {
            Object obj = objMeta;
            if (obj instanceof Element) {
                Element el = (Element)obj;
                if ("http://www.w3.org/2005/08/addressing/wsdl".equals(el.getNamespaceURI())) {
                    try {
                        JAXBContext context = JAXBContext.newInstance(ObjectFactory.class);
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
            if (service.getPorts().size() == 1) { 
                return (Port)service.getPorts().values().iterator().next();
            }
            String str = attribMap.get(PORT_NAME);
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

    /**
     * Get the address from the provided endpoint reference.
     * @param ref - the endpoint reference
     * @return String the address of the endpoint
     */
    public static String getAddress(EndpointReferenceType ref) {
        AttributedURIType a = ref.getAddress();
        if (null != a) {
            return a.getValue();
        }
        // should wsdl be parsed for an address now?
        return null;
    }

    /**
     * Set the address of the provided endpoint reference.
     * @param ref - the endpoint reference
     * @param address - the address
     */
    public static void setAddress(EndpointReferenceType ref, String address) {
        AttributedURIType a = new ObjectFactory().createAttributedURIType();
        a.setValue(address);
        ref.setAddress(a);
    }

    /**
     * Create an endpoint reference for the provided wsdl, service and portname.
     * @param wsdlUrl - url of the wsdl that describes the service.
     * @param serviceName - the <code>QName</code> of the service.
     * @param portName - the name of the port.
     * @return EndpointReferenceType - the endpoint reference
     */
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
    
    /**
     * Create an endpoint reference for the provided .
     * @param address - address URI
     * @return EndpointReferenceType - the endpoint reference
     */
    public static EndpointReferenceType getEndpointReference(String address) {

        EndpointReferenceType reference = new EndpointReferenceType();
        setAddress(reference, address);

        return reference;
    }

    /**
     * Get the WebService for the provided class.  If the class
     * itself does not have a WebService annotation, this method
     * is called recursively on the class's interfaces and superclass. 
     * @param cls - the Class .
     * @return WebService - the web service
     */
    public static WebService getWebServiceAnnotation(Class<?> cls) {
        if (cls == null) {
            return null;
        }
        WebService ws = cls.getAnnotation(WebService.class); 
        if (null != ws) {
            return ws;
        }
        for (Class<?> inf : cls.getInterfaces()) {
            ws = getWebServiceAnnotation(inf);
            if (null != ws) {
                return ws;
            }
        }
        
        return getWebServiceAnnotation(cls.getSuperclass());
    }
    
    
    /**
     * Gets an endpoint reference for the provided implementor object.
     * @param manager - the wsdl manager.
     * @param implementor - the service implementor.
     * @return EndpointReferenceType - the endpoint reference
     */
    public static EndpointReferenceType getEndpointReference(WSDLManager manager, Object implementor) {

        WebService ws = getWebServiceAnnotation(implementor.getClass());
        WebServiceProvider wsp = null;
        if (null == ws) {
            wsp = implementor.getClass().getAnnotation(WebServiceProvider.class);
            if (null == wsp) {
                return null;
            }
        }
        
        EndpointReferenceType reference = new EndpointReferenceType();
        reference.setMetadata(new MetadataType());
        Map<QName, String> attribMap = reference.getMetadata().getOtherAttributes();

        String serviceName = (null != ws) ? ws.serviceName() : wsp.serviceName();
        if (null == serviceName || "".equals(serviceName)) {
            serviceName = implementor.getClass().getSimpleName() + "Service";
        }
        String targetNamespace = (null != ws) ? ws.targetNamespace() : wsp.targetNamespace();
        if (null != targetNamespace && !"".equals(targetNamespace)) {
            serviceName = "{" + targetNamespace + "}" + serviceName;
        }

        attribMap.put(SERVICE_NAME, serviceName.toString());

        String portName = (null != ws) ? ws.portName() : wsp.portName();
        if (null != portName && !"".equals(portName)) {
            attribMap.put(PORT_NAME, portName);
        }

        String url = (null != ws) ? ws.wsdlLocation() : wsp.wsdlLocation();
        String className = (null != ws) ? ws.endpointInterface() : null;
        
        if (null == className || "".equals(className)) {
            Class<?> cls = implementor.getClass();
            while (cls != null && (null == className || "".equals(className))) {
                Class<?>[] interfaces = cls.getInterfaces();
                for (Class<?> c : interfaces) {
                    WebService a = c.getAnnotation(WebService.class);
                    if (null != a) {
                        className = a.endpointInterface();
                        if (null == url || url.length() == 0) {
                            url = a.wsdlLocation();
                        }
                        break;
                    }
                }
                cls = cls.getSuperclass();
            }

            if (null == className || "".equals(className)) {
                className = implementor.getClass().getName();
            }
        }

        if (null != url && url.length() > 0) {
            //REVISIT Resolve the url for all cases
            if (wsp != null) {                
                URL wsdlUrl = implementor.getClass().getResource(url);
                if (wsdlUrl != null) {
                    url = wsdlUrl.toExternalForm();
                }
            }
            attribMap.put(WSDL_LOCATION, url);
        }

        attribMap.put(SEI, className);

        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("created endpoint reference with");
            LOG.fine("    service name: " + attribMap.get(SERVICE_NAME));
            LOG.fine("    wsdl location: " + attribMap.get(WSDL_LOCATION));
            LOG.fine("    sei class: " + attribMap.get(SEI));
        }
        return reference;
    }
   
}
