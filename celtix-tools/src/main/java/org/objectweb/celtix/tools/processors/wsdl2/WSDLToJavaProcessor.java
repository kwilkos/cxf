package org.objectweb.celtix.tools.processors.wsdl2;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
        List<String> argList = new ArrayList<String>();

        String javaClasspath = System.getProperty("java.class.path");
        // hard code celtix.jar
        boolean classpathSetted = javaClasspath != null && (javaClasspath.indexOf("celtix.jar") >= 0);
        if (env.isVerbose()) {
            argList.add("-verbose");
        }

        if (!classpathSetted) {
            System.setProperty("java.ext.dirs", this.getClass().getClassLoader().getResource(".").getFile()
                                                + "../lib/");
        } else {
            argList.add("-classpath");
            argList.add(javaClasspath);
        }

        if (env.get(ToolConstants.CFG_CLASSDIR) != null) {
            argList.add("-d");
            argList.add((String)env.get(ToolConstants.CFG_CLASSDIR));
        }

        Set<String> dirSet = new HashSet<String>();
        Iterator ite = classCollector.getGeneratedFileInfo().iterator();
        while (ite.hasNext()) {
            String fileName = (String)ite.next();
            fileName = fileName.replaceAll("\\.", "/");
            String dirName = fileName.substring(0, fileName.lastIndexOf("/") + 1);
            String outPutDir = (String)env.get(ToolConstants.CFG_OUTPUTDIR);
            if (!dirSet.contains(dirName)) {
                String path = outPutDir + "/" + dirName;
                dirSet.add(path);
                File file = new File(path);
                if (file.isDirectory()) {
                    for (String str : file.list()) {
                        if (str.endsWith("java")) {
                            argList.add(path + File.separator + str);
                        } else {
                            // copy generated xml file or others to class
                            // directory
                            File otherFile = new File(path + File.separator + str);
                            if (otherFile.isFile() && str.toLowerCase().endsWith("xml")
                                && env.get(ToolConstants.CFG_CLASSDIR) != null) {
                                String targetDir = (String)env.get(ToolConstants.CFG_CLASSDIR);

                                File targetFile = new File(targetDir + File.separator + dirName
                                                           + File.separator + str);
                                copyXmlFile(otherFile, targetFile);
                            }
                        }
                    }
                }
            }

        }

        String[] args = new String[argList.size()];
        int i = 0;
        for (Object obj : argList.toArray()) {
            String arg = (String)obj;
            args[i] = arg;
            i++;
        }

        Compiler compiler = new Compiler(System.out);

        if (!compiler.internalCompile(args)) {
            throw new ToolException("Compile generated code failed");
        }

    }

    private void copyXmlFile(File from, File to) throws ToolException {

        try {
            String dir = to.getCanonicalPath()
                .substring(0, to.getCanonicalPath().lastIndexOf(File.separator));
            File dirFile = new File(dir);
            dirFile.mkdirs();
            FileInputStream input = new FileInputStream(from);
            FileOutputStream output = new FileOutputStream(to);
            byte[] b = new byte[1024 * 3];
            int len = 0;
            while (len != -1) {
                len = input.read(b);
                if (len != -1) {
                    output.write(b, 0, len);
                }
            }
            output.flush();
            output.close();
            input.close();
        } catch (Exception e) {
            throw new ToolException("Copy generated file error", e);
        }
    }
}
