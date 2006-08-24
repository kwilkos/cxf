package org.apache.cxf.tools.common.model;

import java.util.ArrayList;
import java.util.List;
import javax.jws.soap.SOAPBinding;

public class JavaPort {
    
    private String name;
    private String portType;
    private String bindingName;
    private final List<JavaMethod> operations = new ArrayList<JavaMethod>();
    private String address;
    private String soapVersion;
    private SOAPBinding.Style style;
    private String transURI;
    private String interfaceClass; 
    private String packageName;
    private String namespace;
    private String portName;
    private String methodName;
    
    public JavaPort(String pname) {
        this.name = pname;
    }

    public void setTransURI(String uri) {
        this.transURI = uri;
    }

    public String getTransURI() {
        return this.transURI;
    }

    public void setStyle(SOAPBinding.Style sty) {
        this.style = sty;
    }

    public SOAPBinding.Style getStyle() {
        return this.style;
    }

    public void setName(String portname) {
        name = portname;
    }

    public String getName() {
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
    
    public void setPackageName(String pkgName) {
        this.packageName = pkgName;
    }
    
    public String getPackageName() {
        return this.packageName;
    }
    

    public String getInterfaceClass() {
        return this.interfaceClass;
    }
    
    
    public void setInterfaceClass(String clzname) {
        this.interfaceClass = clzname;
    }
    
    
    public void setNameSpace(String ns) {
        this.namespace = ns;
    }

    public String getNameSpace() {
        return this.namespace;
    }
    
    public void setPortName(String pname) {
        portName = pname;
    }
    
    public String getPortName() {
        return portName;
    }
    
    public void setMethodName(String mname) {
        methodName = mname;       
    }
    
    public String getMethodName(String mname) {
        return methodName;
    }
    
}
