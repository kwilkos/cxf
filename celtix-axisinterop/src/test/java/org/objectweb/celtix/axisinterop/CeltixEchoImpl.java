package org.objectweb.celtix.axisinterop;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import javax.jws.WebService;
import javax.xml.datatype.XMLGregorianCalendar;

import org.soapinterop.celtix.InteropTestPortType;
import org.soapinterop.celtix.SOAPStruct;

@WebService(serviceName = "CeltixEchoService", portName = "Echo",
            name = "InteropTestPortType",
            targetNamespace = "http://soapinterop.org/celtix")
public class CeltixEchoImpl implements InteropTestPortType {

    public String echoString(String in) {
        return in;
    }

    public List<String> echoStringArray(List<String> in) {
        return in;
    }

    public int echoInteger(int in) {
        return in;
    }

    public float echoFloat(float in) {
        return in;
    }

    public SOAPStruct echoStruct(SOAPStruct in) {
        return in;
    }

    public void echoVoid() {
    }

    public byte[] echoBase64(byte[] in) {
        return in;
    }

    public XMLGregorianCalendar echoDate(XMLGregorianCalendar in) {
        return in;
    }

    public XMLGregorianCalendar echoDateTime(XMLGregorianCalendar in) {
        return in;
    }

    public byte[] echoHexBinary(byte[] in) {
        return in;
    }

    public BigDecimal echoDecimal(BigDecimal in) {
        return in;
    }

    public boolean echoBoolean(boolean in) {
        return in;
    }

    public String echoToken(String in) {
        return in;
    }

    public String echoNormalizedString(String in) {
        return in;
    }

    public long echoUnsignedInt(long in) {
        return in;
    }

    public BigInteger echoUnsignedLong(BigInteger in) {
        return in;
    }

    public int echoUnsignedShort(int in) {
        return in;
    }

    public short echoUnsignedByte(short in) {
        return in;
    }

    public BigInteger echoNonNegativeInteger(BigInteger in) {
        return in;
    }

    public BigInteger echoPositiveInteger(BigInteger in) {
        return in;
    }

    public BigInteger echoNonPositiveInteger(BigInteger in) {
        return in;
    }

    public BigInteger echoNegativeInteger(BigInteger in) {
        return in;
    }

}
