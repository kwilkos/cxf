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

package org.apache.cxf.tools.validator.internal;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.wsdl.WSDLException;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLReader;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.ls.LSInput;
import org.w3c.dom.ls.LSResourceResolver;

import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import org.apache.cxf.common.i18n.Message;
import org.apache.cxf.common.logging.LogUtils;
import org.apache.cxf.helpers.DOMUtils;
import org.apache.cxf.resource.URIResolver;
import org.apache.cxf.tools.common.ToolException;
import org.apache.cxf.tools.util.WSDLExtensionRegister;

public class SchemaValidator extends AbstractValidator {
    protected static final Logger LOG = LogUtils.getL7dLogger(SchemaValidator.class);

    protected String[] defaultSchemas;

    protected String schemaLocation = "./";

    private String wsdlsrc;

    private String[] xsds;

    private DocumentBuilder docBuilder;

    private SAXParser saxParser;

    public SchemaValidator(String schemaDir) throws ToolException {
        super(schemaDir);
        schemaLocation = schemaDir;
        defaultSchemas = getDefaultSchemas();
    }

    public SchemaValidator(String schemaDir, String wsdl, String[] schemas) throws ToolException {
        super(schemaDir);
        schemaLocation = schemaDir;
        defaultSchemas = getDefaultSchemas();
        wsdlsrc = wsdl;
        xsds = schemas;
    }

    public boolean isValid() {
        return validate(wsdlsrc, xsds);
    }

    public boolean validate(String wsdlsource, String[] schemas) throws ToolException {

        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        try {
            docFactory.setNamespaceAware(true);
            docBuilder = docFactory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            throw new ToolException(e);
        }

        String systemId = null;
        try {
            systemId = getWsdlUrl(wsdlsource);
        } catch (IOException ioe) {
            throw new ToolException(ioe);
        }
        InputSource is = new InputSource(systemId);

        return validate(is, schemas);

    }

    private Schema createSchema(String[] schemas) throws SAXException, IOException {

        SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);

        SchemaResourceResolver resourceResolver = new SchemaResourceResolver();

        sf.setResourceResolver(resourceResolver);

        Source[] sources = new Source[schemas.length];

        for (int i = 0; i < schemas.length; i++) {
            // need to validate the schema file
            Document doc = docBuilder.parse(schemas[i]);

            DOMSource stream = new DOMSource(doc, schemas[i]);

            sources[i] = stream;
        }
        return sf.newSchema(sources);

    }

    public boolean validate(InputSource wsdlsource, String[] schemas) throws ToolException {
        boolean isValid = false;
        try {

            Document document = docBuilder.parse(wsdlsource.getSystemId());

            Node node = DOMUtils.getChild(document, null);
            if (node != null && !"definitions".equals(node.getLocalName())) {
                Message msg = new Message("NOT_A_WSDLFILE", LOG, wsdlsource.getSystemId());
                throw new ToolException(msg);
            }

            SAXParserFactory saxFactory = SAXParserFactory.newInstance();
            saxFactory.setFeature("http://xml.org/sax/features/namespaces", true);
            saxParser = saxFactory.newSAXParser();

            schemas = addSchemas(defaultSchemas, schemas);

            SAXSource saxSource = new SAXSource(saxParser.getXMLReader(), wsdlsource);

            Schema schema = createSchema(schemas);

            Validator validator = schema.newValidator();

            NewStackTraceErrorHandler errHandler = new NewStackTraceErrorHandler();
            validator.setErrorHandler(errHandler);
            validator.validate(saxSource);

            if (!errHandler.isValid()) {
                throw new ToolException(errHandler.getErrorMessages());
            }

            try {
                WSDLFactory wsdlFactory = WSDLFactory.newInstance();
                WSDLReader reader = wsdlFactory.newWSDLReader();
                reader.setFeature("javax.wsdl.verbose", false);
                WSDLExtensionRegister register = new WSDLExtensionRegister(wsdlFactory, reader);
                register.registerExtensions();
                def = reader.readWSDL(wsdlsource.getSystemId());
            } catch (WSDLException e) {
                throw new ToolException(e);
            }

            WSDLElementReferenceValidator wsdlRefValidator = new 
                WSDLElementReferenceValidator(def, this,
                wsdlsource.getSystemId(), document);

            isValid = wsdlRefValidator.isValid();

            if (!isValid) {
                throw new ToolException(this.getErrorMessage());
            }

            isValid = true;

        } catch (IOException ioe) {
            throw new ToolException("Cannot get the wsdl " + wsdlsource.getSystemId(), ioe);
        } catch (SAXException saxEx) {
            throw new ToolException(saxEx);
        } catch (ParserConfigurationException e) {
            throw new ToolException(e);
        }
        return isValid;
    }

    private String[] addSchemas(String[] defaults, String[] schemas) {
        if (schemas == null || schemas.length == 0) {
            return defaultSchemas;
        }
        String[] ss = new String[schemas.length + defaults.length];
        System.arraycopy(defaults, 0, ss, 0, defaults.length);
        System.arraycopy(schemas, 0, ss, defaults.length, schemas.length);
        return ss;
    }

    private String[] getDefaultSchemas() throws ToolException {

        String loc = schemaLocation;

        if (loc == null || "".equals(loc.trim())) {
            loc = "./";
        }
        File f = new File(loc);

        if (f.exists() && f.isDirectory()) {
            FilenameFilter filter = new FilenameFilter() {
                public boolean accept(File dir, String name) {
                    if (name.toLowerCase().endsWith(".xsd")
                            && !new File(dir.getPath() + File.separator + name).isDirectory()) {
                        return true;
                    }
                    return false;
                }
            };

            File[] files = f.listFiles(filter);

            List<String> xsdUrls = new ArrayList<String>(files.length);
            for (File file : files) {
                try {
                    String s = file.toURL().toString();
                    xsdUrls.add(s);
                    if (s.indexOf("http-conf") > 0) {
                        xsdUrls.add(0, s);
                    }
                } catch (MalformedURLException e) {
                    throw new ToolException(e);
                }
            }
            return xsdUrls.toArray(new String[xsdUrls.size()]);
        }
        return null;
    }

    private String getWsdlUrl(String path) throws IOException {
        File file = new File(path);
        if (file != null && file.exists()) {
            return file.toURL().toString();
        }

        return null;
    }

}

class NewStackTraceErrorHandler implements ErrorHandler {
    protected boolean valid;

    private StringBuffer buffer;

    private int numErrors;

    private List<SAXParseException> errors;

    NewStackTraceErrorHandler() {
        valid = true;
        numErrors = 0;
        buffer = new StringBuffer();
        errors = new ArrayList<SAXParseException>();
    }

    public void error(SAXParseException ex) throws SAXParseException {
        addError(ex);
    }

    public void fatalError(SAXParseException ex) {
        addError(ex);
    }

    public void warning(SAXParseException ex) {
        // Warning messages are ignored.
        // return;
    }

    boolean isValid() {
        return valid;
    }

    int getTotalErrors() {
        return numErrors;
    }

    String getErrorMessages() {
        return buffer.toString();
    }

    SAXParseException[] getErrors() {
        if (errors == null) {
            return null;
        }
        return errors.toArray(new SAXParseException[errors.size()]);
    }

    void addError(String msg, SAXParseException ex) {
        valid = false;
        if (numErrors == 0) {
            buffer.append("\n");
        } else {
            buffer.append("\n\n");
        }
        buffer.append(msg);
        numErrors++;
        errors.add(ex);

    }

    private String getErrorMessage(SAXParseException ex) {
        return "line " + ex.getLineNumber() + " column " + ex.getColumnNumber() + " of " + ex.getSystemId()
                + ": " + ex.getMessage();
    }

    private void addError(SAXParseException ex) {
        addError(getErrorMessage(ex), ex);
    }

}

class SchemaResourceResolver implements LSResourceResolver {
    public LSInput resolveResource(String type, String namespaceURI, String publicId, String systemId,
            String baseURI) {
        String schemaLocation = baseURI.substring(0, baseURI.lastIndexOf("/") + 1);

        if (systemId.indexOf("http://") < 0) {
            systemId = schemaLocation + systemId;
        }

        URIResolver resolver = null;
        try {
            resolver = new URIResolver(systemId);
        } catch (IOException e1) {
            return null;
        }

        if (resolver.getInputStream() != null) {
            LSInput lsin = new LSInputImpl();
            lsin.setSystemId(systemId);
            lsin.setByteStream(resolver.getInputStream());
            return lsin;
        } else {
            return null;
        }
    }
}

class LSInputImpl implements LSInput {

    protected String fPublicId;

    protected String fSystemId;

    protected String fBaseSystemId;

    protected InputStream fByteStream;

    protected Reader fCharStream;

    protected String fData;

    protected String fEncoding;

    protected boolean fCertifiedText;

    public LSInputImpl() {
    }

    public LSInputImpl(String publicId, String systemId, InputStream byteStream) {
        fPublicId = publicId;
        fSystemId = systemId;
        fByteStream = byteStream;
    }

    public InputStream getByteStream() {
        return fByteStream;
    }

    public void setByteStream(InputStream byteStream) {
        fByteStream = byteStream;
    }

    public Reader getCharacterStream() {
        return fCharStream;
    }

    public void setCharacterStream(Reader characterStream) {
        fCharStream = characterStream;
    }

    public String getStringData() {
        return fData;
    }

    public void setStringData(String stringData) {
        fData = stringData;
    }

    public String getEncoding() {
        return fEncoding;
    }

    public void setEncoding(String encoding) {
        fEncoding = encoding;
    }

    public String getPublicId() {
        return fPublicId;
    }

    public void setPublicId(String publicId) {
        fPublicId = publicId;
    }

    public String getSystemId() {
        return fSystemId;
    }

    public void setSystemId(String systemId) {
        fSystemId = systemId;
    }

    public String getBaseURI() {
        return fBaseSystemId;
    }

    public void setBaseURI(String baseURI) {
        fBaseSystemId = baseURI;
    }

    public boolean getCertifiedText() {
        return fCertifiedText;
    }

    public void setCertifiedText(boolean certifiedText) {
        fCertifiedText = certifiedText;
    }

}
