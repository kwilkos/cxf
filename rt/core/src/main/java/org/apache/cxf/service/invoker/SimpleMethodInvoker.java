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

package org.apache.cxf.service.invoker;


import java.lang.reflect.Method;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.cxf.helpers.CastUtils;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.message.Exchange;
import org.apache.cxf.service.model.BindingOperationInfo;


/**
 * An invoker which invokes a bean method. Will be replaced soon with something better.
 */
public class SimpleMethodInvoker implements Invoker {
    private Object bean;
    
    public SimpleMethodInvoker(Object bean) {
        super();
        this.bean = bean;
    }
    
    public Object invoke(Exchange exchange, Object o) {
        BindingOperationInfo bop = exchange.get(BindingOperationInfo.class);
        
        
        Method m = (Method)bop.getOperationInfo().getProperty(Method.class.getName());
        List<Object> params = CastUtils.cast((List<?>)o);
                
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
            return Arrays.asList(retList.toArray());
        } catch (Exception e) {
            throw new Fault(e);
        }
    }

   

    
}
