package org.apache.cxf.binding.soap;

import javax.xml.namespace.QName;

import org.w3c.dom.Element;

import org.apache.cxf.common.i18n.Message;
import org.apache.cxf.interceptor.Fault;

public class SoapFault extends Fault {

    public static final QName VERSION_MISMATCH = new QName(Soap12.SOAP_NAMESPACE, "VersionMismatch");
    public static final QName MUST_UNDERSTAND = new QName(Soap12.SOAP_NAMESPACE, "MustUnderstand");
    public static final QName DATA_ENCODING_UNKNOWN = new QName(Soap12.SOAP_NAMESPACE, "DataEncodingUnknown");
    public static final QName ATTACHMENT_IO = new QName(Soap12.SOAP_NAMESPACE, "AttachmentIOError");

    /**
     * "The message was incorrectly formed or did not contain the appropriate
     * information in order to succeed." -- SOAP 1.2 Spec
     */
    public static final QName SENDER = new QName(Soap12.SOAP_NAMESPACE, "Sender");

    /**
     * A SOAP 1.2 only fault code. <p/> "The message could not be processed for
     * reasons attributable to the processing of the message rather than to the
     * contents of the message itself." -- SOAP 1.2 Spec <p/> If this message is
     * used in a SOAP 1.1 Fault it will most likely (depending on the
     * FaultHandler) be mapped to "Sender" instead.
     */
    public static final QName RECEIVER = new QName(Soap12.SOAP_NAMESPACE, "Receiver");

    public static final QName SOAP11_SERVER = new QName(Soap11.SOAP_NAMESPACE, "Server");
    public static final QName SOAP11_CLIENT = new QName(Soap11.SOAP_NAMESPACE, "Client");
    public static final QName SOAP11_MUST_UNDERSTAND = new QName(Soap11.SOAP_NAMESPACE, "MustUnderstand");
    public static final QName SOAP11_VERSION_MISMATCH = new QName(Soap11.SOAP_NAMESPACE, "VersionMismatch");

    private QName faultCode;
    private QName subCode;
    private String role;
    private Element detail;

    public SoapFault(Message message, Throwable throwable, QName type) {
        super(message, throwable);
        this.faultCode = type;
    }

    public SoapFault(Message message, QName faultCode) {
        super(message);
        this.faultCode = faultCode;
    }

    /**
     * Returns the detail node. If no detail node has been set, an empty
     * <code>&lt;detail&gt;</code> is created.
     * 
     * @return the detail node.
     */
    public Element getDetail() {
        return detail;
    }

    /**
     * Sets a details <code>Node</code> on this fault.
     * 
     * @param details the detail node.
     */
    public void setDetail(Element details) {
        detail = details;
    }

    /**
     * Returns the fault code of this fault.
     * 
     * @return the fault code.
     */
    public QName getFaultCode() {
        return faultCode;
    }

    /**
     * Sets the fault code of this fault.
     * 
     * @param faultCode the fault code.
     */
    public void setFaultCode(QName faultCode) {
        this.faultCode = faultCode;
    }

    public String getReason() {
        return getMessage();
    }

    /**
     * Returns the fault actor.
     * 
     * @return the fault actor.
     */
    public String getRole() {
        return role;
    }

    /**
     * Sets the fault actor.
     * 
     * @param actor the actor.
     */
    public void setRole(String actor) {
        this.role = actor;
    }

    /**
     * Returns the SubCode for the Fault Code.
     * 
     * @return The SubCode element as detailed by the SOAP 1.2 spec.
     */
    public QName getSubCode() {
        return subCode;
    }

    /**
     * Sets the SubCode for the Fault Code.
     * 
     * @param subCode The SubCode element as detailed by the SOAP 1.2 spec.
     */
    public void setSubCode(QName subCode) {
        this.subCode = subCode;
    }

    /**
     * Indicates whether this fault has a detail message.
     * 
     * @return <code>true</code> if this fault has a detail message;
     *         <code>false</code> otherwise.
     */
    public boolean hasDetails() {
        return detail == null ? false : true;
    }
}
