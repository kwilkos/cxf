package org.apache.cxf.common.annotation;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.cxf.common.i18n.Message;
import org.apache.cxf.common.logging.LogUtils;


/** Process instance of an annotated class.  This is a visitable
 * object that allows an caller to visit that annotated elements in
 * this class definition.  If a class level annotation is overridden
 * by a member level annotation, only the visit method for the member
 * level annotation
 */
public  class AnnotationProcessor {
    
    private static final Logger LOG = LogUtils.getL7dLogger(AnnotationProcessor.class); 
    
    private static Method visitClassMethod; 
    private static Method visitFieldMethod; 
    private static Method visitMethodMethod; 
    
    static { 
        try {
            visitClassMethod = AnnotationVisitor.class.getMethod("visitClass", Class.class, Annotation.class);
            visitFieldMethod = AnnotationVisitor.class.getMethod("visitField", Field.class, Annotation.class);
            visitMethodMethod = AnnotationVisitor.class.getMethod("visitMethod", 
                                                                  Method.class, Annotation.class);
            
        } catch (NoSuchMethodException e) {
            // ignore
        }
        
    } 
    
    private final Object target; 
    private List<Class<? extends Annotation>> annotationTypes; 
    
    
    public AnnotationProcessor(Object o) {
        if (o == null) {
            throw new IllegalArgumentException(new Message("INVALID_CTOR_ARGS", LOG).toString()); 
        }
        target = o; 
    }
    
    /** 
     * Visits each of the annotated elements of the object.
     * 
     * @param visitor a visitor 
     *
     */
    public void accept(AnnotationVisitor visitor) { 
        
        if (visitor == null) {
            throw new IllegalArgumentException();
        }
        
        annotationTypes = visitor.getTargetAnnotations();
        visitor.setTarget(target);
        processClass(visitor);
        processFields(visitor); 
        processMethods(visitor);
    } 
    
    
    private void processMethods(AnnotationVisitor visitor) {
        
        visitAnnotatedElement(target.getClass().getDeclaredMethods(), visitor, visitMethodMethod); 
    }
    
    private void processFields(AnnotationVisitor visitor) { 
        
        visitAnnotatedElement(target.getClass().getDeclaredFields(), visitor, visitFieldMethod); 
    } 
    
    
    private void processClass(AnnotationVisitor visitor) {
        Class<?>[] classes = {target.getClass()}; 
        visitAnnotatedElement(classes, visitor, visitClassMethod);
    }
    
    private <U extends AnnotatedElement> void visitAnnotatedElement(U[] elements, 
                                                                    AnnotationVisitor visitor,
                                                                    Method visitorMethod) {
        
        for (U element : elements) {
            for (Class<? extends Annotation> clz : annotationTypes) {
                Annotation ann = element.getAnnotation(clz); 
                if (ann != null) {
                    try {
                        visitorMethod.invoke(visitor, element, ann);
                    } catch (IllegalAccessException e) {
                        // ignore, we're invoking methods of a public interface
                    } catch (InvocationTargetException e) {
                        Throwable cause = e.getCause() == null ? e : e.getCause();
                        LogUtils.log(LOG, Level.SEVERE, "VISITOR_RAISED_EXCEPTION", cause, visitor);
                    }
                }
            }
        }
    }
}
