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

package org.apache.cxf.binding.soap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.wsdl.extensions.ExtensibilityElement;
import javax.wsdl.extensions.mime.MIMEContent;
import javax.wsdl.extensions.mime.MIMEMultipartRelated;
import javax.wsdl.extensions.mime.MIMEPart;
import javax.wsdl.extensions.soap.SOAPBinding;
import javax.wsdl.extensions.soap.SOAPBody;
import javax.wsdl.extensions.soap.SOAPHeader;
import javax.wsdl.extensions.soap.SOAPOperation;
import javax.xml.namespace.QName;

import org.apache.cxf.Bus;
import org.apache.cxf.binding.AbstractBindingFactory;
import org.apache.cxf.binding.Binding;
import org.apache.cxf.binding.BindingFactoryManager;
import org.apache.cxf.binding.soap.interceptor.AttachmentOutInterceptor;
import org.apache.cxf.binding.soap.interceptor.MultipartMessageInterceptor;
import org.apache.cxf.binding.soap.interceptor.MustUnderstandInterceptor;
import org.apache.cxf.binding.soap.interceptor.RPCInInterceptor;
import org.apache.cxf.binding.soap.interceptor.RPCOutInterceptor;
import org.apache.cxf.binding.soap.interceptor.ReadHeadersInterceptor;
import org.apache.cxf.binding.soap.interceptor.Soap11FaultInInterceptor;
import org.apache.cxf.binding.soap.interceptor.Soap11FaultOutInterceptor;
import org.apache.cxf.binding.soap.interceptor.SoapOutInterceptor;
import org.apache.cxf.binding.soap.interceptor.SoapPreProtocolOutInterceptor;
import org.apache.cxf.binding.soap.model.SoapBindingInfo;
import org.apache.cxf.binding.soap.model.SoapBodyInfo;
import org.apache.cxf.binding.soap.model.SoapHeaderInfo;
import org.apache.cxf.binding.soap.model.SoapOperationInfo;
import org.apache.cxf.common.util.StringUtils;
import org.apache.cxf.interceptor.BareInInterceptor;
import org.apache.cxf.interceptor.BareOutInterceptor;
import org.apache.cxf.interceptor.StaxInInterceptor;
import org.apache.cxf.interceptor.StaxOutInterceptor;
import org.apache.cxf.interceptor.WrappedInInterceptor;
import org.apache.cxf.interceptor.WrappedOutInterceptor;
import org.apache.cxf.service.model.BindingInfo;
import org.apache.cxf.service.model.BindingMessageInfo;
import org.apache.cxf.service.model.BindingOperationInfo;
import org.apache.cxf.service.model.MessageInfo;
import org.apache.cxf.service.model.MessagePartInfo;
import org.apache.cxf.service.model.ServiceInfo;

public class SoapBindingFactory extends AbstractBindingFactory {

    private Map cachedBinding = new HashMap<BindingInfo, Binding>();

    @Resource
    private Bus bus;

    @Resource
    private Collection<String> activationNamespaces;

    @PostConstruct
    void registerSelf() {
        if (null == bus) {
            return;
        }
        BindingFactoryManager bfm = bus.getExtension(BindingFactoryManager.class);
        if (null != bfm) {
            for (String ns : activationNamespaces) {
                bfm.registerBindingFactory(ns, this);
            }
        }
    }

    public Binding createBinding(BindingInfo binding) {

        if (cachedBinding.get(binding) != null) {
            return (Binding) cachedBinding.get(binding);
        }

        // TODO what about the mix style/use?

        // The default style should be doc-lit wrapped.
        String parameterStyle = SoapConstants.PARAMETER_STYLE_WRAPPED;
        String bindingStyle = SoapConstants.BINDING_STYLE_DOC;

        SoapBinding sb = new SoapBinding();
        if (binding instanceof SoapBindingInfo) {
            SoapBindingInfo sbi = (SoapBindingInfo) binding;
            // Service wide style
            if (!StringUtils.isEmpty(sbi.getStyle())) {
                bindingStyle = sbi.getStyle();
            }

            // Operation wide style, what to do with the mixed style/use?
            for (BindingOperationInfo boi : sbi.getOperations()) {
                if (sbi.getStyle(boi.getOperationInfo()) != null) {
                    bindingStyle = sbi.getStyle(boi.getOperationInfo());
                }
                if (boi.getUnwrappedOperation() == null) {
                    parameterStyle = SoapConstants.PARAMETER_STYLE_BARE;
                }
            }
        }

        sb.getInInterceptors().add(new MultipartMessageInterceptor());
        sb.getInInterceptors().add(new ReadHeadersInterceptor());
        sb.getInInterceptors().add(new MustUnderstandInterceptor());
        sb.getInInterceptors().add(new StaxInInterceptor());

        sb.getInFaultInterceptors().add(new Soap11FaultInInterceptor());

        sb.getOutInterceptors().add(new AttachmentOutInterceptor());
        sb.getOutInterceptors().add(new StaxOutInterceptor());
        sb.getOutInterceptors().add(new SoapPreProtocolOutInterceptor());
        sb.getOutInterceptors().add(new SoapOutInterceptor());

        sb.getOutFaultInterceptors().add(new StaxOutInterceptor());
        sb.getOutFaultInterceptors().add(new SoapOutInterceptor());
        sb.getOutFaultInterceptors().add(new Soap11FaultOutInterceptor());

        if (SoapConstants.BINDING_STYLE_RPC.equalsIgnoreCase(bindingStyle)) {
            sb.getInInterceptors().add(new RPCInInterceptor());
            sb.getOutInterceptors().add(new RPCOutInterceptor());
        } else if (SoapConstants.BINDING_STYLE_DOC.equalsIgnoreCase(bindingStyle)
                        && SoapConstants.PARAMETER_STYLE_BARE.equalsIgnoreCase(parameterStyle)) {
            sb.getInInterceptors().add(new BareInInterceptor());
            sb.getOutInterceptors().add(new BareOutInterceptor());
        } else {
            sb.getInInterceptors().add(new WrappedInInterceptor());
            sb.getOutInterceptors().add(new WrappedOutInterceptor());
            sb.getOutInterceptors().add(new BareOutInterceptor());
        }
        
        return sb;
    }

    public BindingInfo createBindingInfo(ServiceInfo service, javax.wsdl.Binding binding) {
        String ns = ((ExtensibilityElement) binding.getExtensibilityElements().get(0)).getElementType()
                        .getNamespaceURI();
        SoapBindingInfo bi = new SoapBindingInfo(service, ns, Soap11.getInstance());

        // Copy all the extensors
        initializeBindingInfo(service, binding, bi);

        SOAPBinding wSoapBinding = bi.getExtensor(SOAPBinding.class);
        bi.setTransportURI(wSoapBinding.getTransportURI());
        bi.setStyle(wSoapBinding.getStyle());

        for (BindingOperationInfo boi : bi.getOperations()) {
            initializeBindingOperation(bi, boi);
        }

        return bi;
    }

    private void initializeBindingOperation(SoapBindingInfo bi, BindingOperationInfo boi) {
        SoapOperationInfo soi = new SoapOperationInfo();

        SOAPOperation soapOp = boi.getExtensor(SOAPOperation.class);
        if (soapOp != null) {
            String action = soapOp.getSoapActionURI();
            if (action == null) {
                action = "";
            }

            soi.setAction(action);
            soi.setStyle(soapOp.getStyle());

        }

        boi.addExtensor(soi);

        if (boi.getInput() != null) {
            initializeMessage(bi, boi, boi.getInput());
        }

        if (boi.getOutput() != null) {
            initializeMessage(bi, boi, boi.getOutput());
        }
    }

    private void initializeMessage(SoapBindingInfo bi, BindingOperationInfo boi, BindingMessageInfo bmsg) {
        MessageInfo msg = bmsg.getMessageInfo();

        List<MessagePartInfo> messageParts = new ArrayList<MessagePartInfo>();
        messageParts.addAll(msg.getMessageParts());

        List<SOAPHeader> headers = bmsg.getExtensors(SOAPHeader.class);
        if (headers != null) {
            for (SOAPHeader header : headers) {
                SoapHeaderInfo headerInfo = new SoapHeaderInfo();
                headerInfo.setUse(header.getUse());

                MessagePartInfo part = msg.getMessagePart(new QName(msg.getName().getNamespaceURI(), header
                                .getPart()));
                if (part != null) {
                    part.setInSoapHeader(true);
                }
                headerInfo.setPart(part);
                messageParts.remove(part);

                bmsg.addExtensor(headerInfo);
            }
        }

        SoapBodyInfo bodyInfo = new SoapBodyInfo();
        SOAPBody soapBody = bmsg.getExtensor(SOAPBody.class);
        List parts = null;
        if (soapBody == null) {
            MIMEMultipartRelated mmr = bmsg.getExtensor(MIMEMultipartRelated.class);
            if (mmr != null) {
                parts = mmr.getMIMEParts();
            }
        } else {
            bodyInfo.setUse(soapBody.getUse());
            parts = soapBody.getParts();
        }
        // Initialize the body parts.
        if (parts != null) {
            List<MessagePartInfo> bodyParts = new ArrayList<MessagePartInfo>();
            for (Iterator itr = parts.iterator(); itr.hasNext();) {
                Object part = itr.next();
                String partName = null;
                if (part instanceof MIMEPart) {
                    MIMEPart mpart = (MIMEPart) part;
                    if (mpart.getExtensibilityElements().size() == 1) {
                        Object content = mpart.getExtensibilityElements().get(0);
                        if (content instanceof MIMEContent) {
                            partName = ((MIMEContent) content).getPart();
                        } else {
                            if (((SOAPBody) content).getParts().size() == 1) {
                                partName = (String) ((SOAPBody) content).getParts().get(0);
                            }
                        }
                    }
                } else {
                    partName = (String)part;
                }
                if (partName != null) {
                    MessagePartInfo mpi = msg.getMessagePart(new QName(msg.getName().getNamespaceURI(),
                                    partName));
                    bodyParts.add(mpi);
                }
            }
            bodyInfo.setParts(bodyParts);
        } else {
            bodyInfo.setParts(messageParts);
        }

        bmsg.addExtensor(bodyInfo);
    }    
}
