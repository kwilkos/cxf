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

package org.apache.cxf.tools.validator.internal;

import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.jws.soap.SOAPBinding;
import javax.wsdl.Binding;
import javax.wsdl.BindingOperation;
import javax.wsdl.Definition;
import javax.wsdl.Fault;
import javax.wsdl.Message;
import javax.wsdl.Operation;
import javax.wsdl.Part;
import javax.wsdl.PortType;

import org.apache.cxf.common.util.CollectionUtils;
import org.apache.cxf.common.util.StringUtils;
import org.apache.cxf.helpers.WSDLHelper;
import org.apache.cxf.tools.common.ToolException;
import org.apache.cxf.tools.common.extensions.soap.SoapBody;
import org.apache.cxf.tools.common.extensions.soap.SoapFault;
import org.apache.cxf.tools.common.extensions.soap.SoapHeader;
import org.apache.cxf.tools.util.SOAPBindingUtil;

public class WSIBPValidator extends AbstractDefinitionValidator {
    private List<String> operationMap = new ArrayList<String>();
    private WSDLHelper wsdlHelper = new WSDLHelper();
    
    public WSIBPValidator(Definition def) {
        super(def);
    }

    public boolean isValid() {
        for (Method m : getClass().getMethods()) {
            Boolean res = Boolean.TRUE;

            if (m.getName().startsWith("check") || m.getModifiers() == Member.PUBLIC) {
                try {
                    res = (Boolean)m.invoke(this, new Object[] {});
                } catch (Exception e) {
                    e.printStackTrace();
                    throw new ToolException(e);
                }
                if (!res.booleanValue()) {
                    return false;
                }
            }
        }
        return true;

    }

    private boolean checkR2716(final BindingOperation bop) {
        SoapBody inSoapBody = SOAPBindingUtil.getBindingInputSOAPBody(bop);
        SoapBody outSoapBody = SOAPBindingUtil.getBindingOutputSOAPBody(bop);
        if (inSoapBody != null && !StringUtils.isEmpty(inSoapBody.getNamespaceURI())
            || outSoapBody != null && !StringUtils.isEmpty(outSoapBody.getNamespaceURI())) {
            addErrorMessage("Violate WSI-BP-1.0 R2716 operation '"
                            + bop.getName() + "' soapBody MUST NOT have namespace attribute");
            return false;
        }

        SoapHeader inSoapHeader = SOAPBindingUtil.getBindingInputSOAPHeader(bop);
        SoapHeader outSoapHeader = SOAPBindingUtil.getBindingOutputSOAPHeader(bop);
        if (inSoapHeader != null && !StringUtils.isEmpty(inSoapHeader.getNamespaceURI())
            || outSoapHeader != null && !StringUtils.isEmpty(outSoapHeader.getNamespaceURI())) {
            addErrorMessage("Violate WSI-BP-1.0 R2716 operation '"
                            + bop.getName() + "' soapHeader MUST NOT have namespace attribute");
            return false;
        }

        List<SoapFault> soapFaults = SOAPBindingUtil.getBindingOperationSoapFaults(bop);
        for (SoapFault fault : soapFaults) {
            if (!StringUtils.isEmpty(fault.getNamespaceURI())) {
                addErrorMessage("Violate WSI-BP-1.0 R2716 operation '"
                                + bop.getName() + "' soapFault MUST NOT have namespace attribute");
                return false;
            }
        }
        return true;
    }

    private boolean checkR2717AndR2726(final BindingOperation bop) {
        SoapBody inSoapBody = SOAPBindingUtil.getBindingInputSOAPBody(bop);
        SoapBody outSoapBody = SOAPBindingUtil.getBindingOutputSOAPBody(bop);
        if (inSoapBody != null && StringUtils.isEmpty(inSoapBody.getNamespaceURI())
            || outSoapBody != null && StringUtils.isEmpty(outSoapBody.getNamespaceURI())) {
            addErrorMessage("Violate WSI-BP-1.0 R2717 operation '"
                            + bop.getName() + "' soapBody MUST have namespace attribute");
            return false;
        }

        SoapHeader inSoapHeader = SOAPBindingUtil.getBindingInputSOAPHeader(bop);
        SoapHeader outSoapHeader = SOAPBindingUtil.getBindingOutputSOAPHeader(bop);
        if (inSoapHeader != null && !StringUtils.isEmpty(inSoapHeader.getNamespaceURI())
            || outSoapHeader != null && !StringUtils.isEmpty(outSoapHeader.getNamespaceURI())) {
            addErrorMessage("Violate WSI-BP-1.0 R2726 operation '"
                            + bop.getName() + "' soapHeader MUST NOT have namespace attribute");
            return false;
        }

        List<SoapFault> soapFaults = SOAPBindingUtil.getBindingOperationSoapFaults(bop);
        for (SoapFault fault : soapFaults) {
            if (!StringUtils.isEmpty(fault.getNamespaceURI())) {
                addErrorMessage("Violate WSI-BP-1.0 R2726 operation '"
                                + bop.getName() + "' soapFault MUST NOT have namespace attribute");
                return false;
            }
        }
        return true;
    }

    private boolean checkR2201Input(final Operation operation,
                                    final BindingOperation bop) {
        List<Part> partsList = wsdlHelper.getInMessageParts(operation);
        int inmessagePartsCount = partsList.size();
        SoapBody soapBody = SOAPBindingUtil.getBindingInputSOAPBody(bop);
        if (soapBody != null) {
            List parts = soapBody.getParts();
            int boundPartSize = parts == null ? inmessagePartsCount : parts.size();
            SoapHeader soapHeader = SOAPBindingUtil.getBindingInputSOAPHeader(bop);
            boundPartSize = soapHeader != null 
                && soapHeader.getMessage().equals(
                    operation.getInput().getMessage()
                    .getQName())
                ? boundPartSize - 1 : boundPartSize;
            
            if (parts != null) {
                Iterator partsIte = parts.iterator();
                while (partsIte.hasNext()) {
                    String partName = (String)partsIte.next();
                    boolean isDefined = false;
                    for (Part part : partsList) {
                        if (partName.equalsIgnoreCase(part.getName())) {
                            isDefined = true;
                            break;
                        }
                    }
                    if (!isDefined) {
                        addErrorMessage("Violate WSI-BP-1.0 R2201 operation '"
                                        + operation.getName() + "' soapBody parts : "
                                        + partName + " not found in the message, wrong WSDL");
                        return false;
                    }
                }
            } else {
                if (partsList.size() > 1) {
                    addErrorMessage("Violate WSI-BP-1.0 R2210:  operation '" + operation.getName()
                                    + "' more than one part bound to body");
                    return false;
                }
            }
            
            
            if (boundPartSize > 1) {
                addErrorMessage("Violate WSI-BP-1.0  R2201 operation '" + operation.getName()
                                + "' more than one part bound to body");
                return false;
            }
        }
        return true;
    }

    private boolean checkR2201Output(final Operation operation,
                                     final BindingOperation bop) {
        int outmessagePartsCount = wsdlHelper.getOutMessageParts(operation).size();
        SoapBody soapBody = SOAPBindingUtil.getBindingOutputSOAPBody(bop);
        if (soapBody != null) {
            List parts = soapBody.getParts();
            int boundPartSize = parts == null ? outmessagePartsCount : parts.size();
            SoapHeader soapHeader = SOAPBindingUtil.getBindingOutputSOAPHeader(bop);
            boundPartSize = soapHeader != null 
                && soapHeader.getMessage().equals(
                    operation.getOutput().getMessage()
                    .getQName())
                ? boundPartSize - 1 : boundPartSize;
            if (parts != null) {
                Iterator partsIte = parts.iterator();
                while (partsIte.hasNext()) {
                    String partName = (String)partsIte.next();
                    boolean isDefined = false;
                    for (Part part : wsdlHelper.getOutMessageParts(operation)) {
                        if (partName.equalsIgnoreCase(part.getName())) {
                            isDefined = true;
                            break;
                        }
                    }
                    if (!isDefined) {
                        addErrorMessage("Violate WSI-BP-1.0 R2201 operation '"
                                        + operation.getName() + "' soapBody parts : "
                                        + partName + " not found in the message, wrong WSDL");
                        return false;
                    }

                }
            } else {
                if (wsdlHelper.getOutMessageParts(operation).size() > 1) {
                    addErrorMessage("Violate WSI-BP-1.0 R2210:  operation '" + operation.getName()
                                    + "' more than one part bound to body");
                    return false;
                }
            }

            if (boundPartSize > 1) {
                addErrorMessage("Violate WSI-BP-1.0 R2201 operation '" + operation.getName()
                                + "' more than one part bound to body");
                return false;
            }
        }
        return true;
    }
    
    public boolean checkBinding() {
        for (PortType portType : wsdlHelper.getPortTypes(def)) {
            Iterator ite = portType.getOperations().iterator();
            while (ite.hasNext()) {
                Operation operation = (Operation)ite.next();
                if (isOverloading(operation.getName())) {
                    continue;
                }
                BindingOperation bop = wsdlHelper.getBindingOperation(def, operation.getName());
                Binding binding = wsdlHelper.getBinding(bop, def);
                String bindingStyle = binding != null ? SOAPBindingUtil.getBindingStyle(binding) : "";

                String style = "".equals(SOAPBindingUtil.getSOAPOperationStyle(bop))
                    ? bindingStyle : SOAPBindingUtil.getSOAPOperationStyle(bop);
                if ("DOCUMENT".equalsIgnoreCase(style)) {
                    boolean passed = checkR2201Input(operation, bop)
                        && checkR2201Output(operation, bop)
                        && checkR2716(bop);
                    if (!passed) {
                        return false;
                    }
                } else {
                    if (!checkR2717AndR2726(bop)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    public boolean checkR2203And2204() {

        for (Iterator ite = def.getBindings().values().iterator(); ite.hasNext();) {
            Binding binding = (Binding)ite.next();

            String style = SOAPBindingUtil.getCanonicalBindingStyle(binding);

            //

            for (Iterator ite2 = binding.getPortType().getOperations().iterator(); ite2.hasNext();) {
                Operation operation = (Operation)ite2.next();
                if (operation.getInput() != null && operation.getInput().getMessage() != null) {
                    Message inMess = operation.getInput().getMessage();

                    for (Iterator ite3 = inMess.getParts().values().iterator(); ite3.hasNext();) {
                        Part p = (Part)ite3.next();
                        if (style.equalsIgnoreCase(SOAPBinding.Style.RPC.name()) && p.getTypeName() == null) {
                            addErrorMessage("An rpc-literal binding in a DESCRIPTION MUST refer, "
                                            + "in its soapbind:body element(s), only to "
                                            + "wsdl:part element(s) that have been defined "
                                            + "using the type attribute.");
                            return false;
                        }

                        if (style.equalsIgnoreCase(SOAPBinding.Style.DOCUMENT.name())
                            && p.getElementName() == null) {
                            addErrorMessage("A document-literal binding in a DESCRIPTION MUST refer, "
                                            + "in each of its soapbind:body element(s),"
                                            + "only to wsdl:part element(s)"
                                            + " that have been defined using the element attribute.");
                            return false;
                        }

                    }
                }
                if (operation.getOutput() != null && operation.getOutput().getMessage() != null) {
                    Message outMess = operation.getOutput().getMessage();
                    for (Iterator ite3 = outMess.getParts().values().iterator(); ite3.hasNext();) {
                        Part p = (Part)ite3.next();
                        if (style.equalsIgnoreCase(SOAPBinding.Style.RPC.name()) && p.getTypeName() == null) {
                            addErrorMessage("An rpc-literal binding in a DESCRIPTION MUST refer, "
                                            + "in its soapbind:body element(s), only to "
                                            + "wsdl:part element(s) that have been defined "
                                            + "using the type attribute.");
                            return false;
                        }

                        if (style.equalsIgnoreCase(SOAPBinding.Style.DOCUMENT.name())
                            && p.getElementName() == null) {
                            addErrorMessage("A document-literal binding in a DESCRIPTION MUST refer, "
                                            + "in each of its soapbind:body element(s),"
                                            + "only to wsdl:part element(s)"
                                            + " that have been defined using the element attribute.");
                            return false;
                        }

                    }
                }
            }

        }
        return true;
    }

    // TODO: Should also check SoapHeader/SoapHeaderFault
    @SuppressWarnings("unchecked")
    public boolean checkR2205() {
        for (Iterator ite = def.getBindings().values().iterator(); ite.hasNext();) {
            Binding binding = (Binding)ite.next();

            if (!SOAPBindingUtil.isSOAPBinding(binding)) {
                System.err.println("NOT SOAPBINDING");
                continue;
            }

            for (Iterator ite2 = binding.getPortType().getOperations().iterator(); ite2.hasNext();) {
                Operation operation = (Operation)ite2.next();
                Collection<Fault> faults = operation.getFaults().values();
                if (CollectionUtils.isEmpty(faults)) {
                    continue;
                }

                for (Fault fault : faults) {
                    Message message = fault.getMessage();
                    System.err.println("message:" + message.getQName());                    
                    Collection<Part> parts = message.getParts().values();
                    for (Part part : parts) {
                        System.err.println("Part: " + part.getElementName());
                        if (part.getElementName() == null) {
                            addErrorMessage("Violate WSI-BP-1.0 R2205: In Message " + message.getQName()
                                            + ", part " + part.getName()
                                            + " must specify a 'element' attribute");
                            return false;
                        }
                    }
                }
            }
        }
        return true;
    }

    public boolean checkR2705() {
        Iterator ite = def.getBindings().values().iterator();
        while (ite.hasNext()) {
            Object obj = ite.next();
            Binding binding = (Binding)obj;
            if (SOAPBindingUtil.isMixedStyle(binding)) {
                addErrorMessage("Mixed style, invalid WSDL");
                return false;
            }
        }
        return true;
    }

    private boolean isOverloading(String operationName) {
        if (operationMap.contains(operationName)) {
            return true;
        } else {
            operationMap.add(operationName);
        }
        return false;
    }

}
