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

package org.apache.cxf.tools.util;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import javax.wsdl.Message;
import javax.wsdl.Operation;
import javax.wsdl.Part;
import javax.xml.namespace.QName;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import com.sun.xml.bind.api.JAXBRIContext;

import org.apache.cxf.helpers.JavaUtils;
import org.apache.cxf.jaxb.JAXBUtils;
import org.apache.cxf.tools.common.DataBindingGenerator;
import org.apache.cxf.tools.common.ToolConstants;
import org.apache.cxf.tools.common.ToolContext;
import org.apache.cxf.tools.common.ToolException;

public final class ProcessorUtil {
    //private static final Logger LOG = LogUtils.getL7dLogger(ProcessorUtil.class);
    private static final String KEYWORDS_PREFIX = "_";
    
    private ProcessorUtil() {
    }

    public static String resolvePartName(Part part) {
        return mangleNameToVariableName(part.getName());
    }

    public static String getPartType(Part part) {
        String typeName;
        if (part.getElementName() != null) {
            typeName = part.getElementName().getLocalPart();
        } else if (part.getTypeName() != null) {
            typeName = part.getTypeName().getLocalPart();
        } else {
            typeName = "BadType";
        }
        return typeName;
    }

    public static String resolvePartType(Part part) {
        return mangleNameToClassName(getPartType(part));
    }
    
    public static String getType(Part part, ToolContext env, boolean fullname) {
        DataBindingGenerator dataBinder = (DataBindingGenerator)env.get(ToolConstants.BINDING_GENERATOR);
        String type = dataBinder.getType(getElementName(part), fullname);
        if (type == null) {
            type = resolvePartType(part);
        }
        return type;
    }
    
    public static QName getElementName(Part part) {
        if (part == null) {
            return null;
        }
        QName elementName = part.getElementName();
        if (elementName == null) {
            elementName = part.getTypeName();
        }
        return elementName;
    }

    //
    // support multiple -p options
    // if user change the package name through -p namespace=package name
    //
    public static QName getMappedElementName(Part part, ToolContext env) {
        QName origin = getElementName(part);
        if (origin == null) {
            return null;
        }
        if (!env.hasNamespace(origin.getNamespaceURI())) {
            return origin;
        }
        return new QName(env.getCustomizedNS(origin.getNamespaceURI()), origin.getLocalPart());
    }

    public static String resolvePartType(Part part, ToolContext env) {
        if (env != null) {
            return resolvePartType(part, env, false);
        } else {
            return resolvePartType(part);
        }
    }

   /* public static String resolvePartType(Part part, ProcessorEnvironment env) {
        return resolvePartType(part, env, false);
    }*/

    public static String resolvePartType(Part part, ToolContext env, boolean fullName) {
        DataBindingGenerator binder = (DataBindingGenerator)env.get(ToolConstants.BINDING_GENERATOR);
        if (binder == null) {
            String primitiveType = JAXBUtils.builtInTypeToJavaType(part.getTypeName().getLocalPart());
            if (part.getTypeName() != null &&  primitiveType != null) {
                return primitiveType;
            } else {
                return resolvePartType(part);
            }
        }
        String name = binder.getType(getElementName(part), fullName);
        if (name == null) {
            return resolvePartType(part);
        }
        return name;
       
    }

    public static String resolvePartNamespace(Part part) {
        QName qname = part.getElementName();
        if (qname == null) {
            qname = part.getTypeName();
        }
        if (qname != null) {
            return qname.getNamespaceURI();
        } else {
            return null;
        }
    }

    public static String mangleNameToClassName(String clzName) {
        return JAXBRIContext.mangleNameToClassName(clzName);
    }

    public static String mangleNameToVariableName(String vName) {
        String result  = JAXBRIContext.mangleNameToVariableName(vName);
        if (JavaUtils.isJavaKeyword(result)) {
            return KEYWORDS_PREFIX + result;
        } else {
            return result;
        }
    }

    public static String parsePackageName(String namespace, String defaultPackageName) {
        String packageName = (defaultPackageName != null && defaultPackageName.trim().length() > 0)
            ? defaultPackageName : null;

        if (packageName == null) {
            packageName = URIParserUtil.getPackageName(namespace);
        }
        return packageName;
    }

    public static String getAbsolutePath(String location) throws IOException {
        if (location.startsWith("http://")) {
            return location;
        } else {
            return resolvePath(new File(location).getCanonicalPath());
        }
    }

    public static URL getWSDLURL(String location) throws Exception {
        if (location.startsWith("http://")) {
            return new URL(location);
        } else {
            return new File(getAbsolutePath(location)).toURL();
        }
    }

    private static String resolvePath(String path) {
        return path.replace('\\', '/');
    }

    public static URL[] pathToURLs(String path) {
        StringTokenizer st = new StringTokenizer(path, File.pathSeparator);
        URL[] urls = new URL[st.countTokens()];
        int count = 0;
        while (st.hasMoreTokens()) {
            File file = new File(st.nextToken());
            URL url = null;
            try {
                url = file.toURL();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            if (url != null) {
                urls[count++] = url;
            }
        }
        if (urls.length != count) {
            URL[] tmp = new URL[count];
            System.arraycopy(urls, 0, tmp, 0, count);
            urls = tmp;
        }
        return urls;
    }

    public static String classNameToFilePath(String className) {
        String str;
        if (className.indexOf(".") < 0) {
            return className;
        } else {
            str = className.replaceAll("\\.", "/");
        }
        return str;
    }

    //
    // the non-wrapper style will get the type info from the part directly
    //
    public static String getFullClzName(Part part, ToolContext env,
                                        ClassCollector collector, boolean boxify) {
        DataBindingGenerator dataBindingGenerator = (DataBindingGenerator)env
            .get(ToolConstants.BINDING_GENERATOR);
        String jtype = null;
        QName xmlTypeName = getElementName(part);
        if (xmlTypeName == null) {
            xmlTypeName = part.getTypeName();
           
        }

        // if this flag  is true , mapping to java Type first;
        // if not found , findd the primitive type : int ,long 
        // if not found,  find in the generated class
       
            
        if (boxify && dataBindingGenerator != null) {
            jtype = dataBindingGenerator.getJavaType(xmlTypeName, true);
        } 
        
        if (boxify && dataBindingGenerator == null) {
            Class holderClass = JAXBUtils.holderClass(xmlTypeName.getLocalPart());
            jtype = holderClass == null ? null : holderClass.getName();
            if (jtype == null) {
                jtype = JAXBUtils.builtInTypeToJavaType(xmlTypeName.getLocalPart());
            }
                       
        }
        
        if (!boxify && dataBindingGenerator != null) {
            jtype = dataBindingGenerator.getJavaType(xmlTypeName, false);
        }
        
        if (!boxify && dataBindingGenerator == null) {
            jtype = JAXBUtils.builtInTypeToJavaType(xmlTypeName.getLocalPart());
        }
            
        
        String namespace = xmlTypeName.getNamespaceURI();
        String type = resolvePartType(part, env, true);
        String userPackage = env.mapPackageName(namespace);

        if (jtype == null) {
            jtype = collector.getTypesFullClassName(parsePackageName(namespace, userPackage), type);        
        }

        if (jtype == null) {
            if (!type.equals(resolvePartType(part))) {
                jtype = resolvePartType(part, env, true);
            } else {
                jtype = parsePackageName(namespace, userPackage) + "." + type;
            }          
        } 
        
        
        return jtype;
    }

   

    public static String getFileOrURLName(String fileOrURL) {
        try {
            try {
                return escapeSpace(new URL(fileOrURL).toExternalForm());
            } catch (MalformedURLException e) {
                return new File(fileOrURL).getCanonicalFile().toURL().toExternalForm();
            }
        } catch (Exception e) {
            return fileOrURL;
        }
    }

    private static String escapeSpace(String url) {
        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < url.length(); i++) {
            if (url.charAt(i) == ' ') {
                buf.append("%20");
            } else {
                buf.append(url.charAt(i));
            }
        }
        return buf.toString();
    }

    public static String absolutize(String name) {
        // absolutize all the system IDs in the input,
        // so that we can map system IDs to DOM trees.
        try {
            URL baseURL = new File(".").getCanonicalFile().toURL();
            return new URL(baseURL, name.replaceAll(" ", "%20")).toExternalForm();
        } catch (IOException e) {
            // ignore
        }
        return name;
    }

    public static String getHandlerConfigFileName(String name) {
        return name + "_handler";
    }

   

    @SuppressWarnings("unchecked")
    public static boolean isWrapperStyle(Operation operation, ToolContext env) throws ToolException {

        Message inputMessage = operation.getInput() == null ? null : operation.getInput().getMessage();
        Message outputMessage = operation.getOutput() == null ? null : operation.getOutput().getMessage();

        Map<String, Part> inputParts = new HashMap<String, Part>();
        Map<String, Part> outputParts = new HashMap<String, Part>();

        if (inputMessage != null) {
            inputParts = inputMessage.getParts();
        }
        if (outputMessage != null) {
            outputParts = outputMessage.getParts();
        }

        //
        // RULE No.1:
        // The operation's input and output message (if present) each contain
        // only a single part
        //
        if (inputParts.size() > 1 || outputParts.size() > 1) {
            return false;
        }

        //
        // RULE No.2:
        // The input message part refers to a global element decalration whose
        // localname
        // is equal to the operation name
        //
        Part inputPart = null;
        if (inputParts.size() == 1) {
            inputPart = inputParts.values().iterator().next();
            if (inputPart != null) {
                QName inputElement = inputPart.getElementName();
                if (inputElement == null) {
                    return false;
                } else if (!operation.getName().equals(inputElement.getLocalPart())) {
                    return false;
                }
            }
        }
        //
        // RULE No.3:
        // The output message part refers to a global element decalration
        //
        Part outputPart = null;
        if (outputParts.size() == 1) {
            outputPart = outputParts.values().iterator().next();
            if (outputPart != null) {
                QName outputElement = outputPart.getElementName();
                if (outputElement == null) {
                    return false;
                }
            }
        }

        //
        // RULE No.4 and No5:
        // wrapper element should be pure complex type
        //
        DataBindingGenerator databinder = (DataBindingGenerator)env.get(ToolConstants.BINDING_GENERATOR);
        if (databinder.getBlock(inputPart) == null
            || databinder.getBlock(outputPart) == null) {
            return false;
        }

        return true;
    }
    public static Node cloneNode(Document document, Node node, boolean deep) throws DOMException {
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

}
