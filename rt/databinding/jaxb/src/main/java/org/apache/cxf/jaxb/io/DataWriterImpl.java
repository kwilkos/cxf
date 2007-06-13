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

import javax.xml.bind.JAXBContext;

import org.apache.cxf.databinding.DataWriter;
import org.apache.cxf.jaxb.JAXBDataBase;
import org.apache.cxf.jaxb.JAXBEncoderDecoder;
import org.apache.cxf.service.model.MessagePartInfo;

public class DataWriterImpl<T> extends JAXBDataBase implements DataWriter<T> {
    public DataWriterImpl(JAXBContext ctx) {
        setJAXBContext(ctx);
    }
    
    public void write(Object obj, T output) {
        write(obj, null, output);
    }
    
    public void write(Object obj, MessagePartInfo part, T output) {
        if (obj != null) {
            JAXBEncoderDecoder.marshall(getJAXBContext(), getSchema(), obj, part, output, 
                                        getAttachmentMarrshaller());
        }
    }
}
