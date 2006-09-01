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

package org.apache.cxf.interceptor;

import java.io.InputStream;
import java.util.ResourceBundle;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.cxf.common.i18n.BundleUtils;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.Phase;

/**
 * Creates an XMLStreamReader from the InputStream on the Message.
 */
public class StaxInInterceptor extends AbstractPhaseInterceptor<Message> {
    private static final ResourceBundle BUNDLE = BundleUtils.getBundle(StaxInInterceptor.class);
    private XMLInputFactory xif;
    
    public StaxInInterceptor() {
        super();
        setPhase(Phase.POST_STREAM);
    }

    public void handleMessage(Message message) {
        InputStream is = message.getContent(InputStream.class);
        

        
        

        assert is != null;

        // TODO: where does encoding constant go?
        String encoding = (String)message.get("Encoding");
        XMLStreamReader reader;
        try {
            reader = getXMLInputFactory().createXMLStreamReader(is, encoding);
        } catch (XMLStreamException e) {
            throw new Fault(new org.apache.cxf.common.i18n.Message("STREAM_CREATE_EXC", BUNDLE), e);
        }

        message.setContent(XMLStreamReader.class, reader);
    }

    protected XMLInputFactory getXMLInputFactory() {
        if (xif == null) {
            return XMLInputFactory.newInstance();
        }

        return xif;
    }
}
