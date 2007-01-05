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
package org.apache.cxf.tools.wsdlto.databinding.jaxb;


import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import javax.wsdl.Definition;
import javax.xml.namespace.QName;

import org.w3c.dom.Element;

import org.xml.sax.InputSource;

import com.sun.codemodel.JCodeModel;
import com.sun.tools.xjc.api.S2JJAXBModel;
import com.sun.tools.xjc.api.TypeAndAnnotation;
import com.sun.tools.xjc.api.XJC;
import com.sun.tools.xjc.api.impl.s2j.SchemaCompilerImpl;

import org.apache.cxf.common.i18n.Message;
import org.apache.cxf.common.logging.LogUtils;
import org.apache.cxf.common.util.StringUtils;
import org.apache.cxf.service.model.SchemaInfo;
import org.apache.cxf.service.model.ServiceInfo;
import org.apache.cxf.tools.common.ToolConstants;
import org.apache.cxf.tools.common.ToolContext;
import org.apache.cxf.tools.common.ToolException;
import org.apache.cxf.tools.util.ClassCollector;
import org.apache.cxf.tools.util.ProcessorUtil;
import org.apache.cxf.tools.wsdlto.core.DataBindingProfile;
public class JAXBDataBinding implements DataBindingProfile {
    private static final Logger LOG = LogUtils.getL7dLogger(JAXBDataBinding.class);
    protected S2JJAXBModel rawJaxbModelGenCode;
    //private Model model;
    private ToolContext env;
    private ServiceInfo serviceInfo;
    private Definition def;

    @SuppressWarnings("unchecked")
    public void initialize(ToolContext penv) throws ToolException {
        env = penv;
        serviceInfo = (ServiceInfo)env.get(ServiceInfo.class);
        def = (Definition)env.get(Definition.class);
        
        Set<InputSource> jaxbBindings = (Set<InputSource>)env.get(ToolConstants.NS_JAXB_BINDINGS);

                
        SchemaCompilerImpl schemaCompiler = (SchemaCompilerImpl)XJC.createSchemaCompiler();        
        
        ClassCollector classCollector = env.get(ClassCollector.class);
        ClassNameAllocatorImpl allocator = new ClassNameAllocatorImpl(classCollector);
        allocator.setInterface(serviceInfo.getInterface(), env.mapPackageName(def.getTargetNamespace()));
        schemaCompiler.setClassNameAllocator(allocator);
        
        JAXBBindErrorListener listener = new JAXBBindErrorListener(env);
        schemaCompiler.setErrorListener(listener);
        
        
        Collection<SchemaInfo> schemas = serviceInfo.getTypeInfo().getSchemas();
        
        for (SchemaInfo schema : schemas) {
            Element element = schema.getElement();
            String tns = element.getAttribute("targetNamespace");
            schemaCompiler.parseSchema(tns, element);
        }
                 
        for (InputSource binding : jaxbBindings) {
            schemaCompiler.parseSchema(binding);
        }
       
        rawJaxbModelGenCode = schemaCompiler.bind();
        addedEnumClassToCollector(schemas, allocator);
    }
  
    
    // JAXB bug. JAXB ClassNameCollector may not be invoked when generated
    // class is an enum.  We need to use this method to add the missed file
    // to classCollector.
    private void addedEnumClassToCollector(Collection<SchemaInfo> schemaList,
                                           ClassNameAllocatorImpl allocator) {
        for (SchemaInfo schema : schemaList) {
            Element schemaElement = schema.getElement();
            String targetNamespace = schemaElement.getAttribute("targetNamespace");
            if (StringUtils.isEmpty(targetNamespace)) {
                continue;
            }
            String packageName = ProcessorUtil.parsePackageName(targetNamespace, null);
            if (!addedToClassCollector(packageName)) {
                allocator.assignClassName(packageName, "*");
            }
        }
    }
    
    private boolean addedToClassCollector(String packageName) {
        ClassCollector classCollector = env.get(ClassCollector.class);
        List<String> files = (List<String>)classCollector.getGeneratedFileInfo();
        for (String file : files) {
            int dotIndex = file.lastIndexOf(".");
            String sub = file.substring(0, dotIndex - 1);
            if (sub.equals(packageName)) {
                return true;
            }
        }
        return false;        
    }

   
    
    public void generate() throws ToolException {
        if (rawJaxbModelGenCode == null) {
            return;
        }
        
        try {
            String dir = (String)env.get(ToolConstants.CFG_OUTPUTDIR);
             
            TypesCodeWriter fileCodeWriter = new TypesCodeWriter(new File(dir), env.getExcludePkgList());

            if (rawJaxbModelGenCode instanceof S2JJAXBModel) {
                S2JJAXBModel schem2JavaJaxbModel = (S2JJAXBModel)rawJaxbModelGenCode;
                //TO DO....
                // enable jaxb plugin
                JCodeModel jcodeModel = schem2JavaJaxbModel.generateCode(null, null);
                
                jcodeModel.build(fileCodeWriter);
                env.put(JCodeModel.class, jcodeModel);
                for (String str : fileCodeWriter.getExcludeFileList()) {
                    env.getExcludeFileList().add(str);
                }
            }
            return;
        } catch (IOException e) {
            Message msg = new Message("FAIL_TO_GENERATE_TYPES", LOG); 
            throw new ToolException(msg);
        }
    }
    
    public String getType(QName qname) {
        TypeAndAnnotation typeAnno = rawJaxbModelGenCode.getJavaType(qname);        
        if (typeAnno.getTypeClass() != null) {
            return typeAnno.getTypeClass().fullName();
        }
        return null;
        
    }
}
