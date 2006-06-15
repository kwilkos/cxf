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

import org.objectweb.celtix.common.i18n.Message;
import org.objectweb.celtix.tools.common.ToolConstants;
import org.objectweb.celtix.tools.common.ToolException;
import org.objectweb.celtix.tools.common.model.JavaModel;
import org.objectweb.celtix.tools.extensions.jaxws.CustomizationParser;
import org.objectweb.celtix.tools.extensions.jaxws.JAXWSBinding;
import org.objectweb.celtix.tools.generators.wsdl2.AntGenerator;
import org.objectweb.celtix.tools.generators.wsdl2.ClientGenerator;
import org.objectweb.celtix.tools.generators.wsdl2.FaultGenerator;
import org.objectweb.celtix.tools.generators.wsdl2.ImplGenerator;
import org.objectweb.celtix.tools.generators.wsdl2.SEIGenerator;
import org.objectweb.celtix.tools.generators.wsdl2.ServerGenerator;
import org.objectweb.celtix.tools.generators.wsdl2.ServiceGenerator;
import org.objectweb.celtix.tools.processors.wsdl2.compiler.Compiler;
import org.objectweb.celtix.tools.processors.wsdl2.internal.ClassCollector;
import org.objectweb.celtix.tools.processors.wsdl2.internal.PortTypeProcessor;
import org.objectweb.celtix.tools.processors.wsdl2.internal.SEIAnnotationProcessor;
import org.objectweb.celtix.tools.processors.wsdl2.internal.ServiceProcessor;

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

    public void process() throws ToolException {
        validateWSDL();
        init();
        if (isSOAP12Binding(wsdlDefinition)) {
            Message msg = new Message("SOAP12_UNSUPPORTED", LOG);
            throw new ToolException(msg);
        }
        generateTypes();
        JavaModel jmodel = wsdlDefinitionToJavaModel(getWSDLDefinition());
        if (jmodel == null) {
            Message msg = new Message("FAIL_TO_CREATE_JAVA_MODEL", LOG);
            throw new ToolException(msg);
        }
        registerGenerators(jmodel);
        doGeneration();
        if (env.get(ToolConstants.CFG_COMPILE) != null) {
            compile();
        }
    }

    private void generateTypes() throws ToolException {
        if (rawJaxbModelGenCode == null) {
            return;
        }
        try {
            if (rawJaxbModelGenCode instanceof S2JJAXBModel) {
                S2JJAXBModel schem2JavaJaxbModel = (S2JJAXBModel)rawJaxbModelGenCode;
                JCodeModel jcodeModel = schem2JavaJaxbModel.generateCode(null, null);
                String dir = (String)env.get(ToolConstants.CFG_OUTPUTDIR);
                FileCodeWriter fileCodeWriter = new FileCodeWriter(new File(dir));
                jcodeModel.build(fileCodeWriter);
            } else {
                return;
            }
        } catch (IOException e) {
            Message msg = new Message("FAIL_TO_GENERATE_TYPES", LOG);
            throw new ToolException(msg);

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
        seiAnnotationProcessor.process(javaModel, definition);

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
        List<String> fileList = new ArrayList<String>();

        String javaClasspath = System.getProperty("java.class.path");
        // hard code celtix.jar
        boolean classpathSetted = javaClasspath != null ? true : false;
        // && (javaClasspath.indexOf("celtix.jar") >= 0);
        if (env.isVerbose()) {
            argList.add("-verbose");
        }

        if (env.get(ToolConstants.CFG_CLASSDIR) != null) {
            argList.add("-d");
            argList.add(((String)env.get(ToolConstants.CFG_CLASSDIR)).replace(File.pathSeparatorChar, '/'));
        }

        if (!classpathSetted) {
            argList.add("-extdirs");
            argList.add(getClass().getClassLoader().getResource(".").getFile() + "../lib/");
        } else {
            argList.add("-classpath");
            argList.add(javaClasspath);
        }

        String outPutDir = (String)env.get(ToolConstants.CFG_OUTPUTDIR);

        Set<String> dirSet = new HashSet<String>();
        Iterator ite = classCollector.getGeneratedFileInfo().iterator();
        while (ite.hasNext()) {
            String fileName = (String)ite.next();
            fileName = fileName.replace('.', File.separatorChar);
            String dirName = fileName.substring(0, fileName.lastIndexOf(File.separator) + 1);
            String path = outPutDir + File.separator + dirName;
            if (!dirSet.contains(path)) {

                dirSet.add(path);
                File file = new File(path);
                if (file.isDirectory()) {
                    for (String str : file.list()) {
                        if (str.endsWith("java")) {
                            fileList.add(path + str);
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

        String[] arguments = new String[argList.size() + fileList.size() + 1];
        arguments[0] = "javac";
        int i = 1;
        for (Object obj : argList.toArray()) {
            String arg = (String)obj;
            arguments[i] = arg;
            i++;
        }

        for (Object o : fileList.toArray()) {
            String file = (String)o;
           
            arguments[i] = file;
            i++;
        }

        Compiler compiler = new Compiler();

        if (!compiler.internalCompile(arguments)) {
            Message msg = new Message("FAIL_TO_COMPILE_GENERATE_CODES", LOG);
            throw new ToolException(msg);
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
            Message msg = new Message("FAIL_TO_COPY_GENERATED_RESOURCE_FILE", LOG);
            throw new ToolException(msg, e);
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

}
