package org.objectweb.celtix.tools.utils;

import java.io.*;
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

    public static String resolvePartType(Part part) {
        String typeName;
        if (part.getElementName() != null) {
            typeName = part.getElementName().getLocalPart();
        } else if (part.getTypeName() != null) {
            typeName = part.getTypeName().getLocalPart();
        } else {
            typeName = "BadType";
        }
        return mangleNameToClassName(typeName);
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

    public static String parsePackageName(String namespace, String[] defaultPackageNames) {
        String packageName = (defaultPackageNames != null
                              && defaultPackageNames.length > 0) ? defaultPackageNames[0] : null;
        
        if (packageName == null) {
            packageName = URIParserUtil.getPackageName(namespace);
        }
        return packageName;
    }

    public static String getAbsolutePath(String location) throws Exception {
        return resolvePath(new File(location).getCanonicalPath());
    }

    private static String resolvePath(String path) {
        return path.replace('\\', '/');
    }

    @SuppressWarnings("unchecked")
    public static List<? extends Property> getBlock(Part part, ProcessorEnvironment env) {
        if (part == null) {
            return new ArrayList<Property>();
        }
        Map<String, S2JJAXBModel> jaxbModels = (Map<String, S2JJAXBModel>) env.get("jaxbmodels");
        QName element;
        element = part.getElementName();
        if (element == null) {
            element = part.getTypeName();
        }
        if (element != null) {
            S2JJAXBModel jaxbModel = jaxbModels.get(element.getNamespaceURI());
            return  jaxbModel.get(element).getWrapperStyleDrilldown();
        } else {
            return new ArrayList();
        }
    }
}
