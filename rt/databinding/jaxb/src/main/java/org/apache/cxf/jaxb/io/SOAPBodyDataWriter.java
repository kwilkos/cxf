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
import javax.xml.bind.Marshaller;
import javax.xml.soap.SOAPBody;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamSource;

import org.w3c.dom.Document;

import org.apache.cxf.databinding.DataWriter;
import org.apache.cxf.helpers.XMLUtils;
import org.apache.cxf.jaxb.JAXBDataWriterFactory;
import org.apache.cxf.service.model.MessagePartInfo;

public class SOAPBodyDataWriter implements DataWriter<SOAPBody> {
    protected SOAPBody dest;
    final JAXBDataWriterFactory factory;
    
    public SOAPBodyDataWriter(JAXBDataWriterFactory f) {
        factory = f;
    }

    public void write(Object obj, SOAPBody output) {
        dest = (SOAPBody) output;
        try {

            if (DOMSource.class.isAssignableFrom(obj.getClass())) {
                DOMSource domSource = (DOMSource)obj;
                dest.addDocument((Document)domSource.getNode());
            } else if (SAXSource.class.isAssignableFrom(obj.getClass())) {
                SAXSource saxSource = (SAXSource)obj;
                Document doc = XMLUtils.getParser().parse(saxSource.getInputSource());
                dest.addDocument(doc); 
            } else if (StreamSource.class.isAssignableFrom(obj.getClass())) {
                StreamSource streamSource = (StreamSource)obj;
                Document doc = XMLUtils.getParser().parse(streamSource.getInputStream());
                dest.addDocument(doc); 
            } else if (Object.class.isAssignableFrom(obj.getClass())) {
                JAXBContext context = factory.getJAXBContext();
                
                Marshaller u = context.createMarshaller();
                u.setProperty(Marshaller.JAXB_ENCODING , "UTF-8");
                u.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
                u.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.TRUE);  
                
                DOMResult domResult = new DOMResult();
                u.marshal(obj, domResult);
                dest.addDocument((Document)domResult.getNode());                
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    


    public void write(Object obj, MessagePartInfo part, SOAPBody output) {
        // Complete      

    }

}
