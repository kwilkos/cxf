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

import javax.activation.MimeType;
import javax.activation.MimeTypeParseException;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.MatrixParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.UriParam;
import javax.ws.rs.core.HttpContext;
import javax.ws.rs.ext.EntityProvider;
import javax.ws.rs.ext.ProviderFactory;

import org.apache.cxf.common.util.PrimitiveUtils;
import org.apache.cxf.jaxrs.interceptor.JAXRSInInterceptor;
import org.apache.cxf.jaxrs.model.ClassResourceInfo;
import org.apache.cxf.jaxrs.model.OperationResourceInfo;
import org.apache.cxf.jaxrs.model.URITemplate;
import org.apache.cxf.jaxrs.provider.ProviderFactoryImpl;
import org.apache.cxf.message.Message;

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

    //Message contains following information: PATH, HTTP_REQUEST_METHOD, CONTENT_TYPE, InputStream.
    public static List<Object> processParameters(Method method, Map<String, String> values, Message message) {
        Class[] parameterTypes = method.getParameterTypes();
        Type[] genericParameterTypes = method.getGenericParameterTypes();
        Annotation[][] parameterAnnotations = method.getParameterAnnotations();

        List<Object> params = new ArrayList<Object>(parameterTypes.length);

        for (int i = 0; i < parameterTypes.length; i++) {
            Object param = processParameter(parameterTypes[i], genericParameterTypes[i],
                                            parameterAnnotations[i], values, message);

            params.add(param);
        }

        return params;
    }

    private static Object processParameter(Class<?> parameterClass, Type parameterType,
                                           Annotation[] parameterAnnotations, Map<String, String> values,
                                           Message message) {
        InputStream is = message.getContent(InputStream.class);
        String contentTypes = (String)message.get(Message.CONTENT_TYPE);
        if (contentTypes != null) {
            try {
                MimeType mt = new MimeType(contentTypes);
                contentTypes = mt.getBaseType();
            } catch (MimeTypeParseException e) {
                // ignore
            }
        }
        String path = (String)message.get(JAXRSInInterceptor.RELATIVE_PATH);
        String httpMethod = (String)message.get(Message.HTTP_REQUEST_METHOD);
        
        Object result = null;
        
        if ((parameterAnnotations == null || parameterAnnotations.length == 0)
            && ("PUT".equals(httpMethod) || "POST".equals(httpMethod))) {
            result = readFromEntityBody(parameterClass, is, contentTypes);
        } else if (parameterAnnotations[0].annotationType() == UriParam.class) {
            result = readFromUriParam((UriParam)parameterAnnotations[0], parameterClass, parameterType,
                                      parameterAnnotations, path, values);
        } else if (parameterAnnotations[0].annotationType() == QueryParam.class) {
            //TODO
        } else if (parameterAnnotations[0].annotationType() == MatrixParam.class) {
            //TODO
        } else if (parameterAnnotations[0].annotationType() == HeaderParam.class) {
            //TODO
        } else if (parameterAnnotations[0].annotationType() == HttpContext.class) {
            //TODO
        }

        return result;
    }

    @SuppressWarnings("unchecked")
    private static Object readFromEntityBody(Class targetTypeClass, InputStream is, String contentTypes) {
        Object result = null;
        //Refactor once we move to JSR-311 0.5 API
        EntityProvider provider = ((ProviderFactoryImpl)ProviderFactory.getInstance())
            .createEntityProvider(targetTypeClass, new String[]{contentTypes}, true);

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
