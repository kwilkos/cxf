package org.objectweb.celtix.tools.utils;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.logging.Logger;

import javax.wsdl.Message;
import javax.wsdl.Operation;
import javax.wsdl.Part;
import javax.xml.namespace.QName;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import com.sun.tools.xjc.api.Property;
import com.sun.tools.xjc.api.S2JJAXBModel;
import com.sun.tools.xjc.api.TypeAndAnnotation;
import com.sun.xml.bind.api.JAXBRIContext;

import org.objectweb.celtix.common.logging.LogUtils;
import org.objectweb.celtix.tools.common.ProcessorEnvironment;
import org.objectweb.celtix.tools.common.ToolConstants;
import org.objectweb.celtix.tools.common.ToolException;
import org.objectweb.celtix.tools.processors.wsdl2.internal.ClassCollector;

public final class ProcessorUtil {
    private static final Logger LOG = LogUtils.getL7dLogger(ProcessorUtil.class);
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
    public static QName getMappedElementName(Part part, ProcessorEnvironment env) {
        QName origin = getElementName(part);
        if (origin == null) {
            return null;
        }
        if (!env.hasNamespace(origin.getNamespaceURI())) {
            return origin;
        }
        return new QName(env.getCustomizedNS(origin.getNamespaceURI()), origin.getLocalPart());
    }

    public static String resolvePartType(Part part, ProcessorEnvironment env) {
        if (env != null) {
            return resolvePartType(part, (S2JJAXBModel)env.get(ToolConstants.RAW_JAXB_MODEL));
        } else {
            return resolvePartType(part);
        }
    }

    public static String resolvePartType(Part part, S2JJAXBModel jaxbModel) {
        return resolvePartType(part, jaxbModel, false);
    }

    public static String resolvePartType(Part part, S2JJAXBModel jaxbModel, boolean fullName) {
        if (jaxbModel == null) {
            return resolvePartType(part);
        }
        com.sun.tools.xjc.api.Mapping mapping = jaxbModel.get(getElementName(part));
        if (mapping == null) {
            return resolvePartType(part);
        }
        if (fullName) {
            return mapping.getType().getTypeClass().fullName();
        } else {
            return mapping.getType().getTypeClass().name();
        }
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
        return JAXBRIContext.mangleNameToVariableName(vName);
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

    //
    // the wrapper style will get the type info from the properties in the block
    //
    public static List<? extends Property> getBlock(Part part, ProcessorEnvironment env) 
        throws ToolException {
        if (part == null) {
            return new ArrayList<Property>();
        }

        S2JJAXBModel jaxbModel = (S2JJAXBModel)env.get(ToolConstants.RAW_JAXB_MODEL);

        // QName element = getMappedElementName(part, env);
        QName element = getElementName(part);

        if (element != null && jaxbModel != null) {
            com.sun.tools.xjc.api.Mapping mapping = jaxbModel.get(element);
            if (mapping != null) {
                return mapping.getWrapperStyleDrilldown();
            } else {
                org.objectweb.celtix.common.i18n.Message msg = 
                    new org.objectweb.celtix.common.i18n.Message("ELEMENT_MISSING", 
                                                                 LOG, 
                                                                 new Object[]{element.toString(), 
                                                                              part.getName()});
                throw new ToolException(msg);
                // return new ArrayList<Property>();
            }
        } else {
            return new ArrayList<Property>();
        }
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
    public static String getFullClzName(Part part, ProcessorEnvironment env, boolean boxify,
                                        ClassCollector collector) {
        S2JJAXBModel jaxbModel = (S2JJAXBModel)env.get(ToolConstants.RAW_JAXB_MODEL);

        QName xmlTypeName = getElementName(part);
        String jtype = BuiltInTypesJavaMappingUtil.getJType(xmlTypeName, jaxbModel, boxify);
        String namespace = xmlTypeName.getNamespaceURI();
        String type = resolvePartType(part, jaxbModel);
        String userPackage = env.mapPackageName(namespace);

        if (jtype == null) {
            jtype = collector.getTypesFullClassName(parsePackageName(namespace, userPackage), type);
        }

        if (jtype == null) {
            if (!type.equals(resolvePartType(part))) {
                jtype = resolvePartType(part, jaxbModel, true);
            } else {
                jtype = parsePackageName(namespace, userPackage) + "." + type;
            }
        }

        return jtype;
    }

    public static String getFullClzName(Part part, ProcessorEnvironment env, ClassCollector collector) {
        return getFullClzName(part, env, false, collector);

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
            return new URL(baseURL, name).toExternalForm();
        } catch (IOException e) {
            // ignore
        }
        return name;
    }

    public static String getHandlerConfigFileName(String name) {
        return name + "_handler";
    }

    public static String boxify(QName xmlTypeName, S2JJAXBModel jaxbModel) {
        TypeAndAnnotation typeAndAnnotation = jaxbModel.getJavaType(xmlTypeName);
        if (typeAndAnnotation == null) {
            return null;
        }
        return typeAndAnnotation.getTypeClass().boxify().fullName();
    }

    @SuppressWarnings("unchecked")
    public static boolean isWrapperStyle(Operation operation, ProcessorEnvironment env) throws ToolException {

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
        if (ProcessorUtil.getBlock(inputPart, env) == null 
            || ProcessorUtil.getBlock(outputPart, env) == null) {
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
