package org.objectweb.celtix.common.injection;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.InjectionComplete;
import javax.annotation.Resource;
import org.objectweb.celtix.common.logging.LogUtils;

/**
 * injects references specified using @Resource annotation 
 * 
 */
public class ResourceInjector {

    private static final Logger LOG = LogUtils.getL7dLogger(ResourceInjector.class);


    private final ResourceResolver resolver; 

    public ResourceInjector(ResourceResolver r) {
        resolver = r;
    }

    public void inject(Object target) {

        injectClassLevel(target);
        injectSetters(target); 
        injectFields(target);
        invokeInjectionComplete(target);
    }

    private void injectClassLevel(Object target) {
        
        LOG.fine("injecting class resource references");
        
        Resource res = target.getClass().getAnnotation(Resource.class);
        if (res == null) {
            return;
        }

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
        Method setter = findSetterForResource(target, res);
        if (setter != null) { 
            invokeSetter(setter, target, resource);
            return;
        }
        
        Field field = findFieldForResource(target, res);
        if (field != null) { 
            injectField(target, field, resource); 
            return;
        }

        LOG.log(Level.SEVERE, "NO_SETTER_OR_FIELD_FOR_RESOURCE", target.getClass().getName());
    }


    private Field findFieldForResource(Object target, Resource res) {
        assert target != null; 
        assert res.name() != null;

        for (Field field : target.getClass().getFields()) { 
            if (field.getName().equals(res.name())) { 
                return field;
            } 
        }
        return null;
    }


    private Method findSetterForResource(Object target, Resource res) {
        assert target != null; 

        String setterName = resourceNameToSetter(res.name());
        Method setterMethod = null;

        for (Method method : target.getClass().getMethods()) {
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
    

    private void injectSetters(Object target) { 
        LOG.fine("injecting setter resource references");

        Method[] methods = target.getClass().getMethods();
        for (Method method : methods) {
            Resource res = method.getAnnotation(Resource.class);
            if (res == null) {
                continue;
            }
            
            String resourceName = getResourceName(res, method);
            Class<?> clz = getResourceType(res, method); 

            Object resource = resolver.resolve(resourceName, clz);
            if (resource != null) {
                invokeSetter(method, target, resource);
            } else { 
                LOG.log(Level.SEVERE, "RESOURCE_RESOLVE_FAILED");
            }
        }

    } 


    private void invokeSetter(Method method, Object target, Object resource) { 

        try {
            method.invoke(target, resource);
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


    private void injectFields(Object target) { 
        LOG.fine("injecting field resource references");

        for (Field field : target.getClass().getFields()) {
            Resource res  = field.getAnnotation(Resource.class);

            if (res == null) {
                continue;
            }

            String name = getFieldNameForResource(res, field);
            Class<?> type = getFieldTypeForResource(res, field); 
            
            // TODO -- need to add some magic to set private fields
            // here 
            if ((field.getModifiers() & (Modifier.PRIVATE ^ Modifier.PROTECTED)) > 0) {
                LOG.log(Level.SEVERE, "PRIVATE_FIELD_INJECTION_NYI", field);
                continue;
            } 

            Object resource = resolver.resolve(name, type);
            if (resource != null) {
                injectField(target, field, resource);
            } else {
                LOG.log(Level.SEVERE, "RESOURCE_RESOLVE_FAILED");
            }
        }
    } 

    
    private void injectField(Object target, Field field, Object resource) { 
        try {
            field.set(target, resource);
        } catch (IllegalAccessException e) { 
            e.printStackTrace();
            LOG.severe("FAILED_TO_INJECT_FIELD"); 
        } 
    } 


    private void invokeInjectionComplete(Object target) {

        for (Method method : target.getClass().getMethods()) {
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
