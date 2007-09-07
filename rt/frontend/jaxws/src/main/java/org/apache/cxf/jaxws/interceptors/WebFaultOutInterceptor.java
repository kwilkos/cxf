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

package org.apache.cxf.jaxws.interceptors;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ResourceBundle;
import java.util.logging.Logger;

import javax.xml.namespace.QName;
import javax.xml.ws.WebFault;

import org.w3c.dom.Node;

import org.apache.cxf.common.i18n.BundleUtils;
import org.apache.cxf.common.logging.LogUtils;
import org.apache.cxf.common.util.StringUtils;
import org.apache.cxf.databinding.DataWriter;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.jaxws.support.JaxWsServiceConfiguration;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.Phase;
import org.apache.cxf.service.Service;
import org.apache.cxf.service.model.BindingOperationInfo;
import org.apache.cxf.service.model.FaultInfo;
import org.apache.cxf.service.model.MessagePartInfo;
import org.apache.cxf.service.model.OperationInfo;

public class WebFaultOutInterceptor extends AbstractPhaseInterceptor<Message> {

    private static final Logger LOG = LogUtils.getL7dLogger(WebFaultOutInterceptor.class);
    private static final ResourceBundle BUNDLE = BundleUtils.getBundle(JaxWsServiceConfiguration.class);

    public WebFaultOutInterceptor() {
        super(Phase.PRE_PROTOCOL);
    }
    
    private QName getFaultName(WebFault wf, Class<?> cls, OperationInfo op) {
        String ns = wf.targetNamespace();
        if (StringUtils.isEmpty(ns)) {
            ns = op.getName().getNamespaceURI();
        }
        String name = wf.name();
        if (StringUtils.isEmpty(name)) {
            name = cls.getSimpleName();
        }
        return new QName(ns, name);
    }
    

    private WebFault getWebFaultAnnotation(Class<?> t) {
        WebFault fault = t.getAnnotation(WebFault.class);
        if (fault == null
            && t.getSuperclass() != null
            && Throwable.class.isAssignableFrom(t.getSuperclass())) {
            fault = getWebFaultAnnotation(t.getSuperclass());
        }
        return fault;
    }
    
    public void handleMessage(Message message) throws Fault {
        Fault f = (Fault)message.getContent(Exception.class);

        Throwable cause = f.getCause();
        WebFault fault = null;
        if (cause != null) {
            fault = getWebFaultAnnotation(cause.getClass());
        }
        if (cause instanceof Exception && fault != null) {
            Exception ex = (Exception)cause;
            Object faultInfo = null;
            try {
                Method method = cause.getClass().getMethod("getFaultInfo", new Class[0]);
                faultInfo = method.invoke(cause, new Object[0]);
            } catch (NoSuchMethodException e) {
                faultInfo = cause;
                LOG.fine("Using @WebFault annotated class "
                        + faultInfo.getClass().getName() 
                        + " as faultInfo since getFaultInfo() was not found");
            } catch (InvocationTargetException e) {
                throw new Fault(new org.apache.cxf.common.i18n.Message("INVOCATION_TARGET_EXC", BUNDLE), e);
            } catch (IllegalAccessException e) {
                throw new Fault(new org.apache.cxf.common.i18n.Message("COULD_NOT_INVOKE", BUNDLE), e);
            } catch (IllegalArgumentException e) {
                throw new Fault(new org.apache.cxf.common.i18n.Message("COULD_NOT_INVOKE", BUNDLE), e);
            }
            Service service = message.getExchange().get(Service.class);

            DataWriter<Node> writer = service.getDataBinding().createWriter(Node.class);

            OperationInfo op = message.getExchange().get(BindingOperationInfo.class).getOperationInfo();
            QName faultName = getFaultName(fault, cause.getClass(), op);
            MessagePartInfo part = getFaultMessagePart(faultName, op);
            writer.write(faultInfo, part, f.getOrCreateDetail());

            f.setMessage(ex.getMessage());
        }
    }

    private MessagePartInfo getFaultMessagePart(QName qname, OperationInfo op) {
        for (FaultInfo faultInfo : op.getFaults()) {
            for (MessagePartInfo mpi : faultInfo.getMessageParts()) {
                String ns = null;
                if (mpi.isElement()) {
                    ns = mpi.getElementQName().getNamespaceURI();
                } else {
                    ns = mpi.getTypeQName().getNamespaceURI();
                }
                if (qname.getLocalPart().equals(mpi.getConcreteName().getLocalPart()) 
                        && qname.getNamespaceURI().equals(ns)) {
                    return mpi;
                }
            }
            
        }
        return null;
    }

}
