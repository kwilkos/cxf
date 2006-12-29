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

package org.apache.cxf.tools.wsdlto.frontend.jaxws.processor.internal;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.StringTokenizer;
import javax.xml.namespace.QName;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import com.sun.xml.bind.api.JAXBRIContext;

import org.apache.cxf.helpers.JavaUtils;
import org.apache.cxf.jaxb.JAXBUtils;
import org.apache.cxf.service.model.MessagePartInfo;

import org.apache.cxf.tools.common.ToolContext;
import org.apache.cxf.tools.util.ClassCollector;
import org.apache.cxf.tools.util.URIParserUtil;
import org.apache.cxf.tools.wsdlto.core.DataBindingProfile;

public final class ProcessorUtil {
    private static final String KEYWORDS_PREFIX = "_";
    
    private ProcessorUtil() {
    }

    public static String resolvePartName(MessagePartInfo part) {
        return mangleNameToVariableName(part.getName().getLocalPart());
    }

    public static String getPartType(MessagePartInfo part) {
        return part.getConcreteName().getLocalPart();
    }

    public static String resolvePartType(MessagePartInfo part) {
        return mangleNameToClassName(getPartType(part));
    }
    
    public static String getType(MessagePartInfo part, ToolContext context, boolean fullname) {
        DataBindingProfile dataBinding = context.get(DataBindingProfile.class);
        String type = dataBinding.getType(getElementName(part), fullname);
        if (type == null) {
            type = resolvePartType(part);
        }
        return type;
    }
    
    public static QName getElementName(MessagePartInfo part) {
        return part == null ? null : part.getConcreteName();
    }

    //
    // support multiple -p options
    // if user change the package name through -p namespace=package name
    //
    public static QName getMappedElementName(MessagePartInfo part, ToolContext env) {
        QName origin = getElementName(part);
        if (origin == null) {
            return null;
        }
        if (!env.hasNamespace(origin.getNamespaceURI())) {
            return origin;
        }
        return new QName(env.getCustomizedNS(origin.getNamespaceURI()), origin.getLocalPart());
    }

    public static String resolvePartType(MessagePartInfo part, ToolContext env) {
        if (env != null) {
            return resolvePartType(part, env, false);
        } else {
            return resolvePartType(part);
        }
    }

    public static String resolvePartType(MessagePartInfo part, ToolContext context, boolean fullName) {
        DataBindingProfile dataBinding = context.get(DataBindingProfile.class);
        if (dataBinding == null) {
            String primitiveType = JAXBUtils.builtInTypeToJavaType(part.getTypeQName().getLocalPart());
            if (part.getTypeQName() != null &&  primitiveType != null) {
                return primitiveType;
            } else {
                return resolvePartType(part);
            }
        }
        String name = dataBinding.getType(getElementName(part), fullName);
        if (name == null) {
            return resolvePartType(part);
        }
        return name;
       
    }

    public static String resolvePartNamespace(MessagePartInfo part) {
        return part.getConcreteName().getNamespaceURI();
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
    public static String getFullClzName(MessagePartInfo part, ToolContext context, boolean boxify) {
        DataBindingProfile dataBinding = context.get(DataBindingProfile.class);
        String jtype = null;
        QName xmlTypeName = getElementName(part);

        // if this flag  is true , mapping to java Type first;
        // if not found , findd the primitive type : int ,long 
        // if not found,  find in the generated class
       
            
        if (boxify && dataBinding != null) {
            jtype = dataBinding.getJavaType(xmlTypeName, true);
        } 
        
        if (boxify && dataBinding == null) {
            Class holderClass = JAXBUtils.holderClass(xmlTypeName.getLocalPart());
            jtype = holderClass == null ? null : holderClass.getName();
            if (jtype == null) {
                jtype = JAXBUtils.builtInTypeToJavaType(xmlTypeName.getLocalPart());
            }
                       
        }
        
        if (!boxify && dataBinding != null) {
            jtype = dataBinding.getJavaType(xmlTypeName, false);
        }
        
        if (!boxify && dataBinding == null) {
            jtype = JAXBUtils.builtInTypeToJavaType(xmlTypeName.getLocalPart());
        }
            
        String namespace = xmlTypeName.getNamespaceURI();
        String type = resolvePartType(part, context, true);
        String userPackage = context.mapPackageName(namespace);

        ClassCollector collector = context.get(ClassCollector.class);
        if (jtype == null) {
            jtype = collector.getTypesFullClassName(parsePackageName(namespace, userPackage), type);
        }

        if (jtype == null) {
            if (!type.equals(resolvePartType(part))) {
                jtype = resolvePartType(part, context, true);
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
