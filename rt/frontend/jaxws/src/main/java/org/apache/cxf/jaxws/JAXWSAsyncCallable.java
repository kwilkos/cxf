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
import java.util.Map;
import java.util.concurrent.Callable;

import org.apache.cxf.service.model.BindingOperationInfo;

public class JAXWSAsyncCallable implements Callable<Object> {

    private EndpointInvocationHandler endPointInvocationHandler;
    private Method method;
    private BindingOperationInfo oi; 
    private Object[] params;
    private Object[] paramsWithOutHolder; 
    private Map<String, Object> requestContext;
    private Map<String, Object> responseContext;
    
    public JAXWSAsyncCallable(EndpointInvocationHandler endPointInvocationHandler,
                              Method method,
                              BindingOperationInfo oi,
                              Object[] params,
                              Object[] paramsWithOutHolder,
                              Map<String, Object> reqCxt,
                              Map<String, Object> respCxt) {
        this.endPointInvocationHandler = endPointInvocationHandler;
        this.method = method;
        this.oi = oi;
        this.params = params;
        this.paramsWithOutHolder = paramsWithOutHolder;
        this.requestContext = reqCxt;
        this.responseContext = respCxt;
    }
    
    public Object call() throws Exception {
        return endPointInvocationHandler.invokeSync(method, 
                                                oi, 
                                                params, 
                                                paramsWithOutHolder,
                                                requestContext,
                                                responseContext);
    }

}
