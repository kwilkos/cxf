package org.objectweb.celtix.tools.processors.wsdl2;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.wsdl.Definition;
import javax.wsdl.PortType;

import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.writer.FileCodeWriter;
import com.sun.tools.xjc.api.S2JJAXBModel;

import org.objectweb.celtix.tools.common.ToolConstants;
import org.objectweb.celtix.tools.common.model.JavaModel;
import org.objectweb.celtix.tools.common.toolspec.ToolException;
import org.objectweb.celtix.tools.generators.wsdl2.AntGenerator;
import org.objectweb.celtix.tools.generators.wsdl2.ClientGenerator;
import org.objectweb.celtix.tools.generators.wsdl2.FaultGenerator;
import org.objectweb.celtix.tools.generators.wsdl2.ImplGenerator;
import org.objectweb.celtix.tools.generators.wsdl2.SEIGenerator;
import org.objectweb.celtix.tools.generators.wsdl2.ServerGenerator;
import org.objectweb.celtix.tools.generators.wsdl2.ServiceGenerator;
import org.objectweb.celtix.tools.jaxws.CustomizationParser;
import org.objectweb.celtix.tools.jaxws.JAXWSBinding;
import org.objectweb.celtix.tools.processors.wsdl2.internal.ClassCollector;
import org.objectweb.celtix.tools.processors.wsdl2.internal.PortTypeProcessor;
import org.objectweb.celtix.tools.processors.wsdl2.internal.SEIAnnotationProcessor;
import org.objectweb.celtix.tools.processors.wsdl2.internal.ServiceProcessor;
import org.objectweb.celtix.tools.wsdl2.compile.Compiler;
import org.objectweb.celtix.tools.wsdl2.validate.MIMEBindingValidator;
import org.objectweb.celtix.tools.wsdl2.validate.UniqueBodyPartsValidator;
import org.objectweb.celtix.tools.wsdl2.validate.WSIBPValidator;
import org.objectweb.celtix.tools.wsdl2.validate.XMLFormatValidator;

public class WSDLToJavaProcessor extends WSDLToProcessor {

    protected void registerGenerators(JavaModel jmodel) {
        addGenerator(ToolConstants.SEI_GENERATOR, new SEIGenerator(jmodel, getEnvironment()));
        addGenerator(ToolConstants.FAULT_GENERATOR, new FaultGenerator(jmodel, getEnvironment()));
        addGenerator(ToolConstants.SVR_GENERATOR, new ServerGenerator(jmodel, getEnvironment()));
        addGenerator(ToolConstants.IMPL_GENERATOR, new ImplGenerator(jmodel, getEnvironment()));
        addGenerator(ToolConstants.CLT_GENERATOR, new ClientGenerator(jmodel, getEnvironment()));
        addGenerator(ToolConstants.SERVICE_GENERATOR, new ServiceGenerator(jmodel, getEnvironment()));
        addGenerator(ToolConstants.ANT_GENERATOR, new AntGenerator(jmodel, getEnvironment()));
    }

    protected void registerValidator() {
        this.addValidator(new UniqueBodyPartsValidator(this.wsdlDefinition));
        this.addValidator(new WSIBPValidator(this.wsdlDefinition));
        this.addValidator(new MIMEBindingValidator(this.wsdlDefinition));
        this.addValidator(new XMLFormatValidator(this.wsdlDefinition));
    }
    
    
    public void process() throws ToolException {
        init();
        registerValidator();
        validateWSDL();
        generateTypes();
        JavaModel jmodel = wsdlDefinitionToJavaModel(getWSDLDefinition());
        if (jmodel == null) {
            throw new ToolException("Can not create java model from wsdl model");
        }
        registerGenerators(jmodel);
        doGeneration();
        if (env.get(ToolConstants.CFG_COMPILE) != null) {
            compile();
        }
    }

    private void generateTypes() throws ToolException {
        if (rawJaxbModel == null) {
            return;
        }
        try {
            if (rawJaxbModel instanceof S2JJAXBModel) {
                S2JJAXBModel schem2JavaJaxbModel = (S2JJAXBModel)rawJaxbModel;
                JCodeModel jcodeModel = schem2JavaJaxbModel.generateCode(null, null);
                String dir = (String)env.get(ToolConstants.CFG_OUTPUTDIR);
                FileCodeWriter fileCodeWriter = new FileCodeWriter(new File(dir));
                jcodeModel.build(fileCodeWriter);
            } else {
                return;
            }
        } catch (IOException e) {
            throw new ToolException("Build type failed", e);
        }
    }

    private JavaModel wsdlDefinitionToJavaModel(Definition definition) throws ToolException {
        JavaModel javaModel = new JavaModel();
        getEnvironment().put(ToolConstants.RAW_JAXB_MODEL, getRawJaxbModel());

        javaModel.setJAXWSBinding(customizing(definition));

        Map portTypes = definition.getPortTypes();

        for (Iterator iter = portTypes.keySet().iterator(); iter.hasNext();) {
            PortType portType = (PortType)portTypes.get(iter.next());
            PortTypeProcessor portTypeProcessor = new PortTypeProcessor(getEnvironment());
            portTypeProcessor.process(javaModel, portType);
        }

        ServiceProcessor serviceProcessor = new ServiceProcessor(env, getWSDLDefinition());
        serviceProcessor.process(javaModel);

        SEIAnnotationProcessor seiAnnotationProcessor = new SEIAnnotationProcessor(env);
        seiAnnotationProcessor.process(javaModel);

        return javaModel;
    }

    private JAXWSBinding customizing(Definition def) {
        JAXWSBinding binding = CustomizationParser.getInstance().getDefinitionExtension();
        if (binding != null) {
            return binding;
        }
        
        List extElements = def.getExtensibilityElements();
        if (extElements.size() > 0) {
            Iterator iterator = extElements.iterator();
            while (iterator.hasNext()) {
                Object obj = iterator.next();
                if (obj instanceof JAXWSBinding) {
                    binding = (JAXWSBinding)obj;
                }
            }
        }
        
        if (binding == null) {
            binding = new JAXWSBinding();
        }
        return binding;
    }

    private void compile() throws ToolException {
        ClassCollector classCollector = (ClassCollector)env.get(ToolConstants.GENERATED_CLASS_COLLECTOR);
        int fileCount = classCollector.getGeneratedFileInfo().size();
        int index = 0;
        String javaClasspath = System.getProperty("java.class.path");
        boolean classpathSetted = javaClasspath != null && (javaClasspath.indexOf("celtix.jar") >= 0);
        String[] args = new String[fileCount + (classpathSetted ? 2 : 0) + (env.isVerbose() ? 1 : 0)
                                   + (env.get(ToolConstants.CFG_CLASSDIR) != null ? 2 : 0)];
        if (env.isVerbose()) {
            args[index] = "-verbose";
            index++;
        }

        if (!classpathSetted) {
            System.setProperty("java.ext.dirs", this.getClass().getClassLoader().getResource(".").getFile()
                                                + "../lib/");
        } else {
            args[index] = "-classpath";
            index++;
            args[index] = javaClasspath;
            index++;
        }

        if (env.get(ToolConstants.CFG_CLASSDIR) != null) {
            args[index] = "-d";
            index++;
            args[index] = (String)env.get(ToolConstants.CFG_CLASSDIR);
            index++;
        }
        Iterator ite = classCollector.getGeneratedFileInfo().iterator();
        while (ite.hasNext()) {
            String fileName = (String)ite.next();
            fileName = fileName.replaceAll("\\.", "/");
            args[index] = fileName;
            if (env.get(ToolConstants.CFG_OUTPUTDIR) != null) {
                args[index] = (String)env.get(ToolConstants.CFG_OUTPUTDIR) + "/" + fileName + ".java";
            }

            index++;
        }
        Compiler compiler = new Compiler(System.out);

        if (!compiler.internalCompile(args)) {
            throw new ToolException("Compile generated code failed");
        }

    }

}
