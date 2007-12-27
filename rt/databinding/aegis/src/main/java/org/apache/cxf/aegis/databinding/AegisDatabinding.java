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
package org.apache.cxf.aegis.databinding;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.w3c.dom.Node;

import org.apache.cxf.aegis.DatabindingException;
import org.apache.cxf.aegis.type.AbstractTypeCreator.TypeClassInfo;
import org.apache.cxf.aegis.type.Configuration;
import org.apache.cxf.aegis.type.DefaultTypeMappingRegistry;
import org.apache.cxf.aegis.type.Type;
import org.apache.cxf.aegis.type.TypeCreator;
import org.apache.cxf.aegis.type.TypeMapping;
import org.apache.cxf.aegis.type.TypeMappingRegistry;
import org.apache.cxf.aegis.type.TypeUtil;
import org.apache.cxf.aegis.type.basic.BeanType;
import org.apache.cxf.common.classloader.ClassLoaderUtils;
import org.apache.cxf.common.util.SOAPConstants;
import org.apache.cxf.common.xmlschema.SchemaCollection;
import org.apache.cxf.databinding.DataBinding;
import org.apache.cxf.databinding.DataReader;
import org.apache.cxf.databinding.DataWriter;
import org.apache.cxf.databinding.source.AbstractDataBinding;
import org.apache.cxf.frontend.MethodDispatcher;
import org.apache.cxf.frontend.SimpleMethodDispatcher;
import org.apache.cxf.service.Service;
import org.apache.cxf.service.factory.ServiceConstructionException;
import org.apache.cxf.service.model.AbstractMessageContainer;
import org.apache.cxf.service.model.FaultInfo;
import org.apache.cxf.service.model.MessagePartInfo;
import org.apache.cxf.service.model.OperationInfo;
import org.apache.cxf.service.model.ServiceInfo;
import org.apache.cxf.wsdl.WSDLConstants;
import org.apache.ws.commons.schema.XmlSchema;
import org.apache.ws.commons.schema.XmlSchemaAnnotated;
import org.apache.ws.commons.schema.utils.NamespaceMap;
import org.jdom.Attribute;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.jdom.output.DOMOutputter;

/**
 * Handles DataBinding functions for Aegis.
 * <p>
 * NOTE: There is an assumed 1:1 mapping between an AegisDatabinding and a Service;
 * the code in here gets and puts properties on the service. This looks as if it's leftover
 * from some idea of sharing a databinding amongst services, which I (bimargulies) didn't
 * think was especially valid. Why not just keep these items right here in the data binding?
 * 
 */
public class AegisDatabinding extends AbstractDataBinding implements DataBinding {
    
    public static final String WRITE_XSI_TYPE_KEY = "org.apache.cxf.databinding.aegis.writeXsiType";
    public static final String READ_XSI_TYPE_KEY = "org.apache.cxf.databinding.aegis.readXsiType";
    public static final String OVERRIDE_TYPES_KEY = "org.apache.cxf.databinding.aegis.overrideTypesList";

    protected static final int IN_PARAM = 0;
    protected static final int OUT_PARAM = 1;
    protected static final int FAULT_PARAM = 2;
    
    static final String OLD_WRITE_XSI_TYPE_KEY = "writeXsiType";
    static final String OLD_OVERRIDE_TYPES_KEY = "overrideTypesList";
    static final String OLD_READ_XSI_TYPE_KEY = "readXsiType";
    private boolean writeXsiTypes;
    private boolean readXsiTypes = true;

    private TypeMappingRegistry typeMappingRegistry;
    private Map<MessagePartInfo, Type> part2Type;
    private Set<String> overrideTypes;
    private Set<Class<?>> overrideClasses;
    private Set<QName> overrideQNames;
    private Service service;
    // There's a split personality in here. Much of the code of Aegis assumes that 
    // types get registered based an 'encoding style' URL, presumably either Soap 1.1 or 1.2.
    // However, the override types, and apparently some others, end up registered against
    // an extra type mapping object created for the service's namespace. This is odd insofar
    // as the XML mapping for for the service could have a mapping uri in it.
    // Until this is better understood, we keep this extra one around.
    private TypeMapping serviceTypeMapping;
    private boolean isInitialized;
    
    public AegisDatabinding() {
        super();
        this.typeMappingRegistry = new DefaultTypeMappingRegistry(true);
        part2Type = new HashMap<MessagePartInfo, Type>();
    }
    
    public static boolean isOverrideTypesKey(String key) {
        return OVERRIDE_TYPES_KEY.equals(key)
            || OLD_OVERRIDE_TYPES_KEY.equals(key);
    }
    
    private void ensureInitialized() {
        if (!isInitialized) {
            initializeWithoutService();
        }
    }

    @SuppressWarnings("unchecked")
    public <T> DataReader<T> createReader(Class<T> cls) {
        ensureInitialized();
        if (cls.equals(XMLStreamReader.class)) {
            return (DataReader<T>) new XMLStreamDataReader(this);
        } else if (cls.equals(Node.class)) {
            return (DataReader<T>) new ElementDataReader(this);
        } else {
            throw new UnsupportedOperationException();
        }
    }

    @SuppressWarnings("unchecked")
    public <T> DataWriter<T> createWriter(Class<T> cls) {
        ensureInitialized();
        if (cls.equals(XMLStreamWriter.class)) {
            return (DataWriter<T>)new XMLStreamDataWriter(this);
        } else if (cls.equals(Node.class)) {
            return (DataWriter<T>) new ElementDataWriter(this);
        } else {
            throw new UnsupportedOperationException();
        }
    }

    public Class<?>[] getSupportedReaderFormats() {
        return new Class[] {XMLStreamReader.class, Node.class};
    }

    public Class<?>[] getSupportedWriterFormats() {
        return new Class[] {XMLStreamWriter.class, Node.class};
    }

    public TypeMappingRegistry getTypeMappingRegistry() {
        return typeMappingRegistry;
    }

    public void setTypeMappingRegistry(TypeMappingRegistry typeMappingRegistry) {
        this.typeMappingRegistry = typeMappingRegistry;
    }
    
    /**
     * This is the central point of override processing on the write side.
     * @param clazz
     * @return
     */
    public Type getOverrideType(Class clazz) {
        ensureInitialized();
        if (overrideClasses.contains(clazz)) {
            return serviceTypeMapping.getType(clazz);
        } else {
            return null;
        }
    }
    
    /**
     * This is the central point of override processing on the read side.
     * @param schemaTypeName
     * @return
     */
    public Type getOverrideType(QName schemaTypeName) {
        ensureInitialized();
        if (overrideQNames.contains(schemaTypeName)) {
            return serviceTypeMapping.getType(schemaTypeName);
        } else {
            return null;
        }
    }
    
    private void initializeWithoutService() {
        isInitialized = true;
        serviceTypeMapping  = typeMappingRegistry.createTypeMapping(SOAPConstants.XSD, true);
        typeMappingRegistry.register("urn:dummyService", serviceTypeMapping);
        processOverrideTypes();
    }

    public void initialize(Service s) {
        isInitialized = true;
        this.service = s;
        
        QName serviceName = s.getServiceInfos().get(0).getName();
        serviceTypeMapping = typeMappingRegistry.createTypeMapping(SOAPConstants.XSD, true);
        typeMappingRegistry.register(serviceName.getNamespaceURI(), serviceTypeMapping);
        
        Object val = s.get(AegisDatabinding.READ_XSI_TYPE_KEY);
        
        if (val == null) {
            val = s.get(AegisDatabinding.OLD_READ_XSI_TYPE_KEY);
        }
        if ("false".equals(val) || Boolean.FALSE.equals(val)) {
            readXsiTypes = false;
        }

        val = s.get(AegisDatabinding.WRITE_XSI_TYPE_KEY);
        
        if (val == null) {
            val = s.get(AegisDatabinding.OLD_WRITE_XSI_TYPE_KEY);
        }
        if ("true".equals(val) || Boolean.TRUE.equals(val)) {
            writeXsiTypes = true;
        }


        Set<Type> deps = new HashSet<Type>();

        for (ServiceInfo info : s.getServiceInfos()) {
            for (OperationInfo opInfo : info.getInterface().getOperations()) {
                if (opInfo.isUnwrappedCapable()) {
                    initializeOperation(s, serviceTypeMapping, opInfo.getUnwrappedOperation(), deps);
                } else {
                    initializeOperation(s, serviceTypeMapping, opInfo, deps);
                }
            }
        }

        List<Type> additional = getAdditionalTypes(s);

        if (additional != null) {
            for (Type t : additional) {
                if (!deps.contains(t)) {
                    deps.add(t);
                }
            }
        }

        createSchemas(s, deps);
        for (ServiceInfo info : s.getServiceInfos()) {
            for (OperationInfo opInfo : info.getInterface().getOperations()) {
                if (opInfo.isUnwrappedCapable()) {
                    initializeOperationTypes(info, opInfo.getUnwrappedOperation());
                } else {
                    initializeOperationTypes(info, opInfo);
                }
            }
        }
    }

    private List<Type> getAdditionalTypes(Service s) {
        List classes = (List)s.get(OVERRIDE_TYPES_KEY);
        if (classes == null) {
            classes = (List)s.get(OLD_OVERRIDE_TYPES_KEY);
        } 
        if (classes != null) {
            if (this.overrideTypes == null) {
                this.overrideTypes = new HashSet<String>();
            }
            
            for (Object classNameObject : classes) {
                String className = (String) classNameObject;
                this.overrideTypes.add(className);
            }
        }

        return processOverrideTypes();
    }

    /**
     * Examine a list of override classes, and register all of them.
     * @param tm      type manager for this binding
     * @param classes list of class names
     * @return        list of Types.
     */
    private List<Type> processOverrideTypes() {
        overrideClasses = new HashSet<Class<?>>();
        overrideQNames = new HashSet<QName>();
        if (this.overrideTypes != null) {
            List<Type> types = new ArrayList<Type>();
            for (String typeName : overrideTypes) {
                Class c = null;
                try {
                    c = ClassLoaderUtils.loadClass(typeName, TypeUtil.class);
                } catch (ClassNotFoundException e) {
                    throw new DatabindingException("Could not find override type class: " + typeName, e);
                }
                
                overrideClasses.add(c);
                
                Type t = serviceTypeMapping.getType(c);
                if (t == null) {
                    t = serviceTypeMapping.getTypeCreator().createType(c);
                    serviceTypeMapping.register(t);
                }
                overrideQNames.add(t.getSchemaType());
                if (t instanceof BeanType) {
                    BeanType bt = (BeanType)t;
                    bt.getTypeInfo().setExtension(true);
                    types.add(bt);
                }
            }
            return types;
        } else {
            return null;
        }
    }

    private void initializeOperation(Service s, TypeMapping serviceTM, OperationInfo opInfo,
                                     Set<Type> deps) {
        try {
            initializeMessage(s, serviceTM, opInfo.getInput(), IN_PARAM, deps);

            if (opInfo.hasOutput()) {
                initializeMessage(s, serviceTM, opInfo.getOutput(), OUT_PARAM, deps);
            }

            for (FaultInfo info : opInfo.getFaults()) {
                initializeMessage(s, serviceTM, info, FAULT_PARAM, deps);
            }

        } catch (DatabindingException e) {
            e.prepend("Error initializing parameters for operation " + opInfo.getName());
            throw e;
        }
    }
    private void initializeOperationTypes(ServiceInfo s, OperationInfo opInfo) {
        try {
            initializeMessageTypes(s, opInfo.getInput(), IN_PARAM);

            if (opInfo.hasOutput()) {
                initializeMessageTypes(s, opInfo.getOutput(), OUT_PARAM);
            }

            for (FaultInfo info : opInfo.getFaults()) {
                initializeMessageTypes(s, info, FAULT_PARAM);
            }

        } catch (DatabindingException e) {
            e.prepend("Error initializing parameters for operation " + opInfo.getName());
            throw e;
        }
    }

    protected void initializeMessage(Service s, TypeMapping serviceTM,
                                     AbstractMessageContainer container, 
                                     int partType, Set<Type> deps) {
        for (Iterator itr = container.getMessageParts().iterator(); itr.hasNext();) {
            MessagePartInfo part = (MessagePartInfo)itr.next();

            Type type = getParameterType(s, serviceTM, part, partType);

            if (part.getXmlSchema() == null) {
                //schema hasn't been filled in yet
                if (type.isAbstract()) {
                    part.setTypeQName(type.getSchemaType());
                } else {
                    part.setElementQName(type.getSchemaType());
                }
            }

            part2Type.put(part, type);

            // QName elName = getSuggestedName(service, op, param)
            deps.add(type);

            addDependencies(deps, type);
        }
    }

    protected void initializeMessageTypes(ServiceInfo s,
                                     AbstractMessageContainer container, 
                                     int partType) {
        SchemaCollection col = s.getXmlSchemaCollection();
        for (Iterator itr = container.getMessageParts().iterator(); itr.hasNext();) {
            MessagePartInfo part = (MessagePartInfo)itr.next();
            if (part.getXmlSchema() == null) {
                if (part.isElement()) {
                    XmlSchemaAnnotated tp = col.getElementByQName(part.getElementQName());
                    part.setXmlSchema(tp);
                } else {
                    XmlSchemaAnnotated tp = col.getTypeByQName(part.getTypeQName());
                    part.setXmlSchema(tp);
                }
            }
        }
    }
    private void addDependencies(Set<Type> deps, Type type) {
        Set<Type> typeDeps = type.getDependencies();
        if (typeDeps != null) {
            for (Type t : typeDeps) {
                if (!deps.contains(t)) {
                    deps.add(t);
                    addDependencies(deps, t);
                }
            }
        }
    }

    private void createSchemas(Service s, Set<Type> deps) {

        Map<String, Set<Type>> tns2Type = new HashMap<String, Set<Type>>();
        for (Type t : deps) {
            String ns = t.getSchemaType().getNamespaceURI();
            Set<Type> types = tns2Type.get(ns);
            if (types == null) {
                types = new HashSet<Type>();
                tns2Type.put(ns, types);
            }
            types.add(t);
        }
        for (ServiceInfo si : s.getServiceInfos()) {
            SchemaCollection col = si.getXmlSchemaCollection();
            if (col.getXmlSchemas().length > 1) {
                // someone has already filled in the types
                continue;
            }
        }

        Map<String, String> namespaceMap = getDeclaredNamespaceMappings();
        
        for (Map.Entry<String, Set<Type>> entry : tns2Type.entrySet()) {
            String xsdPrefix = SOAPConstants.XSD_PREFIX;
            if (namespaceMap != null && namespaceMap.containsKey(SOAPConstants.XSD)) {
                xsdPrefix = namespaceMap.get(SOAPConstants.XSD);
            }
            
            Element e = new Element("schema", xsdPrefix, SOAPConstants.XSD);

            e.setAttribute(new Attribute(WSDLConstants.ATTR_TNS, entry.getKey()));
            
            if (null != namespaceMap) { // did application hand us some additional namespaces?
                for (Map.Entry<String, String> mapping : namespaceMap.entrySet()) {
                    // user gives us namespace->prefix mapping. 
                    e.addNamespaceDeclaration(Namespace.getNamespace(mapping.getValue(),
                                                                     mapping.getKey())); 
                }
            }

            // if the user didn't pick something else, assign 'tns' as the prefix.
            if (namespaceMap == null || !namespaceMap.containsKey(entry.getKey())) {
                // Schemas are more readable if there is a specific prefix for the TNS.
                e.addNamespaceDeclaration(Namespace.getNamespace(WSDLConstants.CONVENTIONAL_TNS_PREFIX, 
                                                                 entry.getKey()));
            }
            e.setAttribute(new Attribute("elementFormDefault", "qualified"));
            e.setAttribute(new Attribute("attributeFormDefault", "qualified"));

            for (Type t : entry.getValue()) {
                t.writeSchema(e);
            }

            if (e.getChildren().size() == 0) {
                continue;
            }

            try {
                NamespaceMap nsMap = new NamespaceMap();
                
                nsMap.add(xsdPrefix, SOAPConstants.XSD);
                
                // We prefer explicit prefixes over those generated in the types.
                // This loop may have intended to support prefixes from individual aegis files,
                // but that isn't a good idea. 
                for (Iterator itr = e.getAdditionalNamespaces().iterator(); itr.hasNext();) {
                    Namespace n = (Namespace) itr.next();
                    if (!nsMap.containsValue(n.getURI())) {
                        nsMap.add(n.getPrefix(), n.getURI());
                    }
                }

                org.w3c.dom.Document schema = new DOMOutputter().output(new Document(e));

                for (ServiceInfo si : s.getServiceInfos()) {
                    SchemaCollection col = si.getXmlSchemaCollection();
                    col.setNamespaceContext(nsMap);
                    XmlSchema xmlSchema = addSchemaDocument(si, col, schema, entry.getKey());
                    // Work around bug in JDOM DOMOutputter which fails to correctly
                    // assign namespaces to attributes. If JDOM worked right, 
                    // the collection object would get the prefixes for itself.
                    xmlSchema.setNamespaceContext(nsMap);
                }
            } catch (JDOMException e1) {
                throw new ServiceConstructionException(e1);
            }
        }

    }

    public QName getSuggestedName(Service s, TypeMapping tm, OperationInfo op, int param) {
        Method m = getMethod(s, op);
        if (m == null) {
            return null;
        }

        QName name = tm.getTypeCreator().getElementName(m, param);

        // No mapped name was specified, so if its a complex type use that name
        // instead
        if (name == null) {
            Type type = tm.getTypeCreator().createType(m, param);

            if (type.isComplex() && !type.isAbstract()) {
                name = type.getSchemaType();
            }
        }

        return name;
    }
    
    private Type getParameterType(Service s, TypeMapping tm, MessagePartInfo param, int paramtype) {
        Type type = tm.getType(param.getTypeQName());

        /*
         * if (type == null && tm.isRegistered(param.getTypeClass())) { type =
         * tm.getType(param.getTypeClass()); part2type.put(param, type); }
         */

        int offset = 0;
        if (paramtype == OUT_PARAM) {
            offset = 1;
        }
        
        TypeCreator typeCreator = tm.getTypeCreator();
        if (type == null) {
            OperationInfo op = param.getMessageInfo().getOperation();

            Method m = getMethod(s, op);
            TypeClassInfo info;
            if (paramtype != FAULT_PARAM && m != null) {
                info = typeCreator.createClassInfo(m, param.getIndex() - offset);
            } else {
                info = typeCreator.createBasicClassInfo(param.getTypeClass());
            }
            if (param.getMessageInfo().getOperation().isUnwrapped()
                && param.getTypeClass().isArray()) {
                //The service factory expects arrays going into the wrapper to be
                //mapped to the array component type and will then add
                //min=0/max=unbounded.   That doesn't work for Aegis where we
                //already created a wrapper ArrayType so we'll let it know we want the default.
                param.setProperty("minOccurs", "1");
                param.setProperty("maxOccurs", "1");
                param.setProperty("nillable", Boolean.TRUE);
            }
            if (info.getMappedName() != null) {
                param.setConcreteName(info.getMappedName());
                param.setName(info.getMappedName());
            }
            type = typeCreator.createTypeForClass(info);
            // We have to register the type if we want minOccurs and such to work.
            if (info.nonDefaultAttributes()) {
                tm.register(type);
            }
            type.setTypeMapping(tm);

            part2Type.put(param, type);
        }

        return type;
    }

    private Method getMethod(Service s, OperationInfo op) {
        MethodDispatcher md = (MethodDispatcher)s.get(MethodDispatcher.class.getName());
        SimpleMethodDispatcher smd = (SimpleMethodDispatcher)md;
        return smd.getPrimaryMethod(op);
    }

    public Type getType(MessagePartInfo part) {
        return part2Type.get(part);
    }

    public Set<String> getOverrideTypes() {
        return overrideTypes;
    }
    
    public void setOverrideTypes(Set<String> typeNames) {
        overrideTypes = typeNames;
    }

    public Service getService() {
        return service;
    }

    /** 
     * Return the type mapping configuration associated with this databinding object.
     * The configuration is retrieved from the type mapping registry.
     * @return Returns the configuration.
     */
    public Configuration getConfiguration() {
        return typeMappingRegistry.getConfiguration();
    }

    /**
     * Set the configuration for this databinding object.
     * @param configuration The configuration to set.
     */
    public void setConfiguration(Configuration configuration) {
        typeMappingRegistry.setConfiguration(configuration);
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

    public TypeMapping getServiceTypeMapping() {
        return serviceTypeMapping;
    }
}
