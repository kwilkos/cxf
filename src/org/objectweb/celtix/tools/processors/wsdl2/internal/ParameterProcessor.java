package org.objectweb.celtix.tools.processors.wsdl2.internal;

import java.util.*;
import javax.wsdl.Message;
import javax.wsdl.Part;
import javax.xml.namespace.QName;

import com.sun.codemodel.JType;
import com.sun.tools.xjc.api.Property;
import org.objectweb.celtix.tools.common.ProcessorEnvironment;
import org.objectweb.celtix.tools.common.ToolConstants;
import org.objectweb.celtix.tools.common.model.JavaAnnotation;
import org.objectweb.celtix.tools.common.model.JavaMethod;
import org.objectweb.celtix.tools.common.model.JavaParameter;
import org.objectweb.celtix.tools.common.model.JavaPort;
import org.objectweb.celtix.tools.common.model.JavaReturn;
import org.objectweb.celtix.tools.common.model.JavaType;
import org.objectweb.celtix.tools.utils.ProcessorUtil;

public class ParameterProcessor {

    private final ProcessorEnvironment env;
    
    public ParameterProcessor(ProcessorEnvironment penv) {
        this.env = penv;
    }
    
    public void process(JavaMethod method,
                        Message inputMessage,
                        Message outputMessage,
                        boolean isRequestResponse,
                        List<String> parameterOrder) throws Exception {
        
        boolean parameterOrderPresent = false;

        if (parameterOrder != null && !parameterOrder.isEmpty()) {
            parameterOrderPresent = true;
        }

        if (parameterOrderPresent
            && isValidOrdering(parameterOrder, inputMessage, outputMessage)
            && !method.isWrapperStyle()) {
            buildParamModelsWithOrdering(method,
                                         inputMessage,
                                         outputMessage,
                                         isRequestResponse,
                                         parameterOrder);
        } else {
            buildParamModelsWithoutOrdering(method,
                                            inputMessage,
                                            outputMessage,
                                            isRequestResponse);
        }
    }

    private JavaParameter getParameterFromPart(JavaMethod method,
                                               Part part,
                                               JavaType.Style style) throws Exception {
        String name = ProcessorUtil.resolvePartName(part);
        String namespace = ProcessorUtil.resolvePartNamespace(part);
        String type = ProcessorUtil.resolvePartType(part);

        JavaParameter parameter = new JavaParameter(name, type, namespace);
        String userPackage = (String) env.get(ToolConstants.CFG_PACKAGENAME);
        parameter.setClassName(ProcessorUtil.getFullClzName(namespace,
                                                            type,
                                                            method.getInterface().getPackageName(),
                                                            userPackage));

        if (style == JavaType.Style.INOUT || style == JavaType.Style.OUT) {
            parameter.setHolder(true);
            parameter.setHolderName(javax.xml.ws.Holder.class.getName());
        }
        parameter.setStyle(style);
        return parameter;
    }

    private void addParameter(JavaMethod method, JavaParameter parameter) throws Exception {
        JavaAnnotation webParamAnnotation = new JavaAnnotation("WebParam");
        String name = parameter.getName();
        String targetNamespace = method.getInterface().getNamespace();
        String partName = null;

        if (method.getSoapStyle() == JavaPort.SOAPStyle.DOCUMENT) {
            targetNamespace = parameter.getTargetNamespace();
            if (!method.isWrapperStyle()) {
                name = method.getName();
                partName = parameter.getName();
            }
        }
        
        if (method.getSoapStyle() == JavaPort.SOAPStyle.RPC) {
            partName = parameter.getName();    
        }

        if (partName != null) {
            webParamAnnotation.addArgument("partName", partName);
        }
        
        webParamAnnotation.addArgument("name", name);
        webParamAnnotation.addArgument("targetNamespace", targetNamespace);
        parameter.setAnnotation(webParamAnnotation.toString());
        
        method.addParameter(parameter);
    }
    
    private void processReturn(JavaMethod method, Part part) throws Exception {
        String name = part == null ? "return" : part.getName();
        String type = part == null ? "void" : ProcessorUtil.resolvePartType(part);
        String namespace = part == null ? null : ProcessorUtil.resolvePartNamespace(part);
        
        JavaReturn returnType = new JavaReturn(name, type, namespace);
        returnType.setStyle(JavaType.Style.OUT);
        String userPackage = (String) env.get(ToolConstants.CFG_PACKAGENAME);
        if (namespace != null && type != null && !"void".equals(type)) {
            returnType.setClassName(ProcessorUtil.getFullClzName(namespace,
                                                                 type,
                                                                 method.getInterface().getPackageName(),
                                                                 userPackage));
        }
        method.setReturn(returnType);
    }

    @SuppressWarnings("unchecked")
    private void processInput(JavaMethod method,
                              Message inputMessage) throws Exception {
        Map<String, Part> inputPartsMap = inputMessage.getParts();
        Collection<Part> inputParts = inputPartsMap.values();
        for (Part part : inputParts) {
            addParameter(method, getParameterFromPart(method, part, JavaType.Style.IN));
        }
    }


    @SuppressWarnings("unchecked")
    private void processWrappedInput(JavaMethod method,
                                     Message inputMessage) throws Exception {

        Map<String, Part> inputPartsMap = inputMessage.getParts();
        Collection<Part> inputParts = inputPartsMap.values();
        if (inputParts.size() > 1) {
            processInput(method, inputMessage);
            return;
        }
        Part part = inputParts.iterator().next();

        List<? extends Property> block = ProcessorUtil.getBlock(part, env);
        if (block != null) {
            if (block.size() == 0) {
                // complete
            }
            for (Property item : block) {
                addParameter(method, getParameterFromProperty(item, JavaType.Style.IN));
            }        
        }
    }
    
    @SuppressWarnings("unchecked")
    private void processOutput(JavaMethod method,
                               Message inputMessage,
                               Message outputMessage,
                               boolean isRequestResponse) throws Exception {
        Map<String, Part> inputPartsMap = inputMessage.getParts();
        Map<String, Part> outputPartsMap = outputMessage.getParts();
        Collection<Part> outputParts = outputPartsMap.values();

        //figure out output parts that are not present in input parts
        List<Part> outParts = new ArrayList<Part>();

        if (isRequestResponse) {
            for (Part part : outputParts) {
                Part outpart = inputPartsMap.get(part.getName());
                if (outpart == null) {
                    continue;
                } else if (isSamePart(outpart, part)) {
                    addParameter(method, getParameterFromPart(method, part, JavaType.Style.INOUT));
                    continue;
                }
                outParts.add(outpart);
            }
        }

        if (outParts.size() == 1) {
            processReturn(method, outParts.get(0));
            outParts.clear();
        } else if (isRequestResponse && outputParts.size() == 1) {
            processReturn(method, outputParts.iterator().next());
            return;
        } else {
            processReturn(method, null);
        }
        
        if (isRequestResponse) {
            for (Part part : outputParts) {
                addParameter(method, getParameterFromPart(method, part, JavaType.Style.INOUT));
            }
        }
    }

        
    @SuppressWarnings("unchecked")
    private void processWrappedOutput(JavaMethod method,
                                      Message inputMessage,
                                      Message outputMessage,
                                      boolean isRequestResponse) throws Exception {
        Map<String, Part> inputPartsMap = inputMessage.getParts();
        Map<String, Part> outputPartsMap = outputMessage.getParts();
        Collection<Part> outputParts = outputPartsMap.values();
        Collection<Part> inputParts = inputPartsMap.values();
            
        if (inputPartsMap.size() > 1 || outputPartsMap.size() > 1) {
            processOutput(method, inputMessage, outputMessage, isRequestResponse);
            return;
        }

        Part inputPart = inputParts.iterator().next();
        Part outputPart = outputParts.iterator().next();
        List<? extends Property> inputBlock = ProcessorUtil.getBlock(inputPart, env);
        List<? extends Property> outputBlock = ProcessorUtil.getBlock(outputPart, env);
            
        if (outputBlock == null || outputBlock.size() == 0) {
            addVoidReturn(method);
            return;
        }

        if (outputBlock.size() == 1) {
            Property outElement = outputBlock.iterator().next();
            method.setReturn(getReturnFromProperty(outElement));
            return;
        }
        method.setReturn(null);
        for (Property outElement : outputBlock) {
            for (Property inElement : inputBlock) {
                if (isSameWrapperChild(inElement, outElement)) {
                    addParameter(method, getParameterFromProperty(outElement, JavaType.Style.INOUT));
                } else if ("return".equals(outElement.elementName().getLocalPart())) {
                    if (method.getReturn() != null) {
                        throw new Exception("Wrapper style can not have two return types");
                    }
                    method.setReturn(getReturnFromProperty(outElement));
                } else {
                    addParameter(method, getParameterFromProperty(outElement, JavaType.Style.INOUT));
                }
            }
        }
        if (method.getReturn() == null) {
            addVoidReturn(method);
        }
    }

    private void addVoidReturn(JavaMethod method) {
        JavaReturn returnType = new JavaReturn("return", "void", null);
        method.setReturn(returnType);
    }

    private boolean isSameWrapperChild(Property in, Property out) {
        if (!in.elementName().getNamespaceURI().equals(out.elementName().getNamespaceURI())) {
            return false;
        }
        return true;
    }

    private JavaParameter getParameterFromProperty(Property property, JavaType.Style style) {
        JType t = property.type();
        String namespace = property.elementName().getNamespaceURI();
        JavaParameter parameter = new JavaParameter(property.name(), t.fullName(), namespace);
        parameter.setStyle(style);
        if (style == JavaType.Style.OUT || style == JavaType.Style.INOUT) {
            parameter.setHolder(true);
            parameter.setHolderName(javax.xml.ws.Holder.class.getName());
        }
        return parameter;
    }
            
    private JavaReturn getReturnFromProperty(Property property) {
        JType t = property.type();
        String namespace = property.elementName().getNamespaceURI();
        JavaReturn returnType = new JavaReturn(property.name(), t.fullName(), namespace);
        returnType.setStyle(JavaType.Style.OUT);
        return returnType;
    }
    
    private void buildParamModelsWithoutOrdering(JavaMethod method,
                                                 Message inputMessage,
                                                 Message outputMessage,
                                                 boolean isRequestResponse) throws Exception {
        if (inputMessage != null) {
            if (method.isWrapperStyle()) {
                processWrappedInput(method, inputMessage);
            } else {
                processInput(method, inputMessage);
            }
        }
        
        if (outputMessage == null) {
            processReturn(method, null);
        } else {
            if (method.isWrapperStyle()) {
                processWrappedOutput(method, inputMessage, outputMessage, isRequestResponse);
            } else {
                processOutput(method, inputMessage, outputMessage, isRequestResponse);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void buildParamModelsWithOrdering(JavaMethod method,
                                              Message inputMessage,
                                              Message outputMessage,
                                              boolean isRequestResponse,
                                              List<String> parameterList) throws Exception {
        Map<String, Part> inputPartsMap = inputMessage.getParts();
        Map<String, Part> outputPartsMap = outputMessage.getParts();

        Collection<Part> inputParts = inputPartsMap.values();
        Collection<Part> outputParts = outputPartsMap.values();

        List<Part> inputUnlistedParts = new ArrayList<Part>();
        List<Part> outputUnlistedParts = new ArrayList<Part>();

        for (Part part : inputParts) {
            if (!parameterList.contains(part.getName())) {
                inputUnlistedParts.add(part);
            }
        }

        if (isRequestResponse) {
            for (Part part : outputParts) {
                if (!parameterList.contains(part.getName())) {
                    Part inpart = inputMessage.getPart(part.getName());
                    if (inpart == null || (inpart != null && !isSamePart(inpart, part))) {
                        outputUnlistedParts.add(part);
                    }
                }
            }

            if (outputUnlistedParts.size() == 1) {
                processReturn(method, outputUnlistedParts.get(0));
                outputPartsMap.remove(outputUnlistedParts.get(0));
                outputUnlistedParts.clear();
            }
        }

        // now create list of paramModel with parts
        // first for the ordered list
        int index = 0;
        int size = parameterList.size();

        while (index < size) {
            String partName = parameterList.get(index);
            Part part = inputPartsMap.get(partName);
            JavaType.Style style = JavaType.Style.IN;
            if (part == null) {
                part = outputPartsMap.get(partName);
                style = JavaType.Style.INOUT;
            }
            if (part != null) {
                addParameter(method, getParameterFromPart(method, part, style));
            }
            index++;
        }
        // now from unlisted input parts
        for (Part part : inputUnlistedParts) {
            addParameter(method, getParameterFromPart(method, part, JavaType.Style.IN));
        }
        // now from unlisted output parts
        for (Part part : outputUnlistedParts) {
            addParameter(method, getParameterFromPart(method, part, JavaType.Style.INOUT));
        }
    }

    private boolean isSamePart(Part part1, Part part2) {
        QName qname1 = part1.getElementName();
        QName qname2 = part1.getElementName();
        if (qname1 != null && qname2 != null) {
            return qname1.equals(qname2);
        }
        qname1 = part1.getTypeName();
        qname2 = part1.getTypeName();
        if (qname1 != null && qname2 != null) {
            return qname1.equals(qname2);
        }
        return false;
    }
    
    @SuppressWarnings("unchecked")
    private boolean isValidOrdering(List<String> parameterOrder,
                                    Message inputMessage,
                                    Message outputMessage) {
        Iterator<String> params = parameterOrder.iterator();

        Collection<Part> inputParts = inputMessage.getParts().values();
        Collection<Part> outputParts = outputMessage.getParts().values();

        boolean partFound = false;

        while (params.hasNext()) {
            String param = params.next();
            partFound = false;
            for (Part part : inputParts) {
                if (param.equals(part.getName())) {
                    partFound = true;
                    break;
                }
            }
            // if not found, check output parts
            if (!partFound) {
                for (Part part : outputParts) {
                    if (param.equals(part.getName())) {
                        partFound = true;
                        break;
                    }
                }
            }
            if (!partFound) {
                break;
            }
        }
        return partFound;
    }

}
