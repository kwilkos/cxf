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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import javax.xml.namespace.QName;
import javax.xml.soap.SOAPMessage;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamSource;

import org.xml.sax.InputSource;

import org.apache.cxf.databinding.DataReader;
import org.apache.cxf.jaxb.JAXBDataReaderFactory;

public class SOAPMessageDataReader implements DataReader<SOAPMessage> {
    final JAXBDataReaderFactory factory;
    
    public SOAPMessageDataReader(JAXBDataReaderFactory cb) {
        factory = cb;
    }

    public Object read(SOAPMessage input) {
        // Complete
        return null;
    }

    public Object read(QName name, SOAPMessage input) {
        // Complete
        return null;
    }

    public Object read(QName name, SOAPMessage input, Class type) {
        SOAPMessage src = (SOAPMessage)input;
        Object obj = null;
        try {
            if (DOMSource.class.isAssignableFrom(type)) {                
                obj = new DOMSource(src.getSOAPPart());               
            } else if (SAXSource.class.isAssignableFrom(type)) {
                
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                src.writeTo(baos);
                ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
                InputSource inputSource = new InputSource(bais);
                obj = new SAXSource(inputSource);
                
            } else if (StreamSource.class.isAssignableFrom(type)) {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                src.writeTo(baos);
                ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
                obj = new StreamSource(bais);
            } else if (SOAPMessage.class.isAssignableFrom(type)) {
                obj = src;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return obj;        
    }

}
