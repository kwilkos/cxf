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

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.xml.ws.Holder;

import org.apache.cxf.helpers.CastUtils;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.message.Exchange;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.Phase;
import org.apache.cxf.service.factory.ReflectionServiceFactoryBean;
import org.apache.cxf.service.model.MessagePartInfo;
import org.apache.cxf.service.model.OperationInfo;


public class HolderOutInterceptor extends AbstractPhaseInterceptor<Message> {

    private static final Logger LOG = Logger.getLogger(HolderOutInterceptor.class.getName());

    public HolderOutInterceptor() {
        super(Phase.PRE_LOGICAL);
        addBefore(WrapperClassOutInterceptor.class.getName());
    }

    public void handleMessage(Message message) throws Fault {
        List<Object> outObjects = CastUtils.cast(message.getContent(List.class));
        Exchange exchange = message.getExchange();
        OperationInfo op = exchange.get(OperationInfo.class);
        
        LOG.fine("op: " + op);
        if (null != op) {
            LOG.fine("op.hasOutput(): " + op.hasOutput());
            if (op.hasOutput()) {
                LOG.fine("op.getOutput().size(): " + op.getOutput().size());
            }
        }

        if (op == null || !op.hasOutput() || op.getOutput().size() == 0) {
            LOG.fine("Returning.");
            return;
        }

        List<MessagePartInfo> parts = op.getOutput().getMessageParts();
        LOG.fine("output message parts: " + parts);
        
        // is this a client invocation?
        if (Boolean.TRUE.equals(message.get(Message.REQUESTOR_ROLE))) {
            LOG.fine("client invocation");
            // Extract the Holders and store them for later
            List<Holder> holders = new ArrayList<Holder>();
            //int size = op.getInput().size() + op.getOutput().size();
            int size = outObjects.size();
            List<Object> newObjects = new ArrayList<Object>(size);
            for (int i = 0; i < size; i++) {
                newObjects.add(null);
            }

            Object deleteMarker = markOutHolderInParaList(outObjects, parts, holders, newObjects);
            
            if (holders.size() == 0) {
                return;
            }
            
            int i = 0;
            for (MessagePartInfo part : op.getInput().getMessageParts()) {
                List<String> ordering = part.getMessageInfo().getOperation().getParameterOrdering();
                if (ordering != null && ordering.size() > 0) {
                    int orderIdx = -1;
                    for (int j = 0; j < ordering.size(); j++) {
                        if (ordering.get(j).equals(part.getName().getLocalPart())) {
                            orderIdx = j;
                            break;
                        }
                    }
                    if (orderIdx != -1) {                        
                        newObjects.set(part.getIndex(), getValue(part, outObjects, orderIdx));
                        
                    } else {
                        newObjects.set(part.getIndex(), getValue(part, outObjects, i));
                    }                    
                } else {
                    newObjects.set(part.getIndex(), getValue(part, outObjects, i));
                }
                i++;
            }

            if (deleteMarker != null) {
                // regenerate the param list by removing all params marked with the deleteMarker.
                Object[] newObjectsArray = newObjects.toArray();
                newObjects.clear();
                for (Object param : newObjectsArray) {
                    if (param != deleteMarker) {
                        newObjects.add(param);
                    }
                }
            }

            message.setContent(List.class, newObjects);
            exchange.put(HolderInInterceptor.CLIENT_HOLDERS, holders);
        } else {
            // Add necessary holders so we match the method signature of the service class
            List<Object> reqObjects =
                CastUtils.cast(exchange.getInMessage().getContent(List.class));
    
            int outIdx = 0;
            boolean holderOutIsFirst = false;
            for (MessagePartInfo part : parts) {
                if (part.getIndex() == -1) {
                    outIdx++;
                    break;
                } else {
                    holderOutIsFirst = true;
                }
            }
            
            for (MessagePartInfo part : parts) {
                int methodIdx = part.getIndex();
                if (methodIdx >= 0) {
                    Holder holder = (Holder) reqObjects.get(methodIdx);
                    Object o = holder.value;
                    if (methodIdx < outIdx && holderOutIsFirst) {
                        //Holder is first part of mesage
                        outObjects.add(methodIdx, o);
                    } else if (outIdx >= outObjects.size()) {
                        outObjects.add(o);
                    } else {
                        outObjects.add(outIdx, o);
                    }
                    outIdx++;
                }
            }
            message.setContent(List.class, outObjects);
        }
    }

    private Object getValue(MessagePartInfo part, List<Object> outObjects, int idx) {
        if (part.getProperty(ReflectionServiceFactoryBean.MODE_INOUT) != null) {
            Holder holder = (Holder) outObjects.get(idx);
            if (holder == null) {
                return null;
            } else {
                return holder.value;
            }
        } else {
            return outObjects.get(idx);
        }
    }

    private Object markOutHolderInParaList(List<Object> outObjects, 
                                           List<MessagePartInfo> parts, 
                                           List<Holder> holders, 
                                           List<Object> newObjects) {
        Object deleteObject = null;
        for (MessagePartInfo part : parts) {
            int idx = part.getIndex();
            if (idx >= 0) {
                Holder holder = (Holder) outObjects.get(idx);

                if (part.getProperty(ReflectionServiceFactoryBean.MODE_OUT) != null) {
                    if (deleteObject == null) {
                        deleteObject = new Object();
                    }
                    newObjects.set(idx, deleteObject);
                }
                
                holders.add(holder);
            }
        }
        return deleteObject;
    }
    
}
