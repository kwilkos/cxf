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

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.xml.ws.Holder;

import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.message.Exchange;
import org.apache.cxf.service.invoker.Invoker;
import org.apache.cxf.service.model.BindingOperationInfo;

public class JAXWSMethodInvoker implements Invoker {

    private Object bean;
    
    public JAXWSMethodInvoker(Object bean) {
        super();
        this.bean = bean;
    }
    
    @SuppressWarnings("unchecked")
    public Object invoke(Exchange exchange, Object o) {
        BindingOperationInfo bop = exchange.get(BindingOperationInfo.class);
        
        
        Method m = (Method)bop.getOperationInfo().getProperty(Method.class.getName());
        List<Object> params = (List<Object>) o;
                
        checkHolder(m, params, exchange);
        Object res;
        try {
            Object[] paramArray = params.toArray();
            res = m.invoke(bean, paramArray);
            if (exchange.isOneWay()) {
                return null;
            }
            List<Object> retList = new ArrayList<Object>();
            if (!((Class)m.getReturnType()).getName().equals("void")) {
                retList.add(res);
            }
            for (int i = 0; i < paramArray.length; i++) {
                if (paramArray[i] instanceof Holder) {
                    retList.add(((Holder)paramArray[i]).value);
                }
            }
            return Arrays.asList(retList.toArray());
        } catch (Exception e) {
            throw new Fault(e);
        }
    }

    @SuppressWarnings("unchecked")
    private void checkHolder(Method method, List<Object> params, Exchange exchange) {
        if (method != null) {
           
            Type[] para = method.getGenericParameterTypes();
            for (int i = 0; i < para.length; i++) {
                if (para[i] instanceof ParameterizedType) {
                    Object param = null;
                    ParameterizedType paramType = (ParameterizedType)para[i];
                    if (((Class)paramType.getRawType()).getName().equals("javax.xml.ws.Holder")) {
                        
                        try {
                            param = new Holder(
                                ((Class)paramType.getActualTypeArguments()[0]).newInstance());
                        } catch (InstantiationException e) {
                            throw new Fault(e);
                        } catch (IllegalAccessException e) {
                            throw new Fault(e);
                        }
                        if (i >= params.size()) {
                            params.add(param);
                        } else {
                            params.set(i, new Holder(params.get(i)));
                        }
                        
                    }
                                       
                    
                }
            }
            
        }
        
    }

    

}
