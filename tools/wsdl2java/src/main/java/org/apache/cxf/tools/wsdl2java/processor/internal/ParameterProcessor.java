/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.cxf.tools.wsdl2java.processor.internal;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.jws.soap.SOAPBinding;
import javax.wsdl.Definition;
import javax.wsdl.Message;
import javax.wsdl.Part;
import javax.xml.namespace.QName;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.xml.sax.SAXException;

import com.sun.codemodel.JType;
import com.sun.tools.xjc.api.Property;

import org.apache.cxf.helpers.DOMUtils;
import org.apache.cxf.tools.common.ToolConstants;
import org.apache.cxf.tools.common.ToolContext;
import org.apache.cxf.tools.common.ToolException;
import org.apache.cxf.tools.common.WSDLConstants;
import org.apache.cxf.tools.common.model.JavaAnnotation;
import org.apache.cxf.tools.common.model.JavaMethod;
import org.apache.cxf.tools.common.model.JavaParameter;
import org.apache.cxf.tools.common.model.JavaReturn;
import org.apache.cxf.tools.common.model.JavaType;
import org.apache.cxf.tools.util.ProcessorUtil;

public class ParameterProcessor extends AbstractProcessor {
    
    private Map<String, Element> wsdlElementMap = new HashMap<String, Element>();
    private Map<String, String>  wsdlLoc = new HashMap<String, String>();
    private Definition definition;
    
    @SuppressWarnings("unchecked")
    public ParameterProcessor(ToolContext penv) {
        super(penv);
        definition = (Definition)penv.get(ToolConstants.WSDL_DEFINITION);
        wsdlLoc.put(definition.getTargetNamespace(), definition.getDocumentBaseURI());
        List<Definition> defs = (List<Definition>)penv.get(ToolConstants.IMPORTED_DEFINITION);
        for (Definition def : defs) {
            wsdlLoc.put(def.getTargetNamespace(), def.getDocumentBaseURI());
        }
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
        String namespace = ProcessorUtil.resolvePartNamespace(part, definition);
        String type = ProcessorUtil.resolvePartType(part, this.env);

        JavaParameter parameter = new JavaParameter(name, type, namespace);
        parameter.setPartName(part.getName());
        parameter.setQName(ProcessorUtil.getElementName(part));

        parameter.setClassName(ProcessorUtil.getFullClzName(part, env, this.collector, false));

        if (style == JavaType.Style.INOUT || style == JavaType.Style.OUT) {
            parameter.setHolder(true);
            parameter.setHolderName(javax.xml.ws.Holder.class.getName());
            
            parameter.setHolderClass(ProcessorUtil.getFullClzName(part, env, this.collector, true));
        }
        parameter.setStyle(style);
        
        return parameter;
    }

    private JavaParameter addParameter(JavaMethod method, JavaParameter parameter) throws ToolException {
        JavaAnnotation webParamAnnotation = new JavaAnnotation("WebParam");
        String name = parameter.getName();
        String targetNamespace = method.getInterface().getNamespace();
        String partName = null;

        if (method.getSoapStyle() == SOAPBinding.Style.DOCUMENT
            || parameter.isHeader()) {
            //headers are always DOCUMENT style
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
        if (method.getSoapStyle() == SOAPBinding.Style.DOCUMENT
            || parameter.isHeader()) {
            if (parameter.getTypeReference() != null) {
                targetNamespace = parameter.getTypeReference().tagName.getNamespaceURI();
            }
            webParamAnnotation.addArgument("targetNamespace", targetNamespace);
        }

        parameter.setAnnotation(webParamAnnotation);

        method.addParameter(parameter);

        return parameter;
    }

    private void processReturn(JavaMethod method, Part part) {
        String name = part == null ? "return" : part.getName();
        String type = part == null ? "void" : ProcessorUtil.resolvePartType(part, this.env);
 
        String namespace = part == null ? null : ProcessorUtil.resolvePartNamespace(part, definition);
              
        JavaReturn returnType = new JavaReturn(name, type, namespace);
        returnType.setQName(ProcessorUtil.getElementName(part));
        returnType.setStyle(JavaType.Style.OUT);
        if (namespace != null && type != null && !"void".equals(type)) {
            returnType.setClassName(ProcessorUtil.getFullClzName(part, env, this.collector, false));
        }
        method.setReturn(returnType);
    }

    @SuppressWarnings("unchecked")
    private void processInput(JavaMethod method, Message inputMessage) throws ToolException {
        List<Part> inputParts = getDefaultOrderParts(inputMessage);
        for (Part part : inputParts) {
            addParameter(method, getParameterFromPart(method, part, JavaType.Style.IN));
        }
    }
    
    @SuppressWarnings("unchecked")
    private List<Part> getDefaultOrderParts(Message message) {
        Map<String, Part> partsMap = message.getParts();
        List<Part> parts = new ArrayList<Part>();
        if (message.getParts().size() > 1) {
            List<String> paraOrder = getDefaultOrderPartNameList(message);
            parts = message.getOrderedParts(paraOrder);
        } else {
            Collection<Part> partsValues = partsMap.values();
            for (Part part : partsValues) {
                parts.add(part);
            }
        }
        return parts;
    }
    
    
    private Element getWSDLElement(Message message) {
        String ns = message.getQName().getNamespaceURI();
        String wsdlLocation = wsdlLoc.get(ns);
        Element wsdlElement = wsdlElementMap.get(ns);
        if (wsdlElementMap.get(ns) == null) {
            Document doc = null;
            try {
                doc = DOMUtils.createDocumentBuilder().parse(wsdlLocation);
                wsdlElement = doc.getDocumentElement();
                wsdlElementMap.put(ns, wsdlElement);
            } catch (SAXException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return wsdlElement;
    }
    
    
    private List<String> getDefaultOrderPartNameList(Message message) {
        List<String> nameList = new ArrayList<String>(); 
        Element wsdlElement = getWSDLElement(message);     
        NodeList messageNodeList = wsdlElement.getElementsByTagNameNS(message.getQName().getNamespaceURI(),
                                   WSDLConstants.QNAME_MESSAGE.getLocalPart());       
        Node messageNode = null;
        
        for (int i = 0; i < messageNodeList.getLength(); i++) {
            Node node = messageNodeList.item(i);
            if (DOMUtils.getAttribute(node, 
                                      WSDLConstants.ATTR_NAME).
                                          equals(message.getQName().getLocalPart())) {
                messageNode = node;
                break;
            }
        }
        if (messageNode == null) {
            return null;
        }
        Node partNode = DOMUtils.getChild(messageNode, Node.ELEMENT_NODE);
        nameList.add(DOMUtils.getAttribute(partNode, WSDLConstants.ATTR_NAME));
        while (partNode.getNextSibling() != null) {
            partNode = partNode.getNextSibling();
            if (partNode.getNodeType() == Node.ELEMENT_NODE) {
                nameList.add(DOMUtils.getAttribute(partNode, WSDLConstants.ATTR_NAME));
            }
        }
        
        
        return nameList;
        
        
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
        
        List<? extends Property> block = dataBinder.getBlock(part);
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
        Map<String, Part> inputPartsMap = 
            inputMessage == null ? new HashMap<String, Part>() : inputMessage.getParts();
        List<Part> outputParts = 
            outputMessage == null ? new ArrayList<Part>() : this.getDefaultOrderParts(outputMessage);
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
            processReturn(method, outParts.get(0));
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
        
        Map<String, Part> inputPartsMap = 
            inputMessage == null ? new HashMap<String, Part>() : inputMessage.getParts();
        Map<String, Part> outputPartsMap = 
            outputMessage == null ? new HashMap<String, Part>() : outputMessage.getParts();
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
        
        Part inputPart = inputParts.size() > 0 ? inputParts.iterator().next() : null;
        Part outputPart = outputParts.size() > 0 ? outputParts.iterator().next() : null;
        
        List<? extends Property> inputBlock = null;
        List<? extends Property> outputBlock = null;
        
        if (inputPart != null) {
            inputBlock = dataBinder.getBlock(inputPart);
        }       
        
        if (outputPart != null) {
            outputBlock = dataBinder.getBlock(outputPart);
        }

        if (outputBlock == null || outputBlock.size() == 0) {
            addVoidReturn(method);
            return;
        }
        method.setReturn(null);
        if (outputBlock.size() == 1 && inputBlock != null) {
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
                return;
            }
            
        }
        for (Property outElement : outputBlock) {
            if ("return".equals(outElement.elementName().getLocalPart())) {
                if (method.getReturn() != null) {
                    org.apache.cxf.common.i18n.Message msg = 
                        new org.apache.cxf.common.i18n.Message("WRAPPER_STYLE_TWO_RETURN_TYPES", LOG);
                    throw new ToolException(msg);
                }
                method.setReturn(getReturnFromProperty(outElement, outputPart));
                continue;
            }
            boolean sameWrapperChild = false;
            if (inputBlock != null) {
                for (Property inElement : inputBlock) {
                    if (isSameWrapperChild(inElement, outElement)) {
                        addParameter(method, getParameterFromProperty(outElement, JavaType.Style.INOUT,
                                                                      outputPart));
                        sameWrapperChild = true;
                        break;
                    }
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
        String targetNamespace = ProcessorUtil.resolvePartNamespace(part, definition);
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
        String targetNamespace = ProcessorUtil.resolvePartNamespace(part, definition);
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
        Map<String, Part> inputPartsMap = 
            inputMessage == null ? new HashMap<String, Part>() : inputMessage.getParts();
        Map<String, Part> outputPartsMap = 
            outputMessage == null ? new HashMap<String, Part>() : outputMessage.getParts();

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
