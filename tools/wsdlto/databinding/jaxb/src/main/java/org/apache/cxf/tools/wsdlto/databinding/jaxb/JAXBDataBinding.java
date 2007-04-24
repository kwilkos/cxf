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
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import javax.xml.namespace.QName;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
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
import org.apache.cxf.helpers.DOMUtils;
import org.apache.cxf.helpers.FileUtils;
import org.apache.cxf.tools.common.ToolConstants;
import org.apache.cxf.tools.common.ToolContext;
import org.apache.cxf.tools.common.ToolException;
import org.apache.cxf.tools.util.ClassCollector;
import org.apache.cxf.tools.wsdlto.core.DataBindingProfile;

public class JAXBDataBinding implements DataBindingProfile {
    private static final Logger LOG = LogUtils.getL7dLogger(JAXBDataBinding.class);

    private S2JJAXBModel rawJaxbModelGenCode;
    private ToolContext context;

    @SuppressWarnings("unchecked")
    private void initialize(ToolContext c) throws ToolException {
        this.context = c;

        SchemaCompilerImpl schemaCompiler = (SchemaCompilerImpl)XJC.createSchemaCompiler();
        ClassCollector classCollector = context.get(ClassCollector.class);
        ClassNameAllocatorImpl allocator = new ClassNameAllocatorImpl(classCollector);

        schemaCompiler.setClassNameAllocator(allocator);

        JAXBBindErrorListener listener = new JAXBBindErrorListener(context);
        schemaCompiler.setErrorListener(listener);
        // Collection<SchemaInfo> schemas = serviceInfo.getSchemas();
        List<InputSource> jaxbBindings = context.getJaxbBindingFile();
        Map<String, Element> schemaLists = (Map<String, Element>)context.get(ToolConstants.SCHEMA_MAP);

        Set<String> keys = schemaLists.keySet();
        for (String key : keys) {
            Element ele = schemaLists.get(key);
            this.removeImportElement(ele);
            String tns = ele.getAttribute("targetNamespace");
            if (StringUtils.isEmpty(tns)) {
                continue;
            }

            schemaCompiler.parseSchema(key, ele);

        }

        for (InputSource binding : jaxbBindings) {
            schemaCompiler.parseSchema(binding);
        }

        if (context.getPackageName() != null) {
            schemaCompiler.forcePackageName(context.getPackageName());
        } else {
            Map<String, String> nsPkgMap = context.getNamespacePackageMap();
            for (String ns : nsPkgMap.keySet()) {
                File file = getCustomizedSchemaElement(ns, nsPkgMap.get(ns));
                InputSource ins = new InputSource(file.toURI().toString());
                schemaCompiler.parseSchema(ins);
                FileUtils.delete(file);
            }
        }

        rawJaxbModelGenCode = schemaCompiler.bind();

        addedEnumClassToCollector(schemaLists, allocator);
    }

    // JAXB bug. JAXB ClassNameCollector may not be invoked when generated
    // class is an enum. We need to use this method to add the missed file
    // to classCollector.
    private void addedEnumClassToCollector(Map<String, Element> schemaList, 
                                           ClassNameAllocatorImpl allocator) {
        for (Element schemaElement : schemaList.values()) {
            String targetNamespace = schemaElement.getAttribute("targetNamespace");
            if (StringUtils.isEmpty(targetNamespace)) {
                continue;
            }
            String packageName = context.mapPackageName(targetNamespace);
            if (!addedToClassCollector(packageName)) {
                allocator.assignClassName(packageName, "*");
            }
        }
    }

    private boolean addedToClassCollector(String packageName) {
        ClassCollector classCollector = context.get(ClassCollector.class);
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

    private boolean isSuppressCodeGen() {
        return context.optionSet(ToolConstants.CFG_SUPPRESS_GEN);
    }

    public void generate(ToolContext c) throws ToolException {
        initialize(c);
        if (rawJaxbModelGenCode == null) {
            return;
        }

        try {
            String dir = (String)context.get(ToolConstants.CFG_OUTPUTDIR);

            TypesCodeWriter fileCodeWriter = new TypesCodeWriter(new File(dir), context.getExcludePkgList());

            if (rawJaxbModelGenCode instanceof S2JJAXBModel) {
                S2JJAXBModel schem2JavaJaxbModel = (S2JJAXBModel)rawJaxbModelGenCode;
                // TODO : enable jaxb plugin
                JCodeModel jcodeModel = schem2JavaJaxbModel.generateCode(null, null);

                if (!isSuppressCodeGen()) {
                    jcodeModel.build(fileCodeWriter);
                }

                context.put(JCodeModel.class, jcodeModel);
                for (String str : fileCodeWriter.getExcludeFileList()) {
                    context.getExcludeFileList().add(str);
                }
            }
            return;
        } catch (IOException e) {
            Message msg = new Message("FAIL_TO_GENERATE_TYPES", LOG);
            throw new ToolException(msg);
        }
    }

    public String getType(QName qname, boolean element) {
        TypeAndAnnotation typeAnno = rawJaxbModelGenCode.getJavaType(qname);
        if (element) {
            Mapping mapping = rawJaxbModelGenCode.get(qname);
            if (mapping != null) {
                typeAnno = mapping.getType();
            }
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

    public Node cloneNode(Document document, Node node, boolean deep) throws DOMException {
        if (document == null || node == null) {
            return null;
        }
        int type = node.getNodeType();

        if (node.getOwnerDocument() == document) {
            return node.cloneNode(deep);
        }
        Node clone;
        switch (type) {
        case Node.CDATA_SECTION_NODE:
            clone = document.createCDATASection(node.getNodeValue());
            break;
        case Node.COMMENT_NODE:
            clone = document.createComment(node.getNodeValue());
            break;
        case Node.ENTITY_REFERENCE_NODE:
            clone = document.createEntityReference(node.getNodeName());
            break;
        case Node.ELEMENT_NODE:
            clone = document.createElement(node.getNodeName());
            NamedNodeMap attributes = node.getAttributes();
            for (int i = 0; i < attributes.getLength(); i++) {
                ((Element)clone).setAttribute(attributes.item(i).getNodeName(), attributes.item(i)
                    .getNodeValue());
            }
            break;

        case Node.TEXT_NODE:
            clone = document.createTextNode(node.getNodeValue());
            break;
        default:
            return null;
        }
        if (deep && type == Node.ELEMENT_NODE) {
            Node child = node.getFirstChild();
            while (child != null) {
                clone.appendChild(cloneNode(document, child, true));
                child = child.getNextSibling();
            }
        }
        return clone;
    }

    /**
     * Create the jaxb binding file to customize namespace to package mapping
     * 
     * @param namespace
     * @param pkgName
     * @return file
     */
    public File getCustomizedSchemaElement(String namespace, String pkgName) {
        Document doc = DOMUtils.createDocument();
        Element rootElement = doc.createElement("schema");
        rootElement.setAttribute("xmlns", ToolConstants.SCHEMA_URI);
        rootElement.setAttribute("xmlns:jaxb", ToolConstants.NS_JAXB_BINDINGS);
        rootElement.setAttribute("jaxb:version", "1.0");
        rootElement.setAttribute("targetNamespace", namespace);
        Element annoElement = doc.createElement("annotation");
        Element appInfo = doc.createElement("appinfo");
        Element schemaBindings = doc.createElement("jaxb:schemaBindings");
        Element pkgElement = doc.createElement("jaxb:package");
        pkgElement.setAttribute("name", pkgName);
        annoElement.appendChild(appInfo);
        appInfo.appendChild(schemaBindings);
        schemaBindings.appendChild(pkgElement);
        rootElement.appendChild(annoElement);
        File tmpFile = null;
        try {
            tmpFile = File.createTempFile("customzied", ".xsd");
            FileOutputStream fout = new FileOutputStream(tmpFile);
            DOMUtils.writeXml(rootElement, fout);
            fout.close();
        } catch (Exception e) {
            Message msg = new Message("FAIL_TO_CREATE_JAXBBINIDNG_FILE", LOG);
            throw new ToolException(msg, e);
        }
        return tmpFile;
    }

}
