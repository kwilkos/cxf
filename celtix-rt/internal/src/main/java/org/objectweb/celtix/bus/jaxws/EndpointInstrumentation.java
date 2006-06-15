package org.objectweb.celtix.bus.jaxws;


import java.util.ArrayList;
import java.util.List;

import javax.wsdl.Port;
import javax.wsdl.WSDLException;
import javax.xml.namespace.QName;
import javax.xml.ws.handler.Handler;

import org.objectweb.celtix.management.Instrumentation;
import org.objectweb.celtix.management.annotation.ManagedAttribute;
import org.objectweb.celtix.management.annotation.ManagedOperation;
import org.objectweb.celtix.management.annotation.ManagedResource;
import org.objectweb.celtix.wsdl.EndpointReferenceUtils;

@ManagedResource(componentName = "Endpoint", 
                 description = "The Celtix bus Endpoint", 
                 currencyTimeLimit = 15, persistPolicy = "OnUpdate")
public class EndpointInstrumentation implements Instrumentation {
    private static final String INSTRUMENTED_NAME = "Bus.Endpoint";
    private String objectName;
    private EndpointImpl endPoint;
    
    public EndpointInstrumentation(EndpointImpl ep) {
        endPoint = ep; 
        // setup the port name
        objectName = ",Bus.Service=\"" 
            + getEndpointServiceName() + "\"" 
            + ",Bus.Port=" + getEndpointPortName() 
            + ",name=Endpoint";
    }
    
    public String getInstrumentationName() {
        return INSTRUMENTED_NAME;
    }

    public Object getComponent() {
        return endPoint;
    }
    
    public String getUniqueInstrumentationName() {
        return objectName;
    }
    
    private String getEndpointServiceName() {
        QName serviceName = EndpointReferenceUtils.getServiceName(endPoint.getEndpointReferenceType());
        if (serviceName != null) {
            return serviceName.toString();
        } else {
            return "";
        }
        
    }
    
    private String getEndpointPortName() { 
        Port port;
        String portName = null;
        try {
            portName = EndpointReferenceUtils.getPortName(endPoint.getEndpointReferenceType());
            if (portName == null) {
                port = EndpointReferenceUtils.getPort(endPoint.getBus().getWSDLManager(), 
                                                      endPoint.getEndpointReferenceType());
                if (null != port) {
                    portName = port.getName();
                } else {
                    portName = "";
                }
            }
        
        } catch (WSDLException e) {
            // wsdlexception 
            // Now do nothing here           
        }     
        if (portName != null) {
            return portName;
        } else {
            if (null != endPoint.getImplementorClass()) {
                return endPoint.getImplementorClass().getName();
            }
            return "";
        }
    }
    
    @ManagedAttribute(currencyTimeLimit = 30, 
                      description = "The celtix bus Endpoint service name") 
    public String getServiceName() {
        return getEndpointServiceName();
    }    
    
    @ManagedAttribute(currencyTimeLimit = 30, 
                      description = "The celtix bus Endpoint port name")
    public String getPortName() {
        return getEndpointPortName();
    }
    
    @ManagedAttribute(currencyTimeLimit = -1,
                      description = "The state of celtix bus Endpoint")
    public String getState() {        
        if (endPoint.isPublished()) {
            return "ACTIVED";
        } else {
            return "DEACTIVED";
        }        
    }
    
    @ManagedAttribute(currencyTimeLimit = 30, 
                      description = "The celtix bus Registed Endpoints's handlerChains")
    public String[] getHandlerChains() {
        List<String> strList = new ArrayList<String>();
        List<Handler> handlers = endPoint.getServerBinding().getBinding().getHandlerChain();
        for (Handler h : handlers) {
            String handler = h.getClass().getName();
            strList.add(handler);
        }
        String[] strings = new String[strList.size()];
        return strList.toArray(strings);
    }

                     
    
    @ManagedOperation(currencyTimeLimit = 30, 
                      description = "The operation to start the celtix bus Endpoint") 
    public void start() {        
        endPoint.start();        
    }
    
    @ManagedOperation(currencyTimeLimit = 30, 
                      description = "The operation to stop the celtix bus Endpoint") 
    public void stop() {
        endPoint.stop();        
    }
    
}
