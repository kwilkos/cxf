package org.objectweb.celtix.tools.misc.processor;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.wsdl.Binding;
import javax.wsdl.Definition;
import javax.wsdl.Import;
import javax.wsdl.Message;
import javax.wsdl.PortType;
import javax.wsdl.Service;
import javax.wsdl.WSDLException;
import javax.wsdl.extensions.schema.Schema;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLReader;

import org.objectweb.celtix.common.logging.LogUtils;
import org.objectweb.celtix.tools.common.Processor;
import org.objectweb.celtix.tools.common.ProcessorEnvironment;
import org.objectweb.celtix.tools.common.ToolConstants;
import org.objectweb.celtix.tools.common.ToolException;

import org.objectweb.celtix.tools.util.ClassCollector;
import org.objectweb.celtix.tools.util.FileWriterUtil;
import org.objectweb.celtix.tools.util.WSDLExtensionRegister;

import org.objectweb.celtix.tools.validator.internal.WSDL11Validator;

public class AbstractWSDLToProcessor implements Processor, com.sun.tools.xjc.api.ErrorListener {
    protected static final Logger LOG = LogUtils.getL7dLogger(AbstractWSDLToProcessor.class);
    protected static final String WSDL_FILE_NAME_EXT = ".wsdl";

    protected Definition wsdlDefinition;
    protected ProcessorEnvironment env;
    protected WSDLFactory wsdlFactory;
    protected WSDLReader wsdlReader;


    protected ClassCollector classColletor;
    List<Schema> schemaList = new ArrayList<Schema>();
    private List<Definition> importedDefinitions = new ArrayList<Definition>();

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
            WSDLExtensionRegister register = new WSDLExtensionRegister(wsdlFactory, wsdlReader);
            register.registerExtenstions();
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









/*
    private void parseCustomization() {
        CustomizationParser customizationParser = CustomizationParser.getInstance();
        customizationParser.clean();
        if (!env.optionSet(ToolConstants.CFG_BINDING)) {
            return;
        }
        customizationParser.parse(env, wsdlDefinition);
    }*/

    protected void init() throws ToolException {

    }


    public Definition getWSDLDefinition() {
        return this.wsdlDefinition;
    }


    public void process() throws ToolException {
    }

    public void validateWSDL() throws ToolException {
        if (env.validateWSDL()) {
            WSDL11Validator validator = new WSDL11Validator(this.wsdlDefinition, this.env);
            validator.isValid();
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

}
