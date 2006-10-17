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

package org.apache.cxf.tools.java2wsdl.generator;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.jws.soap.SOAPBinding;
import javax.wsdl.Binding;
import javax.wsdl.BindingFault;
import javax.wsdl.BindingInput;
import javax.wsdl.BindingOperation;
import javax.wsdl.BindingOutput;
import javax.wsdl.Definition;
import javax.wsdl.WSDLException;
import javax.wsdl.extensions.ExtensionRegistry;
import javax.xml.namespace.QName;

import org.apache.cxf.common.i18n.Message;
import org.apache.cxf.common.logging.LogUtils;
import org.apache.cxf.tools.common.ToolException;
import org.apache.cxf.tools.common.WSDLConstants;
import org.apache.cxf.tools.common.extensions.soap.SoapBinding;
import org.apache.cxf.tools.common.extensions.soap.SoapBody;
import org.apache.cxf.tools.common.extensions.soap.SoapFault;
import org.apache.cxf.tools.common.extensions.soap.SoapHeader;
import org.apache.cxf.tools.common.extensions.soap.SoapOperation;
import org.apache.cxf.tools.common.model.JavaMethod;
import org.apache.cxf.tools.common.model.JavaParameter;
import org.apache.cxf.tools.common.model.WSDLModel;
import org.apache.cxf.tools.common.model.WSDLParameter;
import org.apache.cxf.tools.java2wsdl.processor.JavaToWSDLProcessor;
import org.apache.cxf.tools.util.SOAPBindingUtil;


public class BindingGenerator {
    private static final Logger LOG = LogUtils.getL7dLogger(JavaToWSDLProcessor.class);
    private WSDLModel wmodel;
    private Definition definition;
    private ExtensionRegistry extensionRegistry;

    public BindingGenerator(WSDLModel model) {
        this.wmodel = model;
        definition = model.getDefinition();
        extensionRegistry = definition.getExtensionRegistry();
    }

    public void generate() {
        generate(false);
    }
    
    public void generate(boolean isSOAP12) {
        Binding binding = definition.createBinding();

        binding.setQName(new QName(WSDLConstants.NS_WSDL, wmodel.getPortTypeName() + "Binding"));
        binding.setPortType(definition.getPortType(new QName(wmodel.getTargetNameSpace(),
                                                             wmodel.getPortTypeName())));

        // default to genearte the soap 1.1 binding

        SoapBinding soapBinding = null;
        try {
            soapBinding = SOAPBindingUtil.createSoapBinding(extensionRegistry, isSOAP12);
            
            soapBinding.setStyle(wmodel.getStyle().toString().toLowerCase());
            binding.addExtensibilityElement(soapBinding);
        } catch (WSDLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        try {
            generateBindingOperation(binding, isSOAP12);
        } catch (WSDLException e) {
            throw new ToolException(e.getMessage(), e);
        }
        binding.setUndefined(false);
        definition.addBinding(binding);

    }

    private void generateBindingOperation(Binding binding, boolean isSOAP12) throws WSDLException {
        for (JavaMethod jmethod : wmodel.getJavaMethods()) {
            BindingOperation bindOperation = definition.createBindingOperation();
            bindOperation.setName(jmethod.getName());
            generateBindingOperationInputOutPut(bindOperation, jmethod, isSOAP12);
            binding.addBindingOperation(bindOperation);
        }

    }

    private void generateBindingOperationInputOutPut(BindingOperation operation,
                                                     JavaMethod jmethod,
                                                     boolean isSOAP12)
        throws WSDLException {
        // generate soap binding action

        SoapOperation soapOperation = SOAPBindingUtil.createSoapOperation(extensionRegistry, isSOAP12);
        
        soapOperation.setStyle(jmethod.getSoapStyle().name().toLowerCase());
        if (jmethod.getSoapAction() != null && !jmethod.getSoapAction().equals("")) {
            soapOperation.setSoapActionURI(jmethod.getSoapAction());
        }
        operation.addExtensibilityElement(soapOperation);

        generateInputSoapBody(jmethod, operation, jmethod.getRequest(), isSOAP12);
        
        generateOutputSoapBody(jmethod, operation, jmethod.getResponse(), isSOAP12);

        for (org.apache.cxf.tools.common.model.WSDLException ex : jmethod.getWSDLExceptions()) {

            BindingFault bindingFault = definition.createBindingFault();
            bindingFault.setName(ex.getExcpetionClass().getSimpleName());
            operation.addBindingFault(bindingFault);
            SoapFault soapFault = SOAPBindingUtil.createSoapFault(extensionRegistry, isSOAP12);
            soapFault.setUse("literal");
            soapFault.setName(ex.getExcpetionClass().getSimpleName());
            bindingFault.addExtensibilityElement(soapFault);

        }

    }

    private void generateOutputSoapBody(JavaMethod jmethod,
                                        BindingOperation operation,
                                        WSDLParameter param,
                                        boolean isSOAP12) throws WSDLException {
        if (param == null) {
            return;
        }

        BindingOutput bindingOutput = definition.createBindingOutput();
        bindingOutput.setName(param.getName());

        operation.setBindingOutput(bindingOutput);
        
        SoapBody body = SOAPBindingUtil.createSoapBody(extensionRegistry, BindingOutput.class, isSOAP12);
        if (jmethod.getSoapUse() == SOAPBinding.Use.LITERAL) {
            body.setUse("literal");
        } else {
            Message msg = new Message("ENCODED_USE_NOT_SUPPORTED", LOG);
            throw new ToolException(msg);
        }

        List<JavaParameter> bodyParams = new ArrayList<JavaParameter>();
        List<JavaParameter> headerParams = new ArrayList<JavaParameter>();

        splitSoapHeaderBodyParams(param, bodyParams, headerParams);
        // if exists soap header,then generate soap body parts

        if (headerParams.size() > 0) {
            List<String> parts = new ArrayList<String>();
            for (JavaParameter parameter : bodyParams) {
                parts.add(parameter.getPartName());
            }
            body.setParts(parts);
            SoapHeader soapHeader = null;
            for (JavaParameter jp : headerParams) {
                soapHeader = SOAPBindingUtil.createSoapHeader(extensionRegistry,
                                                              BindingOutput.class,
                                                              isSOAP12);
                soapHeader.setMessage(new QName(param.getTargetNamespace(), param.getName()));
                soapHeader.setPart(jp.getPartName());
                soapHeader.setUse("literal");
            }

            if (jmethod.getSoapStyle() == SOAPBinding.Style.RPC) {
                body.setNamespaceURI(param.getTargetNamespace());
            }
            bindingOutput.addExtensibilityElement(soapHeader);

        }
        bindingOutput.addExtensibilityElement(body);

    }

    private void generateInputSoapBody(JavaMethod jmethod,
                                       BindingOperation operation,
                                       WSDLParameter param,
                                       boolean isSOAP12) throws WSDLException {
        if (param == null) {
            return;
        }
        BindingInput bindingInput = definition.createBindingInput();
        bindingInput.setName(param.getName());

        operation.setBindingInput(bindingInput);

        SoapBody body = SOAPBindingUtil.createSoapBody(extensionRegistry, BindingInput.class, isSOAP12);
        if (jmethod.getSoapUse() == SOAPBinding.Use.LITERAL) {
            body.setUse("literal");
        } else {
            Message msg = new Message("ENCODED_USE_NOT_SUPPORTED", LOG);
            throw new ToolException(msg);
        }

        List<JavaParameter> bodyParams = new ArrayList<JavaParameter>();
        List<JavaParameter> headerParams = new ArrayList<JavaParameter>();

        splitSoapHeaderBodyParams(param, bodyParams, headerParams);

        // if exists soap header,then generate soap body parts

        if (headerParams.size() > 0) {
            List<String> parts = new ArrayList<String>();
            for (JavaParameter parameter : bodyParams) {
                parts.add(parameter.getPartName());
            }
            body.setParts(parts);
            SoapHeader soapHeader = null;
            for (JavaParameter jp : headerParams) {
                soapHeader = SOAPBindingUtil.createSoapHeader(extensionRegistry,
                                                              BindingInput.class,
                                                              isSOAP12);
                soapHeader.setMessage(new QName(param.getTargetNamespace(), param.getName()));
                soapHeader.setPart(jp.getPartName());
                soapHeader.setUse("literal");
            }
            
            if (jmethod.getSoapStyle() == SOAPBinding.Style.RPC) {
                body.setNamespaceURI(param.getTargetNamespace());
            }
            bindingInput.addExtensibilityElement(soapHeader);

        }
        bindingInput.addExtensibilityElement(body);

    }

    private void splitSoapHeaderBodyParams(WSDLParameter param, List<JavaParameter> bodyList,
                                           List<JavaParameter> headerList) {
        for (JavaParameter jpara : param.getChildren()) {
            if (jpara.isHeader()) {
                headerList.add(jpara);
            } else {
                bodyList.add(jpara);
            }
        }

    }

}
