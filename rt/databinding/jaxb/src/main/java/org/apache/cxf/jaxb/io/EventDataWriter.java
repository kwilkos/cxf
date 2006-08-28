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

package org.apache.cxf.jaxb.io;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventWriter;

import org.apache.cxf.databinding.DataWriter;
import org.apache.cxf.jaxb.JAXBDataWriterFactory;
import org.apache.cxf.jaxb.JAXBEncoderDecoder;

public class EventDataWriter implements DataWriter<XMLEventWriter> {
    final JAXBDataWriterFactory factory;

    public EventDataWriter(JAXBDataWriterFactory cb) {
        factory = cb;
    }
    
    public void write(Object obj, XMLEventWriter output) {
        write(obj, null, output);
    }
    
    public void write(Object obj, QName elName, XMLEventWriter output) {
        if (obj != null) {
            JAXBEncoderDecoder.marshall(factory.getJAXBContext(),
                                        factory.getSchema(), obj,
                                        elName, output, null);
        }
    }
    /*
    public void writeWrapper(ObjectMessageContext objCtx, boolean isOutBound, T output) {
        Object obj = callback.createWrapperType(objCtx, isOutBound);
        QName elName = isOutBound ? callback.getResponseWrapperQName()
                                  : callback.getRequestWrapperQName();
        write(obj, elName, output);
    }
    */
}
