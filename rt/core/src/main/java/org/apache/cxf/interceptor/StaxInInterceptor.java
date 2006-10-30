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
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.cxf.common.classloader.ClassLoaderUtils;
import org.apache.cxf.common.i18n.BundleUtils;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.Phase;
import org.apache.cxf.staxutils.StaxUtils;

/**
 * Creates an XMLStreamReader from the InputStream on the Message.
 */
public class StaxInInterceptor extends AbstractPhaseInterceptor<Message> {
    private static final ResourceBundle BUNDLE = BundleUtils.getBundle(StaxInInterceptor.class);
    private static Map<Object, XMLInputFactory> factories = new HashMap<Object, XMLInputFactory>();

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
            reader = getXMLInputFactory(message).createXMLStreamReader(is, encoding);
        } catch (XMLStreamException e) {
            throw new Fault(new org.apache.cxf.common.i18n.Message("STREAM_CREATE_EXC", BUNDLE), e);
        }

        message.setContent(XMLStreamReader.class, reader);
    }

    public static XMLInputFactory getXMLInputFactory(Message m) throws Fault {
        Object o = m.getContextualProperty(XMLInputFactory.class.getName());
        if (o instanceof XMLInputFactory) {
            return (XMLInputFactory)o;
        } else if (o != null) {
            XMLInputFactory xif = (XMLInputFactory)factories.get(o);
            if (xif == null) {
                Class cls;
                if (o instanceof Class) {
                    cls = (Class)o;
                } else if (o instanceof String) {
                    try {
                        cls = ClassLoaderUtils.loadClass((String)o, StaxInInterceptor.class);
                    } catch (ClassNotFoundException e) {
                        throw new Fault(e);
                    }
                } else {
                    throw new Fault(
                                    new org.apache.cxf.common.i18n.Message("INVALID_INPUT_FACTORY", 
                                                                           BUNDLE, o));
                }

                try {
                    xif = (XMLInputFactory)(cls.newInstance());
                    factories.put(o, xif);
                } catch (InstantiationException e) {
                    throw new Fault(e);
                } catch (IllegalAccessException e) {
                    throw new Fault(e);
                }
            }
            return xif;
        } else {
            return StaxUtils.getXMLInputFactory();
        }
    }
}
