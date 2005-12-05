package org.objectweb.celtix.tools.common.model;

import java.util.ArrayList;
import java.util.List;

public class JavaServiceClass extends JavaClass {

    private final List<JavaPort> ports = new ArrayList<JavaPort>();
  
    public JavaServiceClass(JavaModel model) {
        super(model);
    }

    public void addPort(JavaPort port) {
        ports.add(port);
    }

    public List getPorts() {
        return ports;
    }

}
