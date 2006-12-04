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

package org.apache.cxf.service.factory;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import javax.xml.namespace.QName;

import org.apache.cxf.common.util.ParamReader;
import org.apache.cxf.helpers.ServiceUtils;
import org.apache.cxf.message.Exchange;
import org.apache.cxf.service.model.InterfaceInfo;
import org.apache.cxf.service.model.OperationInfo;

public class DefaultServiceConfiguration extends AbstractServiceConfiguration {

    @Override
    public QName getOperationName(InterfaceInfo service, Method method) {
        return new QName(service.getName().getNamespaceURI(), method.getName());
    }

    @Override
    public QName getFaultName(InterfaceInfo service, OperationInfo o, Class<?> exClass, Class<?> beanClass) {
        String name = ServiceUtils.makeServiceNameFromClassName(beanClass);
        return new QName(service.getName().getNamespaceURI(), name);
    }

    @Override
    public QName getInPartName(OperationInfo op, Method method, int paramNumber) {
        return getInParameterName(op, method, paramNumber);
    }

    @Override
    public QName getOutPartName(OperationInfo op, Method method, int paramNumber) {
        return getOutParameterName(op, method, paramNumber);
    }

    @Override
    public QName getInParameterName(OperationInfo op, Method method, int paramNumber) {
        return new QName(op.getName().getNamespaceURI(), 
                         getDefaultLocalName(op, method, paramNumber, "arg" + paramNumber));
    }

    @Override
    public QName getInputMessageName(OperationInfo op) {
        return new QName(op.getName().getNamespaceURI(), op.getName().getLocalPart());
    }

    @Override
    public QName getOutParameterName(OperationInfo op, Method method, int paramNumber) {
        return new QName(op.getName().getNamespaceURI(), 
                         getDefaultLocalName(op, method, paramNumber, "return"));
    }

    private String getDefaultLocalName(OperationInfo op, Method method, int paramNumber, String prefix) {
        Class<?> impl = getServiceFactory().getServiceClass();
        // try to grab the implementation class so we can read the debug symbols from it
        if (impl == null) {
            try {
                method = impl.getMethod(method.getName(), method.getParameterTypes());
            } catch (Exception e) {
                throw new ServiceConstructionException(e);
            }
        }
        
        return DefaultServiceConfiguration.createName(method, paramNumber, op.getInput()
            .getMessageParts().size(), false, prefix);
    }

    public static String createName(final Method method, final int paramNumber, final int currentSize,
                              boolean addMethodName, final String flow) {
        String paramName = "";

        if (paramNumber != -1) {
            String[] names = ParamReader.getParameterNamesFromDebugInfo(method);

            // get the specific parameter name from the parameter Number
            if (names != null && names[paramNumber] != null) {
                paramName = names[paramNumber];
                addMethodName = false;
            } else {
                paramName = flow + currentSize;
            }
        } else {
            paramName = flow;
        }

        paramName = addMethodName ? method.getName() + paramName : paramName;

        return paramName;
    }

    @Override
    public QName getOutputMessageName(OperationInfo op) {
        return new QName(op.getName().getNamespaceURI(), op.getName().getLocalPart() + "Response");
    }

    @Override
    public QName getInterfaceName() {
        return new QName(getServiceFactory().getServiceNamespace(), getServiceName() + "PortType");
    }

    @Override
    public QName getEndpointName() {
        return new QName(getServiceFactory().getServiceNamespace(), getServiceName() + "Port");
    }

    @Override
    public String getServiceName() {
        return getServiceFactory().getServiceClass().getSimpleName();
    }

    @Override
    public String getServiceNamespace() {
        return ServiceUtils.makeNamespaceFromClassName(getServiceFactory().getServiceClass().getName(),
                                                       "http");
    }

    @Override
    public Boolean hasOutMessage(Method m) {
        return true;
    }

    @Override
    public Boolean isAsync(Method method) {
        return Boolean.FALSE;
    }

    @Override
    public Boolean isHeader(Method method, int j) {
        return Boolean.FALSE;
    }

    @Override
    public Boolean isInParam(Method method, int j) {
        if (j >= 0) {
            Class c = method.getParameterTypes()[j];
            if (Exchange.class.equals(c)) {
                return false;
            }
            return true;
        }
        return false;
    }

    @Override
    public Boolean isOperation(Method method) {
        if (getServiceFactory().getIgnoredClasses().contains(method.getDeclaringClass().getName())) {
            return Boolean.FALSE;
        }

        final int modifiers = method.getModifiers();

        if (Modifier.isPublic(modifiers) && !Modifier.isStatic(modifiers)) {
            return Boolean.TRUE;
        }
        return Boolean.FALSE;
    }

    @Override
    public Boolean isOutParam(Method method, int j) {
        return j < 0;
    }

    @Override
    public Boolean isWrapped(Method m) {
        return getServiceFactory().isWrapped();
    }
}
