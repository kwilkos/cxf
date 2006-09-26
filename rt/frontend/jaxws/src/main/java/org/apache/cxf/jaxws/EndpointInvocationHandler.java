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
import java.util.concurrent.FutureTask;
import java.util.logging.Logger;

import javax.xml.ws.AsyncHandler;
import javax.xml.ws.Binding;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Holder;
import javax.xml.ws.Response;
import javax.xml.ws.WebServiceException;

import org.apache.cxf.common.i18n.Message;
import org.apache.cxf.common.logging.LogUtils;
import org.apache.cxf.endpoint.Client;
import org.apache.cxf.endpoint.Endpoint;
import org.apache.cxf.service.factory.MethodDispatcher;
import org.apache.cxf.service.model.BindingOperationInfo;

public final class EndpointInvocationHandler extends BindingProviderImpl implements InvocationHandler {

    private static final Logger LOG = LogUtils.getL7dLogger(EndpointInvocationHandler.class);

    private Endpoint endpoint;

    private Client client;

    EndpointInvocationHandler(Client c, Binding b) {
        super(b);
        endpoint = c.getEndpoint();
        client = c;
    }

    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        MethodDispatcher dispatcher = 
            (MethodDispatcher)endpoint.getService().get(MethodDispatcher.class.getName());
        BindingOperationInfo oi = dispatcher.getBindingOperation(method, endpoint);
        if (oi == null) {
            // check for method on BindingProvider
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
        Map<String, Object> requestContext = this.getRequestContext();
        Map<String, Object> responseContext = this.getResponseContext();
        Map<String, Object> context = new HashMap<String, Object>();
        context.put(Client.REQUEST_CONTEXT, requestContext);
        context.put(Client.RESPONSE_CONTEXT, responseContext);

        requestContext.put(Method.class.getName(), method);

        boolean isAsync = method.getName().endsWith("Async");
        if (isAsync) {
            return invokeAsync(method, oi, params, paramsWithOutHolder, context);
        } else {
            return invokeSync(method, oi, params, paramsWithOutHolder, context);
        }
    }


    Object invokeSync(Method method, 
                          BindingOperationInfo oi, 
                          Object[] params, 
                          Object[] paramsWithOutHolder, 
                          Map<String, Object> context) {
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
    
    Client getClient() {
        return client;
    }

    private Object invokeAsync(Method method, 
                               BindingOperationInfo oi, 
                               Object[] params, 
                               Object[] paramsWithOutHolder, 
                               Map<String, Object> context) {
        
        FutureTask<Object> f = new FutureTask<Object>(new JAXWSAsyncCallable(this, 
                                                                             method,
                                                                             oi,
                                                                             params,
                                                                             paramsWithOutHolder,
                                                                             context
                                                                             ));

        endpoint.getService().getExecutor().execute(f);

        Response<?> r = new AsyncResponse<Object>(f, Object.class);
        if (params.length > 0 && params[params.length - 1] instanceof AsyncHandler) {
            // callback style
            AsyncCallbackFuture callback = 
                new AsyncCallbackFuture(r, (AsyncHandler)params[params.length - 1]);
            endpoint.getService().getExecutor().execute(callback);
            return callback;
        } else {
            return r;
        }
    }

    private Object[] handleHolder(Object[] params) {
        // get value out of Holder
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
                ((Holder)params[holderStartIndex]).getClass().getField("value").set(params[holderStartIndex],
                                                                                    rawRet[i]);
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
}
