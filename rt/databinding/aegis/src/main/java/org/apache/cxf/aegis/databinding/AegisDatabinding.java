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

import org.apache.cxf.aegis.DatabindingException;
import org.apache.cxf.aegis.type.DefaultTypeMappingRegistry;
import org.apache.cxf.aegis.type.Type;
import org.apache.cxf.aegis.type.TypeMapping;
import org.apache.cxf.aegis.type.TypeMappingRegistry;
import org.apache.cxf.aegis.type.TypeUtil;
import org.apache.cxf.aegis.type.basic.BeanType;
import org.apache.cxf.aegis.util.XmlConstants;
import org.apache.cxf.common.classloader.ClassLoaderUtils;
import org.apache.cxf.databinding.DataBinding;
import org.apache.cxf.databinding.DataReader;
import org.apache.cxf.databinding.DataWriter;
import org.apache.cxf.frontend.MethodDispatcher;
import org.apache.cxf.frontend.SimpleMethodDispatcher;
import org.apache.cxf.service.Service;
import org.apache.cxf.service.factory.ServiceConstructionException;
import org.apache.cxf.service.model.AbstractMessageContainer;
import org.apache.cxf.service.model.FaultInfo;
import org.apache.cxf.service.model.MessagePartInfo;
import org.apache.cxf.service.model.OperationInfo;
import org.apache.cxf.service.model.SchemaInfo;
import org.apache.cxf.service.model.ServiceInfo;
import org.apache.cxf.wsdl.WSDLConstants;
import org.apache.ws.commons.schema.XmlSchema;
import org.apache.ws.commons.schema.XmlSchemaCollection;
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
 * NOTE: There is an assumed 1:1 mapping between an AegisDatabinding and a Service!
 */
public class AegisDatabinding implements DataBinding {
    
    public static final String CURRENT_MESSAGE_PART = "currentMessagePart";
    public static final String TYPE_MAPPING_KEY = "type.mapping";
    public static final String ENCODING_URI_KEY = "type.encodingUri";
    public static final String WRITE_XSI_TYPE_KEY = "writeXsiType";
    public static final String OVERRIDE_TYPES_KEY = "overrideTypesList";
    public static final String READ_XSI_TYPE_KEY = "readXsiType";

    protected static final int IN_PARAM = 0;
    protected static final int OUT_PARAM = 1;
    protected static final int FAULT_PARAM = 2;

    private TypeMappingRegistry typeMappingRegistry;
    private Map<MessagePartInfo, Type> part2Type;
    private List overrideTypes;
    private Service service;

    public AegisDatabinding() {
        super();
        this.typeMappingRegistry = new DefaultTypeMappingRegistry(true);
        part2Type = new HashMap<MessagePartInfo, Type>();
    }

    @SuppressWarnings("unchecked")
    public <T> DataReader<T> createReader(Class<T> cls) {
        if (cls.equals(XMLStreamReader.class)) {
            return (DataReader<T>) new XMLStreamDataReader(this);
        } else {
            throw new UnsupportedOperationException();
        }
    }

    @SuppressWarnings("unchecked")
    public <T> DataWriter<T> createWriter(Class<T> cls) {
        if (cls.equals(XMLStreamWriter.class)) {
            return (DataWriter<T>)new XMLStreamDataWriter(this);
        } else {
            throw new UnsupportedOperationException();
        }
    }

    public Class<?>[] getSupportedReaderFormats() {
        return new Class[] {XMLStreamReader.class};
    }

    public Class<?>[] getSupportedWriterFormats() {
        return new Class[] {XMLStreamWriter.class};
    }

    public TypeMappingRegistry getTypeMappingRegistry() {
        return typeMappingRegistry;
    }

    public void setTypeMappingRegistry(TypeMappingRegistry typeMappingRegistry) {
        this.typeMappingRegistry = typeMappingRegistry;
    }

    public void initialize(Service s) {
        this.service = s;
        
        QName serviceName = s.getServiceInfos().get(0).getName();
        TypeMapping serviceTM = typeMappingRegistry.createTypeMapping(XmlConstants.XSD, true);
        typeMappingRegistry.register(serviceName.getNamespaceURI(), serviceTM);

        s.put(TypeMapping.class.getName(), serviceTM);

        Set<Type> deps = new HashSet<Type>();

        for (ServiceInfo info : s.getServiceInfos()) {
            for (OperationInfo opInfo : info.getInterface().getOperations()) {
                if (opInfo.isUnwrappedCapable()) {
                    initializeOperation(s, serviceTM, opInfo.getUnwrappedOperation(), deps);
                } else {
                    initializeOperation(s, serviceTM, opInfo, deps);
                }
            }
        }

        List<Type> additional = getAdditionalTypes(s, serviceTM);

        if (additional != null) {
            for (Type t : additional) {
                if (!deps.contains(t)) {
                    deps.add(t);
                }
            }
        }

        createSchemas(s, deps);
    }

    List<Type> getAdditionalTypes(Service s, TypeMapping tm) {
        List classes = (List)s.get(OVERRIDE_TYPES_KEY);

        this.overrideTypes = classes;

        if (classes != null) {
            List<Type> types = new ArrayList<Type>();
            for (Iterator it = classes.iterator(); it.hasNext();) {
                String typeName = (String)it.next();
                Class c;
                try {
                    c = ClassLoaderUtils.loadClass(typeName, TypeUtil.class);
                } catch (ClassNotFoundException e) {
                    throw new DatabindingException("Could not find override type class: " + typeName, e);
                }

                Type t = tm.getType(c);
                if (t == null) {
                    t = tm.getTypeCreator().createType(c);
                    tm.register(t);
                }
                if (t instanceof BeanType) {
                    BeanType bt = (BeanType)t;
                    bt.getTypeInfo().setExtension(true);
                    types.add(bt);
                }
            }
            return types;
        }
        return null;
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

    protected void initializeMessage(Service s, TypeMapping serviceTM,
                                     AbstractMessageContainer container, 
                                     int partType, Set<Type> deps) {
        for (Iterator itr = container.getMessageParts().iterator(); itr.hasNext();) {
            MessagePartInfo part = (MessagePartInfo)itr.next();

            Type type = getParameterType(s, serviceTM, part, partType);

            if (type.isAbstract()) {
                part.setTypeQName(type.getSchemaType());
            } else {
                part.setElementQName(type.getSchemaType());
            }

            part2Type.put(part, type);

            // QName elName = getSuggestedName(service, op, param)
            deps.add(type);

            addDependencies(deps, type);
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

        for (Map.Entry<String, Set<Type>> entry : tns2Type.entrySet()) {
            Element e = new Element("schema", "xsd", XmlConstants.XSD);

            e.setAttribute(new Attribute(WSDLConstants.ATTR_TNS, entry.getKey()));
            // Schemas are more readable if there is a specific prefix for the TNS.
            // note: if aegis ever allows users to ask for a specific prefix in the 
            // .aegis.xml file, this will need some adjustment in case of conflicts.
            e.addNamespaceDeclaration(Namespace.getNamespace(WSDLConstants.CONVENTIONAL_TNS_PREFIX, 
                                                             entry.getKey()));
            e.setAttribute(new Attribute("elementFormDefault", "qualified"));
            e.setAttribute(new Attribute("attributeFormDefault", "qualified"));

            for (Type t : entry.getValue()) {
                t.writeSchema(e);
            }

            if (e.getChildren().size() == 0) {
                continue;
            }

            try {
                XmlSchemaCollection col = new XmlSchemaCollection();
                NamespaceMap nsMap = new NamespaceMap();
                nsMap.add("xsd", "http://www.w3.org/2001/XMLSchema");
                
                for (Iterator itr = e.getAdditionalNamespaces().iterator(); itr.hasNext();) {
                    Namespace n = (Namespace) itr.next();
                    nsMap.add(n.getPrefix(), n.getURI());
                }
                
                col.setNamespaceContext(nsMap);

                org.w3c.dom.Document schema = new DOMOutputter().output(new Document(e));

                for (ServiceInfo si : s.getServiceInfos()) {
                    SchemaInfo info = new SchemaInfo(si, entry.getKey());

                    info.setElement(schema.getDocumentElement());

                    XmlSchema xmlSchema = col.read(schema.getDocumentElement());
                    // Work around bug in JDOM DOMOutputter which fails to correctly
                    // assign namespaces to attributes. If JDOM worked right, 
                    // the collection object would get the prefixes for itself.
                    xmlSchema.setNamespaceContext(nsMap);
                    info.setSchema(xmlSchema);

                    info.setSystemId(entry.getKey());

                    si.addSchema(info);
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
        if (type == null) {
            OperationInfo op = param.getMessageInfo().getOperation();

            Method m = getMethod(s, op);
            if (paramtype != FAULT_PARAM && m != null) {

                /*
                 * Note: we are not registering the type here, because it is an
                 * anonymous type. Potentially there could be many schema types
                 * with this name. For example, there could be many ns:in0
                 * paramters.
                 */
                type = tm.getTypeCreator().createType(m, param.getIndex() - offset);
            } else {
                type = tm.getTypeCreator().createType(param.getTypeClass());
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

    public List getOverrideTypes() {
        return overrideTypes;
    }

    public Service getService() {
        return service;
    }
    
}
