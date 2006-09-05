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

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ResourceBundle;
import java.util.logging.Logger;

import javax.jws.WebService;
import javax.xml.namespace.QName;
import javax.xml.ws.BindingType;
import javax.xml.ws.Provider;
import javax.xml.ws.Service;
import javax.xml.ws.ServiceMode;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.WebServiceProvider;
import javax.xml.ws.soap.SOAPBinding;

import org.apache.cxf.common.classloader.ClassLoaderUtils;
import org.apache.cxf.common.logging.LogUtils;
import org.apache.cxf.common.util.StringUtils;
import org.apache.cxf.jaxb.JAXBEncoderDecoder;

public class JaxwsImplementorInfo {

    private static final Logger LOG = LogUtils.getL7dLogger(JaxwsImplementorInfo.class);
    private static final ResourceBundle BUNDLE = LOG.getResourceBundle();

    private Class<?> implementorClass;
    private Class<?> seiClass;
    private WebService implementorAnnotation;
    private WebService seiAnnotation;
    private WebServiceProvider wsProviderAnnotation;

    public JaxwsImplementorInfo(Class<?> ic) {
        implementorClass = ic;
        initialise();
    }

    public Class<?> getSEIClass() {
        return seiClass;
    }

    public Class<?> getImplementorClass() {
        return implementorClass;
    }

    public String getWsdlLocation() {
        if (null != seiAnnotation) {
            return seiAnnotation.wsdlLocation();
        } else if (null != implementorAnnotation) {
            return implementorAnnotation.wsdlLocation();
        } else if (null != wsProviderAnnotation) {
            return wsProviderAnnotation.wsdlLocation();
        }
        return null;
    }

    /**
     * See use of targetNamespace in {@link WebService}.
     * 
     * @return the qualified name of the service.
     */
    public QName getServiceName() {
        String serviceName = null;
        String namespace = null;
        if (implementorAnnotation != null) {
            serviceName = implementorAnnotation.serviceName();
            namespace = implementorAnnotation.targetNamespace();
        } else {
            // Must be a provider
            serviceName = wsProviderAnnotation.serviceName();
            namespace = wsProviderAnnotation.targetNamespace();
        }
        if (StringUtils.isEmpty(serviceName)) {
            serviceName = implementorClass.getName();
        }
        if (!StringUtils.isEmpty(namespace) && !StringUtils.isEmpty(serviceName)) {
            return new QName(namespace, serviceName);
        }
        return null;
    }

    /**
     * See use of targetNamespace in {@link WebService}.
     * 
     * @return the qualified name of the endpoint.
     */
    public QName getEndpointName() {
        String portName = null;
        String namespace = null;
        if (implementorAnnotation != null) {
            portName = implementorAnnotation.portName();
            namespace = implementorAnnotation.targetNamespace();
        } else {
            // Must be a provider
            portName = wsProviderAnnotation.portName();
            namespace = wsProviderAnnotation.targetNamespace();
        }

        if (StringUtils.isEmpty(portName)) {
            portName = implementorClass.getSimpleName();
        }

        if (!StringUtils.isEmpty(namespace) && !StringUtils.isEmpty(portName)) {
            return new QName(namespace, portName);
        }
        return null;
    }

    private void initialise() {
        implementorAnnotation = implementorClass.getAnnotation(WebService.class);
        if (null != implementorAnnotation) {

            String sei = implementorAnnotation.endpointInterface();
            if (null != sei && !"".equals(sei)) {
                try {
                    seiClass = ClassLoaderUtils.loadClass(sei, implementorClass);
                } catch (ClassNotFoundException ex) {
                    throw new WebServiceException(BUNDLE.getString("SEI_LOAD_FAILURE_MSG"), ex);
                }
                seiAnnotation = seiClass.getAnnotation(WebService.class);
                if (null == seiAnnotation) {
                    throw new WebServiceException(BUNDLE.getString("SEI_WITHOUT_WEBSERVICE_ANNOTATION_EXC"));
                }
                String portName = seiAnnotation.portName();
                String serviceName = seiAnnotation.serviceName();
                String endpointInterface = seiAnnotation.endpointInterface();
                if ((null != portName && !"".equals(portName))
                    || (null != serviceName && !"".equals(serviceName))
                    || (null != endpointInterface && !"".equals(endpointInterface))) {
                    throw new 
                    WebServiceException(BUNDLE.getString("ILLEGAL_ATTRIBUTE_IN_SEI_ANNOTATION_EXC"));
                }
            }
        } else {
            wsProviderAnnotation = implementorClass.getAnnotation(WebServiceProvider.class);
        }
    }

    public boolean isWebServiceProvider() {
        return Provider.class.isAssignableFrom(implementorClass);
    }

    public WebServiceProvider getWsProvider() {
        return wsProviderAnnotation;
    }

    public Service.Mode getServiceMode() {
        return implementorClass.getAnnotation(ServiceMode.class).value();
    }

    public Class<?> getProviderParameterType() {
        //The Provider Implementor inherits out of Provier<T>
        Type intfTypes[] = implementorClass.getGenericInterfaces();
        for (Type t : intfTypes) {
            Class<?> clazz = JAXBEncoderDecoder.getClassFromType(t);
            if (Provider.class == clazz) {
                Type paramTypes[] = ((ParameterizedType)t).getActualTypeArguments();
                return JAXBEncoderDecoder.getClassFromType(paramTypes[0]);
            }
        }
        return null;
    }

    public String getBindingType() {
        BindingType bType = implementorClass.getAnnotation(BindingType.class);
        if (bType != null) {
            return bType.value();
        }
        return SOAPBinding.SOAP11HTTP_BINDING;
    }
}
