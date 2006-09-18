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

import java.io.InputStream;

import javax.activation.DataSource;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimePartDataSource;
import javax.mail.util.ByteArrayDataSource;
import javax.xml.namespace.QName;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamSource;

import org.w3c.dom.Document;

import org.xml.sax.InputSource;

import org.apache.cxf.databinding.DataReader;
import org.apache.cxf.helpers.XMLUtils;
import org.apache.cxf.jaxb.JAXBDataReaderFactory;
import org.apache.cxf.message.XMLMessage;

public class XMLMessageDataReader implements DataReader<XMLMessage> {
    final JAXBDataReaderFactory factory;

    public XMLMessageDataReader(JAXBDataReaderFactory cb) {
        factory = cb;
    }

    public Object read(XMLMessage input) {
        // Complete
        return null;
    }

    public Object read(QName name, XMLMessage input) {
        // Complete
        return null;
    }

    /**
     * @param name
     * @param input
     * @param type
     * @return
     */
    public Object read(QName name, XMLMessage input, Class type) {
        Object obj = null;
        InputStream is = input.getContent(InputStream.class);

        try {
            // Tolerate empty InputStream in order to deal with HTTP GET
            if (is == null || is.available() == 0) {
                // TODO LOG ERROR here
                return null;
            }

            // Processing Souce type
            if (DOMSource.class.isAssignableFrom(type)) {
                Document doc = XMLUtils.parse(is);
                obj = new DOMSource(doc);
            } else if (SAXSource.class.isAssignableFrom(type)) {
                obj = new SAXSource(new InputSource(is));
            } else if (StreamSource.class.isAssignableFrom(type) || Source.class.isAssignableFrom(type)) {
                obj = new StreamSource(is);
            }

            // Processing DataSource type
            if (MimePartDataSource.class.isAssignableFrom(type)) {
                // Support JavaMail MimePart DataSource type
                obj = new MimePartDataSource(new MimeBodyPart(is));
            } else if (ByteArrayDataSource.class.isAssignableFrom(type)
                       || DataSource.class.isAssignableFrom(type)) {
                // Support JavaMail ByteArrayDataSource
                obj = new ByteArrayDataSource(is, null);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return obj;
    }

}
