package org.objectweb.celtix.tools.common.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.jws.soap.SOAPBinding;
import javax.jws.soap.SOAPBinding.ParameterStyle;
import javax.jws.soap.SOAPBinding.Style;
import javax.jws.soap.SOAPBinding.Use;
import javax.wsdl.Definition;
import javax.wsdl.WSDLException;
import javax.wsdl.factory.WSDLFactory;
import javax.xml.bind.JAXBException;

import com.sun.xml.bind.api.JAXBRIContext;
import com.sun.xml.bind.api.TypeReference;

import org.objectweb.celtix.tools.common.toolspec.ToolException;
import org.objectweb.celtix.tools.processors.java2.internal.ClassProcessor;

public class WSDLModel {
    protected JAXBRIContext jaxbContext;
    private Definition definition;
    private String wsdlLocation;
    private String serviceName;
    private String targetNameSpace;
    private String portTypeName;
    private String packageName;
    private final List<JavaMethod> methods = new ArrayList<JavaMethod>();
    private final Map<String, String> schemaNSFileMap = new HashMap<String, String>();
    //default Doc-Lit-Wrapped
    private Style style = SOAPBinding.Style.DOCUMENT;
    private Use use = SOAPBinding.Use.LITERAL;
    private ParameterStyle paraStyle = SOAPBinding.ParameterStyle.WRAPPED;

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
        List<TypeReference> types = getAllTypeReferences();
        Class[] clzzs = new Class[types.size()];
        int i = 0;
        for (TypeReference typeref : types) {
            clzzs[i++] = (Class)typeref.type;
        }

        try {
            jaxbContext = JAXBRIContext.newInstance(clzzs, types, this.targetNameSpace, false);
        } catch (JAXBException e) {
            throw new ToolException("Exception When New JAXBRIContext :" + e.getMessage(), e);
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
                    if (wrapPara.getTypeReference() != null
                        && wrapPara.getTypeReference().type != ClassProcessor.class) {
                        types.add(wrapPara.getTypeReference());
                    }
                    // added for rpc
                    Iterator ite2 = wrapPara.getWrapperChildren().iterator();
                    while (ite2.hasNext()) {

                        JavaParameter jp = (JavaParameter)ite2.next();
                        if (jp.getTypeReference() != null) {
                            types.add(jp.getTypeReference());
                        }
                    }

                }

                if (obj instanceof JavaParameter) {

                    JavaParameter jpara = (JavaParameter)obj;

                    if (jpara.getTypeReference() != null) {
                        types.add(jpara.getTypeReference());
                    }
                }

                Iterator ite3 = m.getWSDLExceptions().iterator();
                while (ite3.hasNext()) {
                    org.objectweb.celtix.tools.common.model.WSDLException wsdlEx = 
                        (org.objectweb.celtix.tools.common.model.WSDLException)ite3.next();
                    types.add(wsdlEx.getDetailTypeReference());
                }
            }

        }
        return types;
    }

    public JAXBRIContext getJaxbContext() {
        return this.jaxbContext;
    }

    public void setStyle(Style s) {
        this.style = s;
    }

    public Style getStyle() {
        return this.style;
    }

    public void setUse(Use u) {
        this.use = u;
    }

    public ParameterStyle getParameterStyle() {
        return paraStyle;
    }
    
    public void setPrameterStyle(ParameterStyle pstyle) {
        paraStyle = pstyle;
    }

    public Use getUse() {
        return this.use;
    }
    
    public boolean isDocLit() {
        if (this.style == Style.DOCUMENT && this.use == Use.LITERAL) {
            return true;
        } 
        return false;
    }
    
   
    public boolean isWrapped() {
        return this.paraStyle == SOAPBinding.ParameterStyle.WRAPPED;
    }

    public boolean isRPC() {
        return (this.style == SOAPBinding.Style.RPC) && (this.use == SOAPBinding.Use.LITERAL)
               && (this.paraStyle == SOAPBinding.ParameterStyle.WRAPPED);
    }

    public Map<String, String> getSchemaNSFileMap() {
        return this.schemaNSFileMap;
    }

    public void addSchemaNSFileToMap(String schemaNS, String filename) {
        this.schemaNSFileMap.put(schemaNS, filename);
    }

}
