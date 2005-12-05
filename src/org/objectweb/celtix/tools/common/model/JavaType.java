package org.objectweb.celtix.tools.common.model;

public class JavaType {
    
    public static enum Style { IN, OUT, INOUT }
    protected String name;
    protected String type;
    protected String className;
    protected String targetNamespace;
    private Style style;

    public JavaType() {
    }

    public JavaType(String n, String t, String tns) {
        this.name = n;
        this.type = t;
        this.targetNamespace = tns;
        this.className = t;
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

    public String getName() {
        return name;
    }

    public void setName(String s) {
        name = s;
    }

    public String getType() {
        return type;
    }

    public void setType(String t) {
        type = t;
    }
    
    //
    // getter and setter for in, out inout style
    //
    public JavaType.Style getStyle() {
        return this.style;
    }

    public void setStyle(Style s) {
        this.style = s;
    }

    public boolean isIN() {
        return this.style == Style.IN;
    }

    public boolean isOUT() {
        return this.style == Style.OUT;
    }

    public boolean isINOUT() {
        return this.style == Style.INOUT;
    }
}
