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

package org.apache.cxf.jaxb;

import javax.xml.soap.SOAPBody;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLStreamWriter;

import org.w3c.dom.Node;

import org.apache.cxf.databinding.DataWriter;
import org.apache.cxf.databinding.DataWriterFactory;
import org.apache.cxf.jaxb.io.EventDataWriter;
import org.apache.cxf.jaxb.io.MessageDataWriter;
import org.apache.cxf.jaxb.io.NodeDataWriter;
import org.apache.cxf.jaxb.io.SOAPBodyDataWriter;
import org.apache.cxf.jaxb.io.XMLStreamDataWriter;
import org.apache.cxf.message.Message;

public final class JAXBDataWriterFactory extends JAXBDataFactoryBase implements DataWriterFactory {
    private static final Class<?> SUPPORTED_FORMATS[] = new Class<?>[] {Node.class,
                                                                        Message.class,
                                                                        XMLEventWriter.class,
                                                                        XMLStreamWriter.class,
                                                                        SOAPBody.class};
    
    public JAXBDataWriterFactory() {
        
    }

    
    @SuppressWarnings("unchecked")
    public <T> DataWriter<T> createWriter(Class<T> cls) {
        
        if (cls == XMLStreamWriter.class) {
            return (DataWriter<T>)new XMLStreamDataWriter(this);
        } else if (cls == XMLEventWriter.class) {
            return (DataWriter<T>)new EventDataWriter(this);            
        } else if (cls == Message.class) {
            return (DataWriter<T>)new MessageDataWriter(this);
        } else if (cls == Node.class) {
            return (DataWriter<T>)new NodeDataWriter(this);
        } else if (cls == SOAPBody.class) {
            return (DataWriter<T>)new SOAPBodyDataWriter(this);
        }
        
        return null;
    }

    public Class<?>[] getSupportedFormats() {
        return SUPPORTED_FORMATS;
    }

}
