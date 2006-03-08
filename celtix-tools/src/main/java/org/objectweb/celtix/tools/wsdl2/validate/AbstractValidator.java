package org.objectweb.celtix.tools.wsdl2.validate;

import javax.wsdl.Definition;
import org.objectweb.celtix.tools.common.ProcessorEnvironment;

public abstract class AbstractValidator {
    protected String errorMessage;
    protected Definition def;
    protected ProcessorEnvironment env;
    
    public AbstractValidator(Definition definition) {
        this.def = definition;
    }
    
    public AbstractValidator(Definition definition, ProcessorEnvironment pEnv) {
        this.env = pEnv;
    }
    
    public abstract boolean isValid();
    public  String getErrorMessage() {
        return errorMessage;
    }
  
}
