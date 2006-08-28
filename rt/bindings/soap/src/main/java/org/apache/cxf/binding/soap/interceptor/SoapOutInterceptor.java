package org.apache.cxf.binding.soap.interceptor;

import java.util.ResourceBundle;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.w3c.dom.Element;

import org.apache.cxf.binding.soap.Soap11;
import org.apache.cxf.binding.soap.SoapFault;
import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.binding.soap.SoapVersion;
import org.apache.cxf.common.i18n.BundleUtils;
import org.apache.cxf.common.i18n.Message;
import org.apache.cxf.phase.Phase;
import org.apache.cxf.staxutils.StaxUtils;

public class SoapOutInterceptor extends AbstractSoapInterceptor {
    private static final ResourceBundle BUNDLE = BundleUtils.getBundle(SoapOutInterceptor.class);

    public SoapOutInterceptor() {
        super();
        setPhase(Phase.WRITE);
    }
    
    public void handleMessage(SoapMessage message) {
        try {
            XMLStreamWriter xtw = message.getContent(XMLStreamWriter.class);
            message.setContent(XMLStreamWriter.class, xtw);
            SoapVersion soapVersion = message.getVersion();
            if (soapVersion == null
                && message.getExchange().getInMessage() instanceof SoapMessage) {
                soapVersion = ((SoapMessage)message.getExchange().getInMessage()).getVersion();
                message.setVersion(soapVersion);
            }
            
            if (soapVersion == null) {
                soapVersion = Soap11.getInstance();
                message.setVersion(soapVersion);
            }
            
            xtw.writeStartElement(soapVersion.getPrefix(), 
                                  soapVersion.getEnvelope().getLocalPart(),
                                  soapVersion.getNamespace());
            xtw.writeNamespace(soapVersion.getPrefix(), soapVersion.getNamespace());
            Element eleHeaders = message.getHeaders(Element.class);

            if (eleHeaders != null) {
                StaxUtils.writeElement(eleHeaders, xtw, true);
            }
            
            xtw.writeStartElement(soapVersion.getPrefix(), 
                                  soapVersion.getBody().getLocalPart(),
                                  soapVersion.getNamespace());
            
            // Calling for Wrapped/RPC/Doc/ Interceptor for writing SOAP body
            message.getInterceptorChain().doIntercept(message);

            xtw.writeEndElement();
            
            // Write Envelop end element
            xtw.writeEndElement();
            xtw.flush();
        } catch (XMLStreamException e) {
            throw new SoapFault(new Message("XML_STREAM_EXC", BUNDLE), e, SoapFault.SENDER);
        }
    }
    
}
