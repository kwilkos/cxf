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
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;
import javax.xml.soap.SOAPBody;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamSource;

import org.w3c.dom.Document;

import org.xml.sax.InputSource;

import org.apache.cxf.databinding.DataReader;
import org.apache.cxf.helpers.XMLUtils;
import org.apache.cxf.jaxb.JAXBDataReaderFactory;

public class SOAPBodyDataReader implements DataReader<SOAPBody> {
    final JAXBDataReaderFactory factory;
    
    public SOAPBodyDataReader(JAXBDataReaderFactory cb) {
        factory = cb;
    }
    
    public Object read(SOAPBody input) {
        // Complete
        return null;
    }

    public Object read(QName name, SOAPBody input) {
        // Complete
        return null;
    }

    public Object read(QName name, SOAPBody input, Class type) {
        Source obj = null;        
        SOAPBody src = (SOAPBody)input;    
        try {
            Document doc = src.extractContentAsDocument();
            assert doc != null;
    
            if (DOMSource.class.isAssignableFrom(type)) {
                obj = new DOMSource();
                ((DOMSource)obj).setNode(doc);          
            } else if (SAXSource.class.isAssignableFrom(type)) {     
                InputSource inputSource = new InputSource(XMLUtils.getInputStream(doc));
                obj = new SAXSource(inputSource);
            } else if (StreamSource.class.isAssignableFrom(type) 
                || Source.class.isAssignableFrom(type)) {     
                obj = new StreamSource(XMLUtils.getInputStream(doc));
            } else if (Object.class.isAssignableFrom(type)) {
                JAXBContext context = factory.getJAXBContext();
                Unmarshaller u = context.createUnmarshaller();
                return u.unmarshal(doc);                    
            }
        } catch (Exception se) {
            se.printStackTrace();
        }
        return obj;      
    }

}
