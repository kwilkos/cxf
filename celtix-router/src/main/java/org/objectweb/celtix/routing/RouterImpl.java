package org.objectweb.celtix.routing;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.wsdl.Definition;
import javax.wsdl.Port;
import javax.wsdl.PortType;
import javax.wsdl.Service;
import javax.xml.namespace.QName;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.ws.Endpoint;
import javax.xml.ws.WebServiceException;

import org.objectweb.celtix.bus.configuration.wsdl.WsdlPortProvider;
import org.objectweb.celtix.common.i18n.Message;
import org.objectweb.celtix.common.logging.LogUtils;
import org.objectweb.celtix.jaxb.JAXBUtils;
import org.objectweb.celtix.routing.configuration.DestinationType;
import org.objectweb.celtix.routing.configuration.RouteType;
import org.objectweb.celtix.routing.configuration.SourceType;


public class RouterImpl implements Router {
    private static final Logger LOG = LogUtils.getL7dLogger(Router.class);
    
    protected final ClassLoader seiClassLoader;
    protected final Definition wsdlModel;
    protected final RouteType route;
    protected Map<QName, Port> sourcePortMap;
    protected Map<QName, Port> destPortMap;
    protected List<Endpoint> epList;
    
    public RouterImpl(ClassLoader loader, Definition model , RouteType rt) {
        seiClassLoader = loader;
        wsdlModel = model;
        route = rt;
        getSourceServicesAndPorts();
        getDestinationServicesAndPorts();
        epList = new ArrayList<Endpoint>(route.getSource().size());
    }
    
    public Definition getWSDLModel() {
        return wsdlModel;
    }
    
    public RouteType getRoute() {
        return route;
    }
    
    public void init() {
        List<SourceType> stList = route.getSource();
        
        List<Source> metadata = createMetadata();
        for (SourceType st : stList) {
            //TODO Config For Pass Through
            Port p = sourcePortMap.get(st.getService());
            WsdlPortProvider portProvider = new WsdlPortProvider(p);
            String srcBindingId = (String) portProvider.getObject("bindingId");
            Object implementor = null;
            if (isSameBindingId(srcBindingId)) {
                //Pass Through Mode
                implementor = new StreamSourceMessageProvider(wsdlModel, route);
            } else {
                //CodeGenerated Servant
                InvocationHandler implHandler = new SEIImplHandler(wsdlModel, route);
                implementor = createSEIImplementor(p.getBinding().getPortType(), implHandler);
            }

            Endpoint sourceEP = Endpoint.create(srcBindingId, implementor);

            Map<String, Object> properties = 
                createEndpointProperties(st.getService(), p.getName());            
            sourceEP.setMetadata(metadata);
            sourceEP.setProperties(properties);
            //TODO Set Executor on endpoint.
            epList.add(sourceEP);
        }
    }

    public void publish() {
        for (Endpoint ep : epList) {
            Port port = (Port) sourcePortMap.get(ep.getProperties().get(Endpoint.WSDL_SERVICE));
            WsdlPortProvider portProvider = new WsdlPortProvider(port);
            ep.publish((String) portProvider.getObject("address"));
        }
    }
    
    protected boolean isSameBindingId(String srcId) {        
        Collection<Port> destPorts = destPortMap.values();
        for (Port destPort : destPorts) {
            WsdlPortProvider portProvider = new WsdlPortProvider(destPort);
            String destId = (String) portProvider.getObject("bindingId");
            
            if (null == srcId
                && null == destId
                || srcId.equals(destId)) {
                continue;
            } else {
                return false;
            }
        }
        return true;
    }

    protected Object createSEIImplementor(PortType portType, InvocationHandler implHandler) {
        Object impl = null;
        StringBuffer seiName = new StringBuffer();
        seiName.append(JAXBUtils.namespaceURIToPackage(portType.getQName().getNamespaceURI()));
        seiName.append(".");
        seiName.append(JAXBUtils.nameToIdentifier(portType.getQName().getLocalPart(),
                                                  JAXBUtils.IdentifierType.INTERFACE));

        Class<?> sei = null;
        try {
            sei = seiClassLoader.loadClass(seiName.toString());
        } catch (ClassNotFoundException cnfe) {
            throw new WebServiceException("Could not load sei", cnfe);
        }
        
        impl = Proxy.newProxyInstance(sei.getClassLoader(),
                                      new Class[] {sei},
                                      implHandler);
        return impl;
    }

    private Map<String, Object> createEndpointProperties(QName serviceName, String portName) {
        Map<String, Object> props = new HashMap<String, Object>(2);
        props.put(Endpoint.WSDL_SERVICE, serviceName);
        props.put(Endpoint.WSDL_PORT, new QName(serviceName.getNamespaceURI(), portName));
        return props;
    }

    private List<Source> createMetadata() {
        List<Source> metadata = new ArrayList<Source>();
        metadata.add(new StreamSource(wsdlModel.getDocumentBaseURI()));
        return metadata;
    }
    
    private void getSourceServicesAndPorts() {
        List<SourceType> stList = route.getSource();
        
        if (null == sourcePortMap) {
            sourcePortMap = new HashMap<QName, Port>(stList.size());
        }

        for (SourceType st : stList) {
            Service sourceService = wsdlModel.getService(st.getService());
            if (null == sourceService) {
                throw new WebServiceException(
                            new Message("UNDEFINED_SERVICE", LOG, st.getService()).toString());
            }
            Port sourcePort = sourceService.getPort(st.getPort());
            
            if (null == sourcePort) {
                throw new WebServiceException(
                            new Message("UNDEFINED_PORT", LOG, st.getPort()).toString());                
            }
            sourcePortMap.put(sourceService.getQName(), sourcePort);
        }
    }
    
    private void getDestinationServicesAndPorts() {
        List<DestinationType> dtList = route.getDestination();
        
        if (null == destPortMap) {
            destPortMap = new HashMap<QName, Port>(dtList.size());
        }

        for (DestinationType dt : dtList) {
            Service destService = wsdlModel.getService(dt.getService());
            if (null == destService) {
                throw new WebServiceException(
                            new Message("UNDEFINED_SERVICE", LOG, dt.getService()).toString());
            }
            Port destPort = destService.getPort(dt.getPort());
            
            if (null == destPort) {
                throw new WebServiceException(
                            new Message("UNDEFINED_PORT", LOG, dt.getPort()).toString());                
            }
            destPortMap.put(destService.getQName(), destPort);
        }
    }
}
