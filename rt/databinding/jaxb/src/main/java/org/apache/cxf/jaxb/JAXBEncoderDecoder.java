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

import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.jws.WebMethod;
import javax.jws.WebParam;
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
import javax.xml.ws.Holder;
import javax.xml.ws.RequestWrapper;
import javax.xml.ws.ResponseWrapper;
import javax.xml.ws.WebEndpoint;

import org.w3c.dom.Node;

import org.apache.cxf.common.i18n.BundleUtils;
import org.apache.cxf.common.i18n.Message;
import org.apache.cxf.common.util.PackageUtils;
import org.apache.cxf.interceptor.Fault;

/**
 * JAXBEncoderDecoder
 * 
 * @author apaibir
 */
public final class JAXBEncoderDecoder {
    private static final ResourceBundle BUNDLE = BundleUtils.getBundle(JAXBEncoderDecoder.class);

    private static Map<Class<?>, JAXBContext> contextMap = new ConcurrentHashMap<Class<?>, JAXBContext>();

    private JAXBEncoderDecoder() {
    }

    public static JAXBContext createJAXBContextForClass(Class<?> cls) throws JAXBException {
        JAXBContext context = contextMap.get(cls);
        if (context == null) {
            Set<Class<?>> classes = new HashSet<Class<?>>();
            getClassesForContext(cls, classes, cls.getClassLoader());

            try {
                classes.add(Class.forName("org.apache.cxf.ws.addressing.wsdl.AttributedQNameType"));
                classes.add(Class.forName("org.apache.cxf.ws.addressing.wsdl.ObjectFactory"));
                classes.add(Class.forName("org.apache.cxf.ws.addressing.wsdl.ServiceNameType"));
            } catch (ClassNotFoundException e) {
                // REVISIT - ignorable if WS-ADDRESSING not available?
                // maybe add a way to allow interceptors to add stuff to the
                // context?
            }
           
            context = JAXBContext.newInstance(classes.toArray(new Class[classes.size()]));
            contextMap.put(cls, context);
        }
        return context;
    }

    private static Class<?> getValidClass(Class<?> cls) {
        if (cls.isEnum()) {
            return cls;
        }
        if (cls.isArray()) {
            return getValidClass(cls.getComponentType());
        }

        if (cls == Object.class || cls == String.class || cls == Holder.class) {
            cls = null;
        } else if (cls.isPrimitive() || cls.isInterface() || cls.isAnnotation()) {
            cls = null;
        }
        if (cls != null) {
            try {
                if (cls.getConstructor(new Class[0]) == null) {
                    cls = null;
                }
            } catch (NoSuchMethodException ex) {
                cls = null;
            }
        }
        return cls;
    }

    private static void addClass(Class<?> cls, Set<Class<?>> classes) {
        if (cls.isArray()) {
            // REVISIT-- add java primitive type array will cause jaxb exception
            //classes.add(cls);
            return;
        }
        cls = getValidClass(cls);
        if (null != cls) {
            if (cls.isEnum()) {
                // The object factory stuff doesn't work for enums
                classes.add(cls);
            }
            String name = PackageUtils.getPackageName(cls) + ".ObjectFactory";
            try {
                cls = Class.forName(name, false, cls.getClassLoader());
                if (cls != null) {
                    classes.add(cls);
                }
            } catch (ClassNotFoundException ex) {
                // cannot add factory, just add the class
                classes.add(cls);
            }
        }
    }

    private static void addType(Type cls, Set<Class<?>> classes) {
        if (cls instanceof Class) {
            addClass((Class)cls, classes);
        } else if (cls instanceof ParameterizedType) {
            for (Type t2 : ((ParameterizedType)cls).getActualTypeArguments()) {
                addType(t2, classes);
            }
        } else if (cls instanceof GenericArrayType) {
            GenericArrayType gt = (GenericArrayType)cls;
            Class ct = (Class) gt.getGenericComponentType();
            ct = Array.newInstance(ct, 0).getClass();
            //addType(ct, classes);
            classes.add(ct);
        }
    }

    // collect ALL the classes that are accessed by the class
    private static void getClassesForContext(Class<?> theClass, Set<Class<?>> classes, ClassLoader loader) {
        Method methods[] = theClass.getMethods();
        for (Method meth : methods) {
            WebEndpoint webEndpoint = meth.getAnnotation(WebEndpoint.class);
            if (webEndpoint != null) {
                getClassesForContext(meth.getReturnType(), classes, loader);
            }

            // only methods marked as WebMethods are interesting to us
            WebMethod webMethod = meth.getAnnotation(WebMethod.class);
            if (webMethod == null) {
                continue;
            }

            for (Type t : meth.getGenericParameterTypes()) {
                addType(t, classes);
            }
            addType(meth.getGenericReturnType(), classes);

            if (meth.getReturnType().isArray()) {
                addClass(meth.getReturnType(), classes);
            }
            for (Class<?> cls : meth.getParameterTypes()) {
                addClass(cls, classes);
            }

            for (Class<?> cls : meth.getExceptionTypes()) {
                // addClass(cls, classes);
                try {
                    Method fim = cls.getMethod("getFaultInfo", new Class[0]);
                    addClass(fim.getReturnType(), classes);
                } catch (NoSuchMethodException ex) {
                    // ignore - not a valid JAXB fault thing
                }
            }
            try {
                // Get the RequestWrapper
                RequestWrapper reqWrapper = meth.getAnnotation(RequestWrapper.class);
                if (reqWrapper != null) {
                    Class<?> cls = Class.forName(reqWrapper.className(), false, loader);
                    addClass(cls, classes);

                }
                // Get the RequestWrapper

                ResponseWrapper respWrapper = meth.getAnnotation(ResponseWrapper.class);
                if (respWrapper != null) {
                    Class<?> cls = Class.forName(respWrapper.className(), false, loader);
                    addClass(cls, classes);
                }
            } catch (ClassNotFoundException ex) {
                // ignore
            }
            // get ObjectFactory in case of bare

            Annotation[][] parasAnnotation = meth.getParameterAnnotations();
            String packageName = null;
            for (int i = 0; i < parasAnnotation.length; i++) {
                Annotation[] paraAnno = parasAnnotation[i];
                for (int j = 0; j < paraAnno.length; j++) {
                    if (paraAnno[j].annotationType() == WebParam.class) {
                     
                        packageName = ((WebParam)paraAnno[j]).targetNamespace();
                        packageName = PackageUtils.parsePackageName(packageName, null);

                        try {
                            Class<?> cls = Class.forName(packageName + ".ObjectFactory", false, theClass
                                .getClassLoader());
                            if (cls != null) {
                                classes.add(cls);
                            }
                        } catch (ClassNotFoundException ex) {
                            // ignore
                        }
                    }
                }
            }
        }

        for (Class<?> intf : theClass.getInterfaces()) {
            getClassesForContext(intf, classes, loader);
        }
        if (theClass.getSuperclass() != null) {
            getClassesForContext(theClass.getSuperclass(), classes, loader);
        }
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

    public static void marshall(JAXBContext context, Schema schema, Object elValue, QName elNname,
                                Object source, AttachmentMarshaller am) {

        Class<?> cls = null != elValue ? elValue.getClass() : null;
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

            if (null != elNname && null != cls && !cls.isAnnotationPresent(XmlRootElement.class)) {
                mObj = JAXBElement.class.getConstructor(new Class[] {QName.class, Class.class, Object.class})
                    .newInstance(elNname, cls, mObj);
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
            throw new Fault(new Message("MARSHAL_ERROR", BUNDLE), ex);
        }
    }

    public static void marshall(JAXBContext context, Schema schema, Object elValue, Object source) {
        marshall(context, schema, elValue, null, source, null);
    }

    public static void marshall(JAXBContext context, Schema schema, Object elValue, QName elNname,
                                Object source) {
        marshall(context, schema, elValue, elNname, source, null);
    }

    private static Unmarshaller createUnmarshaller(JAXBContext context, Class<?> cls) throws JAXBException {
        Unmarshaller um = null;
        if (context == null) {
            context = JAXBContext.newInstance(cls);
        }

        um = context.createUnmarshaller();

        return um;
    }

    public static Object unmarshall(JAXBContext context, Schema schema, Object source) {
        return unmarshall(context, schema, source, null, null, null);
    }

    public static Object unmarshall(JAXBContext context, Schema schema, Object source, QName elName) {
        return unmarshall(context, schema, source, elName, null, null);
    }

    public static Object unmarshall(JAXBContext context, Schema schema, Object source, QName elName,
                                    Class<?> clazz) {
        return unmarshall(context, schema, source, elName, clazz, null);
    }

    public static Object unmarshall(JAXBContext context, Schema schema, Object source, QName elName,
                                    Class<?> clazz, AttachmentUnmarshaller au) {
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
            throw new Fault(new Message("MARSHAL_ERROR", BUNDLE), ex);
        }
        return getElementValue(obj, elName);
    }

    public static Object getElementValue(Object obj, QName elName) {
        if (null == obj) {
            return null;
        }

        if (obj instanceof JAXBElement) {
            return ((JAXBElement<?>)obj).getValue();
        }
        return obj;
    }

//    private static boolean isSame(QName messageQName, QName methodQName) {
//        boolean same = false;
//        if (StringUtils.isEmpty(messageQName.getNamespaceURI())) {
//            same = messageQName.getLocalPart().equals(methodQName.getLocalPart());
//        } else {
//            same = messageQName.equals(methodQName);
//        }
//        return same;
//    }

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
