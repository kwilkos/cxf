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
import java.util.Iterator;
import java.util.List;

import javax.wsdl.extensions.ExtensibilityElement;
import javax.wsdl.extensions.mime.MIMEContent;
import javax.wsdl.extensions.mime.MIMEMultipartRelated;
import javax.wsdl.extensions.mime.MIMEPart;
import javax.xml.namespace.QName;

import org.apache.cxf.binding.AbstractBindingFactory;
import org.apache.cxf.binding.Binding;
import org.apache.cxf.binding.soap.interceptor.EndpointSelectionInterceptor;
import org.apache.cxf.binding.soap.interceptor.MustUnderstandInterceptor;
import org.apache.cxf.binding.soap.interceptor.RPCInInterceptor;
import org.apache.cxf.binding.soap.interceptor.RPCOutInterceptor;
import org.apache.cxf.binding.soap.interceptor.ReadHeadersInterceptor;
import org.apache.cxf.binding.soap.interceptor.Soap11FaultInInterceptor;
import org.apache.cxf.binding.soap.interceptor.Soap11FaultOutInterceptor;
import org.apache.cxf.binding.soap.interceptor.Soap12FaultInInterceptor;
import org.apache.cxf.binding.soap.interceptor.Soap12FaultOutInterceptor;
import org.apache.cxf.binding.soap.interceptor.SoapActionInterceptor;
import org.apache.cxf.binding.soap.interceptor.SoapHeaderInterceptor;
import org.apache.cxf.binding.soap.interceptor.SoapOutInterceptor;
import org.apache.cxf.binding.soap.interceptor.SoapPreProtocolOutInterceptor;
import org.apache.cxf.binding.soap.model.SoapBindingInfo;
import org.apache.cxf.binding.soap.model.SoapBodyInfo;
import org.apache.cxf.binding.soap.model.SoapHeaderInfo;
import org.apache.cxf.binding.soap.model.SoapOperationInfo;
import org.apache.cxf.common.util.StringUtils;
import org.apache.cxf.endpoint.Endpoint;
import org.apache.cxf.interceptor.AttachmentInInterceptor;
import org.apache.cxf.interceptor.AttachmentOutInterceptor;
import org.apache.cxf.interceptor.BareOutInterceptor;
import org.apache.cxf.interceptor.DocLiteralInInterceptor;
import org.apache.cxf.interceptor.StaxInInterceptor;
import org.apache.cxf.interceptor.StaxOutInterceptor;
import org.apache.cxf.interceptor.URIMappingInterceptor;
import org.apache.cxf.interceptor.WrappedOutInterceptor;
import org.apache.cxf.message.Message;
import org.apache.cxf.service.model.BindingInfo;
import org.apache.cxf.service.model.BindingMessageInfo;
import org.apache.cxf.service.model.BindingOperationInfo;
import org.apache.cxf.service.model.MessageInfo;
import org.apache.cxf.service.model.MessagePartInfo;
import org.apache.cxf.service.model.OperationInfo;
import org.apache.cxf.service.model.ServiceInfo;
import org.apache.cxf.tools.common.extensions.soap.SoapBody;
import org.apache.cxf.tools.common.extensions.soap.SoapHeader;
import org.apache.cxf.tools.common.extensions.soap.SoapOperation;
import org.apache.cxf.tools.util.SOAPBindingUtil;
import org.apache.cxf.transport.ChainInitiationObserver;
import org.apache.cxf.transport.Destination;
import org.apache.cxf.transport.MessageObserver;
import org.apache.cxf.transport.MultipleEndpointObserver;


public class SoapBindingFactory extends AbstractBindingFactory {

    public static final String SOAP_11_BINDING = "http://schemas.xmlsoap.org/wsdl/soap/";
    public static final String SOAP_12_BINDING = "http://schemas.xmlsoap.org/wsdl/soap12/";
    
    public static final String MESSAGE_PROCESSING_DISABLED = "disable.header.processing";
    
    private boolean mtomEnabled = true;
    
    public BindingInfo createBindingInfo(ServiceInfo si, String bindingid, Object conf) {
        SoapBindingConfiguration config;
        if (conf instanceof SoapBindingConfiguration) {
            config = (SoapBindingConfiguration)conf;
        } else {
            config = new SoapBindingConfiguration();
        }
        if (bindingid.equals(SOAP_12_BINDING) 
            || "http://www.w3.org/2003/05/soap/bindings/HTTP/".equals(bindingid)) {
            config.setVersion(Soap12.getInstance());
        }
        SoapBindingInfo info = new SoapBindingInfo(si,
                                                   bindingid,
                                                   config.getVersion());
        
        info.setName(new QName(si.getName().getNamespaceURI(), 
                               si.getName().getLocalPart() + "SoapBinding"));
        info.setStyle(config.getStyle());
        info.setTransportURI(config.getTransportURI());
        
        if (config.isMtomEnabled()) {
            info.setProperty(Message.MTOM_ENABLED, Boolean.TRUE);
        }
        
        for (OperationInfo op : si.getInterface().getOperations()) {
            SoapOperationInfo sop = new SoapOperationInfo();
            sop.setAction(config.getSoapAction(op));
            sop.setStyle(config.getStyle(op));
            
            BindingOperationInfo bop = 
                info.buildOperation(op.getName(), op.getInputName(), op.getOutputName());
            
            bop.addExtensor(sop);
            
            info.addOperation(bop);
            
            
            BindingMessageInfo bInput = bop.getInput();
            if (bInput != null) {
                MessageInfo input = null;
                BindingMessageInfo unwrappedMsg = bInput;
                if (bop.isUnwrappedCapable()) {
                    input = bop.getOperationInfo().getUnwrappedOperation().getInput();
                    unwrappedMsg = bop.getUnwrappedOperation().getInput();
                } else {
                    input = bop.getOperationInfo().getInput();
                }
                setupHeaders(bop, bInput, unwrappedMsg, input, config);
            }
            
            BindingMessageInfo bOutput = bop.getOutput();
            if (bOutput != null) {
                MessageInfo output = null;
                BindingMessageInfo unwrappedMsg = bOutput;
                if (bop.isUnwrappedCapable()) {
                    output = bop.getOperationInfo().getUnwrappedOperation().getOutput();
                    unwrappedMsg = bop.getUnwrappedOperation().getOutput();
                } else {
                    output = bop.getOperationInfo().getOutput();
                }
                setupHeaders(bop, bOutput, unwrappedMsg, output, config);
            }
        }
        
        return info;
    }
    private void setupHeaders(BindingOperationInfo op, 
                              BindingMessageInfo bMsg, 
                              BindingMessageInfo unwrappedBMsg, 
                              MessageInfo msg,
                              SoapBindingConfiguration config) {
        List<MessagePartInfo> parts = new ArrayList<MessagePartInfo>();
        for (MessagePartInfo part : msg.getMessageParts()) {
            if (config.isHeader(op, part)) {
                SoapHeaderInfo headerInfo = new SoapHeaderInfo();
                headerInfo.setPart(part);
                headerInfo.setUse(config.getUse());

                bMsg.addExtensor(headerInfo);
            } else {
                parts.add(part);
            }
        }
        unwrappedBMsg.setMessageParts(parts);
    }
    
    public Binding createBinding(BindingInfo binding) {
        // TODO what about the mix style/use?

        // The default style should be doc-lit wrapped.
        String parameterStyle = SoapConstants.PARAMETER_STYLE_WRAPPED;
        String bindingStyle = SoapConstants.BINDING_STYLE_DOC;

        SoapBinding sb = null;
        SoapVersion version = null;
        if (binding instanceof SoapBindingInfo) {
            SoapBindingInfo sbi = (SoapBindingInfo) binding;
            version = sbi.getSoapVersion();
            sb = new SoapBinding(binding, version);
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
        } else {
            throw new RuntimeException("Can not initialize SoapBinding, BindingInfo is not SoapBindingInfo");
        }

        sb.getInInterceptors().add(new AttachmentInInterceptor());
        sb.getInInterceptors().add(new StaxInInterceptor()); 
        
        sb.getOutInterceptors().add(new SoapActionInterceptor());
        sb.getOutInterceptors().add(new AttachmentOutInterceptor());
        sb.getOutInterceptors().add(new StaxOutInterceptor());
        
        sb.getOutFaultInterceptors().add(new StaxOutInterceptor());

        if (!Boolean.TRUE.equals(binding.getProperty(DATABINDING_DISABLED))) {
            if (SoapConstants.BINDING_STYLE_RPC.equalsIgnoreCase(bindingStyle)) {
                sb.getInInterceptors().add(new RPCInInterceptor());
                sb.getOutInterceptors().add(new RPCOutInterceptor());
            } else if (SoapConstants.BINDING_STYLE_DOC.equalsIgnoreCase(bindingStyle)
                            && SoapConstants.PARAMETER_STYLE_BARE.equalsIgnoreCase(parameterStyle)) {
                //sb.getInInterceptors().add(new BareInInterceptor());
                sb.getInInterceptors().add(new DocLiteralInInterceptor());
                sb.getOutInterceptors().add(new BareOutInterceptor());
            } else {
                //sb.getInInterceptors().add(new WrappedInInterceptor());
                sb.getInInterceptors().add(new DocLiteralInInterceptor());
                sb.getOutInterceptors().add(new WrappedOutInterceptor());
                sb.getOutInterceptors().add(new BareOutInterceptor());
            }
            sb.getInInterceptors().add(new SoapHeaderInterceptor());
        }
        
        if (!Boolean.TRUE.equals(binding.getProperty(MESSAGE_PROCESSING_DISABLED))) {
            sb.getInInterceptors().add(new ReadHeadersInterceptor(getBus()));
            sb.getInInterceptors().add(new MustUnderstandInterceptor());
            sb.getOutInterceptors().add(new SoapPreProtocolOutInterceptor());
            sb.getOutInterceptors().add(new SoapOutInterceptor(getBus()));
            sb.getOutFaultInterceptors().add(new SoapOutInterceptor(getBus()));

            // REVISIT: The phase interceptor chain seems to freak out if this added
            // first. Not sure what the deal is at the moment, I suspect the
            // ordering algorithm needs to be improved
            sb.getInInterceptors().add(new URIMappingInterceptor());
        }
        
        if (version.getVersion() == 1.1) {
            sb.getInFaultInterceptors().add(new Soap11FaultInInterceptor());
            sb.getOutFaultInterceptors().add(new Soap11FaultOutInterceptor());
        } else if (version.getVersion() == 1.2) {
            sb.getInFaultInterceptors().add(new Soap12FaultInInterceptor());
            sb.getOutFaultInterceptors().add(new Soap12FaultOutInterceptor());
        }        

        return sb;
    }

    public BindingInfo createBindingInfo(ServiceInfo service, javax.wsdl.Binding binding, String ns) {
        SoapBindingInfo bi = new SoapBindingInfo(service, ns);
        // Copy all the extensors
        initializeBindingInfo(service, binding, bi);

        org.apache.cxf.tools.common.extensions.soap.SoapBinding wSoapBinding
            = SOAPBindingUtil.getSoapBinding(bi.getExtensors(ExtensibilityElement.class));
        
        bi.setTransportURI(wSoapBinding.getTransportURI());
        bi.setStyle(wSoapBinding.getStyle());

        for (BindingOperationInfo boi : bi.getOperations()) {
            initializeBindingOperation(bi, boi);
        }

        return bi;
    }

    private void initializeBindingOperation(SoapBindingInfo bi, BindingOperationInfo boi) {
        SoapOperationInfo soi = new SoapOperationInfo();

        SoapOperation soapOp = 
            SOAPBindingUtil.getSoapOperation(boi.getExtensors(ExtensibilityElement.class));
        
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

        List<SoapHeader> headers = 
            SOAPBindingUtil.getSoapHeaders(bmsg.getExtensors(ExtensibilityElement.class));
        if (headers != null) {
            for (SoapHeader header : headers) {
                SoapHeaderInfo headerInfo = new SoapHeaderInfo();
                headerInfo.setUse(header.getUse());
                MessagePartInfo part = msg.getMessagePart(new QName(msg.getName().getNamespaceURI(), header
                                .getPart()));
                if (part != null) {
                    headerInfo.setPart(part);
                    messageParts.remove(part);
                    bmsg.addExtensor(headerInfo);
                }
            }
            
            // Exclude the header parts from the message part list.
            bmsg.setMessageParts(messageParts);
        }

        SoapBodyInfo bodyInfo = new SoapBodyInfo();
        SoapBody soapBody = SOAPBindingUtil.getSoapBody(bmsg.getExtensors(ExtensibilityElement.class));
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
                    if (mpart.getExtensibilityElements().size() < 1) {
                        throw new RuntimeException("MIMEPart should at least contain one element!");
                    }
                    Object content = mpart.getExtensibilityElements().get(0);
                    if (content instanceof MIMEContent) {
                        partName = ((MIMEContent) content).getPart();
                    } else if (SOAPBindingUtil.isSOAPBody(content)) {
                        SoapBody sb = SOAPBindingUtil.getSoapBody(content);
                        if (sb.getParts().size() == 1) {
                            partName = (String) sb.getParts().get(0);
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
    
    @Override
    public synchronized void addListener(Destination d, Endpoint e) {
        MessageObserver mo = d.getMessageObserver();
        if (mo == null) {
            super.addListener(d, e);
            return;
        } 
        
        if (mo instanceof ChainInitiationObserver) {
            ChainInitiationObserver cio = (ChainInitiationObserver) mo;
            MultipleEndpointObserver newMO = new MultipleEndpointObserver(getBus()) {
                @Override
                protected Message createMessage(Message message) {
                    return new SoapMessage(message);
                }
            };
            
            newMO.getBindingInterceptors().add(new AttachmentInInterceptor());
            newMO.getBindingInterceptors().add(new StaxInInterceptor()); 
            
            // This will not work if we one of the endpoints disables message
            // processing. But, if you've disabled message processing, you 
            // probably aren't going to use this feature.
            newMO.getBindingInterceptors().add(new ReadHeadersInterceptor(getBus()));

            // Add in a default selection interceptor
            newMO.getRoutingInterceptors().add(new EndpointSelectionInterceptor());
            
            newMO.getEndpoints().add(cio.getEndpoint());
            
            mo = newMO;
        }
        
        if (mo instanceof MultipleEndpointObserver) {
            MultipleEndpointObserver meo = (MultipleEndpointObserver) mo;
            meo.getEndpoints().add(e);
        }
        
        d.setMessageObserver(mo);
    }

    public void setMtomEnabled(boolean mtomEnabled) {
        this.mtomEnabled = mtomEnabled;
    }

    public boolean isMtomEnabled() {
        return mtomEnabled;
    }    
}
