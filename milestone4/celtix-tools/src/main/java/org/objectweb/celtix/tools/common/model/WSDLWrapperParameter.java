package org.objectweb.celtix.tools.common.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.sun.xml.bind.api.TypeReference;

import org.objectweb.celtix.tools.common.ToolException;
import org.objectweb.celtix.tools.common.model.JavaType.Style;

public class WSDLWrapperParameter {

    protected String name;
    protected String type;
    protected String className;
    protected String targetNamespace;
    protected final List<JavaParameter> wrapperChildren = new ArrayList<JavaParameter>();
    private TypeReference typeRef;
    private Style style;
    private String pname;
    
    public WSDLWrapperParameter() {
        
    }

    public WSDLWrapperParameter(String paraName, TypeReference ref, Style paraStyle) {
        pname = paraName;
        typeRef = ref;
        style = paraStyle;
    }
    
    public void setName(String arg) {
        this.pname = arg;
    }

    public void setClassName(String clzName) {
        this.className = clzName;
    }

    public String getClassName() {
        return this.className;
    }

    public void setTargetNamespace(String tns) {
        this.targetNamespace = tns;
    }

    public String getTargetNamespace() {
        return this.targetNamespace;
    }

    public Style getStyle() {
        return this.style;
    }

    public void setStyle(Style s) {
        this.style = s;
    }

    public String getName() {
        return pname;
    }

    

    public void setTypeReference(TypeReference ref) {
        this.typeRef = ref;
    }

    public TypeReference getTypeReference() {
        return this.typeRef;
    }

    public boolean isWrapped() {
        return true;
    }

    public List<JavaParameter> getWrapperChildren() {
        return Collections.unmodifiableList(wrapperChildren);
    }

    public void addWrapperChild(JavaParameter wrapperChild) {
        if (wrapperChildren.contains(wrapperChild)) {
            throw new ToolException("Wrapper Children IS Not Unique");
        }
        wrapperChildren.add(wrapperChild);
    }

    public JavaParameter removeWrapperChild(int index) {
        return wrapperChildren.remove(index);
    }

    public void clear() {
        wrapperChildren.clear();
    }

   
}
