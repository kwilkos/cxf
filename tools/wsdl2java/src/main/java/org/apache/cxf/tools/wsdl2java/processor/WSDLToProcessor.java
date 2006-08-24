package org.apache.cxf.tools.wsdl2java.processor;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.jws.soap.SOAPBinding;
import javax.wsdl.Binding;
import javax.wsdl.BindingOperation;
import javax.wsdl.Definition;
import javax.wsdl.Import;
import javax.wsdl.Port;
import javax.wsdl.PortType;
import javax.wsdl.Service;
import javax.wsdl.Types;
import javax.wsdl.WSDLException;
import javax.wsdl.extensions.schema.Schema;
import javax.wsdl.extensions.schema.SchemaImport;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLReader;
import javax.xml.namespace.QName;
import javax.xml.transform.Result;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.xml.sax.InputSource;

import com.sun.codemodel.JCodeModel;
import com.sun.tools.xjc.BadCommandLineException;
import com.sun.tools.xjc.ErrorReceiver;
import com.sun.tools.xjc.Language;
import com.sun.tools.xjc.ModelLoader;
import com.sun.tools.xjc.Options;
import com.sun.tools.xjc.api.S2JJAXBModel;
import com.sun.tools.xjc.api.XJC;
import com.sun.tools.xjc.api.impl.s2j.SchemaCompilerImpl;
import com.sun.tools.xjc.model.Model;

import org.apache.velocity.app.Velocity;
import org.apache.cxf.common.logging.LogUtils;
import org.apache.cxf.common.util.StringUtils;
import org.apache.cxf.helpers.WSDLHelper;
import org.apache.cxf.tools.common.Processor;
import org.apache.cxf.tools.common.ProcessorEnvironment;
import org.apache.cxf.tools.common.ToolConstants;
import org.apache.cxf.tools.common.ToolException;
import org.apache.cxf.tools.common.extensions.jaxws.CustomizationParser;
import org.apache.cxf.tools.util.ClassCollector;
import org.apache.cxf.tools.util.FileWriterUtil;
import org.apache.cxf.tools.util.JAXBUtils;
import org.apache.cxf.tools.util.URIParserUtil;
import org.apache.cxf.tools.util.WSDLExtensionRegister;
import org.apache.cxf.tools.validator.internal.WSDL11Validator;
import org.apache.cxf.tools.wsdl2java.generator.AbstractGenerator;
import org.apache.cxf.tools.wsdl2java.processor.internal.ClassNameAllocatorImpl;
import org.apache.cxf.tools.wsdl2java.processor.internal.JAXBBindingMerger;

public class WSDLToProcessor implements Processor, com.sun.tools.xjc.api.ErrorListener {
    protected static final Logger LOG = LogUtils.getL7dLogger(WSDLToProcessor.class);
    protected static final String WSDL_FILE_NAME_EXT = ".wsdl";

    protected Definition wsdlDefinition;
    protected ProcessorEnvironment env;
    protected WSDLFactory wsdlFactory;
    protected WSDLReader wsdlReader;
    protected S2JJAXBModel rawJaxbModel;
    protected S2JJAXBModel rawJaxbModelGenCode;

    protected ClassCollector classColletor;
    protected List<String> excludePkgList = new ArrayList<String>();
    protected List<String> excludeGenFiles;
    protected Map<QName, Service> importedServices = new java.util.HashMap<QName, Service>();
    protected Map<QName, PortType> importedPortTypes = new java.util.HashMap<QName, PortType>();

    //  For process nestedJaxbBinding
    protected boolean nestedJaxbBinding;
    protected Model model;

    
    protected List<Schema> schemaList = new ArrayList<Schema>();
    private final Map<String, AbstractGenerator> generators = new HashMap<String, AbstractGenerator>();
    private List<Definition> importedDefinitions = new ArrayList<Definition>();
    private List<String> schemaTargetNamespaces = new ArrayList<String>();
    
    protected Writer getOutputWriter(String newNameExt) throws ToolException {
        Writer writer = null;
        String newName = null;
        String outputDir;

        if (env.get(ToolConstants.CFG_OUTPUTFILE) != null) {
            newName = (String)env.get(ToolConstants.CFG_OUTPUTFILE);
        } else {
            String oldName = (String)env.get(ToolConstants.CFG_WSDLURL);
            int position = oldName.lastIndexOf("/");
            if (position < 0) {
                position = oldName.lastIndexOf("\\");
            }
            if (position >= 0) {
                oldName = oldName.substring(position + 1, oldName.length());
            }
            if (oldName.toLowerCase().indexOf(WSDL_FILE_NAME_EXT) >= 0) {
                newName = oldName.substring(0, oldName.length() - 5) + newNameExt + WSDL_FILE_NAME_EXT;
            } else {
                newName = oldName + newNameExt;
            }
        }
        if (env.get(ToolConstants.CFG_OUTPUTDIR) != null) {
            outputDir = (String)env.get(ToolConstants.CFG_OUTPUTDIR);
            if (!("/".equals(outputDir.substring(outputDir.length() - 1)) || "\\".equals(outputDir
                .substring(outputDir.length() - 1)))) {
                outputDir = outputDir + "/";
            }
        } else {
            outputDir = "./";
        }
        FileWriterUtil fw = new FileWriterUtil(outputDir);
        try {
            writer = fw.getWriter("", newName);
        } catch (IOException ioe) {
            org.apache.cxf.common.i18n.Message msg = 
                new org.apache.cxf.common.i18n.Message("FAIL_TO_WRITE_FILE",
                                                              LOG,
                                                              env.get(ToolConstants.CFG_OUTPUTDIR)
                                                              + System.getProperty("file.seperator")
                                                              + newName);
            throw new ToolException(msg, ioe);
        }
        return writer;
    }

    protected void parseWSDL(String wsdlURL) throws ToolException {
        try {
            wsdlFactory = WSDLFactory.newInstance();
            wsdlReader = wsdlFactory.newWSDLReader();
            wsdlReader.setFeature("javax.wsdl.verbose", false);
            WSDLExtensionRegister register = new WSDLExtensionRegister(wsdlFactory, wsdlReader);
            register.registerExtenstions();
            wsdlDefinition = wsdlReader.readWSDL(wsdlURL);
            parseImports(wsdlDefinition);
            buildImportedMaps();
        } catch (WSDLException we) {
            org.apache.cxf.common.i18n.Message msg = 
                new org.apache.cxf.common.i18n.Message("FAIL_TO_CREATE_WSDL_DEFINITION", 
                                                             LOG);
            throw new ToolException(msg, we);
        }

    }

    @SuppressWarnings("unchecked")
    private void buildImportedMaps() {
        for (Definition def : importedDefinitions) {
            for (java.util.Iterator<QName> ite = def.getServices().keySet().iterator(); ite.hasNext();) {
                QName qn = ite.next();
                importedServices.put(qn, (Service)def.getServices().get(qn));
            }

        }

        if (getWSDLDefinition().getServices().size() == 0 && importedServices.size() == 0) {
            for (Definition def : importedDefinitions) {
                for (java.util.Iterator<QName> ite = def.getPortTypes().keySet().iterator(); ite.hasNext();) {
                    QName qn = ite.next();
                    importedPortTypes.put(qn, (PortType)def.getPortTypes().get(qn));
                }

            }

        }
    }

    @SuppressWarnings("unchecked")
    private void parseImports(Definition def) {
        List<Import> importList = new ArrayList<Import>();
        Map imports = def.getImports();
        for (Iterator iter = imports.keySet().iterator(); iter.hasNext();) {
            String uri = (String)iter.next();
            importList.addAll((List<Import>)imports.get(uri));
        }
        for (Import impt : importList) {
            parseImports(impt.getDefinition());
            importedDefinitions.add(impt.getDefinition());
        }
    }

    private String getVelocityLogFile(String logfile) {
        String logdir = System.getProperty("user.home");
        if (logdir == null || logdir.length() == 0) {
            logdir = System.getProperty("user.dir");
        }
        return logdir + File.separator + logfile;
    }

    private void initVelocity() throws ToolException {
        try {
            Properties props = new Properties();
            String clzName = "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader";
            props.put("resource.loader", "class");
            props.put("class.resource.loader.class", clzName);
            props.put("runtime.log", getVelocityLogFile("velocity.log"));

            Velocity.init(props);
        } catch (Exception e) {
            org.apache.cxf.common.i18n.Message msg = 
                new org.apache.cxf.common.i18n.Message("FAIL_TO_INITIALIZE_VELOCITY_ENGINE",
                                                             LOG);
            LOG.log(Level.SEVERE, msg.toString());
            throw new ToolException(msg, e);
        }
    }

    private void extractSchema(Definition def) {
        Types typesElement = def.getTypes();
        if (typesElement != null) {
            Iterator ite = typesElement.getExtensibilityElements().iterator();
            while (ite.hasNext()) {
                Object obj = ite.next();
                if (obj instanceof Schema) {
                    Schema schema = (Schema)obj;
                    addSchema(schema);
                }
            }
        }
    }

    private void initJAXBModel() {
        schemaTargetNamespaces.clear();
        extractSchema(wsdlDefinition);
        for (Definition def : importedDefinitions) {
            extractSchema(def);
        }

        if (schemaList.size() == 0) {
            if (env.isVerbose()) {
                System.err.println("No schema provided in the wsdl file");
            }
            return;
        }

        schemaTargetNamespaces.clear();
        try {
            buildJaxbModel();
        } catch (Exception e) {
            org.apache.cxf.common.i18n.Message msg = 
                new org.apache.cxf.common.i18n.Message("FAIL_TO_CREATE_JAXB_MODEL",
                                                             LOG);
            LOG.log(Level.SEVERE, msg.toString());
            throw new ToolException(msg, e);
        }
    }

    @SuppressWarnings("unchecked")
    private void buildJaxbModel() throws Exception {
        SchemaCompilerImpl schemaCompiler = (SchemaCompilerImpl)XJC.createSchemaCompiler();

        ClassNameAllocatorImpl allocator = new ClassNameAllocatorImpl(classColletor);

        allocator.setPortTypes(getPortTypes(wsdlDefinition).values(), env.mapPackageName(this.wsdlDefinition
            .getTargetNamespace()));
        schemaCompiler.setClassNameAllocator(allocator);
        schemaCompiler.setErrorListener(this);

        SchemaCompilerImpl schemaCompilerGenCode = schemaCompiler;
        String excludePackageName = "";
        if (env.isExcludeNamespaceEnabled()) {
            schemaCompilerGenCode = (SchemaCompilerImpl)XJC.createSchemaCompiler();
            schemaCompilerGenCode.setClassNameAllocator(allocator);
            schemaCompilerGenCode.setErrorListener(this);
        }
        List schemaSystemidList = new ArrayList();
        // this variable for jaxb binding nested in jaxws
        Options opt = new OptionsEx();
        int fileIDX = 0;
        for (Schema schema : schemaList) {
            String xsdFile = "schema" + (fileIDX++);
            Element schemaElement = schema.getElement();
            String targetNamespace = schemaElement.getAttribute("targetNamespace");
            if (StringUtils.isEmpty(targetNamespace)) {
                continue;
            }

            if (env.hasExcludeNamespace(targetNamespace)) {
                excludePackageName = env.getExcludePackageName(targetNamespace);
                if (excludePackageName != null) {
                    excludePkgList.add(excludePackageName);
                } else {
                    excludePkgList.add(URIParserUtil.getPackageName(targetNamespace));
                }
            }
            customizeSchema(schemaElement, targetNamespace);
            String systemid = schema.getDocumentBaseURI();
            if (schemaSystemidList.contains(systemid)) {
                systemid = schema.getDocumentBaseURI() + "#" + targetNamespace;
            }
            schemaSystemidList.add(systemid);
            schemaCompiler.parseSchema(systemid, schemaElement);
            schemaCompilerGenCode.parseSchema(systemid, schemaElement);
            if (nestedJaxbBinding) {
                File file = File.createTempFile(xsdFile, ".xsd");
                Result result = new StreamResult(file);
                DOMSource source = new DOMSource(schemaElement);

                TransformerFactory.newInstance().newTransformer().transform(source, result);
                InputSource insource = new InputSource((InputStream)new FileInputStream(file));
                insource.setSystemId(systemid);
                opt.setSchemaLanguage(Language.XMLSCHEMA);
                opt.addGrammar(file);
               
            } 

        }

        Collection<InputSource> jaxbBindingFiles = env.getJaxbBindingFile().values();
        for (InputSource bindingFile : jaxbBindingFiles) {
            schemaCompiler.parseSchema(bindingFile);
            if (env.isExcludeNamespaceEnabled()) {
                schemaCompilerGenCode.parseSchema(bindingFile);
            }
        }
        rawJaxbModel = schemaCompiler.bind();
        if (env.isExcludeNamespaceEnabled()) {
            rawJaxbModelGenCode = schemaCompilerGenCode.bind();
        } else {
            rawJaxbModelGenCode = rawJaxbModel;
        }
        if (nestedJaxbBinding) {
            opt.classNameAllocator = allocator;
            model = ModelLoader.load(opt, new JCodeModel(), new JAXBErrorReceiver());
            model.generateCode(opt, new JAXBErrorReceiver());
        }
       
    }

    @SuppressWarnings("unchecked")
    protected Map<QName, PortType> getPortTypes(Definition definition) {
        Map<QName, PortType> portTypes = definition.getPortTypes();
        if (portTypes.size() == 0) {
            for (Iterator ite = definition.getServices().values().iterator(); ite.hasNext();) {
                Service service = (Service)ite.next();
                for (Iterator ite2 = service.getPorts().values().iterator(); ite2.hasNext();) {
                    Port port = (Port)ite2.next();
                    Binding binding = port.getBinding();
                    portTypes.put(binding.getPortType().getQName(), binding.getPortType());
                }
            }
        }

        if (portTypes.size() == 0) {
            for (Iterator ite = importedServices.values().iterator(); ite.hasNext();) {
                Service service = (Service)ite.next();
                for (Iterator ite2 = service.getPorts().values().iterator(); ite2.hasNext();) {
                    Port port = (Port)ite2.next();
                    Binding binding = port.getBinding();
                    portTypes.put(binding.getPortType().getQName(), binding.getPortType());
                }
            }
        }

        if (portTypes.size() == 0) {
            portTypes.putAll(importedPortTypes);
        }

        return portTypes;
    }

    private boolean isSchemaParsed(String targetNamespace) {
        if (!schemaTargetNamespaces.contains(targetNamespace)) {
            schemaTargetNamespaces.add(targetNamespace);
            return false;
        } else {
            return true;
        }
    }

    private void customizeSchema(Element schema, String targetNamespace) {
        String userPackage = env.mapPackageName(targetNamespace);
        if (env.hasExcludeNamespace(targetNamespace) && env.getExcludePackageName(targetNamespace) != null) {
            // generate excluded namespace types classes with specified package
            // name
            userPackage = env.getExcludePackageName(targetNamespace);
        }
        if (!isSchemaParsed(targetNamespace) && !StringUtils.isEmpty(userPackage)) {
            Node jaxbBindings = JAXBUtils.innerJaxbPackageBinding(schema, userPackage);
            schema.appendChild(jaxbBindings);
        }

        int nodeListLen = schema.getElementsByTagNameNS(ToolConstants.SCHEMA_URI, "import").getLength();
        for (int i = 0; i < nodeListLen; i++) {
            removeImportElement(schema);
        }
        JAXBBindingMerger jaxbBindingMerger = new JAXBBindingMerger();
        jaxbBindingMerger.mergeJaxwsBinding(schema, env);

        if (jaxbBindingMerger.isMerged()) {
            nestedJaxbBinding = true;
        }
    }

    private void removeImportElement(Element element) {
        NodeList nodeList = element.getElementsByTagNameNS(ToolConstants.SCHEMA_URI, "import");
        if (nodeList.getLength() > 0) {
            Node importNode = nodeList.item(0);
            Node schemaNode = importNode.getParentNode();
            schemaNode.removeChild(importNode);
        }
    }

    private boolean isSchemaImported(Schema schema) {
        return schemaList.contains(schema);
    }

    @SuppressWarnings("unchecked")
    private void addSchema(Schema schema) {
        Map<String, List> imports = schema.getImports();
        if (imports != null && imports.size() > 0) {
            Collection<String> importKeys = imports.keySet();
            for (String importNamespace : importKeys) {
                if (!isSchemaParsed(importNamespace + "?file=" + schema.getDocumentBaseURI())) {
                    List<SchemaImport> schemaImports = imports.get(importNamespace);
                    for (SchemaImport schemaImport : schemaImports) {
                        Schema tempImport = schemaImport.getReferencedSchema();
                        if (tempImport != null && !isSchemaImported(tempImport)) {
                            addSchema(tempImport);
                        }
                    }
                }
            }
        }
        if (!isSchemaImported(schema)) {
            schemaList.add(schema);
        }
    }

    public void parseCustomization(Definition def) {
        CustomizationParser customizationParser = CustomizationParser.getInstance();
        customizationParser.clean();
        if (!env.optionSet(ToolConstants.CFG_BINDING)) {
            return;
        }
        customizationParser.parse(env, def);
    }

    protected void init() throws ToolException {
        parseWSDL((String)env.get(ToolConstants.CFG_WSDLURL));
        checkSupported(getWSDLDefinition());
        validateWSDL(getWSDLDefinition());
        parseCustomization(getWSDLDefinition());
        initVelocity();
        classColletor = new ClassCollector();
        env.put(ToolConstants.GENERATED_CLASS_COLLECTOR, classColletor);
        initJAXBModel();

    }

    public S2JJAXBModel getRawJaxbModel() {
        return this.rawJaxbModel;
    }

    public Definition getWSDLDefinition() {
        return this.wsdlDefinition;
    }

    public void addGenerator(String name, AbstractGenerator gen) {
        generators.put(name, gen);
    }

    public void process() throws ToolException {
    }

    public void validateWSDL(Definition def) throws ToolException {
        if (env.validateWSDL()) {
            WSDL11Validator validator = new WSDL11Validator(def, this.env);
            validator.isValid();
        }
    }

    protected void doGeneration() throws ToolException {
        for (String genName : generators.keySet()) {
            AbstractGenerator gen = generators.get(genName);
            gen.generate();
        }
    }

    public void setEnvironment(ProcessorEnvironment penv) {
        this.env = penv;
    }

    public ProcessorEnvironment getEnvironment() {
        return this.env;
    }

    public void error(org.xml.sax.SAXParseException exception) {
        if (this.env.isVerbose()) {
            exception.printStackTrace();
        } else {
            System.err.println("Parsing schema error: \n" + exception.toString());
        }
    }

    public void fatalError(org.xml.sax.SAXParseException exception) {
        if (this.env.isVerbose()) {
            exception.printStackTrace();
        } else {
            System.err.println("Parsing schema fatal error: \n" + exception.toString());
        }
    }

    public void info(org.xml.sax.SAXParseException exception) {
        if (this.env.isVerbose()) {
            System.err.println("Parsing schema info: " + exception.toString());
        }
    }

    public void warning(org.xml.sax.SAXParseException exception) {
        if (this.env.isVerbose()) {
            System.err.println("Parsing schema warning " + exception.toString());
        }
    }

    public void checkSupported(Definition def) throws ToolException {
        if (isSOAP12Binding(wsdlDefinition)) {
            org.apache.cxf.common.i18n.Message msg = 
                new org.apache.cxf.common.i18n.Message("SOAP12_UNSUPPORTED",
                                                             LOG);
            throw new ToolException(msg);
        }

        if (isRPCEncoded(wsdlDefinition)) {
            org.apache.cxf.common.i18n.Message msg = 
                new org.apache.cxf.common.i18n.Message("UNSUPPORTED_RPC_ENCODED",
                                                             LOG);
            throw new ToolException(msg);
        }
    }

    private boolean isSOAP12Binding(Definition def) {
        String namespace = "";
        for (Iterator ite = def.getNamespaces().values().iterator(); ite.hasNext();) {
            namespace = (String)ite.next();
            if (namespace != null
                && namespace.toLowerCase().indexOf("http://schemas.xmlsoap.org/wsdl/soap12") >= 0) {
                return true;
            }
        }
        return false;
    }

    private boolean isRPCEncoded(Definition def) {
        WSDLHelper whelper = new WSDLHelper();
        Iterator ite1 = def.getBindings().values().iterator();
        while (ite1.hasNext()) {
            Binding binding = (Binding)ite1.next();
            String bindingStyle = whelper.getBindingStyle(binding);

            Iterator ite2 = binding.getBindingOperations().iterator();
            while (ite2.hasNext()) {
                BindingOperation bop = (BindingOperation)ite2.next();
                String bopStyle = whelper.getSOAPOperationStyle(bop);

                String outputUse = "";
                if (whelper.getBindingOutputSOAPBody(bop) != null) {
                    outputUse = whelper.getBindingOutputSOAPBody(bop).getUse();
                }
                String inputUse = "";
                if (whelper.getBindingInputSOAPBody(bop) != null) {
                    inputUse = whelper.getBindingInputSOAPBody(bop).getUse();
                }
                if ((SOAPBinding.Style.RPC.name().equalsIgnoreCase(bindingStyle) || SOAPBinding.Style.RPC
                    .name().equalsIgnoreCase(bopStyle))
                    && (SOAPBinding.Use.ENCODED.name().equalsIgnoreCase(inputUse) || SOAPBinding.Use.ENCODED
                        .name().equalsIgnoreCase(outputUse))) {
                    return true;
                }
            }

        }
        return false;
    }

    static class OptionsEx extends Options {

        protected Mode mode = Mode.CODE;

        protected int parseArgument(String[] args, int i) throws BadCommandLineException {

            return super.parseArgument(args, i);
        }
    }

    private static enum Mode {       
        CODE,
        BGM,
        SIGNATURE,
        FOREST,
        DRYRUN,
        ZIP,
    }

    class JAXBErrorReceiver extends ErrorReceiver {
        public void warning(org.xml.sax.SAXParseException saxEx) throws com.sun.tools.xjc.AbortException {
            if (env.isVerbose()) {
                saxEx.printStackTrace();
            } else {
                System.err.println("Use jaxb customization binding file to generate types warring " 
                                   + saxEx.getMessage());
            }
            
        }

        public void error(org.xml.sax.SAXParseException saxEx) throws com.sun.tools.xjc.AbortException {
            if (env.isVerbose()) {
                saxEx.printStackTrace();
            } else {
                System.err.println("Use jaxb customization binding file to generate types error " 
                                   + saxEx.getMessage());
            }
        }

        public void info(org.xml.sax.SAXParseException saxEx) {
            if (env.isVerbose()) {
                saxEx.printStackTrace();
            } 
        }

        public void fatalError(org.xml.sax.SAXParseException saxEx) throws com.sun.tools.xjc.AbortException {
            if (env.isVerbose()) {
                saxEx.printStackTrace();
            } else {
                System.err.println("Use jaxb customization binding file to generate types fatal error " 
                                   + saxEx.getMessage());
            }
        }
    }

}
