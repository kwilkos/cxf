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

package org.apache.cxf.wsdl11;

import java.net.URL;
import java.util.List;
import java.util.logging.Logger;

import javax.wsdl.Definition;
import javax.wsdl.WSDLException;
import javax.xml.namespace.QName;

import org.apache.cxf.Bus;
import org.apache.cxf.common.i18n.Message;
import org.apache.cxf.common.logging.LogUtils;
import org.apache.cxf.service.Service;
import org.apache.cxf.service.ServiceImpl;
import org.apache.cxf.service.factory.AbstractServiceFactoryBean;
import org.apache.cxf.service.factory.ServiceConstructionException;
import org.apache.cxf.service.model.ServiceInfo;
import org.apache.cxf.wsdl.WSDLManager;

public class WSDLServiceFactory extends AbstractServiceFactoryBean {
    
    private static final Logger LOG = LogUtils.getL7dLogger(WSDLServiceFactory.class);
    
    private URL wsdlUrl;
    private QName serviceName;
    private Definition definition;
    
    public WSDLServiceFactory(Bus b, Definition d) {
        setBus(b);
        definition = d;
    }
    
    public WSDLServiceFactory(Bus b, Definition d, QName sn) {
        this(b, d);
        serviceName = sn;
    }
    
    public WSDLServiceFactory(Bus b, URL url) {
        setBus(b);
        wsdlUrl = url;
        
        try {
            // use wsdl manager to parse wsdl or get cached definition
            definition = getBus().getExtension(WSDLManager.class).getDefinition(wsdlUrl);
        } catch (WSDLException ex) {
            throw new ServiceConstructionException(new Message("SERVICE_CREATION_MSG", LOG), ex);
        }
        
    }
    
    public WSDLServiceFactory(Bus b, URL url, QName sn) {
        this(b, url);
        serviceName = sn;
    }
    
    public Service create() {
        ServiceInfo serviceInfo;
        if (serviceName == null) {
            List<ServiceInfo> services = new WSDLServiceBuilder(getBus()).buildServices(definition);
            if (services.size() == 0) {
                throw new ServiceConstructionException(new Message("NO_SERVICE_EXC", LOG));
            } else {
                //@@TODO  - this isn't good, need to return all the services
                serviceInfo = services.get(0);
                serviceName = serviceInfo.getName();
            }
        } else {
            javax.wsdl.Service wsdlService = definition.getService(serviceName);
            if (wsdlService == null) {
                throw new ServiceConstructionException(new Message("NO_SUCH_SERVICE_EXC", LOG, serviceName));
            }
            serviceInfo = new WSDLServiceBuilder(getBus()).buildServices(definition, wsdlService).get(0);
        }
        ServiceImpl service = new ServiceImpl(serviceInfo);
        setService(service);
        return service;
    }
    
}
