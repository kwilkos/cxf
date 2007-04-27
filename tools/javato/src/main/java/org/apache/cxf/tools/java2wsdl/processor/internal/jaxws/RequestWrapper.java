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

import org.apache.cxf.common.util.StringUtils;
import org.apache.cxf.tools.common.model.JavaClass;
import org.apache.cxf.tools.util.AnnotationUtil;

public class RequestWrapper extends Wrapper {
    public boolean isWrapperAbsent(final Method method) {
        javax.xml.ws.RequestWrapper reqWrapper = method.getAnnotation(javax.xml.ws.RequestWrapper.class);
        return reqWrapper == null || StringUtils.isEmpty(reqWrapper.className());
    }

    public JavaClass getWrapperBeanClass(final Method method) {
        javax.xml.ws.RequestWrapper reqWrapper = method.getAnnotation(javax.xml.ws.RequestWrapper.class);
        String reqClassName = "";
        String reqNs = null;
        
        if (!isWrapperAbsent(method)) {
            reqClassName = reqWrapper.className().length() > 0 ? reqWrapper.className() : reqClassName;
            reqNs = reqWrapper.targetNamespace();
        } else {
            reqClassName = getPackageName(method) + ".jaxws." + AnnotationUtil.capitalize(method.getName());
        }

        JavaClass jClass = new JavaClass();
        jClass.setFullClassName(reqClassName);
        jClass.setNamespace(reqNs);
        return jClass;
    }
}
