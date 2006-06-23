package org.objectweb.celtix.management;

import javax.wsdl.Port;
import javax.wsdl.WSDLException;

import org.objectweb.celtix.Bus;
import org.objectweb.celtix.ws.addressing.EndpointReferenceType;
import org.objectweb.celtix.wsdl.EndpointReferenceUtils;

public class TransportInstrumentation {
    protected Bus bus;
    protected String objectName;
    protected String serviceName;
    protected String portName;
    
    public TransportInstrumentation(Bus b) {
        bus = b;
    }

    protected String findServiceName(EndpointReferenceType reference) {
        return EndpointReferenceUtils.getServiceName(
             reference).toString();
    }
    
    protected String findPortName(EndpointReferenceType reference) {      
        Port port = null;
        String name = " ";
        try {
            port = EndpointReferenceUtils.getPort(bus.getWSDLManager(), reference);
            name = port.getName();
        } catch (WSDLException e) {
            // wsdlexception 
        }        
        return name;        
    }
    
    protected String getPortObjectName() {
        return ",Bus.Service=\"" + serviceName + "\"" + ",Bus.Service.Port=" + portName;
    }

}
