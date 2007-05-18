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

//import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Logger;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamReader;

import org.apache.cxf.common.i18n.BundleUtils;
import org.apache.cxf.databinding.DataReader;
import org.apache.cxf.endpoint.Endpoint;
import org.apache.cxf.message.Exchange;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.Phase;
import org.apache.cxf.service.model.BindingMessageInfo;
import org.apache.cxf.service.model.BindingOperationInfo;
import org.apache.cxf.service.model.EndpointInfo;
import org.apache.cxf.service.model.MessageInfo;
import org.apache.cxf.service.model.MessagePartInfo;
import org.apache.cxf.service.model.OperationInfo;
import org.apache.cxf.service.model.ServiceInfo;
import org.apache.cxf.staxutils.DepthXMLStreamReader;
import org.apache.cxf.staxutils.StaxUtils;

public class DocLiteralInInterceptor extends AbstractInDatabindingInterceptor {
    private static final Logger LOG = Logger.getLogger(DocLiteralInInterceptor.class.getName());
    private static final ResourceBundle BUNDLE = BundleUtils.getBundle(DocLiteralInInterceptor.class);

    public DocLiteralInInterceptor() {
        super();
        setPhase(Phase.UNMARSHAL);
        addAfter(URIMappingInterceptor.class.getName());
    }

    public void handleMessage(Message message) {
        if (isGET(message) && message.getContent(List.class) != null) {
            LOG.info("DocLiteralInInterceptor skipped in HTTP GET method");
            return;
        }

        DepthXMLStreamReader xmlReader = getXMLStreamReader(message);
        DataReader<XMLStreamReader> dr = getDataReader(message);
        List<Object> parameters = new ArrayList<Object>();

        Exchange exchange = message.getExchange();
        BindingOperationInfo bop = exchange.get(BindingOperationInfo.class);

        boolean client = isRequestor(message);

        //if body is empty and we have BindingOperationInfo, we do not need to match 
        //operation anymore, just return
        if (!StaxUtils.toNextElement(xmlReader) && bop != null) {
            // body may be empty for partial response to decoupled request
            return;
        }

        //bop might be a unwrpped, wrap it back so that we can get correct info 
        if (bop != null && bop.isUnwrapped()) {
            bop = bop.getWrappedOperation();
        }

        if (bop == null) {
            QName startQName = xmlReader.getName();
            bop = getBindingOperationInfo(exchange, startQName, client);
        }

        if (bop != null && bop.isUnwrappedCapable()) {
            ServiceInfo si = bop.getBinding().getService();
            // Wrapped case
            MessageInfo msgInfo = setMessage(message, bop, client, si);

            // Determine if there is a wrapper class
            if (msgInfo.getMessageParts().get(0).getTypeClass() != null) {
                Object wrappedObject = dr.read(msgInfo.getMessageParts().get(0), xmlReader);
                parameters.add(wrappedObject);

            } else {
                // Unwrap each part individually if we don't have a wrapper

                bop = bop.getUnwrappedOperation();

                msgInfo = setMessage(message, bop, client, si);
                List<MessagePartInfo> messageParts = msgInfo.getMessageParts();
                Iterator<MessagePartInfo> itr = messageParts.iterator();

                // advance just past the wrapped element so we don't get
                // stuck
                if (xmlReader.getEventType() == XMLStreamConstants.START_ELEMENT) {
                    StaxUtils.nextEvent(xmlReader);
                }

                // loop through each child element
                getPara(xmlReader, dr, parameters, itr);
            }

        } else {
            //Bare style
            BindingMessageInfo msgInfo = null;

            if (bop != null) { //for xml binding or client side
                getMessageInfo(message, bop);
                if (client) {
                    msgInfo = bop.getOutput();
                } else {
                    msgInfo = bop.getInput();
                }
            }

            Collection<OperationInfo> operations = null;
            operations = new ArrayList<OperationInfo>();
            Endpoint ep = exchange.get(Endpoint.class);
            ServiceInfo si = ep.getEndpointInfo().getService();
            operations.addAll(si.getInterface().getOperations());

            if (!StaxUtils.toNextElement(xmlReader)) {
                // empty input

                // TO DO : check duplicate operation with no input
                for (OperationInfo op : operations) {
                    MessageInfo bmsg = op.getInput();
                    if (bmsg.getMessageParts().size() == 0) {
                        BindingOperationInfo boi = ep.getEndpointInfo().getBinding().getOperation(op);
                        exchange.put(BindingOperationInfo.class, boi);
                        exchange.put(OperationInfo.class, op);
                        exchange.setOneWay(op.isOneWay());
                    }
                }
                return;
            }

            int paramNum = 0;

            do {
                QName elName = xmlReader.getName();
                Object o = null;

                MessagePartInfo p;
                if (!client && msgInfo != null && msgInfo.getMessageParts() != null 
                    && msgInfo.getMessageParts().size() == 0) {
                    //no input messagePartInfo
                    return;
                }
                if (msgInfo != null && msgInfo.getMessageParts() != null 
                    && msgInfo.getMessageParts().size() > 0) {
                    assert msgInfo.getMessageParts().size() > paramNum;
                    p = msgInfo.getMessageParts().get(paramNum);
                } else {
                    p = findMessagePart(exchange, operations, elName, client, paramNum);
                }

                if (p == null) {
                    throw new Fault(new org.apache.cxf.common.i18n.Message("NO_PART_FOUND", BUNDLE, elName),
                                    Fault.FAULT_CODE_CLIENT);
                }

                o = dr.read(p, xmlReader);

                if (o != null) {
                    if (p.getIndex() == -1) {
                        parameters.add(0, o);
                    } else {
                        parameters.add(o);
                    }
                    
                }
                paramNum++;
            } while (StaxUtils.toNextElement(xmlReader));

        }

        if (parameters.size() > 0) {
            message.setContent(List.class, parameters);
        }
    }
    
    private void getPara(DepthXMLStreamReader xmlReader,
                         DataReader<XMLStreamReader> dr,
                         List<Object> parameters,
                         Iterator<MessagePartInfo> itr) {

        boolean isListPara = false;
        //List<Object> list = new ArrayList<Object>();
        MessagePartInfo part = null;
        while (StaxUtils.toNextElement(xmlReader)) { 
            if (itr.hasNext()) {
                part = itr.next();
                if (part.getTypeClass().getName().startsWith("[L")) {
                    //&& Collection.class.isAssignableFrom(part.getTypeClass())) {
                    //it's List Para
                    //
                    Type genericType = (Type) part.getProperty("generic.type");
                    
                    if (genericType instanceof ParameterizedType) {
                        isListPara = true;
                        //ParameterizedType pt = (ParameterizedType) genericType;
                        //part.setTypeClass((Class<?>)pt.getActualTypeArguments()[0]);
                    } /*else if (genericType instanceof GenericArrayType) {
                        GenericArrayType gt = (GenericArrayType)genericType;
                        part.setTypeClass((Class<?>)gt.getGenericComponentType());
                    }*/
                } 
            } 
            if (part == null) {
                break;
            }
            Object obj = dr.read(part, xmlReader);
            if (isListPara) {
                List<Object> listArg = new ArrayList<Object>();
                for (Object o : (Object[])obj) {
                    listArg.add(o);
                }
                parameters.add(listArg);
            } else {
                parameters.add(obj);
            }

        }
        
        /*if (isListPara) {
            parameters.add(list);
        } else {
            for (Object obj : list) {
                parameters.add(obj);
            }
        }*/
    }


    private MessageInfo setMessage(Message message, BindingOperationInfo operation,
                                   boolean requestor, ServiceInfo si) {
        MessageInfo msgInfo = getMessageInfo(message, operation, requestor);
        message.put(MessageInfo.class, msgInfo);

        message.getExchange().put(BindingOperationInfo.class, operation);
        message.getExchange().put(OperationInfo.class, operation.getOperationInfo());
        message.getExchange().setOneWay(operation.getOperationInfo().isOneWay());

        //Set standard MessageContext properties required by JAX_WS, but not specific to JAX_WS.
        message.put(Message.WSDL_OPERATION, operation.getName());

        QName serviceQName = si.getName();
        message.put(Message.WSDL_SERVICE, serviceQName);

        QName interfaceQName = si.getInterface().getName();
        message.put(Message.WSDL_INTERFACE, interfaceQName);

        EndpointInfo endpointInfo = message.getExchange().get(Endpoint.class).getEndpointInfo();
        QName portQName = endpointInfo.getName();
        message.put(Message.WSDL_PORT, portQName);

        String address = endpointInfo.getAddress();
        URI wsdlDescription = null;
        try {
            wsdlDescription = new URI(address + "?wsdl");
        } catch (URISyntaxException e) {
            //do nothing
        }
        message.put(Message.WSDL_DESCRIPTION, wsdlDescription);

        return msgInfo;
    }

}
