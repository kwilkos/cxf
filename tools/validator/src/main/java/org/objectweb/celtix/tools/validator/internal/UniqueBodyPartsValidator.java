package org.objectweb.celtix.tools.validator.internal;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.wsdl.Definition;
import javax.wsdl.Message;
import javax.wsdl.Operation;
import javax.wsdl.Part;
import javax.wsdl.PortType;
import javax.xml.namespace.QName;

public class UniqueBodyPartsValidator extends AbstractValidator {
    private Map<QName, String> uniqueBodyPartsMap = new HashMap<QName, String>();

    public UniqueBodyPartsValidator(Definition def) {
        super(def);

    }

    public boolean isValid() {
        Iterator ite = def.getPortTypes().values().iterator();
        while (ite.hasNext()) {
            PortType portType = (PortType)ite.next();
            Iterator ite2 = portType.getOperations().iterator();
            while (ite2.hasNext()) {
                Operation operation = (Operation)ite2.next();
                if (operation.getInput() != null) {
                    Message inMessage = operation.getInput().getMessage();
                    if (inMessage != null && !isUniqueBodyPart(operation.getName(), inMessage)) {
                        return false;
                    }
                }
            }
        }
        return true;

    }

    private boolean isUniqueBodyPart(String operationName, Message msg) {
        Map partsMap = msg.getParts();
        Iterator ite = partsMap.values().iterator();
        if (ite.hasNext()) {
            Part part = (Part)ite.next();
            if (part.getElementName() == null) {
                return true;
            }
            String opName = getOperationNameWithSamePart(operationName, part);
            if (opName != null) {
                addErrorMessage("Non unique body parts, operation " + "[ " + opName + " ] "
                                + "and  operation [ " + operationName + "] have the same body block "
                                + part.getElementName());
                return false;
            }
        }
        return true;
    }

    private String getOperationNameWithSamePart(String operationName, Part part) {
        QName partQN = part.getElementName();
        String opName = uniqueBodyPartsMap.get(partQN);
        if (opName == null) {
            uniqueBodyPartsMap.put(partQN, operationName);
            return null;
        } else {
            return opName;
        }
    }

}
