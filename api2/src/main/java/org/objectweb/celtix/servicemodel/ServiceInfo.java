package org.objectweb.celtix.servicemodel;

import java.util.HashMap;
import java.util.Map;

import javax.xml.namespace.QName;


public class ServiceInfo extends AbstractPropertiesHolder {
    QName name;
    Map<String, PortInfo> portMap = new HashMap<String, PortInfo>(2);
    String targetNamespace;

    
    public ServiceInfo() {
        
    }
    
    public String getTargetNamespace() {
        return targetNamespace;
    }
    public void setTargetNamespace(String ns) {
        targetNamespace = ns;
    }
    
    public void setName(QName n) {
        name = n;
    }
    public QName getName() {
        return name;
    }
    
    public PortInfo createPort(String q) {
        PortInfo pi = new PortInfo(q, this);
        addPort(pi);
        return pi;
    }
    
    public void addPort(PortInfo pi) {
        if (portMap.containsKey(pi.getName())) {
            throw new IllegalStateException("Already contains port named " + pi.getName());
        }
        portMap.put(pi.getName(), pi);
    }
    
    public PortInfo getPort(String q) {
        return portMap.get(q);
    }
    

}
