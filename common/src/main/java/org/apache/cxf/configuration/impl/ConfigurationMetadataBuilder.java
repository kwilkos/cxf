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

package org.apache.cxf.configuration.impl;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import org.apache.cxf.common.i18n.Message;
import org.apache.cxf.common.logging.LogUtils;
import org.apache.cxf.configuration.ConfigurationException;
import org.apache.cxf.configuration.ConfigurationItemMetadata.LifecyclePolicy;
import org.apache.cxf.configuration.ConfigurationMetadata;
import org.apache.cxf.resource.DefaultResourceManager;


public class ConfigurationMetadataBuilder  {

    final class ValidatorErrorHandler implements ErrorHandler {

        public void error(SAXParseException exception) throws SAXException {
            throw exception;
        }

        public void fatalError(SAXParseException exception) throws SAXException {
            throw exception;
        }

        public void warning(SAXParseException exception) throws SAXException {
            throw exception;
        }
    }
    private static final Logger LOG = LogUtils.getL7dLogger(ConfigurationMetadataBuilder.class);
    private static final String MEATADATA_NAMESPACE_URI =
        "http://cxf.apache.org/configuration/metadata";
    private static Schema metadataSchema;
    private static Validator metadataValidator;

    private static ErrorHandler validatorErrorHandler;

    private final ConfigurationMetadataImpl model;
    private final boolean forceDefaults;
    private boolean doValidate;

    public ConfigurationMetadataBuilder(boolean fd) {
        model = new ConfigurationMetadataImpl();
        forceDefaults = fd;
    }

    public void setValidation(boolean onOff) {
        doValidate = onOff;
    }

    public ConfigurationMetadata build(InputSource is) throws IOException {
        parseXML(is);
        return model;
    }

    public ConfigurationMetadata build(InputStream is) throws IOException {
        return build(new InputSource(is));
    }

    private void deserializeConfig(Document document) {
        Element root = document.getDocumentElement();
        model.setNamespaceURI(root.getAttribute("namespace"));
        model.setParentNamespaceURI(root.getAttribute("parentNamespace"));
    }

    private void deserializeConfigItem(Document document, Element configItemElement) {

        ConfigurationItemMetadataImpl item = new ConfigurationItemMetadataImpl();

        for (Node nd = configItemElement.getFirstChild(); nd != null; nd = nd.getNextSibling()) {
            if (Node.ELEMENT_NODE != nd.getNodeType()) {
                continue;
            } else if ("name".equals(nd.getLocalName())) {
                item.setName(ConfigurationMetadataUtils.getElementValue(nd));
            } else if ("type".equals(nd.getLocalName())) {
                QName type = ConfigurationMetadataUtils.elementValueToQName(document,
                                                                           (Element)nd);
                item.setType(type);
                if (doValidate) {
                    if (XMLConstants.W3C_XML_SCHEMA_NS_URI.equals(type.getNamespaceURI())) {
                        continue;
                    }
                    TypeSchema ts = new TypeSchemaHelper(forceDefaults).get(type.getNamespaceURI());
                    if (ts == null) {
                        throw new ConfigurationException(new Message("NO_TYPESCHEMA_FOR_NAMESPACE_EXC", LOG,
                                                                     type.getNamespaceURI()));
                    }
                    if (!ts.hasType(type.getLocalPart())) {
                        throw new ConfigurationException(new Message("TYPE_NOT_DEFINED_IN_NAMESPACE_EXC",
                                                                     LOG, type.getLocalPart(), type
                                                                         .getNamespaceURI()));
                    }
                }
            } else if ("description".equals(nd.getLocalName())) {
                // item.setDescription(getElementValue(nd));
            } else if ("lifecyclePolicy".equals(nd.getLocalName())) {
                String value = ConfigurationMetadataUtils.getElementValue(nd);
                if (null != value) {
                    if ("static".equals(value)) {
                        item.setLifecyclePolicy(LifecyclePolicy.STATIC);
                    } else if ("process".equals(value)) {
                        item.setLifecyclePolicy(LifecyclePolicy.PROCESS);
                    } else if ("bus".equals(value)) {
                        item.setLifecyclePolicy(LifecyclePolicy.BUS);
                    } else {
                        item.setLifecyclePolicy(LifecyclePolicy.DYNAMIC);
                    }
                }
            } else {
                // this must be the extension element holding the default value
                deserializeDefaultValue(item, (Element)nd);
            }
        }

        model.addItem(item);
    }

    private void deserializeConfigItems(Document document) {
        for (Node nd = document.getDocumentElement().getFirstChild(); nd != null; nd = nd.getNextSibling()) {
            if (Node.ELEMENT_NODE == nd.getNodeType()
                && "configItem".equals(nd.getLocalName())
                && MEATADATA_NAMESPACE_URI.equals(nd.getNamespaceURI())) {
                Element configItemElement = (Element)nd;
                deserializeConfigItem(document, configItemElement);
            }
        }
    }

    private void deserializeDefaultValue(ConfigurationItemMetadataImpl item, Element data) {
        /*
        String namespaceURI = data.getNamespaceURI();
        System.out.println("deserializeDefaultValue: \n"
                           + "    data namespaceURI: " + namespaceURI + "\n"
                           + "    data localName: " + data.getLocalName() + "\n"
                           + "    item type: " + item.getType());

        if (!namespaceURI.equals(item.getType().getNamespaceURI())) {
            Message msg = new Message("INVALID_ELEMENT_FOR_DEFAULT_VALUE_EXC",
                                      LOG, item.getName(), item.getType());
            throw new ConfigurationException(msg);
        }
        TypeSchema ts = new TypeSchemaHelper().get(namespaceURI);
        assert ts != null;
        String name = data.getLocalName();
        QName type = ts.getDeclaredType(name);
        if (null == type || !type.equals(item.getType().getLocalPart())) {
            Message msg = new Message("INVALID_ELEMENT_FOR_DEFAULT_VALUE_EXC",
                                      LOG, item.getName(), item.getType());
            throw new ConfigurationException(msg);
        }
        unmarshalDefaultValue(item, data);
        */
        String elementName = data.getLocalName();
        String namespaceURI = data.getNamespaceURI();
        TypeSchema ts = new TypeSchemaHelper(forceDefaults).get(namespaceURI);
        QName type = null;
        if (null != ts) {
            type = ts.getDeclaredType(elementName);
        }
        if (null == ts || null == type) {
            System.err.println(elementName);
            System.err.println(namespaceURI);
            System.err.println(ts);
            System.err.println(type);
            throw new ConfigurationException(new Message("INVALID_ELEMENT_FOR_DEFAULT_VALUE_EXC", LOG,
                                                         item.getName(), item.getType()));
        }
        if (!type.equals(item.getType())) {
            throw new ConfigurationException(new Message("INVALID_TYPE_FOR_DEFAULT_VALUE_EXC", LOG,
                                                       item.getName(), item.getType()));
        }
        unmarshalDefaultValue(item, data);
    }

    private void deserializeImports(Document document) {
        TypeSchemaHelper tsh = new TypeSchemaHelper(forceDefaults);
        for (Node nd = document.getDocumentElement().getFirstChild(); nd != null; nd = nd.getNextSibling()) {
            if (Node.ELEMENT_NODE == nd.getNodeType()
                && "configImport".equals(nd.getLocalName())
                && MEATADATA_NAMESPACE_URI.equals(nd.getNamespaceURI())) {
                Element importElement = (Element)nd;
                String location = importElement.getAttribute("location");
                String namespaceURI = importElement.getAttribute("namespace");
                if (null == tsh.get(namespaceURI)) {
                    tsh.get(namespaceURI, document.getDocumentURI(), location);
                }
            }
        }
    }

    /**
     * The configuration metadata schema is obtained system resource
     * "schemas/configuration/metadata.xsd".
     * It requires that either the resources directory is on the classpath or that
     * the resources is listed in the classpath specified in the manifest of cxf.jar.
     *
     * @return the metadata schema
     */

    private Schema getMetadataSchema() {
        if (null == metadataSchema) {
            InputStream is =
                DefaultResourceManager.instance()
                    .getResourceAsStream("schemas/configuration/metadata.xsd");

            if (null == is) {
                throw new ConfigurationException(new Message("CANNOT_FIND_CONFIG_METADATA_SCHEMA_MSG", LOG));
            }

            try {
                metadataSchema = getSchema(is);
            } catch (ConfigurationException ex) {
                // should never happen as metadata schema is immutable
                LOG.log(Level.SEVERE, "CANNOT_CREATE_CONFIG_METADATA_SCHEMA_MSG", ex);
            }
        }
        return metadataSchema;
    }

    private Validator getMetadataValidator() {
        if (null == metadataValidator) {
            Schema schema = getMetadataSchema();
            // assert null != schema;
            metadataValidator = schema.newValidator();
            if (null == validatorErrorHandler) {
                validatorErrorHandler = new ValidatorErrorHandler();
            }
            metadataValidator.setErrorHandler(validatorErrorHandler);
            // assert null != metadataValidator;
        }
        return metadataValidator;
    }

    private Schema getSchema(InputStream is) {
        Source schemaFile = new StreamSource(is);

        SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        Schema schema = null;
        try {
            schema = factory.newSchema(schemaFile);
        } catch (SAXException ex) {
            throw new ConfigurationException(new Message("SCHEMA_CREATION_ERROR_EXC", LOG), ex);
        }
        return schema;
    }

    private void parseXML(InputSource is) throws IOException {

        // parse
        Document document = null;
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            DocumentBuilder parser = factory.newDocumentBuilder();
            document = parser.parse(is);
        } catch (ParserConfigurationException ex) {
            throw new ConfigurationException(new Message("PARSER_CONFIGURATION_ERROR_EXC", LOG), ex);
        } catch (SAXException ex) {
            throw new ConfigurationException(new Message("PARSE_ERROR_EXC", LOG), ex);
        }

        if (doValidate) {
            try {
                Validator v = getMetadataValidator();
                v.validate(new DOMSource(document));
            } catch (SAXException ex) {
                Message msg = new Message("METADATA_VALIDATION_ERROR_EXC", LOG);
                throw new ConfigurationException(msg, ex);
            }
        }

        deserializeImports(document);
        deserializeConfig(document);
        deserializeConfigItems(document);
    }

    private void unmarshalDefaultValue(ConfigurationItemMetadataImpl item, Element data) {
        TypeSchema ts = new TypeSchemaHelper(forceDefaults).get(data.getNamespaceURI());
        Object obj = ts.unmarshalDefaultValue(item, data, doValidate);
        if (null != obj) {
            item.setDefaultValue(obj);
        }
    }
}
