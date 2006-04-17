package org.objectweb.celtix.wsdl;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.jws.WebService;
import javax.wsdl.Definition;
import javax.wsdl.Import;
import javax.wsdl.Port;
import javax.wsdl.Service;
import javax.wsdl.Types;
import javax.wsdl.WSDLException;
import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.WebServiceProvider;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import org.objectweb.celtix.common.logging.LogUtils;
import org.objectweb.celtix.ws.addressing.AttributedURIType;
import org.objectweb.celtix.ws.addressing.EndpointReferenceType;
import org.objectweb.celtix.ws.addressing.MetadataType;
import org.objectweb.celtix.ws.addressing.ObjectFactory;
import org.objectweb.celtix.ws.addressing.wsdl.AttributedQNameType;
import org.objectweb.celtix.ws.addressing.wsdl.ServiceNameType;

/**
 * Provides utility methods for obtaining endpoint references, wsdl definitions, etc.
 */
public final class EndpointReferenceUtils {

    static WeakHashMap<Definition, Schema> schemaMap = new WeakHashMap<Definition, Schema>();

    private static final Logger LOG = LogUtils.getL7dLogger(EndpointReferenceUtils.class);

    private static final QName WSDL_LOCATION = new QName("http://www.w3.org/2006/01/wsdl-instance",
                                                         "wsdlLocation");
    private static final String WS_ADDRESSING_WSDL_PACKAGE = ServiceNameType.class.getPackage().getName();

    private static JAXBContext jaxbContext;

    private static final Transformer XML_TRANSFORMER;
    static {
        Transformer transformer = null;
        try {
            TransformerFactory tf = TransformerFactory.newInstance();
            transformer = tf.newTransformer();            
        } catch (TransformerConfigurationException tce) {
            throw new WebServiceException("Could not create transformer", tce);
        }
        XML_TRANSFORMER = transformer;
    }
    
    private EndpointReferenceUtils() {
        // Utility class - never constructed
    }
    
    /**
     * Sets the service and port name of the provided endpoint reference. 
     * @param ref the endpoint reference.
     * @param serviceName the name of service.
     * @param portName the port name.
     */
    public static void setServiceAndPortName(EndpointReferenceType ref, 
                                             QName serviceName, 
                                             String portName) 
        throws WebServiceException {
        if (null != serviceName) {
            
            ServiceNameType serviceNameType = new ServiceNameType();
            serviceNameType.setValue(serviceName);
            serviceNameType.setEndpointName(portName);
            org.objectweb.celtix.ws.addressing.wsdl.ObjectFactory objectFactory = 
                new org.objectweb.celtix.ws.addressing.wsdl.ObjectFactory();
            JAXBElement<ServiceNameType> jaxbElement = objectFactory.createServiceName(serviceNameType);
            
            DOMResult result = new DOMResult();
            Marshaller marshaller;
            try {
                marshaller = getJAXBContext().createMarshaller();
                marshaller.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.TRUE);
                marshaller.marshal((Object)jaxbElement, result);
            } catch (JAXBException jbe) {
                throw new WebServiceException("setting service name failed", jbe);
            }
            MetadataType mt = ref.getMetadata();
            if (null == mt) {
                mt = new MetadataType();
                ref.setMetadata(mt);
            }
            Node node = result.getNode();
            if (node.getNodeType() != Node.ELEMENT_NODE) {
                node = result.getNode().getFirstChild();
            }
            
            mt.getAny().add(node);
        }
    }
    
    /**
     * Gets the service name of the provided endpoint reference. 
     * @param ref the endpoint reference.
     * @return the service name.
     */
    public static QName getServiceName(EndpointReferenceType ref) {
        QName serviceName = null;
        MetadataType metadata = ref.getMetadata();
        if (metadata != null) {
            for (Object obj : metadata.getAny()) {
                if (obj instanceof Element) {
                    Node node = (Element)obj;
                    if (node.getNamespaceURI().equals("http://www.w3.org/2005/08/addressing/wsdl") 
                        && node.getLocalName().equals("ServiceName")) {
                        String content = node.getTextContent();
                        String namespaceURI = node.getFirstChild().getNamespaceURI();
                        String service = content;
                        if (content.contains(":")) {
                            namespaceURI = getNameSpaceUri(node, content, namespaceURI);
                            service = getService(content);
                        }
                        
                        serviceName = new QName(namespaceURI, service);
                    }
                }
            }
        }
        return serviceName;
    }
    
    /**
     * Gets the port name of the provided endpoint reference.
     * @param ref the endpoint reference.
     * @return the port name.
     */
    public static String getPortName(EndpointReferenceType ref) {
        String portName = null;
        MetadataType metadata = ref.getMetadata();
        if (metadata != null) {
            for (Object obj : metadata.getAny()) {
                if (obj instanceof Element) {
                    Node node = (Element)obj;
                    if (node.getNamespaceURI().equals("http://www.w3.org/2005/08/addressing/wsdl")
                        && node.getNodeName().equals("ServiceName")) {
                        
                        portName = node.getAttributes().getNamedItem("EndpointName").getTextContent();
                    }
                }
            }
        }
        return portName;
    }
    
    private static void setInterfaceName(EndpointReferenceType ref, String targetNameSpace, 
                                         String interfaceName) {
        if (null != interfaceName) {
            AttributedQNameType interfaceNameType = new AttributedQNameType();
            
            interfaceNameType.setValue(new QName(targetNameSpace, interfaceName));
            
            org.objectweb.celtix.ws.addressing.wsdl.ObjectFactory objectFactory = 
                new org.objectweb.celtix.ws.addressing.wsdl.ObjectFactory();
            JAXBElement<AttributedQNameType> jaxbElement = 
                objectFactory.createInterfaceName(interfaceNameType);
      
            DOMResult result = new DOMResult();
            Marshaller marshaller;
            try {
                marshaller = getJAXBContext().createMarshaller();
                marshaller.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.TRUE);
                marshaller.marshal((Object)jaxbElement, result);
            } catch (JAXBException jbe) {
                throw new WebServiceException("setting service name failed", jbe);
            }
            MetadataType mt = ref.getMetadata();
            if (null == mt) {
                mt = new MetadataType();
                ref.setMetadata(mt);
            }
            Node node = result.getNode();
            if (node.getNodeType() != Node.ELEMENT_NODE) {
                node = result.getNode().getFirstChild();
            }
            mt.getAny().add(node);
        }
    }
  
    protected static String getInterfaceName(EndpointReferenceType ref) {
        String interfaceName = null;
        MetadataType metadata = ref.getMetadata();
        if (metadata != null) {
            for (Object obj : metadata.getAny()) {
                if (obj instanceof Element) {
                    Node node = (Element)obj;
                    if (node.getNamespaceURI().equals("http://www.w3.org/2005/08/addressing/wsdl")
                        && node.getNodeName().equals("InterfaceName")) {
                        String content = node.getTextContent();
                        interfaceName = content.substring(content.indexOf(":") + 1, content.length());
                    }
                }
            }
        }

        return interfaceName;
    }
    
    private static void setWSDLLocation(EndpointReferenceType ref, String wsdlLocation) {
        
        MetadataType metadata = ref.getMetadata();
        if (null == metadata) {
            metadata = new MetadataType();
            ref.setMetadata(metadata);
        }
        
        String wsdlLoc = getWSDLLocation(ref);
        if (wsdlLoc != null) {
            wsdlLoc = wsdlLoc + " " + wsdlLocation;
        } else {
            wsdlLoc = wsdlLocation;
        }
        
        metadata.getOtherAttributes().put(WSDL_LOCATION, wsdlLoc);
        
    }
    
    
    
    protected static String getWSDLLocation(EndpointReferenceType ref) {
        String wsdlLocation = null;
        MetadataType metadata = ref.getMetadata();
        if (metadata != null) {
            
            wsdlLocation = metadata.getOtherAttributes().get(WSDL_LOCATION);
            
        }

        return wsdlLocation;
    }
        

    /**
     * Sets the metadata on the provided endpoint reference.
     * @param ref the endpoint reference.
     * @param the list of metadata source.
     */
    public static void setMetadata(EndpointReferenceType ref, List<Source> metadata) {
        if (null != ref) {
            MetadataType mt = ref.getMetadata();
            if (null == mt) {
                mt = new MetadataType();
                ref.setMetadata(mt);
            }
            List<Object> anyList = mt.getAny();
            try {
                for (Source source : metadata) {
                    Node node = null;
                    if (!(source instanceof DOMSource)) {
                        DOMResult domResult = new DOMResult();
                        domResult.setSystemId(source.getSystemId());
                        
                        XML_TRANSFORMER.transform(source, domResult);

                        node = domResult.getNode();
                    } else {
                        node = ((DOMSource)node).getNode();
                    }

                    if (node instanceof Document) {
                        ((Document)node).setDocumentURI(source.getSystemId());
                        node =  node.getFirstChild();
                    }
                    while (null != node 
                           && node.getNodeType() != Node.ELEMENT_NODE) {
                        node = node.getNextSibling();
                    }
                    anyList.add(node);
                }
            } catch (TransformerException te) {
                throw new WebServiceException("Populating metadata in EPR failed", te);
            }
        }
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

        if (null == manager) {
            return null;
        }

        MetadataType metadata = ref.getMetadata();
        String location = getWSDLLocation(ref);

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

        String className = getInterfaceName(ref);
        if (null != className) {
            Class<?> sei = null;
            try {
                sei = Class.forName(className, true, manager.getClass().getClassLoader());
            } catch (ClassNotFoundException ex) {
                LOG.log(Level.SEVERE, "SEI_LOAD_FAILURE_MSG", ex);
                return null;
            }
            Definition def = manager.getDefinition(sei);
            if (def == null && sei.getInterfaces().length > 0) {
                sei = sei.getInterfaces()[0];
                def = manager.getDefinition(sei);
            }
            return def;
        }
        return null;
    }

    private static List<javax.wsdl.extensions.schema.Schema> getSchemas(Definition definition) {
        Types types = definition.getTypes();
        List<javax.wsdl.extensions.schema.Schema> schemaList = 
            new ArrayList<javax.wsdl.extensions.schema.Schema>();
        if (types != null) {
            for (Object o : types.getExtensibilityElements()) {
                if (o instanceof javax.wsdl.extensions.schema.Schema) {
                    javax.wsdl.extensions.schema.Schema s =
                        (javax.wsdl.extensions.schema.Schema) o;
                    schemaList.add(s);
                }
            }
        }

        Map wsdlImports = definition.getImports();
        for (Object o : wsdlImports.values()) {
            if (o instanceof List) {
                for (Object p : (List)o) {
                    if (p instanceof Import) {
                        schemaList.addAll(getSchemas(((Import)p).getDefinition()));
                    }
                }
            }
        }
        return schemaList;
    }

    public static Schema getSchema(WSDLManager manager, EndpointReferenceType ref) {
        Definition definition;
        try {
            definition = getWSDLDefinition(manager, ref);
        } catch (javax.wsdl.WSDLException wsdlEx) {
            return null;
        }
        if (definition == null) {
            return null;
        }
        synchronized (schemaMap) {
            if (schemaMap.containsKey(definition)) {
                return schemaMap.get(definition);
            }
        }
        Schema schema = schemaMap.get(definition);
        if (schema == null) {
            List<javax.wsdl.extensions.schema.Schema> schemas = getSchemas(definition);
            SchemaFactory factory = SchemaFactory.newInstance(
                XMLConstants.W3C_XML_SCHEMA_NS_URI);
            List<Source> schemaSources = new ArrayList<Source>();
            for (javax.wsdl.extensions.schema.Schema s : schemas) {
                Source source = new DOMSource(s.getElement());
                if (source != null) {
                    schemaSources.add(source);
                }
            }
            try {
                schema = factory.newSchema(schemaSources.toArray(
                    new Source[schemaSources.size()]));
                if (schema != null) {
                    synchronized (schemaMap) {
                        schemaMap.put(definition, schema);
                    }
                    LOG.log(Level.FINE, "Obtained schema from wsdl definition");
                }
            } catch (SAXException ex) {
                // Something not right with the schema from the wsdl.
                LOG.log(Level.WARNING, "SAXException for newSchema()", ex);
            }
        }
        return schema;
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
        if (def == null) {
            throw new WSDLException(WSDLException.OTHER_ERROR, "unable to find definition for reference");
        }

        MetadataType metadata = ref.getMetadata();
        for (Object objMeta : metadata.getAny()) {
            Object obj = objMeta;
            if (obj instanceof Element) {
                Element el = (Element)obj;
                if ("http://www.w3.org/2005/08/addressing/wsdl".equals(el.getNamespaceURI())) {
                    try {
                        Unmarshaller u = getJAXBContext().createUnmarshaller();
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
                Port port = null;
                ServiceNameType snt = (ServiceNameType)obj;
                LOG.log(Level.FINEST, "found service name ", snt.getEndpointName());
                Service service = def.getService(snt.getValue());
                if (service == null) {
                    service = (Service)def.getServices().values().iterator().next();
                    if (service == null) {
                        return null;
                    }
                }
                String endpoint = snt.getEndpointName();
                if ("".equals(endpoint) && service.getPorts().size() == 1) {
                    port = (Port)service.getPorts().values().iterator().next();
                } else {
                    port = service.getPort(endpoint);
                }
                // FIXME this needs to be looked at service.getPort(endpoint)
                //should not return null when endpoint is valid
                if (port == null) {
                    port = (Port)service.getPorts().values().iterator().next();
                }
                return port;
            }
        }

        if (def.getServices().size() == 1) {
            Service service = (Service)def.getServices().values().iterator().next();
            if (service.getPorts().size() == 1) { 
                return (Port)service.getPorts().values().iterator().next();
            }
        }
        
        QName serviceName = getServiceName(ref);
        if (null != serviceName) {
            Service service = def.getService(serviceName);
            if (service == null) {
                throw new WSDLException(WSDLException.OTHER_ERROR, "Cannot find service for " + serviceName);
            }
            if (service.getPorts().size() == 1) {
                return (Port)service.getPorts().values().iterator().next();
            }
            String str = getPortName(ref);
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
        setServiceAndPortName(reference, serviceName, portName);
        setWSDLLocation(reference, wsdlUrl.toString());

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
     * @throws WSDLException 
     */
    public static EndpointReferenceType getEndpointReference(WSDLManager manager, 
                                                                 Object implementor) {
  
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
        String serviceName = (null != ws) ? ws.serviceName() : wsp.serviceName();
        String targetNamespace = (null != ws) ? ws.targetNamespace() : wsp.targetNamespace();
        String portName = (null != ws) ? ws.portName() : wsp.portName();
        String url = (null != ws) ? ws.wsdlLocation() : wsp.wsdlLocation();
        String className = (null != ws) ? ws.endpointInterface() : null; 
     
        if (null != className && !"".equals(className)) {
            Class<?> seiClazz = null;
            try {
                seiClazz = Class.forName(className);
            } catch (ClassNotFoundException cnfe) {
                LOG.log(Level.SEVERE, "SEI_LOAD_FAILURE_MSG", cnfe);
                throw new WebServiceException("endpointInterface element in WebService annotation invalid", 
                                              cnfe);
            }
            
            if (!seiClazz.isInterface()) {
                throw new WebServiceException("endpointInterface element does not refer to a java interface");
            }
            
            WebService seiws = seiClazz.getAnnotation(WebService.class);
            if (null == seiws) {
                throw new WebServiceException("SEI should have a WebService Annotation");
            }

            if ("".equals(url)) {
                url = seiws.wsdlLocation();
            }
            
            //WebService.name maps to wsdl:portType name.
            className = seiws.name();
            
            //ServiceName,portName,endpointInterface not allowed on the WebService annotation 
            // of a SEI, Section 3.2 JSR181.            
            // set interfaceName using WebService.targetNamespace of SEI only.
            setInterfaceName(reference, ws.targetNamespace(), className);            
        } else {
            className = implementor.getClass().getName();
            
            // set interfaceName
            setInterfaceName(reference, targetNamespace, className);            
        }
        
        // set serviceName, portName and targetNamespace
        if (!"".equals(serviceName)) {
            setServiceAndPortName(reference, new QName(targetNamespace, serviceName), 
                                  portName);
        }

        if (null != url && url.length() > 0) {
            //REVISIT Resolve the url for all cases           
            URL wsdlUrl = implementor.getClass().getResource(url);
            if (wsdlUrl != null) {
                url = wsdlUrl.toExternalForm();
            }
        }
        // set wsdlLocation
        if (!"".equals(url)) {
            setWSDLLocation(reference, url); 
        }

        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("created endpoint reference with");
            LOG.fine("    service name: " + getServiceName(reference));
            LOG.fine("    wsdl location: " + getWSDLLocation(reference));
            LOG.fine("    sei class: " + getInterfaceName(reference));
        }
        return reference;
    }

    private static JAXBContext getJAXBContext() throws JAXBException {
        synchronized (EndpointReferenceUtils.class) {
            if (jaxbContext == null) {
                jaxbContext = JAXBContext.newInstance(WS_ADDRESSING_WSDL_PACKAGE);
            }
        }
        return jaxbContext;
    }
    
    private static String getNameSpaceUri(Node node, String content, String namespaceURI) {
        if (namespaceURI == null) {
            namespaceURI =  node.lookupNamespaceURI(content.substring(0, 
                                                                  content.indexOf(":")));
        }
        return namespaceURI;
    }

    private static String getService(String content) {
        return content.substring(content.indexOf(":") + 1, content.length());
    }
}
