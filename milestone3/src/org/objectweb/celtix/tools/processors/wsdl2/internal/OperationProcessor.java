package org.objectweb.celtix.tools.processors.wsdl2.internal;

import java.util.*;

import javax.jws.soap.SOAPBinding;
import javax.wsdl.Fault;
import javax.wsdl.Message;
import javax.wsdl.Operation;
import javax.wsdl.OperationType;
import javax.wsdl.Part;
import javax.xml.namespace.QName;

import org.objectweb.celtix.tools.common.ProcessorEnvironment;
import org.objectweb.celtix.tools.common.ToolConstants;
import org.objectweb.celtix.tools.common.model.JavaAnnotation;
import org.objectweb.celtix.tools.common.model.JavaInterface;
import org.objectweb.celtix.tools.common.model.JavaMethod;
import org.objectweb.celtix.tools.common.model.JavaParameter;
import org.objectweb.celtix.tools.common.toolspec.ToolException;
import org.objectweb.celtix.tools.utils.ProcessorUtil;

public class OperationProcessor  {
    
    private final ProcessorEnvironment env;
    private JavaParameter wrapperRequest;
    private JavaParameter wrapperResponse;
    
    public OperationProcessor(ProcessorEnvironment penv) {
        this.env = penv;
    }

    @SuppressWarnings("unchecked")
    public void process(JavaInterface intf, Operation operation) throws ToolException {
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
    public void processMethod(JavaMethod method, Operation operation) throws ToolException {
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
        
        addWrapperAnnotation(method, operation);
        addWebResultAnnotation(method);
    }

    private void addWebResultAnnotation(JavaMethod method) {
        if (method.isOneWay()) {
            JavaAnnotation oneWayAnnotation = new JavaAnnotation("Oneway");
            method.addAnnotation(oneWayAnnotation.toString());
            return;
        }
        if ("void".equals(method.getReturn().getType())) {
            return;
        }
        JavaAnnotation resultAnnotation = new JavaAnnotation("WebResult");
        String name = null;
        String targetNamespace = null;

        if (method.getSoapStyle() == SOAPBinding.Style.DOCUMENT
            && !method.isWrapperStyle()) {
            name = method.getName() + "Operaion";
        } else {
            name = method.getReturn().getName();
        }

        if (method.getSoapStyle() == SOAPBinding.Style.DOCUMENT) {
            targetNamespace = method.getReturn().getTargetNamespace();
        } else {
            targetNamespace = method.getInterface().getNamespace();
        }
         
        resultAnnotation.addArgument("name", name);
        resultAnnotation.addArgument("targetNamespace", targetNamespace);
        if (method.getSoapStyle() == SOAPBinding.Style.RPC
            || (method.getSoapStyle() == SOAPBinding.Style.DOCUMENT && !method.isWrapperStyle())) {
            resultAnnotation.addArgument("partName", method.getReturn().getName());
        }

        method.addAnnotation(resultAnnotation.toString());
    }
    
    private void addWrapperAnnotation(JavaMethod method, Operation operation) {
        if (!isWrapperStyle(operation)) {
            return;
        }

        if (wrapperRequest != null) {
            JavaAnnotation wrapperRequestAnnotation = new JavaAnnotation("RequestWrapper");
            wrapperRequestAnnotation.addArgument("localName", wrapperRequest.getType());
            wrapperRequestAnnotation.addArgument("targetNamespace", wrapperRequest.getTargetNamespace());
            wrapperRequestAnnotation.addArgument("className", wrapperRequest.getClassName());
            method.addAnnotation(wrapperRequestAnnotation.toString());
        }
        if (wrapperResponse != null) {
            JavaAnnotation wrapperResponseAnnotation = new JavaAnnotation("ResponseWrapper");
            wrapperResponseAnnotation.addArgument("localName", wrapperResponse.getType());
            wrapperResponseAnnotation.addArgument("targetNamespace", wrapperResponse.getTargetNamespace());
            wrapperResponseAnnotation.addArgument("className", wrapperResponse.getClassName());
            method.addAnnotation(wrapperResponseAnnotation.toString());
        }
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
            inputPart = inputParts.values().iterator().next();
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
            outputPart = outputParts.values().iterator().next();
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
        String userPackage = (String)env.get(ToolConstants.CFG_PACKAGENAME);

        if (inputPart != null) {
            wrapperRequest = new JavaParameter();
            wrapperRequest.setName(ProcessorUtil.resolvePartName(inputPart));
            wrapperRequest.setType(ProcessorUtil.getPartType(inputPart));
            wrapperRequest.setTargetNamespace(ProcessorUtil.resolvePartNamespace(inputPart));
            wrapperRequest.setClassName(ProcessorUtil.getFullClzName(wrapperRequest.getTargetNamespace(),
                                                                     ProcessorUtil.resolvePartType(inputPart),
                                                                     userPackage));
        }
        if (outputPart != null) {
            wrapperResponse = new JavaParameter();
            wrapperResponse.setName(ProcessorUtil.resolvePartName(outputPart));
            wrapperResponse.setType(ProcessorUtil.getPartType(outputPart));
            wrapperResponse.setTargetNamespace(ProcessorUtil.resolvePartNamespace(outputPart));
            wrapperResponse.setClassName(ProcessorUtil.getFullClzName(wrapperResponse.getTargetNamespace(),
                                                                      ProcessorUtil.
                                                                      resolvePartType(outputPart),
                                                                      userPackage));
        }
        
        return true;
    }

    private boolean isRequestResponse(Operation operation) throws ToolException {
        if (operation.getStyle() == null) {
            throw new ToolException("can't get operation style for " + operation.getName());
        }
        return OperationType.REQUEST_RESPONSE.equals(operation.getStyle());
    }
}
