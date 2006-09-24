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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.stream.XMLStreamReader;

import org.w3c.dom.Node;

import org.apache.cxf.common.i18n.BundleUtils;
import org.apache.cxf.databinding.DataReader;
import org.apache.cxf.databinding.DataReaderFactory;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.service.Service;
import org.apache.cxf.service.model.MessagePartInfo;
import org.apache.cxf.service.model.OperationInfo;
import org.apache.cxf.service.model.ServiceModelUtil;
import org.apache.cxf.staxutils.DepthXMLStreamReader;

public abstract class AbstractInDatabindingInterceptor extends AbstractPhaseInterceptor<Message> {
    private static final ResourceBundle BUNDLE = BundleUtils
        .getBundle(AbstractInDatabindingInterceptor.class);

    protected boolean isRequestor(Message message) {
        return Boolean.TRUE.equals(message.containsKey(Message.REQUESTOR_ROLE));
    }

    protected DataReader getDataReader(Message message, Class<?> input) {
        Service service = ServiceModelUtil.getService(message.getExchange());
        DataReaderFactory factory = service.getDataBinding().getDataReaderFactory();

        DataReader dataReader = null;
        for (Class<?> cls : factory.getSupportedFormats()) {
            if (cls == input) {
                dataReader = factory.createReader(input);
                break;
            }
        }
        if (dataReader == null) {
            throw new Fault(new org.apache.cxf.common.i18n.Message("NO_DATAREADER", BUNDLE, 
                service.getName()));
        }
        return dataReader;        
    }

    protected DataReader<Message> getMessageDataReader(Message message) {
        Service service = ServiceModelUtil.getService(message.getExchange());
        DataReaderFactory factory = service.getDataBinding().getDataReaderFactory();

        DataReader<Message> dataReader = null;
        for (Class<?> cls : factory.getSupportedFormats()) {
            if (cls == Message.class) {
                dataReader = factory.createReader(Message.class);
                break;
            }
        }
        if (dataReader == null) {
            throw new Fault(new org.apache.cxf.common.i18n.Message("NO_DATAREADER", BUNDLE, 
                service.getName()));
        }
        return dataReader;
    }

    protected DataReader<XMLStreamReader> getDataReader(Message message) {
        Service service = ServiceModelUtil.getService(message.getExchange());
        DataReaderFactory factory = service.getDataBinding().getDataReaderFactory();

        DataReader<XMLStreamReader> dataReader = null;
        for (Class<?> cls : factory.getSupportedFormats()) {
            if (cls == XMLStreamReader.class) {
                dataReader = factory.createReader(XMLStreamReader.class);
                break;
            }
        }
        if (dataReader == null) {
            throw new Fault(new org.apache.cxf.common.i18n.Message("NO_DATAREADER", BUNDLE, 
                service.getName()));
        }
        return dataReader;
    }
    
    protected DataReader<Node> getNodeDataReader(Message message) {
        Service service = ServiceModelUtil.getService(message.getExchange());
        DataReaderFactory factory = service.getDataBinding().getDataReaderFactory();

        DataReader<Node> dataReader = null;
        for (Class<?> cls : factory.getSupportedFormats()) {
            if (cls == Node.class) {
                dataReader = factory.createReader(Node.class);
                break;
            }
        }
        if (dataReader == null) {
            throw new Fault(new org.apache.cxf.common.i18n.Message("NO_DATAREADER", BUNDLE, 
                service.getName()));
        }
        return dataReader;
    }

    protected DepthXMLStreamReader getXMLStreamReader(Message message) {
        XMLStreamReader xr = message.getContent(XMLStreamReader.class);
        return new DepthXMLStreamReader(xr);
    }

    protected OperationInfo findOperation(Collection<OperationInfo> operations, 
                                          List<Object> parameters, boolean isRequestor) {
        // first check for exact matches
        for (OperationInfo o : operations) {
            List messageParts = null;
            if (isRequestor) {
                if (o.hasOutput()) {
                    messageParts = o.getOutput().getMessageParts();
                } else {
                    messageParts = new ArrayList();
                }
            } else {
                if (o.hasInput()) {
                    messageParts = o.getInput().getMessageParts();
                } else {
                    messageParts = new ArrayList();
                }
            }
            if (messageParts.size() == parameters.size() 
                    && checkExactParameters(messageParts, parameters)) {
                return o;
                
            }
        }

//        // now check for assignable matches
        /*for (OperationInfo o : operations) {
            List messageParts = o.getInput().getMessageParts();
            if (messageParts.size() == parameters.size()) {
                if (checkParameters(messageParts, parameters)) {
                    return o;
                }
            }
        }*/
        return null;
    }

      /**
       * Return true only if the message parts exactly match the classes of the
       * parameters
       * 
       * @param messageParts
       * @param parameters
       * @return
       */
    private boolean checkExactParameters(List messageParts, List parameters) {
        Iterator messagePartIterator = messageParts.iterator();
        for (Iterator parameterIterator = parameters.iterator(); parameterIterator.hasNext();) {
            Object param = parameterIterator.next();
            JAXBElement paramEl = null;
            MessagePartInfo mpi = (MessagePartInfo)messagePartIterator.next();
            if (param instanceof JAXBElement) {
                paramEl = (JAXBElement)param;
                if (!mpi.getElementQName().equals(paramEl.getName())) {
                    return false;
                }
            } else {
                
                if (!mpi.getElementQName().getLocalPart().equals(
                    param.getClass().getAnnotation(XmlRootElement.class).name())) {
               
                    return false;
                }
            }
        }
        return true;
    }

    /*private boolean checkParameters(List messageParts, List parameters) {
        Iterator messagePartIterator = messageParts.iterator();
        for (Iterator parameterIterator = parameters.iterator(); parameterIterator.hasNext();) {
            Object param = parameterIterator.next();
            MessagePartInfo mpi = (MessagePartInfo)messagePartIterator.next();

            if (!mpi.getTypeClass().isAssignableFrom(param.getClass())) {
                if (!param.getClass().isPrimitive() && mpi.getTypeClass().isPrimitive()) {
                    return checkPrimitiveMatch(mpi.getTypeClass(), param.getClass());
                } else {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean checkPrimitiveMatch(Class clazz, Class typeClass) {
        if ((typeClass == Integer.class && clazz == int.class)
            || (typeClass == Double.class && clazz == double.class)
            || (typeClass == Long.class && clazz == long.class)
            || (typeClass == Float.class && clazz == float.class)
            || (typeClass == Short.class && clazz == short.class)
            || (typeClass == Boolean.class && clazz == boolean.class)
            || (typeClass == Byte.class && clazz == byte.class)) {
            return true;
        }

        return false;
    }*/
}
