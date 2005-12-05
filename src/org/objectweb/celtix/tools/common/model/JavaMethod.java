package org.objectweb.celtix.tools.common.model;

import java.util.*;
import javax.wsdl.OperationType;

public class JavaMethod {

    private String name;
    private JavaReturn javaReturn;
    private OperationType style;
    private String soapAction;
    private String soapStyle;
    private String soapUse;
    private boolean wrapperStyle;
    private final JavaInterface javaInterface;
    private final List<JavaParameter> parameters = new ArrayList<JavaParameter>();
    private final List<JavaException> exceptions = new ArrayList<JavaException>();
    
    public JavaMethod() {
        this.javaInterface = null;
    }
    
    public JavaMethod(JavaInterface i) {
        this.javaInterface = i;
    }

    public void clear() {
        parameters.clear();
        javaReturn = null;
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
            if (paramName.equals(
                ((JavaParameter)parameters.get(i)).getName())) {

                return true;
            }
        }
        return false;
    }

    public void addParameter(JavaParameter param) throws Exception {
        // verify that this member does not already exist
        if (hasParameter(param.getName())) {
            throw new Exception("model.uniqueness");
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

    public void addException(JavaException exception) throws Exception {
        if (hasException(exception)) {
            throw new Exception("model.uniqueness");
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

    public void setSoapStyle(String sty) {
        this.soapStyle = sty;
    }
    
    public String getSoapStyle() {
        return this.soapStyle;
    }

    public void setSoapAction(String action) {
        this.soapAction = action;
    }

    public String getSoapAction() {
        return this.soapAction; 
    }
    
    public void setSoapUse(String u) {
        this.soapUse = u;
    }
    
    public String getSoapUse() {
        return this.soapUse;
    }
}
