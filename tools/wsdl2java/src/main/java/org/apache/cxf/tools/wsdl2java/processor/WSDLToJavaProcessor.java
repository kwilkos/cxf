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
import javax.xml.namespace.QName;

import org.apache.cxf.common.i18n.Message;
import org.apache.cxf.tools.common.FrontEndGeneratorsProfile;
import org.apache.cxf.tools.common.ToolConstants;
import org.apache.cxf.tools.common.ToolException;
import org.apache.cxf.tools.common.extensions.jaxws.CustomizationParser;
import org.apache.cxf.tools.common.extensions.jaxws.JAXWSBinding;
import org.apache.cxf.tools.common.model.JavaModel;
import org.apache.cxf.tools.util.ClassCollector;
import org.apache.cxf.tools.wsdl2java.frontend.jaxws.JAXWSProfile;
import org.apache.cxf.tools.wsdl2java.processor.compiler.Compiler;
import org.apache.cxf.tools.wsdl2java.processor.internal.PortTypeProcessor;
import org.apache.cxf.tools.wsdl2java.processor.internal.SEIAnnotationProcessor;
import org.apache.cxf.tools.wsdl2java.processor.internal.ServiceProcessor;

public class WSDLToJavaProcessor extends WSDLToProcessor {

    public void process() throws ToolException {
        init();

        //Generate type classes for specified databinding
        generateTypes();

        //Generate client, server and service stub codeS for specified profile
        registerGenerators();
        doGeneration();

        if (env.get(ToolConstants.CFG_COMPILE) != null) {
            compile();
        }
        try {
            if (env.isExcludeNamespaceEnabled()) {
                removeExcludeFiles();
            }
        } catch (IOException e) {
            throw new ToolException(e);
        }
    }

    public void processImportDefinition(Definition def) throws ToolException {
        checkSupported(def);
        validateWSDL(def);
        parseCustomization(def);
        env.put(ToolConstants.GENERATED_CLASS_COLLECTOR, classColletor);

        //Generate client, server and service stub code for specified profile
        registerGenerators();
        doGeneration();

        try {
            if (env.isExcludeNamespaceEnabled()) {
                removeExcludeFiles();
            }
        } catch (IOException e) {
            throw new ToolException(e);
        }
    }

    public void removeExcludeFiles() throws IOException {
        excludeGenFiles = env.getExcludeFileList();
        if (excludeGenFiles == null) {
            return;
        }
        String outPutDir = (String)env.get(ToolConstants.CFG_OUTPUTDIR);
        for (int i = 0; i < excludeGenFiles.size(); i++) {
            String excludeFile = excludeGenFiles.get(i);
            File file = new File(outPutDir, excludeFile);
            file.delete();
            File tmpFile = file.getParentFile();
            while (tmpFile != null && !tmpFile.getCanonicalPath().equalsIgnoreCase(outPutDir)) {
                if (tmpFile.isDirectory() && tmpFile.list().length == 0) {
                    tmpFile.delete();
                }
                tmpFile = tmpFile.getParentFile();
            }

            if (env.get(ToolConstants.CFG_COMPILE) != null) {
                String classDir = env.get(ToolConstants.CFG_CLASSDIR) == null ? outPutDir : (String)env
                    .get(ToolConstants.CFG_CLASSDIR);
                File classFile = new File(classDir, excludeFile.substring(0, excludeFile.indexOf(".java"))
                                                    + ".class");
                classFile.delete();
                File tmpClzFile = classFile.getParentFile();
                while (tmpClzFile != null && !tmpClzFile.getCanonicalPath().equalsIgnoreCase(outPutDir)) {
                    if (tmpClzFile.isDirectory() && tmpClzFile.list().length == 0) {
                        tmpClzFile.delete();
                    }
                    tmpClzFile = tmpClzFile.getParentFile();
                }
            }

        }
    }

    private void registerGenerators() {
        //FIXME: Get profile name from input parameters
        String profile = null;
        if (profile == null) {
            profile = JAXWSProfile.class.getName();
        }

        FrontEndGeneratorsProfile profileObj = null;
        try {
            profileObj = (FrontEndGeneratorsProfile) Class.forName(profile).newInstance();
        } catch (Exception e) {
            Message msg = new Message("FAIl_TO_CREATE_PLUGINPROFILE", LOG);
            throw new ToolException(msg);
        }

        //Setup JavaModel for generators.
        JavaModel jmodel = wsdlDefinitionToJavaModel(getWSDLDefinition());
        if (jmodel == null) {
            Message msg = new Message("FAIL_TO_CREATE_JAVA_MODEL", LOG);
            throw new ToolException(msg);
        }
        getEnvironment().setJavaModel(jmodel);

        generators =  profileObj.getPlugins();
    }
   
    public boolean passthrough() {
        if (env.optionSet(ToolConstants.CFG_GEN_TYPES)
            || env.optionSet(ToolConstants.CFG_ALL)) {
            return false;
        } 
        if (env.optionSet(ToolConstants.CFG_GEN_ANT)
            || env.optionSet(ToolConstants.CFG_GEN_CLIENT)
            || env.optionSet(ToolConstants.CFG_GEN_IMPL)
            || env.optionSet(ToolConstants.CFG_GEN_SEI)
            || env.optionSet(ToolConstants.CFG_GEN_SERVER)
            || env.optionSet(ToolConstants.CFG_GEN_SERVICE)) {
            return true;
        }
        
        return false;
    }
    
    private void generateTypes() throws ToolException {
        if (passthrough()) {
            return;
        }
        if (bindingGenerator == null) {
            return;
        }
        
        bindingGenerator.generate();
       
        

    }

    private JavaModel wsdlDefinitionToJavaModel(Definition definition) throws ToolException {

        JavaModel javaModel = new JavaModel();
       /* getEnvironment().put(ToolConstants.RAW_JAXB_MODEL, getRawJaxbModel());*/

        javaModel.setJAXWSBinding(customizing(definition));

        Map<QName, PortType> portTypes = getPortTypes(definition);

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
        // hard code cxf.jar
        boolean classpathSetted = javaClasspath != null ? true : false;
        // && (javaClasspath.indexOf("cxf.jar") >= 0);
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
        //Jaxb's bug . Jaxb ClassNameCollecotr may not be invoked when generated class is an enum.
        //So we need recheck whether we add all generated source files to  fileList
        
       
        

        String[] arguments = new String[argList.size() + fileList.size() + 1];
        arguments[0] = "javac";
        
        int i = 1;
        
        for (Object obj : argList.toArray()) {
            String arg = (String)obj;
            arguments[i] = arg;
            i++;
        }
        
        int srcFileIndex = i; 
        
        for (Object o : fileList.toArray()) {
            String file = (String)o;
            arguments[i] = file;
            i++;
        }

        Compiler compiler = new Compiler();

        if (!compiler.internalCompile(arguments, srcFileIndex)) {
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
}
