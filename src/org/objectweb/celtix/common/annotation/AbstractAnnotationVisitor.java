package org.objectweb.celtix.common.annotation;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public abstract class AbstractAnnotationVisitor implements AnnotationVisitor {

    private List<Class<? extends Annotation>> targetAnnotations = 
                                 new ArrayList<Class<? extends Annotation>>(); 
    
    private Object target; 
    
    protected AbstractAnnotationVisitor(Class<? extends Annotation> ann) {
        addTargetAnnotation(ann);
    }

    protected void addTargetAnnotation(Class<? extends Annotation> ann) { 
        targetAnnotations.add(ann); 
    } 

    public void visitClass(Class clz, Annotation annotation) {
        // complete
    }

    public List<Class<? extends Annotation>> getTargetAnnotations() {
        return targetAnnotations;
    }

    public void visitField(Field field, Annotation annotation) {
        // complete
    }

    public void visitMethod(Method method, Annotation annotation) {
        // complete
    }

    public void setTarget(Object object) {
        target = object;
    }
    
    public Object getTarget() { 
        return target;
    } 

}
