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
import java.util.ArrayList;
import java.util.List;

import org.apache.cxf.common.util.StringUtils;
import org.apache.cxf.service.model.MessageInfo;
import org.apache.cxf.service.model.MessagePartInfo;
import org.apache.cxf.service.model.OperationInfo;
import org.apache.cxf.tools.common.model.JavaField;
import org.apache.cxf.tools.java2wsdl.generator.wsdl11.model.WrapperBeanClass;
import org.apache.cxf.tools.util.AnnotationUtil;

public class RequestWrapper extends Wrapper {
    @Override
    public void setOperationInfo(final OperationInfo op) {
        super.setOperationInfo(op);
        setName(op.getInput().getMessageParts().get(0).getElementQName());
    }

    @Override
    public boolean isWrapperAbsent(final Method method) {
        javax.xml.ws.RequestWrapper reqWrapper = method.getAnnotation(javax.xml.ws.RequestWrapper.class);
        return reqWrapper == null || StringUtils.isEmpty(reqWrapper.className());
    }

    @Override
    protected List<JavaField> buildFields() {
        return buildFields(getMethod(), getOperationInfo().getUnwrappedOperation().getInput());
    }
    
    protected List<JavaField> buildFields(final Method method, final MessageInfo message) {
        List<JavaField> fields = new ArrayList<JavaField>();
        String name;
        String type;

        final Class[] paramClasses = method.getParameterTypes();
        for (MessagePartInfo mpi : message.getMessageParts()) {
            int idx = mpi.getIndex();
            name = mpi.getName().getLocalPart();
            Class clz = paramClasses[idx];
            if (clz.isArray()) {
                if (isBuiltInTypes(clz.getComponentType())) {
                    type = clz.getComponentType().getSimpleName() + "[]";
                } else {
                    type = clz.getComponentType().getName() + "[]";
                }
            } else {
                type = clz.getName();
            }
            JavaField field = new JavaField(name, type, "");
            fields.add(field);
            
        }

        return fields;
    }

    @Override
    public WrapperBeanClass getWrapperBeanClass(final Method method) {
        javax.xml.ws.RequestWrapper reqWrapper = method.getAnnotation(javax.xml.ws.RequestWrapper.class);
        String reqClassName = "";
        String reqNs = null;
        
        if (!isWrapperAbsent(method)) {
            reqClassName = reqWrapper.className().length() > 0 ? reqWrapper.className() : reqClassName;
            reqNs = reqWrapper.targetNamespace();
        } else {
            reqClassName = getPackageName(method) + ".jaxws." + AnnotationUtil.capitalize(method.getName());
        }

        WrapperBeanClass jClass = new WrapperBeanClass();
        jClass.setFullClassName(reqClassName);
        jClass.setNamespace(reqNs);
        return jClass;
    }
}
