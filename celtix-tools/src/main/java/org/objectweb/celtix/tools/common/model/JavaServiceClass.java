package org.objectweb.celtix.tools.common.model;

import java.util.ArrayList;
import java.util.List;

public class JavaServiceClass extends JavaClass {

    private final List<JavaPort> ports = new ArrayList<JavaPort>();
  
    private String serviceName;
    
    public JavaServiceClass(JavaModel model) {
        super(model);
    }

    public void addPort(JavaPort port) {
        ports.add(port);
    }

    public List getPorts() {
        return ports;
    }
    
    public void setServiceName(String name) {
        this.serviceName = name;
    }
    
    public String getServiceName() {
        return serviceName;
    }
    

}
