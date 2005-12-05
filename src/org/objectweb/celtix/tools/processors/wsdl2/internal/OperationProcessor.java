package org.objectweb.celtix.tools.processors.wsdl2.internal;

import java.util.*;

import javax.wsdl.Fault;
import javax.wsdl.Message;
import javax.wsdl.Operation;
import javax.wsdl.OperationType;
import javax.wsdl.Part;
import javax.xml.namespace.QName;

import org.objectweb.celtix.tools.common.ProcessorEnvironment;
import org.objectweb.celtix.tools.common.model.JavaInterface;
import org.objectweb.celtix.tools.common.model.JavaMethod;
import org.objectweb.celtix.tools.utils.ProcessorUtil;

public class OperationProcessor  {
    
    private final ProcessorEnvironment env;
    
    public OperationProcessor(ProcessorEnvironment penv) {
        this.env = penv;
    }

    @SuppressWarnings("unchecked")
    public void process(JavaInterface intf, Operation operation) throws Exception {
        JavaMethod method = new JavaMethod(intf);
        method.setName(operation.getName());
        method.setStyle(operation.getStyle());
        method.setWrapperStyle(isWrapperStyle(operation));

        processMethod(method, operation);
        
        Map<String, Fault> faults = operation.getFaults();
        FaultProcessor faultProcessor = new FaultProcessor(env);
        faultProcessor.process(method, faults);

        intf.addMethod(method);
    }

    @SuppressWarnings("unchecked")
    public void processMethod(JavaMethod method, Operation operation) throws Exception {
        List<String> parameterOrder = operation.getParameterOrdering();
        Message inputMessage = operation.getInput() == null ? null : operation.getInput().getMessage();
        Message outputMessage = operation.getOutput() == null ? null : operation.getOutput().getMessage();

        ParameterProcessor paramProcessor = new ParameterProcessor(env);
        method.clear();
        paramProcessor.process(method,
                               inputMessage,
                               outputMessage,
                               isRequestResponse(operation),
                               parameterOrder);
    }

    @SuppressWarnings("unchecked")
    private boolean isWrapperStyle(Operation operation) {

        Message inputMessage = operation.getInput() == null ? null : operation.getInput().getMessage();
        Message outputMessage = operation.getOutput() == null ? null : operation.getOutput().getMessage();

        Map<String, Part> inputParts = new HashMap<String, Part>();
        Map<String, Part> outputParts = new HashMap<String, Part>();
        
        if (inputMessage != null) {
            inputParts = inputMessage.getParts();
        }
        if (outputMessage != null) {
            outputParts = outputMessage.getParts();
        }
        
        //
        // RULE No.1:
        // The operation's input and output message (if present) each contain only a single part
        //
        if (inputParts.size() > 1 || outputParts.size() > 1) {
            return false;
        }

        //
        // RULE No.2:
        // The input message part refers to a global element decalration whose localname
        // is equal to the operation name
        //
        Part inputPart = null;
        if (inputParts.size() == 1) {
            inputPart = (Part) inputParts.values().iterator().next();
            if (inputPart != null) {
                QName inputElement = inputPart.getElementName();
                if (inputElement == null) {
                    return false;
                } else if (!operation.getName().equals(inputElement.getLocalPart())) {
                    return false;
                }
            }
        }
        //
        // RULE No.3:
        // The output message part refers to a global element decalration
        //
        Part outputPart = null;
        if (outputParts.size() == 1) {
            outputPart = (Part) outputParts.values().iterator().next();
            if (outputPart != null) {
                QName outputElement = outputPart.getElementName();
                if (outputElement == null) {
                    return false;
                }
            }
        }

        //
        // RULE No.4 and No5:
        // wrapper element should be pure complex type
        //
        if (ProcessorUtil.getBlock(inputPart, env) == null
            || ProcessorUtil.getBlock(outputPart, env) == null) {
            return false;
        }

        return true;
    }

    private boolean isRequestResponse(Operation operation) throws Exception {
        if (operation.getStyle() == null) {
            throw new Exception("can't get operation style for " + operation.getName());
        }
        return OperationType.REQUEST_RESPONSE.equals(operation.getStyle());
    }
}
