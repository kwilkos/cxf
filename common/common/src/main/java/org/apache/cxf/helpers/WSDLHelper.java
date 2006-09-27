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

package org.apache.cxf.helpers;

import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.*;
import javax.jws.WebParam;
import javax.wsdl.Binding;
import javax.wsdl.BindingInput;
import javax.wsdl.BindingOperation;
import javax.wsdl.BindingOutput;
import javax.wsdl.Definition;
import javax.wsdl.Input;
import javax.wsdl.Message;
import javax.wsdl.Operation;
import javax.wsdl.Output;
import javax.wsdl.Part;
import javax.wsdl.PortType;
import javax.wsdl.extensions.soap.SOAPBinding;
import javax.wsdl.extensions.soap.SOAPBody;
import javax.wsdl.extensions.soap.SOAPHeader;
import javax.wsdl.extensions.soap.SOAPOperation;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLReader;
import javax.xml.ws.RequestWrapper;

public class WSDLHelper {

    public BindingOperation getBindingOperation(Definition def, String operationName) {
        if (operationName == null) {
            return null;
        }
        Iterator ite = def.getBindings().values().iterator();
        while (ite.hasNext()) {
            Binding binding = (Binding)ite.next();
            Iterator ite1 = binding.getBindingOperations().iterator();
            while (ite1.hasNext()) {
                BindingOperation bop = (BindingOperation)ite1.next();
                if (bop.getName().equals(operationName)) {
                    return bop;
                }
            }
        }
        return null;
    }

    public BindingOperation getBindingOperation(Binding binding, String operationName) {
        if (operationName == null) {
            return null;
        }
        List bindingOperations = binding.getBindingOperations();
        for (Iterator iter = bindingOperations.iterator(); iter.hasNext();) {
            BindingOperation bindingOperation = (BindingOperation)iter.next();
            if (operationName.equals(bindingOperation.getName())) {
                return bindingOperation;
            }
        }
        return null;
    }

    public Map getParts(Operation operation, boolean out) {
        Message message = null;
        if (out) {
            Output output = operation.getOutput();
            message = output.getMessage();
        } else {
            Input input = operation.getInput();
            message = input.getMessage();
        }
        return message.getParts() == null ? new HashMap() : message.getParts();
    }

    public javax.jws.soap.SOAPBinding getBindingAnnotationFromClass(List<Class<?>> classList) {
        javax.jws.soap.SOAPBinding sb = null;
        for (Class<?> c : classList) {
            sb = c.getAnnotation(javax.jws.soap.SOAPBinding.class);
            if (null != sb) {
                break;
            }
        }
        return sb;
    }

    public javax.jws.soap.SOAPBinding getBindingAnnotationFromMethod(Method m) {
        javax.jws.soap.SOAPBinding sb = null;
        if (null != m) {
            sb = m.getAnnotation(javax.jws.soap.SOAPBinding.class);
        }
        return sb;
    }

    public WebParam getWebParamAnnotation(Annotation[] pa) {
        WebParam wp = null;

        if (null != pa) {
            for (Annotation annotation : pa) {
                if (WebParam.class.equals(annotation.annotationType())) {
                    wp = (WebParam)annotation;
                    break;
                }
            }
        }
        return wp;
    }

    public RequestWrapper getRequestWrapperAnnotation(Method m) {
        RequestWrapper rw = null;

        if (null != m) {
            rw = m.getAnnotation(RequestWrapper.class);
        }
        return rw;
    }

    public List<PortType> getPortTypes(Definition def) {
        List<PortType> portTypes = new ArrayList<PortType>();
        Iterator ite = def.getPortTypes().values().iterator();
        while (ite.hasNext()) {
            PortType portType = (PortType)ite.next();
            portTypes.add(portType);
        }
        return portTypes;
    }

    public List<Part> getInMessageParts(Operation operation) {
        Input input = operation.getInput();
        List<Part> partsList = new ArrayList<Part>();
        if (input != null) {
            Iterator ite = input.getMessage().getParts().values().iterator();
            while (ite.hasNext()) {
                partsList.add((Part)ite.next());
            }
        }
        return partsList;
    }

    public List<Part> getOutMessageParts(Operation operation) {
        Output output = operation.getOutput();
        List<Part> partsList = new ArrayList<Part>();
        if (output != null) {
            Iterator ite = output.getMessage().getParts().values().iterator();
            while (ite.hasNext()) {
                partsList.add((Part)ite.next());
            }
        }
        return partsList;
    }

    public String getBindingStyle(Binding binding) {
        Iterator ite = binding.getExtensibilityElements().iterator();
        while (ite.hasNext()) {
            Object obj = ite.next();
            if (obj instanceof SOAPBinding) {
                return ((SOAPBinding)obj).getStyle();
            }
        }
        return "";
    }

    public Binding getBinding(BindingOperation bop, Definition def) {
        Iterator ite = def.getBindings().values().iterator();
        while (ite.hasNext()) {
            Binding binding = (Binding)ite.next();
            for (Iterator ite2 = binding.getBindingOperations().iterator(); ite2.hasNext();) {
                BindingOperation bindingOperation = (BindingOperation)ite2.next();
                if (bindingOperation.getName().equals(bop)) {
                    return binding;
                }
            }
        }
        return null;
    }

    public String getSOAPOperationStyle(BindingOperation bop) {
        String style = "";
        if (bop != null) {
            Iterator ite = bop.getExtensibilityElements().iterator();
            while (ite.hasNext()) {
                Object obj = ite.next();
                if (obj instanceof SOAPOperation) {
                    SOAPOperation soapOperation = (SOAPOperation)obj;
                    style = soapOperation.getStyle();
                    break;
                }
            }
        }
        return style;
    }

    public SOAPBody getBindingInputSOAPBody(BindingOperation bop) {
        BindingInput bindingInput = bop.getBindingInput();
        if (bindingInput != null) {
            Iterator ite = bindingInput.getExtensibilityElements().iterator();
            while (ite.hasNext()) {
                Object obj = ite.next();
                if (obj instanceof SOAPBody) {
                    return (SOAPBody)obj;
                }
            }
        }

        return null;
    }

    public SOAPHeader getBindingInputSOAPHeader(BindingOperation bop) {
        BindingInput bindingInput = bop.getBindingInput();
        if (bindingInput != null) {
            Iterator ite = bindingInput.getExtensibilityElements().iterator();
            while (ite.hasNext()) {
                Object obj = ite.next();
                if (obj instanceof SOAPHeader) {
                    return (SOAPHeader)obj;
                }
            }
        }

        return null;
    }

    public SOAPHeader getBindingOutputSOAPHeader(BindingOperation bop) {
        BindingOutput bindingOutput = bop.getBindingOutput();
        if (bindingOutput != null) {
            Iterator ite = bindingOutput.getExtensibilityElements().iterator();
            while (ite.hasNext()) {
                Object obj = ite.next();
                if (obj instanceof SOAPHeader) {
                    return (SOAPHeader)obj;
                }
            }
        }

        return null;
    }

    public SOAPBody getBindingOutputSOAPBody(BindingOperation bop) {
        BindingOutput bindingOutput = bop.getBindingOutput();
        if (bindingOutput != null) {
            Iterator ite = bindingOutput.getExtensibilityElements().iterator();
            while (ite.hasNext()) {
                Object obj = ite.next();
                if (obj instanceof SOAPBody) {
                    return (SOAPBody)obj;
                }
            }
        }

        return null;
    }

    public Definition getDefinition(File wsdlFile) throws Exception {
        WSDLFactory wsdlFactory = WSDLFactory.newInstance();
        WSDLReader reader = wsdlFactory.newWSDLReader();
        reader.setFeature("javax.wsdl.verbose", false);
        return reader.readWSDL(wsdlFile.toURL().toString());
    }

    public boolean isMixedStyle(Binding binding) {
        Iterator ite = binding.getExtensibilityElements().iterator();
        String bindingStyle = "";
        String previousOpStyle = "";
        String style = "";
        while (ite.hasNext()) {
            Object obj = ite.next();
            if (obj instanceof SOAPBinding) {
                SOAPBinding soapBinding = (SOAPBinding)obj;
                bindingStyle = soapBinding.getStyle();
                if (bindingStyle == null) {
                    bindingStyle = "";
                }
            }
        }
        Iterator ite2 = binding.getBindingOperations().iterator();
        while (ite2.hasNext()) {
            BindingOperation bop = (BindingOperation)ite2.next();
            Iterator ite3 = bop.getExtensibilityElements().iterator();
            while (ite3.hasNext()) {
                Object obj = ite3.next();

                if (obj instanceof SOAPOperation) {
                    SOAPOperation soapOperation = (SOAPOperation)obj;
                    style = soapOperation.getStyle();
                    if (style == null) {
                        style = "";
                    }

                    if ("".equals(bindingStyle) && "".equals(previousOpStyle) || "".equals(bindingStyle)
                        && previousOpStyle.equalsIgnoreCase(style)) {
                        previousOpStyle = style;

                    } else if (!"".equals(bindingStyle) && "".equals(previousOpStyle)
                               && bindingStyle.equalsIgnoreCase(style)
                               || bindingStyle.equalsIgnoreCase(previousOpStyle)
                               && bindingStyle.equalsIgnoreCase(style)) {
                        previousOpStyle = style;
                    } else if (!"".equals(bindingStyle) && "".equals(style) && "".equals(previousOpStyle)) {
                        continue;
                    } else {
                        return true;
                    }

                }

            }
        }

        return false;

    }

    public String getCanonicalBindingStyle(Binding binding) {
        String bindingStyle = getBindingStyle(binding);
        if (bindingStyle != null && !("".equals(bindingStyle))) {
            return bindingStyle;
        }
        for (Iterator ite2 = binding.getBindingOperations().iterator(); ite2.hasNext();) {
            BindingOperation bindingOp = (BindingOperation)ite2.next();
            String bopStyle = getSOAPOperationStyle(bindingOp);
            if (!"".equals(bopStyle)) {
                return bopStyle;
            }
        }
        return "";

    }
}
