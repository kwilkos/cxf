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

import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.namespace.QName;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import org.apache.cxf.common.i18n.BundleUtils;
import org.apache.cxf.common.i18n.Message;
import org.apache.cxf.common.i18n.UncheckedException;
import org.apache.cxf.databinding.DataBinding;
import org.apache.cxf.databinding.DataBindingContext;
import org.apache.cxf.databinding.DataReaderFactory;
import org.apache.cxf.databinding.DataWriterFactory;
import org.apache.cxf.helpers.CastUtils;
import org.apache.cxf.helpers.DOMUtils;
import org.apache.cxf.resource.URIResolver;
import org.apache.cxf.service.Service;
import org.apache.cxf.service.model.BindingInfo;
import org.apache.cxf.service.model.BindingOperationInfo;
import org.apache.cxf.service.model.MessageInfo;
import org.apache.cxf.service.model.MessagePartInfo;
import org.apache.cxf.service.model.SchemaInfo;
import org.apache.cxf.service.model.ServiceInfo;
import org.apache.ws.commons.schema.XmlSchema;
import org.apache.ws.commons.schema.XmlSchemaCollection;

public final class JAXBDataBinding implements DataBinding {

    public static final String SCHEMA_RESOURCE = "SCHEMRESOURCE";

    private static final Logger LOG = Logger.getLogger(JAXBDataBinding.class.getName());

    private static final ResourceBundle BUNDLE = BundleUtils.getBundle(JAXBDataBinding.class);

    JAXBDataReaderFactory reader;

    JAXBDataWriterFactory writer;

    JAXBContext context;

    Service service;

    Class clazz;

    // this will be used to call unmarshall to provide the QName and Class
    JAXBDataBindingContext jaxbDataBindingContext;

    public JAXBDataBinding() throws JAXBException {
        reader = new JAXBDataReaderFactory();
        writer = new JAXBDataWriterFactory();
    }

    public JAXBDataBinding(Class<?> cls, Service pService) throws JAXBException {
        this();
        clazz = cls;
        service = pService;
        context = JAXBEncoderDecoder.createJAXBContextForClass(cls);
        reader.setJAXBContext(context);
        writer.setJAXBContext(context);

        Map<BindingInfo, Map<BindingOperationInfo, Map<QName, Class>>> typeClassMap;
        typeClassMap = new HashMap<BindingInfo, Map<BindingOperationInfo, Map<QName, Class>>>();        
        buildTypeClassMapping(typeClassMap);
        jaxbDataBindingContext = new JAXBDataBindingContext(typeClassMap);
    }

    public DataBindingContext getDataBindingContext() {
        return jaxbDataBindingContext;
    }

    public void setContext(JAXBContext ctx) {
        context = ctx;
        reader.setJAXBContext(context);
        writer.setJAXBContext(context);
    }

    public DataReaderFactory getDataReaderFactory() {
        return reader;
    }

    public DataWriterFactory getDataWriterFactory() {
        return writer;
    }

    public Map<String, SchemaInfo> getSchemas(ServiceInfo serviceInfo) {
        Collection<String> schemaResources = CastUtils.cast(serviceInfo.getProperty(SCHEMA_RESOURCE,
                        List.class), String.class);

        return loadSchemas(schemaResources);
    }

    private Map<String, SchemaInfo> loadSchemas(Collection<String> schemaResources) {
        Map<String, SchemaInfo> schemas = new HashMap<String, SchemaInfo>();
        for (String schema : schemaResources) {
            URIResolver resolver = null;
            try {
                resolver = new URIResolver(schema);
            } catch (IOException e) {
                LOG.log(Level.SEVERE, e.getMessage());
            }
            if (!resolver.isResolved()) {
                throw new UncheckedException(new Message("SCHEMA_NOT_RESOLVED", BUNDLE, schema));
            }
            if (resolver.isFile()) {
                // load schemas from file system
                loadSchemaFromFile(schema, schemas);
            } else {
                // load schemas from classpath
                loadSchemaFromClassPath(schema, schemas);
            }
        }
        return schemas;
    }

    private void loadSchemaFromClassPath(String schema, Map<String, SchemaInfo> schemas) {
        // we can reuse code in javatowsdl tool after tool refactor
    }

    private void loadSchemaFromFile(String schema, Map<String, SchemaInfo> schemas) {
        try {
            XmlSchemaCollection schemaCol = new XmlSchemaCollection();
            URIResolver resolver = new URIResolver(schema);
            schemaCol.setBaseUri(resolver.getFile().getParent());

            Document schemaDoc = DOMUtils.readXml(resolver.getInputStream());

            XmlSchema xmlSchema = schemaCol.read(schemaDoc.getDocumentElement());
            SchemaInfo schemaInfo = new SchemaInfo(null, xmlSchema.getTargetNamespace());

            schemaInfo.setElement(schemaDoc.getDocumentElement());
            schemas.put(schemaInfo.getNamespaceURI(), schemaInfo);
        } catch (IOException e) {
            LOG.log(Level.SEVERE, e.getMessage());
            throw new UncheckedException(e);
        } catch (SAXException e) {
            LOG.log(Level.SEVERE, e.getMessage());
            throw new UncheckedException(e);
        } catch (ParserConfigurationException e) {
            LOG.log(Level.SEVERE, e.getMessage());
            throw new UncheckedException(e);
        }
    }

    private void buildTypeClassMapping(
                    Map<BindingInfo, Map<BindingOperationInfo, Map<QName, Class>>> typeClassMap) {
        for (BindingInfo bi : service.getServiceInfo().getBindings()) {
            Map<BindingOperationInfo, Map<QName, Class>> biMap = 
                new HashMap<BindingOperationInfo, Map<QName, Class>>();
            typeClassMap.put(bi, biMap);
            for (BindingOperationInfo boi : bi.getOperations()) {
                Map<QName, Class> boiMap = new HashMap<QName, Class>();
                biMap.put(boi, boiMap);
                addTypeClassMapping(clazz, boiMap, boi.getOperationInfo().getInput());
                addTypeClassMapping(clazz, boiMap, boi.getOperationInfo().getOutput());
            }
        }
    }

    private static void addTypeClassMapping(Class cls, Map<QName, Class> boiMap, MessageInfo msg) {
        if (msg == null) {
            return;
        }
        for (MessagePartInfo mpi : msg.getMessageParts()) {
            if (!mpi.isElement()) {
                String methodName = msg.getOperation().getName().getLocalPart();
                String paramName = mpi.getTypeQName().getLocalPart();
                Class paramCls = getParamClass(cls, methodName, paramName);
                if (paramCls != null) {
                    boiMap.put(mpi.getTypeQName(), paramCls);
                }
            }
        }
    }

    private static Class getParamClass(Class cls, String methodName, String paramName) {
        Method methods[] = cls.getMethods();
        for (Method meth : methods) {
            if (meth.getName().equals(methodName)) {
                for (Type t : meth.getGenericParameterTypes()) {
                    if (t.getClass().getSimpleName().equals(paramName)) {
                        return t.getClass();
                    }
                }
            }
        }
        return null;
    }

}
