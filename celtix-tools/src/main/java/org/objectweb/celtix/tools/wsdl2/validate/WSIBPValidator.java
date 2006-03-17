package org.objectweb.celtix.tools.wsdl2.validate;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.jws.soap.SOAPBinding;
import javax.wsdl.Binding;
import javax.wsdl.BindingOperation;
import javax.wsdl.Definition;
import javax.wsdl.Message;
import javax.wsdl.Operation;
import javax.wsdl.Part;
import javax.wsdl.PortType;
import javax.wsdl.extensions.soap.SOAPBody;
import javax.wsdl.extensions.soap.SOAPHeader;

import org.objectweb.celtix.tools.common.ToolException;
import org.objectweb.celtix.tools.utils.WSDLParserUtil;

public class WSIBPValidator extends AbstractValidator {
    private List<String> operationMap = new ArrayList<String>();

    public WSIBPValidator(Definition def) {
        super(def);
    }

    public boolean isValid() {
        for (Method m : getClass().getMethods()) {
            Boolean res = Boolean.TRUE;

            if (m.getName().startsWith("check")) {
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

    public boolean checkR2201() {
        for (PortType portType : WSDLParserUtil.getPortTypes(def)) {
            Iterator ite = portType.getOperations().iterator();
            while (ite.hasNext()) {
                Operation operation = (Operation)ite.next();
                if (isOverloading(operation.getName())) {
                    continue;
                }
                BindingOperation bop = WSDLParserUtil.getBindingOperation(operation, def);
                Binding binding = WSDLParserUtil.getBinding(bop, def);
                String bindingStyle = binding != null ? WSDLParserUtil.getBindingStyle(binding) : "";

                String style = "".equals(WSDLParserUtil.getSOAPOperationStyle(bop))
                    ? bindingStyle : WSDLParserUtil.getSOAPOperationStyle(bop);

                if ("DOCUMENT".equalsIgnoreCase(style)) {
                    List<Part> partsList = WSDLParserUtil.getInMessageParts(operation);
                    int inmessagePartsCount = partsList.size();
                    SOAPBody soapBody = WSDLParserUtil.getBindingInputSOAPBody(bop);
                    if (soapBody != null) {
                        List parts = soapBody.getParts();
                        int boundPartSize = parts == null ? inmessagePartsCount : parts.size();
                        SOAPHeader soapHeader = WSDLParserUtil.getBindingInputSOAPHeader(bop);
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
                                    errorMessage = "opartion: " + operation.getName() + "  soapBody parts : "
                                                   + partName + " not found in the message, wrong WSDL";
                                    return false;
                                }

                            }
                        }

                        if (boundPartSize > 1) {
                            this.errorMessage = "operation:" + operation.getName()
                                                + " more than one part bound to body";
                            return false;
                        }
                    }

                    int outmessagePartsCount = WSDLParserUtil.getOutMessageParts(operation).size();
                    soapBody = WSDLParserUtil.getBindingOutputSOAPBody(bop);
                    if (soapBody != null) {
                        List parts = soapBody.getParts();
                        int boundPartSize = parts == null ? outmessagePartsCount : parts.size();
                        SOAPHeader soapHeader = WSDLParserUtil.getBindingOutputSOAPHeader(bop);
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
                                for (Part part : WSDLParserUtil.getOutMessageParts(operation)) {
                                    if (partName.equalsIgnoreCase(part.getName())) {
                                        isDefined = true;
                                        break;
                                    }
                                }
                                if (!isDefined) {
                                    errorMessage = "opartion: " + operation.getName() + "  soapBody parts : "
                                                   + partName + " not found in the message, wrong WSDL";
                                    return false;
                                }

                            }
                        }

                        if (boundPartSize > 1) {
                            this.errorMessage = "operation:" + operation.getName()
                                                + " more than one part bound to body";
                            return false;
                        }
                    }
                }

            }
        }
        return true;

    }

    public boolean checkR2203And2204() {

        for (Iterator ite = def.getBindings().values().iterator(); ite.hasNext();) {
            Binding binding = (Binding)ite.next();

            String style = WSDLParserUtil.getCanonicalBindingStyle(binding);

            //

            for (Iterator ite2 = binding.getPortType().getOperations().iterator(); ite2.hasNext();) {
                Operation operation = (Operation)ite2.next();
                if (operation.getInput() != null && operation.getInput().getMessage() != null) {
                    Message inMess = operation.getInput().getMessage();

                    for (Iterator ite3 = inMess.getParts().values().iterator(); ite3.hasNext();) {
                        Part p = (Part)ite3.next();
                        if (style.equalsIgnoreCase(SOAPBinding.Style.RPC.name()) && p.getTypeName() == null) {
                            errorMessage = "An rpc-literal binding in a DESCRIPTION MUST refer, "
                                           + "in its soapbind:body element(s), only to "
                                           + "wsdl:part element(s) that have been defined "
                                           + "using the type attribute.";
                            return false;
                        }

                        if (style.equalsIgnoreCase(SOAPBinding.Style.DOCUMENT.name())
                            && p.getElementName() == null) {
                            errorMessage = "A document-literal binding in a DESCRIPTION MUST refer, "
                                           + "in each of its soapbind:body element(s),"
                                           + "only to wsdl:part element(s)"
                                           + " that have been defined using the element attribute.";
                            return false;
                        }

                    }
                }
                if (operation.getOutput() != null && operation.getOutput().getMessage() != null) {
                    Message outMess = operation.getOutput().getMessage();
                    for (Iterator ite3 = outMess.getParts().values().iterator(); ite3.hasNext();) {
                        Part p = (Part)ite3.next();
                        if (style.equalsIgnoreCase(SOAPBinding.Style.RPC.name()) && p.getTypeName() == null) {
                            errorMessage = "An rpc-literal binding in a DESCRIPTION MUST refer, "
                                           + "in its soapbind:body element(s), only to "
                                           + "wsdl:part element(s) that have been defined "
                                           + "using the type attribute.";
                            return false;
                        }

                        if (style.equalsIgnoreCase(SOAPBinding.Style.DOCUMENT.name())
                            && p.getElementName() == null) {
                            errorMessage = "A document-literal binding in a DESCRIPTION MUST refer, "
                                           + "in each of its soapbind:body element(s),"
                                           + "only to wsdl:part element(s)"
                                           + " that have been defined using the element attribute.";
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
            if (WSDLParserUtil.isMixedStyle(binding)) {
                this.errorMessage = "Mixted style ,Wrong WSDL";
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
