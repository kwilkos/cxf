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

package org.apache.cxf.jaxws;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import javax.jws.WebMethod;
import javax.xml.namespace.QName;
import javax.xml.ws.Binding;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Holder;
import javax.xml.ws.RequestWrapper;
import javax.xml.ws.ResponseWrapper;
import javax.xml.ws.WebServiceException;

import org.apache.cxf.common.classloader.ClassLoaderUtils;
import org.apache.cxf.common.i18n.Message;
import org.apache.cxf.common.logging.LogUtils;
import org.apache.cxf.endpoint.Client;
import org.apache.cxf.endpoint.Endpoint;
import org.apache.cxf.interceptor.WrappedInInterceptor;
import org.apache.cxf.jaxws.interceptors.WrapperClassOutInterceptor;
import org.apache.cxf.service.model.BindingOperationInfo;
import org.apache.cxf.service.model.InterfaceInfo;
import org.apache.cxf.service.model.OperationInfo;

public final class EndpointInvocationHandler extends BindingProviderImpl implements InvocationHandler {

    private static final Logger LOG = LogUtils.getL7dLogger(EndpointInvocationHandler.class);

    // private static final ResourceBundle BUNDLE = LOG.getResourceBundle();

    private Endpoint endpoint;

    private Client client;

    private Map<Method, BindingOperationInfo> infoMap = new ConcurrentHashMap<Method, BindingOperationInfo>();

    EndpointInvocationHandler(Client c, Binding b) {
        super(b);
        endpoint = c.getEndpoint();
        client = c;
    }

    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

        BindingOperationInfo oi = getOperationInfo(proxy, method);
        if (oi == null) {
            //check for method on BindingProvider
            if (method.getDeclaringClass().equals(BindingProvider.class)
                || method.getDeclaringClass().equals(BindingProviderImpl.class)) {
                return method.invoke(this);
            }
            
            Message msg = new Message("NO_OPERATION_INFO", LOG, method.getName());
            throw new WebServiceException(msg.toString());
        }

        

        Object[] params = args;
        if (null == params) {
            params = new Object[0];
        }
        

        Object[] paramsWithOutHolder = handleHolder(params);
        Map<String, Object> context = new HashMap<String, Object>();
        context.put(Method.class.getName(), method);
        Object rawRet[] = client.invoke(oi, paramsWithOutHolder, context);

        if (rawRet != null && rawRet.length != 0) {
            List<Object> retList = new ArrayList<Object>();
            handleHolderReturn(params, method, rawRet, retList);
            Object[] obj = retList.toArray();
            return obj == null || obj.length == 0 ? null : obj[0];
        } else {
            return null;
        }
    }

    private Object[] handleHolder(Object[] params) {
        //get value out of Holder
        Object[] ret = new Object[params.length];
        for (int i = 0; i < params.length; i++) {
            if (params[i] instanceof Holder) {
                ret[i] = ((Holder)params[i]).value;
            } else {
                ret[i] = params[i];
            }
        }
        return ret;
    }

    BindingOperationInfo getOperationInfo(Object proxy, Method method) {
        // TODO: We can't really just associate a method with the operationInfo
        // by its name. The operation name in the wsdl might be something
        // different.
        // For instance, if we have two methods named Foo, there might bee Foo1
        // and Foo2 since the WS-I BP disallows operations with the same name.

        BindingOperationInfo boi = infoMap.get(method);

        if (null == boi) {
            WebMethod wma = method.getAnnotation(WebMethod.class);
            String operationName = null;
            if (null != wma && !"".equals(wma.operationName())) {
                operationName = wma.operationName();
            } else {
                operationName = method.getName();
            }

            InterfaceInfo ii = endpoint.getService().getServiceInfo().getInterface();
            QName oiQName = new QName(endpoint.getService().getName().getNamespaceURI(), operationName);
            OperationInfo oi = ii.getOperation(oiQName);
            if (null == oi) {
                return null;
            }
            // found the OI in the Interface, now find it in the binding
            BindingOperationInfo boi2 = endpoint.getEndpointInfo().getBinding().getOperation(oiQName);
            if (boi2.getOperationInfo() == oi) {
                if (boi2.isUnwrappedCapable()) {
                    try {
                        Class requestWrapper = getRequestWrapper(method);
                        Class responseWrapper = getResponseWrapper(method);

                        if (requestWrapper != null || responseWrapper != null) {
                            BindingOperationInfo boi3 = boi2.getUnwrappedOperation();
                            oi = boi3.getOperationInfo();
                            oi.setProperty(WrapperClassOutInterceptor.SINGLE_WRAPPED_PART, requestWrapper);
                            boi2.getOperationInfo().setProperty(WrappedInInterceptor.SINGLE_WRAPPED_PART,
                                            Boolean.TRUE);
                            infoMap.put(method, boi3);
                            return boi3;
                        }
                    } catch (ClassNotFoundException cnfe) {
                        cnfe.printStackTrace();
                        // TODO - exception
                    }
                }
                infoMap.put(method, boi2);
                return boi2;
            }
        }
        return boi;
    }
    
    
    private void handleHolderReturn(Object[] params, Method method, Object[] rawRet, List<Object> retList) {
                
        int idx = 0;
        
        if (method == null) {
            return;
        }
        if (!((Class)method.getReturnType()).getName().equals("void")) {
            retList.add(rawRet[0]);
            idx++;
        }
        int holderStartIndex = 0;
        Type[] para = method.getGenericParameterTypes();
        for (int i = 0; i < para.length; i++) {
            if (para[i] instanceof ParameterizedType) {
                ParameterizedType paramType = (ParameterizedType)para[i];
                if (((Class)paramType.getRawType()).getName().equals("javax.xml.ws.Holder")) {
                    break;
                } else {
                    holderStartIndex++;
                }
            } else {
                holderStartIndex++;
            }
        }
                    
        for (int i = idx; i < rawRet.length; i++, holderStartIndex++) {
            try {
                ((Holder)params[holderStartIndex]).getClass().getField(
                    "value").set(params[holderStartIndex], rawRet[i]);
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (SecurityException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            }
            
        }
    }


    protected Class getResponseWrapper(Method selected) throws ClassNotFoundException {
        ResponseWrapper rw = selected.getAnnotation(ResponseWrapper.class);
        if (rw == null) {
            return null;
        }
        String cn = rw.className();
        return ClassLoaderUtils.loadClass(cn, selected.getDeclaringClass());
    }

    protected Class getRequestWrapper(Method selected) throws ClassNotFoundException {
        RequestWrapper rw = selected.getAnnotation(RequestWrapper.class);
        if (rw == null) {
            return null;
        }
        String cn = rw.className();
        return ClassLoaderUtils.loadClass(cn, selected.getDeclaringClass());
    }

}
