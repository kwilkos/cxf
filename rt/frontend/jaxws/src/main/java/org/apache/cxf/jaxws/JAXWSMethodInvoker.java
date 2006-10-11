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

import java.lang.reflect.Array;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;

import javax.xml.ws.Holder;

import org.apache.cxf.message.Exchange;
import org.apache.cxf.service.invoker.BeanInvoker;

public class JAXWSMethodInvoker extends BeanInvoker {

    public JAXWSMethodInvoker(Object bean) {
        super(bean);
    }

    @SuppressWarnings("unchecked")
    protected Object invoke(Exchange exchange, final Object serviceObject, Method m, List<Object> params) {
        checkHolder(m, params, exchange);

        List<Object> res = (List<Object>) super.invoke(exchange, serviceObject, m, params);
        for (Object o : params) {
            if (o instanceof Holder) {
                res.add(((Holder) o).value);
            }
        }
        return res;
    }

    @SuppressWarnings("unchecked")
    private void checkHolder(Method method, List<Object> params, Exchange exchange) {
        if (method != null) {

            Type[] para = method.getGenericParameterTypes();
            for (int i = 0; i < para.length; i++) {
                if (para[i] instanceof ParameterizedType) {
                    ParameterizedType paramType = (ParameterizedType)para[i];
                    if (((Class)paramType.getRawType()).getName().equals("javax.xml.ws.Holder")) {

                        Object rawType = paramType.getActualTypeArguments()[0];
                        Class rawClass = null;
                        if (rawType instanceof GenericArrayType) {
                            rawClass = (Class)((GenericArrayType)rawType).getGenericComponentType();
                            rawClass = Array.newInstance(rawClass, 0).getClass();
                        } else if (rawType instanceof Class) {
                            rawClass = (Class)rawType;
                        } else if (rawType instanceof ParameterizedType) {
                            rawClass = (Class)((ParameterizedType)rawType).getRawType();
                        }
                        // param = new Holder((Class) rawClass);

                        if (i >= params.size()) {
                            params.add(new Holder());
                        } else {
                            params.set(i, new Holder(params.get(i)));
                        }
                    }
                }
            }
        }
    }
}
