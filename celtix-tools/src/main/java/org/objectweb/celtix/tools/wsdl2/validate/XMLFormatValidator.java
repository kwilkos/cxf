package org.objectweb.celtix.tools.wsdl2.validate;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.wsdl.Binding;
import javax.wsdl.BindingOperation;
import javax.wsdl.Definition;
import javax.wsdl.Operation;

import org.objectweb.celtix.tools.extensions.xmlformat.XMLFormat;
import org.objectweb.celtix.tools.extensions.xmlformat.XMLFormatBinding;

public class XMLFormatValidator
    extends AbstractValidator {

    public XMLFormatValidator(Definition def) {
        super(def);
    }

    @Override
    public boolean isValid() {
        // TODO Auto-generated method stub
        return checkXMLBindingFormat();
    }

    private boolean checkXMLBindingFormat() {
        Collection<Binding> bindings = def.getBindings().values();
        if (bindings != null) {
            for (Binding binding : bindings) {
                Iterator it = binding.getExtensibilityElements().iterator();
                while (it.hasNext()) {
                    if (it.next() instanceof XMLFormatBinding) {
                        if (!checkXMLFormat(binding)) {
                            return false;
                        }
                    } else {
                        break;
                    }
                }
            }
        }
        return true;
    }

    private boolean checkXMLFormat(Binding binding) {
        List<BindingOperation> bos = binding.getBindingOperations();
        boolean result = true;
        boolean needRootNode = false;
        for (BindingOperation bo : bos) {
            Operation op = binding.getPortType().getOperation(bo.getName(), null, null);
            needRootNode = false;
            if (op.getInput().getMessage().getParts().size() == 0
                || op.getInput().getMessage().getParts().size() > 1) {
                needRootNode = true;
            }
            if (needRootNode) {
                String path = "Binding(" + binding.getQName().getLocalPart()
                              + "):BindingOperation(" + bo.getName() + ")";
                Iterator itIn = bo.getBindingInput().getExtensibilityElements().iterator();
                if (findXMLFormatRootNode(itIn, bo, path + "-input")) {
                    // Input check correct, continue to check output binding
                    needRootNode = false;
                    if (op.getOutput().getMessage().getParts().size() == 0
                        || op.getOutput().getMessage().getParts().size() > 1) {
                        needRootNode = true;
                    }
                    if (needRootNode) {
                        Iterator itOut = bo.getBindingInput().getExtensibilityElements().iterator();
                        result = result
                                 && findXMLFormatRootNode(itOut, bo, path + "-Output");
                        if (!result) {
                            return false;
                        }
                    }
                } else {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean findXMLFormatRootNode(Iterator it, BindingOperation bo, String errorPath) {
        while (it.hasNext()) {
            Object ext = it.next();
            if (ext instanceof XMLFormat) {
                XMLFormat xmlFormat = (XMLFormat)ext;
                String rootNodeValue = def.getPrefix(def.getTargetNamespace()) + ":" + bo.getName();
                if (xmlFormat.getRootNode().equals(rootNodeValue)) {
                    return true;
                } else {
                    this.errorMessage = errorPath
                                        + ": wrong value of rootNode attribute, the value should be "
                                        + rootNodeValue;
                    return false;
                }
            }
        }
        this.errorMessage = errorPath + ": missing xml format body element";
        return false;
    }
}
