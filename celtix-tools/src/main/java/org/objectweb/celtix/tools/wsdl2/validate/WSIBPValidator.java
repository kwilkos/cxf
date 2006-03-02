package org.objectweb.celtix.tools.wsdl2.validate;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.wsdl.BindingOperation;
import javax.wsdl.Definition;
import javax.wsdl.Operation;
import javax.wsdl.Part;
import javax.wsdl.PortType;
import javax.wsdl.extensions.soap.SOAPBody;

import org.objectweb.celtix.tools.utils.WSDLParserUtil;

public class WSIBPValidator extends AbstractValidator {
    private List<String> operationMap = new ArrayList<String>();
    public WSIBPValidator(Definition def) {
        super(def);

    }

    public boolean isValid() {
        return checkR2201();

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
                String style = WSDLParserUtil.getSOAPOperationStyle(bop).equals("") ? WSDLParserUtil
                    .getBindingStyle(bop, def) : WSDLParserUtil.getSOAPOperationStyle(bop);
                if (style.equalsIgnoreCase("DOCUMENT")) {
                    List<Part> partsList = WSDLParserUtil.getInMessageParts(operation);
                    int inmessagePartsCount = partsList.size();
                    SOAPBody soapBody = WSDLParserUtil.getBindingInputSOAPBody(bop);
                    if (soapBody != null) {
                        List parts = soapBody.getParts();
                        int boundPartSize;
                        if (parts == null) {
                            boundPartSize = inmessagePartsCount;
                        } else {
                            boundPartSize = parts.size();
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
                        int boundPartSize;
                        if (parts == null) {
                            boundPartSize = outmessagePartsCount;
                        } else {
                            boundPartSize = parts.size();
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
    
    
    private boolean isOverloading(String operationName) {
        if (operationMap.contains(operationName)) {
            return true;
        } else {
            operationMap.add(operationName);
        }
        return false;
    }

}
