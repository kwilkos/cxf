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

import javax.ws.rs.Path;

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
            ClassResourceInfo classResourceInfo = createRootClassResourceInfo(resourceClass);
            classResourceInfos.add(classResourceInfo);
        }

        JAXRSServiceImpl service = new JAXRSServiceImpl(classResourceInfos);

        setService(service);

        if (properties != null) {
            service.putAll(properties);
        }
    }

    protected ClassResourceInfo createRootClassResourceInfo(final Class<?> c) {
        Path uriTemplateAnnotation = c.getAnnotation(Path.class);
        if (uriTemplateAnnotation == null) {
            return null;
        }

        ClassResourceInfo classResourceInfo = createClassResourceInfo(c);

        String annotationValue = uriTemplateAnnotation.value();
        if (!annotationValue.startsWith("/")) {
            annotationValue = "/" + annotationValue;
        }
        String suffixPattern = (uriTemplateAnnotation.limited())
            ? URITemplate.LIMITED_REGEX_SUFFIX : URITemplate.UNLIMITED_REGEX_SUFFIX;
        URITemplate t = new URITemplate(annotationValue, suffixPattern);
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

    protected ClassResourceInfo createClassResourceInfo(final Class<?> c) {
        ClassResourceInfo classResourceInfo  = new ClassResourceInfo(c);

        MethodDispatcher md = createOperation(c, classResourceInfo);
        classResourceInfo.setMethodDispatcher(md);
        return classResourceInfo;
    }

    protected MethodDispatcher createOperation(Class c, ClassResourceInfo cri) {
        MethodDispatcher md = new MethodDispatcher();
        for (Method m : c.getMethods()) {
            String httpMethod = JAXRSUtils.getHttpMethodValue(m);
            
            if (m.getAnnotation(Path.class) != null && httpMethod != null) {
                /*
                 * Sub-resource method, URI template created by concatenating
                 * the URI template of the resource class with the URI template
                 * of the method
                 */
                OperationResourceInfo ori = new OperationResourceInfo(m, cri);
                String uriTemplate = m.getAnnotation(Path.class).value();
                if (!uriTemplate.startsWith("/")) {
                    uriTemplate = "/" + uriTemplate;
                }

                ori.setURITemplate(new URITemplate(uriTemplate, URITemplate.UNLIMITED_REGEX_SUFFIX));

                ori.setHttpMethod(httpMethod);

                md.bind(ori, m);
            } else if (m.getAnnotation(Path.class) != null) {
                // sub-resource locator
                OperationResourceInfo ori = new OperationResourceInfo(m, cri);
                String uriTemplate = m.getAnnotation(Path.class).value();
                if (!uriTemplate.startsWith("/")) {
                    uriTemplate = "/" + uriTemplate;
                }                
                ori.setURITemplate(new URITemplate(uriTemplate,
                                                   URITemplate.LIMITED_REGEX_SUFFIX));
                md.bind(ori, m);     
                
                //REVISIT: Check the return type is indeed a sub-resource type
                Class subResourceClass = m.getReturnType();
                
                //Iterate through sub-resources
                ClassResourceInfo subCri = createClassResourceInfo(subResourceClass);
                cri.addSubClassResourceInfo(subCri);
            } else if (httpMethod != null) {
                OperationResourceInfo ori = new OperationResourceInfo(m, cri);
                String uriTemplate = "/";
                ori.setURITemplate(new URITemplate(uriTemplate, URITemplate.UNLIMITED_REGEX_SUFFIX));
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
