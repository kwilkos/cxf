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

package org.apache.cxf.jaxws.support;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import javax.xml.ws.WebServiceException;
import javax.xml.ws.WebServiceProvider;

import org.apache.cxf.common.i18n.BundleUtils;
import org.apache.cxf.common.i18n.Message;
import org.apache.cxf.resource.URIResolver;
import org.apache.cxf.service.factory.AbstractServiceConfiguration;
import org.apache.cxf.service.factory.ReflectionServiceFactoryBean;
import org.apache.cxf.service.factory.ServiceConstructionException;

public class WebServiceProviderConfiguration extends AbstractServiceConfiguration {
    private static final ResourceBundle BUNDLE = BundleUtils.getBundle(WebServiceProviderConfiguration.class);

    private JaxwsImplementorInfo implInfo;
    private WebServiceProvider wsProvider;
    
    @Override
    public void setServiceFactory(ReflectionServiceFactoryBean serviceFactory) {
        super.setServiceFactory(serviceFactory);
        implInfo = ((ProviderServiceFactoryBean) serviceFactory).getJaxWsImplmentorInfo();
        wsProvider = implInfo.getWsProvider();
    }

    @Override
    public String getServiceName() {
        if (wsProvider.serviceName().length() > 0) {
            return wsProvider.serviceName();
        }
        return null;
    }

    @Override
    public String getServiceNamespace() {
        if (wsProvider.targetNamespace().length() > 0) {
            return wsProvider.targetNamespace();
        }
        return null;
    }

    @Override
    public URL getWsdlURL() {
        String loc = wsProvider.wsdlLocation();
        if (loc.length() > 0) {
            try {
                URIResolver resolver = new URIResolver(null, loc, getClass());
                if (resolver.isResolved()) {
                    return resolver.getURI().toURL();
                } else {
                    throw new WebServiceException("Could not find WSDL with URL " + loc);
                }
            } catch (IOException e) {
                throw new ServiceConstructionException(new Message("LOAD_WSDL_EXC", 
                                                                   BUNDLE, 
                                                                   loc),
                                                       e);
            }
        }
        return null;
    }
}
