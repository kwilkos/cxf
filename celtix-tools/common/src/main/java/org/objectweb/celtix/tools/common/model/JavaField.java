package org.objectweb.celtix.tools.common.model;

public class JavaField extends JavaType {
    private String modifier;

    public JavaField() {
    }

    public JavaField(String n, String t, String tns) {
        super(n, t, tns);
        this.modifier = "private";
    }

    public String getModifier() {
        return this.modifier;
    }
}
