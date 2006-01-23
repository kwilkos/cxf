package org.objectweb.celtix.tools.common.model;

import java.util.*;
import javax.jws.soap.SOAPBinding;
import javax.jws.soap.SOAPBinding.ParameterStyle;
import javax.jws.soap.SOAPBinding.Style;
import javax.jws.soap.SOAPBinding.Use;
import javax.wsdl.Definition;
import javax.wsdl.WSDLException;
import javax.wsdl.factory.WSDLFactory;
import com.sun.xml.bind.api.JAXBRIContext;
import com.sun.xml.bind.api.TypeReference;
import org.objectweb.celtix.tools.common.toolspec.ToolException;

public class WSDLModel {
    protected JAXBRIContext jaxbContext;

    private Definition definition;

    private String wsdlLocation;

    private String serviceName;

    private String targetNameSpace;

    private String portTypeName;

    private String portName;

    private String packageName;

    private final List<JavaMethod> methods = new ArrayList<JavaMethod>();

    private final Map<String, String> schemaNSFileMap = new HashMap<String, String>();

    // default Doc-Lit-Wrapped
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

    public String getPortTypeName() {
        return this.portTypeName;
    }

    public void setPortTypeName(String pname) {
        this.portTypeName = pname;
    }

    public void setPortName(String name) {
        this.portName = name;
    }

    public String getPortName() {
        return this.portName;
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

    public void createJAXBContext() throws ToolException {
        List<TypeReference> types = this.getAllTypeReference();

        Class[] clzzs = new Class[types.size()];
        int i = 0;
        for (TypeReference typeref : types) {
            clzzs[i++] = (Class) typeref.type;
        }
        try {
            jaxbContext = JAXBRIContext.newInstance(clzzs, types, this
                    .getTargetNameSpace(), false);

        } catch (Exception e) {
            throw new ToolException("Exception When New JAXBRIContext :"
                    + e.getMessage(), e);
        }

    }

    /**
     * @return returns non-null list of TypeReference
     */
    public List<TypeReference> getAllTypeReference() {
        List<TypeReference> types = new ArrayList<TypeReference>();
        for (JavaMethod m : methods) {
            for (WSDLWrapperParameter wrapPara : m.getWSDLWrapperParameters()) {
                if (wrapPara.getTypeReference() != null && m.isWrapperStyle()) {
                    types.add(wrapPara.getTypeReference());

                } else {
                    Iterator ite2 = wrapPara.getWrapperChildren().iterator();
                    while (ite2.hasNext()) {
                        JavaParameter jp = (JavaParameter) ite2.next();
                        if (jp.getTypeReference() != null) {
                            types.add(jp.getTypeReference());

                        }
                    }
                }

            }

            Iterator ite3 = m.getWSDLExceptions().iterator();
            while (ite3.hasNext()) {
                org.objectweb.celtix.tools.common.model.WSDLException wsdlEx = 
                    (org.objectweb.celtix.tools.common.model.WSDLException) ite3.next();

                types.add(wsdlEx.getDetailTypeReference());
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
        return (this.style == SOAPBinding.Style.RPC)
                && (this.use == SOAPBinding.Use.LITERAL)
                && (this.paraStyle == SOAPBinding.ParameterStyle.WRAPPED);
    }

    public Map<String, String> getSchemaNSFileMap() {
        return this.schemaNSFileMap;
    }

    public void addSchemaNSFileToMap(String schemaNS, String filename) {
        this.schemaNSFileMap.put(schemaNS, filename);
    }

}
