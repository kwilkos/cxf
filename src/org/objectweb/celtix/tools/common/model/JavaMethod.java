package org.objectweb.celtix.tools.common.model;

import java.util.*;
import javax.jws.soap.SOAPBinding;
import javax.wsdl.OperationType;
import org.objectweb.celtix.tools.common.ToolException;
import org.objectweb.celtix.tools.jaxws.JAXWSBinding;

public class JavaMethod {

    private String name;
    private JavaReturn javaReturn;
    private OperationType style;
    private String soapAction;
    private SOAPBinding.Style soapStyle;
    private SOAPBinding.Use soapUse;
    private boolean wrapperStyle;
    private final JavaInterface javaInterface;
    private final List<JavaParameter> parameters = new ArrayList<JavaParameter>();
    private final List<JavaException> exceptions = new ArrayList<JavaException>();
    private final Map<String, JavaAnnotation> annotations = new HashMap<String, JavaAnnotation>();
    private final List<Object> objparas = new ArrayList<Object>();
    private final List<WSDLException> wsdlExceptions = new ArrayList<WSDLException>();

    private JAXWSBinding jaxwsBinding = new JAXWSBinding();
    
    public JavaMethod() {
        this.javaInterface = null;
    }

    public JavaMethod(JavaInterface i) {
        this.javaInterface = i;
    }

    public void clear() {
        parameters.clear();
        javaReturn = null;
        annotations.clear();
    }

    public JavaInterface getInterface() {
        return this.javaInterface;
    }

    public String getName() {
        return name;
    }

    public void setName(String n) {
        this.name = n;
    }

    public JavaReturn getReturn() {
        return javaReturn;
    }

    public void setReturn(JavaReturn rt) {
        this.javaReturn = rt;
    }

    public boolean hasParameter(String paramName) {
        for (int i = 0; i < parameters.size(); i++) {
            if (paramName.equals((parameters.get(i)).getName())) {
                return true;
            }
        }
        return false;
    }

    private void removeParameter(JavaParameter param) {
        parameters.remove(param);
    }

    public void addParameter(JavaParameter param) {
        if (hasParameter(param.getName())) {
            JavaParameter paramInList = getParameter(param.getName());
            if (paramInList.isIN()) {
                removeParameter(paramInList);
            } else {
                throw new ToolException("model.uniqueness, following model already exist:\n"
                                        + param.toString());
            }
        }
        parameters.add(param);
    }

    public JavaParameter getParameter(String paramName) {
        for (int i = 0; i < parameters.size(); i++) {
            JavaParameter jParam = parameters.get(i);
            if (paramName.equals(jParam.getName())) {
                return jParam;
            }
        }
        return null;
    }

    public List<JavaParameter> getParameters() {
        return parameters;
    }

    public int getParameterCount() {
        return parameters.size();
    }

    public boolean hasException(JavaException exception) {
        return exceptions.contains(exception);
    }

    public void addException(JavaException exception) {
        if (hasException(exception)) {
            throw new ToolException("model.uniqueness");
        }
        exceptions.add(exception);
    }

    public List<JavaException> getExceptions() {
        return exceptions;
    }

    public OperationType getStyle() {
        return this.style;
    }

    public void setStyle(OperationType ot) {
        this.style = ot;
    }

    public boolean isOneWay() {
        return OperationType.ONE_WAY.equals(getStyle());
    }

    public boolean isWrapperStyle() {
        return this.wrapperStyle;
    }

    public void setWrapperStyle(boolean w) {
        this.wrapperStyle = w;
    }

    public void setSoapStyle(SOAPBinding.Style sty) {
        this.soapStyle = sty;
    }

    public SOAPBinding.Style getSoapStyle() {
        return this.soapStyle;
    }

    public void setSoapAction(String action) {
        this.soapAction = action;
    }

    public String getSoapAction() {
        return this.soapAction;
    }

    public void setSoapUse(SOAPBinding.Use u) {
        this.soapUse = u;
    }

    public SOAPBinding.Use getSoapUse() {
        return this.soapUse;
    }

    public void addAnnotation(String tag, JavaAnnotation annotation) {
        this.annotations.put(tag, annotation);
    }

    public Collection<JavaAnnotation> getAnnotations() {
        return this.annotations.values();
    }
    
    public Map<String, JavaAnnotation> getAnnotationMap() {
        return this.annotations;
    }

    public void addWSDLException(WSDLException exception) {
        if (wsdlExceptions.contains(exception)) {
            throw new ToolException("exception.uniqueness");
        }
        wsdlExceptions.add(exception);
    }

    public List<WSDLException> getWSDLExceptions() {
        return wsdlExceptions;
    }

    public void addObjectParameter(Object obj) {
        // verify that this member does not already exist
        if (objparas.contains(obj)) {
            throw new ToolException("model.uniqueness");
        }
        objparas.add(obj);
    }

    public List<Object> getObjectParameters() {
        return objparas;
    }

    public String getParameterList() {
        return getParameterList(true);
    }
    
    public String getParameterList(boolean includeAnnotation) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < parameters.size(); i++) {
            JavaParameter parameter = parameters.get(i);
            if (includeAnnotation) {
                sb.append(parameter.getAnnotation());
            }
            sb.append("\n");
            if (parameter.isHolder()) {
                sb.append(parameter.getHolderName());
                sb.append("<");
                sb.append(parameter.getHolderClass());
                sb.append(">");
            } else {
                sb.append(parameter.getClassName());
            }
            sb.append(" ");
            sb.append(parameter.getName());
            if (i != parameters.size() - 1) {
                sb.append(",\n");
            }
        }
        return sb.toString();
    }

    public JAXWSBinding getJAXWSBinding() {
        return this.jaxwsBinding;
    }
    
    public void setJAXWSBinding(JAXWSBinding binding) {
        if (binding != null) {
            this.jaxwsBinding = binding;
        }
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("\n========================\n");
        sb.append("\nMethod:");
        sb.append(getName());
        sb.append("\n-----------\n");
        sb.append("\nReturn:");
        sb.append(getReturn());
        sb.append("\n------------\n");
        sb.append("\nParameter:");
        sb.append(getParameterList());
        sb.append("\n========================\n");
        return sb.toString();
    }
}
