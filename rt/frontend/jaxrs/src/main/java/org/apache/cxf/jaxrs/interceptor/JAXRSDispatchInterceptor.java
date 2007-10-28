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

package org.apache.cxf.jaxrs.interceptor;

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.HeaderParam;
import javax.ws.rs.MatrixParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.UriParam;
import javax.ws.rs.core.HttpContext;
import javax.ws.rs.ext.EntityProvider;
import javax.ws.rs.ext.ProviderFactory;

import org.apache.cxf.jaxrs.JAXRSServiceImpl;
import org.apache.cxf.jaxrs.model.ClassResourceInfo;
import org.apache.cxf.jaxrs.model.OperationResourceInfo;
import org.apache.cxf.jaxrs.model.URITemplate;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.Phase;
import org.apache.cxf.service.Service;


public class JAXRSDispatchInterceptor extends AbstractPhaseInterceptor<Message> {

    public static final String RELATIVE_PATH = "relative.path";
    //private static final Logger LOG = Logger.getLogger(RESTDispatchInterceptor.class.getName());
    //private static final ResourceBundle BUNDLE = BundleUtils.getBundle(RESTDispatchInterceptor.class);

    public JAXRSDispatchInterceptor() {
        super(Phase.PRE_STREAM);
    }

    public void handleMessage(Message message) {
        String path = (String)message.get(Message.PATH_INFO);
        String address = (String)message.get(Message.BASE_PATH);
        String httpMethod = (String)message.get(Message.HTTP_REQUEST_METHOD);

        if (address.startsWith("http")) {
            int idx = address.indexOf('/', 7);
            if (idx != -1) {
                address = address.substring(idx);
            }
        }

        if (path.startsWith(address)) {
            path = path.substring(address.length());
            if (!path.startsWith("/")) {
                path = "/" + path;
            }
        }

        if (!path.endsWith("/")) {
            //path = path.substring(0, path.length() - 1);
            path = path + "/";
        }
        message.put(RELATIVE_PATH, path);


        //1. Matching target resource classes and method
        Service service = message.getExchange().get(Service.class);
        List<ClassResourceInfo> resources = ((JAXRSServiceImpl)service).getClassResourceInfos();

        Map<String, String> values = new HashMap<String, String>();
        OperationResourceInfo ori = findTargetResource(resources, path, httpMethod, values);

        if (ori == null) {
            //throw new Fault(new org.apache.cxf.common.i18n.Message("NO_OP", BUNDLE, method, path));
        }
        message.getExchange().put(OperationResourceInfo.class, ori);


        //2. Process parameters
        InputStream is = message.getContent(InputStream.class);
        List<Object> params = processParameters(ori.getMethod(), path, httpMethod, values, is);

        message.setContent(List.class, params);

    }    

    protected OperationResourceInfo findTargetResource(List<ClassResourceInfo> resources, String path,
                                                       String httpMethod, Map<String, String> values) {
        for (ClassResourceInfo resource : resources) {
            URITemplate uriTemplate = resource.getURITemplate();
            if (uriTemplate.match(path, values)) {
                String subResourcePath = values.values().iterator().next();
                OperationResourceInfo ori = findTargetMethod(resource, subResourcePath, httpMethod, values);
                if (ori != null) {
                    return ori;
                }
            }
        }
        return null;
    }

    protected OperationResourceInfo findTargetMethod(ClassResourceInfo resource, String path,
                                                     String httpMethod, Map<String, String> values) {
        for (OperationResourceInfo ori : resource.getMethodDispatcher().getOperationResourceInfos()) {
            URITemplate uriTemplate = ori.getURITemplate();
            if (uriTemplate != null && uriTemplate.match(path, values)
                && ori.getHttpMethod().equalsIgnoreCase(httpMethod)) {
                return ori;
            } /*
                 * else { //URITemplate == null means match by default if
                 * (httpMethod.equalsIgnoreCase(ori.getHttpMethod())) { return
                 * ori; } }
                 */
        }
        return null;
    }

    private List<Object> processParameters(Method method, String path, String httpMethod,
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

    private Object processParameter(Class<?> parameterClass, Type parameterType,
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
    private Object readFromEntityBody(Class targetTypeClass, InputStream is) {
        Object result = null;
        EntityProvider provider = ProviderFactory.getInstance().createEntityProvider(targetTypeClass);

        try {
            result = provider.readFrom(targetTypeClass, null, null, is);
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        return result;
    }

    private String readFromUriParam(UriParam uriParamAnnotation,
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

        String result = values.get(parameterName);

        return result;
    }
}
