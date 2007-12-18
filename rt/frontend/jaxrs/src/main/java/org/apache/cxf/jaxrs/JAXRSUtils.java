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

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.ws.rs.HeaderParam;
import javax.ws.rs.MatrixParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.UriParam;
import javax.ws.rs.core.HttpContext;
import javax.ws.rs.ext.EntityProvider;
import javax.ws.rs.ext.ProviderFactory;

import org.apache.cxf.common.util.PrimitiveUtils;
import org.apache.cxf.jaxrs.model.ClassResourceInfo;
import org.apache.cxf.jaxrs.model.OperationResourceInfo;
import org.apache.cxf.jaxrs.model.URITemplate;

public final class JAXRSUtils {
    
    private JAXRSUtils() {        
    }
    
    public static ClassResourceInfo findSubResourceClass(ClassResourceInfo resource,
                                                             Class subResourceClassType) {
        for (ClassResourceInfo subCri : resource.getSubClassResourceInfo()) {
            if (subCri.getResourceClass() == subResourceClassType) {
                return subCri;
            }
        }
        return null;
    }
    
    public static OperationResourceInfo findTargetResourceClass(List<ClassResourceInfo> resources,
                                                                String path, String httpMethod,
                                                                Map<String, String> values) {
        for (ClassResourceInfo resource : resources) {
            URITemplate uriTemplate = resource.getURITemplate();
            if (uriTemplate.match(path, values)) {
                String subResourcePath = values.get(URITemplate.RIGHT_HAND_VALUE);
                OperationResourceInfo ori = findTargetMethod(resource, subResourcePath, httpMethod, values);
                if (ori != null) {
                    return ori;
                }
            }
        }
        return null;
    }

    public static OperationResourceInfo findTargetMethod(ClassResourceInfo resource, String path,
                                                     String httpMethod, Map<String, String> values) {
        for (OperationResourceInfo ori : resource.getMethodDispatcher().getOperationResourceInfos()) {
            URITemplate uriTemplate = ori.getURITemplate();
            if (uriTemplate != null && uriTemplate.match(path, values)) {
                if (ori.isSubResourceLocator()) {
                    return ori;
                } else if (ori.getHttpMethod() != null && ori.getHttpMethod().equalsIgnoreCase(httpMethod)) {
                    return ori;
                }
            } 
        }
        return null;
    }

    public static List<Object> processParameters(Method method, String path, String httpMethod,
                                           Map<String, String> values, InputStream is) {
        Class[] parameterTypes = method.getParameterTypes();
        Type[] genericParameterTypes = method.getGenericParameterTypes();
        Annotation[][] parameterAnnotations = method.getParameterAnnotations();

        boolean readFromEntityBody = false;
        if ("PUT".equals(httpMethod) || "POST".equals(httpMethod)) {
            readFromEntityBody = true;
        }

        List<Object> params = new ArrayList<Object>(parameterTypes.length);

        for (int i = 0; i < parameterTypes.length; i++) {
            Object param = processParameter(parameterTypes[i], genericParameterTypes[i],
                                            parameterAnnotations[i], readFromEntityBody, path, values, is);

            params.add(param);
        }

        return params;
    }

    private static Object processParameter(Class<?> parameterClass, Type parameterType,
                                    Annotation[] parameterAnnotations, boolean readFromEntityBody,
                                    String path, Map<String, String> values, InputStream is) {
        Object result = null;
        if (parameterAnnotations == null || parameterAnnotations.length == 0) {
            if (readFromEntityBody) {
                result = readFromEntityBody(parameterClass, is);
            }
            return result;
        } 

        Annotation annotation = parameterAnnotations[0];
        if (annotation.annotationType() == UriParam.class) {
            result = readFromUriParam((UriParam)annotation, parameterClass, parameterType,
                                      parameterAnnotations, path, values);
        } else if (annotation.annotationType() == QueryParam.class) {
            //TODO
        } else if (annotation.annotationType() == MatrixParam.class) {
            //TODO
        } else if (annotation.annotationType() == HeaderParam.class) {
            //TODO
        } else if (annotation.annotationType() == HttpContext.class) {
            //TODO
        }

        return result;
    }

    @SuppressWarnings("unchecked")
    private static Object readFromEntityBody(Class targetTypeClass, InputStream is) {
        Object result = null;
        EntityProvider provider = ProviderFactory.getInstance().createEntityProvider(targetTypeClass);

        try {
            result = provider.readFrom(targetTypeClass, null, null, is);
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        return result;
    }

    private static Object readFromUriParam(UriParam uriParamAnnotation,
                                    Class<?> parameter,
                                    Type parameterType,
                                    Annotation[] parameterAnnotations,
                                    String path,
                                    Map<String, String> values) {
        String parameterName = uriParamAnnotation.value();
        if (parameterName == null || parameterName.length() == 0) {
            // Invalid URI parameter name
            return null;
        }

        Object result = values.get(parameterName);
        
        if (parameter.isPrimitive()) {
            result = PrimitiveUtils.read((String)result, parameter);
        }
        return result;
    }
}
