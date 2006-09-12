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

import java.util.ResourceBundle;

import javax.xml.stream.XMLStreamWriter;

import org.apache.cxf.common.i18n.BundleUtils;
import org.apache.cxf.databinding.DataWriter;
import org.apache.cxf.databinding.DataWriterFactory;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.service.Service;
import org.apache.cxf.service.model.ServiceModelUtil;

public abstract class AbstractOutDatabindingInterceptor extends AbstractPhaseInterceptor<Message> {
    private static final ResourceBundle BUNDLE = BundleUtils
        .getBundle(AbstractOutDatabindingInterceptor.class);

    protected boolean isRequestor(Message message) {
        return Boolean.TRUE.equals(message.containsKey(Message.REQUESTOR_ROLE));
    }
    
    protected DataWriter getDataWriter(Message message, Class<?> output) {
        Service service = ServiceModelUtil.getService(message.getExchange());
        DataWriterFactory factory = service.getDataWriterFactory();

        DataWriter dataWriter = null;
        for (Class<?> cls : factory.getSupportedFormats()) {
            if (cls == output) {
                dataWriter = factory.createWriter(output);
                break;
            }
        }

        if (dataWriter == null) {
            throw new Fault(new org.apache.cxf.common.i18n.Message("NO_DATAWRITER", BUNDLE, service
                .getName()));
        }

        return dataWriter;        
    }

    protected DataWriter<Message> getMessageDataWriter(Message message) {
        
        Service service = ServiceModelUtil.getService(message.getExchange());
        DataWriterFactory factory = service.getDataWriterFactory();

        DataWriter<Message> dataWriter = null;
        for (Class<?> cls : factory.getSupportedFormats()) {
            if (cls == Message.class) {
                dataWriter = factory.createWriter(Message.class);
                break;
            }
        }

        if (dataWriter == null) {
            throw new Fault(new org.apache.cxf.common.i18n.Message("NO_DATAWRITER", BUNDLE, service
                .getName()));
        }

        return dataWriter;
    }

    protected DataWriter<XMLStreamWriter> getDataWriter(Message message) {
        Service service = ServiceModelUtil.getService(message.getExchange());
        DataWriterFactory factory = service.getDataWriterFactory();

        DataWriter<XMLStreamWriter> dataWriter = null;
        for (Class<?> cls : factory.getSupportedFormats()) {
            if (cls == XMLStreamWriter.class) {
                dataWriter = factory.createWriter(XMLStreamWriter.class);
                break;
            }
        }

        if (dataWriter == null) {
            throw new Fault(new org.apache.cxf.common.i18n.Message("NO_DATAWRITER", BUNDLE, service
                .getName()));
        }

        return dataWriter;
    }

    protected XMLStreamWriter getXMLStreamWriter(Message message) {
        return message.getContent(XMLStreamWriter.class);
    }
}
