package org.objectweb.celtix.bindings.soap2;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.wsdl.extensions.ExtensibilityElement;
import javax.wsdl.extensions.soap.SOAPBinding;
import javax.wsdl.extensions.soap.SOAPBody;
import javax.wsdl.extensions.soap.SOAPHeader;
import javax.wsdl.extensions.soap.SOAPOperation;
import javax.xml.namespace.QName;

import org.objectweb.celtix.bindings.AbstractBindingFactory;
import org.objectweb.celtix.bindings.Binding;
import org.objectweb.celtix.bindings.BindingFactory;
import org.objectweb.celtix.bindings.soap2.binding.WrapperInterceptor;
import org.objectweb.celtix.bindings.soap2.model.SoapBindingInfo;
import org.objectweb.celtix.bindings.soap2.model.SoapBodyInfo;
import org.objectweb.celtix.bindings.soap2.model.SoapHeaderInfo;
import org.objectweb.celtix.bindings.soap2.model.SoapOperationInfo;
import org.objectweb.celtix.interceptors.BareInInterceptor;
import org.objectweb.celtix.service.model.BindingInfo;
import org.objectweb.celtix.service.model.BindingMessageInfo;
import org.objectweb.celtix.service.model.BindingOperationInfo;
import org.objectweb.celtix.service.model.MessageInfo;
import org.objectweb.celtix.service.model.MessagePartInfo;
import org.objectweb.celtix.service.model.ServiceInfo;
import org.objectweb.celtix.wsdl11.WSDLBindingFactory;

public class SoapBindingFactory extends AbstractBindingFactory implements BindingFactory, WSDLBindingFactory {

    private Map cachedBinding = new HashMap<BindingInfo, Binding>();
        
    public Binding createBinding(BindingInfo binding) {
        
        if (cachedBinding.get(binding) != null) {
            return (Binding)cachedBinding.get(binding);
        }
        
        SoapBinding sb = new SoapBinding();
        SoapBindingInfo sbi = (SoapBindingInfo)binding;
                
        sb.getInInterceptors().add(new MultipartMessageInterceptor());        
        sb.getInInterceptors().add(new ReadHeadersInterceptor());        
        sb.getInInterceptors().add(new MustUnderstandInterceptor());
        
        if (sbi.getStyle().equalsIgnoreCase(SoapConstants.STYLE_RPC)) {
            sb.getInInterceptors().add(new RPCInInterceptor());
        } else if(sbi.getStyle().equalsIgnoreCase(SoapConstants.STYLE_BARE)) {
            sb.getInInterceptors().add(new BareInInterceptor());
        } else if(sbi.getStyle().equalsIgnoreCase(SoapConstants.STYLE_WRAPPED)) {
            sb.getInInterceptors().add(new WrapperInterceptor());
        }        
                
        return sb;
    }

    public BindingInfo createBindingInfo(ServiceInfo service, javax.wsdl.Binding binding) {
        String ns = ((ExtensibilityElement)binding.getExtensibilityElements().get(0)).getElementType()
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
                headerInfo.setPart(part);
                messageParts.remove(part);

                bmsg.addExtensor(headerInfo);
            }
        }

        SOAPBody soapBody = bmsg.getExtensor(SOAPBody.class);
        SoapBodyInfo bodyInfo = new SoapBodyInfo();
        bodyInfo.setUse(soapBody.getUse());

        // Initialize the body parts.
        if (soapBody.getParts() != null) {
            List<MessagePartInfo> bodyParts = new ArrayList<MessagePartInfo>();
            for (Iterator itr = soapBody.getParts().iterator(); itr.hasNext();) {
                String partName = (String)itr.next();

                MessagePartInfo part = msg
                    .getMessagePart(new QName(msg.getName().getNamespaceURI(), partName));
                bodyParts.add(part);
            }
            bodyInfo.setParts(bodyParts);
        } else {
            bodyInfo.setParts(messageParts);
        }

        bmsg.addExtensor(bodyInfo);
    }
}
