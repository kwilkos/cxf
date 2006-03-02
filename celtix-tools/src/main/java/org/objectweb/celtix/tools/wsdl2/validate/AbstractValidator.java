package org.objectweb.celtix.tools.wsdl2.validate;

import javax.wsdl.Definition;

public abstract class AbstractValidator {
    protected String errorMessage;
    protected Definition def;
    public AbstractValidator(Definition definition) {
        this.def = definition;
    }
    public abstract boolean isValid();
    public  String getErrorMessage() {
        return errorMessage;
    }
  
}
