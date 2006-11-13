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
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.xml.ws.Holder;
import javax.xml.ws.handler.MessageContext;

import org.apache.cxf.binding.soap.model.SoapHeaderInfo;
import org.apache.cxf.common.util.factory.Factory;
import org.apache.cxf.jaxws.context.WebServiceContextImpl;
import org.apache.cxf.jaxws.support.ContextPropertiesMapping;
import org.apache.cxf.message.Exchange;
import org.apache.cxf.service.invoker.ApplicationScopePolicy;
import org.apache.cxf.service.invoker.FactoryInvoker;
import org.apache.cxf.service.invoker.ScopePolicy;
import org.apache.cxf.service.model.BindingMessageInfo;
import org.apache.cxf.service.model.BindingOperationInfo;

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

    @SuppressWarnings("unchecked")
    protected Object invoke(Exchange exchange, final Object serviceObject, Method m, List<Object> params) {
        BindingOperationInfo bop = exchange.get(BindingOperationInfo.class);
        List<Object> orderedParams = params;
        if (bop != null && bop.getInput() != null) {
            orderedParams = getOrderedParamList(params, bop.getInput());
        }
        checkHolder(m, orderedParams, exchange);
        // set up the webservice request context 
        MessageContext ctx = 
            ContextPropertiesMapping.createWebServiceContext(exchange);
        WebServiceContextImpl.setMessageContext(ctx);
        
        List<Object> res = (List<Object>) super.invoke(exchange, serviceObject,
            m, orderedParams);
        if (bop != null && bop.getOutput() != null) {
            getOrderedReturnList(res, orderedParams, bop.getOutput());
        } else {
            for (Object o : orderedParams) {
                if (o instanceof Holder) {
                    res.add(((Holder) o).value);
                }
            }
        }
        //update the webservice response context
        ContextPropertiesMapping.updateWebServiceContext(exchange, ctx);
        return res;
    }

    @SuppressWarnings("unchecked")
    private void checkHolder(Method method, List<Object> params, Exchange exchange) {
        if (method != null) {

            Type[] para = method.getGenericParameterTypes();
            for (int i = 0; i < para.length; i++) {
                if (para[i] instanceof ParameterizedType) {
                    ParameterizedType paramType = (ParameterizedType)para[i];
                    if (((Class)paramType.getRawType()).getName().equals("javax.xml.ws.Holder")) {
                        if (i >= params.size()) {
                            params.add(new Holder());
                        } else {
                            params.set(i, new Holder(params.get(i)));
                        }
                    }
                }
            }
        }
    }
    
    private List<Object> getOrderedParamList(List<Object> params, BindingMessageInfo bmi) {
        List<SoapHeaderInfo> headers = bmi.getExtensors(SoapHeaderInfo.class);
        if (headers == null || headers.isEmpty()) {
            return params;
        }
        List<Object> orderedParams = new ArrayList<Object>();
        
        // Add non-header params to the list first.
        int headerIdx = params.size() - headers.size();
        orderedParams.addAll(params.subList(0, headerIdx));
        
        Map<Integer, Object> headerParams = new TreeMap<Integer, Object>();
        for (SoapHeaderInfo header : headers) {
            headerParams.put(header.getSequence(), params.get(headerIdx));
            headerIdx++;
        }
        // Insert the header params according to their wsdl message order.
        for (Integer i : headerParams.keySet()) {
            if (i.intValue() <= orderedParams.size()) {
                orderedParams.add(i.intValue(), headerParams.get(i));
            } else {
                orderedParams.add(headerParams.get(i));
            }
        }

        return orderedParams;
    }

    private void getOrderedReturnList(List<Object> res, List<Object> params, BindingMessageInfo bmi) {
        List<SoapHeaderInfo> headers = bmi.getExtensors(SoapHeaderInfo.class);
        int returnIdx = 0;
        if (headers != null && !headers.isEmpty()) {
            Collection<Integer> headerIndices = new TreeSet<Integer>();
            for (SoapHeaderInfo header : headers) {
                headerIndices.add(header.getSequence());
            }
            // XXX - Does this assume that the header parts in the IN message
            // have the same order as in the out message?
            for (Integer i : headerIndices) {
                if (i.intValue() <= returnIdx && params.get(i) instanceof Holder) {
                    res.add(i.intValue(), ((Holder) params.get(i.intValue())).value);
                    returnIdx++;
                } else {
                    break;
                }
            }
        }
        for (int i = returnIdx; i < params.size(); i++) {
            Object o = params.get(i);
            if (o instanceof Holder) {
                res.add(((Holder) o).value);
            }
        }
    }

}
