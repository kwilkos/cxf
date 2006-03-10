package org.objectweb.celtix.geronimo.builder;

import java.util.List;

import com.sun.java.xml.ns.j2ee.PortComponentHandlerType;

public class PortInfo {

    private String portName;
    private String seiInterfaceName;
    private String wsdlFile;
    private String servletLink;
    private List<PortComponentHandlerType> handlers;
   
    public String getPortName() {
        return portName;
    }
    public void setPortName(String pn) {
        portName = pn;
    }
    public String getServiceEndpointInterfaceName() {
        return seiInterfaceName;
    }
    public void setServiceEndpointInterfaceName(String sei) {
        seiInterfaceName = sei;
    }
    public String getServletLink() {
        return servletLink;
    }
    public void setServletLink(String sl) {
        servletLink = sl;
    }
    public String getWsdlFile() {
        return wsdlFile;
    }
    public void setWsdlFile(String wf) {
        wsdlFile = wf;
    }
    
    public void setHandlers(List<PortComponentHandlerType> h) {
        handlers = h;
    }
    
    public List<PortComponentHandlerType> getHandlers() {
        return handlers;
    }
    
    
}
