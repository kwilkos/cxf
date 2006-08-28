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
    public static final String PORT_CONFIGURATION_URI =
        "http://cxf.apache.org/bus/jaxws/port-config";

    private final Map<PortInfo, List<Handler>> handlerMap = new HashMap<PortInfo, List<Handler>>();
    //private Configuration busConfiguration;
    //private QName service;
    private ClassLoader serviceEndpointInterfaceClassLoader;

    public HandlerResolverImpl(Bus bus, QName serviceName) {
        //this.busConfiguration = pBusConfiguration;
        //this.service = pService;
    }

    public HandlerResolverImpl() {
        this(null, null);
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
        /*
        Configuration portConfiguration = null;
        String id = portInfo.getPortName().getLocalPart();
        if (service != null) {
            id = service.toString() + "/" + portInfo.getPortName().getLocalPart();
        }
        if (null != busConfiguration) {
            portConfiguration = busConfiguration
                .getChild(PORT_CONFIGURATION_URI, id);
        }
        if (null != portConfiguration) {
            HandlerChainBuilder builder = new HandlerChainBuilder();
            builder.setHandlerClassLoader(serviceEndpointInterfaceClassLoader);
            HandlerChainType hc = (HandlerChainType)portConfiguration.getObject("handlerChain");
            chain = builder.buildHandlerChainFromConfiguration(hc);
        }
        */
        if (null == chain) {
            chain = new ArrayList<Handler>();
        }
        return chain;
    }

    public ClassLoader getServiceEndpointInterfaceClassLoader() {
        return serviceEndpointInterfaceClassLoader;
    }

    public void setServiceEndpointInterfaceClassLoader(ClassLoader classLoader) {
        this.serviceEndpointInterfaceClassLoader = classLoader;
    }
}
