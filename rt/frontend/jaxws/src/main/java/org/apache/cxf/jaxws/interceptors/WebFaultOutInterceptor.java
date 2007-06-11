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

import javax.xml.namespace.QName;
import javax.xml.ws.WebFault;

import org.w3c.dom.Node;

import org.apache.cxf.common.i18n.BundleUtils;
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
    private static final ResourceBundle BUNDLE = BundleUtils.getBundle(JaxWsServiceConfiguration.class);

    public WebFaultOutInterceptor() {
        super(Phase.PRE_PROTOCOL);
    }

    public void handleMessage(Message message) throws Fault {
        Fault f = (Fault)message.getContent(Exception.class);

        Throwable cause = f.getCause();
        if (cause instanceof Exception && cause.getClass().isAnnotationPresent(WebFault.class)) {
            Exception ex = (Exception)cause;
            try {
                Method method = cause.getClass().getMethod("getFaultInfo", new Class[0]);
                Object faultInfo = method.invoke(cause, new Object[0]);

                Service service = message.getExchange().get(Service.class);

                DataWriter<Node> writer = service.getDataBinding().createWriter(Node.class);

                OperationInfo op = message.getExchange().get(BindingOperationInfo.class).getOperationInfo();
                QName faultName = getFaultName(ex);
                MessagePartInfo part = getFaultMessagePart(faultName, op);
                writer.write(faultInfo, part, f.getOrCreateDetail());

                f.setMessage(ex.getMessage());
            } catch (InvocationTargetException e) {
                throw new Fault(new org.apache.cxf.common.i18n.Message("INVOCATION_TARGET_EXC", BUNDLE), e);
            } catch (NoSuchMethodException e) {
                throw new Fault(new org.apache.cxf.common.i18n.Message("NO_GETFAULTINFO_METHOD", BUNDLE), e);
            } catch (IllegalArgumentException e) {
                throw new Fault(new org.apache.cxf.common.i18n.Message("COULD_NOT_INVOKE", BUNDLE), e);
            } catch (IllegalAccessException e) {
                throw new Fault(new org.apache.cxf.common.i18n.Message("COULD_NOT_INVOKE", BUNDLE), e);
            }
        }
    }

    private QName getFaultName(Exception webFault) {
        WebFault wf = webFault.getClass().getAnnotation(WebFault.class);

        return new QName(wf.targetNamespace(), wf.name());
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
