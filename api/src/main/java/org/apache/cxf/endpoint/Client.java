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

package org.apache.cxf.endpoint;

import java.util.Map;

import org.apache.cxf.interceptor.InterceptorProvider;
import org.apache.cxf.service.model.BindingOperationInfo;

public interface Client extends InterceptorProvider {
    
    /**
     * Invokes an operation syncronously
     * @param oi  The operation to be invoked
     * @param params  The params that matches the parts of the input message of the operation
     * @param requestContext  Optional (can be null) request contextual information for the invocation
     * @param responseContext Optional (can be null) response contextual information for the invocation
     * @return The return values that matche the parts of the output message of the operation
     */
    Object[] invoke(BindingOperationInfo oi,
                    Object[] params,
                    Map<String, Object> requestContext,
                    Map<String, Object> responseContext);

    Endpoint getEndpoint();
   
}
