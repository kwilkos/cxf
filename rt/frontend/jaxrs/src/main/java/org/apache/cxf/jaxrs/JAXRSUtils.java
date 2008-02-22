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
import java.io.UnsupportedEncodingException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.ConsumeMime;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.MatrixParam;
import javax.ws.rs.ProduceMime;
import javax.ws.rs.QueryParam;
import javax.ws.rs.UriParam;
import javax.ws.rs.core.HttpContext;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.ProviderFactory;

import org.apache.cxf.common.util.PrimitiveUtils;
import org.apache.cxf.common.util.StringUtils;
import org.apache.cxf.jaxrs.interceptor.JAXRSInInterceptor;
import org.apache.cxf.jaxrs.model.ClassResourceInfo;
import org.apache.cxf.jaxrs.model.OperationResourceInfo;
import org.apache.cxf.jaxrs.model.URITemplate;
import org.apache.cxf.message.Message;

public final class JAXRSUtils {

    public static final MediaType ALL_TYPES = new MediaType();

    private JAXRSUtils() {        
    }
    
    public static String getHttpMethodValue(Method m) {
        for (Annotation a : m.getAnnotations()) {
            HttpMethod httpM = a.annotationType().getAnnotation(HttpMethod.class);
            if (httpM != null) {
                return httpM.value();
            }
        }
        return null;
    }

    public static List<MediaType> getMediaTypes(String[] values) {
        List<MediaType> supportedMimeTypes = new ArrayList<MediaType>(values.length);
        for (int i = 0; i < values.length; i++) {
            supportedMimeTypes.add(MediaType.parse(values[i]));    
        }
        return supportedMimeTypes;
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
                                                                String path, 
                                                                String httpMethod,
                                                                Map<String, String> values,
                                                                String requestContentType, 
                                                                String acceptContentTypes) {
        
        for (ClassResourceInfo resource : resources) {
            URITemplate uriTemplate = resource.getURITemplate();
            if (uriTemplate.match(path, values)) {
                String subResourcePath = values.get(URITemplate.RIGHT_HAND_VALUE);
                OperationResourceInfo ori = findTargetMethod(resource, subResourcePath, httpMethod, values,
                                                             requestContentType, acceptContentTypes);
                if (ori != null) {
                    return ori;
                }
            }
        }
        return null;
    }

    public static OperationResourceInfo findTargetMethod(ClassResourceInfo resource, 
                                                         String path,
                                                         String httpMethod, 
                                                         Map<String, String> values, 
                                                         String requestContentType, 
                                                         String acceptContentTypes) {
        List<OperationResourceInfo> candidateList = new ArrayList<OperationResourceInfo>();
        MediaType requestType = requestContentType == null 
                                ? ALL_TYPES : MediaType.parse(requestContentType);
        List<MediaType> acceptTypes = JAXRSUtils.sortMediaTypes(acceptContentTypes);
        for (MediaType acceptType : acceptTypes) {
            for (OperationResourceInfo ori : resource.getMethodDispatcher().getOperationResourceInfos()) {
                URITemplate uriTemplate = ori.getURITemplate();
                if ((uriTemplate != null && uriTemplate.match(path, values))
                    && (ori.isSubResourceLocator() || (ori.getHttpMethod() != null && ori.getHttpMethod()
                        .equalsIgnoreCase(httpMethod)))
                        && matchMimeTypes(requestType, acceptType, ori)) {
                    candidateList.add(ori);
                }
            }
            if (!candidateList.isEmpty()) {
                Collections.sort(candidateList, new OperationResourceInfoComparator());
                return candidateList.get(0);
            }
        }

        return null;
    }    

    public static List<MediaType> getConsumeTypes(ConsumeMime cm) {
        return cm == null ? Collections.singletonList(ALL_TYPES)
                          : getMediaTypes(cm.value());
    }
    
    public static List<MediaType> getProduceTypes(ProduceMime pm) {
        return pm == null ? Collections.singletonList(ALL_TYPES)
                          : getMediaTypes(pm.value());
    }
    
    private static class OperationResourceInfoComparator implements Comparator<OperationResourceInfo> {
        public int compare(OperationResourceInfo e1, OperationResourceInfo e2) {
            
            List<MediaType> mimeType1 = 
                getConsumeTypes(e1.getMethod().getAnnotation(ConsumeMime.class));
            List<MediaType> mimeType2 = 
                getConsumeTypes(e2.getMethod().getAnnotation(ConsumeMime.class));
            
            // TODO: we actually need to check all consume and produce types here ?
            int result = JAXRSUtils.compareMediaTypes(mimeType1.get(0), 
                                                      mimeType2.get(0));
            if (result == 0) {
                //use the media type of output data as the secondary key.
                List<MediaType> mimeTypeP1 = 
                    getProduceTypes(e1.getMethod().getAnnotation(ProduceMime.class));

                List<MediaType> mimeTypeP2 = 
                    getProduceTypes(e2.getMethod().getAnnotation(ProduceMime.class));    

                return JAXRSUtils.compareMediaTypes(mimeTypeP1.get(0), 
                                                    mimeTypeP2.get(0));
            } else {
                return result;
            }

        }
        
    }
    
    public static int compareMediaTypes(MediaType mt1, MediaType mt2) {
        
        if (mt1.equals(mt2)) {
            float q1 = getMediaTypeQualityFactor(mt1);
            float q2 = getMediaTypeQualityFactor(mt2);
            int result = Float.compare(q1, q2);
            return result == 0 ? result : ~result;
        }
        
        if (mt1.isWildcardType() && !mt2.isWildcardType()) {
            return 1;
        }
        if (!mt1.isWildcardType() && mt2.isWildcardType()) {
            return -1;
        }
         
        if (mt1.getType().equals(mt2.getType())) {
            if (mt1.isWildcardSubtype() && !mt2.isWildcardSubtype()) {
                return 1;
            }
            if (!mt1.isWildcardSubtype() && mt2.isWildcardSubtype()) {
                return -1;
            }       
        }
        return mt1.toString().compareTo(mt2.toString());
        
    }

    private static float getMediaTypeQualityFactor(MediaType mt) {
        String q = mt.getParameters().get("q");
        if (q == null) {
            return 1;
        }
        if (q.charAt(0) == '.') {
            q = '0' + q;
        }
        try {
            return Float.parseFloat(q);
        } catch (NumberFormatException ex) {
            // default value will do
        }
        return 1;
    }
    
    //Message contains following information: PATH, HTTP_REQUEST_METHOD, CONTENT_TYPE, InputStream.
    public static List<Object> processParameters(OperationResourceInfo ori, 
                                                 Map<String, String> values, 
                                                 Message message) {
        
        
        Method method = ori.getMethod();
        Class[] parameterTypes = method.getParameterTypes();
        Type[] genericParameterTypes = method.getGenericParameterTypes();
        Annotation[][] parameterAnnotations = method.getParameterAnnotations();

        List<Object> params = new ArrayList<Object>(parameterTypes.length);

        for (int i = 0; i < parameterTypes.length; i++) {
            Object param = processParameter(parameterTypes[i], 
                                            genericParameterTypes[i],
                                            parameterAnnotations[i], 
                                            values, 
                                            message,
                                            ori);
            params.add(param);
        }

        return params;
    }

    private static Object processParameter(Class<?> parameterClass, 
                                           Type parameterType,
                                           Annotation[] parameterAnnotations, 
                                           Map<String, String> values,
                                           Message message,
                                           OperationResourceInfo ori) {
        InputStream is = message.getContent(InputStream.class);
        String contentType = (String)message.get(Message.CONTENT_TYPE);
        
        String path = (String)message.get(JAXRSInInterceptor.RELATIVE_PATH);
        String httpMethod = (String)message.get(Message.HTTP_REQUEST_METHOD);

        Object result = null;

        if ((parameterAnnotations == null || parameterAnnotations.length == 0)
            && ("PUT".equals(httpMethod) || "POST".equals(httpMethod))) {
            result = readFromMessageBody(parameterClass, 
                                         is, 
                                         MediaType.parse(contentType),
                                         ori.getConsumeTypes());
        } else if (parameterAnnotations[0].annotationType() == UriParam.class) {
            result = readFromUriParam((UriParam)parameterAnnotations[0], parameterClass, parameterType,
                                      parameterAnnotations, path, values);
        } else if (parameterAnnotations[0].annotationType() == QueryParam.class) {
            result = readQueryString((QueryParam)parameterAnnotations[0], parameterClass, message);
        } else if (parameterAnnotations[0].annotationType() == MatrixParam.class) {
            //TODO
        } else if (parameterAnnotations[0].annotationType() == HeaderParam.class) {
            //TODO
        } else if (parameterAnnotations[0].annotationType() == HttpContext.class) {
            //TODO
        }

        return result;
    }

    private static Object readQueryString(QueryParam queryParam, Class<?> parameter, Message message) {
        String queryName = queryParam.value();

        Object result = getQueries(message).get(queryName);

        if (parameter.isPrimitive()) {
            result = PrimitiveUtils.read((String)result, parameter);
        }
        return result;  
    }

    /**
     * Retrieve map of query parameters from the passed in message
     * @param message
     * @return a Map of query parameters.
     */
    protected  static Map<String, String> getQueries(Message message) {
        Map<String, String> queries = new LinkedHashMap<String, String>();
        String query = (String)message.get(Message.QUERY_STRING);
        if (!StringUtils.isEmpty(query)) {            
            List<String> parts = Arrays.asList(query.split("&"));
            for (String part : parts) {
                String[] keyValue = part.split("=");
                queries.put(keyValue[0], uriDecode(keyValue[1]));
            }
        }
        return queries;
    }

    private static String uriDecode(String query) {
        try {
            query = URLDecoder.decode(query, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            //Swallow unsupported decoding exception          
        }
        return query;
    }

    @SuppressWarnings("unchecked")
    private static <T> Object readFromMessageBody(Class<T> targetTypeClass, InputStream is, 
                                                  MediaType contentType, List<MediaType> consumeTypes) {
        
        List<MediaType> types = JAXRSUtils.intersectMimeTypes(consumeTypes, contentType);
        
        MessageBodyReader provider = null;
        
        for (MediaType type : types) { 
            provider = ProviderFactory.getInstance()
                .createMessageBodyReader(targetTypeClass, type);
            if (provider != null) {
                try {
                    return provider.readFrom(targetTypeClass, contentType, null, is);
                } catch (IOException e) {
                    e.printStackTrace();
                    return null;
                }    
            }
        }

        return null;
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

    public static boolean matchMimeTypes(MediaType requestContentType, 
                                         MediaType acceptContentType, 
                                         OperationResourceInfo ori) {
        
        if (intersectMimeTypes(ori.getConsumeTypes(), requestContentType).size() != 0
            && intersectMimeTypes(ori.getProduceTypes(), acceptContentType).size() != 0) {
            return true;
        }
        return false;
    }

    public static List<MediaType> parseMediaTypes(String types) {
        List<MediaType> acceptValues = new ArrayList<MediaType>();
        
        if (types != null) {
            while (types.length() > 0) {
                String tp = types;
                int index = types.indexOf(',');
                if (index != -1) {
                    tp = types.substring(0, index);
                    types = types.substring(index + 1).trim();
                } else {
                    types = "";
                }
                acceptValues.add(MediaType.parse(tp));
            }
        } else {
            acceptValues.add(ALL_TYPES);
        }
        
        return acceptValues;
    }
    
    /**
     * intersect two mime types
     * 
     * @param mimeTypesA 
     * @param mimeTypesB 
     * @return return a list of intersected mime types
     */   
    public static List<MediaType> intersectMimeTypes(List<MediaType> mimeTypesA, 
                                                     List<MediaType> mimeTypesB) {
        Set<MediaType> supportedMimeTypeList = new LinkedHashSet<MediaType>();

        for (MediaType mimeTypeA : mimeTypesA) {
            for (MediaType mimeTypeB : mimeTypesB) {
                if (mimeTypeB.isCompatible(mimeTypeA) || mimeTypeA.isCompatible(mimeTypeB)) {
                    
                    String type = mimeTypeA.getType().equals(MediaType.MEDIA_TYPE_WILDCARD) 
                                      ? mimeTypeB.getType() : mimeTypeA.getType();
                    String subtype = mimeTypeA.getSubtype().equals(MediaType.MEDIA_TYPE_WILDCARD) 
                                      ? mimeTypeB.getSubtype() : mimeTypeA.getSubtype();                  
                    supportedMimeTypeList.add(new MediaType(type, subtype));
                }
            }
        }

        return new ArrayList<MediaType>(supportedMimeTypeList);
        
    }
    
    public static List<MediaType> intersectMimeTypes(List<MediaType> mimeTypesA, 
                                                     MediaType mimeTypeB) {
        return intersectMimeTypes(mimeTypesA, 
                                  Collections.singletonList(mimeTypeB));
    }
    
    public static List<MediaType> intersectMimeTypes(String mimeTypesA, 
                                                     String mimeTypesB) {
        return intersectMimeTypes(parseMediaTypes(mimeTypesA),
                                  parseMediaTypes(mimeTypesB));
    }
    
    public static List<MediaType> sortMediaTypes(String mediaTypes) {
        List<MediaType> types = JAXRSUtils.parseMediaTypes(mediaTypes);
        if (types.size() > 1) {
            Collections.sort(types, new Comparator<MediaType>() {

                public int compare(MediaType mt1, MediaType mt2) {
                    return JAXRSUtils.compareMediaTypes(mt1, mt2);
                }
                
            });
        }
        return types;
    }
}
