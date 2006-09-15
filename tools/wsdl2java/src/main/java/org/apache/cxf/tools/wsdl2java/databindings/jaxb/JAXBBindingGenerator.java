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

package org.apache.cxf.tools.wsdl2java.databindings.jaxb;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.wsdl.Definition;
import javax.wsdl.Part;
import javax.wsdl.PortType;
import javax.wsdl.extensions.schema.Schema;
import javax.xml.namespace.QName;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.xml.sax.InputSource;

import com.sun.codemodel.JCodeModel;
import com.sun.tools.xjc.api.Property;
import com.sun.tools.xjc.api.S2JJAXBModel;
import com.sun.tools.xjc.api.TypeAndAnnotation;
import com.sun.tools.xjc.api.XJC;
import com.sun.tools.xjc.api.impl.s2j.SchemaCompilerImpl;

import org.apache.cxf.common.i18n.Message;
import org.apache.cxf.common.logging.LogUtils;
import org.apache.cxf.common.util.StringUtils;
import org.apache.cxf.tools.common.DataBindingGenerator;
import org.apache.cxf.tools.common.ToolConstants;
import org.apache.cxf.tools.common.ToolContext;
import org.apache.cxf.tools.common.ToolException;
import org.apache.cxf.tools.util.ClassCollector;
import org.apache.cxf.tools.util.JAXBUtils;
import org.apache.cxf.tools.util.ProcessorUtil;
import org.apache.cxf.tools.util.URIParserUtil;
import org.apache.cxf.tools.wsdl2java.processor.internal.ClassNameAllocatorImpl;
public class JAXBBindingGenerator implements DataBindingGenerator {
    private static final Logger LOG = LogUtils.getL7dLogger(JAXBBindingGenerator.class);
    protected S2JJAXBModel rawJaxbModel;
    protected S2JJAXBModel rawJaxbModelGenCode;
    private boolean nestedJaxbBinding;
    //private Model model;
    private ToolContext env;
    private int fileIDX;

    @SuppressWarnings("unchecked")
    public void initialize(ToolContext penv) throws ToolException {
        env = penv;

        SchemaCompilerImpl schemaCompiler = (SchemaCompilerImpl)XJC.createSchemaCompiler();
        ClassCollector classCollector = (ClassCollector)env.get(ToolConstants.GENERATED_CLASS_COLLECTOR);

        ClassNameAllocatorImpl allocator = new ClassNameAllocatorImpl(classCollector);

        Map<QName, PortType> portTypeMap = (Map<QName, PortType>)env.get(ToolConstants.PORTTYPE_MAP);
        Definition def = (Definition)env.get(ToolConstants.WSDL_DEFINITION);

        allocator.setPortTypes(portTypeMap.values(), env.mapPackageName(def.getTargetNamespace()));

        schemaCompiler.setClassNameAllocator(allocator);
        JAXBBindErrorListener listener = new JAXBBindErrorListener(env);
        schemaCompiler.setErrorListener(listener);
        

        SchemaCompilerImpl schemaCompilerGenCode = schemaCompiler;
        String excludePackageName = "";
        if (env.isExcludeNamespaceEnabled()) {
            schemaCompilerGenCode = (SchemaCompilerImpl)XJC.createSchemaCompiler();
            schemaCompilerGenCode.setClassNameAllocator(allocator);
            schemaCompilerGenCode.setErrorListener(listener);
        }
        List schemaSystemidList = new ArrayList();

        //Options opt = new OptionsEx();
        List<Schema> schemaList = env.getSchemaList();
        for (Schema schema : schemaList) {

            Element schemaElement = schema.getElement();
            String targetNamespace = schemaElement.getAttribute("targetNamespace");
            if (StringUtils.isEmpty(targetNamespace)) {
                continue;
            }

            if (env.hasExcludeNamespace(targetNamespace)) {
                excludePackageName = env.getExcludePackageName(targetNamespace);
                if (excludePackageName != null) {
                    env.getExcludePkgList().add(excludePackageName);
                } else {
                    env.getExcludePkgList().add(URIParserUtil.getPackageName(targetNamespace));
                }
            }
            customizeSchema(schemaElement, targetNamespace);
            String systemid = schema.getDocumentBaseURI();
            if (schemaSystemidList.contains(systemid)) {
                systemid = schema.getDocumentBaseURI() + "#" + targetNamespace;
            }
            schemaSystemidList.add(systemid);
           
            if (nestedJaxbBinding) {
                InputSource ins = processNestedBinding(schemaElement, systemid);
                schemaCompiler.parseSchema(ins);
                schemaCompilerGenCode.parseSchema(ins);
            } else {
                schemaCompiler.parseSchema(systemid, schemaElement);
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
        try {
            rawJaxbModel = schemaCompiler.bind();

        } catch (ToolException e) {
            throw e;
        }
        if (env.isExcludeNamespaceEnabled()) {
            rawJaxbModelGenCode = schemaCompilerGenCode.bind();
        } else {
            rawJaxbModelGenCode = rawJaxbModel;
        }
        
        /*if (nestedJaxbBinding) {
            opt.classNameAllocator = allocator;
            model = ModelLoader.load(opt, new JCodeModel(), new JAXBErrorReceiver(env));
            model.generateCode(opt, new JAXBErrorReceiver(env));
        }   */    
        addedEnumClassToCollector(schemaList, allocator);
    }
  
    
    //Jaxb's bug . Jaxb ClassNameCollecotr may not be invoked when generated class is an enum.
    //So we need use this method to add the missed file to classCollector
    
    private void addedEnumClassToCollector(List<Schema> schemaList, ClassNameAllocatorImpl allocator) {
        for (Schema schema : schemaList) {
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
        ClassCollector classCollector = (ClassCollector)env.get(ToolConstants.GENERATED_CLASS_COLLECTOR);
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

    private InputSource processNestedBinding(Element schemaElement, String systemid) {
        String xsdFile = "schema" + (fileIDX++);
        File file = null;
        try {
            file = File.createTempFile(xsdFile, ".xsd");
        } catch (IOException e) {
            e.printStackTrace();
        }

        StreamResult result = new StreamResult(file);
        
        DOMSource source = new DOMSource(schemaElement);

        try {
            TransformerFactory.newInstance().newTransformer().transform(source, result);
        } catch (TransformerConfigurationException e) {
            e.printStackTrace();
        } catch (TransformerException e) {
            e.printStackTrace();
        } catch (TransformerFactoryConfigurationError e) {
            e.printStackTrace();
        }

        InputSource insource = null;
       
        insource = new InputSource(result.getSystemId());
        
        return insource;
        
    }

    public void generate() throws ToolException {
        if (env.optionSet(ToolConstants.CFG_GEN_CLIENT) || env.optionSet(ToolConstants.CFG_GEN_SERVER)) {
            return;
        }
        if (rawJaxbModelGenCode == null) {
            return;
        }
        try {
            String dir = (String)env.get(ToolConstants.CFG_OUTPUTDIR);

            TypesCodeWriter fileCodeWriter = new TypesCodeWriter(new File(dir), env.getExcludePkgList());

            if (rawJaxbModelGenCode instanceof S2JJAXBModel) {
                S2JJAXBModel schem2JavaJaxbModel = (S2JJAXBModel)rawJaxbModelGenCode;
                
                JCodeModel jcodeModel = schem2JavaJaxbModel.generateCode(null, null);
                jcodeModel.build(fileCodeWriter);
                for (String str : fileCodeWriter.getExcludeFileList()) {
                    env.getExcludeFileList().add(str);
                }
            }

           /* if (rawJaxbModelGenCode instanceof S2JJAXBModel && nestedJaxbBinding) {
                model.codeModel.build(fileCodeWriter);
                for (String str : fileCodeWriter.getExcludeFileList()) {
                    env.getExcludeFileList().add(str);
                }
            }*/

            return;
        } catch (IOException e) {
            Message msg = new Message("FAIL_TO_GENERATE_TYPES", LOG); 
            throw new ToolException(msg);
        }
    }

    public String getType(QName qn, boolean fullName) {
        String type;
        if (rawJaxbModel == null) {
            return null;
        }
        com.sun.tools.xjc.api.Mapping mapping = rawJaxbModel.get(qn);
        if (mapping == null) {
            return null;
        }
        if (fullName) {
            type = mapping.getType().getTypeClass().fullName();
            if (type == null) {
                type = mapping.getType().getTypeClass().boxify().fullName();
            }
            return type;

        } else {
            type = mapping.getType().getTypeClass().name();
            if (type == null) {
                type = mapping.getType().getTypeClass().boxify().name();
            }
            return type;
        }

    }

    public String getJavaType(QName qn, boolean boxify) {
        String jtypeClass;
        if (rawJaxbModel == null) {
            return null;
        }
        TypeAndAnnotation jtype = rawJaxbModel.getJavaType(qn);
        if (jtype == null || jtype.getTypeClass() == null) {
            return null;
        } else {         
            if (boxify) {           
                jtypeClass = jtype.getTypeClass().boxify().fullName();
            } else {
                jtypeClass = jtype.getTypeClass().fullName();
            }
        }
        
        return jtypeClass;
    

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
        JAXBBindingMerger jaxbBindingMerger = new JAXBBindingMerger();
        jaxbBindingMerger.mergeJaxwsBinding(schema, env);

        if (jaxbBindingMerger.isMerged()) {
            nestedJaxbBinding = true;
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

    @SuppressWarnings("unchecked")
    private boolean isSchemaParsed(String targetNamespace) {
        List schemaNamespaces = (List)env.get(ToolConstants.SCHEMA_TARGET_NAMESPACES);
        if (!schemaNamespaces.contains(targetNamespace)) {
            schemaNamespaces.add(targetNamespace);
            return false;
        } else {
            return true;
        }
    }

    public List<? extends Property> getBlock(Part part) {
        if (part == null) {
            return new ArrayList<Property>();
        }

        // QName element = getMappedElementName(part, env);
        QName element = ProcessorUtil.getElementName(part);

        if (element != null && rawJaxbModel != null) {
            com.sun.tools.xjc.api.Mapping mapping = rawJaxbModel.get(element);
            if (mapping != null) {
                return mapping.getWrapperStyleDrilldown();
            } else {
                org.apache.cxf.common.i18n.Message msg = 
                    new org.apache.cxf.common.i18n.Message("ELEMENT_MISSING",
                                                           LOG,
                                                           new Object[] {element.toString(), 
                                                                         part.getName()});
                System.err.println(msg);
                // return new ArrayList<Property>();
            }
        }
        return new ArrayList<Property>();
    }

}
