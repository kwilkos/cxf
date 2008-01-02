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
 * At the level of the data binding, the 'root elements' are defined by the WSDL message parts.
 * Additional classes that participate are termed 'override' classes.
 * 
 * Aegis, unlike JAXB, has no concept of a 'root element'. So, an application that 
 * uses Aegis without a web service has to either depend on xsi:type (at least for 
 * root elements) or have its own mapping from elements to classes, and pass the 
 * resulting Class objects to the readers.
 * 
 * At this level, the initial set of classes are just the initial set of classes.
 * If the application leaves this list empty, and reads, then no .aegis.xml files 
 * are used unless the application feeds in a Class&lt;T&gt; for the root of a 
 * particular item read. Specifically, if the application just leaves it to Aegis to
 * map an xsi:type spec to a class, Aegis can't know that some arbitrary class in
 * some arbitrary package is mapped to a particular schema type by QName in a
 * mapping XML file. 
 * 
 */
public class AegisContext {
    // perhaps this should be SoapConstants.XSD? Or perhaps the code that looks for that should look for this?
    private static final String DEFAULT_ENCODING_STYLE_URI = "urn:aegis.cxf.apache.org:defaultEncoding";
    private boolean writeXsiTypes;
    private boolean readXsiTypes = true;
    private String mappingNamespaceURI = DEFAULT_ENCODING_STYLE_URI;

    private TypeMappingRegistry typeMappingRegistry;
    private Set<String> rootClassNames;
    private Set<Class<?>> rootClasses;
    private Set<QName> rootTypeQNames;
    // this type mapping is the front of the chain of delegating type mappings.
    private TypeMapping typeMapping;
    private Set<Type> rootTypes;
    private Map<Class<?>, String> beanImplementationMap;
    private Configuration configuration;
    
    /**
     * Construct a context.
     */
    public AegisContext() {
        beanImplementationMap = new HashMap<Class<?>, String>();
        rootClasses = new HashSet<Class<?>>();
        rootTypeQNames = new HashSet<QName>();
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
        processRootTypes();
    }
    
    
    public AegisReader<org.w3c.dom.Element>
    createDomElementReader() {
        return new AegisElementDataReader(this);
    }
    
    public AegisReader<XMLStreamReader>
    createXMLStreamReader() {
        return new AegisXMLStreamDataReader(this);
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
     * If a class was provided as part of the 'root' list, retrieve it's Type by
     * Class.
     * @param clazz
     * @return
     */
    public Type getRootType(Class clazz) {
        if (rootClasses.contains(clazz)) {
            return typeMapping.getType(clazz);
        } else {
            return null;
        }
    }
    
    /**
     * If a class was provided as part of the root list, retrieve it's Type by schema
     * type QName.
     * @param schemaTypeName
     * @return
     */
    public Type getRootType(QName schemaTypeName) {
        if (rootTypeQNames.contains(schemaTypeName)) {
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
    private void processRootTypes() {
        rootTypes = new HashSet<Type>();
        // app may have already supplied classes.
        if (rootClasses == null) {
            rootClasses = new HashSet<Class<?>>();
        }
        rootTypeQNames = new HashSet<QName>();
        if (this.rootClassNames != null) {
            for (String typeName : rootClassNames) {
                Class c = null;
                try {
                    c = ClassLoaderUtils.loadClass(typeName, TypeUtil.class);
                } catch (ClassNotFoundException e) {
                    throw new DatabindingException("Could not find override type class: " + typeName, e);
                }
                
                rootClasses.add(c);
            }
        }
         
        for (Class<?> c : rootClasses) {
            Type t = typeMapping.getType(c);
            if (t == null) {
                t = typeMapping.getTypeCreator().createType(c);
                typeMapping.register(t);
            }
            rootTypeQNames.add(t.getSchemaType());
            if (t instanceof BeanType) {
                BeanType bt = (BeanType)t;
                bt.getTypeInfo().setExtension(true);
                rootTypes.add(bt);
            }
        }
    }

    /**
     * Retrieve the set of root class names. Note that if the application
     * specifies the root classes by Class instead of by name, this will
     * return null.
     * @return
     */
    public Set<String> getRootClassNames() {
        return rootClassNames;
    }
    
    /**
     * Set the root class names. This function is a convenience for Spring
     * configuration. It sets the same underlying 
     * collection as {@link #setRootClasses(Set)}.
     * 
     * @param classNames
     */
    public void setRootClassNames(Set<String> classNames) {
        rootClassNames = classNames;
    }

    /** 
     * Return the type mapping configuration associated with this context.
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
     * Set the configuration object. The configuration specifies default
     * type mapping behaviors.
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

    /**
     * Controls whether Aegis writes xsi:type attributes on all elements.
     * False by default.
     * @param flag
     */
    public void setWriteXsiTypes(boolean flag) {
        this.writeXsiTypes = flag;
    }

    /**
     * Controls the use of xsi:type attributes when reading objects. By default,
     * xsi:type reading is enabled. When disabled, Aegis will only map for objects
     * in the type mapping. 
     * @param flag
     */
    public void setReadXsiTypes(boolean flag) {
        this.readXsiTypes = flag;
    }

    /**
     * Return the type mapping object used by this context.
     * @return
     */
    public TypeMapping getTypeMapping() {
        return typeMapping;
    }

    /**
     * Set the type mapping object used by this context.
     * @param typeMapping
     */
    public void setTypeMapping(TypeMapping typeMapping) {
        this.typeMapping = typeMapping;
    }

    /**
     * Retrieve the Aegis type objects for the root classes.
     * @return the set of type objects.
     */
    public Set<Type> getRootTypes() {
        return rootTypes;
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

    public Set<Class<?>> getRootClasses() {
        return rootClasses;
    }

    public void setRootClasses(Set<Class<?>> rootClasses) {
        this.rootClasses = rootClasses;
    }

}
