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
import javax.xml.ws.RequestWrapper;
import javax.xml.ws.ResponseWrapper;

import org.apache.cxf.common.util.StringUtils;
import org.apache.cxf.tools.util.AnnotationUtil;

public final class WrapperUtil {
    
    private WrapperUtil() {
    }

    public static Wrapper getRequestWrapper(Method method) {
        RequestWrapper reqWrapper = method.getAnnotation(RequestWrapper.class);
        String reqClassName = "";
        String reqName = method.getName();
        String reqNS = null;
        if (reqWrapper != null && !StringUtils.isEmpty(reqWrapper.className())) {
            reqClassName = reqWrapper.className().length() > 0 ? reqWrapper.className() : reqClassName;
            reqName = reqWrapper.localName().length() > 0 ? reqWrapper.localName() : reqName;
            reqNS = reqWrapper.targetNamespace();
        } else {
            reqClassName = getPackageName(method) + ".jaxws." + AnnotationUtil.capitalize(method.getName());
        }

        return new Wrapper(reqClassName, reqName, reqNS);
    }

    public static Wrapper getResponseWrapper(Method method) {
        ResponseWrapper resWrapper = method.getAnnotation(ResponseWrapper.class);
        String resClassName = "";
        // rule 3.5 suffix -"Response"
        String resName = method.getName() + "Response";
        String resNS = null;
        if (resWrapper != null && !StringUtils.isEmpty(resWrapper.className())) {
            resClassName = resWrapper.className();
            resName = resWrapper.localName().length() > 0 ? resWrapper.localName() : resName;
            resNS = resWrapper.targetNamespace();
        } else {
            resClassName = getPackageName(method) + ".jaxws." 
                + AnnotationUtil.capitalize(method.getName())
                + "Response";
        }
        return new Wrapper(resClassName, resName, resNS);
    }

    public static boolean isWrapperClassExists(Method method) {
        Wrapper requestWrapper = getRequestWrapper(method);
        Wrapper responseWrapper = getResponseWrapper(method);
        boolean isOneWay = method.isAnnotationPresent(Oneway.class);
        try {
            requestWrapper.getWrapperClass();
            if (!isOneWay) {
                responseWrapper.getWrapperClass();
            }
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    private static String getPackageName(Method method) {
        return method.getDeclaringClass().getPackage().getName();
    }
}
