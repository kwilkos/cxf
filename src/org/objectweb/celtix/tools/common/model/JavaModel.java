package org.objectweb.celtix.tools.common.model;

import java.util.*;

public class JavaModel {

    private final Map<String, JavaInterface> interfaces;
    private final Map<String, JavaExceptionClass> exceptionClasses;
    private final Map<String, JavaServiceClass> serviceClasses;

    public JavaModel() {
        interfaces = new HashMap<String, JavaInterface>();
        exceptionClasses = new HashMap<String, JavaExceptionClass>();
        serviceClasses = new HashMap<String, JavaServiceClass>();
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
}
