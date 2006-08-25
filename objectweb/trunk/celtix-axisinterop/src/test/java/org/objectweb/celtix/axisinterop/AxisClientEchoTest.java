package org.objectweb.celtix.axisinterop;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.apache.axis.types.NegativeInteger;
import org.apache.axis.types.NonNegativeInteger;
import org.apache.axis.types.NonPositiveInteger;
import org.apache.axis.types.NormalizedString;
import org.apache.axis.types.PositiveInteger;
import org.apache.axis.types.Token;
import org.apache.axis.types.UnsignedByte;
import org.apache.axis.types.UnsignedInt;
import org.apache.axis.types.UnsignedLong;
import org.apache.axis.types.UnsignedShort;
import org.objectweb.celtix.testutil.common.AbstractClientServerSetupBase;
import org.soapinterop.axis.CeltixEchoServiceLocator;
import org.soapinterop.axis.InteropTestDocLitBindingStub;
import org.soapinterop.axis.InteropTestPortType;
import org.soapinterop.axis.SOAPStruct;

public class AxisClientEchoTest extends TestCase {

    private static InteropTestPortType binding;

    public AxisClientEchoTest() {
    }

    public static Test suite() throws Exception {
        TestSuite suite = new TestSuite(AxisClientEchoTest.class);
        return new AbstractClientServerSetupBase(suite) {
            public void startServers() throws Exception {
                boolean ok = launchServer(CeltixServer.class);
                if (!ok) {
                    fail("Failed to launch celtix server.");
                }
            }
        };
    }

    public void setUp() throws Exception {
        
        java.net.URL url = new java.net.URL("http://localhost:9240/CeltixEchoService/Echo");
        binding = new CeltixEchoServiceLocator().getEcho(url);
        assertNotNull("Could not create binding", binding);
        ((InteropTestDocLitBindingStub)binding).setTimeout(30000);
        ((InteropTestDocLitBindingStub)binding).setMaintainSession(true);
    }

    private boolean equalsDate(Calendar orig, Calendar actual) {
        return orig.get(Calendar.YEAR) == actual.get(Calendar.YEAR) 
            && orig.get(Calendar.MONTH) == actual.get(Calendar.MONTH)
            && orig.get(Calendar.DATE) == actual.get(Calendar.DATE);
    }

    public void testBoolean() throws Exception {
        boolean in = true;
        boolean out = binding.echoBoolean(in);
        assertEquals("echoBoolean : incorrect return value : "
            + out + " expected : " + in, in, out);
    }

    public void testFloat() throws Exception {
        float in = 3.7F;
        float out = binding.echoFloat(in);
        assertEquals("echoFloat : incorrect return value : "
            + out + " expected : " + in, in, out);
    }

    public void testInteger() throws Exception {
        int in = 42;
        int out = binding.echoInteger(in);
        assertEquals("echoInteger : incorrect return value : "
            + out + " expected : " + in, in, out);
    }

    public void testVoid() throws Exception {
        // sleep for a little to allow the response to the previous invocation
        // to be fully read.  Axis and Jetty appear to have a problem with 
        // synchronizing the two-way followed by  one-way.
        Thread.sleep(2000);
        binding.echoVoid();
    }

    // TODO: Investigate why this test fails.
    //public void testHexBinary() throws Exception {
    //    HexBinary in = new HexBinary("3344".getBytes());
    //    HexBinary out = new HexBinary(binding.echoHexBinary(in.getBytes()));
    //    assertEquals("echoHexBinary : incorrect return value : "
    //        + out + " expected : " + in, in, out);
    //}

    public void testNegativeInteger() throws Exception {
        // Test xsd:negativeInteger
        NegativeInteger in = new NegativeInteger("-12345678900987654321");
        NegativeInteger out = binding.echoNegativeInteger(in);
        assertEquals("echoNegativeInteger : incorrect return value : "
            + out + " expected : " + in, in, out);
    }

    public void testNonNegativeInteger() throws Exception {
        // Test xsd:nonNegativeInteger
        NonNegativeInteger in = new NonNegativeInteger("12345678901234567890");
        NonNegativeInteger out = binding.echoNonNegativeInteger(in);
        assertEquals("echoNonNegativeInteger : incorrect return value : "
            + out + " expected : " + in, in, out);
    }

    public void testNonPositiveInteger() throws Exception {
        // Test xsd:nonPositiveInteger
        NonPositiveInteger in = new NonPositiveInteger("-12345678901234567890");
        NonPositiveInteger out = binding.echoNonPositiveInteger(in);
        assertEquals("echoNonPositiveInteger : incorrect return value : "
            + out + " expected : " + in, in, out);
    }

    public void testPositiveInteger() throws Exception {
        // Test xsd:positiveInteger
        PositiveInteger in = new PositiveInteger("12345678900987654321");
        PositiveInteger out = binding.echoPositiveInteger(in);
        assertEquals("echoPositiveInteger : incorrect return value : "
            + out + " expected : " + in, in, out);
    }

    public void testNormalizedString() throws Exception {
        // Test xsd:normalizedString
        NormalizedString in = new NormalizedString("abc-Normalized-def");
        NormalizedString out = binding.echoNormalizedString(in);
        assertEquals("echoNormalizedString : incorrect return value : "
            + out + " expected : " + in, in, out);
    }

    public void testToken() throws Exception {
        // Test xsd:token
        Token in = new Token("abc-Token-def");
        Token out = binding.echoToken(in);
        assertEquals("echoToken : incorrect return value : " + out + " expected : " + in, in, out);
    }

    public void testUnsignedByte() throws Exception {
        // Test xsd:unsignedByte
        UnsignedByte in = new UnsignedByte(103);
        UnsignedByte out = binding.echoUnsignedByte(in);
        assertEquals("echoUnsignedByte : incorrect return value : "
            + out + " expected : " + in, in, out);
    }

    public void testUnsignedInt() throws Exception {
        // Test xsd:unsignedInt
        UnsignedInt in = new UnsignedInt(101);
        UnsignedInt out = binding.echoUnsignedInt(in);
        assertEquals("echoUnsignedInt : incorrect return value : "
            + out + " expected : " + in, in, out);
    }

    public void testUnsignedLong() throws Exception {
        // Test xsd:unsignedLong
        UnsignedLong in = new UnsignedLong(100);
        UnsignedLong out = binding.echoUnsignedLong(in);
        assertEquals("echoUnsignedLong : incorrect return value : "
            + out + " expected : " + in, in, out);
    }

    public void testUnsignedShort() throws Exception {
        // Test xsd:unsignedShort
        UnsignedShort in = new UnsignedShort(102);
        UnsignedShort out = binding.echoUnsignedShort(in);
        assertEquals("echoUnsignedShort : incorrect return value : "
            + out + " expected : " + in, in, out);
    }

    public void testString() throws Exception {
        String in = "abcdefg";
        String out = binding.echoString(in);
        assertEquals("echoString : incorrect return value : "
            + out + " expected : " + in, in, out);
    }

    // TODO: Figure out why this fails.
    //public void testStringArray() throws Exception {
    //    String[] in = new String[] {"abc", "def"};
    //    String[] out = binding.echoStringArray(in);
    //    for (String s1 : in) {
    //        System.out.print(s1 + " ");
    //    }
    //    System.out.println(".");
    //    for (String s2 : out) {
    //        System.out.print(s2 + " ");
    //    }
    //    System.out.println(".");
    //    assertTrue("echoStringArray : incorrect return value", Arrays.equals(in, out));
    //}

    public void testStruct() throws Exception {
        SOAPStruct in = new SOAPStruct();
        in.setVarInt(5);
        in.setVarString("Hello");
        in.setVarFloat(103F);
        SOAPStruct out = binding.echoStruct(in);
        assertEquals("echoStruct : incorrect return value : "
            + out + " expected : " + in, in, out);
    }

    public void testBase64() throws Exception {
        byte[] in = "Base64".getBytes();
        byte[] out = binding.echoBase64(in);
        assertTrue("echoBase64 : incorrect return value : ", Arrays.equals(in, out));
    }

    public void testDate() throws Exception {
        Calendar inCalendar = Calendar.getInstance();
        Date out = binding.echoDate(inCalendar.getTime());
        Calendar outCalendar = Calendar.getInstance();
        outCalendar.setTime(out);
        assertTrue("echoDate : incorrect return value", equalsDate(inCalendar, outCalendar));
    }

    // : Fails with Axis 1.3, passes with Axis 1.2
    public void testDateTime() throws Exception {
        Calendar in = Calendar.getInstance();
        in.setTimeZone(TimeZone.getTimeZone("GMT"));
        in.setTime(new Date());
        Calendar out = binding.echoDateTime(in);
        assertTrue("echoDateTime : incorrect return value : "
            + out + " expected : " + in, in.equals(out));
    }

    public void testDecimal() throws Exception {
        BigDecimal in = new BigDecimal("3.14159");
        BigDecimal out = binding.echoDecimal(in);
        assertEquals("echoDecimal : incorrect return value : "
            + out + " expected : " + in, in, out);
    }

    public static void main(String[] args) {
        junit.textui.TestRunner.run(AxisClientEchoTest.class);
    }

}
