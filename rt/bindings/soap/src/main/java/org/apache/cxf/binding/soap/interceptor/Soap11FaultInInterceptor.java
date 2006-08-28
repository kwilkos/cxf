package org.apache.cxf.binding.soap.interceptor;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.w3c.dom.Element;

import org.apache.cxf.binding.soap.SoapFault;
import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.staxutils.FragmentStreamReader;
import org.apache.cxf.staxutils.StaxUtils;

public class Soap11FaultInInterceptor extends AbstractSoapInterceptor {

    public void handleMessage(SoapMessage message) throws Fault {
        String exMessage = null;
        QName faultCode = null;
        String role = null;
        Element detail = null;

        XMLStreamReader reader = message.getContent(XMLStreamReader.class);

        try {
            boolean end = false;
            while (!end && reader.hasNext()) {
                int event = reader.next();
                switch (event) {
                case XMLStreamReader.END_DOCUMENT:
                    end = true;
                    break;
                case XMLStreamReader.END_ELEMENT:
                    break;
                case XMLStreamReader.START_ELEMENT:
                    if (reader.getLocalName().equals("faultcode")) {
                        faultCode = StaxUtils.readQName(reader);
                    } else if (reader.getLocalName().equals("faultstring")) {
                        exMessage = reader.getElementText();
                    } else if (reader.getLocalName().equals("faultactor")) {
                        role = reader.getElementText();
                    } else if (reader.getLocalName().equals("detail")) {
                        detail = StaxUtils.read(new FragmentStreamReader(reader)).getDocumentElement();
                    }
                    break;
                default:
                    break;
                }
            }
        } catch (XMLStreamException e) {
            throw new SoapFault("Could not parse message.", SoapFault.SENDER);
        }

        SoapFault fault = new SoapFault(exMessage, faultCode);
        fault.setDetail(detail);
        fault.setRole(role);

        message.setContent(Exception.class, fault);
    }

}
