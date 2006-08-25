package org.objectweb.celtix.tools.common.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

import com.sun.xml.bind.api.TypeReference;

import org.objectweb.celtix.common.i18n.Message;
import org.objectweb.celtix.common.logging.LogUtils;
import org.objectweb.celtix.tools.common.ToolException;
import org.objectweb.celtix.tools.common.model.JavaType.Style;
public class WSDLParameter {
    private static final Logger LOG = LogUtils.getL7dLogger(WSDLParameter .class);
    protected String name;
    protected String type;
    protected String className;
    protected String targetNamespace;
    protected final List<JavaParameter> parts = new ArrayList<JavaParameter>();
    private TypeReference typeRef;
    private Style style;
    private String pname;
    
    public WSDLParameter() {
        
    }

    public WSDLParameter(String paraName, TypeReference ref, Style paraStyle) {
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

    public List<JavaParameter> getChildren() {
        return Collections.unmodifiableList(parts);
    }

    public void addChildren(JavaParameter jp) {
        if (parts.contains(jp)) {
            Message message = new Message("PART_ALREADY_EXIST", LOG, jp.getName());
            throw new ToolException(message);
        }
        parts.add(jp);
    }

    public JavaParameter removeChildren(int index) {
        return parts.remove(index);
    }

    public void clear() {
        parts.clear();
    }

   
}
