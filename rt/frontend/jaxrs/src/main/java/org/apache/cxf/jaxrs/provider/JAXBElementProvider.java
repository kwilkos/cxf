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

package org.apache.cxf.jaxrs.provider;


import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.EntityProvider;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.namespace.QName;

public final class JAXBElementProvider implements EntityProvider<Object>  {

    static Map<Class, JAXBContext> jaxbContexts = new WeakHashMap<Class, JAXBContext>();

    public boolean supports(Class<?> type) {
        return type.getAnnotation(XmlRootElement.class) != null;
    }

    public Object readFrom(Class<Object> type, String mediaType, MultivaluedMap<String, String> headers,
                           InputStream is) {
        try {
            JAXBContext context = getJAXBContext(type);
            Unmarshaller unmarshaller = context.createUnmarshaller();
            return unmarshaller.unmarshal(is);
        } catch (JAXBException e) {
            e.printStackTrace();         
        }

        return null;
    }

    public void writeTo(Object obj, MultivaluedMap<String, Object> headers, OutputStream os) {
        try {
            if (obj.getClass().isArray() || obj instanceof List) {
                Class<?> cls = null;
                Object objArray;
                if (obj instanceof List) {
                    List l = (List)obj;
                    objArray = l.toArray(new Object[l.size()]);
                    cls = null;
                } else {
                    objArray = obj;
                    cls = objArray.getClass().getComponentType();
                }
                int len = Array.getLength(objArray);
                for (int x = 0; x < len; x++) {
                    Object o = Array.get(objArray, x);
                    JAXBContext context = getJAXBContext(o.getClass());
                    Marshaller marshaller = context.createMarshaller();
                    marshaller.setProperty(Marshaller.JAXB_FRAGMENT, true);
                    marshaller.marshal(new JAXBElement(new QName(null, o.getClass().getSimpleName()),
                                                       cls == null ? o.getClass() : cls, o), os);
                }
            } else {
                JAXBContext context = getJAXBContext(obj.getClass());
                Marshaller marshaller = context.createMarshaller();
                marshaller.setProperty(Marshaller.JAXB_FRAGMENT, true);
                marshaller.marshal(obj, os);
            }
        } catch (JAXBException e) {
            e.printStackTrace();
        }
    }

    private JAXBContext getJAXBContext(Class type) throws JAXBException {
        synchronized (jaxbContexts) {
            JAXBContext context = jaxbContexts.get(type);
            if (context == null) {
                context = JAXBContext.newInstance(type);
                jaxbContexts.put(type, context);
            }
            return context;
        }
    }
}
