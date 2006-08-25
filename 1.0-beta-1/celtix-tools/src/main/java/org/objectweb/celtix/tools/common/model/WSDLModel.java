package org.objectweb.celtix.tools.common.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.jws.soap.SOAPBinding;
import javax.jws.soap.SOAPBinding.ParameterStyle;
import javax.jws.soap.SOAPBinding.Style;
import javax.jws.soap.SOAPBinding.Use;
import javax.wsdl.Definition;
import javax.wsdl.WSDLException;
import javax.wsdl.factory.WSDLFactory;

import com.sun.xml.bind.api.JAXBRIContext;
import com.sun.xml.bind.api.TypeReference;

import org.objectweb.celtix.common.i18n.Message;
import org.objectweb.celtix.common.logging.LogUtils;
import org.objectweb.celtix.tools.common.ToolException;
import org.objectweb.celtix.tools.processors.java2.JavaToWSDLProcessor;

public class WSDLModel {
    private static final Logger LOG = LogUtils.getL7dLogger(JavaToWSDLProcessor.class);
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
            Message message =  new Message("FAIL_TO_CREATE_WSDL_DEFINITION", LOG);
            throw new ToolException(message, e);
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
            clzzs[i++] = (Class)typeref.type;
        }
        try {
            jaxbContext = JAXBRIContext.newInstance(clzzs, types, this.getTargetNameSpace(), false);

        } catch (Exception e) {
            Message message = new Message("CREATE_JAXBRICONTEXT_EXCEPTION", LOG);
            throw new ToolException(message, e);
        }

    }

    /**
     * @return returns non-null list of TypeReference
     */
    public List<TypeReference> getAllTypeReference() {
        List<TypeReference> types = new ArrayList<TypeReference>();
        for (JavaMethod m : methods) {
            WSDLParameter request = m.getRequest();
            if (request.getTypeReference() != null && m.isWrapperStyle()) {
                types.add(request.getTypeReference());

            } else {
                Iterator ite2 = request.getChildren().iterator();
                while (ite2.hasNext()) {
                    JavaParameter jp = (JavaParameter)ite2.next();
                    if (jp.getTypeReference() != null) {
                        types.add(jp.getTypeReference());

                    }
                }
            }
            if (!m.isOneWay()) {
                WSDLParameter response = m.getResponse();
                if (response.getTypeReference() != null && m.isWrapperStyle()) {
                    types.add(response.getTypeReference());

                } else {
                    Iterator ite2 = response.getChildren().iterator();
                    while (ite2.hasNext()) {
                        JavaParameter jp = (JavaParameter)ite2.next();
                        if (jp.getTypeReference() != null) {
                            types.add(jp.getTypeReference());

                        }
                    }
                }
            }
            Iterator ite3 = m.getWSDLExceptions().iterator();
            while (ite3.hasNext()) {
                org.objectweb.celtix.tools.common.model.WSDLException wsdlEx = 
                    (org.objectweb.celtix.tools.common.model.WSDLException)ite3.next();
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
