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

package org.apache.cxf.service.invoker;

import java.util.ResourceBundle;

import org.apache.cxf.common.i18n.BundleUtils;
import org.apache.cxf.common.i18n.Message;
import org.apache.cxf.common.util.factory.Factory;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.message.Exchange;

/**
 * This invoker implementation calls a Factory to create the service object and
 * then applies a scope policy for caching.
 * 
 * @author Ben Yu Feb 2, 2006 12:55:59 PM
 */
public class FactoryInvoker extends AbstractInvoker {
    private static final ResourceBundle BUNDLE = BundleUtils.getBundle(FactoryInvoker.class);

    private final Factory factory;
    private final ScopePolicy scope;

    /**
     * Create a FactoryInvoker object.
     * 
     * @param factory the factory used to create service object.
     * @param scope the scope policy. Null for default.
     */
    public FactoryInvoker(Factory factory, ScopePolicy scope) {
        this.factory = factory;
        this.scope = scope == null ? new ApplicationScopePolicy() : scope;
    }

    public Object getServiceObject(Exchange ex) {
        try {
            return getScopedFactory(ex).create();
        } catch (Throwable e) {
            throw new Fault(new Message("CREATE_SERVICE_OBJECT_EXC", BUNDLE), e);
        }
    }

    private Factory getScopedFactory(Exchange ex) {
        return scope.applyScope(factory, ex);
    }
}
