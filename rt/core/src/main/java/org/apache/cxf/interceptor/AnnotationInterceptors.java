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

package org.apache.cxf.interceptor;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import javax.jws.WebService;

import org.apache.cxf.common.classloader.ClassLoaderUtils;
import org.apache.cxf.common.i18n.BundleUtils;
import org.apache.cxf.common.i18n.Message;
import org.apache.cxf.common.util.StringUtils;
import org.apache.cxf.endpoint.EndpointException;

public class AnnotationInterceptors {
    
    private static final ResourceBundle BUNDLE = BundleUtils.getBundle(AnnotationInterceptors.class);
    
    private Class<?> clazz;
    
    public AnnotationInterceptors(Class<?> clz) {
        clazz = clz;
    }
    
    public List<Interceptor> getInFaultInterceptors() throws EndpointException {
        return getInterceptors(InFaultInterceptors.class);
    }
    
    @SuppressWarnings (value = "unchecked")
    private List<Interceptor> getInterceptors(Class clz) throws EndpointException {
        Annotation  annotation = clazz.getAnnotation(clz);
        if (annotation != null) {
            return initializeInterceptors(getInterceptorNames(annotation));
        } else {
            WebService ws = clazz.getAnnotation(WebService.class);
            if (ws != null && !StringUtils.isEmpty(ws.endpointInterface())) {
                String seiClassName = ws.endpointInterface().trim();
                Class seiClass = null;
                try {
                    seiClass = ClassLoaderUtils.loadClass(seiClassName, this.getClass());
                } catch (ClassNotFoundException e) {
                    throw new RuntimeException("couldnt find class :" + seiClass, e);
                }
                annotation = seiClass.getAnnotation(clz);
                if (annotation != null) {
                    return initializeInterceptors(getInterceptorNames(annotation));
                }
            }           
        }
        return new ArrayList<Interceptor>();
    }
    
    private String[] getInterceptorNames(Annotation ann) {
        if (ann instanceof InFaultInterceptors) {
            return ((InFaultInterceptors)ann).interceptors();
        } else if (ann instanceof InInterceptors) {
            return ((InInterceptors)ann).interceptors();
        } else if (ann instanceof OutFaultInterceptors) {
            return ((OutFaultInterceptors)ann).interceptors();
        } else if (ann instanceof OutInterceptors) {
            return ((OutInterceptors)ann).interceptors();
        }
        throw new UnsupportedOperationException("Doesn't support other annotation for interceptor: " + ann);
    }
    
    
    private List<Interceptor> initializeInterceptors(String[] interceptors) throws EndpointException {
        List<Interceptor> theInterceptors = new ArrayList<Interceptor>();
        if (interceptors != null && interceptors.length > 0) {
            for (String interceptorName : interceptors) {
                Interceptor interceptor = null;
                try {
                    interceptor = (Interceptor)ClassLoaderUtils.loadClass(interceptorName, 
                                                                          this.getClass()).newInstance();
                } catch (ClassNotFoundException e) {
                    throw new EndpointException(new Message("COULD_NOT_CREATE_ANNOTATION_INTERCEPOTR", 
                                                    BUNDLE, interceptorName), e);
                } catch (InstantiationException ie) {
                    throw new EndpointException(new Message("COULD_NOT_CREATE_ANNOTATION_INTERCEPOTR", 
                                                    BUNDLE, interceptorName), ie);
                } catch (IllegalAccessException iae) {
                    throw new EndpointException(new Message("COULD_NOT_CREATE_ANNOTATION_INTERCEPOTR", 
                                                    BUNDLE, interceptorName), iae);
                }
                if (interceptor != null) {
                    theInterceptors.add(interceptor);
                }
            }
        }
        return theInterceptors;
    }


    public List<Interceptor> getInInterceptors() throws EndpointException {
        return getInterceptors(InInterceptors.class);
    }

    public List<Interceptor> getOutFaultInterceptors() throws EndpointException {
        return getInterceptors(OutFaultInterceptors.class);
    }


    public List<Interceptor> getOutInterceptors() throws EndpointException {
        return getInterceptors(OutInterceptors.class);
    }

}
