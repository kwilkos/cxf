package org.objectweb.celtix.common.injection;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.InjectionComplete;
import javax.annotation.Resource;
import org.objectweb.celtix.common.annotation.AnnotationProcessor;
import org.objectweb.celtix.common.annotation.AnnotationVisitor;
import org.objectweb.celtix.common.logging.LogUtils;

/**
 * injects references specified using @Resource annotation 
 * 
 */
public class ResourceInjector implements AnnotationVisitor {

    private static final Logger LOG = LogUtils.getL7dLogger(ResourceInjector.class);

    private final ResourceResolver resolver; 
    private Object target; 

    public ResourceInjector(ResourceResolver r) {
        resolver = r;
    }

    public void inject(Object o) {

        AnnotationProcessor processor = new AnnotationProcessor(o); 
        processor.accept(this); 

        invokeInjectionComplete();
    }



    // Implementation of org.objectweb.celtix.common.annotation.AnnotationVisitor

    public final void visitClass(final Class<?> clz, final Annotation annotation) {
        
        assert annotation != null && annotation instanceof Resource : annotation; 

        Resource res = (Resource)annotation; 

        if (res.name() == null || "".equals(res.name())) { 
            LOG.log(Level.SEVERE, "RESOURCE_NAME_NOT_SPECIFIED", target.getClass().getName());
            return;
        } 

        Object resource = resolver.resolve(res.name(), res.type()); 
        if (resource == null) {
            LOG.log(Level.SEVERE, "RESOURCE_RESOLVE_FAILED");
            return;
        } 

        // first find a setter that matches this resource
        Method setter = findSetterForResource(res);
        if (setter != null) { 
            invokeSetter(setter, resource);
            return;
        }
        
        Field field = findFieldForResource(res);
        if (field != null) { 
            injectField(field, resource); 
            return;
        }

        LOG.log(Level.SEVERE, "NO_SETTER_OR_FIELD_FOR_RESOURCE", getTarget().getClass().getName());

    }

    public final List<Class<? extends Annotation>> getTargetAnnotations() {
        List<Class<? extends Annotation>> al = new LinkedList<Class<? extends Annotation>>();
        al.add(Resource.class); 
        return al;
    }

    public final void visitField(final Field field, final Annotation annotation) {

        assert annotation != null && annotation instanceof Resource : annotation;
        
        Resource res = (Resource)annotation;

        String name = getFieldNameForResource(res, field);
        Class<?> type = getFieldTypeForResource(res, field); 
        
        // TODO -- need to add some magic to set private fields
        // here 
        if ((field.getModifiers() & (Modifier.PRIVATE ^ Modifier.PROTECTED)) > 0) {
            LOG.log(Level.SEVERE, "PRIVATE_FIELD_INJECTION_NYI", field);
            return;
        } 

        Object resource = resolver.resolve(name, type);
        if (resource != null) {
            injectField(field, resource);
        } else {
            LOG.log(Level.SEVERE, "RESOURCE_RESOLVE_FAILED");
        }
    }

    public final void visitMethod(final Method method, final Annotation annotation) {
        
        assert annotation != null && annotation instanceof Resource : annotation;

        Resource res = (Resource)annotation; 

        String resourceName = getResourceName(res, method);
        Class<?> clz = getResourceType(res, method); 
        Object resource = resolver.resolve(resourceName, clz);
        if (resource != null) {
            invokeSetter(method, resource);
        } else { 
            LOG.log(Level.SEVERE, "RESOURCE_RESOLVE_FAILED");
        }
    }


    public final void setTarget(final Object object) {
        target = object;
    }

    public final Object getTarget() { 
        return target; 
    } 

    private Field findFieldForResource(Resource res) {
        assert target != null; 
        assert res.name() != null;

        for (Field field : target.getClass().getFields()) { 
            if (field.getName().equals(res.name())) { 
                return field;
            } 
        }
        return null;
    }


    private Method findSetterForResource(Resource res) {
        assert target != null; 

        String setterName = resourceNameToSetter(res.name());
        Method setterMethod = null;

        for (Method method : getTarget().getClass().getMethods()) {
            if (setterName.equals(method.getName())) {
                setterMethod = method;
                break;
            }
        }

        if (setterMethod.getParameterTypes().length != 1) {
            LOG.log(Level.WARNING, "SETTER_INJECTION_WITH_INCORRECT_TYPE", setterMethod);
        }
        return setterMethod;
    }

    
    private String resourceNameToSetter(String resName) {

        return "set" + Character.toUpperCase(resName.charAt(0)) + resName.substring(1);
    }
    

    private void invokeSetter(Method method, Object resource) { 

        try {
            method.invoke(getTarget(), resource);
        } catch (IllegalAccessException e) { 
            LOG.log(Level.SEVERE, "INJECTION_SETTER_NOT_VISIBLE", method);
        } catch (InvocationTargetException e) { 
            LogUtils.log(LOG, Level.SEVERE, "INJECTION_SETTER_RAISED_EXCEPTION", e, method);
        } 
    } 


    private Class<?> getResourceType(Resource res, Method method) { 
        return res.type() != null ? res.type() : method.getParameterTypes()[0];
    } 

    private String getResourceName(Resource res, Method method) { 
        assert method != null; 
        assert res != null; 
        assert method.getName().startsWith("set") : method;

        if (res.name() == null || "".equals(res.name())) {
            String name = method.getName(); 
            name = name.substring(3);
            name = Character.toLowerCase(name.charAt(0)) + name.substring(1); 
            return name;
        }
        return res.name();
    } 



    private void injectField(Field field, Object resource) { 
        try {
            field.set(getTarget(), resource);
        } catch (IllegalAccessException e) { 
            e.printStackTrace();
            LOG.severe("FAILED_TO_INJECT_FIELD"); 
        } 
    } 


    private void invokeInjectionComplete() {

        for (Method method : getTarget().getClass().getMethods()) {
            InjectionComplete ic = method.getAnnotation(InjectionComplete.class);
            if (ic != null) {
                try {
                    method.invoke(target);
                } catch (IllegalAccessException e) {
                    LOG.log(Level.WARNING, "INJECTION_COMPLETE_NOT_VISIBLE", method);
                } catch (InvocationTargetException e) {
                    LOG.log(Level.WARNING, "INJECTION_COMPLETE_THREW_EXCEPTION", e);
                }
            }
        }
    }
     
        
    private Class<?> getFieldTypeForResource(Resource res, Field field) {
        assert res != null;
        
        Class type = res.type();
        if (res.type() == null || Object.class == res.type()) {
            type = field.getType();
        }
        return type;
    }

    private String getFieldNameForResource(Resource res, Field field) {
        assert res != null;
        if (res.name() == null || "".equals(res.name())) {
            return field.getName();
        }
        return res.name();
    }

}
