package org.objectweb.celtix.tools.common.model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.wsdl.Definition;
import javax.wsdl.WSDLException;
import javax.wsdl.factory.WSDLFactory;
import javax.xml.bind.JAXBException;

import com.sun.xml.bind.api.JAXBRIContext;
import com.sun.xml.bind.api.TypeReference;

import org.objectweb.celtix.tools.common.WSDLConstants;
import org.objectweb.celtix.tools.common.toolspec.ToolException;

public class WSDLModel {
    protected JAXBRIContext jaxbContext;
    private Definition definition;
    private String wsdlLocation;
    private String serviceName;
    private String targetNameSpace;
    private String portTypeName;
    private String packageName;
    private final List<JavaMethod> methods = new ArrayList<JavaMethod>();
    private List<String> knownNamespaceURIs;

    public WSDLModel() throws ToolException {
        try {
            WSDLFactory wsdlFactory = WSDLFactory.newInstance();
            definition = wsdlFactory.newDefinition();
        } catch (WSDLException e) {
            throw new ToolException("New WSDL model failed", e);
        }
    }

    public void setWsdllocation(String loc) {
        this.wsdlLocation = loc;

    }

    public String getWsdllocation() {
        return this.wsdlLocation;

    }

    public void setServiceName(String name) {
        this.serviceName = name;
    }

    public String getServiceName() {
        return this.serviceName;
    }

    public void setPortyTypeName(String name) {
        this.portTypeName = name;
    }

    public String getPortyTypeName() {
        return this.portTypeName;
    }

    public void setTargetNameSpace(String space) {
        this.targetNameSpace = space;
    }

    public String getTargetNameSpace() {
        return this.targetNameSpace;
    }

    public Definition getDefinition() {
        return this.definition;
    }

    public String getPackageName() {
        return this.packageName;
    }

    public void setPackageName(String name) {
        this.packageName = name;
    }

    public void addJavaMethod(JavaMethod jmothd) {
        this.methods.add(jmothd);
    }

    public List<JavaMethod> getJavaMethods() {
        return this.methods;
    }

    public JAXBRIContext createJAXBContext() throws ToolException {
        final List<TypeReference> types = getAllTypeReferences();
        final Class[] clzzs = new Class[types.size()];
        int i = 0;
        for (TypeReference typeref : types) {
            clzzs[i++] = (Class)typeref.type;
        }

        try {
            jaxbContext = JAXBRIContext.newInstance(clzzs, types, this.targetNameSpace, false);
        } catch (JAXBException e) {
            throw new ToolException("Exception When New JAXBRIContext :" + e.getMessage(), e);
        }

        knownNamespaceURIs = new ArrayList<String>();
        for (String ns : jaxbContext.getKnownNamespaceURIs()) {
            if (ns.length() > 0 && !ns.equals(WSDLConstants.XSD_NAMESPACE)) {

                knownNamespaceURIs.add(ns);

            }
        }

        return jaxbContext;
    }

    /**
     * @return returns non-null list of TypeReference
     */
    public List<TypeReference> getAllTypeReferences() {
        List<TypeReference> types = new ArrayList<TypeReference>();

        for (JavaMethod m : methods) {

            Iterator ite1 = m.getObjectParameters().iterator();
            while (ite1.hasNext()) {
                Object obj = ite1.next();
                if (obj instanceof WSDLWrapperParameter) {
                    WSDLWrapperParameter wrapPara = (WSDLWrapperParameter)obj;
                    types.add(wrapPara.getTypeReference());
                    Iterator ite2 = wrapPara.getWrapperChildren().iterator();
                    while (ite2.hasNext()) {
                        JavaParameter jp = (JavaParameter)ite2.next();
                        types.add(jp.getTypeReference());
                    }
                }

                if (obj instanceof JavaParameter) {
                    JavaParameter jpara = (JavaParameter)obj;
                    types.add(jpara.getTypeReference());
                }

                Iterator ite3 = m.getWSDLExceptions().iterator();
                while (ite3.hasNext()) {
                    org.objectweb.celtix.tools.common.model.WSDLException 
                    wsdlEx = (org.objectweb.celtix.tools.common.model.WSDLException)ite3.next();
                    types.add(wsdlEx.getDetailTypeReference());
                }
            }

        }
        return types;
    }

    public JAXBRIContext getJaxbContext() {
        return this.jaxbContext;
    }

}
