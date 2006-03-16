package org.objectweb.celtix.routing;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.wsdl.Binding;
import javax.wsdl.Definition;
import javax.wsdl.Port;
import javax.wsdl.Service;
import javax.wsdl.extensions.ExtensibilityElement;
import javax.xml.ws.WebServiceException;

import org.objectweb.celtix.common.i18n.Message;
import org.objectweb.celtix.common.logging.LogUtils;
import org.objectweb.celtix.routing.configuration.DestinationType;
import org.objectweb.celtix.routing.configuration.RouteType;
import org.objectweb.celtix.routing.configuration.SourceType;


public class Router {
    private static final Logger LOG = LogUtils.getL7dLogger(Router.class);
    
    protected final Definition wsdlModel;
    protected final RouteType route;
    protected List<Service> sourceServices;
    protected List<Service> destServices;
    protected Map<Service, Port> sourcePortMap;
    protected Map<Service, Port> destPortMap;
    
    public Router(Definition model , RouteType rt) {
        wsdlModel = model;
        route = rt;
        getSourceServicesAndPorts();
        getDestinationServicesAndPorts();
    }
    
    public Definition getWSDLModel() {
        return wsdlModel;
    }
    
    public RouteType getRoute() {
        return route;
    }
    
    public void init() {
        for (Service s : sourceServices) {
            Port p = sourcePortMap.get(s);
            //TODO Config For Pass Through         
            if (isSameBindingId(p.getBinding())) {
                //PassThroughProvider)
            } else {
                //CodeGenerated Servant
            }
        }
    }
    
    private void getSourceServicesAndPorts() {
        if (null == sourceServices) {
            sourceServices = new ArrayList<Service>();
        }
        
        if (null == sourcePortMap) {
            sourcePortMap = new Hashtable<Service, Port>();
        }
        
        List<SourceType> stList = route.getSource();
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
            sourceServices.add(sourceService);
            sourcePortMap.put(sourceService, sourcePort);
        }
    }
    
    private void getDestinationServicesAndPorts() {
        if (null == destServices) {
            destServices = new ArrayList<Service>();
        }
        
        if (null == destPortMap) {
            destPortMap = new Hashtable<Service, Port>();
        }
        
        List<DestinationType> stList = route.getDestination();
        for (DestinationType dt : stList) {
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
            destServices.add(destService);
            destPortMap.put(destService, destPort);
        }
    }
    
    @SuppressWarnings("unchecked")
    protected boolean isSameBindingId(Binding sourceBinding) {

        List<ExtensibilityElement> srcExtList = sourceBinding.getExtensibilityElements();
        
        String srcId = null;
        if (srcExtList.size() > 0) {
            ExtensibilityElement srcExtEl = srcExtList.get(0);
            srcId = srcExtEl.getElementType().getNamespaceURI();
        }
        
        Collection<Port> destPorts = destPortMap.values();
        for (Port destPort : destPorts) {            
            Binding destBinding = destPort.getBinding();
            List<ExtensibilityElement> destExtList = destBinding.getExtensibilityElements();

            String destId = null;
            if (destExtList.size() > 0) {
                ExtensibilityElement destExtEl = destExtList.get(0);
                destId = destExtEl.getElementType().getNamespaceURI();
            }
            
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
}
