package org.objectweb.celtix.tools.processors.wsdl2.validators;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.wsdl.Binding;
import javax.wsdl.BindingOperation;
import javax.wsdl.Definition;
import javax.wsdl.extensions.ExtensibilityElement;
import javax.wsdl.extensions.mime.MIMEContent;
import javax.wsdl.extensions.mime.MIMEMultipartRelated;
import javax.wsdl.extensions.mime.MIMEPart;
import javax.wsdl.extensions.soap.SOAPBody;

public class MIMEBindingValidator
    extends AbstractValidator {

    public MIMEBindingValidator(Definition def) {
        super(def);
    }

    public boolean isValid() {
        Iterator itBinding = def.getBindings().keySet().iterator();
        while (itBinding.hasNext()) {
            Binding binding = (Binding)def.getBindings().get(itBinding.next());
            Iterator itOperation = binding.getBindingOperations().iterator();
            while (itOperation.hasNext()) {
                BindingOperation bindingOperation = (BindingOperation)itOperation.next();
                Iterator itInputExt = bindingOperation.getBindingInput().getExtensibilityElements()
                    .iterator();
                while (itInputExt.hasNext()) {
                    ExtensibilityElement extElement = (ExtensibilityElement)itInputExt.next();
                    if (extElement instanceof MIMEMultipartRelated) {
                        Iterator itMimeParts = ((MIMEMultipartRelated)extElement).getMIMEParts()
                            .iterator();
                        if (!doValidate(itMimeParts, bindingOperation.getName())) {
                            return false;
                        }
                    }
                }
            }
        }
        return true;
    }

    private boolean doValidate(Iterator mimeParts, String operationName) {
        boolean gotRootPart = false;        
        while (mimeParts.hasNext()) {
            MIMEPart mPart = (MIMEPart)mimeParts.next();
            List<MIMEContent> mimeContents = new ArrayList<MIMEContent>();
            Iterator extns = mPart.getExtensibilityElements().iterator();
            while (extns.hasNext()) {
                ExtensibilityElement extElement = (ExtensibilityElement)extns.next();
                if (extElement instanceof SOAPBody) {
                    if (gotRootPart) {
                        addErrorMessage("Operation("
                                        + operationName
                                        + "): There's more than one soap body mime part" 
                                        + " in its binding input");
                        return false;
                    }
                    gotRootPart = true;
                } else if (extElement instanceof MIMEContent) {
                    mimeContents.add((MIMEContent)extElement);
                }
            }
            if (!doValidateMimeContentPartNames(mimeContents.iterator(), operationName)) {
                return false;
            }
        }
        if (!gotRootPart) {
            addErrorMessage("Operation("
                            + operationName
                            + "): There's no soap body in mime part" 
                            + " in its binding input");
            return false;            
        }
        return true;
    }

    private boolean doValidateMimeContentPartNames(Iterator mimeContents, String operationName) {
        // validate mime:content(s) in the mime:part as per R2909
        String partName = null;
        while (mimeContents.hasNext()) {
            MIMEContent mimeContent = (MIMEContent)mimeContents.next();
            String mimeContnetPart = mimeContent.getPart();
            if (mimeContnetPart == null) {
                addErrorMessage("Operation("
                                + operationName
                                + "): Must provide part attribute value for meme:content elements");
                return false;
            } else {
                if (partName == null) {
                    partName = mimeContnetPart;
                } else {
                    if (!partName.equals(mimeContnetPart)) {
                        addErrorMessage("Operation("
                                        + operationName
                                        + "): Part attribute value for meme:content " 
                                        + "elements are different");
                        return false;
                    }
                }
            }
        }
        return true;
    }
}
