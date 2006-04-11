package org.objectweb.celtix.tools.processors.wsdl2;

import java.io.File;
import java.io.IOException;
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

import javax.wsdl.Binding;
import javax.wsdl.BindingInput;
import javax.wsdl.BindingOperation;
import javax.wsdl.BindingOutput;
import javax.wsdl.Definition;
import javax.wsdl.Import;
import javax.wsdl.Message;
import javax.wsdl.Operation;
import javax.wsdl.Port;
import javax.wsdl.PortType;
import javax.wsdl.Service;
import javax.wsdl.Types;
import javax.wsdl.WSDLException;
import javax.wsdl.extensions.ExtensionRegistry;
import javax.wsdl.extensions.schema.Schema;
import javax.wsdl.extensions.schema.SchemaImport;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLReader;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.xml.sax.InputSource;

import com.sun.tools.xjc.api.S2JJAXBModel;
import com.sun.tools.xjc.api.SchemaCompiler;
import com.sun.tools.xjc.api.XJC;

import org.apache.velocity.app.Velocity;
import org.objectweb.celtix.common.logging.LogUtils;
import org.objectweb.celtix.common.util.StringUtils;
import org.objectweb.celtix.tools.common.Processor;
import org.objectweb.celtix.tools.common.ProcessorEnvironment;
import org.objectweb.celtix.tools.common.ToolConstants;
import org.objectweb.celtix.tools.common.ToolException;
import org.objectweb.celtix.tools.extensions.jaxws.CustomizationParser;
import org.objectweb.celtix.tools.extensions.jaxws.JAXWSBinding;
import org.objectweb.celtix.tools.extensions.jaxws.JAXWSBindingDeserializer;
import org.objectweb.celtix.tools.extensions.jaxws.JAXWSBindingSerializer;
import org.objectweb.celtix.tools.extensions.jms.JMSAddress;
import org.objectweb.celtix.tools.extensions.jms.JMSAddressSerializer;
import org.objectweb.celtix.tools.extensions.xmlformat.XMLFormat;
import org.objectweb.celtix.tools.extensions.xmlformat.XMLFormatBinding;
import org.objectweb.celtix.tools.extensions.xmlformat.XMLFormatBindingSerializer;
import org.objectweb.celtix.tools.extensions.xmlformat.XMLFormatSerializer;
import org.objectweb.celtix.tools.extensions.xmlformat.XMLHttpAddress;
import org.objectweb.celtix.tools.extensions.xmlformat.XMLHttpSerializer;
import org.objectweb.celtix.tools.generators.AbstractGenerator;
import org.objectweb.celtix.tools.processors.wsdl2.internal.ClassCollector;
import org.objectweb.celtix.tools.processors.wsdl2.internal.ClassNameAllocatorImpl;
import org.objectweb.celtix.tools.processors.wsdl2.validators.AbstractValidator;
import org.objectweb.celtix.tools.utils.FileWriterUtil;
import org.objectweb.celtix.tools.utils.JAXBUtils;

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
    List<Schema> schemaList = new ArrayList<Schema>();
    private final Map<String, AbstractGenerator> generators = new HashMap<String, AbstractGenerator>();
    private final List<AbstractValidator> validators = new ArrayList<AbstractValidator>();
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
                newName = oldName.substring(0, oldName.length() - 5) + newNameExt
                          + WSDL_FILE_NAME_EXT;
            } else {
                newName = oldName + newNameExt;
            }
        }
        if (env.get(ToolConstants.CFG_OUTPUTDIR) != null) {
            outputDir = (String)env.get(ToolConstants.CFG_OUTPUTDIR);
            if (!("/".equals(outputDir.substring(outputDir.length() - 1))
                  || "\\".equals(outputDir.substring(outputDir.length() - 1)))) {
                outputDir = outputDir + "/";
            }
        } else {
            outputDir = "./";
        }
        FileWriterUtil fw = new FileWriterUtil(outputDir);
        try {
            writer = fw.getWriter("", newName);
        } catch (IOException ioe) {
            org.objectweb.celtix.common.i18n.Message msg = 
                new org.objectweb.celtix.common.i18n.Message("FAIL_TO_WRITE_FILE", 
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
            registerExtenstions(wsdlReader);
            wsdlDefinition = wsdlReader.readWSDL(wsdlURL);
            parseImports(wsdlDefinition);
            buildWSDLDefinition();
        } catch (WSDLException we) {
            org.objectweb.celtix.common.i18n.Message msg = 
                new org.objectweb.celtix.common.i18n.Message("FAIL_TO_CREATE_WSDL_DEFINITION", LOG);
            throw new ToolException(msg, we);
        }
        
        
        
    }

    private void buildWSDLDefinition() {
        for (Definition def : importedDefinitions) {
            this.wsdlDefinition.addNamespace(def.getPrefix(def.getTargetNamespace()), def
                .getTargetNamespace());
            Object[] services = def.getServices().values().toArray();
            for (int i = 0; i < services.length; i++) {
                this.wsdlDefinition.addService((Service)services[i]);
            }

            Object[] messages = def.getMessages().values().toArray();
            for (int i = 0; i < messages.length; i++) {
                this.wsdlDefinition.addMessage((Message)messages[i]);
            }

            Object[] bindings = def.getBindings().values().toArray();
            for (int i = 0; i < bindings.length; i++) {
                this.wsdlDefinition.addBinding((Binding)bindings[i]);
            }

            Object[] portTypes = def.getPortTypes().values().toArray();
            for (int i = 0; i < portTypes.length; i++) {
                this.wsdlDefinition.addPortType((PortType)portTypes[i]);
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
            org.objectweb.celtix.common.i18n.Message msg = 
                new org.objectweb.celtix.common.i18n.Message("FAIL_TO_INITIALIZE_VELOCITY_ENGINE", LOG);
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
        
        buildJaxbModel();
    }

    @SuppressWarnings("unchecked")
    private void buildJaxbModel() {
        SchemaCompiler schemaCompiler = XJC.createSchemaCompiler();        
        ClassNameAllocatorImpl allocator = new ClassNameAllocatorImpl(classColletor);

        allocator.setPortTypes(wsdlDefinition.getPortTypes().values(), env.mapPackageName(this.wsdlDefinition
            .getTargetNamespace()));
        schemaCompiler.setClassNameAllocator(allocator);
        schemaCompiler.setErrorListener(this);
        
        SchemaCompiler schemaCompilerGenCode = schemaCompiler;
        if (env.isExcludeNamespaceEnabled()) {            
            schemaCompilerGenCode = XJC.createSchemaCompiler();
            schemaCompilerGenCode.setClassNameAllocator(allocator);
            schemaCompilerGenCode.setErrorListener(this);        
        }
        for (Schema schema : schemaList) {
            boolean skipGenCode = false;
            
            Element schemaElement = schema.getElement();
            String targetNamespace = schemaElement.getAttribute("targetNamespace");
            if (StringUtils.isEmpty(targetNamespace)) {
                continue;
            }
            //             if (targetNamespace == null || targetNamespace.trim().length() == 0) {
            //                 continue;
            //             }
            if (env.hasExcludeNamespace(targetNamespace)
                && env.getExcludePackageName(targetNamespace) == null) {
                skipGenCode = true;
            }
            customizeSchema(schemaElement, targetNamespace);
            String systemid = schema.getDocumentBaseURI();            
            schemaCompiler.parseSchema(systemid, schemaElement);
            if (env.isExcludeNamespaceEnabled() && !skipGenCode) {
                schemaCompilerGenCode.parseSchema(systemid, schemaElement);
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
                        if (!isSchemaImported(schemaImport.getReferencedSchema())) {
                            addSchema(schemaImport.getReferencedSchema());
                        }
                    }
                }
            }
        }
        if (!isSchemaImported(schema)) {

            schemaList.add(schema);
        }
    }

    private void parseCustomization() {
        CustomizationParser customizationParser = CustomizationParser.getInstance();
        customizationParser.clean();
        if (!env.optionSet(ToolConstants.CFG_BINDING)) {
            return;
        }
        customizationParser.parse(env, wsdlDefinition);
    }

    protected void init() throws ToolException {

        parseWSDL((String)env.get(ToolConstants.CFG_WSDLURL));
        parseCustomization();
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

    public void addValidator(AbstractValidator validator) {
        this.validators.add(validator);
    }

    public void process() throws ToolException {
    }

    public void validateWSDL() throws ToolException {
        for (AbstractValidator validator : validators) {
            if (!validator.isValid()) {
                throw new ToolException(validator.getErrorMessage());
            }
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

    private void registerExtenstions(WSDLReader reader) {
        ExtensionRegistry registry = reader.getExtensionRegistry();
        if (registry == null) {
            registry = wsdlFactory.newPopulatedExtensionRegistry();
        }
        registerJAXWSBinding(registry, Definition.class);
        registerJAXWSBinding(registry, PortType.class);
        registerJAXWSBinding(registry, Operation.class);

        registerJAXWSBinding(registry, Binding.class);
        registerJAXWSBinding(registry, BindingOperation.class);

        registerJMSAddress(registry, Port.class);

        registerXMLFormat(registry, BindingInput.class);
        registerXMLFormat(registry, BindingOutput.class);
        registerXMLFormatBinding(registry, Binding.class);
        registerXMLHttpAddress(registry, Port.class);

        reader.setExtensionRegistry(registry);
    }

    private void registerXMLFormat(ExtensionRegistry registry, Class clz) {
        registry.registerSerializer(clz, ToolConstants.XML_FORMAT, new XMLFormatSerializer());

        registry.registerDeserializer(clz, ToolConstants.XML_FORMAT, new XMLFormatSerializer());
        registry.mapExtensionTypes(clz, ToolConstants.XML_FORMAT, XMLFormat.class);
    }

    private void registerXMLFormatBinding(ExtensionRegistry registry, Class clz) {
        registry.registerSerializer(clz, ToolConstants.XML_BINDING_FORMAT, new XMLFormatBindingSerializer());

        registry
            .registerDeserializer(clz, ToolConstants.XML_BINDING_FORMAT, new XMLFormatBindingSerializer());
        registry.mapExtensionTypes(clz, ToolConstants.XML_BINDING_FORMAT, XMLFormatBinding.class);
    }

    private void registerXMLHttpAddress(ExtensionRegistry registry, Class clz) {
        registry.registerSerializer(clz, ToolConstants.XML_HTTP_ADDRESS, new XMLHttpSerializer());

        registry.registerDeserializer(clz, ToolConstants.XML_HTTP_ADDRESS, new XMLHttpSerializer());
        registry.mapExtensionTypes(clz, ToolConstants.XML_HTTP_ADDRESS, XMLHttpAddress.class);
    }

    private void registerJMSAddress(ExtensionRegistry registry, Class clz) {
        registry.registerSerializer(clz, ToolConstants.JMS_ADDRESS, new JMSAddressSerializer());

        registry.registerDeserializer(clz, ToolConstants.JMS_ADDRESS, new JMSAddressSerializer());
        registry.mapExtensionTypes(clz, ToolConstants.JMS_ADDRESS, JMSAddress.class);
    }

    private void registerJAXWSBinding(ExtensionRegistry registry, Class clz) {
        registry.registerSerializer(clz, ToolConstants.JAXWS_BINDINGS, new JAXWSBindingSerializer());

        registry.registerDeserializer(clz, ToolConstants.JAXWS_BINDINGS, new JAXWSBindingDeserializer());
        registry.mapExtensionTypes(clz, ToolConstants.JAXWS_BINDINGS, JAXWSBinding.class);
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

}
