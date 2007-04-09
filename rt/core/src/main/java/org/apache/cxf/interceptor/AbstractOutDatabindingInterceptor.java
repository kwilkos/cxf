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

import javax.xml.stream.XMLStreamWriter;
import javax.xml.validation.Schema;

import org.apache.cxf.databinding.DataWriter;
import org.apache.cxf.message.Attachment;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.service.Service;
import org.apache.cxf.service.model.ServiceModelUtil;
import org.apache.cxf.wsdl.EndpointReferenceUtils;

public abstract class AbstractOutDatabindingInterceptor extends AbstractPhaseInterceptor<Message> {

    protected boolean isRequestor(Message message) {
        return Boolean.TRUE.equals(message.containsKey(Message.REQUESTOR_ROLE));
    }
    
    protected <T> DataWriter<T> getDataWriter(Message message, Class<T> output) {
        Service service = ServiceModelUtil.getService(message.getExchange());
        DataWriter<T> writer = service.getDataBinding().createWriter(output);
        
        if (Boolean.TRUE.equals(message.getContextualProperty(Message.MTOM_ENABLED))) {
            Collection<Attachment> atts = message.getAttachments();
            if (atts == null) {
                atts = new ArrayList<Attachment>();
                message.setAttachments(atts);
            }
            writer.setAttachments(atts);
        }
        
        setSchemaOutMessage(service, message, writer);
        return writer;
    }

    private void setSchemaOutMessage(Service service, Message message, DataWriter<?> writer) {
        Object en = message.getContextualProperty(Message.SCHEMA_VALIDATION_ENABLED);
        if (Boolean.TRUE.equals(en) || "true".equals(en)) {
            Schema schema = EndpointReferenceUtils.getSchema(service.getServiceInfo());
            writer.setSchema(schema);
        }
    }

    protected XMLStreamWriter getXMLStreamWriter(Message message) {
        return message.getContent(XMLStreamWriter.class);
    }
}
