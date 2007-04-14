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

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.concurrent.Future;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebParam.Mode;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.jws.soap.SOAPBinding.ParameterStyle;
import javax.jws.soap.SOAPBinding.Style;
import javax.xml.namespace.QName;
import javax.xml.ws.Holder;
import javax.xml.ws.RequestWrapper;
import javax.xml.ws.Response;
import javax.xml.ws.ResponseWrapper;
import javax.xml.ws.WebFault;

import org.apache.cxf.common.classloader.ClassLoaderUtils;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.service.factory.AbstractServiceConfiguration;
import org.apache.cxf.service.factory.ReflectionServiceFactoryBean;
import org.apache.cxf.service.factory.ServiceConstructionException;
import org.apache.cxf.service.model.InterfaceInfo;
import org.apache.cxf.service.model.MessageInfo;
import org.apache.cxf.service.model.OperationInfo;

public class JaxWsServiceConfiguration extends AbstractServiceConfiguration {

    private JaxWsImplementorInfo implInfo;

    @Override
    public void setServiceFactory(ReflectionServiceFactoryBean serviceFactory) {
        super.setServiceFactory(serviceFactory);
        implInfo = ((JaxWsServiceFactoryBean)serviceFactory).getJaxWsImplementorInfo();
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
    public QName getInterfaceName() {
        return implInfo.getInterfaceName();
    }

    @Override
    public String getWsdlURL() {
        String wsdlLocation = implInfo.getWsdlLocation();
        if (wsdlLocation != null && wsdlLocation.length() > 0) {
            return wsdlLocation;
        }
        return null;
    }

    @Override
    public QName getOperationName(InterfaceInfo intf, Method method) {
        method = getDeclaredMethod(method);

        WebMethod wm = method.getAnnotation(WebMethod.class);
        if (wm != null) {
            String name = wm.operationName();
            if (name.length() == 0) {
                name = method.getName();
            }

            return new QName(intf.getName().getNamespaceURI(), name);
        }

        return null;
    }

    @Override
    public Boolean isOperation(Method method) {
        method = getDeclaredMethod(method);
        if (method.getReturnType().equals(Future.class)
            || method.getReturnType().equals(Response.class)) {
            return false;
        }
        
        if (method != null) {
            WebMethod wm = method.getAnnotation(WebMethod.class);
            if (wm != null) {
                if (wm.exclude()) {
                    return Boolean.FALSE;
                } else {
                    return Boolean.TRUE;
                }
            } else {
                return hasWebServiceAnnotation(method);              
            }
        }
        return Boolean.FALSE;
    }
    
    private boolean hasWebServiceAnnotation(Method method) {
        return method.getDeclaringClass().getAnnotation(WebService.class) != null; 
    }

    Method getDeclaredMethod(Method method) {
        Class<?> endpointClass = implInfo.getEndpointClass();

        if (!method.getDeclaringClass().equals(endpointClass)) {
            try {
                method = endpointClass.getMethod(method.getName(), (Class[])method.getParameterTypes());
            } catch (SecurityException e) {
                throw new ServiceConstructionException(e);
            } catch (NoSuchMethodException e) {
                // Do nothing
            }
        }
        return method;
    }

    @Override
    public QName getInPartName(OperationInfo op, Method method, int paramNumber) {
        if (paramNumber < 0) {
            return null;
        }
                
        return getPartName(op, method, paramNumber, op.getInput(), "arg");
    }

    @Override
    public QName getInParameterName(OperationInfo op, Method method, int paramNumber) {
        if (paramNumber < 0) {
            return null;
        }
        
        return getParameterName(op, method, paramNumber, op.getInput().size(), "arg");
    }

    private QName getPartName(OperationInfo op, Method method,
                              int paramNumber, MessageInfo mi, String prefix) {
        int curSize = mi.size();
        
        method = getDeclaredMethod(method);
        WebParam param = getWebParam(method, paramNumber);
        String tns = mi.getName().getNamespaceURI();
        QName ret = null;
        if (param != null) {
            String local = param.partName();
            if (local.length() == 0) {
                local = param.name();
            }
            if (local.length() == 0) {
                local = getDefaultLocalName(op, method, paramNumber, curSize, prefix);
            }
            ret = new QName(tns, local);
        } else {
            ret = new QName(tns, getDefaultLocalName(op, method, paramNumber, curSize, prefix));
        }
        return ret;
    }

    private QName getParameterName(OperationInfo op, Method method, int paramNumber, 
                                   int curSize, String prefix) {
        method = getDeclaredMethod(method);
        WebParam param = getWebParam(method, paramNumber);
        String tns = null;
        String local = null;
        if (param != null) {
            tns = param.targetNamespace();
            local = param.name();
        }
        
        if (tns == null || tns.length() == 0) {
            tns = op.getName().getNamespaceURI();
        }

        if (local == null || local.length() == 0) {
            local = getDefaultLocalName(op, method, paramNumber, curSize, prefix);
        }
        
        return new QName(tns, local);
    }

    private String getDefaultLocalName(OperationInfo op, Method method, int paramNumber, 
                                       int curSize, String prefix) {
        String paramName = null;        
        if (paramNumber != -1) {
            paramName = prefix + curSize;
        } else {
            paramName = prefix;
        }
        return paramName;
    }

    private WebParam getWebParam(Method method, int parameter) {
        Annotation[][] annotations = method.getParameterAnnotations();
        if (parameter >= annotations.length) {
            return null;
        } else {
            for (int i = 0; i < annotations[parameter].length; i++) {
                Annotation annotation = annotations[parameter][i];
                // With the ibm jdk, the condition:
                // if (annotation.annotationType().equals(WebParam.class)) {
                // SOMETIMES returns false even when the annotation type
                // is a WebParam.  Doing an instanceof check or using the
                // == operator seems to give the desired result.
                if (annotation instanceof WebParam) {
                    return (WebParam)annotation;
                }
            }
            return null;
        }
    }

    @Override
    public QName getOutParameterName(OperationInfo op, Method method, int paramNumber) {
        method = getDeclaredMethod(method);
        
        if (paramNumber >= 0) {
            return getParameterName(op, method, paramNumber, op.getOutput().size(), "return");
        } else {
            WebResult webResult = getWebResult(method);

            String tns = null;
            String local = null;
            if (webResult != null) {
                tns = webResult.targetNamespace();
                local = webResult.name();
            }

            if (tns == null || tns.length() == 0) {
                tns = op.getName().getNamespaceURI();
            }

            if (local == null || local.length() == 0) {
                local = getDefaultLocalName(op, method, paramNumber, op.getOutput().size(), "return");
            }
            
            return new QName(tns, local);
        }
    }

    @Override
    public QName getOutPartName(OperationInfo op, Method method, int paramNumber) {
        method = getDeclaredMethod(method);
        
        if (paramNumber >= 0) {
            return getPartName(op, method, paramNumber, op.getOutput(), "return");
        } else {
            WebResult webResult = getWebResult(method);
            String tns = op.getOutput().getName().getNamespaceURI();
            
            QName ret;
            if (webResult != null) {
                String local = webResult.partName();
//                if (webResult.targetNamespace().length() > 0) {
//                    tns = webResult.targetNamespace();
//                }
                if (local.length() == 0) {
                    local = webResult.name();
                }
                if (local.length() == 0) {
                    local = getDefaultLocalName(op, method, paramNumber, op.getOutput().size(), "return");
                }
                ret = new QName(tns, local);
            } else {
                ret = new QName(tns, "return");
            }
            return ret;
        }        
    }

    @Override
    public Boolean isInParam(Method method, int j) {        
        if (j < 0) {
            return Boolean.FALSE;
        }
            
        method = getDeclaredMethod(method);
        
        WebParam webParam = getWebParam(method, j);

        return webParam == null || (webParam.mode().equals(Mode.IN) || webParam.mode().equals(Mode.INOUT));
    }

    private WebResult getWebResult(Method method) {
        return method.getAnnotation(WebResult.class);
    }
    
    @Override
    public QName getOutputMessageName(OperationInfo op, Method method) {
        Method m = getDeclaredMethod(method);
        ResponseWrapper rw = m.getAnnotation(ResponseWrapper.class);
        if (rw == null) {
            return null;
        }
        String nm = rw.targetNamespace();
        String lp = rw.localName();
        if (nm.length() > 0 && lp.length() > 0) {            
            return new QName(nm, lp); 
        } 
        return null;
    }
    
    @Override
    public QName getInputMessageName(OperationInfo op, Method method) {
        Method m = getDeclaredMethod(method);
        RequestWrapper rw = m.getAnnotation(RequestWrapper.class);
        if (rw == null) {
            return null;
        }
        String nm = rw.targetNamespace();
        String lp = rw.localName();
        if (nm.length() > 0 && lp.length() > 0) {            
            return new QName(nm, lp); 
        } 
        return null;        
    }

    @Override
    public Boolean isOutParam(Method method, int j) {
        method = getDeclaredMethod(method);
        if (j == -1) {
            return !method.getReturnType().equals(void.class);
        }

        WebParam webParam = getWebParam(method, j);

        if (webParam != null && (webParam.mode().equals(Mode.OUT) || webParam.mode().equals(Mode.INOUT))) {
            return Boolean.TRUE;
        }
        
        return method.getParameterTypes()[j] == Holder.class;
    }

    @Override
    public Class getResponseWrapper(Method selected) {
        Method m = getDeclaredMethod(selected);

        ResponseWrapper rw = m.getAnnotation(ResponseWrapper.class);
        if (rw == null) {
            return null;
        }

        String clsName = rw.className();
        if (clsName.length() > 0) {
            try {
                return ClassLoaderUtils.loadClass(clsName, getClass());
            } catch (ClassNotFoundException e) {
                throw new ServiceConstructionException(e);
            }
        }

        return null;
    }

    @Override
    public Class getRequestWrapper(Method selected) {
        Method m = getDeclaredMethod(selected);

        RequestWrapper rw = m.getAnnotation(RequestWrapper.class);
        if (rw == null) {
            return null;
        }

        String clsName = rw.className();

        if (clsName.length() > 0) {
            try {
                return ClassLoaderUtils.loadClass(clsName, getClass());
            } catch (ClassNotFoundException e) {
                throw new ServiceConstructionException(e);
            }
        }

        return null;
    }

    @Override
    public QName getFaultName(InterfaceInfo service, OperationInfo o, Class<?> exClass, Class<?> beanClass) {
        WebFault fault = exClass.getAnnotation(WebFault.class);
        if (fault != null) {
            String name = fault.name();
            if (name.length() == 0) {
                name = exClass.getSimpleName();
            }
            String ns = fault.targetNamespace();
            if (ns.length() == 0) {
                ns = service.getName().getNamespaceURI();
            }

            return new QName(ns, name);
        }
        return null;
    }

    @Override
    public Boolean isWrapped(Method m) {
        // see if someone overrode the default value
        if (!getServiceFactory().isWrapped()) {
            return Boolean.FALSE;
        }
        m = getDeclaredMethod(m);

        SOAPBinding ann = m.getAnnotation(SOAPBinding.class);
        if (ann != null) {
            if (ann.style().equals(Style.RPC)) {        
                throw new Fault(new RuntimeException("Method [" 
                                                     + m.getName() 
                                                     + "] processing error: " 
                                                     + "SOAPBinding can not on method with RPC style"));
            }
            return !(ann.parameterStyle().equals(ParameterStyle.BARE));
        }

        return isWrapped();
    }
    
    @Override
    public Boolean isWrapped() {
        SOAPBinding ann = implInfo.getEndpointClass().getAnnotation(SOAPBinding.class);
        if (ann != null) {
            return !(ann.parameterStyle().equals(ParameterStyle.BARE) || ann.style().equals(Style.RPC));
        }
        return null;
    }

    @Override
    public Boolean isHeader(Method method, int j) {
        method = getDeclaredMethod(method);
        if (j >= 0) {
            WebParam webParam = getWebParam(method, j);
            return webParam != null && webParam.header();
        } else {
            WebResult webResult = getWebResult(method);
            return webResult != null && webResult.header();
        }
    }

    @Override
    public String getStyle() {
        SOAPBinding ann = implInfo.getEndpointClass().getAnnotation(SOAPBinding.class);
        if (ann != null) {
            return ann.style().toString().toLowerCase();
        }
        return "document";
    }
    
}
