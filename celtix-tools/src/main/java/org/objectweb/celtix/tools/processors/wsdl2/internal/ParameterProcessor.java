package org.objectweb.celtix.tools.processors.wsdl2.internal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.jws.soap.SOAPBinding;
import javax.wsdl.Message;
import javax.wsdl.Part;
import javax.xml.namespace.QName;

import com.sun.codemodel.JType;
import com.sun.tools.xjc.api.Property;

import org.objectweb.celtix.tools.common.ProcessorEnvironment;
import org.objectweb.celtix.tools.common.ToolException;
import org.objectweb.celtix.tools.common.model.JavaAnnotation;
import org.objectweb.celtix.tools.common.model.JavaMethod;
import org.objectweb.celtix.tools.common.model.JavaParameter;
import org.objectweb.celtix.tools.common.model.JavaReturn;
import org.objectweb.celtix.tools.common.model.JavaType;
import org.objectweb.celtix.tools.utils.ProcessorUtil;

public class ParameterProcessor extends AbstractProcessor {

    public ParameterProcessor(ProcessorEnvironment penv) {
        super(penv);
    }

    public void process(JavaMethod method, Message inputMessage, Message outputMessage,
                        boolean isRequestResponse, List<String> parameterOrder) throws ToolException {

        boolean parameterOrderPresent = false;

        if (parameterOrder != null && !parameterOrder.isEmpty()) {
            parameterOrderPresent = true;
        }

        if (parameterOrderPresent && isValidOrdering(parameterOrder, inputMessage, outputMessage)
            && !method.isWrapperStyle()) {
            buildParamModelsWithOrdering(method, inputMessage, outputMessage, isRequestResponse,
                                         parameterOrder);
        } else {
            buildParamModelsWithoutOrdering(method, inputMessage, outputMessage, isRequestResponse);
        }
    }

    /**
     * This method will be used by binding processor to change existing
     * generated java method of porttype
     * 
     * @param method
     * @param part
     * @param style
     * @throws ToolException
     */
    public JavaParameter addParameterFromBinding(JavaMethod method, Part part, JavaType.Style style)
        throws ToolException {
        return addParameter(method, getParameterFromPart(method, part, style));
    }

    private JavaParameter getParameterFromPart(JavaMethod method, Part part, JavaType.Style style) {
        String name = ProcessorUtil.resolvePartName(part);
        String namespace = ProcessorUtil.resolvePartNamespace(part);
        String type = ProcessorUtil.resolvePartType(part, this.env);

        JavaParameter parameter = new JavaParameter(name, type, namespace);
        parameter.setPartName(part.getName());
        parameter.setQName(ProcessorUtil.getElementName(part));

        parameter.setClassName(ProcessorUtil.getFullClzName(part, env, this.collector));

        if (style == JavaType.Style.INOUT || style == JavaType.Style.OUT) {
            parameter.setHolder(true);
            parameter.setHolderName(javax.xml.ws.Holder.class.getName());
            parameter.setHolderClass(ProcessorUtil.getFullClzName(part, env, true, this.collector));
        }
        parameter.setStyle(style);
        return parameter;
    }

    private JavaParameter addParameter(JavaMethod method, JavaParameter parameter) throws ToolException {
        JavaAnnotation webParamAnnotation = new JavaAnnotation("WebParam");
        String name = parameter.getName();
        String targetNamespace = method.getInterface().getNamespace();
        String partName = null;

        if (method.getSoapStyle() == SOAPBinding.Style.DOCUMENT) {
            targetNamespace = parameter.getTargetNamespace();
            if (parameter.getQName() != null) {
                name = parameter.getQName().getLocalPart();
            }
            if (!method.isWrapperStyle()) {
                partName = parameter.getPartName();
            }
        }

        if (method.getSoapStyle() == SOAPBinding.Style.RPC) {
            name = parameter.getPartName();
            partName = parameter.getPartName();
        }

        if (partName != null) {
            webParamAnnotation.addArgument("partName", partName);
        }
        if (parameter.getStyle() == JavaType.Style.OUT || parameter.getStyle() == JavaType.Style.INOUT) {
            webParamAnnotation.addArgument("mode", "Mode." + parameter.getStyle().toString(), "");
        }
        webParamAnnotation.addArgument("name", name);
        if (method.getSoapStyle() == SOAPBinding.Style.DOCUMENT) {
            webParamAnnotation.addArgument("targetNamespace", targetNamespace);
        }

        parameter.setAnnotation(webParamAnnotation);

        method.addParameter(parameter);

        return parameter;
    }

    private void processReturn(JavaMethod method, Part part) {
        String name = part == null ? "return" : part.getName();
        String type = part == null ? "void" : ProcessorUtil.resolvePartType(part, this.env);
        String namespace = part == null ? null : ProcessorUtil.resolvePartNamespace(part);

        JavaReturn returnType = new JavaReturn(name, type, namespace);
        returnType.setQName(ProcessorUtil.getElementName(part));
        returnType.setStyle(JavaType.Style.OUT);
        if (namespace != null && type != null && !"void".equals(type)) {
            returnType.setClassName(ProcessorUtil.getFullClzName(part, env, this.collector));
        }
        method.setReturn(returnType);
    }

    @SuppressWarnings("unchecked")
    private void processInput(JavaMethod method, Message inputMessage) throws ToolException {
        Map<String, Part> inputPartsMap = inputMessage.getParts();
        Collection<Part> inputParts = inputPartsMap.values();
        for (Part part : inputParts) {
            addParameter(method, getParameterFromPart(method, part, JavaType.Style.IN));
        }
    }

    @SuppressWarnings("unchecked")
    private void processWrappedInput(JavaMethod method, Message inputMessage) throws ToolException {
        Map<String, Part> inputPartsMap = inputMessage.getParts();
        Collection<Part> inputParts = inputPartsMap.values();
        if (inputParts.size() > 1) {
            processInput(method, inputMessage);
            return;
        } else if (inputParts.isEmpty()) {
            return;
        }
        Part part = inputParts.iterator().next();

        List<? extends Property> block = ProcessorUtil.getBlock(part, env);
        if (block != null) {
            if (block.size() == 0) {
                // complete
            }
            for (Property item : block) {
                addParameter(method, getParameterFromProperty(item, JavaType.Style.IN, part));
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void processOutput(JavaMethod method, Message inputMessage, Message outputMessage,
                               boolean isRequestResponse) throws ToolException {
        Map<String, Part> inputPartsMap = inputMessage.getParts();
        Map<String, Part> outputPartsMap = outputMessage.getParts();
        Collection<Part> outputParts = outputPartsMap.values();
        // figure out output parts that are not present in input parts
        List<Part> outParts = new ArrayList<Part>();
        if (isRequestResponse) {

            for (Part outpart : outputParts) {
                Part inpart = inputPartsMap.get(outpart.getName());
                if (inpart == null) {
                    outParts.add(outpart);
                    continue;
                } else if (isSamePart(inpart, outpart)) {
                    addParameter(method, getParameterFromPart(method, outpart, JavaType.Style.INOUT));
                    continue;
                } else if (!isSamePart(inpart, outpart)) {
                    outParts.add(outpart);
                    continue;
                }
                // outParts.add(outpart);
            }
        }

        if (isRequestResponse && outParts.size() == 1) {
            processReturn(method, outputParts.iterator().next());
            return;
        } else {
            processReturn(method, null);
        }
        if (isRequestResponse) {
            for (Part part : outParts) {
                addParameter(method, getParameterFromPart(method, part, JavaType.Style.OUT));
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void processWrappedOutput(JavaMethod method, Message inputMessage, Message outputMessage,
                                      boolean isRequestResponse) throws ToolException {
        Map<String, Part> inputPartsMap = inputMessage.getParts();
        Map<String, Part> outputPartsMap = outputMessage.getParts();
        Collection<Part> outputParts = outputPartsMap.values();
        Collection<Part> inputParts = inputPartsMap.values();

        if (inputPartsMap.size() > 1 || outputPartsMap.size() > 1) {
            processOutput(method, inputMessage, outputMessage, isRequestResponse);
            return;
        }
        if (outputParts.size() == 0) {
            addVoidReturn(method);
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
        method.setReturn(null);
        if (outputBlock.size() == 1) {
            Property outElement = outputBlock.iterator().next();
            boolean sameWrapperChild = false;
            for (Property inElement : inputBlock) {
                if (isSameWrapperChild(inElement, outElement)) {
                    addParameter(method, getParameterFromProperty(outElement, JavaType.Style.INOUT,
                                                                  outputPart));
                    sameWrapperChild = true;
                    if (method.getReturn() == null) {
                        addVoidReturn(method);
                    }
                    break;
                }
            }
            if (!sameWrapperChild) {
                method.setReturn(getReturnFromProperty(outElement, outputPart));
            }
            return;
        }
        method.setReturn(null);
        for (Property outElement : outputBlock) {
            if ("return".equals(outElement.elementName().getLocalPart())) {
                if (method.getReturn() != null) {
                    org.objectweb.celtix.common.i18n.Message msg = 
                        new org.objectweb.celtix.common.i18n.Message("WRAPPER_STYLE_TWO_RETURN_TYPES", LOG);
                    throw new ToolException(msg);
                }
                method.setReturn(getReturnFromProperty(outElement, outputPart));
                continue;
            }
            boolean sameWrapperChild = false;
            for (Property inElement : inputBlock) {
                if (isSameWrapperChild(inElement, outElement)) {
                    addParameter(method, getParameterFromProperty(outElement, JavaType.Style.INOUT,
                                                                  outputPart));
                    sameWrapperChild = true;
                    break;
                }
            }
            if (!sameWrapperChild) {
                addParameter(method, getParameterFromProperty(outElement, JavaType.Style.OUT, outputPart));
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
        if (!in.name().equals(out.name())) {
            return false;
        }
        if (!in.type().fullName().equals(out.type().fullName())) {
            return false;
        }
        if (!in.elementName().getNamespaceURI().equals(out.elementName().getNamespaceURI())) {
            return false;
        }
        return true;
    }

    private JavaParameter getParameterFromProperty(Property property, JavaType.Style style, Part part) {
        JType t = property.type();
        String targetNamespace = ProcessorUtil.resolvePartNamespace(part);
        if (targetNamespace == null) {
            targetNamespace = property.elementName().getNamespaceURI();
        }
        JavaParameter parameter = new JavaParameter(property.name(), t.fullName(), targetNamespace);
        parameter.setStyle(style);
        parameter.setQName(property.elementName());
        if (style == JavaType.Style.OUT || style == JavaType.Style.INOUT) {
            parameter.setHolder(true);
            parameter.setHolderName(javax.xml.ws.Holder.class.getName());
            parameter.setHolderClass(t.boxify().fullName());
        }
        return parameter;
    }

    private JavaReturn getReturnFromProperty(Property property, Part part) {
        JType t = property.type();
        String targetNamespace = ProcessorUtil.resolvePartNamespace(part);
        if (targetNamespace == null) {
            targetNamespace = property.elementName().getNamespaceURI();
        }
        JavaReturn returnType = new JavaReturn(property.name(), t.fullName(), targetNamespace);
        returnType.setQName(property.elementName());
        returnType.setStyle(JavaType.Style.OUT);
        return returnType;
    }

    private void buildParamModelsWithoutOrdering(JavaMethod method, Message inputMessage,
                                                 Message outputMessage, boolean isRequestResponse)
        throws ToolException {
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
    private void buildParamModelsWithOrdering(JavaMethod method, Message inputMessage, Message outputMessage,
                                              boolean isRequestResponse, List<String> parameterList)
        throws ToolException {
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
            } else {
                processReturn(method, null);
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
                style = JavaType.Style.OUT;
            } else if (outputPartsMap.get(partName) != null 
                && isSamePart(part, outputPartsMap.get(partName))) {
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
        QName qname2 = part2.getElementName();
        if (qname1 != null && qname2 != null) {
            return qname1.equals(qname2);
        }
        qname1 = part1.getTypeName();
        qname2 = part2.getTypeName();
        if (qname1 != null && qname2 != null) {
            return qname1.equals(qname2);
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    private boolean isValidOrdering(List<String> parameterOrder, 
                                    Message inputMessage, Message outputMessage) {
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
