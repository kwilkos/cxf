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
package org.apache.cxf.aegis.type.java5;

import java.beans.PropertyDescriptor;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.WildcardType;
import java.util.Collection;

import javax.xml.namespace.QName;

import org.apache.cxf.aegis.type.AbstractTypeCreator;
import org.apache.cxf.aegis.type.Type;
import org.apache.cxf.aegis.type.basic.BeanType;
import org.apache.cxf.aegis.util.NamespaceHelper;
import org.apache.cxf.aegis.util.ServiceUtils;

public class Java5TypeCreator extends AbstractTypeCreator {

    @Override
    public TypeClassInfo createClassInfo(Method m, int index) {
        if (index >= 0) {
            TypeClassInfo info;
            java.lang.reflect.Type genericType = m.getGenericParameterTypes()[index];
            if (genericType instanceof Class) {
                info = nextCreator.createClassInfo(m, index);
            } else {
                info = new TypeClassInfo();
                info.setDescription("method " + m.getName() + " parameter " + index);
                info.setGenericType(genericType);
            }
            info.setTypeClass(m.getParameterTypes()[index]);

            XmlParamType xmlParam = getXmlParamAnnotation(m, index);
            if (xmlParam != null) {
                if (xmlParam.type() != Type.class) {
                    info.setType(xmlParam.type());
                }

                info.setTypeName(createQName(m.getParameterTypes()[index], xmlParam.name(), xmlParam
                    .namespace()));
            }

            return info;
        } else {
            java.lang.reflect.Type genericReturnType = m.getGenericReturnType();
            TypeClassInfo info;
            if (genericReturnType instanceof Class) {
                info = nextCreator.createClassInfo(m, index);
            } else {
                info = new TypeClassInfo();
                info.setDescription("method " + m.getName() + " parameter " + index);
                info.setGenericType(genericReturnType);
            }
            info.setTypeClass(m.getReturnType());
            if (m.getParameterAnnotations() != null && m.getAnnotations().length > 0) {
                info.setAnnotations(m.getAnnotations());
            }

            XmlReturnType xmlParam = m.getAnnotation(XmlReturnType.class);
            if (xmlParam != null) {
                if (xmlParam.type() != Type.class) {
                    info.setType(xmlParam.type());
                }

                info.setTypeName(createQName(m.getReturnType(), xmlParam.name(), xmlParam.namespace()));
            }

            return info;
        }
    }

    public XmlParamType getXmlParamAnnotation(Method m, int index) {
        if (m.getParameterAnnotations() == null
            || m.getParameterAnnotations().length <= index
            || m.getParameterAnnotations()[index] == null) {
            return null;
        }

        Annotation[] annotations = m.getParameterAnnotations()[index];

        for (int i = 0; i < annotations.length; i++) {
            Annotation annotation = annotations[i];
            if (annotation.annotationType().equals(XmlParamType.class)) {
                return (XmlParamType)annotations[i];
            }
        }

        return null;
    }

    @Override
    public TypeClassInfo createClassInfo(PropertyDescriptor pd) {
        TypeClassInfo info = createBasicClassInfo(pd.getPropertyType());
        info.setGenericType(pd.getReadMethod().getGenericReturnType());
        info.setAnnotations(pd.getReadMethod().getAnnotations());

        XmlElement el = pd.getReadMethod().getAnnotation(XmlElement.class);
        if (el != null && !el.type().equals(Type.class)) {
            info.setType(el.type());
        }

        XmlAttribute att = pd.getReadMethod().getAnnotation(XmlAttribute.class);
        if (att != null && !att.type().equals(Type.class)) {
            info.setType(att.type());
        }

        return info;
    }

    @Override
    public Type createCollectionType(TypeClassInfo info) {
        Object genericType = info.getGenericType();
        Class paramClass = getComponentType(genericType, 0);

        if (paramClass != null) {
            return createCollectionTypeFromGeneric(info);
        } else {
            return nextCreator.createCollectionType(info);
        }
    }

    protected Type getOrCreateGenericType(TypeClassInfo info) {
        return getOrCreateParameterizedType(info.getGenericType(), 0);
    }

    protected Type getOrCreateMapKeyType(TypeClassInfo info) {
        return getOrCreateParameterizedType(info.getGenericType(), 0);
    }

    protected Type getOrCreateMapValueType(TypeClassInfo info) {
        return getOrCreateParameterizedType(info.getGenericType(), 1);
    }

    protected Type getOrCreateParameterizedType(Object generic, int index) {
        Class clazz = getComponentType(generic, index);

        if (clazz == null) {
            return createObjectType();
        }
        
        if (!Collection.class.isAssignableFrom(clazz)) {
            return getTopCreator().createType(clazz);
        }
        
        Object component = getGenericComponent(generic, index);
        
        TypeClassInfo info = createBasicClassInfo(clazz);
        info.setDescription(clazz.toString());
        info.setGenericType(component);

        Type type = createTypeForClass(info);

        return type;
    }

    private Object getGenericComponent(Object genericType, int index) {
        if (genericType instanceof ParameterizedType) {
            ParameterizedType type = (ParameterizedType)genericType;

            if (type.getActualTypeArguments()[index] instanceof WildcardType) {
                WildcardType wildcardType = (WildcardType)type.getActualTypeArguments()[index];

                return wildcardType;
            } else if (type.getActualTypeArguments()[index] instanceof ParameterizedType) {
                ParameterizedType ptype = (ParameterizedType)type.getActualTypeArguments()[index];

                return ptype;
            }
        }

        return null;
    }

    protected Class getComponentType(Object genericType, int index) {
        Class paramClass = null;

        if (genericType instanceof ParameterizedType) {
            ParameterizedType type = (ParameterizedType)genericType;

            if (type.getActualTypeArguments()[index] instanceof Class) {
                paramClass = (Class)type.getActualTypeArguments()[index];
            } else if (type.getActualTypeArguments()[index] instanceof WildcardType) {
                WildcardType wildcardType = (WildcardType)type.getActualTypeArguments()[index];

                if (wildcardType.getUpperBounds()[index] instanceof Class) {
                    paramClass = (Class)wildcardType.getUpperBounds()[index];
                }
            } else if (type.getActualTypeArguments()[index] instanceof ParameterizedType) {
                ParameterizedType ptype = (ParameterizedType)type.getActualTypeArguments()[index];
                paramClass = (Class)ptype.getRawType();
            }
        }
        return paramClass;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Type createDefaultType(TypeClassInfo info) {
        QName typeName = info.getTypeName();
        if (typeName == null) {
            typeName = createQName(info.getTypeClass());
        }

        AnnotatedTypeInfo typeInfo = new AnnotatedTypeInfo(getTypeMapping(), info.getTypeClass(), typeName
            .getNamespaceURI());
        XmlType xtype = (XmlType)info.getTypeClass().getAnnotation(XmlType.class);
        if (xtype != null) {
            typeInfo.setExtensibleElements(xtype.extensibleElements());
            typeInfo.setExtensibleAttributes(xtype.extensibleAttributes());
        } else {
            typeInfo.setExtensibleElements(getConfiguration().isDefaultExtensibleElements());
            typeInfo.setExtensibleAttributes(getConfiguration().isDefaultExtensibleAttributes());
        }

        typeInfo.setDefaultMinOccurs(getConfiguration().getDefaultMinOccurs());
        typeInfo.setDefaultNillable(getConfiguration().isDefaultNillable());

        BeanType type = new BeanType(typeInfo);
        type.setTypeMapping(getTypeMapping());
        type.setSchemaType(typeName);

        return type;
    }

    @Override
    public Type createEnumType(TypeClassInfo info) {
        EnumType type = new EnumType();

        type.setSchemaType(createQName(info.getTypeClass()));
        type.setTypeClass(info.getTypeClass());
        type.setTypeMapping(getTypeMapping());

        return type;
    }

    @SuppressWarnings("unchecked")
    @Override
    public QName createQName(Class typeClass) {
        String name = null;
        String ns = null;

        XmlType xtype = (XmlType)typeClass.getAnnotation(XmlType.class);
        if (xtype != null) {
            name = xtype.name();
            ns = xtype.namespace();
        }

        return createQName(typeClass, name, ns);
    }

    private QName createQName(Class typeClass, String name, String ns) {
        String clsName = typeClass.getName();
        if (name == null || name.length() == 0) {
            name = ServiceUtils.makeServiceNameFromClassName(typeClass);
        }

        if (ns == null || ns.length() == 0) {
            ns = NamespaceHelper.makeNamespaceFromClassName(clsName, "http");
        }

        return new QName(ns, name);
    }

    @Override
    protected boolean isEnum(Class javaType) {
        return javaType.isEnum();
    }
}
