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

package org.apache.cxf.jaxrs.model;

import java.lang.reflect.Method;
import java.util.List;

import javax.ws.rs.ext.EntityProvider;

import org.apache.cxf.jaxrs.provider.JAXBElementProvider;

public class OperationResourceInfo {
    private URITemplate uriTemplate;
    private ClassResourceInfo classResourceInfo;
    private Method method;
    private List<Class> parameterTypeList;
    private List<Class> annotatedParameterTypeList;
    private List<EntityProvider> entityProviderList;
    private String httpMethod;

    public OperationResourceInfo(Method m, ClassResourceInfo cri) {
        method = m;
        classResourceInfo = cri;
    }

    public URITemplate getURITemplate() {
        return uriTemplate;
    }

    public void setURITemplate(URITemplate u) {
        uriTemplate = u;
    }

    public ClassResourceInfo getClassResourceInfo() {
        return classResourceInfo;
    }

    public void setClassResourceInfo(ClassResourceInfo c) {
        classResourceInfo = c;
    }

    public Method getMethod() {
        return method;
    }

    public void setMethod(Method m) {
        method = m;
    }

    public String getHttpMethod() {
        return httpMethod;
    }

    public void setHttpMethod(String m) {
        httpMethod = m;
    }

    public List<Class> getParameterTypeList() {
        return parameterTypeList;
    }

    public List<Class> getAnnotatedParameterTypeList() {
        return annotatedParameterTypeList;
    }

    public List<EntityProvider> getEntityProviderList() {
        return entityProviderList;
    }

    protected EntityProvider findEntityProvider() {
        return new JAXBElementProvider();

    }
}
