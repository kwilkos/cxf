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
package org.apache.cxf.aegis.type;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.Source;

import org.w3c.dom.Document;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.cxf.aegis.DatabindingException;
import org.apache.cxf.aegis.type.basic.Base64Type;
import org.apache.cxf.aegis.type.basic.BigDecimalType;
import org.apache.cxf.aegis.type.basic.BigIntegerType;
import org.apache.cxf.aegis.type.basic.BooleanType;
import org.apache.cxf.aegis.type.basic.CalendarType;
import org.apache.cxf.aegis.type.basic.CharacterType;
import org.apache.cxf.aegis.type.basic.DateTimeType;
import org.apache.cxf.aegis.type.basic.DoubleType;
import org.apache.cxf.aegis.type.basic.FloatType;
import org.apache.cxf.aegis.type.basic.IntType;
import org.apache.cxf.aegis.type.basic.LongType;
import org.apache.cxf.aegis.type.basic.ObjectType;
import org.apache.cxf.aegis.type.basic.ShortType;
import org.apache.cxf.aegis.type.basic.SqlDateType;
import org.apache.cxf.aegis.type.basic.StringType;
import org.apache.cxf.aegis.type.basic.TimeType;
import org.apache.cxf.aegis.type.basic.TimestampType;
import org.apache.cxf.aegis.type.basic.URIType;
import org.apache.cxf.aegis.type.java5.Java5TypeCreator;
import org.apache.cxf.aegis.type.mtom.AbstractXOPType;
import org.apache.cxf.aegis.type.mtom.DataHandlerType;
import org.apache.cxf.aegis.type.mtom.DataSourceType;
import org.apache.cxf.aegis.type.xml.DocumentType;
import org.apache.cxf.aegis.type.xml.JDOMDocumentType;
import org.apache.cxf.aegis.type.xml.JDOMElementType;
import org.apache.cxf.aegis.type.xml.SourceType;
import org.apache.cxf.aegis.type.xml.XMLStreamReaderType;
import org.apache.cxf.binding.soap.Soap11;
import org.apache.cxf.common.classloader.ClassLoaderUtils;
import org.apache.cxf.common.util.SOAPConstants;
import org.apache.cxf.common.util.XMLSchemaQNames;
import org.jdom.Element;

/**
 * The default implementation of TypeMappingRegistry.
 * 
 * @author <a href="mailto:dan@envoisolutions.com">Dan Diephouse</a>
 * @since Feb 22, 2004
 */
public final class DefaultTypeMappingRegistry extends AbstractTypeMappingRegistry implements
    TypeMappingRegistry {

    private static final Log LOG = LogFactory.getLog(DefaultTypeMappingRegistry.class);

    private Map<String, TypeMapping> registry;

    private TypeMapping defaultTM;

    private TypeCreator typeCreator;

    public DefaultTypeMappingRegistry() {
        this(null, false, false);
    }

    public DefaultTypeMappingRegistry(TypeCreator typeCreator, boolean createDefault) {
        this(typeCreator, createDefault, false);
    }

    public DefaultTypeMappingRegistry(boolean createDefault) {
        this(null, createDefault, false);
    }

    public DefaultTypeMappingRegistry(TypeCreator typeCreator, 
                                      boolean createDefault, 
                                      boolean enableMtomXmime) {
        registry = Collections.synchronizedMap(new HashMap<String, TypeMapping>());

        this.typeCreator = typeCreator;

        if (createDefault) {
            createDefaultMappings(enableMtomXmime);
        }
    }

    /**
     * {@inheritDoc}
     */
    public TypeMapping register(String namespaceURI, TypeMapping mapping) {
        TypeMapping previous = registry.get(namespaceURI);

        mapping.setEncodingStyleURI(namespaceURI);

        registry.put(namespaceURI, mapping);

        return previous;
    }

    public void registerDefault(TypeMapping mapping) {
        defaultTM = mapping;
    }

    /**
     * @see org.apache.cxf.aegis.type.TypeMappingRegistry#getDefaultTypeMapping()
     */
    public TypeMapping getDefaultTypeMapping() {
        return defaultTM;
    }

    /**
     * @see org.apache.cxf.aegis.type.TypeMappingRegistry#getRegisteredEncodingStyleURIs()
     */
    public String[] getRegisteredEncodingStyleURIs() {
        return registry.keySet().toArray(new String[registry.size()]);
    }

    /**
     * @see org.apache.cxf.aegis.type.TypeMappingRegistry#getTypeMapping(java.lang.String)
     */
    public TypeMapping getTypeMapping(String encodingStyleURI) {
        return registry.get(encodingStyleURI);
    }

    /**
     * @see org.apache.cxf.aegis.type.TypeMappingRegistry#createTypeMapping(boolean)
     */
    public TypeMapping createTypeMapping(boolean autoTypes) {
        return createTypeMapping(getDefaultTypeMapping(), autoTypes);
    }

    /**
     * @see org.apache.cxf.aegis.type.TypeMappingRegistry#createTypeMapping(String,
     *      boolean)
     */
    public TypeMapping createTypeMapping(String parentNamespace, boolean autoTypes) {
        return createTypeMapping(getTypeMapping(parentNamespace), autoTypes);
    }

    protected TypeMapping createTypeMapping(TypeMapping parent, boolean autoTypes) {
        CustomTypeMapping tm = new CustomTypeMapping(parent);

        if (autoTypes) {
            tm.setTypeCreator(createTypeCreator());
        }

        return tm;
    }

    public TypeCreator getTypeCreator() {
        if (typeCreator == null) {
            typeCreator = createTypeCreator();
        }

        return typeCreator;
    }

    public void setTypeCreator(TypeCreator typeCreator) {
        this.typeCreator = typeCreator;
    }

    protected TypeCreator createTypeCreator() {
        AbstractTypeCreator xmlCreator = createRootTypeCreator();

        Java5TypeCreator j5Creator = new Java5TypeCreator();
        j5Creator.setNextCreator(createDefaultTypeCreator());
        j5Creator.setConfiguration(getConfiguration());
        xmlCreator.setNextCreator(j5Creator);

        return xmlCreator;
    }

    protected AbstractTypeCreator createRootTypeCreator() {
        AbstractTypeCreator creator = new XMLTypeCreator();
        creator.setConfiguration(getConfiguration());
        return creator;
    }

    protected AbstractTypeCreator createDefaultTypeCreator() {
        AbstractTypeCreator creator = new DefaultTypeCreator();
        creator.setConfiguration(getConfiguration());
        return creator;
    }

    /**
     * @see org.apache.cxf.aegis.type.TypeMappingRegistry#unregisterTypeMapping(java.lang.String)
     */
    public TypeMapping unregisterTypeMapping(String encodingStyleURI) {
        TypeMapping tm = registry.get(encodingStyleURI);
        registry.remove(encodingStyleURI);
        return tm;
    }

    public boolean removeTypeMapping(TypeMapping mapping) {
        int n = 0;

        for (Iterator itr = registry.values().iterator(); itr.hasNext();) {
            if (itr.next().equals(mapping)) {
                itr.remove();
                n++;
            }
        }

        return n > 0;
    }

    /**
     * @see org.apache.cxf.aegis.type.TypeMappingRegistry#clear()
     */
    public void clear() {
        registry.clear();
    }

    public TypeMapping createDefaultMappings() {
        return createDefaultMappings(false);
    }

    public TypeMapping createDefaultMappings(boolean enableMtomXmime) {
        TypeMapping tm = createTypeMapping(false);

        createDefaultMappings(enableMtomXmime, tm);

        // Create a Type Mapping for SOAP 1.1 Encoding
        TypeMapping soapTM = createTypeMapping(tm, false);

        register(soapTM, boolean.class, Soap11.ENCODED_BOOLEAN, new BooleanType());
        register(soapTM, int.class, Soap11.ENCODED_INT, new IntType());
        register(soapTM, short.class, Soap11.ENCODED_SHORT, new ShortType());
        register(soapTM, double.class, Soap11.ENCODED_DOUBLE, new DoubleType());
        register(soapTM, float.class, Soap11.ENCODED_FLOAT, new FloatType());
        register(soapTM, long.class, Soap11.ENCODED_LONG, new LongType());
        register(soapTM, char.class, Soap11.ENCODED_CHAR, new CharacterType());
        register(soapTM, Character.class, Soap11.ENCODED_CHAR, new CharacterType());
        register(soapTM, String.class, Soap11.ENCODED_STRING, new StringType());
        register(soapTM, Boolean.class, Soap11.ENCODED_BOOLEAN, new BooleanType());
        register(soapTM, Integer.class, Soap11.ENCODED_INT, new IntType());
        register(soapTM, Short.class, Soap11.ENCODED_SHORT, new ShortType());
        register(soapTM, Double.class, Soap11.ENCODED_DOUBLE, new DoubleType());
        register(soapTM, Float.class, Soap11.ENCODED_FLOAT, new FloatType());
        register(soapTM, Long.class, Soap11.ENCODED_LONG, new LongType());
        register(soapTM, Date.class, Soap11.ENCODED_DATETIME, new DateTimeType());
        register(soapTM, java.sql.Date.class, Soap11.ENCODED_DATETIME, new SqlDateType());
        register(soapTM, Calendar.class, Soap11.ENCODED_DATETIME, new CalendarType());
        register(soapTM, byte[].class, Soap11.ENCODED_BASE64, new Base64Type());
        register(soapTM, BigDecimal.class, Soap11.ENCODED_DECIMAL, new BigDecimalType());
        register(soapTM, BigInteger.class, Soap11.ENCODED_INTEGER, new BigIntegerType());

        register(soapTM, boolean.class, XMLSchemaQNames.XSD_BOOLEAN, new BooleanType());
        register(soapTM, int.class, XMLSchemaQNames.XSD_INT, new IntType());
        register(soapTM, short.class, XMLSchemaQNames.XSD_SHORT, new ShortType());
        register(soapTM, double.class, XMLSchemaQNames.XSD_DOUBLE, new DoubleType());
        register(soapTM, float.class, XMLSchemaQNames.XSD_FLOAT, new FloatType());
        register(soapTM, long.class, XMLSchemaQNames.XSD_LONG, new LongType());
        register(soapTM, String.class, XMLSchemaQNames.XSD_STRING, new StringType());
        register(soapTM, Boolean.class, XMLSchemaQNames.XSD_BOOLEAN, new BooleanType());
        register(soapTM, Integer.class, XMLSchemaQNames.XSD_INT, new IntType());
        register(soapTM, Short.class, XMLSchemaQNames.XSD_SHORT, new ShortType());
        register(soapTM, Double.class, XMLSchemaQNames.XSD_DOUBLE, new DoubleType());
        register(soapTM, Float.class, XMLSchemaQNames.XSD_FLOAT, new FloatType());
        register(soapTM, Long.class, XMLSchemaQNames.XSD_LONG, new LongType());
        register(soapTM, Date.class, XMLSchemaQNames.XSD_DATETIME, new DateTimeType());
        register(soapTM, java.sql.Date.class, XMLSchemaQNames.XSD_DATETIME, new SqlDateType());
        register(soapTM, Time.class, XMLSchemaQNames.XSD_TIME, new TimeType());
        register(soapTM, Timestamp.class, XMLSchemaQNames.XSD_DATETIME, new TimestampType());
        register(soapTM, Calendar.class, XMLSchemaQNames.XSD_DATETIME, new CalendarType());
        register(soapTM, byte[].class, XMLSchemaQNames.XSD_BASE64, new Base64Type());
        register(soapTM, BigDecimal.class, XMLSchemaQNames.XSD_DECIMAL, new BigDecimalType());
        register(soapTM, URI.class, XMLSchemaQNames.XSD_URI, new URIType());
        register(soapTM, Document.class, XMLSchemaQNames.XSD_ANY, new DocumentType());
        register(soapTM, Source.class, XMLSchemaQNames.XSD_ANY, new SourceType());
        register(soapTM, XMLStreamReader.class, XMLSchemaQNames.XSD_ANY, new XMLStreamReaderType());
        register(soapTM, Element.class, XMLSchemaQNames.XSD_ANY, new JDOMElementType());
        register(soapTM, org.jdom.Document.class, XMLSchemaQNames.XSD_ANY, new JDOMDocumentType());
        register(soapTM, Object.class, XMLSchemaQNames.XSD_ANY, new ObjectType());
        // unless there is customization, we use no expectedContentTypes.
        register(soapTM, DataSource.class, XMLSchemaQNames.XSD_BASE64, new DataSourceType(enableMtomXmime,
                                                                                          null));
        register(soapTM, DataHandler.class, XMLSchemaQNames.XSD_BASE64, new DataHandlerType(enableMtomXmime,
                                                                                            null));
        register(soapTM, BigInteger.class, XMLSchemaQNames.XSD_INTEGER, new BigIntegerType());

        register(Soap11.SOAP_ENCODING_URI, soapTM);
        register(SOAPConstants.XSD, tm);
        registerDefault(tm);

        return tm;
    }

    protected void createDefaultMappings(TypeMapping tm) {
        createDefaultMappings(false, tm);
    }

    protected void createDefaultMappings(boolean enableMtomXmime, TypeMapping tm) {
        register(tm, boolean.class, XMLSchemaQNames.XSD_BOOLEAN, new BooleanType());
        register(tm, int.class, XMLSchemaQNames.XSD_INT, new IntType());
        register(tm, short.class, XMLSchemaQNames.XSD_SHORT, new ShortType());
        register(tm, double.class, XMLSchemaQNames.XSD_DOUBLE, new DoubleType());
        register(tm, float.class, XMLSchemaQNames.XSD_FLOAT, new FloatType());
        register(tm, long.class, XMLSchemaQNames.XSD_LONG, new LongType());
        register(tm, char.class, XMLSchemaQNames.XSD_STRING, new CharacterType());
        register(tm, Character.class, XMLSchemaQNames.XSD_STRING, new CharacterType());
        register(tm, String.class, XMLSchemaQNames.XSD_STRING, new StringType());
        register(tm, Boolean.class, XMLSchemaQNames.XSD_BOOLEAN, new BooleanType());
        register(tm, Integer.class, XMLSchemaQNames.XSD_INT, new IntType());
        register(tm, Short.class, XMLSchemaQNames.XSD_SHORT, new ShortType());
        register(tm, Double.class, XMLSchemaQNames.XSD_DOUBLE, new DoubleType());
        register(tm, Float.class, XMLSchemaQNames.XSD_FLOAT, new FloatType());
        register(tm, Long.class, XMLSchemaQNames.XSD_LONG, new LongType());
        register(tm, Date.class, XMLSchemaQNames.XSD_DATETIME, new DateTimeType());
        register(tm, java.sql.Date.class, XMLSchemaQNames.XSD_DATETIME, new SqlDateType());
        register(tm, Time.class, XMLSchemaQNames.XSD_TIME, new TimeType());
        register(tm, Timestamp.class, XMLSchemaQNames.XSD_DATETIME, new TimestampType());
        register(tm, Calendar.class, XMLSchemaQNames.XSD_DATETIME, new CalendarType());
        register(tm, byte[].class, XMLSchemaQNames.XSD_BASE64, new Base64Type());
        register(tm, BigDecimal.class, XMLSchemaQNames.XSD_DECIMAL, new BigDecimalType());
        register(tm, BigInteger.class, XMLSchemaQNames.XSD_INTEGER, new BigIntegerType());
        register(tm, URI.class, XMLSchemaQNames.XSD_URI, new URIType());
        register(tm, Document.class, XMLSchemaQNames.XSD_ANY, new DocumentType());
        register(tm, Source.class, XMLSchemaQNames.XSD_ANY, new SourceType());
        register(tm, XMLStreamReader.class, XMLSchemaQNames.XSD_ANY, new XMLStreamReaderType());
        register(tm, Element.class, XMLSchemaQNames.XSD_ANY, new JDOMElementType());
        register(tm, org.jdom.Document.class, XMLSchemaQNames.XSD_ANY, new JDOMDocumentType());
        register(tm, Object.class, XMLSchemaQNames.XSD_ANY, new ObjectType());

        QName mtomBase64 = XMLSchemaQNames.XSD_BASE64;
        if (enableMtomXmime) {
            mtomBase64 = AbstractXOPType.XML_MIME_BASE64;
        }

        register(tm, DataSource.class, mtomBase64, new DataSourceType(enableMtomXmime, null));
        register(tm, DataHandler.class, mtomBase64, new DataHandlerType(enableMtomXmime, null));

        registerIfAvailable(tm, "javax.xml.datatype.Duration", XMLSchemaQNames.XSD_DURATION,
                            "org.apache.cxf.aegis.type.java5.DurationType");
        registerIfAvailable(tm, "javax.xml.datatype.XMLGregorianCalendar", XMLSchemaQNames.XSD_DATE,
                            "org.apache.cxf.aegis.type.java5.XMLGregorianCalendarType");
        registerIfAvailable(tm, "javax.xml.datatype.XMLGregorianCalendar", XMLSchemaQNames.XSD_TIME,
                            "org.apache.cxf.aegis.type.java5.XMLGregorianCalendarType");
        registerIfAvailable(tm, "javax.xml.datatype.XMLGregorianCalendar", XMLSchemaQNames.XSD_G_DAY,
                            "org.apache.cxf.aegis.type.java5.XMLGregorianCalendarType");
        registerIfAvailable(tm, "javax.xml.datatype.XMLGregorianCalendar", XMLSchemaQNames.XSD_G_MONTH,
                            "org.apache.cxf.aegis.type.java5.XMLGregorianCalendarType");
        registerIfAvailable(tm, "javax.xml.datatype.XMLGregorianCalendar", XMLSchemaQNames.XSD_G_MONTH_DAY,
                            "org.apache.cxf.aegis.type.java5.XMLGregorianCalendarType");
        registerIfAvailable(tm, "javax.xml.datatype.XMLGregorianCalendar", XMLSchemaQNames.XSD_G_YEAR,
                            "org.apache.cxf.aegis.type.java5.XMLGregorianCalendarType");
        registerIfAvailable(tm, "javax.xml.datatype.XMLGregorianCalendar", XMLSchemaQNames.XSD_G_YEAR_MONTH,
                            "org.apache.cxf.aegis.type.java5.XMLGregorianCalendarType");
        registerIfAvailable(tm, "javax.xml.datatype.XMLGregorianCalendar", XMLSchemaQNames.XSD_DATETIME,
                            "org.apache.cxf.aegis.type.java5.XMLGregorianCalendarType");
    }

    protected void registerIfAvailable(TypeMapping tm, 
                                       String className, 
                                       QName typeName, 
                                       String typeClassName) {
        try {
            Class cls = ClassLoaderUtils.loadClass(className, getClass());
            Class typeCls = ClassLoaderUtils.loadClass(typeClassName, getClass());
            try {
                Type type = (Type)typeCls.newInstance();

                register(tm, cls, typeName, type);
            } catch (InstantiationException e) {
                throw new DatabindingException("Couldn't instantiate Type ", e);
            } catch (IllegalAccessException e) {
                throw new DatabindingException("Couldn't instantiate Type ", e);
            }
        } catch (ClassNotFoundException e) {
            LOG.debug("Could not find optional Type " + className + ". Skipping.");
        }

    }

    protected void register(TypeMapping tm, Class class1, QName name, Type type) {
        if (!getConfiguration().isDefaultNillable()) {
            type.setNillable(false);
        }

        tm.register(class1, name, type);
    }
}
