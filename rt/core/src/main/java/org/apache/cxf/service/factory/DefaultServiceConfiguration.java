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

import org.apache.cxf.helpers.ServiceUtils;
import org.apache.cxf.service.Service;
import org.apache.cxf.service.model.InterfaceInfo;
import org.apache.cxf.service.model.OperationInfo;

public class DefaultServiceConfiguration extends AbstractServiceConfiguration {

    @Override
    public QName getOperationName(InterfaceInfo service, Method method) {
        return new QName(service.getName().getNamespaceURI(), method.getName());
    }

    @Override
    public String getAction(OperationInfo op) {
        // TODO Auto-generated method stub
        return super.getAction(op);
    }

    @Override
    public QName getFaultName(Service service, OperationInfo o, Class exClass, Class beanClass) {
        // TODO Auto-generated method stub
        return super.getFaultName(service, o, exClass, beanClass);
    }

    @Override
    public QName getInParameterName(OperationInfo op, Method method, int paramNumber, boolean doc) {
        // TODO Auto-generated method stub
        return super.getInParameterName(op, method, paramNumber, doc);
    }

    @Override
    public QName getInputMessageName(OperationInfo op) {
        // TODO Auto-generated method stub
        return super.getInputMessageName(op);
    }

    @Override
    public String getMEP(Method method) {
        // TODO Auto-generated method stub
        return super.getMEP(method);
    }

    @Override
    public QName getOutParameterName(OperationInfo op, Method method, int paramNumber, boolean doc) {
        // TODO Auto-generated method stub
        return super.getOutParameterName(op, method, paramNumber, doc);
    }

    @Override
    public QName getOutputMessageName(OperationInfo op) {
        // TODO Auto-generated method stub
        return super.getOutputMessageName(op);
    }

    @Override
    public QName getInterfaceName() {
        return new QName(getServiceNamespace(), getServiceName() + "PortType");
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
    public Boolean hasOutMessage(String mep) {
        // TODO Auto-generated method stub
        return super.hasOutMessage(mep);
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
        return j >= 0;
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

}
