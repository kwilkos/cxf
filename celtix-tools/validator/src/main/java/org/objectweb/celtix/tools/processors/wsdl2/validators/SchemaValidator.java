package org.objectweb.celtix.tools.processors.wsdl2.validators;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.wsdl.WSDLException;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLReader;
import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.ls.LSInput;
import org.w3c.dom.ls.LSResourceResolver;

import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import org.apache.ws.commons.schema.XmlSchema;
import org.apache.ws.commons.schema.XmlSchemaCollection;
import org.objectweb.celtix.tools.common.ToolException;
import org.objectweb.celtix.tools.common.WSDLConstants;
import org.objectweb.celtix.tools.utils.WSDLExtensionRegister;

public class SchemaValidator extends AbstractValidator {

    protected String[] defaultSchemas;
    protected String schemaLocation = "./";

    private String wsdlsrc;
    private String[] xsds;
    private boolean isdeep;

    private DocumentBuilder docBuilder;
    private SAXParser saxParser;
    private Document schemaValidatedDoc;

    private Map<String, XmlSchema> xmlSchemaMap = new HashMap<String, XmlSchema>();
    private Map<QName, List> msgPartsMap = new HashMap<QName, List>();
    private Map<QName, Map> portTypes = new HashMap<QName, Map>();
    private Map<QName, QName> bindingMap = new HashMap<QName, QName>();

    public SchemaValidator(String schemaDir) throws ToolException {
        super(schemaDir);
        schemaLocation = schemaDir;
        defaultSchemas = getDefaultSchemas();
    }

    public SchemaValidator(String schemaDir, String wsdl, String[] schemas, boolean deep)
        throws ToolException {
        super(schemaDir);
        schemaLocation = schemaDir;
        defaultSchemas = getDefaultSchemas();
        wsdlsrc = wsdl;
        xsds = schemas;
        isdeep = deep;
    }

    public boolean isValid() {
        return validate(wsdlsrc, xsds, isdeep);
    }

    public boolean validate(String wsdlsource, String[] schemas, boolean deep) throws ToolException {

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

        return validate(is, schemas, deep);

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

    public boolean validate(InputSource wsdlsource, String[] schemas, boolean deep) throws ToolException {
        boolean isValid = false;
        try {
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

            Document document = docBuilder.parse(wsdlsource.getSystemId());

            doSchemaValidation(document, errHandler);

            if (!errHandler.isValid()) {
                throw new ToolException(errHandler.getErrorMessages());
            }

            this.schemaValidatedDoc = document;

            WSDLFactory wsdlFactory;
            XMLEventReader xmlEventReader = null;
            try {
                wsdlFactory = WSDLFactory.newInstance();
                WSDLReader reader = wsdlFactory.newWSDLReader();
                reader.setFeature("javax.wsdl.verbose", false);
                WSDLExtensionRegister register = new WSDLExtensionRegister(wsdlFactory, reader);
                register.registerExtenstions();
                def = reader.readWSDL(wsdlsource.getSystemId());

                XMLInputFactory factory = XMLInputFactory.newInstance();
                factory.setProperty(XMLInputFactory.IS_NAMESPACE_AWARE, true);

                File file = new File(new URI(wsdlsource.getSystemId()));
                xmlEventReader = factory.createXMLEventReader(new FileReader(file));

            } catch (WSDLException e) {
                throw new ToolException(e);
            } catch (XMLStreamException streamEx) {
                throw new ToolException(streamEx);
            } catch (URISyntaxException e) {
                throw new ToolException(e);
            }
            WSDLElementReferenceValidator wsdlRefValiadtor = 
                new WSDLElementReferenceValidator(def, this, xmlEventReader);

            isValid = wsdlRefValiadtor.isValid();

            if (!isValid) {
                throw new ToolException(this.getErrorMessage());
            }

            isValid = true;

        } catch (IOException ioe) {
            throw new ToolException("Can not get the wsdl " + wsdlsource.getSystemId(), ioe);
        } catch (SAXException saxEx) {
            throw new ToolException(saxEx);
        } catch (ParserConfigurationException e) {
            throw new ToolException(e);
        }
        return isValid;
    }

    private void doSchemaValidation(Document doc, NewStackTraceErrorHandler handler) throws IOException,
        SAXException {
        XmlSchemaCollection schemaCol = new XmlSchemaCollection();
        NodeList nodes = doc.getElementsByTagNameNS(WSDLConstants.NS_XMLNS, "schema");
        for (int x = 0; x < nodes.getLength(); x++) {
            Node schemaNode = nodes.item(x);
            Element schemaEl = (Element)schemaNode;
            String tns = schemaEl.getAttribute("targetNamespace");
            XmlSchema schema = new XmlSchema(schemaCol);
            schema = schemaCol.read(schemaEl, tns);
            xmlSchemaMap.put(tns, schema);

        }
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

    public Document getSchemaValidatedDoc() {
        return schemaValidatedDoc;
    }

    public Map<String, XmlSchema> getXMLSchemaMap() {
        return xmlSchemaMap;
    }

    public Map<QName, List> getMsgPartsMap() {
        return msgPartsMap;
    }

    public Map<QName, Map> getPortTypesMap() {
        return portTypes;
    }

    public Map<QName, QName> getBindingMap() {
        return bindingMap;
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
            systemId =  schemaLocation + systemId; 
        }
        
        LSInput lsin = new LSInputImpl();
        URI uri = null;
        try {
            uri = new URI(systemId);
        } catch (URISyntaxException e1) {
            return null;
        }

        File file = new File(uri);
        FileInputStream inputStrem = null;

        try {
            inputStrem = new FileInputStream(file);
        } catch (FileNotFoundException e) {
            return null;
        }
        
        lsin.setSystemId(systemId);
        lsin.setByteStream(inputStrem);
        return lsin;
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
