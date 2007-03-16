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

package org.apache.cxf.tools.java2wsdl.processor.internal;

import org.apache.cxf.tools.common.ToolException;
import org.apache.cxf.tools.java2wsdl.processor.FrontendFactory;

public final class ServiceBuilderFactory {
    private static ServiceBuilderFactory instance;
    private Class serviceClass;
    private FrontendFactory frontend;
    
    private ServiceBuilderFactory() {
        frontend = FrontendFactory.getInstance();
    }
    
    public static ServiceBuilderFactory getInstance() {
        if (instance == null) {
            instance = new ServiceBuilderFactory();
        }
        return instance;
    }

    public ServiceBuilder newBuilder() {
        return newBuilder(getStyle());
    }

    public ServiceBuilder newBuilder(FrontendFactory.Style s) {
        ServiceBuilder builder = null;
        try {
            String clzName = getBuilderClassName(s);
            builder = (ServiceBuilder) Class.forName(clzName).newInstance();
        } catch (Exception e) {
            throw new ToolException("Can not find the ServiceBulider for style: " + s, e);
        }
        builder.setServiceClass(serviceClass);
        return builder;
    }

    protected String getBuilderClassName(FrontendFactory.Style s) {
        String pkgName = getClass().getPackage().getName();
        return pkgName + "." + s.toString().toLowerCase() + "." + s + "ServiceBuilder";
    }

    public FrontendFactory.Style getStyle() {
        frontend.setServiceClass(this.serviceClass);
        return frontend.discoverStyle();
    }

    public void setServiceClass(Class c) {
        this.serviceClass = c;
    }
}
