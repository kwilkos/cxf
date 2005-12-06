package org.objectweb.celtix.tools.common.model;

import java.util.*;

public class JavaInterface {

    private String name;
    private String packageName;
    private String namespace;
    private String location;
    private JavaModel model;
    private final List<JavaMethod> methods = new ArrayList<JavaMethod>();

    public JavaInterface() {
    }
    
    public JavaInterface(JavaModel m) {
        this.model = m;
    }

    
    public JavaModel getJavaModel() {
        return this.model;
    }
    
    public void setName(String n) {
        this.name = n;
    }
    
    public String getName() {
        return name;
    }

    public void setLocation(String l) {
        this.location = l;
    }

    public String getLocation() {
        return this.location;
    }

    public List getMethods() {
        return methods;
    }

    public boolean hasMethod(JavaMethod method) {
        for (int i = 0; i < methods.size(); i++) {
            if (method.equals(methods.get(i))) {
                return true;
            }
        }
        return false;
    }

    public void addMethod(JavaMethod method) throws Exception {
        if (hasMethod(method)) {
            throw new Exception("model.uniqueness");
        }
        methods.add(method);
    }

    public String getPackageName() {
        return this.packageName;
    }

    public void setPackageName(String pn) {
        this.packageName = pn;
    }

    public String getNamespace() {
        return this.namespace;
    }

    public void setNamespace(String ns) {
        this.namespace = ns;
    }
}
