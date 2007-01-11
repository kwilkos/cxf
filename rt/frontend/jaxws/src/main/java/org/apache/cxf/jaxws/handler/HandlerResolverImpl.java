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

package org.apache.cxf.jaxws.handler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.ws.handler.Handler;
import javax.xml.ws.handler.HandlerResolver;
import javax.xml.ws.handler.PortInfo;

import org.apache.cxf.Bus;

public class HandlerResolverImpl implements HandlerResolver {
    private final Map<PortInfo, List<Handler>> handlerMap = new HashMap<PortInfo, List<Handler>>();
    
    //private QName service;   
    private Class<?> annotationClass;

    public HandlerResolverImpl(Bus bus, QName serviceName, Class<?> clazz) {
        //this.service = pService;
        this.annotationClass = clazz;
    }

    public HandlerResolverImpl() {
        this(null, null, null);
    }

    public List<Handler> getHandlerChain(PortInfo portInfo) {

        List<Handler> handlerChain = handlerMap.get(portInfo);
        if (handlerChain == null) {
            handlerChain = createHandlerChain(portInfo);
            handlerMap.put(portInfo, handlerChain);
        }
        return handlerChain;
    }

    private List<Handler> createHandlerChain(PortInfo portInfo) {
        List<Handler> chain = null;

        if (null == chain) {
            chain = new ArrayList<Handler>();
        }
        if (annotationClass != null) {
            chain.addAll(getHandlersFromAnnotation(annotationClass));            
        }
        return chain;
    }

    /**
     * Obtain handler chain from annotations.
     * 
     * @param obj A endpoint implementation class or a SEI, or a generated
     *            service class.
     */
    private List<Handler> getHandlersFromAnnotation(Class<?> clazz) {
        AnnotationHandlerChainBuilder builder = new AnnotationHandlerChainBuilder();

        List<Handler> chain = builder.buildHandlerChainFromClass(clazz);
        
        return chain;
    }
}
