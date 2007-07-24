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

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.message.Exchange;
import org.apache.cxf.message.Message;
import org.apache.cxf.message.MessageContentsList;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.Phase;
import org.apache.cxf.service.model.BindingMessageInfo;
import org.apache.cxf.service.model.BindingOperationInfo;
import org.apache.cxf.service.model.MessageInfo;
import org.apache.cxf.service.model.MessagePartInfo;
import org.apache.cxf.service.model.OperationInfo;

public class WrapperClassInInterceptor extends AbstractPhaseInterceptor<Message> {

    private static final Logger LOG = Logger.getLogger(WrapperClassInInterceptor.class.getName());
    
    public WrapperClassInInterceptor() {
        super(Phase.POST_LOGICAL);
    }

    public void handleMessage(Message message) throws Fault {
        Exchange ex = message.getExchange();
        BindingOperationInfo boi = ex.get(BindingOperationInfo.class);
        if (boi == null) {
            return;
        }
               
        Method method = ex.get(Method.class);

        if (method != null && method.getName().endsWith("Async")) {
            Class<?> retType = method.getReturnType();
            if (retType.getName().equals("java.util.concurrent.Future") 
                || retType.getName().equals("javax.xml.ws.Response")) {
                return;
            }
        }        

        
        if (boi != null && boi.isUnwrappedCapable()) {
            BindingOperationInfo boi2 = boi.getUnwrappedOperation();
            OperationInfo op = boi2.getOperationInfo();
            BindingMessageInfo bmi;
            
            MessageInfo wrappedMessageInfo = message.get(MessageInfo.class);
            MessageInfo messageInfo;
            if (wrappedMessageInfo == boi.getOperationInfo().getInput()) {
                messageInfo = op.getInput();
                bmi = boi2.getInput();
            } else {
                messageInfo = op.getOutput();
                bmi = boi2.getOutput();
            }
            
            // Sometimes, an operation can be unwrapped according to WSDLServiceFactory,
            // but not according to JAX-WS. We should unify these at some point, but
            // for now check for the wrapper class.
            MessageContentsList lst = MessageContentsList.getContentsList(message);
            if (lst == null) {
                return;
            }
            Class<?> wrapperClass = null;
            Object wrappedObject = null;
            MessagePartInfo wrapperPart = null;
            if (wrappedMessageInfo != null) {
                for (MessagePartInfo part : wrappedMessageInfo.getMessageParts()) {
                    //headers should appear in both, find the part that doesn't
                    if (messageInfo.getMessagePart(part.getName()) == null) {
                        wrapperClass = part.getTypeClass();
                        for (Object o : lst) {
                            if (wrapperClass.isInstance(o)) {
                                wrappedObject = o;
                                wrapperPart = part;
                                break;
                            }
                        }
                        break;
                    }
                }
            }
            
            if (lst != null) {
                message.put(MessageInfo.class, messageInfo);
                message.put(BindingMessageInfo.class, bmi);
                ex.put(BindingOperationInfo.class, boi2);
                ex.put(OperationInfo.class, op);
            }
            if (isGET(message)) {
                LOG.info("WrapperClassInInterceptor skipped in HTTP GET method");
                return;
            }
            if (wrapperClass == null || wrappedObject == null) {
                return;
            }
            
            WrapperHelper helper = wrapperPart.getProperty("WRAPPER_CLASS", WrapperHelper.class);
            if (helper == null) {
                helper = createWrapperHelper(messageInfo, wrappedMessageInfo, wrapperClass);
                wrapperPart.setProperty("WRAPPER_CLASS", helper);
            }            
            
            MessageContentsList newParams;
            try {
                newParams = new MessageContentsList(helper.getWrapperParts(wrappedObject));
                
                for (MessagePartInfo part : messageInfo.getMessageParts()) {
                    if (wrappedMessageInfo.getMessagePart(part.getName()) != null) {
                        newParams.put(part, lst.get(part));
                    }
                }
            } catch (Exception e) {
                throw new Fault(e);
            }
            
            message.setContent(List.class, newParams);
            message.setContent(MessageContentsList.class, newParams);
        }
    }
    
    private WrapperHelper createWrapperHelper(MessageInfo messageInfo,
                                              MessageInfo wrappedMessageInfo,
                                              Class<?> wrapperClass) {
        List<String> partNames = new ArrayList<String>();
        List<String> elTypeNames = new ArrayList<String>();
        List<Class<?>> partClasses = new ArrayList<Class<?>>();
        
        for (MessagePartInfo p : messageInfo.getMessageParts()) {
            if (wrappedMessageInfo.getMessagePart(p.getName()) != null) {
                int idx = p.getIndex();
                ensureSize(elTypeNames, idx);
                ensureSize(partClasses, idx);
                ensureSize(partNames, idx);
                elTypeNames.set(idx, null);
                partClasses.set(idx, null);
                partNames.set(idx, null);
            } else {
                String elementType = null;
                if (p.isElement()) {
                    elementType = p.getElementQName().getLocalPart();
                } else {
                    if (p.getTypeQName() == null) {
                        // handling anonymous complex type
                        elementType = null;
                    } else {
                        elementType = p.getTypeQName().getLocalPart();
                    }
                }
                int idx = p.getIndex();
                ensureSize(elTypeNames, idx);
                ensureSize(partClasses, idx);
                ensureSize(partNames, idx);
                
                elTypeNames.set(idx, elementType);
                partClasses.set(idx, p.getTypeClass());
                partNames.set(idx, p.getName().getLocalPart());
            }
        }
        return WrapperHelper.createWrapperHelper(wrapperClass,
                                                  partNames,
                                                  elTypeNames,
                                                  partClasses);
    }
    private void ensureSize(List<?> lst, int idx) {
        while (idx >= lst.size()) {
            lst.add(null);
        }
    }
}
