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
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ResourceBundle;

import javax.xml.namespace.QName;
import javax.xml.soap.SOAPMessage;
import javax.xml.transform.Source;
import javax.xml.ws.WebServiceException;

import org.apache.cxf.common.i18n.BundleUtils;
import org.apache.cxf.common.i18n.Message;
import org.apache.cxf.resource.URIResolver;
import org.apache.cxf.service.factory.ReflectionServiceFactoryBean;
import org.apache.cxf.service.factory.ServiceConstructionException;

public class WebServiceProviderConfiguration extends JaxWsServiceConfiguration {
    private static final ResourceBundle BUNDLE = BundleUtils.getBundle(WebServiceProviderConfiguration.class);

    private JaxWsImplementorInfo implInfo;
    
    @Override
    public Boolean isOperation(Method method) {
        return method.getName().equals("invoke") 
            && method.getParameterTypes().length == 1
            && (Source.class.isAssignableFrom(method.getParameterTypes()[0])
                || SOAPMessage.class.isAssignableFrom(method.getParameterTypes()[0]));
    }


    @Override
    public void setServiceFactory(ReflectionServiceFactoryBean serviceFactory) {
        super.setServiceFactory(serviceFactory);
        implInfo = ((JaxWsServiceFactoryBean) serviceFactory).getJaxWsImplementorInfo();
    }


    @Override
    public String getServiceName() {
        QName service = implInfo.getServiceName();
        if (service == null) {
            return null;
        } else {
            return service.getLocalPart();
        }
    }

    @Override
    public String getServiceNamespace() {
        QName service = implInfo.getServiceName();
        if (service == null) {
            return null;
        } else {
            return service.getNamespaceURI();
        }
    }

    @Override
    public QName getEndpointName() {
        return implInfo.getEndpointName();
    }

    @Override
    public URL getWsdlURL() {
        String wsdlLocation = implInfo.getWsdlLocation();
        if (wsdlLocation != null && wsdlLocation.length() > 0) {
            try {
                URIResolver resolver = new URIResolver(null, wsdlLocation, getClass());
                if (resolver.isResolved()) {
                    return resolver.getURI().toURL();
                } else {
                    throw new WebServiceException("Could not find WSDL with URL " + wsdlLocation);
                }
            } catch (IOException e) {
                throw new ServiceConstructionException(
                    new Message("LOAD_WSDL_EXC", BUNDLE, wsdlLocation), e);
            }
        }
        return null;
    }
}
