package org.objectweb.celtix.tools.processors.wsdl2;

import java.io.*;
import java.util.*;

import javax.wsdl.Binding;
import javax.wsdl.Definition;
import javax.wsdl.Import;
import javax.wsdl.Message;
import javax.wsdl.Operation;
import javax.wsdl.PortType;
import javax.wsdl.Service;
import javax.wsdl.Types;
import javax.wsdl.WSDLException;
import javax.wsdl.extensions.ExtensionRegistry;
import javax.wsdl.extensions.schema.Schema;
import javax.wsdl.extensions.schema.SchemaImport;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLReader;

import org.w3c.dom.*;

import com.sun.tools.xjc.api.S2JJAXBModel;
import com.sun.tools.xjc.api.SchemaCompiler;
import com.sun.tools.xjc.api.XJC;
import org.apache.velocity.app.Velocity;
import org.objectweb.celtix.tools.common.Processor;
import org.objectweb.celtix.tools.common.ProcessorEnvironment;
import org.objectweb.celtix.tools.common.ToolConstants;
import org.objectweb.celtix.tools.common.toolspec.ToolException;
import org.objectweb.celtix.tools.generators.AbstractGenerator;
import org.objectweb.celtix.tools.jaxws.CustomizationParser;
import org.objectweb.celtix.tools.jaxws.JAXWSBinding;
import org.objectweb.celtix.tools.jaxws.JAXWSBindingDeserializer;
import org.objectweb.celtix.tools.jaxws.JAXWSBindingSerializer;
import org.objectweb.celtix.tools.processors.wsdl2.internal.ClassNameAllocatorImpl;
import org.objectweb.celtix.tools.utils.ClassCollectorUtil;

public class WSDLToProcessor implements Processor {

    protected Definition wsdlDefinition;
    protected ProcessorEnvironment env;
    protected WSDLFactory wsdlFactory;
    protected WSDLReader wsdlReader;
    protected S2JJAXBModel rawJaxbModel;
    protected ClassCollectorUtil classNameColletor = ClassCollectorUtil.getInstance();
    List<Schema> schemaList = new ArrayList<Schema>();
    private final Map<String, AbstractGenerator> generators = new HashMap<String, AbstractGenerator>();
    private List<Definition> importedDefinitions = new ArrayList<Definition>();

    private void parseWSDL(String wsdlURL) throws ToolException {
        try {
            wsdlFactory = WSDLFactory.newInstance();
            wsdlReader = wsdlFactory.newWSDLReader();
            wsdlReader.setFeature("javax.wsdl.verbose", false);
            registerExtenstions(wsdlReader);
            wsdlDefinition = wsdlReader.readWSDL(wsdlURL);
            parseImports(wsdlDefinition);
            buildWSDLDefinition();
        } catch (WSDLException we) {
            throw new ToolException("Can not create wsdl model, due to " + we.getMessage(), we);
        }
    }

    private void buildWSDLDefinition() {
        for (Definition def : importedDefinitions) {
            this.wsdlDefinition.addNamespace(def.getPrefix(def.getTargetNamespace()),
                                             def.getTargetNamespace());
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
            String uri = (String) iter.next();
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
            throw new ToolException("Can't initialize velocity engine", e);
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

        buildJaxbModel();
    }
    
    @SuppressWarnings("unchecked")
    private void buildJaxbModel() {
        SchemaCompiler schemaCompiler = XJC.createSchemaCompiler();

        String packageName = (String)env.get(ToolConstants.CFG_PACKAGENAME);
        if (packageName != null && packageName.trim().length() > 0) {
            schemaCompiler.setDefaultPackageName(packageName);
        }

        ClassNameAllocatorImpl allocator = new ClassNameAllocatorImpl();
        allocator.setPortTypes(wsdlDefinition.getPortTypes().values(), packageName);
        schemaCompiler.setClassNameAllocator(allocator);
        int schemaCount = 0;
        for (Schema schema : schemaList) {
            schemaCount++;
            Element schemaElement = schema.getElement();

            int nodeListLen = schemaElement.getElementsByTagNameNS(ToolConstants.SCHEMA_URI,
                                                                   "import").getLength();
            for (int i = 0; i < nodeListLen; i++) {
                removeImportElement(schemaElement);
            }
            
            String targetNamespace = schemaElement.getAttribute("targetNamespace");
            if (targetNamespace == null || targetNamespace.trim().length() == 0) {
                continue;
            }
            schemaCompiler.parseSchema(targetNamespace + "#types" + schemaCount, schemaElement);
        }
        rawJaxbModel = schemaCompiler.bind();
    }

    private void removeImportElement(Element element) {
        NodeList nodeList = element.getElementsByTagNameNS(ToolConstants.SCHEMA_URI, "import");
        if (nodeList.getLength() > 0) {
            Node importNode = nodeList.item(0);
            Node schemaNode = importNode.getParentNode();
            schemaNode.removeChild(importNode);
        }
    }

    @SuppressWarnings("unchecked")
    private void addSchema(Schema schema) {
        Map<String, List> imports = schema.getImports();
        if (imports != null && imports.size() > 0) {
            Collection<String> importKeys = imports.keySet();
            for (String importNamespace : importKeys) {
                List<SchemaImport> schemaImports = imports.get(importNamespace);
                for (SchemaImport schemaImport : schemaImports) {
                    addSchema(schemaImport.getReferencedSchema());
                }
            }
        }
        schemaList.add(schema);
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

        reader.setExtensionRegistry(registry);
    }

    private void registerJAXWSBinding(ExtensionRegistry registry, Class clz) {
        registry.registerSerializer(clz,
                                    ToolConstants.JAXWS_BINDINGS,
                                    new JAXWSBindingSerializer());
        
        registry.registerDeserializer(clz,
                                      ToolConstants.JAXWS_BINDINGS,
                                      new JAXWSBindingDeserializer());
        registry.mapExtensionTypes(clz,
                                   ToolConstants.JAXWS_BINDINGS,
                                   JAXWSBinding.class);
    }
}
