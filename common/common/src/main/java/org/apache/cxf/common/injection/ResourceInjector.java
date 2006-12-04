/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.cxf.common.injection;


import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.annotation.Resources;
import org.apache.cxf.common.annotation.AnnotationProcessor;
import org.apache.cxf.common.annotation.AnnotationVisitor;
import org.apache.cxf.common.logging.LogUtils;
import org.apache.cxf.resource.ResourceManager;
import org.apache.cxf.resource.ResourceResolver;


/**
 * injects references specified using @Resource annotation 
 * 
 */
public class ResourceInjector implements AnnotationVisitor {

    private static final Logger LOG = LogUtils.getL7dLogger(ResourceInjector.class);

    private final ResourceManager resourceManager; 
    private final List<ResourceResolver> resourceResolvers;
    private Object target; 

    public ResourceInjector(ResourceManager resMgr) {
        this(resMgr, resMgr.getResourceResolvers());
    }

    public ResourceInjector(ResourceManager resMgr, List<ResourceResolver> resolvers) {
        resourceManager = resMgr;
        resourceResolvers = resolvers;
    }
    
    
    public void inject(Object o) {

        AnnotationProcessor processor = new AnnotationProcessor(o); 
        processor.accept(this); 

        invokePostConstruct();
    }
    
    public void construct(Object o) {
        setTarget(o);
        invokePostConstruct();
    }



    // Implementation of org.apache.cxf.common.annotation.AnnotationVisitor

    public final void visitClass(final Class<?> clz, final Annotation annotation) {
        
        assert annotation instanceof Resource || annotation instanceof Resources : annotation; 

        if (annotation instanceof Resource) { 
            injectResourceClassLevel(clz, (Resource)annotation); 
        } else if (annotation instanceof Resources) { 
            Resources resources = (Resources)annotation;
            for (Resource resource : resources.value()) {
                injectResourceClassLevel(clz, resource); 
            }
        } 

    }

    private void injectResourceClassLevel(Class<?> clz, Resource res) { 
        if (res.name() == null || "".equals(res.name())) { 
            LOG.log(Level.INFO, "RESOURCE_NAME_NOT_SPECIFIED", target.getClass().getName());
            return;
        } 

        Object resource = null;
        // first find a setter that matches this resource
        Method setter = findSetterForResource(res);
        if (setter != null) { 
            Class<?> type = getResourceType(res, setter); 
            resource = resolveResource(res.name(), type);
            if (resource == null) {
                LOG.log(Level.INFO, "RESOURCE_RESOLVE_FAILED");
                return;
            } 

            invokeSetter(setter, resource);
            return;
        }
        
        Field field = findFieldForResource(res);
        if (field != null) { 
            Class<?> type = getResourceType(res, field); 
            resource = resolveResource(res.name(), type);
            if (resource == null) {
                LOG.log(Level.INFO, "RESOURCE_RESOLVE_FAILED");
                return;
            } 
            injectField(field, resource); 
            return;
        }
        LOG.log(Level.SEVERE, "NO_SETTER_OR_FIELD_FOR_RESOURCE", getTarget().getClass().getName());
    } 

    public final List<Class<? extends Annotation>> getTargetAnnotations() {
        List<Class<? extends Annotation>> al = new LinkedList<Class<? extends Annotation>>();
        al.add(Resource.class); 
        al.add(Resources.class); 
        return al;
    }

    public final void visitField(final Field field, final Annotation annotation) {

        assert annotation instanceof Resource : annotation;
        
        Resource res = (Resource)annotation;

        String name = getFieldNameForResource(res, field);
        Class<?> type = getResourceType(res, field); 
        
        Object resource = resolveResource(name, type);
        if (resource != null) {
            injectField(field, resource);
        } else {
            LOG.log(Level.INFO, "RESOURCE_RESOLVE_FAILED", name);
        }
    }

    public final void visitMethod(final Method method, final Annotation annotation) {
        
        assert annotation instanceof Resource : annotation;

        Resource res = (Resource)annotation; 
        
        String resourceName = getResourceName(res, method);
        Class<?> clz = getResourceType(res, method); 

        Object resource = resolveResource(resourceName, clz);
        if (resource != null) {
            invokeSetter(method, resource);
        } else { 
            LOG.log(Level.INFO, "RESOURCE_RESOLVE_FAILED", new Object[] {resourceName, clz});
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

        for (Field field : target.getClass().getDeclaredFields()) { 
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
        
        if (setterMethod != null && setterMethod.getParameterTypes().length != 1) {
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
        assert field != null; 
        assert resource != null; 

        boolean accessible = field.isAccessible(); 
        try {
            if (field.getType().isAssignableFrom(resource.getClass())) { 
                field.setAccessible(true); 
                field.set(getTarget(), resource);
            }
        } catch (IllegalAccessException e) { 
            e.printStackTrace();
            LOG.severe("FAILED_TO_INJECT_FIELD"); 
        } finally {
            field.setAccessible(accessible); 
        }
    } 


    public void invokePostConstruct() {
        
        boolean accessible = false; 
        for (Method method : getPostConstructMethods()) {
            PostConstruct pc = method.getAnnotation(PostConstruct.class);
            if (pc != null) {
                try {
                    method.setAccessible(true);
                    method.invoke(target);
                } catch (IllegalAccessException e) {
                    LOG.log(Level.WARNING, "INJECTION_COMPLETE_NOT_VISIBLE", method);
                } catch (InvocationTargetException e) {
                    LOG.log(Level.WARNING, "INJECTION_COMPLETE_THREW_EXCEPTION", e);
                } finally {
                    method.setAccessible(accessible); 
                }
            }
        }
    }

    private Collection<Method> getPostConstructMethods() { 

        Collection<Method> methods = new LinkedList<Method>(); 
        addPostConstructMethods(getTarget().getClass().getMethods(), methods); 
        addPostConstructMethods(getTarget().getClass().getDeclaredMethods(), methods);
        return methods;
    } 

    private void addPostConstructMethods(Method[] methods, Collection<Method> postConstructMethods) {
        for (Method method : methods) { 
            if (method.getAnnotation(PostConstruct.class) != null 
                && !postConstructMethods.contains(method)) {
                postConstructMethods.add(method); 
            }
        }
    } 
     
        
    /**
     * making this protected to keep pmd happy
     */
    protected Class<?> getResourceType(Resource res, Field field) {
        assert res != null;
        Class<?> type = res.type();
        if (res.type() == null || Object.class == res.type()) {
            type = field.getType();
        }
        return type;
    }


    private Class<?> getResourceType(Resource res, Method method) { 
        return res.type() != null && !Object.class.equals(res.type()) 
            ? res.type() 
            : method.getParameterTypes()[0];
    } 


    private String getFieldNameForResource(Resource res, Field field) {
        assert res != null;
        if (res.name() == null || "".equals(res.name())) {
            return field.getName();
        }
        return res.name();
    }

    private Object resolveResource(String resourceName, Class<?> type) {
        return resourceManager.resolveResource(resourceName, type, resourceResolvers);
    }
}
