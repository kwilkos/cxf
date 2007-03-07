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
import javax.xml.stream.XMLStreamReader;
import javax.xml.validation.Schema;

import org.apache.cxf.aegis.Aegis;
import org.apache.cxf.aegis.Context;
import org.apache.cxf.aegis.DatabindingException;
import org.apache.cxf.aegis.type.Type;
import org.apache.cxf.aegis.xml.stax.ElementReader;
import org.apache.cxf.common.i18n.Message;
import org.apache.cxf.databinding.DataReader;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.message.Attachment;
import org.apache.cxf.service.model.MessagePartInfo;

public class XMLStreamDataReader implements DataReader<XMLStreamReader> {

    private static final Logger LOG = Logger.getLogger(XMLStreamDataReader.class.getName());

    private AegisDatabinding databinding;

    private Context context = new Context();
    
    public XMLStreamDataReader(AegisDatabinding databinding) {
        this.databinding = databinding;
    }

    public Object read(MessagePartInfo part, XMLStreamReader input) {
        Type type = databinding.getType(part);

        type = Aegis.getReadType(input, context, type);
        
        if (type == null) {
            throw new Fault(new Message("NO_MESSAGE_FOR_PART", LOG));
        }

         // I don't think this is the right type mapping
        context.setTypeMapping(type.getTypeMapping());

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

    public Object read(QName name, XMLStreamReader input, Class type) {
        // TODO Auto-generated method stub
        return null;
    }

    public Object read(XMLStreamReader input) {
        // TODO Auto-generated method stub
        return null;
    }

    public void setAttachments(Collection<Attachment> attachments) {
        context.setAttachments(attachments);
    }

    public void setProperty(String prop, Object value) {
        // TODO Auto-generated method stub

    }

    public void setSchema(Schema s) {
        // TODO Auto-generated method stub

    }

}
