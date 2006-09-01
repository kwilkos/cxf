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
import javax.xml.soap.SOAPMessage;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamReader;

import org.w3c.dom.Node;

import org.apache.cxf.databinding.DataReader;
import org.apache.cxf.databinding.DataReaderFactory;
import org.apache.cxf.jaxb.io.EventDataReader;
import org.apache.cxf.jaxb.io.NodeDataReader;
import org.apache.cxf.jaxb.io.SOAPBodyDataReader;
import org.apache.cxf.jaxb.io.SOAPMessageDataReader;
import org.apache.cxf.jaxb.io.XMLStreamDataReader;


public final class JAXBDataReaderFactory extends JAXBDataFactoryBase implements DataReaderFactory {
    private static final Class<?> SUPPORTED_FORMATS[] = new Class<?>[] {Node.class,
                                                                        XMLEventReader.class,
                                                                        XMLStreamReader.class,
                                                                        SOAPBody.class,
                                                                        SOAPMessage.class};
    
    public JAXBDataReaderFactory() {
        
    }

    
    @SuppressWarnings("unchecked")
    public <T> DataReader<T> createReader(Class<T> cls) {
        if (cls == XMLStreamReader.class) {
            return (DataReader<T>)new XMLStreamDataReader(this);
        } else if (cls == XMLEventReader.class) {
            return (DataReader<T>)new EventDataReader(this);            
        } else if (cls == Node.class) {
            return (DataReader<T>)new NodeDataReader(this);
        } else if (cls == SOAPBody.class) {
            return (DataReader<T>)new SOAPBodyDataReader(this);
        } else if (cls == SOAPMessage.class) {
            return (DataReader<T>)new SOAPMessageDataReader(this);
        }
        // TODO Auto-generated method stub
        return null;
    }

    public Class<?>[] getSupportedFormats() {
        return SUPPORTED_FORMATS;
    }


}
