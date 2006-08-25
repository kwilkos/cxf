package org.objectweb.celtix.tools.common.model;

import java.util.*;
import org.objectweb.celtix.tools.extensions.jaxws.JAXWSBinding;

public class JavaModel {

    private final Map<String, JavaInterface> interfaces;
    private final Map<String, JavaExceptionClass> exceptionClasses;
    private final Map<String, JavaServiceClass> serviceClasses;
    
    private String location;
    private JAXWSBinding jaxwsBinding;
    
    public JavaModel() {
        interfaces = new HashMap<String, JavaInterface>();
        exceptionClasses = new HashMap<String, JavaExceptionClass>();
        serviceClasses = new HashMap<String, JavaServiceClass>();
        jaxwsBinding = new JAXWSBinding();
    }

    public void addInterface(String name, JavaInterface i) {
        this.interfaces.put(name, i);
    }

    public Map<String, JavaInterface> getInterfaces() {
        return this.interfaces;
    }


    public void addExceptionClass(String name, JavaExceptionClass ex) {
        this.exceptionClasses.put(name, ex);
    }
    
    public Map<String, JavaExceptionClass> getExceptionClasses() {
        return this.exceptionClasses;
    }

    public void addServiceClass(String name, JavaServiceClass service) {
        this.serviceClasses.put(name, service);
    }
    
    public Map<String, JavaServiceClass> getServiceClasses() {
        return this.serviceClasses;
    }

    public void setLocation(String l) {
        this.location = l;
    }

    public String getLocation() {
        return this.location;
    }

    public JAXWSBinding getJAXWSBinding() {
        return this.jaxwsBinding;
    }
    
    public void setJAXWSBinding(JAXWSBinding binding) {
        if (binding != null) {
            this.jaxwsBinding = binding;
        }
    }
}
