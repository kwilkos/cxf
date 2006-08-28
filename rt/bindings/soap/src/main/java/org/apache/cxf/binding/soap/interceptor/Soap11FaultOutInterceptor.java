package org.apache.cxf.binding.soap.interceptor;

import java.util.Map;
import java.util.ResourceBundle;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import org.apache.cxf.binding.soap.SoapFault;
import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.common.i18n.BundleUtils;
import org.apache.cxf.common.i18n.Message;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.staxutils.StaxUtils;

public class Soap11FaultOutInterceptor extends AbstractSoapInterceptor {
    private static final ResourceBundle BUNDLE = BundleUtils.getBundle(Soap11FaultOutInterceptor.class);

    public void handleMessage(SoapMessage message) throws Fault {
        XMLStreamWriter writer = message.getContent(XMLStreamWriter.class);
        SoapFault fault = (SoapFault) message.getContent(Exception.class);

        try {
            Map<String, String> namespaces = fault.getNamespaces();
            for (Map.Entry<String, String> e : namespaces.entrySet()) {
                writer.writeNamespace(e.getKey(), e.getValue());
            }

            String prefix = message.getVersion().getPrefix();
            
            writer.writeStartElement(prefix, "Fault");

            writer.writeStartElement("faultcode");

            QName faultCode = fault.getFaultCode();
            String codeString;
            if (faultCode.equals(SoapFault.RECEIVER)) {
                codeString = prefix + ":Server";
            } else if (faultCode.equals(SoapFault.SENDER)) {
                codeString = prefix + ":Client";
            } else if (faultCode.equals(SoapFault.VERSION_MISMATCH)) {
                codeString = prefix + ":VersionMismatch";
            } else if (faultCode.equals(SoapFault.MUST_UNDERSTAND)) {
                codeString = prefix + ":MustUnderstand";
            } else if (faultCode.equals(SoapFault.DATA_ENCODING_UNKNOWN)) {
                codeString = prefix + ":Client";
            } else {
                String ns = faultCode.getNamespaceURI();
                String codePrefix = "";
                if (ns.length() > 0) {
                    codePrefix = StaxUtils.getUniquePrefix(writer, ns, true) + ":";
                }

                codeString = codePrefix + faultCode.getLocalPart();
            }

            writer.writeCharacters(codeString);
            writer.writeEndElement();

            writer.writeStartElement("faultstring");
            writer.writeCharacters(fault.getMessage());
            writer.writeEndElement();

            if (fault.hasDetails()) {
                Element detail = fault.getDetail();

                writer.writeStartElement("detail");

                NodeList details = detail.getChildNodes();
                for (int i = 0; i < details.getLength(); i++) {
                    StaxUtils.writeNode(details.item(i), writer, true);
                }

                // Details
                writer.writeEndElement();
            }

            if (fault.getRole() != null) {
                writer.writeStartElement("faultactor");
                writer.writeCharacters(fault.getRole());
                writer.writeEndElement();
            }

            // Fault
            writer.writeEndElement();
        } catch (XMLStreamException xe) {
            throw new Fault(new Message("XML_WRITE_EXC", BUNDLE), xe);
        }
    }

}
