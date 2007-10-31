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

package org.apache.cxf.jaxrs;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.UriTemplate;

import org.apache.cxf.jaxrs.lifecycle.PerRequestResourceProvider;
import org.apache.cxf.jaxrs.lifecycle.ResourceProvider;
import org.apache.cxf.jaxrs.model.ClassResourceInfo;
import org.apache.cxf.jaxrs.model.MethodDispatcher;
import org.apache.cxf.jaxrs.model.OperationResourceInfo;
import org.apache.cxf.jaxrs.model.URITemplate;
import org.apache.cxf.service.Service;
import org.apache.cxf.service.factory.AbstractServiceFactoryBean;
import org.apache.cxf.service.invoker.Invoker;


/**
 * Build a JAX-RS service model from resource classes.
 */
public class JAXRSServiceFactoryBean extends AbstractServiceFactoryBean {
    
    //private static final Logger LOG = Logger.getLogger(JAXRSServiceFactoryBean.class.getName());
    //private static final ResourceBundle BUNDLE = BundleUtils.getBundle(JAXRSServiceFactoryBean.class);

    protected List<ClassResourceInfo> classResourceInfos;
    protected List<Class> resourceClasses;
    protected Map<Class, ResourceProvider> resourceProviders = new HashMap<Class, ResourceProvider>();
    
    private Invoker invoker;
    private Executor executor;
    private Map<String, Object> properties;

    public JAXRSServiceFactoryBean() {
    }

    @Override
    public Service create() {
        initializeServiceModel();

        initializeDefaultInterceptors();

        if (invoker != null) {
            getService().setInvoker(getInvoker());
        } else {
            getService().setInvoker(createInvoker());
        }

        if (getExecutor() != null) {
            getService().setExecutor(getExecutor());
        }
        if (getDataBinding() != null) {
            getService().setDataBinding(getDataBinding());
        }

        return getService();
    }

    public Executor getExecutor() {
        return executor;
    }

    public void setExecutor(Executor executor) {
        this.executor = executor;
    }

    public Invoker getInvoker() {
        return invoker;
    }

    public void setInvoker(Invoker invoker) {
        this.invoker = invoker;
    }

    public List<Class> getResourceClasses() {
        return resourceClasses;
    }

    public void setResourceClasses(List<Class> classes) {
        this.resourceClasses = classes;
    }

    public void setResourceClasses(Class... classes) {
        this.resourceClasses = new ArrayList<Class>(Arrays.asList(classes));
    }
    
    public void setResourceProvider(Class c, ResourceProvider rp) {
        resourceProviders.put(c, rp);
    }
    
    protected void initializeServiceModel() {
        classResourceInfos = new ArrayList<ClassResourceInfo>();

        for (Class resourceClass : resourceClasses) {
            ClassResourceInfo classResourceInfo = createClassResourceInfo(resourceClass);
            classResourceInfos.add(classResourceInfo);
        }

        JAXRSServiceImpl service = new JAXRSServiceImpl(classResourceInfos);

        setService(service);

        if (properties != null) {
            service.putAll(properties);
        }
    }

    protected ClassResourceInfo createClassResourceInfo(final Class<?> c) {
        UriTemplate uriTemplateAnnotation = c.getAnnotation(UriTemplate.class);
        if (uriTemplateAnnotation == null) {
            return null;
        }

        ClassResourceInfo classResourceInfo = getClassResourceInfo(c);

        MethodDispatcher md = createOperation(c, classResourceInfo);
        classResourceInfo.setMethodDispatcher(md);
        
        String annotationValue = uriTemplateAnnotation.value();
        if (!annotationValue.startsWith("/")) {
            annotationValue = "/" + annotationValue;
        }
        String rightHandPattern = (classResourceInfo.hasSubResources())
            ? URITemplate.SUB_RESOURCE_REGEX_SUFFIX : URITemplate.NONE_SUB_RESOURCE_REGEX_SUFFIX;
        URITemplate t = new URITemplate(annotationValue, rightHandPattern);
        classResourceInfo.setURITemplate(t);
        
        //TODO: Using information from annotation to determine which lifecycle provider to use
        ResourceProvider rp = resourceProviders.get(c);
        if (rp != null) {
            rp.setResourceClass(c);
            classResourceInfo.setResourceProvider(rp);
        } else {
            //default lifecycle is per-request
            rp = new PerRequestResourceProvider();
            rp.setResourceClass(c);
            classResourceInfo.setResourceProvider(rp);  
        }
        
        return classResourceInfo;
    }

    protected ClassResourceInfo getClassResourceInfo(final Class<?> c) {
        return new ClassResourceInfo(c);
    }

    protected MethodDispatcher createOperation(Class c, ClassResourceInfo cri) {
        MethodDispatcher md = new MethodDispatcher();
        for (Method m : c.getMethods()) {
            if (m.getAnnotation(UriTemplate.class) != null) {
                OperationResourceInfo ori = new OperationResourceInfo(m, cri);
                String uriTemplate = m.getAnnotation(UriTemplate.class).value();
                if (!uriTemplate.startsWith("/")) {
                    uriTemplate = "/" + uriTemplate;
                }
                if (m.getAnnotation(HttpMethod.class) != null) {
                    /*
                     * Sub-resource method, URI template created by concatenating
                     * the URI template of the resource class with the URI template
                     * of the method
                     */ 
                    ori.setURITemplate(new URITemplate(uriTemplate,
                                                       URITemplate.NONE_SUB_RESOURCE_REGEX_SUFFIX));
                    
                    String httpMethod = m.getAnnotation(HttpMethod.class).value();
                    ori.setHttpMethod(httpMethod);
                } else {
                    //sub-resource locator
                    cri.setHasSubResources(true);
                    ori.setURITemplate(new URITemplate(uriTemplate,
                                                       URITemplate.SUB_RESOURCE_REGEX_SUFFIX));
                }
                md.bind(ori, m);
            } else if (m.getAnnotation(HttpMethod.class) != null) {
                //Sub-resource method
                OperationResourceInfo ori = new OperationResourceInfo(m, cri);
                ori.setURITemplate(new URITemplate("/",
                                                   URITemplate.NONE_SUB_RESOURCE_REGEX_SUFFIX));
                
                String httpMethod = m.getAnnotation(HttpMethod.class).value();
                ori.setHttpMethod(httpMethod);
                md.bind(ori, m);               
            }
        }

        return md;
    }

    protected Invoker createInvoker() {
        return new JAXRSInvoker();
    }

}
