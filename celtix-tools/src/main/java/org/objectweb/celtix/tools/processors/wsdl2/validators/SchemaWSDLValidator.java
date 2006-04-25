package org.objectweb.celtix.tools.processors.wsdl2.validators;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.wsdl.WSDLException;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLReader;
import javax.xml.namespace.QName;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import com.sun.org.apache.xerces.internal.parsers.DOMParser;
import com.sun.org.apache.xerces.internal.parsers.XMLGrammarPreparser;
import com.sun.org.apache.xerces.internal.xni.XMLResourceIdentifier;
import com.sun.org.apache.xerces.internal.xni.XNIException;
import com.sun.org.apache.xerces.internal.xni.grammars.XMLGrammarDescription;
import com.sun.org.apache.xerces.internal.xni.grammars.XSGrammar;
import com.sun.org.apache.xerces.internal.xni.parser.XMLEntityResolver;
import com.sun.org.apache.xerces.internal.xni.parser.XMLErrorHandler;
import com.sun.org.apache.xerces.internal.xni.parser.XMLInputSource;
import com.sun.org.apache.xerces.internal.xni.parser.XMLParseException;
import com.sun.org.apache.xerces.internal.xs.XSModel;
import com.sun.org.apache.xml.internal.serialize.OutputFormat;
import com.sun.org.apache.xml.internal.serialize.XMLSerializer;

import org.objectweb.celtix.tools.common.ToolException;
import org.objectweb.celtix.tools.common.WSDLConstants;
import org.objectweb.celtix.tools.utils.LineNumDOMParser;
import org.objectweb.celtix.tools.utils.URLFactory;
import org.objectweb.celtix.tools.utils.WSDLExtensionRegister;

public class SchemaWSDLValidator extends AbstractValidator {

    protected String[] defaultSchemas;
    protected String schemaLocation = "./";

    private DOMParser parser;
    private XMLGrammarPreparser preparser;

    private List<XSModel> xsmodelList = new Vector<XSModel>();

    private String wsdlsrc;
    private String[] xsds;
    private boolean isdeep;

    private Document schemaValidatedDoc;
    private Map<QName, List> msgPartsMap = new HashMap<QName, List>();
    private Map<QName, Map> portTypes = new HashMap<QName, Map>();
    private Map<QName, QName> bindingMap = new HashMap<QName, QName>();

    public SchemaWSDLValidator(String schemaDir) throws ToolException {
        super(schemaDir);
        schemaLocation = schemaDir;
        defaultSchemas = getDefaultSchemas();
        init();
    }

    public SchemaWSDLValidator(String schemaDir, String wsdl, String[] schemas, boolean deep) {
        super(schemaDir);
        schemaLocation = schemaDir;
        defaultSchemas = getDefaultSchemas();
        init();
        wsdlsrc = wsdl;
        xsds = schemas;
        isdeep = deep;
    }

    public boolean isValid() {
        return validate(wsdlsrc, xsds, isdeep);
    }

    public boolean validate(String wsdlsource, String[] schemas, boolean deep) throws ToolException {
        String systemId = null;
        try {
            systemId = getWsdlUrl(wsdlsource);
        } catch (IOException ioe) {
            throw new ToolException(ioe);
        }

        return validate(new InputSource(systemId), schemas, deep);

    }

    private boolean validate(InputSource wsdlsource, String[] schemas, boolean deep) throws ToolException {
        boolean isValid = false;
        try {
            schemas = addSchemas(defaultSchemas, schemas);
            setExternalSchemaLocations(schemas);
            StackTraceErrorHandler handler = setErrorHandler();
            schemaValidatedDoc = doValidation(wsdlsource, deep);
            //
            if (!handler.isValid()) {
                ToolException ex = new ToolException(handler.getErrorMessages());
                throw ex;
            }

            WSDLFactory wsdlFactory;
            try {
                wsdlFactory = WSDLFactory.newInstance();
                WSDLReader reader = wsdlFactory.newWSDLReader();
                reader.setFeature("javax.wsdl.verbose", false);
                WSDLExtensionRegister register = new WSDLExtensionRegister(wsdlFactory , reader);
                register.registerExtenstions();
                def = reader.readWSDL(wsdlsource.getSystemId());
            } catch (WSDLException e) {
                throw new ToolException("Can not create wsdl definition for " + wsdlsource.getSystemId());
            }
            
            WSDLElementReferenceValidator wsdlRefValiadtor = new WSDLElementReferenceValidator(def, this);
            isValid = wsdlRefValiadtor.isValid();
            
            if (!isValid) {
                throw new ToolException(this.getErrorMessage());
            }
            
            isValid = true;

        } catch (IOException ioe) {
            throw new ToolException(ioe);
        } catch (SAXException saxEx) {
            throw new ToolException(saxEx);

        }
        return isValid;
    }

    public Document validate(InputStream wsdlsource, String[] schemas) throws Exception {
        schemas = addSchemas(defaultSchemas, schemas);
        setExternalSchemaLocations(schemas);
        StackTraceErrorHandler handler = setErrorHandler();
        Document doc = doValidation(new InputSource(wsdlsource), false);

        if (!handler.isValid()) {
            throw new SAXException(handler.getErrorMessages());
        }
        return doc;
    }

    public Document validate(String wsdlsource, String[] schemas) throws ToolException {
        schemas = addSchemas(defaultSchemas, schemas);
        Document doc = null;
        try {
            setExternalSchemaLocations(schemas);
            // StackTraceErrorHandler handler = setErrorHandler();
            setErrorHandler();
            doc = doValidation(new InputSource(getWsdlUrl(wsdlsource)), false);
            /*
             * if (!handler.isValid()) { ToolException ex = new
             * ToolException(handler.getErrorMessages()); throw ex; }
             */
        } catch (Exception e) {
            ToolException ex = new ToolException(e);
            throw ex;
        }
        return doc;
    }

    void setErrorHandler(StackTraceErrorHandler handler) {
        parser.setErrorHandler(handler);

        preparser.setErrorHandler(handler);
    }

    private void init() throws ToolException {
        try {

            parser = new LineNumDOMParser();
            preparser = new XMLGrammarPreparser();
            parser.setFeature("http://xml.org/sax/features/validation", true);
            parser.setFeature("http://apache.org/xml/features/validation/schema", true);
            parser.setFeature("http://apache.org/xml/features/validation/schema-full-checking", true);
            parser.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);

            preparser.registerPreparser(XMLGrammarDescription.XML_SCHEMA, null);
            preparser.setFeature("http://xml.org/sax/features/namespaces", true);
            preparser.setFeature("http://xml.org/sax/features/validation", true);
            preparser.setFeature("http://apache.org/xml/features/validation/schema", true);
            preparser.setFeature("http://apache.org/xml/features/validation/schema-full-checking", true);

            EntityResolver eresolver = new EntityResolver() {
                public InputSource resolveEntity(String publicID, String systemID) throws SAXException,
                    IOException {

                    String url = resolveUrl(systemID);
                    if (url == null) {
                        String msg = "Can't resolve entity from publicID=" + publicID + ", systemID="
                                     + systemID;

                        throw new IOException(msg);
                    }
                    return new InputSource(url);
                }
            };

            parser.setEntityResolver(eresolver);

            SchemaEntityResolver xmlResolver = new SchemaEntityResolver();
            preparser.setEntityResolver(xmlResolver);
        } catch (SAXException sax) {
            Throwable embedded = sax.getException();
            if (embedded == null) {
                embedded = sax;
            }
            throw new ToolException(sax.getMessage());
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

    private Document doValidation(InputSource wsdlsource, boolean deep) throws IOException, SAXException,
        ToolException {

        byte[] bytes = getBytes(wsdlsource);
        parser.parse(copyInputSource(wsdlsource, bytes));
        Document doc = parser.getDocument();

        String base = wsdlsource.getSystemId();
        if (doc == null) {
            return null;
        }

        if (deep) {
            NodeList nodes = doc.getDocumentElement().getElementsByTagNameNS(WSDLConstants.NS_WSDL, "import");
            for (int i = 0; i < nodes.getLength(); i++) {
                Element el = (Element)nodes.item(i);
                String ns = el.getAttribute("namespace");
                String loc = el.getAttribute("location");

                if (!"".equals(ns) && !"".equals(loc)) {

                    SchemaWSDLValidator validator = new SchemaWSDLValidator(schemaLocation);
                    validator.setErrorHandler(getErrorHandler());

                    validator.validate(new InputSource(getWsdlUrl(base, loc)), null, true);

                    if (!getErrorHandler().isValid()) {
                        throw new SAXException(getErrorHandler().getErrorMessages());
                    }
                    return doc;

                }
            }
        }
        StackTraceErrorHandler handler = getErrorHandler();
        if (handler.isValid()) {
            doSchemaValidation(wsdlsource, bytes, doc, handler);
        }
        return doc;
    }

    private byte[] getBytes(InputSource source) throws IOException {
        if (source.getByteStream() != null) {
            int length = source.getByteStream().available();
            byte[] bytes = new byte[length];
            source.getByteStream().read(bytes);
            return bytes;
        }
        return null;
    }

    private void doSchemaValidation(InputSource wsdlsource, byte[] bytes, Document doc,
                                    StackTraceErrorHandler handler) throws IOException, SAXException {
        if (isSchemaDocument(doc)) {
            XSGrammar xsGrammer = (XSGrammar)preparser.preparseGrammar(XMLGrammarDescription.XML_SCHEMA,
                                                                       copyInputSourceXML(wsdlsource, bytes));
            xsmodelList.add(xsGrammer.toXSModel());

        } else {
            Map schemas = serializeSchemaElements(doc);
            SchemaEntityResolver schemaResolver = (SchemaEntityResolver)preparser.getEntityResolver();
            schemaResolver.setSchemas(schemas);
            Iterator it = schemas.keySet().iterator();
            while (it.hasNext()) {
                String tns = (String)it.next();
                byte[] schemaBytes = (byte[])schemas.get(tns);
                WSDLSchemaErrorHandler schemaHandler = new WSDLSchemaErrorHandler(handler, schemaBytes, doc
                    .getXmlEncoding());

                try {
                    preparser.setErrorHandler(schemaHandler);

                    XSGrammar xsGrammer = (XSGrammar)preparser
                        .preparseGrammar(XMLGrammarDescription.XML_SCHEMA,
                                         copyInputSourceXML(wsdlsource, tns, schemaBytes, doc
                                             .getXmlEncoding()));
                    xsmodelList.add(xsGrammer.toXSModel());

                } finally {
                    preparser.setErrorHandler(handler);
                }
            }
        }
    }

    private boolean isSchemaDocument(Document doc) {
        String tagName = doc.getDocumentElement().getTagName();
        int idx = tagName.lastIndexOf(':');
        if (idx != -1) {
            tagName = tagName.substring(idx + 1, tagName.length());
        }
        return "schema".equals(tagName);
    }

    private Map<String, byte[]> serializeSchemaElements(Document doc) throws IOException {
        Map<String, byte[]> result = new HashMap<String, byte[]>();
        NodeList nodes = doc.getElementsByTagNameNS(WSDLConstants.NS_XMLNS, "schema");
        for (int x = 0; x < nodes.getLength(); x++) {
            Node schemaNode = nodes.item(x);
            Element schemaEl = (Element)schemaNode;
            String tns = schemaEl.getAttribute("targetNamespace");

            boolean clone = true;
            NamedNodeMap defAttrs = doc.getDocumentElement().getAttributes();
            for (int i = 0; i < defAttrs.getLength(); ++i) {
                Node attr = defAttrs.item(i);
                if (attr.getNodeName().startsWith("xmlns:")) {
                    if (!schemaEl.hasAttribute(attr.getNodeName()) && clone) {
                        schemaEl = (Element)(schemaNode.cloneNode(true));
                        clone = false;
                        schemaEl.setAttribute(attr.getNodeName(), attr.getNodeValue());
                    }

                    if (!schemaEl.hasAttribute(attr.getNodeName())) {
                        schemaEl.setAttribute(attr.getNodeName(), attr.getNodeValue());
                    }

                }
            }

            // Serialize the schema element.
            //
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            OutputFormat of = new OutputFormat(doc);
            XMLSerializer ser = new XMLSerializer(os, of);
            ser.serialize(schemaEl);
            os.flush();
            os.close();
            result.put(tns, os.toByteArray());
        }
        return result;
    }

    private InputSource copyInputSource(InputSource source, byte[] stream) {
        InputSource is = new InputSource(source.getSystemId());

        is.setPublicId(source.getPublicId());
        is.setEncoding(source.getEncoding());
        if (stream != null) {
            is.setByteStream(new ByteArrayInputStream(stream));
        }

        return is;
    }

    private XMLInputSource copyInputSourceXML(InputSource source, byte[] stream) {
        return copyInputSourceXML(source, stream, source.getEncoding());
    }

    private XMLInputSource copyInputSourceXML(InputSource source, byte[] stream, String encoding) {

        XMLInputSource ret = null;
        if (stream == null) {
            ret = new XMLInputSource(source.getPublicId(), source.getSystemId(), null);
        } else {
            ret = new XMLInputSource(source.getPublicId(), source.getSystemId(), null,
                                     new ByteArrayInputStream(stream), encoding);
        }
        return ret;
    }

    private XMLInputSource copyInputSourceXML(InputSource source, String tns, 
                                              byte[] stream, String encoding) {

        XMLInputSource ret = null;
        if (stream == null) {
            ret = new XMLInputSource(source.getPublicId(), source.getSystemId(), null);
        } else {
            ret = new XMLInputSource(tns, tns, null, new ByteArrayInputStream(stream), encoding);
        }
        return ret;
    }

    private String[] getDefaultSchemas() {

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
            return f.list(filter);
        }
        return null;
    }

    private String getNamespaceOfSchemas(String[] schemas) throws IOException, SAXException {

        String nsSchema = "";

        for (int i = 0; i < schemas.length; i++) {
            String url = null;
            url = getSchemaUrl(schemas[i]);
            DOMParser p = new DOMParser();
            p.parse(url);

            String tns = p.getDocument().getDocumentElement().getAttribute("targetNamespace");
            nsSchema += tns + " " + url + " ";
        }
        return nsSchema;
    }

    private void setExternalSchemaLocations(String[] schemas) throws IOException, SAXException {
        String nsSchemas = getNamespaceOfSchemas(schemas);
        parser.setProperty("http://apache.org/xml/properties/schema/external-schemaLocation", nsSchemas);
        preparser.setProperty("http://apache.org/xml/properties/schema/external-schemaLocation", nsSchemas);
    }

    private StackTraceErrorHandler setErrorHandler() {
        StackTraceErrorHandler handler = new StackTraceErrorHandler();
        setErrorHandler(handler);
        return handler;
    }

    private StackTraceErrorHandler getErrorHandler() {
        return (StackTraceErrorHandler)parser.getErrorHandler();
    }

    private String getUrlFromString(String path) throws IOException {
        URL url = null;
        try {
            url = new URL(path);
        } catch (MalformedURLException mfex) {
            try {
                File f = new File(path);
                if (f.exists() && !f.isDirectory()) {
                    url = f.toURL();
                }
            } catch (Exception fnex) {
                // ignorable
            }
        } catch (Exception ex) {
            // ignorable
        }
        return (url == null) ? null : url.toString();
    }

    private URL getURL(String path) throws IOException {
        URL url = null;

        if (path == null) {
            return url;
        }
        try {
            url = URLFactory.createURL(path);
        } catch (MalformedURLException mfex) {
            File f = new File(path);
            url = f.toURL();
        }

        return url;
    }

    private String getUrlFromString(String base, String path) throws IOException {
        if (base != null) {
            URL url = getURL(base);
            if (path.indexOf(":") != -1) {
                URL tmpurl = getURL(path);
                if (tmpurl != null) {
                    path = tmpurl.toString();
                }
            }
            return new URL(url, path).toString();
        }
        return getUrlFromString(path);
    }

    private String resolveUrl(String path) throws IOException {
        return resolveUrl(null, path);
    }

    private String resolveUrl(String base, String path) throws IOException {
        String url = getUrlFromString(base, path);
        if (url == null) {
            url = getUrlFromString(schemaLocation, path);
        }
        return url;
    }

    private String getSchemaUrl(String path) throws IOException {
        String url = getUrlFromString(path);
        if (url == null && schemaLocation != null && !"".equals(schemaLocation.trim())) {

            url = getUrlFromString(schemaLocation, path);
        }

        if (url == null) {
            throw new IOException("The URL or filename for the specified schema does not exist: " + path);
        }

        return url;
    }

    private String getWsdlUrl(String path) throws IOException {
        return getWsdlUrl(null, path);
    }

    private String getWsdlUrl(String base, String path) throws IOException {
        String url = resolveUrl(base, path);

        if (url == null) {
            throw new IOException("The URL or filename for the specified WSDL does not exist: " + path);
        }

        return url;
    }

    class SchemaEntityResolver implements XMLEntityResolver {

        Map schemas;

        public void setSchemas(Map schemaMap) {
            schemas = schemaMap;
        }

        public XMLInputSource resolveEntity(XMLResourceIdentifier id) throws XNIException, IOException {
            if (id.getLiteralSystemId() != null) {
                String url = resolveUrl(id.getBaseSystemId(), id.getLiteralSystemId());
                if (url == null) {
                    String msg = "Can't resolve entity from publicID=" + id.getPublicId() + ", systemID="
                                 + id.getLiteralSystemId();

                    throw new IOException(msg);
                }
                return new XMLInputSource(id.getPublicId(), url, id.getBaseSystemId());
            } else {
                // Means the schema is in the same wsdl document
                String ns = id.getNamespace();
                byte[] schemaBytes = (byte[])schemas.get(ns);
                if (schemaBytes != null) {
                    return new XMLInputSource(id.getPublicId(), id.getLiteralSystemId(),
                                              id.getBaseSystemId(), new ByteArrayInputStream(schemaBytes),
                                              "UTF-8");
                } else {
                    return null;
                }
            }
        }

    }

    public List<XSModel> getXSModelList() {
        return xsmodelList;
    }

    public Document getSchemaValidatedDoc() {
        return schemaValidatedDoc;
    }
    
    public Map<QName , List> getMsgPartsMap() {
        return msgPartsMap;
    }
    
    public Map<QName , Map> getPortTypesMap() {
        return portTypes;
    }
    
    public Map<QName , QName> getBindingMap() {
        return bindingMap;
    }
   
    
}

class StackTraceErrorHandler implements ErrorHandler, XMLErrorHandler {
    protected boolean valid;
    private StringBuffer buffer;
    private int numErrors;
    private List<SAXParseException> errors;

    StackTraceErrorHandler() {
        valid = true;
        numErrors = 0;
        buffer = new StringBuffer();
        errors = new ArrayList<SAXParseException>();
    }

    public void error(SAXParseException ex) {
        addError(ex);
    }

    public void fatalError(SAXParseException ex) {
        addError(ex);
    }

    public void warning(SAXParseException ex) {
        // Warning messages are ignored.
        // return;
    }

    public void error(String domain, String key, XMLParseException ex) {
        addError(getSAXParseException(ex));
    }

    public void fatalError(String domain, String key, XMLParseException ex) {
        addError(getSAXParseException(ex));
    }

    public void warning(String domain, String key, XMLParseException ex) {
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

    private static SAXParseException getSAXParseException(XMLParseException ex) {
        return new SAXParseException(ex.getMessage(), ex.getPublicId(), ex.getLiteralSystemId(), ex
            .getLineNumber(), ex.getColumnNumber(), ex.getException());
    }
}

class WSDLSchemaErrorHandler implements XMLErrorHandler {
    private StackTraceErrorHandler handler;
    private String schemaText;

    WSDLSchemaErrorHandler(StackTraceErrorHandler hand, byte[] schemaBytes, String schemaEncoding) {
        this.handler = hand;
        try {
            schemaText = new String(schemaBytes, schemaEncoding);
        } catch (java.io.UnsupportedEncodingException ex) {
            // ignoreable
        }
    }

    public void error(String domain, String key, XMLParseException ex) {
        handler.addError(getErrorMessage(ex), getSAXParseException(ex));
    }

    public void fatalError(String domain, String key, XMLParseException ex) {
        handler.addError(getErrorMessage(ex), getSAXParseException(ex));
    }

    public void warning(String domain, String key, XMLParseException ex) {
        // Warning messages are ignored.
        // return;
    }

    private String getErrorMessage(XMLParseException ex) {
        String msg = null;
        if (schemaText != null) {
            int lineNumber = ex.getLineNumber();
            int start = 0;
            int end = schemaText.indexOf('\n');
            int idx = 1;
            while (end != -1 && idx < lineNumber) {
                start = end + 1;
                end = schemaText.indexOf('\n', start);
                ++idx;
            }
            if (end == -1) {
                end = schemaText.length();
            }

            if (idx == lineNumber) {
                msg = "line \"" + schemaText.substring(start, end).trim() + "\" in the schema of "
                      + ex.getLiteralSystemId() + ": " + ex.getMessage();
            }
        }

        if (msg == null) {
            msg = ex.getLiteralSystemId() + ": " + ex.getMessage();
        }

        return msg;
    }

    private static SAXParseException getSAXParseException(XMLParseException ex) {
        return new SAXParseException(ex.getMessage(), ex.getPublicId(), ex.getLiteralSystemId(), -1, -1, ex
            .getException());
    }

}
