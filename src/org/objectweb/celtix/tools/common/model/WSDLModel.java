package org.objectweb.celtix.tools.common.model;

import javax.wsdl.Definition;
import javax.wsdl.WSDLException;
import javax.wsdl.factory.WSDLFactory;

public class WSDLModel {

    private Definition definition;
    private String wsdlLocation;
    private String serviceName;
    private String targetNameSpace;
    private String portTypeName;
    private String packageName;

    public WSDLModel() throws Exception {
        try {
            WSDLFactory wsdlFactory = WSDLFactory.newInstance();
            definition = wsdlFactory.newDefinition();
        } catch (WSDLException e) {
            // TODO Auto-generated catch block
            throw e;
        }
    }

    public void setWsdllocation(String loc) {
        this.wsdlLocation = loc;

    }

    public String getWsdllocation() {
        return this.wsdlLocation;

    }

    public void setServiceName(String name) {
        this.serviceName = name;
    }

    public String getServiceName() {
        return this.serviceName;
    }

    public void setPortyTypeName(String name) {
        this.portTypeName = name;
    }

    public String getPortyTypeName() {
        return this.portTypeName;
    }

    public void setTargetNameSpace(String space) {
        this.targetNameSpace = space;
    }

    public String getTargetNameSpace() {
        return this.targetNameSpace;
    }

    public Definition getDefinition() {
        return this.definition;
    }

    public String getPackageName() {
        return this.packageName;
    }

    public void setPackageName(String name) {
        this.packageName = name;
    }

}
