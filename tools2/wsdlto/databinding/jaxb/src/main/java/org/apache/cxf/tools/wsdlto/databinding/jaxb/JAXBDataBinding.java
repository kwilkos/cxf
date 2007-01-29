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
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.wsdl.Definition;
import javax.xml.namespace.QName;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.xml.sax.InputSource;

import com.sun.codemodel.JCodeModel;
import com.sun.tools.xjc.api.Mapping;
import com.sun.tools.xjc.api.Property;
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
import org.apache.cxf.tools.util.URIParserUtil;
import org.apache.cxf.tools.wsdlto.core.DataBindingProfile;

public class JAXBDataBinding implements DataBindingProfile {
    private static final Logger LOG = LogUtils.getL7dLogger(JAXBDataBinding.class);
    private static S2JJAXBModel rawJaxbModelGenCode;
    private ToolContext env;
    private ServiceInfo serviceInfo;
    private Definition def;
    private Map<String, Element> sysIdSchemeMap = new HashMap<String, Element>();

    @SuppressWarnings("unchecked")
    private void initialize(ToolContext penv) throws ToolException {
        env = penv;
        serviceInfo = env.get(ServiceInfo.class);
        def = (Definition)env.get(Definition.class);

        SchemaCompilerImpl schemaCompiler = (SchemaCompilerImpl)XJC.createSchemaCompiler();
        if (env.getPackageName() != null) {
            schemaCompiler.setDefaultPackageName(env.getPackageName());
        }
        ClassCollector classCollector = env.get(ClassCollector.class);
        ClassNameAllocatorImpl allocator = new ClassNameAllocatorImpl(classCollector);
        allocator.setInterface(serviceInfo.getInterface(), env.mapPackageName(def.getTargetNamespace()));
        schemaCompiler.setClassNameAllocator(allocator);

        JAXBBindErrorListener listener = new JAXBBindErrorListener(env);
        schemaCompiler.setErrorListener(listener);

        Collection<SchemaInfo> schemas = serviceInfo.getSchemas();

        Collection<InputSource> jaxbBindings = env.getJaxbBindingFile().values();

        for (SchemaInfo schema : schemas) {
            Document[] docs = schema.getSchema().getAllSchemas();
            for (int i = 0; i < docs.length; i++) {
                Element ele = docs[i].getDocumentElement();
                this.removeImportElement(ele);
      
                String systemId = schema.getElement().getBaseURI();
                if (systemId == null) {
                    systemId = def.getDocumentBaseURI();
                }
                String tns = ele.getAttribute("targetNamespace");              
                if (StringUtils.isEmpty(tns)) {
                    continue;
                }
                  
                if (sysIdSchemeMap.containsKey(schema.getElement().getBaseURI())) {
                    systemId = schema.getElement().getBaseURI() + "#" + tns;
                    int index = 0;
                    while (sysIdSchemeMap.containsKey(systemId)) {
                        systemId = systemId + index++;
                    }
                    
                }
                
                sysIdSchemeMap.put(systemId, ele);
                schemaCompiler.parseSchema(systemId, ele);
            }
            
        }

        for (InputSource binding : jaxbBindings) {
            schemaCompiler.parseSchema(binding);
        }

        rawJaxbModelGenCode = schemaCompiler.bind();
        addedEnumClassToCollector(schemas, allocator);
    }

    // JAXB bug. JAXB ClassNameCollector may not be invoked when generated
    // class is an enum. We need to use this method to add the missed file
    // to classCollector.
    private void addedEnumClassToCollector(Collection<SchemaInfo> schemaList, 
                                           ClassNameAllocatorImpl allocator) {
        for (SchemaInfo schema : schemaList) {
            Element schemaElement = schema.getElement();
            String targetNamespace = schemaElement.getAttribute("targetNamespace");
            if (StringUtils.isEmpty(targetNamespace)) {
                continue;
            }
            String packageName = URIParserUtil.parsePackageName(targetNamespace, null);
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

    public void generate(ToolContext context) throws ToolException {
        initialize(context);
        if (rawJaxbModelGenCode == null) {
            return;
        }

        try {
            String dir = (String)env.get(ToolConstants.CFG_OUTPUTDIR);

            TypesCodeWriter fileCodeWriter = new TypesCodeWriter(new File(dir), env.getExcludePkgList());

            if (rawJaxbModelGenCode instanceof S2JJAXBModel) {
                S2JJAXBModel schem2JavaJaxbModel = (S2JJAXBModel)rawJaxbModelGenCode;
                // TODO : enable jaxb plugin
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
        Mapping mapping = rawJaxbModelGenCode.get(qname);

        TypeAndAnnotation typeAnno = null;

        if (mapping != null) {
            typeAnno = mapping.getType();
        } else {
            typeAnno = rawJaxbModelGenCode.getJavaType(qname);
        }

        if (typeAnno != null && typeAnno.getTypeClass() != null) {
            return typeAnno.getTypeClass().fullName();
        }
        return null;

    }

    public String getWrappedElementType(QName wrapperElement, QName item) {
        Mapping mapping = rawJaxbModelGenCode.get(wrapperElement);
        if (mapping != null) {
            List<? extends Property> propList = mapping.getWrapperStyleDrilldown();
            for (Property pro : propList) {
                if (pro.elementName().getNamespaceURI().equals(item.getNamespaceURI())
                    && pro.elementName().getLocalPart().equals(item.getLocalPart())) {
                    return pro.type().fullName();
                }
            }
        }
        return null;
    }   
    
    
    private void removeImportElement(Element element) {
        NodeList nodeList = element.getElementsByTagNameNS(ToolConstants.SCHEMA_URI, "import");
        List<Node> ns = new ArrayList<Node>();
        for (int tmp = 0; tmp < nodeList.getLength(); tmp++) {

            Node importNode = nodeList.item(tmp);
            ns.add(importNode);
        }
        for (Node item : ns) {
            Node schemaNode = item.getParentNode();
            schemaNode.removeChild(item);
        }
      
    }
    
}
