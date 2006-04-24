package org.objectweb.celtix.tools.processors.wsdl2.validators;

import java.util.List;
import java.util.Vector;

import javax.wsdl.Definition;

import org.w3c.dom.Node;

import org.objectweb.celtix.tools.common.ProcessorEnvironment;
import org.objectweb.celtix.tools.common.ToolException;
import org.objectweb.celtix.tools.common.WSDLConstants;
import org.objectweb.celtix.tools.utils.ElementLocator;

public abstract class AbstractValidator {
    protected List<String> errorMessages = new Vector<String>();
    protected Definition def;
    protected ProcessorEnvironment env;

    public AbstractValidator(Definition definition) {
        this.def = definition;
    }

    public AbstractValidator(String schemaDir) throws ToolException {        
    }
    
    public AbstractValidator(Definition definition, ProcessorEnvironment pEnv) {
        this.def = definition;
        this.env = pEnv;
    }

    public abstract boolean isValid();

    public void addErrorMessage(String err) {
        errorMessages.add(err);
    }

    public String getErrorMessage() {
        StringBuffer strbuffer = new StringBuffer();
        for (int i = 0; i < errorMessages.size(); i++) {
            strbuffer.append(errorMessages.get(i));
            strbuffer.append(System.getProperty("line.separator"));
        }
        return strbuffer.toString();
    }

    public void addError(Node node, String msg) {
        ElementLocator locator = (ElementLocator)node.getUserData(WSDLConstants.NODE_LOCATION);
        locator.getColumn();
        String errMsg = "line " + locator.getLine() + " column " + locator.getColumn();
        errMsg = errMsg + " of " + def.getDocumentBaseURI() + " " + msg;
        addErrorMessage(msg);
    }
}
