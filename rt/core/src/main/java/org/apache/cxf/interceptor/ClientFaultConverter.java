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
package org.apache.cxf.interceptor;

import java.lang.reflect.Constructor;

import javax.xml.namespace.QName;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import org.apache.cxf.databinding.DataBinding;
import org.apache.cxf.databinding.DataReader;
import org.apache.cxf.helpers.DOMUtils;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.Phase;
import org.apache.cxf.service.Service;
import org.apache.cxf.service.model.BindingOperationInfo;
import org.apache.cxf.service.model.FaultInfo;
import org.apache.cxf.service.model.MessagePartInfo;

/**
 * Takes a Fault and converts it to a local exception type if possible.
 * 
 * @author Dan Diephouse
 */
public class ClientFaultConverter extends AbstractPhaseInterceptor<Message> {

    public ClientFaultConverter() {
        super();
        setPhase(Phase.UNMARSHAL);
    }

    public void handleMessage(Message msg) {
        Fault fault = (Fault) msg.getContent(Exception.class);

        if (fault.getDetail() != null) {
            processFaultDetail(fault, msg);
        }
    }

    protected void processFaultDetail(Fault fault, Message msg) {
        Element exDetail = (Element) DOMUtils.getChild(fault.getDetail(), Node.ELEMENT_NODE);
        QName qname = new QName(exDetail.getNamespaceURI(), exDetail.getLocalName());
        FaultInfo faultWanted = null;        
        Class cls = null;
        BindingOperationInfo boi = msg.getExchange().get(BindingOperationInfo.class);
        for (FaultInfo faultInfo : boi.getOperationInfo().getFaults()) {
            for (MessagePartInfo mpi : faultInfo.getMessageParts()) {
                String ns = null;
                if (mpi.isElement()) {
                    ns = mpi.getElementQName().getNamespaceURI();
                } else {
                    ns = mpi.getTypeQName().getNamespaceURI();
                }
                if (qname.getLocalPart().equals(mpi.getConcreteName().getLocalPart()) 
                        && qname.getNamespaceURI().equals(ns)) {
                    cls = (Class)mpi.getProperty(Class.class.getName());
                    faultWanted = faultInfo;
                    break;
                }
            }
            if (faultWanted != null) {
                break;
            }
        }

        if (faultWanted == null) {
            return;
        }

        Service s = msg.getExchange().get(Service.class);
        DataBinding dataBinding = s.getDataBinding();

        DataReader<Node> reader = dataBinding.getDataReaderFactory().createReader(Node.class);
        Object e = reader.read(qname, exDetail, cls);

        if (!(e instanceof Exception)) {
            Class exClass = faultWanted.getProperty(Class.class.getName(), Class.class);
            Class beanClass = e.getClass();

            try {
                Constructor constructor = exClass.getConstructor(new Class[]{String.class, beanClass});
                e = constructor.newInstance(new Object[]{fault.getMessage(), e});
            } catch (Exception e1) {
                throw new Fault(e1);
            }
        }

        msg.setContent(Exception.class, e);
    }

}
