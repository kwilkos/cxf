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
package org.apache.cxf.aegis.databinding;

import java.util.Collection;
import java.util.logging.Logger;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.validation.Schema;

import org.apache.cxf.aegis.Context;
import org.apache.cxf.aegis.DatabindingException;
import org.apache.cxf.aegis.type.Type;
import org.apache.cxf.aegis.type.TypeUtil;
import org.apache.cxf.aegis.xml.stax.ElementReader;
import org.apache.cxf.common.i18n.Message;
import org.apache.cxf.common.logging.LogUtils;
import org.apache.cxf.common.util.SOAPConstants;
import org.apache.cxf.databinding.DataReader;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.message.Attachment;
import org.apache.cxf.service.model.MessagePartInfo;

public class XMLStreamDataReader implements DataReader<XMLStreamReader> {

    private static final Logger LOG = LogUtils.getL7dLogger(XMLStreamDataReader.class);

    private AegisDatabinding databinding;

    private Context context;

    public XMLStreamDataReader(AegisDatabinding databinding) {
        this.databinding = databinding;
        this.context = new Context(databinding.getAegisContext());
    }

    public Object read(MessagePartInfo part, XMLStreamReader input) {

        Type type = databinding.getType(part);

        type = TypeUtil.getReadType(input, databinding.getAegisContext(), type);
        
        if (type == null) {
            throw new Fault(new Message("NO_MESSAGE_FOR_PART", LOG));
        }
        
        ElementReader elReader = new ElementReader(input);
        if (elReader.isXsiNil()) {
            elReader.readToEnd();
            return null;
        }

        try {
            return type.readObject(elReader, context);
        } catch (DatabindingException e) {
            throw new RuntimeException(e);
        }
    }

    private void setupReaderPosition(XMLStreamReader reader) {
        if (reader.getEventType() == XMLStreamConstants.START_DOCUMENT) {
            while (XMLStreamConstants.START_ELEMENT != reader.getEventType()) {
                try {
                    reader.nextTag();
                } catch (XMLStreamException e) {
                    Message message = new Message("STREAM_READ_ERROR", LOG);
                    throw new DatabindingException(message.toString(), e);
                }
            }
        }
        if (reader.getEventType() != XMLStreamConstants.START_ELEMENT) {
            Message message = new Message("STREAM_BAD_POSITION", LOG);
            throw new DatabindingException(message.toString());
            
        }
    }

    public Object read(QName name, XMLStreamReader reader, Class typeClass) {
        setupReaderPosition(reader);
        ElementReader elReader = new ElementReader(reader);

        if (elReader.isXsiNil()) {
            elReader.readToEnd();
            return null;
        }
        
        
        Type type = TypeUtil.getReadType(reader, context.getGlobalContext(), null);
        
        if (type == null) {
            return null; // throw ?
        }
        

        try {
            return type.readObject(elReader, context);
        } catch (DatabindingException e) {
            throw new RuntimeException(e);
        }

    }

    public Object read(XMLStreamReader reader) {
        setupReaderPosition(reader);
        ElementReader elReader = new ElementReader(reader);

        if (elReader.isXsiNil()) {
            elReader.readToEnd();
            return null;
        }
        
        
        Type type = TypeUtil.getReadType(reader, context.getGlobalContext(), null);
        
        if (type == null) {
            return null; // throw ?
        }
        

        try {
            return type.readObject(elReader, context);
        } catch (DatabindingException e) {
            throw new RuntimeException(e);
        }
    }

    public void setAttachments(Collection<Attachment> attachments) {
        context.setAttachments(attachments);
    }

    public void setProperty(String prop, Object value) {
        if (SOAPConstants.MTOM_ENABLED.equals(prop)) {
            if (value instanceof String) {
                context.setMtomEnabled(Boolean.valueOf((String)value));
            } else if (value instanceof Boolean) {
                context.setMtomEnabled((Boolean)value);
            }
        } else if (DataReader.FAULT.equals(prop)) { 
            context.setFault((Fault)value);
        }
    }

    public void setSchema(Schema s) {
    }

}
