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

package org.apache.cxf.xmlbeans;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.logging.Logger;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamReader;
import javax.xml.validation.Schema;

import org.apache.cxf.common.i18n.Message;
import org.apache.cxf.common.logging.LogUtils;
import org.apache.cxf.databinding.DataReader;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.message.Attachment;
import org.apache.cxf.service.model.MessagePartInfo;
import org.apache.xmlbeans.XmlOptions;


public class DataReaderImpl implements DataReader<XMLStreamReader> {
    private static final Logger LOG = LogUtils.getLogger(XmlBeansDataBinding.class);
    
    public DataReaderImpl() {
    }

    public Object read(XMLStreamReader input) {
        return read(null, input);
    }

    public Object read(MessagePartInfo part, XMLStreamReader reader) {
        Class<?> cls[] = part.getTypeClass().getDeclaredClasses();
        for (Class<?> c : cls) {
            if ("Factory".equals(c.getSimpleName())) {
                try {
                    XmlOptions options = new XmlOptions();
                    options.setLoadReplaceDocumentElement(null);
                    Method meth = c.getMethod("parse", XMLStreamReader.class, XmlOptions.class);
                    return meth.invoke(null, reader, options);
                } catch (Exception e) {
                    throw new Fault(new Message("UNMARSHAL_ERROR", LOG, part.getTypeClass()), e);
                }
            }
        }
        return null;
    }

    public Object read(QName name, XMLStreamReader input, Class type) {
        return null;
    }

    
    public void setAttachments(Collection<Attachment> attachments) {
    }

    public void setProperty(String prop, Object value) {
    }

    public void setSchema(Schema s) {
    }

}
