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
package org.apache.cxf.aegis;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamReader;

import org.apache.cxf.aegis.type.Configuration;
import org.apache.cxf.aegis.type.DefaultTypeMappingRegistry;
import org.apache.cxf.aegis.type.Type;
import org.apache.cxf.aegis.type.TypeMapping;
import org.apache.cxf.aegis.type.TypeMappingRegistry;
import org.apache.cxf.aegis.type.TypeUtil;
import org.apache.cxf.aegis.type.basic.BeanType;
import org.apache.cxf.common.classloader.ClassLoaderUtils;
import org.apache.cxf.common.util.SOAPConstants;

/**
 * The Aegis Databinding context object. This object coordinates the data binding process: reading
 * and writing XML.
 * 
 * By default, this object will use the DefaultTypeMapping registry and set up two type mappings:
 * the default set, plus an empty one that will be used for classes mapped here. Applications
 * may replace the type mapping registry and/or the mappings, though there are currently no examples
 * that show how to do this or how it would be useful.
 * 
 * This class has no API that maps from a Class or QName to a Type or visa versa. Our goal is to allow
 * Aegis-sans-CXF, but the marshal/unmarshal-level APIs aren't sorted out yet.
 * 
 */
public class AegisContext {
    // perhaps this should be SoapConstants.XSD? Or perhaps the code that looks for that should look for this?
    private static final String DEFAULT_ENCODING_STYLE_URI = "urn:aegis.cxf.apache.org:defaultEncoding";
    private boolean writeXsiTypes;
    private boolean readXsiTypes = true;
    private String mappingNamespaceURI = DEFAULT_ENCODING_STYLE_URI;

    private TypeMappingRegistry typeMappingRegistry;
    private Set<String> overrideTypes;
    private Set<Class<?>> overrideClasses;
    private Set<QName> overrideQNames;
    // this type mapping is the front of the chain of delegating type mappings.
    private TypeMapping typeMapping;
    private Set<Type> additionalTypes;
    private Map<Class<?>, String> beanImplementationMap;
    private Configuration configuration;
    
    /**
     * Construct a context.
     */
    public AegisContext() {
        beanImplementationMap = new HashMap<Class<?>, String>();
        overrideClasses = new HashSet<Class<?>>();
        overrideQNames = new HashSet<QName>();
    }

    /**
     * Initialize the context. The encodingStyleURI allows .aegis.xml files to have multiple mappings 
     * for, say, SOAP 1.1 versus SOAP 1.2. Passing null uses a default URI. 
     * @param mappingNamespaceURI URI to select mappings based on the encoding.
     */
    public void initialize() {
        if (typeMappingRegistry == null) {
            typeMappingRegistry = new DefaultTypeMappingRegistry(true);
        } 
        if (configuration != null) {
            typeMappingRegistry.setConfiguration(configuration);
        }
        // The use of the XSD URI in the mapping is, MAGIC.
        typeMapping  = typeMappingRegistry.createTypeMapping(SOAPConstants.XSD, true);
        typeMappingRegistry.register(mappingNamespaceURI, typeMapping);
        processOverrideTypes();
    }
    
    
    public <Reader extends AbstractAegisDataReaderImpl, Source> 
    Reader createReader(Class<Reader> readerClass, Class<Source> sourceClass) {
        if (sourceClass == org.w3c.dom.Element.class) {
            return readerClass.cast(new AegisElementDataReader(this));
        } else if (sourceClass == XMLStreamReader.class) {
            return readerClass.cast(new AegisXMLStreamDataReader(this));
        }
        return null; // throw?
    }
    
    /**
     * Retrieve the type mapping registry object.
     * @return the registry.
     */
    public TypeMappingRegistry getTypeMappingRegistry() {
        return typeMappingRegistry;
    }

    /**
     * Set the type mapping registry. Call this after construction and before 'initialize'.
     * @param typeMappingRegistry
     */
    public void setTypeMappingRegistry(TypeMappingRegistry typeMappingRegistry) {
        this.typeMappingRegistry = typeMappingRegistry;
    }
    
    /**
     * If a class was provided as part of the 'override' list, retrieve it's Type by
     * Class.
     * @param clazz
     * @return
     */
    public Type getOverrideType(Class clazz) {
        if (overrideClasses.contains(clazz)) {
            return typeMapping.getType(clazz);
        } else {
            return null;
        }
    }
    
    /**
     * If a class was provided as part of the override list, retrieve it's Type by schema
     * type QName.
     * @param schemaTypeName
     * @return
     */
    public Type getOverrideType(QName schemaTypeName) {
        if (overrideQNames.contains(schemaTypeName)) {
            return typeMapping.getType(schemaTypeName);
        } else {
            return null;
        }
    }
    
    /**
     * Examine a list of override classes, and register all of them.
     * @param tm      type manager for this binding
     * @param classes list of class names
     */
    private void processOverrideTypes() {
        additionalTypes = new HashSet<Type>();
        overrideClasses = new HashSet<Class<?>>();
        overrideQNames = new HashSet<QName>();
        if (this.overrideTypes != null) {
            for (String typeName : overrideTypes) {
                Class c = null;
                try {
                    c = ClassLoaderUtils.loadClass(typeName, TypeUtil.class);
                } catch (ClassNotFoundException e) {
                    throw new DatabindingException("Could not find override type class: " + typeName, e);
                }
                
                overrideClasses.add(c);
                
                Type t = typeMapping.getType(c);
                if (t == null) {
                    t = typeMapping.getTypeCreator().createType(c);
                    typeMapping.register(t);
                }
                overrideQNames.add(t.getSchemaType());
                if (t instanceof BeanType) {
                    BeanType bt = (BeanType)t;
                    bt.getTypeInfo().setExtension(true);
                    additionalTypes.add(bt);
                }
            }
        }
    }

    public Set<String> getOverrideTypes() {
        return overrideTypes;
    }
    
    public void setOverrideTypes(Set<String> typeNames) {
        overrideTypes = typeNames;
    }

    /** 
     * Return the type mapping configuration associated with this context.
     * The configuration is retrieved from the type mapping registry.
     * @return Returns the configuration.
     */
    public Configuration getConfiguration() {
        if (typeMappingRegistry != null) {
            return typeMappingRegistry.getConfiguration();
        } else {
            return configuration;
        }
            
    }

    /**
     * Set the configuration for this databinding object.
     * @param configuration The configuration to set.
     */
    public void setConfiguration(Configuration newConfiguration) {
        this.configuration = newConfiguration;
        if (typeMappingRegistry != null) {
            typeMappingRegistry.setConfiguration(configuration);
        }
    }

    public boolean isWriteXsiTypes() {
        return writeXsiTypes;
    }

    public boolean isReadXsiTypes() {
        return readXsiTypes;
    }

    public void setWriteXsiTypes(boolean flag) {
        this.writeXsiTypes = flag;
    }

    public void setReadXsiTypes(boolean flag) {
        this.readXsiTypes = flag;
    }

    public TypeMapping getTypeMapping() {
        return typeMapping;
    }

    public void setTypeMapping(TypeMapping typeMapping) {
        this.typeMapping = typeMapping;
    }

    public Set<Type> getAdditionalTypes() {
        return additionalTypes;
    }
    
    public Map<Class<?>, String> getBeanImplementationMap() {
        return beanImplementationMap;
    }

    public void setBeanImplementationMap(Map<Class<?>, String> beanImplementationMap) {
        this.beanImplementationMap = beanImplementationMap;
    }

    public void setMappingNamespaceURI(String uri) {
        this.mappingNamespaceURI = uri;
    }

}
