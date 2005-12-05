package org.objectweb.celtix.tools.common.model;

import java.util.ArrayList;
import java.util.List;

public class JavaPort {
    private String name;
    private String portType;
    private String bindingName;
    private final List<JavaMethod> operations = new ArrayList<JavaMethod>();
    private String address;
    private String soapVersion;
    private String style;
    private String transURI;
    //added by Jim
    private String interfaceClass; 
    private String namespace;
    public JavaPort(String pname) {
        this.name = pname;
    }

    public void setTransURI(String uri) {
        this.transURI = uri;
    }

    public String getTransURI() {
        return this.transURI;
    }

    public void setStyle(String sty) {
        this.style = sty;
    }

    public String getStyle() {
        return this.style;
    }

    public void setPortName(String portname) {
        name = portname;
    }

    public String getPortName() {
        return name;
    }

    public void setPortType(String type) {
        this.portType = type;
    }

    public String getPortType() {
        return portType;
    }

    public void setBindingName(String bName) {
        this.bindingName = bName;
    }

    public String getBindingName() {
        return bindingName;
    }

    public void addOperation(JavaMethod method) {
        operations.add(method);
    }

    public List getOperations() {
        return operations;
    }

    public void setBindingAdress(String add) {
        this.address = add;
    }

    public String getBindingAdress() {
        return address;
    }

    public void setSoapVersion(String version) {
        this.soapVersion = version;
    }

    public String getSoapVersion() {
        return soapVersion;
    }
    
    public void setInterfaceClass(String clzname) {
        this.interfaceClass = clzname;
    }

    public String getInterfaceClass() {
        return this.interfaceClass;
    }
    
    public void setNameSpace(String ns) {
        this.namespace = ns;
    }

    public String getNameSpace() {
        return this.namespace;
    }   
}
