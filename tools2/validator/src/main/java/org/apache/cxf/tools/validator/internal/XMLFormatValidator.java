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

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import javax.wsdl.Binding;
import javax.wsdl.BindingOperation;
import javax.wsdl.Definition;
import javax.wsdl.Operation;
import javax.xml.namespace.QName;

import org.apache.cxf.tools.common.extensions.xmlformat.XMLFormat;
import org.apache.cxf.tools.common.extensions.xmlformat.XMLFormatBinding;

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

    @SuppressWarnings("unchecked")
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

    @SuppressWarnings("unchecked")
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
                // String rootNodeValue = def.getPrefix(def.getTargetNamespace()) + ":" + bo.getName();
                QName rootNodeName = new QName(def.getTargetNamespace(), bo.getName());
                if (xmlFormat.getRootNode() != null) {
                    if (xmlFormat.getRootNode().equals(rootNodeName)) {
                        return true;
                    } else {
                        addErrorMessage(errorPath
                                        + ": wrong value of rootNode attribute, the value should be "
                                        + rootNodeName);
                        return false;
                    }
                } else {                    
                    addErrorMessage(errorPath
                                    + ": empty value of rootNode attribute, the value should be "
                                    + rootNodeName);
                    return false;                    
                }
            }
        }
        addErrorMessage(errorPath + ": missing xml format body element");
        return false;
    }
}
