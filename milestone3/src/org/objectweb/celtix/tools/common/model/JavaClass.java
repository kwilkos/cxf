package org.objectweb.celtix.tools.common.model;

import java.util.*;

public class JavaClass extends JavaInterface {
    
    private final List<JavaField> jfield = new ArrayList<JavaField>();

    public JavaClass() {
    }
    
    public JavaClass(JavaModel model) {
        super(model);
    }

    public void addField(JavaField f) {
        this.jfield.add(f);
    }

    public List<JavaField> getFields() {
        return this.jfield;
    }
}
