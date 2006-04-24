package org.objectweb.celtix.tools.processors.wsdl2.validators;

import java.util.*;
import javax.wsdl.Definition;
import org.objectweb.celtix.tools.common.ProcessorEnvironment;

public abstract class AbstractValidator {
    protected List<String> errorMessages;
    protected Definition def;
    protected ProcessorEnvironment env;
    
    public AbstractValidator(Definition definition) {
        this.def = definition;
        this.errorMessages = new Vector();
    }
    
    public AbstractValidator(Definition definition, ProcessorEnvironment pEnv) {
        this.env = pEnv;
    }
    
    public abstract boolean isValid();

    public void addErrorMessage(String err) {
        errorMessages.add(err);
    }

    public String getErrorMessage() {
        return errorMessages.toString();
    }
}
