package org.objectweb.celtix.tools.common.model;

import java.util.*;
import org.objectweb.celtix.tools.common.toolspec.ToolException;

public class JavaInterface {

    private String name;
    private String packageName;
    private String namespace;
    private String location;
    private JavaModel model;
    private JavaPort.SOAPStyle soapStyle;
    private JavaPort.SOAPUse soapUse;
    private JavaPort.SOAPParameterStyle soapParameterStyle;
    
    private final List<JavaMethod> methods = new ArrayList<JavaMethod>();
    private final List<String> annotations = new ArrayList<String>();

    public JavaInterface() {
    }
    
    public JavaInterface(JavaModel m) {
        this.model = m;
    }

    public void setSOAPStyle(JavaPort.SOAPStyle s) {
        this.soapStyle = s;
    }

    public JavaPort.SOAPStyle getSOAPStyle() {
        return this.soapStyle;
    }

    public void setSOAPUse(JavaPort.SOAPUse u) {
        this.soapUse = u;
    }

    public JavaPort.SOAPUse getSOAPUse() {
        return this.soapUse;
    }

    public void setSOAPParameterStyle(JavaPort.SOAPParameterStyle p) {
        this.soapParameterStyle = p;
    }    
    
    public JavaPort.SOAPParameterStyle getSOAPParameterStyle() {
        return this.soapParameterStyle;
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

    public List<JavaMethod> getMethods() {
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

    public void addMethod(JavaMethod method) throws ToolException {
        if (hasMethod(method)) {
            throw new ToolException("model.uniqueness");
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

    public void addAnnotation(String annotation) {
        this.annotations.add(annotation);
    }

    public List getAnnotations() {
        return this.annotations;
    }
}
