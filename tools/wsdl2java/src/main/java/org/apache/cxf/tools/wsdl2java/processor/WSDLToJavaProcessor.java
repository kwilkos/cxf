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
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.wsdl.Definition;
import javax.wsdl.PortType;
import javax.xml.namespace.QName;

import org.apache.cxf.common.i18n.Message;
import org.apache.cxf.tools.common.ClassUtils;
import org.apache.cxf.tools.common.FrontEndGeneratorsProfile;
import org.apache.cxf.tools.common.ToolConstants;
import org.apache.cxf.tools.common.ToolException;
import org.apache.cxf.tools.common.extensions.jaxws.CustomizationParser;
import org.apache.cxf.tools.common.extensions.jaxws.JAXWSBinding;
import org.apache.cxf.tools.common.model.JavaModel;
import org.apache.cxf.tools.wsdl2java.frontend.jaxws.JAXWSProfile;
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
            new ClassUtils().compile(env);
        }
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
}
