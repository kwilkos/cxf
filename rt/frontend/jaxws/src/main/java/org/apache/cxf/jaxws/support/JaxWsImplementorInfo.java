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
import org.apache.cxf.common.util.PackageUtils;
import org.apache.cxf.common.util.StringUtils;
import org.apache.cxf.jaxb.JAXBEncoderDecoder;

public class JaxWsImplementorInfo {

    private static final Logger LOG = LogUtils.getL7dLogger(JaxWsImplementorInfo.class);
    private static final ResourceBundle BUNDLE = LOG.getResourceBundle();

    private Class<?> implementorClass;
    private Class<?> seiClass;
    private WebService implementorAnnotation;
    private WebService seiAnnotation;
    private WebServiceProvider wsProviderAnnotation;

    public JaxWsImplementorInfo(Class<?> ic) {
        implementorClass = ic;
        initialise();
    }

    public Class<?> getSEIClass() {
        return seiClass;
    }

    public Class<?> getImplementorClass() {
        return implementorClass;
    }
    
    public Class<?> getEndpointClass() {
        Class endpointInterface = getSEIClass();
        if (null == endpointInterface) {
            endpointInterface = getImplementorClass();
        }
        return endpointInterface;
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
        } else if (wsProviderAnnotation != null) {
            serviceName = wsProviderAnnotation.serviceName();
            namespace = wsProviderAnnotation.targetNamespace();
        } else {
            return null;
        }

        if (StringUtils.isEmpty(serviceName)) {
            serviceName = implementorClass.getSimpleName() + "Service";
        }

        if (StringUtils.isEmpty(namespace)) {
            namespace = getDefaultNamespace(implementorClass);
        }

        return new QName(namespace, serviceName);
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
        } else if (wsProviderAnnotation != null) {
            portName = wsProviderAnnotation.portName();
            namespace = wsProviderAnnotation.targetNamespace();
        } else {
            return null;
        }

        if (StringUtils.isEmpty(portName)) {
            portName = implementorClass.getSimpleName() + "Port";
        }

        if (StringUtils.isEmpty(namespace)) {
            namespace = getDefaultNamespace(implementorClass);
        }

        return new QName(namespace, portName);
    }

    public QName getInterfaceName() {
        String name = null;
        String namespace = null;

        if (seiAnnotation != null) {
            if (StringUtils.isEmpty(seiAnnotation.name())) {
                name = seiClass.getSimpleName();
            } else {
                name = seiAnnotation.name();
            }
            if (StringUtils.isEmpty(seiAnnotation.targetNamespace())) {
                namespace = getDefaultNamespace(seiClass);
            } else {
                namespace = seiAnnotation.targetNamespace();
            }
        } else if (implementorAnnotation != null) {
            if (StringUtils.isEmpty(implementorAnnotation.name())) {
                name = implementorClass.getSimpleName();
            } else {
                name = implementorAnnotation.name();
            }
            if (StringUtils.isEmpty(implementorAnnotation.targetNamespace())) {
                namespace = getDefaultNamespace(implementorClass);
            } else {
                namespace = implementorAnnotation.targetNamespace();
            }
        } else {
            return null;
        }
        
        return new QName(namespace, name);
    }

    private String getDefaultNamespace(Class clazz) {
        Package pkg = clazz.getPackage();
        if (pkg == null) {
            return null;
        } else {
            return PackageUtils.getNamespace(pkg.getName());
        }
    }
        
    private String getWSInterfaceName(Class implClz) {
        Class<?>[] clzs = implClz.getInterfaces();
        for (Class<?> clz : clzs) {
            if (null != clz.getAnnotation(WebService.class)) {
                return clz.getName();
            }
        }
        return null;
    }

    private void initialise() {
        implementorAnnotation = implementorClass.getAnnotation(WebService.class);
        if (null != implementorAnnotation) {
            String sei = implementorAnnotation.endpointInterface();
            if (StringUtils.isEmpty(sei)) {
                sei = getWSInterfaceName(implementorClass);                
            }
            if (!StringUtils.isEmpty(sei)) {
                try {
                    seiClass = ClassLoaderUtils.loadClass(sei, implementorClass);
                } catch (ClassNotFoundException ex) {
                    throw new WebServiceException(BUNDLE.getString("SEI_LOAD_FAILURE_MSG"), ex);
                }
                seiAnnotation = seiClass.getAnnotation(WebService.class);
                if (null == seiAnnotation) {
                    throw new WebServiceException(BUNDLE.getString("SEI_WITHOUT_WEBSERVICE_ANNOTATION_EXC"));
                }
                if (!StringUtils.isEmpty(seiAnnotation.portName())
                    || !StringUtils.isEmpty(seiAnnotation.serviceName())
                    || !StringUtils.isEmpty(seiAnnotation.endpointInterface())) {
                    String expString = BUNDLE.getString("ILLEGAL_ATTRIBUTE_IN_SEI_ANNOTATION_EXC");
                    throw new WebServiceException(expString);
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
        ServiceMode m = implementorClass.getAnnotation(ServiceMode.class);
        if (m != null && m.value() != null) {
            return m.value();
        }
        return Service.Mode.PAYLOAD;
    }

    public Class<?> getProviderParameterType() {
        // The Provider Implementor inherits out of Provider<T>
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
