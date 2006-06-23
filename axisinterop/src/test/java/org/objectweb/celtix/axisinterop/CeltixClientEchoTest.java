package org.objectweb.celtix.axisinterop;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URL;
import java.util.Arrays;
//import java.util.List;

import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.objectweb.celtix.bus.jaxws.spi.ProviderImpl;
import org.objectweb.celtix.testutil.common.AbstractClientServerSetupBase;

import org.soapinterop.celtix.AxisEchoService;
import org.soapinterop.celtix.InteropTestPortType;
import org.soapinterop.celtix.SOAPStruct;

public class CeltixClientEchoTest extends TestCase {

    private final QName serviceName = new QName("http://soapinterop.org/celtix",
                                                "AxisEchoService");    
    private final QName portName = new QName("http://soapinterop.org/celtix",
                                             "Echo");

    private InteropTestPortType port;

    static {
        System.setProperty(ProviderImpl.JAXWSPROVIDER_PROPERTY, ProviderImpl.JAXWS_PROVIDER);
    }

    public CeltixClientEchoTest() {
    }

    public static Test suite() throws Exception {
        TestSuite suite = new TestSuite(CeltixClientEchoTest.class);
        return new AbstractClientServerSetupBase(suite) {
            public void startServers() throws Exception {
                boolean ok = launchServer(AxisServer.class);
                if (!ok) {
                    fail("Failed to launch axis server.");
                }
            }
        };
    }

    public void setUp() throws Exception {
        super.setUp();
        URL wsdl = getClass().getResource("/wsdl/axis_echo.wsdl");
        assertNotNull("Could not get axis_echo.wsdl resource.", wsdl);
        
        AxisEchoService service = new AxisEchoService(wsdl, serviceName);
        assertNotNull("Failed to create AxisEchoService.", service);
        
        port = service.getPort(portName, InteropTestPortType.class);
    }

    protected boolean equalsDate(XMLGregorianCalendar orig, XMLGregorianCalendar actual) {
        boolean result = false;

        if ((orig.getYear() == actual.getYear()) 
            && (orig.getMonth() == actual.getMonth())
            && (orig.getDay() == actual.getDay())
            && (actual.getHour() == DatatypeConstants.FIELD_UNDEFINED) 
            && (actual.getMinute() == DatatypeConstants.FIELD_UNDEFINED)
            && (actual.getSecond() == DatatypeConstants.FIELD_UNDEFINED)
            && (actual.getMillisecond() == DatatypeConstants.FIELD_UNDEFINED)) {

            result = orig.getTimezone() == actual.getTimezone();
        }
        return result;
    }

    protected boolean equalsDateTime(XMLGregorianCalendar orig, XMLGregorianCalendar actual) {
        if ((orig.getYear() == actual.getYear())
            && (orig.getMonth() == actual.getMonth())
            && (orig.getDay() == actual.getDay())
            && (orig.getHour() == actual.getHour())
            && (orig.getMinute() == actual.getMinute())
            && (orig.getSecond() == actual.getSecond())) {
            return (orig.getMillisecond() == DatatypeConstants.FIELD_UNDEFINED
                || actual.getMillisecond() == DatatypeConstants.FIELD_UNDEFINED
                || orig.getMillisecond() == actual.getMillisecond())
                && (orig.getTimezone() == DatatypeConstants.FIELD_UNDEFINED
                || actual.getTimezone() == DatatypeConstants.FIELD_UNDEFINED
                || orig.getTimezone() == actual.getTimezone());
        }
        return false;
    }

    protected boolean equals(SOAPStruct obj1, SOAPStruct obj2) {
        if (null == obj1) {
            return null == obj2;
        } else {
            return Float.floatToIntBits(obj1.getVarFloat()) == Float.floatToIntBits(obj2.getVarFloat())
                && obj1.getVarInt() == obj2.getVarInt()
                && obj1.getVarString().equals(obj2.getVarString());
        }
    }

    public void testBoolean() throws Exception {
        boolean in = true;
        boolean out = port.echoBoolean(in);
        assertEquals("echoBoolean : incorrect return value : "
            + out + " expected : " + in, in, out);
    }

    public void testFloat() throws Exception {
        float in = 3.7F;
        float out = port.echoFloat(in);
        assertEquals("echoFloat : incorrect return value : "
            + out + " expected : " + in, in, out);
    }

    public void testInteger() throws Exception {
        int in = 42;
        int out = port.echoInteger(in);
        assertEquals("echoInteger : incorrect return value : "
            + out + " expected : " + in, in, out);
    }

    public void testVoid() throws Exception {
        port.echoVoid();
    }

    // TODO: Investigate why this fails.
    //public void testHexBinary() throws Exception {
    //    byte[] in = "1234".getBytes();
    //    byte[] out = port.echoHexBinary(in);
    //    assertTrue("echoHexBinary : incorrect return value : "
    //        + new String(out) + " expected : " + new String(in), Arrays.equals(in, out));
    //}

    public void testNegativeInteger() throws Exception {
        // Test xsd:negativeInteger
        BigInteger in = new BigInteger("-12345678900987654321");
        BigInteger out = port.echoNegativeInteger(in);
        assertEquals("echoNegativeInteger : incorrect return value : "
            + out + " expected : " + in, in, out);
    }

    public void testNonNegativeInteger() throws Exception {
        // Test xsd:nonNegativeInteger
        BigInteger in = new BigInteger("12345678901234567890");
        BigInteger out = port.echoNonNegativeInteger(in);
        assertEquals("echoNonNegativeInteger : incorrect return value : "
            + out + " expected : " + in, in, out);
    }

    public void testNonPositiveInteger() throws Exception {
        // Test xsd:nonPositiveInteger
        BigInteger in = new BigInteger("-12345678901234567890");
        BigInteger out = port.echoNonPositiveInteger(in);
        assertEquals("echoNonPositiveInteger : incorrect return value : "
            + out + " expected : " + in, in, out);
    }

    public void testPositiveInteger() throws Exception {
        // Test xsd:positiveInteger
        BigInteger in = new BigInteger("12345678900987654321");
        BigInteger out = port.echoPositiveInteger(in);
        assertEquals("echoPositiveInteger : incorrect return value : "
            + out + " expected : " + in, in, out);
    }

    public void testNormalizedString() throws Exception {
        // Test xsd:normalizedString
        String in = "abc-Normalized-def";
        String out = port.echoNormalizedString(in);
        assertEquals("echoNormalizedString : incorrect return value : "
            + out + " expected : " + in, in, out);
    }

    public void testToken() throws Exception {
        // Test xsd:token
        String in = "abc-Token-def";
        String out = port.echoToken(in);
        assertEquals("echoToken : incorrect return value : " + out + " expected : " + in, in, out);
    }

    public void testUnsignedByte() throws Exception {
        // Test xsd:unsignedByte
        short in = 103;
        short out = port.echoUnsignedByte(in);
        assertEquals("echoUnsignedByte : incorrect return value : "
            + out + " expected : " + in, in, out);
    }

    public void testUnsignedInt() throws Exception {
        // Test xsd:unsignedInt
        long in = 101;
        long out = port.echoUnsignedInt(in);
        assertEquals("echoUnsignedInt : incorrect return value : "
            + out + " expected : " + in, in, out);
    }

    public void testUnsignedLong() throws Exception {
        // Test xsd:unsignedLong
        BigInteger in = new BigInteger("123456789");
        BigInteger out = port.echoUnsignedLong(in);
        assertEquals("echoUnsignedLong : incorrect return value : "
            + out + " expected : " + in, in, out);
    }

    public void testUnsignedShort() throws Exception {
        // Test xsd:unsignedShort
        int in = 102;
        int out = port.echoUnsignedShort(in);
        assertEquals("echoUnsignedShort : incorrect return value : "
            + out + " expected : " + in, in, out);
    }

    public void testString() throws Exception {
        String in = "abcdefg";
        String out = port.echoString(in);
        assertEquals("echoString : incorrect return value : "
            + out + " expected : " + in, in, out);
    }

    // TODO: Figure out why this hits an assertion in JAXBDataBindingCallback
    //public void testStringArray() throws Exception {
    //    List<String> in = Arrays.asList("abc", "def");
    //    List<String> out = port.echoStringArray(in);
    //    assertTrue("echoStringArray : incorrect return value : ", in.equals(out));
    //}

    public void testStruct() throws Exception {
        SOAPStruct in = new SOAPStruct();
        in.setVarInt(6);
        in.setVarString("Rover");
        in.setVarFloat(1010F);
        SOAPStruct out = port.echoStruct(in);
        assertTrue("echoStruct : incorrect return value", equals(in, out));
    }

    public void testBase64() throws Exception {
        byte[] in = "Base64".getBytes();
        byte[] out = port.echoBase64(in);
        assertTrue("echoBase64 : incorrect return value : ", Arrays.equals(in, out));
    }

    // TODO: Figure out why this causes a NumberFormatException
    //public void testDate() throws Exception {
    //    javax.xml.datatype.DatatypeFactory factory = javax.xml.datatype.DatatypeFactory.newInstance();
    //    XMLGregorianCalendar in = factory.newXMLGregorianCalendar();
    //    in.setYear(1975);
    //    in.setMonth(5);
    //    in.setDay(5);
    //    XMLGregorianCalendar out = port.echoDate(in);
    //    assertTrue("echoDate : incorrect return value : "
    //        + out + " expected : " + in, equalsDate(in, out));
    //}

    public void testDateTime() throws Exception {
        javax.xml.datatype.DatatypeFactory factory = javax.xml.datatype.DatatypeFactory.newInstance();
        XMLGregorianCalendar in = factory.newXMLGregorianCalendar();
        in.setYear(1975);
        in.setMonth(5);
        in.setDay(5);
        in.setHour(12);
        in.setMinute(30);
        in.setSecond(15);
        XMLGregorianCalendar out = port.echoDateTime(in);
        assertTrue("echoDate : incorrect return value : "
            + out + " expected : " + in, equalsDateTime(in, out));
    }

    public void testDecimal() throws Exception {
        BigDecimal in = new BigDecimal("3.14159");
        BigDecimal out = port.echoDecimal(in);
        assertEquals("echoDecimal : incorrect return value : "
            + out + " expected : " + in, in, out);
    }

    public static void main(String[] args) {
        junit.textui.TestRunner.run(CeltixClientEchoTest.class);
    }

}
