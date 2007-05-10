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

package org.apache.cxf.jaxb;

import java.lang.reflect.Array;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.ResourceBundle;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.attachment.AttachmentMarshaller;
import javax.xml.bind.attachment.AttachmentUnmarshaller;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.validation.Schema;

import org.w3c.dom.Node;

import org.apache.cxf.common.i18n.BundleUtils;
import org.apache.cxf.common.i18n.Message;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.service.model.MessagePartInfo;
import org.apache.ws.commons.schema.XmlSchemaElement;
import org.apache.ws.commons.schema.XmlSchemaSimpleType;
import org.apache.ws.commons.schema.XmlSchemaSimpleTypeList;

/**
 * Utility functions for JAXB.
 */
public final class JAXBEncoderDecoder {
    private static final ResourceBundle BUNDLE = BundleUtils.getBundle(JAXBEncoderDecoder.class);
    
    private JAXBEncoderDecoder() {
    }

    private static Marshaller createMarshaller(JAXBContext context, Class<?> cls) throws JAXBException {
        Marshaller jm = null;
        if (context == null) {
            context = JAXBContext.newInstance(cls);
        }

        jm = context.createMarshaller();
        jm.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
        jm.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);

        return jm;
    }

    public static void marshall(JAXBContext context, 
                                Schema schema, 
                                Object elValue, 
                                MessagePartInfo part,
                                Object source, 
                                AttachmentMarshaller am) {
        Class<?> cls = null;
        if (part != null) {
            cls = part.getTypeClass();
        } 

        if (cls == null) {
            cls = null != elValue ? elValue.getClass() : null;
        }
        
        if (cls != null && cls.isArray() && elValue instanceof Collection) {
            Collection<?> col = (Collection<?>) elValue;
            elValue = col.toArray((Object[]) Array.newInstance(cls.getComponentType(), 0));
        }
        
        try {
            Marshaller u = createMarshaller(context, cls);
            try {
                // The Marshaller.JAXB_FRAGMENT will tell the Marshaller not to
                // generate the xml declaration.
                u.setProperty(Marshaller.JAXB_FRAGMENT, true);
            } catch (javax.xml.bind.PropertyException e) {
                // intentionally empty.
            }
            Object mObj = elValue;

            QName elName = null;
            if (part != null) {
                elName = part.getConcreteName();
            }
            
            if (null != elName
                && !cls.isAnnotationPresent(XmlRootElement.class)) {
                
                if (mObj.getClass().isArray()
                    && part != null
                    && part.getXmlSchema() instanceof XmlSchemaElement) {
                    XmlSchemaElement el = (XmlSchemaElement)part.getXmlSchema();
                    if (el.getSchemaType() instanceof XmlSchemaSimpleType
                        && ((XmlSchemaSimpleType)el.getSchemaType()).getContent()
                            instanceof XmlSchemaSimpleTypeList) {
                        mObj = Arrays.asList((Object[])mObj);
                    }
                }
                mObj = JAXBElement.class.getConstructor(new Class[] {QName.class, Class.class, Object.class})
                    .newInstance(elName, cls, mObj);
            }
            
            u.setSchema(schema);
            if (am != null) {
                u.setAttachmentMarshaller(am);
            }
            if (source instanceof Node) {
                u.marshal(mObj, (Node)source);
            } else if (source instanceof XMLEventWriter) {
                u.marshal(mObj, (XMLEventWriter)source);
            } else if (source instanceof XMLStreamWriter) {
                u.marshal(mObj, (XMLStreamWriter)source);
            } else {
                throw new Fault(new Message("UNKNOWN_SOURCE", BUNDLE, source.getClass().getName()));
            }
        } catch (Exception ex) {
            if (ex instanceof javax.xml.bind.MarshalException) {
                javax.xml.bind.MarshalException marshalEx = (javax.xml.bind.MarshalException)ex;
                Message faultMessage = new Message("MARSHAL_ERROR",
                                                   BUNDLE, marshalEx.getLinkedException().getMessage());
                throw new Fault(faultMessage, ex); 
            } else {
                throw new Fault(new Message("MARSHAL_ERROR", BUNDLE, ex.getMessage()), ex);
            }                       
        }
    }

    public static void marshall(JAXBContext context, Schema schema, Object elValue, Object source) {
        marshall(context, schema, elValue, null, source, null);
    }

    public static void marshall(JAXBContext context, Schema schema, 
                                Object elValue, 
                                MessagePartInfo part,
                                Object source) {
        marshall(context, schema, elValue, part, source, null);
    }

    private static Unmarshaller createUnmarshaller(JAXBContext context, Class<?> cls) throws JAXBException {
        Unmarshaller um = null;
        if (context == null) {
            if (cls == null) {
                throw new IllegalStateException("A JAXBContext or Class to unmarshal must be provided!");
            }
            context = JAXBContext.newInstance(cls);
        }

        um = context.createUnmarshaller();

        return um;
    }

    public static Object unmarshall(JAXBContext context, Schema schema, Object source) {
        return unmarshall(context, schema, source, null, null, true);
    }

    @SuppressWarnings("unchecked")
    public static Object unmarshall(JAXBContext context, 
                                    Schema schema, 
                                    Object source,
                                    MessagePartInfo part, 
                                    AttachmentUnmarshaller au, 
                                    boolean unwrap) {
        Class<?> clazz = part != null ? (Class) part.getTypeClass() : null;
        QName elName = part != null ? part.getConcreteName() : null;
        if (clazz != null
            && clazz.isArray()
            && part != null 
            && part.getXmlSchema() instanceof XmlSchemaElement) {
            XmlSchemaElement el = (XmlSchemaElement)part.getXmlSchema();
            if (el.getSchemaType() instanceof XmlSchemaSimpleType
                && ((XmlSchemaSimpleType)el.getSchemaType()).getContent()
                    instanceof XmlSchemaSimpleTypeList) {
                
                Object obj = unmarshall(context, schema, source, elName, null, au, unwrap);
                if (clazz.isArray()
                    && obj instanceof List) {
                    return ((List)obj).toArray((Object[])Array.newInstance(clazz.getComponentType(),
                                                                 ((List)obj).size()));
                }
                    
                
                return obj;
            }
        }

        return unmarshall(context, schema, source, elName, clazz, au, unwrap);
    }
    
    public static Object unmarshall(JAXBContext context, 
                                    Schema schema, 
                                    Object source,
                                    QName elName,
                                    Class<?> clazz,
                                    AttachmentUnmarshaller au, 
                                    boolean unwrap) {
        Object obj = null;
        
        try {
            Unmarshaller u = createUnmarshaller(context, clazz);
            u.setSchema(schema);
            if (au != null) {
                u.setAttachmentUnmarshaller(au);
            }
            if (source instanceof Node) {
                obj = (clazz != null) ? u.unmarshal((Node)source, clazz) : u.unmarshal((Node)source);
            } else if (source instanceof XMLStreamReader) {
                obj = (clazz != null) ? u.unmarshal((XMLStreamReader)source, clazz) : u
                    .unmarshal((XMLStreamReader)source);
            } else if (source instanceof XMLEventReader) {
                obj = (clazz != null) ? u.unmarshal((XMLEventReader)source, clazz) : u
                    .unmarshal((XMLEventReader)source);
            } else {
                throw new Fault(new Message("UNKNOWN_SOURCE", BUNDLE, source.getClass().getName()));
            }
        } catch (Exception ex) {                        
            if (ex instanceof javax.xml.bind.UnmarshalException) {                
                javax.xml.bind.UnmarshalException unmarshalEx = (javax.xml.bind.UnmarshalException)ex;
                throw new Fault(new Message("UNMARSHAL_ERROR", 
                                            BUNDLE, unmarshalEx.getLinkedException().getMessage()), ex); 
            } else {
                throw new Fault(new Message("UNMARSHAL_ERROR", BUNDLE, ex.getMessage()), ex);
            }
        }
        return unwrap ? getElementValue(obj) : obj;
    }

    public static Object getElementValue(Object obj) {
        if (null == obj) {
            return null;
        }

        if (obj instanceof JAXBElement) {
            return ((JAXBElement<?>)obj).getValue();
        }
        return obj;
    }
    
    public static Class<?> getClassFromType(Type t) {
        if (t instanceof Class) {
            return (Class)t;
        } else if (t instanceof GenericArrayType) {
            GenericArrayType g = (GenericArrayType)t;
            return Array.newInstance(getClassFromType(g.getGenericComponentType()), 0).getClass();
        } else if (t instanceof ParameterizedType) {
            ParameterizedType p = (ParameterizedType)t;
            return getClassFromType(p.getRawType());
        }
        // TypeVariable and WildCardType are not handled as it is unlikely such
        // Types will
        // JAXB Code Generated.
        assert false;
        throw new IllegalArgumentException("Cannot get Class object from unknown Type");
    }
}
