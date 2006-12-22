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

package org.apache.cxf.tools.wsdl2java.processor;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
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

import javax.wsdl.extensions.schema.Schema;
import javax.wsdl.extensions.schema.SchemaImport;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLReader;
import javax.xml.namespace.QName;

import org.xml.sax.InputSource;

import com.sun.tools.xjc.api.S2JJAXBModel;
import com.sun.tools.xjc.model.Model;

import org.apache.cxf.common.logging.LogUtils;
import org.apache.cxf.resource.URIResolver;
import org.apache.cxf.tools.common.DataBindingGenerator;
import org.apache.cxf.tools.common.FrontEndGenerator;
import org.apache.cxf.tools.common.Processor;
import org.apache.cxf.tools.common.ToolConstants;
import org.apache.cxf.tools.common.ToolContext;
import org.apache.cxf.tools.common.ToolException;
import org.apache.cxf.tools.common.extensions.jaxws.CustomizationParser;
import org.apache.cxf.tools.util.ClassCollector;
import org.apache.cxf.tools.util.FileWriterUtil;
import org.apache.cxf.tools.util.SOAPBindingUtil;
import org.apache.cxf.tools.util.WSDLExtensionRegister;
import org.apache.cxf.tools.validator.internal.WSDL11Validator;
import org.apache.cxf.tools.wsdl2java.databindings.jaxb.JAXBBindingGenerator;

import org.apache.cxf.wsdl4jutils.WSDLResolver;
import org.apache.velocity.app.Velocity;

public class WSDLToProcessor implements Processor {
    protected static final Logger LOG = LogUtils.getL7dLogger(WSDLToProcessor.class);
    protected static final String WSDL_FILE_NAME_EXT = ".wsdl";

    protected Definition wsdlDefinition;
    protected ToolContext env;
    protected WSDLFactory wsdlFactory;
    protected WSDLReader wsdlReader;
    protected S2JJAXBModel rawJaxbModel;
    protected S2JJAXBModel rawJaxbModelGenCode;

    protected ClassCollector classColletor;
    protected List<String> excludePkgList = new ArrayList<String>();
    protected List<String> excludeGenFiles;
    protected Map<QName, Service> importedServices = new java.util.HashMap<QName, Service>();
    protected Map<QName, PortType> importedPortTypes = new java.util.HashMap<QName, PortType>();
    protected List<FrontEndGenerator> generators;

    //  For process nestedJaxbBinding
    protected boolean nestedJaxbBinding;
    protected Model model;
    protected DataBindingGenerator bindingGenerator;

    protected List<Schema> schemaList = new ArrayList<Schema>();
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
            register.registerExtensions();
            URIResolver resolver = new URIResolver(wsdlURL);
            InputSource insource = new InputSource(resolver.getInputStream());
            wsdlURL = resolver.getURI().toString();
            wsdlDefinition = wsdlReader.readWSDL(new WSDLResolver(wsdlURL, insource));
            
            parseImports(wsdlDefinition);
            buildImportedMaps();
        } catch (Exception we) {
            org.apache.cxf.common.i18n.Message msg =
                new org.apache.cxf.common.i18n.Message("FAIL_TO_CREATE_WSDL_DEFINITION",
                                                             LOG, wsdlURL);
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

    private void initDataModel() {
        schemaTargetNamespaces.clear();
        extractSchema(wsdlDefinition);
        for (Definition def : importedDefinitions) {
            extractSchema(def);
        }
        env.put(ToolConstants.WSDL_DEFINITION, wsdlDefinition);
        env.put(ToolConstants.IMPORTED_DEFINITION, importedDefinitions);
        env.put(ToolConstants.SCHEMA_LIST, schemaList);
        env.put(ToolConstants.SCHEMA_TARGET_NAMESPACES, schemaTargetNamespaces);
        env.put(ToolConstants.PORTTYPE_MAP, getPortTypes(wsdlDefinition));

        if (schemaList.size() == 0) {
            if (env.isVerbose()) {
                System.err.println("No schema provided in the wsdl file");
            }
            return;
        }

        try {
            bindingGenerator = (DataBindingGenerator)new JAXBBindingGenerator();
            bindingGenerator.initialize(env);
            env.put(ToolConstants.BINDING_GENERATOR, bindingGenerator);
        
        } catch (Exception e) {
            org.apache.cxf.common.i18n.Message msg =
                new org.apache.cxf.common.i18n.Message("FAIL_TO_CREATE_DATABINDING_MODEL",
                                                             LOG, new Object[] {e.getLocalizedMessage()});
            LOG.log(Level.SEVERE, msg.toString());
            throw new ToolException(msg, e);
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
        env.put(ClassCollector.class, new ClassCollector());
        initDataModel();

    }

    

    public Definition getWSDLDefinition() {
        return this.wsdlDefinition;
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
        for (FrontEndGenerator plugin : generators) {
            plugin.generate(env);
        }
    }

    public void setEnvironment(ToolContext penv) {
        this.env = penv;
    }

    public ToolContext getEnvironment() {
        return this.env;
    }

    

    public void checkSupported(Definition def) throws ToolException {
        if (isRPCEncoded(wsdlDefinition)) {
            org.apache.cxf.common.i18n.Message msg =
                new org.apache.cxf.common.i18n.Message("UNSUPPORTED_RPC_ENCODED",
                                                             LOG);
            throw new ToolException(msg);
        }
    }

    private boolean isRPCEncoded(Definition def) {
        Iterator ite1 = def.getBindings().values().iterator();
        while (ite1.hasNext()) {
            Binding binding = (Binding)ite1.next();
            String bindingStyle = SOAPBindingUtil.getBindingStyle(binding);

            Iterator ite2 = binding.getBindingOperations().iterator();
            while (ite2.hasNext()) {
                BindingOperation bop = (BindingOperation)ite2.next();
                String bopStyle = SOAPBindingUtil.getSOAPOperationStyle(bop);

                String outputUse = "";
                if (SOAPBindingUtil.getBindingOutputSOAPBody(bop) != null) {
                    outputUse = SOAPBindingUtil.getBindingOutputSOAPBody(bop).getUse();
                }
                String inputUse = "";
                if (SOAPBindingUtil.getBindingInputSOAPBody(bop) != null) {
                    inputUse = SOAPBindingUtil.getBindingInputSOAPBody(bop).getUse();
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

    

}
