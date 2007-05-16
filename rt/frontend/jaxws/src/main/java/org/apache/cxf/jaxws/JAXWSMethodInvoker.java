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

package org.apache.cxf.jaxws;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.MessageContext.Scope;

import org.apache.cxf.common.util.factory.Factory;
import org.apache.cxf.helpers.CastUtils;
import org.apache.cxf.jaxws.context.WebServiceContextImpl;
import org.apache.cxf.jaxws.context.WrappedMessageContext;
import org.apache.cxf.jaxws.support.ContextPropertiesMapping;
import org.apache.cxf.message.Exchange;
import org.apache.cxf.service.invoker.ApplicationScopePolicy;
import org.apache.cxf.service.invoker.FactoryInvoker;
import org.apache.cxf.service.invoker.ScopePolicy;

public class JAXWSMethodInvoker extends FactoryInvoker {

    public JAXWSMethodInvoker(final Object bean) {
        super(
            new Factory() {
                public Object create() {
                    return bean;
                }
            },
            ApplicationScopePolicy.instance());
        
    }
    
    public JAXWSMethodInvoker(Factory factory) {
        super(factory, ApplicationScopePolicy.instance());
    }
    
    public JAXWSMethodInvoker(Factory factory, ScopePolicy scope) {
        super(factory, scope);
    }

    protected Object invoke(Exchange exchange, final Object serviceObject, Method m, List<Object> params) {
        // set up the webservice request context 
        MessageContext ctx = 
            ContextPropertiesMapping.createWebServiceContext(exchange);
        
        Map<String, Scope> scopes = CastUtils.cast((Map<?, ?>)ctx.get(WrappedMessageContext.SCOPES));
        Map<String, Object> handlerScopedStuff = new HashMap<String, Object>();
        if (scopes != null) {
            for (Map.Entry<String, Scope> scope : scopes.entrySet()) {
                if (scope.getValue() == Scope.HANDLER) {
                    handlerScopedStuff.put(scope.getKey(), ctx.get(scope.getKey()));
                }
            }
            for (String key : handlerScopedStuff.keySet()) {
                ctx.remove(key);
            }
        }

        
        WebServiceContextImpl.setMessageContext(ctx);
        
        List<Object> res = CastUtils.cast((List)super.invoke(exchange, serviceObject, m, params));
        
        for (Map.Entry<String, Object> key : handlerScopedStuff.entrySet()) {
            ctx.put(key.getKey(), key.getValue());
            ctx.setScope(key.getKey(), Scope.HANDLER);
        }
        
        //update the webservice response context
        ContextPropertiesMapping.updateWebServiceContext(exchange, ctx);
        //clear the WebServiceContextImpl's ThreadLocal variable
        WebServiceContextImpl.clear();
        return res;
    }
}
