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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;

import org.apache.cxf.frontend.MethodDispatcher;
import org.apache.cxf.helpers.CastUtils;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.message.Exchange;
import org.apache.cxf.message.FaultMode;
import org.apache.cxf.service.Service;
import org.apache.cxf.service.model.BindingOperationInfo;

/**
 * Abstract implementation of Invoker.
 * <p>
 * 
 * @author Ben Yu Feb 10, 2006 10:57:23 PM
 */
public abstract class AbstractInvoker implements Invoker {

    public Object invoke(Exchange exchange, Object o) {

        final Object serviceObject = getServiceObject(exchange);

        BindingOperationInfo bop = exchange.get(BindingOperationInfo.class);
        MethodDispatcher md = (MethodDispatcher) 
            exchange.get(Service.class).get(MethodDispatcher.class.getName());
        Method m = md.getMethod(bop);
        //Method m = (Method)bop.getOperationInfo().getProperty(Method.class.getName());
        m = matchMethod(m, serviceObject);
        
        List<Object> params = null;
        if (o instanceof List) {
            params = CastUtils.cast((List<?>)o);
        } else {
            if (o != null) {
                params = new ArrayList<Object>();
                params.add(o);
            }
        }
        
        return invoke(exchange, serviceObject, m, params);
    }

    protected Object invoke(Exchange exchange, final Object serviceObject, Method m, List<Object> params) {
        Object res;
        try {
            Object[] paramArray = new Object[]{};
            if (params != null) {
                paramArray = params.toArray();
            }
            
            paramArray = insertExchange(m, paramArray, exchange);
            
            res = m.invoke(serviceObject, paramArray);
            if (exchange.isOneWay()) {
                return null;
            }
            
            List<Object> retList = new ArrayList<Object>();
            if (!((Class)m.getReturnType()).getName().equals("void")) {
                retList.add(res);
            }
            return retList;
        } catch (InvocationTargetException e) {
            Throwable t = e.getCause();
            if (t == null) {
                t = e;
            }
            exchange.getInMessage().put(FaultMode.class, FaultMode.CHECKED_APPLICATION_FAULT);
            throw new Fault(t);
        } catch (Fault f) {
            exchange.getInMessage().put(FaultMode.class, FaultMode.UNCHECKED_APPLICATION_FAULT);
            throw f;
        } catch (Exception e) {
            exchange.getInMessage().put(FaultMode.class, FaultMode.UNCHECKED_APPLICATION_FAULT);
            throw new Fault(e);
        }
    }

    public Object[] insertExchange(Method method, Object[] params, Exchange context) {
        Object[] newParams = params;
        for (int i = 0; i < method.getParameterTypes().length; i++) {
            if (method.getParameterTypes()[i].equals(Exchange.class)) {
                newParams = new Object[params.length + 1];

                for (int j = 0; j < newParams.length; j++) {
                    if (j == i) {
                        newParams[j] = context;
                    } else if (j > i) {
                        newParams[j] = params[j - 1];
                    } else {
                        newParams[j] = params[j];
                    }
                }
            }
        }
        return newParams;
    }
    
    /**
     * Creates and returns a service object depending on the scope.
     */
    public abstract Object getServiceObject(final Exchange context);

    /**
     * Returns a Method that has the same declaring class as the class of
     * targetObject to avoid the IllegalArgumentException when invoking the
     * method on the target object. The methodToMatch will be returned if the
     * targetObject doesn't have a similar method.
     * 
     * @param methodToMatch The method to be used when finding a matching method
     *            in targetObject
     * @param targetObject The object to search in for the method.
     * @return The methodToMatch if no such method exist in the class of
     *         targetObject; otherwise, a method from the class of targetObject
     *         matching the matchToMethod method.
     */
    private static Method matchMethod(Method methodToMatch, Object targetObject) {
        if (isJdkDynamicProxy(targetObject)) {
            Class[] interfaces = targetObject.getClass().getInterfaces();
            for (int i = 0; i < interfaces.length; i++) {
                Method m = getMostSpecificMethod(methodToMatch, interfaces[i]);
                if (!methodToMatch.equals(m)) {
                    return m;
                }
            }
        }
        return methodToMatch;
    }

    /**
     * Return whether the given object is a J2SE dynamic proxy.
     * 
     * @param object the object to check
     * @see java.lang.reflect.Proxy#isProxyClass
     */
    public static boolean isJdkDynamicProxy(Object object) {
        return object != null && Proxy.isProxyClass(object.getClass());
    }

    /**
     * Given a method, which may come from an interface, and a targetClass used
     * in the current AOP invocation, find the most specific method if there is
     * one. E.g. the method may be IFoo.bar() and the target class may be
     * DefaultFoo. In this case, the method may be DefaultFoo.bar(). This
     * enables attributes on that method to be found.
     * 
     * @param method method to be invoked, which may come from an interface
     * @param targetClass target class for the curren invocation. May be
     *            <code>null</code> or may not even implement the method.
     * @return the more specific method, or the original method if the
     *         targetClass doesn't specialize it or implement it or is null
     */
    public static Method getMostSpecificMethod(Method method, Class<?> targetClass) {
        if (method != null && targetClass != null) {
            try {
                method = targetClass.getMethod(method.getName(), method.getParameterTypes());
            } catch (NoSuchMethodException ex) {
                // Perhaps the target class doesn't implement this method:
                // that's fine, just use the original method
            }
        }
        return method;
    }
}
