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

package org.apache.cxf.tools.java2wsdl.processor.internal.jaxws;

import java.lang.reflect.Method;

import javax.jws.Oneway;

public final class WrapperUtil {
    
    private WrapperUtil() {
    }

    public static boolean isWrapperClassExists(Method method) {
        Wrapper requestWrapper = new RequestWrapper();
        requestWrapper.setMethod(method);
        try {
            requestWrapper.getWrapperClass();
            boolean isOneWay = method.isAnnotationPresent(Oneway.class);
            if (!isOneWay) {
                Wrapper responseWrapper = new ResponseWrapper();
                responseWrapper.setMethod(method);
                responseWrapper.getWrapperClass();
            }
        } catch (Exception e) {
            return false;
        }
        return true;
    }
}
