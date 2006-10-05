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
package org.apache.cxf.jaxws.support;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.cxf.endpoint.Endpoint;
import org.apache.cxf.interceptor.WrappedInInterceptor;
import org.apache.cxf.service.factory.MethodDispatcher;
import org.apache.cxf.service.model.BindingInfo;
import org.apache.cxf.service.model.BindingOperationInfo;
import org.apache.cxf.service.model.OperationInfo;

public class JaxWsMethodDispatcher implements MethodDispatcher {

    private Map<Method, Map<BindingInfo, BindingOperationInfo>> infoMap = 
        new ConcurrentHashMap<Method, Map<BindingInfo, BindingOperationInfo>>();
    private Map<BindingOperationInfo, Method> bopToMethod = 
        new ConcurrentHashMap<BindingOperationInfo, Method>();

    public void bind(OperationInfo o, Method... methods) {
        Method primary = methods[0];
        for (BindingInfo b : o.getInterface().getService().getBindings()) {
            Map<BindingInfo, BindingOperationInfo> biToBop = new HashMap<BindingInfo, BindingOperationInfo>();

            for (Method m : methods) {
                infoMap.put(m, biToBop);
            }

            for (BindingOperationInfo bop : b.getOperations()) {
                if (bop.getOperationInfo().equals(o)) {
                    biToBop.put(b, bop);
                    bopToMethod.put(bop, primary);

                    BindingOperationInfo unwrappedOp = bop.getUnwrappedOperation();
                    if (unwrappedOp != null
                        && unwrappedOp.getOperationInfo().equals(o.getUnwrappedOperation())
                        && unwrappedOp.getOperationInfo().getInput()
                            .getProperty(WrappedInInterceptor.WRAPPER_CLASS) != null) {
                        biToBop.put(b, unwrappedOp);
                        bopToMethod.put(unwrappedOp, primary);
                    }
                }
            }
        }
    }

    public BindingOperationInfo getBindingOperation(Method method, Endpoint endpoint) {
        Map<BindingInfo, BindingOperationInfo> bops = infoMap.get(method);
        if (bops == null) {
            return null;
        }
        return bops.get(endpoint.getEndpointInfo().getBinding());
    }

    public Method getMethod(BindingOperationInfo op) {
        return bopToMethod.get(op);
    }
}
