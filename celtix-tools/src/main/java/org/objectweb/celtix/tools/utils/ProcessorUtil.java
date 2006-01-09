package org.objectweb.celtix.tools.utils;

import java.io.*;
import java.net.*;
import java.util.*;

import javax.wsdl.Part;
import javax.xml.namespace.QName;

import com.sun.tools.xjc.api.Property;
import com.sun.tools.xjc.api.S2JJAXBModel;
import com.sun.xml.bind.api.JAXBRIContext;

import org.objectweb.celtix.tools.common.ProcessorEnvironment;

public final class ProcessorUtil {

    private ProcessorUtil() {
        // complete
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

    public static String resolvePartType(Part part, ProcessorEnvironment env) {
        if (env != null) {
            S2JJAXBModel jaxbModel = (S2JJAXBModel) env.get("rawjaxbmodel");
            com.sun.tools.xjc.api.Mapping mapping = jaxbModel.get(getElementName(part));
            if (mapping == null) {
                return resolvePartType(part);
            }
            return mapping.getType().getTypeClass().name();
        } else {
            return resolvePartType(part);
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
        String packageName = (defaultPackageName != null
                              && defaultPackageName.trim().length() > 0) ? defaultPackageName : null;
        
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
            return  new File(getAbsolutePath(location)).toURL();
        }
        
    }
    
    
    private static String resolvePath(String path) {
        return path.replace('\\', '/');
    }

    @SuppressWarnings("unchecked")
    public static List<? extends Property> getBlock(Part part, ProcessorEnvironment env) {
        if (part == null) {
            return new ArrayList<Property>();
        }
        S2JJAXBModel jaxbModel = (S2JJAXBModel) env.get("rawjaxbmodel");
        
        QName element;
        element = part.getElementName();
        if (element == null) {
            element = part.getTypeName();
        }
        if (element != null && jaxbModel != null) {
            com.sun.tools.xjc.api.Mapping mapping = jaxbModel.get(element);
            if (mapping != null) {
                return mapping.getWrapperStyleDrilldown();
            } else {
                return new ArrayList<Property>();
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


    public static String getFullClzName(String namespace,
                                        String type,
                                        String userPackage) {
        String jtype = BuiltInTypesJavaMappingUtil.getJType(namespace, type);
        if (jtype == null) {
            ClassCollectorUtil collector = ClassCollectorUtil.getInstance();
            jtype = collector.getTypesFullClassName(parsePackageName(namespace, userPackage), type);
        }

        if (jtype == null) {
            jtype = parsePackageName(namespace, userPackage) + "." + type;
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
            return new URL(baseURL, name).toExternalForm();
        } catch (IOException e) {
            // ignore
        }
        return name;
    }
}
