package org.objectweb.celtix.systest.type_test;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URL;
//import java.util.ArrayList;
import java.util.Arrays;
//import java.util.List;

import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;
import javax.xml.ws.Holder;
import javax.xml.ws.WebServiceException;

import org.objectweb.celtix.BusException;
import org.objectweb.celtix.systest.common.ClientServerTestBase;

public abstract class AbstractTypeTestClient extends ClientServerTestBase implements TypeTestTester {
    protected static org.objectweb.type_test.doc.TypeTestPortType docClient;
    protected static org.objectweb.type_test.rpc.TypeTestPortType rpcClient;
    protected static boolean testDocLiteral;

    protected boolean perfTestOnly;

    private final QName serviceName;
    private final QName portName;
    private final String wsdlPath;

    public AbstractTypeTestClient(String name, QName theServicename, QName thePort, String theWsdlPath) {
        super(name); 

        System.setProperty("javax.xml.ws.spi.Provider", "org.objectweb.celtix.bus.jaxws.spi.ProviderImpl");
        serviceName = theServicename;
        portName = thePort;
        wsdlPath = theWsdlPath;
    }

    public void setPerformanceTestOnly() {
        perfTestOnly = true;
    }

    public void setUp() throws BusException {
        super.setUp(); 
        initBus(); 
        // for support of soap header, context, substitutionGroup and
        // xsd:anyType, you must register generated TypeFactory.
        //File file = new File("/wsdl/type_test/type_test_soap.wsdl");
        //String location = file.toURL().toString();
        //bus.registerTypeFactory(new org.objectweb.type_test.TypeTestTypeFactory(location));
        
        //
        // Check the name of the wsdlPath to decide whether to test doc
        // literal or rpc literal style.
        //
        URL wsdlLocation = getClass().getResource(wsdlPath);
        assertNotNull(wsdlLocation);
        testDocLiteral = wsdlPath.contains("doclit");
        if (testDocLiteral) {
            org.objectweb.type_test.doc.SOAPService docService =
                new org.objectweb.type_test.doc.SOAPService(wsdlLocation, serviceName);
            docClient = (org.objectweb.type_test.doc.TypeTestPortType)
                docService.getPort(portName, org.objectweb.type_test.doc.TypeTestPortType.class);
            assertNotNull(docClient);
        } else {
            org.objectweb.type_test.rpc.SOAPService rpcService =
                new org.objectweb.type_test.rpc.SOAPService(wsdlLocation, serviceName);
            rpcClient = (org.objectweb.type_test.rpc.TypeTestPortType)
                rpcService.getPort(portName, org.objectweb.type_test.rpc.TypeTestPortType.class);
            assertNotNull(rpcClient);
        }
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

    protected boolean equalsTime(XMLGregorianCalendar orig, XMLGregorianCalendar actual) {
        boolean result = false;
        if ((orig.getHour() == actual.getHour())
            && (orig.getMinute() == actual.getMinute())
            && (orig.getSecond() == actual.getSecond())
            && (orig.getMillisecond() == actual.getMillisecond())
            && (orig.getTimezone() == actual.getTimezone())) {
            result = true;
        }
        return result;
    }

    protected boolean equalsDateTime(XMLGregorianCalendar orig, XMLGregorianCalendar actual) {
        boolean result = false;
        if ((orig.getYear() == actual.getYear())
            && (orig.getMonth() == actual.getMonth())
            && (orig.getDay() == actual.getDay())
            && (orig.getHour() == actual.getHour())
            && (orig.getMinute() == actual.getMinute())
            && (orig.getSecond() == actual.getSecond())
            && (orig.getMillisecond() == actual.getMillisecond())) {

            result = orig.getTimezone() == actual.getTimezone();
        }
        return result;
    }

    public void testVoid() throws Exception {
        //client.testVoid();
    }

    public void testOneway() throws Exception {
        //String x = "hello";
        //String y = "oneway";
        //client.testOneway(x, y);
    }

    public void testByte() throws Exception {
        byte valueSets[][] = {
            {0, 1},
            {-1, 0},
            {Byte.MIN_VALUE, Byte.MAX_VALUE}
        };

        for (int i = 0; i < valueSets.length; i++) {
            byte x = valueSets[i][0];
            Holder<Byte> yOrig = new Holder<Byte>(valueSets[i][1]);
            Holder<Byte> y = new Holder<Byte>(valueSets[i][1]);
            Holder<Byte> z = new Holder<Byte>();

            byte ret;
            if (testDocLiteral) {
                ret = docClient.testByte(x, y, z);
            } else {
                ret = rpcClient.testByte(x, y, z);
            }
            if (!perfTestOnly) {
                assertEquals("testByte(): Incorrect value for inout param",
                             Byte.valueOf(x), y.value);
                assertEquals("testByte(): Incorrect value for out param",
                             yOrig.value, z.value);
                assertEquals("testByte(): Incorrect return value", x, ret);
            }
        }
    }

    public void testShort() throws Exception {
        short valueSets[][] = {
            {0, 1},
            {-1, 0},
            {Short.MIN_VALUE, Short.MAX_VALUE}
        };

        for (int i = 0; i < valueSets.length; i++) {
            short x = valueSets[i][0];
            Holder<Short> yOrig = new Holder<Short>(valueSets[i][1]);
            Holder<Short> y = new Holder<Short>(valueSets[i][1]);
            Holder<Short> z = new Holder<Short>();

            short ret;
            if (testDocLiteral) {
                ret = docClient.testShort(x, y, z);
            } else {
                ret = rpcClient.testShort(x, y, z);
            }
            if (!perfTestOnly) {
                assertEquals("testShort(): Incorrect value for inout param", Short.valueOf(x), y.value);
                assertEquals("testShort(): Incorrect value for out param", yOrig.value, z.value);
                assertEquals("testShort(): Incorrect return value", x, ret);
            }
        }
    }

    public void testUnsignedShort() throws Exception {
        int valueSets[][] = {{0, 1}, {1, 0}, {0, Short.MAX_VALUE * 2 + 1}};

        for (int i = 0; i < valueSets.length; i++) {
            int x = valueSets[i][0];
            Holder<Integer> yOrig = new Holder<Integer>(valueSets[i][1]);
            Holder<Integer> y = new Holder<Integer>(valueSets[i][1]);
            Holder<Integer> z = new Holder<Integer>();

            int ret;
            if (testDocLiteral) {
                ret = docClient.testUnsignedShort(x, y, z);
            } else {
                ret = rpcClient.testUnsignedShort(x, y, z);
            }
            if (!perfTestOnly) {
                assertEquals("testUnsignedShort(): Incorrect value for inout param",
                             Integer.valueOf(x), y.value);
                assertEquals("testUnsignedShort(): Incorrect value for out param",
                             yOrig.value, z.value);
                assertEquals("testUnsignedShort(): Incorrect return value", x, ret);
            }
        }
    }

    public void testInt() throws Exception {
        int valueSets[][] = {{5, 10}, {-10, 50},
                             {Integer.MIN_VALUE, Integer.MAX_VALUE}};

        for (int i = 0; i < valueSets.length; i++) {
            int x = valueSets[i][0];
            Holder<Integer> yOrig = new Holder<Integer>(valueSets[i][1]);
            Holder<Integer> y = new Holder<Integer>(valueSets[i][1]);
            Holder<Integer> z = new Holder<Integer>();
            
            int ret;
            if (testDocLiteral) {
                ret = docClient.testInt(x, y, z);
            } else {
                ret = rpcClient.testInt(x, y, z);
            }
            if (!perfTestOnly) {
                assertEquals("testInt(): Incorrect value for inout param", Integer.valueOf(x), y.value);
                assertEquals("testInt(): Incorrect value for out param", yOrig.value, z.value);
                assertEquals("testInt(): Incorrect return value", x, ret);
            }
        }
    }

    public void testUnsignedInt() throws Exception {
        long valueSets[][] = {{11, 20}, {1, 0},
                              {0, ((long)Integer.MAX_VALUE) * 2 + 1}};

        for (int i = 0; i < valueSets.length; i++) {
            long x = valueSets[i][0];
            long yOrig = valueSets[i][1];
            Holder<Long> y = new Holder<Long>(valueSets[i][1]);
            Holder<Long> z = new Holder<Long>();

            long ret;
            if (testDocLiteral) {
                ret = docClient.testUnsignedInt(x, y, z);
            } else {
                ret = rpcClient.testUnsignedInt(x, y, z);
            }
            if (!perfTestOnly) {
                assertEquals("testUnsignedInt(): Incorrect value for inout param",
                             Long.valueOf(x), y.value);
                assertEquals("testUnsignedInt(): Incorrect value for out param",
                             Long.valueOf(yOrig), z.value);
                assertEquals("testUnsignedInt(): Incorrect return value", x, ret);
            }
        }
    }

    public void testLong() throws Exception {
        long valueSets[][] = {{0, 1}, {-1, 0},
                              {Long.MIN_VALUE, Long.MAX_VALUE}};

        for (int i = 0; i < valueSets.length; i++) {
            long x = valueSets[i][0];
            Holder<Long> yOrig = new Holder<Long>(valueSets[i][1]);
            Holder<Long> y = new Holder<Long>(valueSets[i][1]);
            Holder<Long> z = new Holder<Long>();

            long ret;
            if (testDocLiteral) {
                ret = docClient.testLong(x, y, z);
            } else {
                ret = rpcClient.testLong(x, y, z);
            }
            if (!perfTestOnly) {
                assertEquals("testLong(): Incorrect value for inout param", Long.valueOf(x), y.value);
                assertEquals("testLong(): Incorrect value for out param", yOrig.value, z.value);
                assertEquals("testLong(): Incorrect return value", x, ret);
            }
        }
    }

    public void testUnsignedLong() throws Exception {
        BigInteger valueSets[][] = {{new BigInteger("0"), new BigInteger("1")},
                                    {new BigInteger("1"), new BigInteger("0")},
                                    {new BigInteger("0"), 
                                     new BigInteger(String.valueOf(Long.MAX_VALUE * Long.MAX_VALUE))}};

        for (int i = 0; i < valueSets.length; i++) {
            BigInteger x = valueSets[i][0];
            Holder<BigInteger> yOrig = new Holder<BigInteger>(valueSets[i][1]);
            Holder<BigInteger> y = new Holder<BigInteger>(valueSets[i][1]);
            Holder<BigInteger> z = new Holder<BigInteger>();

            BigInteger ret;
            if (testDocLiteral) {
                ret = docClient.testUnsignedLong(x, y, z);
            } else {
                ret = rpcClient.testUnsignedLong(x, y, z);
            }
            if (!perfTestOnly) {
                assertEquals("testUnsignedLong(): Incorrect value for inout param", x, y.value);
                assertEquals("testUnsignedLong(): Incorrect value for out param", yOrig.value, z.value);
                assertEquals("testUnsignedLong(): Incorrect return value", x, ret);
            }
        }
    }

    public void testFloat() throws Exception {
        float delta = 0.0f;
        float valueSets[][] = {
            {0.0f, 1.0f},
            {-1.0f, (float)java.lang.Math.PI},
            {-100.0f, 100.0f},
            {Float.NEGATIVE_INFINITY, Float.POSITIVE_INFINITY},
        };

        for (int i = 0; i < valueSets.length; i++) {
            float x = valueSets[i][0];
            Holder<Float> yOrig = new Holder<Float>(valueSets[i][1]);
            Holder<Float> y = new Holder<Float>(valueSets[i][1]);
            Holder<Float> z = new Holder<Float>();

            float ret;
            if (testDocLiteral) {
                ret = docClient.testFloat(x, y, z);
            } else {
                ret = rpcClient.testFloat(x, y, z);
            }
            if (!perfTestOnly) {
                assertEquals(i + ": testFloat(): Wrong value for inout param", x, y.value, delta);
                assertEquals(i + ": testFloat(): Wrong value for out param", yOrig.value, z.value, delta);
                assertEquals(i + ": testFloat(): Wrong return value", x, ret, delta);
            }
        }

        float x = Float.NaN;
        Holder<Float> yOrig = new Holder<Float>(0.0f);
        Holder<Float> y = new Holder<Float>(0.0f);
        Holder<Float> z = new Holder<Float>();
        float ret;
        if (testDocLiteral) {
            ret = docClient.testFloat(x, y, z);
        } else {
            ret = rpcClient.testFloat(x, y, z);
        }
        if (!perfTestOnly) {
            assertTrue("testFloat(): Incorrect value for inout param", Float.isNaN(y.value));
            assertEquals("testFloat(): Incorrect value for out param", yOrig.value, z.value, delta);
            assertTrue("testFloat(): Incorrect return value", Float.isNaN(ret));
        }
    }

    public void testDouble() throws Exception {
        double delta = 0.0d;
        double valueSets[][] = {
            {0.0f, 1.0f},
            {-1, java.lang.Math.PI},
            {-100.0, 100.0},
            {Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY},
            //{Double.MIN_VALUE, 0},
            //{Double.MAX_VALUE,0},
        };
        for (int i = 0; i < valueSets.length; i++) {
            double x = valueSets[i][0];
            Holder<Double> yOrig = new Holder<Double>(valueSets[i][1]);
            Holder<Double> y = new Holder<Double>(valueSets[i][1]);
            Holder<Double> z = new Holder<Double>();

            double ret;
            if (testDocLiteral) {
                ret = docClient.testDouble(x, y, z);
            } else {
                ret = rpcClient.testDouble(x, y, z);
            }
            if (!perfTestOnly) {
                assertEquals("testDouble(): Incorrect value for inout param", x, y.value, delta);
                assertEquals("testDouble(): Incorrect value for out param", yOrig.value, z.value, delta);
                assertEquals("testDouble(): Incorrect return value", x, ret, delta);
            }
        }

        double x = Double.NaN;
        Holder<Double> yOrig = new Holder<Double>(0.0);
        Holder<Double> y = new Holder<Double>(0.0);
        Holder<Double> z = new Holder<Double>();
        double ret;
        if (testDocLiteral) {
            ret = docClient.testDouble(x, y, z);
        } else {
            ret = rpcClient.testDouble(x, y, z);
        }
        if (!perfTestOnly) {
            assertTrue("testDouble(): Incorrect value for inout param", Double.isNaN(y.value));
            assertEquals("testDouble(): Incorrect value for out param", yOrig.value, z.value, delta);
            assertTrue("testDouble(): Incorrect return value", Double.isNaN(ret));
        }
    }

    public void testUnsignedByte() throws Exception {
        short valueSets[][] = {{0, 1}, {1, 0},
                               {0, Byte.MAX_VALUE * 2 + 1}};

        for (int i = 0; i < valueSets.length; i++) {
            short x = valueSets[i][0];
            Holder<Short> yOrig = new Holder<Short>(valueSets[i][1]);
            Holder<Short> y = new Holder<Short>(valueSets[i][1]);
            Holder<Short> z = new Holder<Short>();

            short ret;
            if (testDocLiteral) {
                ret = docClient.testUnsignedByte(x, y, z);
            } else {
                ret = rpcClient.testUnsignedByte(x, y, z);
            }
            if (!perfTestOnly) {
                assertEquals("testUnsignedByte(): Incorrect value for inout param",
                             Short.valueOf(x), y.value);
                assertEquals("testUnsignedByte(): Incorrect value for out param",
                             yOrig.value, z.value);
                assertEquals("testUnsignedByte(): Incorrect return value", x, ret);
            }
        }
    }

    public void testBoolean() throws Exception {
        boolean valueSets[][] = {{true, false}, {true, true},
                                 {false, true}, {false, false}};

        for (int i = 0; i < valueSets.length; i++) {
            boolean x = valueSets[i][0];
            Holder<Boolean> yOrig = new Holder<Boolean>(valueSets[i][1]);
            Holder<Boolean> y = new Holder<Boolean>(valueSets[i][1]);
            Holder<Boolean> z = new Holder<Boolean>();

            boolean ret;
            if (testDocLiteral) {
                ret = docClient.testBoolean(x, y, z);
            } else {
                ret = rpcClient.testBoolean(x, y, z);
            }
            if (!perfTestOnly) {
                assertEquals("testBoolean(): Incorrect value for inout param", Boolean.valueOf(x), y.value);
                assertEquals("testBoolean(): Incorrect value for out param", yOrig.value, z.value);
                assertEquals("testBoolean(): Incorrect return value", x, ret);
            }
        }
    }

    public void testString() throws Exception {
        int bufferSize = 1000;
        StringBuffer buffer = new StringBuffer(bufferSize);
        StringBuffer buffer2 = new StringBuffer(bufferSize);
        for (int x = 0; x < bufferSize; x++) {
            buffer.append((char)('a' + (x % 26)));
            buffer2.append((char)('A' + (x % 26)));
        }
        
        String valueSets[][] = {{"hello", "world"}, {"is pi > 3 ?", " is pi < 4\\\""},
                                {"<illegal_tag/>", ""},
                                {buffer.toString(), buffer2.toString()}};

        for (int i = 0; i < valueSets.length; i++) {
            String x = valueSets[i][0];
            Holder<String> yOrig = new Holder<String>(valueSets[i][1]);
            Holder<String> y = new Holder<String>(valueSets[i][1]);
            Holder<String> z = new Holder<String>();

            String ret;
            if (testDocLiteral) {
                ret = docClient.testString(x, y, z);
            } else {
                ret = rpcClient.testString(x, y, z);
            }
            if (!perfTestOnly) {
                assertEquals("testString(): Incorrect value for inout param", x, y.value);
                assertEquals("testString(): Incorrect value for out param", yOrig.value, z.value);
                assertEquals("testString(): Incorrect return value", x, ret);
            }
        }
    }

    public void testQName() throws Exception {
        String valueSets[][] = {
            {"NoNamespaceService", ""},
            {"HelloWorldService", "http://www.iona.com/services"},
            // XXX
            //{I18NStrings.JAP_SIMPLE_STRING,"http://www.iona.com/iona"},
            {"MyService", "http://www.iona.com/iona"}
        };
        for (int i = 0; i < valueSets.length; i++) {
            QName x = new QName(valueSets[i][1], valueSets[i][0]);
            QName yOrig = new QName("http://www.iona.com/inoutqname", "InOutQName");
            Holder<QName> y = new Holder<QName>(yOrig);
            Holder<QName> z = new Holder<QName>();

            QName ret;
            if (testDocLiteral) {
                ret = docClient.testQName(x, y, z);
            } else {
                ret = rpcClient.testQName(x, y, z);
            }

            if (!perfTestOnly) {
                assertEquals("testQName(): Incorrect value for inout param", x, y.value);
                assertEquals("testQName(): Incorrect value for out param", yOrig, z.value);
                assertEquals("testQName(): Incorrect return value", x, ret);
            }
        }
    }

    public void testDate() throws Exception {
        javax.xml.datatype.DatatypeFactory datatypeFactory = javax.xml.datatype.DatatypeFactory.newInstance();

        XMLGregorianCalendar x = datatypeFactory.newXMLGregorianCalendar();
        x.setYear(1975);
        x.setMonth(5);
        x.setDay(5);
        XMLGregorianCalendar yOrig = datatypeFactory.newXMLGregorianCalendar();
        yOrig.setYear(2004);
        yOrig.setMonth(4);
        yOrig.setDay(1);

        Holder<XMLGregorianCalendar> y = new Holder<XMLGregorianCalendar>(yOrig);
        Holder<XMLGregorianCalendar> z = new Holder<XMLGregorianCalendar>();

        XMLGregorianCalendar ret;
        if (testDocLiteral) {
            ret = docClient.testDate(x, y, z);
        } else {
            // XXX - TODO getting a marshalling exception with rpc-lit for the
            // xsd:date tests.
            //ret = rpcClient.testDate(x, y, z);
            return;
        }
        if (!perfTestOnly) {
            assertTrue("testDate(): Incorrect value for inout param " + x
                       + " != " + y.value, equalsDate(x, y.value));
            assertTrue("testDate(): Incorrect value for out param", equalsDate(yOrig, z.value));
            assertTrue("testDate(): Incorrect return value", equalsDate(x, ret));
        }

        x = datatypeFactory.newXMLGregorianCalendar();
        yOrig = datatypeFactory.newXMLGregorianCalendar();

        y = new Holder<XMLGregorianCalendar>(yOrig);
        z = new Holder<XMLGregorianCalendar>();

        try {
            if (testDocLiteral) {
                ret = docClient.testDate(x, y, z);
            } else {
                ret = rpcClient.testDate(x, y, z);
            }
            fail("Expected to catch WebServiceException when calling"
                 + " testDate() with uninitialized parameters.");
        } catch (WebServiceException e) {
            // Ignore expected failure.
        }
    }

    public void testDateTime() throws Exception {
        javax.xml.datatype.DatatypeFactory datatypeFactory = javax.xml.datatype.DatatypeFactory.newInstance();

        XMLGregorianCalendar x = datatypeFactory.newXMLGregorianCalendar();
        x.setYear(1975);
        x.setMonth(5);
        x.setDay(5);
        x.setHour(12);
        x.setMinute(30);
        x.setSecond(15);
        XMLGregorianCalendar yOrig = datatypeFactory.newXMLGregorianCalendar();
        yOrig.setYear(2005);
        yOrig.setMonth(4);
        yOrig.setDay(1);
        yOrig.setHour(17);
        yOrig.setMinute(59);
        yOrig.setSecond(30);

        Holder<XMLGregorianCalendar> y = new Holder<XMLGregorianCalendar>(yOrig);
        Holder<XMLGregorianCalendar> z = new Holder<XMLGregorianCalendar>();

        XMLGregorianCalendar ret;
        if (testDocLiteral) {
            ret = docClient.testDateTime(x, y, z);
        } else {
            // XXX - TODO getting a marshalling exception with rpc-lit for the
            // xsd:date tests.
            //ret = rpcClient.testDateTime(x, y, z);
            return;
        }
        if (!perfTestOnly) {
            assertTrue("testDateTime(): Incorrect value for inout param", equalsDateTime(x, y.value));
            assertTrue("testDateTime(): Incorrect value for out param", equalsDateTime(yOrig, z.value));
            assertTrue("testDateTime(): Incorrect return value", equalsDateTime(x, ret));
        }
    }

    public void testTime() throws Exception {
        javax.xml.datatype.DatatypeFactory datatypeFactory = javax.xml.datatype.DatatypeFactory.newInstance();

        XMLGregorianCalendar x = datatypeFactory.newXMLGregorianCalendar();
        x.setHour(12);
        x.setMinute(14);
        x.setSecond(5);
        XMLGregorianCalendar yOrig = datatypeFactory.newXMLGregorianCalendar();
        yOrig.setHour(22);
        yOrig.setMinute(4);
        yOrig.setSecond(15);
        yOrig.setMillisecond(250);

        Holder<XMLGregorianCalendar> y = new Holder<XMLGregorianCalendar>(yOrig);
        Holder<XMLGregorianCalendar> z = new Holder<XMLGregorianCalendar>();

        XMLGregorianCalendar ret;
        if (testDocLiteral) {
            ret = docClient.testTime(x, y, z);
        } else {
            // XXX - TODO getting a marshalling exception with rpc-lit for the
            // xsd:date tests.
            //ret = rpcClient.testTime(x, y, z);
            return;
        }
        if (!perfTestOnly) {
            assertTrue("testTime(): Incorrect value for inout param", equalsTime(x, y.value));
            assertTrue("testTime(): Incorrect value for out param", equalsTime(yOrig, z.value));
            assertTrue("testTime(): Incorrect return value", equalsTime(x, ret));
        }
    }

    public void testGYear() throws Exception {
        javax.xml.datatype.DatatypeFactory datatypeFactory = javax.xml.datatype.DatatypeFactory.newInstance();

        XMLGregorianCalendar x = datatypeFactory.newXMLGregorianCalendar("2004");
        XMLGregorianCalendar yOrig = datatypeFactory.newXMLGregorianCalendar("2003+05:00");

        Holder<XMLGregorianCalendar> y = new Holder<XMLGregorianCalendar>(yOrig);
        Holder<XMLGregorianCalendar> z = new Holder<XMLGregorianCalendar>();

        XMLGregorianCalendar ret;
        if (testDocLiteral) {
            ret = docClient.testGYear(x, y, z);
        } else {
            // XXX - TODO getting a marshalling exception with rpc-lit for the
            // xsd:date tests.
            //ret = rpcClient.testGYear(x, y, z);
            return;
        }
        assertTrue("testGYear(): Incorrect value for inout param", x.equals(y.value));
        assertTrue("testGYear(): Incorrect value for out param", yOrig.equals(z.value));
        assertTrue("testGYear(): Incorrect return value", x.equals(ret));
    }

    public void testGYearMonth() throws Exception {
        javax.xml.datatype.DatatypeFactory datatypeFactory = javax.xml.datatype.DatatypeFactory.newInstance();

        XMLGregorianCalendar x = datatypeFactory.newXMLGregorianCalendar("2004-08");
        XMLGregorianCalendar yOrig = datatypeFactory.newXMLGregorianCalendar("2003-12+05:00");

        Holder<XMLGregorianCalendar> y = new Holder<XMLGregorianCalendar>(yOrig);
        Holder<XMLGregorianCalendar> z = new Holder<XMLGregorianCalendar>();

        XMLGregorianCalendar ret;
        if (testDocLiteral) {
            ret = docClient.testGYearMonth(x, y, z);
        } else {
            // XXX - TODO getting a marshalling exception with rpc-lit for the
            // xsd:date tests.
            //ret = rpcClient.testGYearMonth(x, y, z);
            return;
        }
        assertTrue("testGYearMonth(): Incorrect value for inout param", x.equals(y.value));
        assertTrue("testGYearMonth(): Incorrect value for out param", yOrig.equals(z.value));
        assertTrue("testGYearMonth(): Incorrect return value", x.equals(ret));
    }

    public void testGMonth() throws Exception {
        javax.xml.datatype.DatatypeFactory datatypeFactory = javax.xml.datatype.DatatypeFactory.newInstance();

        XMLGregorianCalendar x = datatypeFactory.newXMLGregorianCalendar("--08--");
        XMLGregorianCalendar yOrig = datatypeFactory.newXMLGregorianCalendar("--12--+05:00");

        Holder<XMLGregorianCalendar> y = new Holder<XMLGregorianCalendar>(yOrig);
        Holder<XMLGregorianCalendar> z = new Holder<XMLGregorianCalendar>();

        XMLGregorianCalendar ret;
        if (testDocLiteral) {
            ret = docClient.testGMonth(x, y, z);
        } else {
            // XXX - TODO getting a marshalling exception with rpc-lit for the
            // xsd:date tests.
            //ret = rpcClient.testGMonth(x, y, z);
            return;
        }
        assertTrue("testGMonth(): Incorrect value for inout param", x.equals(y.value));
        assertTrue("testGMonth(): Incorrect value for out param", yOrig.equals(z.value));
        assertTrue("testGMonth(): Incorrect return value", x.equals(ret));
    }

    public void testGMonthDay() throws Exception {
        javax.xml.datatype.DatatypeFactory datatypeFactory = javax.xml.datatype.DatatypeFactory.newInstance();

        XMLGregorianCalendar x = datatypeFactory.newXMLGregorianCalendar("--08-21");
        XMLGregorianCalendar yOrig = datatypeFactory.newXMLGregorianCalendar("--12-05+05:00");

        Holder<XMLGregorianCalendar> y = new Holder<XMLGregorianCalendar>(yOrig);
        Holder<XMLGregorianCalendar> z = new Holder<XMLGregorianCalendar>();

        XMLGregorianCalendar ret;
        if (testDocLiteral) {
            ret = docClient.testGMonthDay(x, y, z);
        } else {
            // XXX - TODO getting a marshalling exception with rpc-lit for the
            // xsd:date tests.
            //ret = rpcClient.testGMonthDay(x, y, z);
            return;
        }
        assertTrue("testGMonthDay(): Incorrect value for inout param", x.equals(y.value));
        assertTrue("testGMonthDay(): Incorrect value for out param", yOrig.equals(z.value));
        assertTrue("testGMonthDay(): Incorrect return value", x.equals(ret));
    }

    public void testGDay() throws Exception {
        javax.xml.datatype.DatatypeFactory datatypeFactory = javax.xml.datatype.DatatypeFactory.newInstance();

        XMLGregorianCalendar x = datatypeFactory.newXMLGregorianCalendar("---21");
        XMLGregorianCalendar yOrig = datatypeFactory.newXMLGregorianCalendar("---05+05:00");

        Holder<XMLGregorianCalendar> y = new Holder<XMLGregorianCalendar>(yOrig);
        Holder<XMLGregorianCalendar> z = new Holder<XMLGregorianCalendar>();

        XMLGregorianCalendar ret;
        if (testDocLiteral) {
            ret = docClient.testGDay(x, y, z);
        } else {
            // XXX - TODO getting a marshalling exception with rpc-lit for the
            // xsd:date tests.
            //ret = rpcClient.testGDay(x, y, z);
            return;
        }
        assertTrue("testGDay(): Incorrect value for inout param", x.equals(y.value));
        assertTrue("testGDay(): Incorrect value for out param", yOrig.equals(z.value));
        assertTrue("testGDay(): Incorrect return value", x.equals(ret));
    }

    public void testNormalizedString() throws Exception {
        String x = "  normalized string ";
        String yOrig = "  another normalized  string ";

        Holder<String> y = new Holder<String>(yOrig);
        Holder<String> z = new Holder<String>();

        String ret;
        if (testDocLiteral) {
            ret = docClient.testNormalizedString(x, y, z);
        } else {
            ret = rpcClient.testNormalizedString(x, y, z);
        }
        assertTrue("testNormalizedString(): Incorrect value for inout param", x.equals(y.value));
        assertTrue("testNormalizedString(): Incorrect value for out param", yOrig.equals(z.value));
        assertTrue("testNormalizedString(): Incorrect return value", x.equals(ret));
    }

    public void testToken() throws Exception {
        String x = "token";
        String yOrig = "another token";

        Holder<String> y = new Holder<String>(yOrig);
        Holder<String> z = new Holder<String>();

        String ret;
        if (testDocLiteral) {
            ret = docClient.testToken(x, y, z);
        } else {
            ret = rpcClient.testToken(x, y, z);
        }
        assertTrue("testToken(): Incorrect value for inout param", x.equals(y.value));
        assertTrue("testToken(): Incorrect value for out param", yOrig.equals(z.value));
        assertTrue("testToken(): Incorrect return value", x.equals(ret));
    }

    public void testLanguage() throws Exception {
        String x = "abc";
        String yOrig = "abc-def";

        Holder<String> y = new Holder<String>(yOrig);
        Holder<String> z = new Holder<String>();

        String ret;
        if (testDocLiteral) {
            ret = docClient.testLanguage(x, y, z);
        } else {
            ret = rpcClient.testLanguage(x, y, z);
        }
        assertTrue("testLanguage(): Incorrect value for inout param", x.equals(y.value));
        assertTrue("testLanguage(): Incorrect value for out param", yOrig.equals(z.value));
        assertTrue("testLanguage(): Incorrect return value", x.equals(ret));
    }

    public void testNMTOKEN() throws Exception {
        String x = "123:abc";
        String yOrig = "abc.-_:";

        Holder<String> y = new Holder<String>(yOrig);
        Holder<String> z = new Holder<String>();

        String ret;
        if (testDocLiteral) {
            ret = docClient.testNMTOKEN(x, y, z);
        } else {
            ret = rpcClient.testNMTOKEN(x, y, z);
        }
        assertTrue("testNMTOKEN(): Incorrect value for inout param", x.equals(y.value));
        assertTrue("testNMTOKEN(): Incorrect value for out param", yOrig.equals(z.value));
        assertTrue("testNMTOKEN(): Incorrect return value", x.equals(ret));
    }

    public void testNMTOKENS() throws Exception {
        /*
        //
        // XXX - The ri code generation produces different method
        // signatures for the NMTOKENS type between using rpc literal
        // and doc literal styles.
        //
        if (testDocLiteral) {
            List<String> x = new ArrayList<String>(1);
            x.add("123:abc");
            List<String> yOrig = new ArrayList<String>(2);
            yOrig.add("abc.-_:");
            yOrig.add("a");

            Holder<List<String>> y = new Holder<List<String>>(yOrig);
            Holder<List<String>> z = new Holder<List<String>>();

            List<String> ret = docClient.testNMTOKENS(x, y, z);
            assertTrue("testNMTOKENS(): Incorrect value for inout param", x.equals(y.value));
            assertTrue("testNMTOKENS(): Incorrect value for out param", yOrig.equals(z.value));
            assertTrue("testNMTOKENS(): Incorrect return value", x.equals(ret));
        } else {
            String[] x = new String[1];
            x[0] = "123:abc";
            String[] yOrig = new String[2];
            yOrig[0] = "abc.-_:";
            yOrig[1] = "a";

            Holder<String[]> y = new Holder<String[]>(yOrig);
            Holder<String[]> z = new Holder<String[]>();

            String[] ret = rpcClient.testNMTOKENS(x, y, z);
            assertTrue("testNMTOKENS(): Incorrect value for inout param", x.equals(y.value));
            assertTrue("testNMTOKENS(): Incorrect value for out param", yOrig.equals(z.value));
            assertTrue("testNMTOKENS(): Incorrect return value", x.equals(ret));
        }
        */
    }
    
    public void testName() throws Exception {
        String x = "abc:123";
        String yOrig = "abc.-_";

        Holder<String> y = new Holder<String>(yOrig);
        Holder<String> z = new Holder<String>();

        String ret;
        if (testDocLiteral) {
            ret = docClient.testName(x, y, z);
        } else {
            ret = rpcClient.testName(x, y, z);
        }
        assertTrue("testName(): Incorrect value for inout param", x.equals(y.value));
        assertTrue("testName(): Incorrect value for out param", yOrig.equals(z.value));
        assertTrue("testName(): Incorrect return value", x.equals(ret));
    }

    public void testNCName() throws Exception {
        String x = "abc-123";
        String yOrig = "abc.-";

        Holder<String> y = new Holder<String>(yOrig);
        Holder<String> z = new Holder<String>();

        String ret;
        if (testDocLiteral) {
            ret = docClient.testNCName(x, y, z);
        } else {
            ret = rpcClient.testNCName(x, y, z);
        }
        assertTrue("testNCName(): Incorrect value for inout param", x.equals(y.value));
        assertTrue("testNCName(): Incorrect value for out param", yOrig.equals(z.value));
        assertTrue("testNCName(): Incorrect return value", x.equals(ret));
    }

    public void testID() throws Exception {
        String valueSets[][] = {
            {"root.id-testartix.2", "L.-type_test"},
            {"_iona.com", "zoo-5_wolf"},
            {"x-_liberty", "_-.-_"}
        };

        for (int i = 0; i < valueSets.length; i++) {
            String x = valueSets[i][0];
            Holder<String> yOrig = new Holder<String>(valueSets[i][1]);
            Holder<String> y = new Holder<String>(valueSets[i][1]);
            Holder<String> z = new Holder<String>();

            String ret;
            if (testDocLiteral) {
                ret = docClient.testID(x, y, z);
            } else {
                ret = rpcClient.testID(x, y, z);
            }
            if (!perfTestOnly) {
                assertEquals("testID(): Incorrect value for inout param", x, y.value);
                assertEquals("testID(): Incorrect value for out param", yOrig.value, z.value);
                assertEquals("testID(): Incorrect return value", x, ret);
            }
        }
    }

    public void testDecimal() throws Exception {
        BigDecimal valueSets[][] = {
            {new BigDecimal("-1234567890.000000"), new BigDecimal("1234567890.000000")},
            {new BigDecimal("-" + String.valueOf(Long.MAX_VALUE * Long.MAX_VALUE) + ".000000"),
             new BigDecimal(String.valueOf(Long.MAX_VALUE * Long.MAX_VALUE) + ".000000")}
        };

        for (int i = 0; i < valueSets.length; i++) {
            BigDecimal x = valueSets[i][0];
            Holder<BigDecimal> yOrig = new Holder<BigDecimal>(valueSets[i][1]);
            Holder<BigDecimal> y = new Holder<BigDecimal>(valueSets[i][1]);
            Holder<BigDecimal> z = new Holder<BigDecimal>();

            BigDecimal ret;
            if (testDocLiteral) {
                ret = docClient.testDecimal(x, y, z);
            } else {
                ret = rpcClient.testDecimal(x, y, z);
            }
            if (!perfTestOnly) {
                assertEquals("testDecimal(): Incorrect value for inout param", x, y.value);
                assertEquals("testDecimal(): Incorrect value for out param", yOrig.value, z.value);
                assertEquals("testDecimal(): Incorrect return value", x, ret);
            }
        }
    }

    public void testInteger() throws Exception {
        BigInteger valueSets[][] = {
            {new BigInteger("-1234567890"), new BigInteger("1234567890")},
            {new BigInteger("-" + String.valueOf(Long.MAX_VALUE * Long.MAX_VALUE)),
             new BigInteger(String.valueOf(Long.MAX_VALUE * Long.MAX_VALUE))}
        };

        for (int i = 0; i < valueSets.length; i++) {
            BigInteger x = valueSets[i][0];
            Holder<BigInteger> yOrig = new Holder<BigInteger>(valueSets[i][1]);
            Holder<BigInteger> y = new Holder<BigInteger>(valueSets[i][1]);
            Holder<BigInteger> z = new Holder<BigInteger>();

            BigInteger ret;
            if (testDocLiteral) {
                ret = docClient.testInteger(x, y, z);
            } else {
                ret = rpcClient.testInteger(x, y, z);
            }
            if (!perfTestOnly) {
                assertEquals("testInteger(): Incorrect value for inout param", x, y.value);
                assertEquals("testInteger(): Incorrect value for out param", yOrig.value, z.value);
                assertEquals("testInteger(): Incorrect return value", x, ret);
            }
        }
    }

    public void testPositiveInteger() throws Exception {
        BigInteger valueSets[][] = {
            {new BigInteger("1"), new BigInteger("1234567890")},
            {new BigInteger(String.valueOf(Integer.MAX_VALUE * Integer.MAX_VALUE)),
             new BigInteger(String.valueOf(Long.MAX_VALUE * Long.MAX_VALUE))}
        };

        for (int i = 0; i < valueSets.length; i++) {
            BigInteger x = valueSets[i][0];
            Holder<BigInteger> yOrig = new Holder<BigInteger>(valueSets[i][1]);
            Holder<BigInteger> y = new Holder<BigInteger>(valueSets[i][1]);
            Holder<BigInteger> z = new Holder<BigInteger>();

            BigInteger ret;
            if (testDocLiteral) {
                ret = docClient.testPositiveInteger(x, y, z);
            } else {
                ret = rpcClient.testPositiveInteger(x, y, z);
            }
            if (!perfTestOnly) {
                assertEquals("testPositiveInteger(): Incorrect value for inout param", x, y.value);
                assertEquals("testPositiveInteger(): Incorrect value for out param", yOrig.value, z.value);
                assertEquals("testPositiveInteger(): Incorrect return value", x, ret);
            }
        }
    }

    public void testNonPositiveInteger() throws Exception {
        BigInteger valueSets[][] = {
            {new BigInteger("0"), new BigInteger("-1234567890")},
            {new BigInteger("-" + String.valueOf(Integer.MAX_VALUE * Integer.MAX_VALUE)),
             new BigInteger("-" + String.valueOf(Long.MAX_VALUE * Long.MAX_VALUE))}
        };

        for (int i = 0; i < valueSets.length; i++) {
            BigInteger x = valueSets[i][0];
            Holder<BigInteger> yOrig = new Holder<BigInteger>(valueSets[i][1]);
            Holder<BigInteger> y = new Holder<BigInteger>(valueSets[i][1]);
            Holder<BigInteger> z = new Holder<BigInteger>();

            BigInteger ret;
            if (testDocLiteral) {
                ret = docClient.testNonPositiveInteger(x, y, z);
            } else {
                ret = rpcClient.testNonPositiveInteger(x, y, z);
            }
            if (!perfTestOnly) {
                assertEquals("testNonPositiveInteger(): Incorrect value for inout param", x, y.value);
                assertEquals("testNonPositiveInteger(): Incorrect value for out param", yOrig.value, z.value);
                assertEquals("testNonPositiveInteger(): Incorrect return value", x, ret);
            }
        }
    }

    public void testNegativeInteger() throws Exception {
        BigInteger valueSets[][] = {
            {new BigInteger("-1"), new BigInteger("-1234567890")},
            {new BigInteger("-" + String.valueOf(Integer.MAX_VALUE * Integer.MAX_VALUE)),
             new BigInteger("-" + String.valueOf(Long.MAX_VALUE * Long.MAX_VALUE))}
        };

        for (int i = 0; i < valueSets.length; i++) {
            BigInteger x = valueSets[i][0];
            Holder<BigInteger> yOrig = new Holder<BigInteger>(valueSets[i][1]);
            Holder<BigInteger> y = new Holder<BigInteger>(valueSets[i][1]);
            Holder<BigInteger> z = new Holder<BigInteger>();

            BigInteger ret;
            if (testDocLiteral) {
                ret = docClient.testNegativeInteger(x, y, z);
            } else {
                ret = rpcClient.testNegativeInteger(x, y, z);
            }
            if (!perfTestOnly) {
                assertEquals("testNegativeInteger(): Incorrect value for inout param", x, y.value);
                assertEquals("testNegativeInteger(): Incorrect value for out param", yOrig.value, z.value);
                assertEquals("testNegativeInteger(): Incorrect return value", x, ret);
            }
        }
    }

    public void testNonNegativeInteger() throws Exception {
        BigInteger valueSets[][] = {
            {new BigInteger("0"), new BigInteger("1234567890")},
            {new BigInteger(String.valueOf(Integer.MAX_VALUE * Integer.MAX_VALUE)),
             new BigInteger(String.valueOf(Long.MAX_VALUE * Long.MAX_VALUE))}
        };

        for (int i = 0; i < valueSets.length; i++) {
            BigInteger x = valueSets[i][0];
            Holder<BigInteger> yOrig = new Holder<BigInteger>(valueSets[i][1]);
            Holder<BigInteger> y = new Holder<BigInteger>(valueSets[i][1]);
            Holder<BigInteger> z = new Holder<BigInteger>();

            BigInteger ret;
            if (testDocLiteral) {
                ret = docClient.testNonNegativeInteger(x, y, z);
            } else {
                ret = rpcClient.testNonNegativeInteger(x, y, z);
            }
            if (!perfTestOnly) {
                assertEquals("testNonNegativeInteger(): Incorrect value for inout param", x, y.value);
                assertEquals("testNonNegativeInteger(): Incorrect value for out param", yOrig.value, z.value);
                assertEquals("testNonNegativeInteger(): Incorrect return value", x, ret);
            }
        }
    }

    public void testHexBinary() throws Exception {
        byte[] x = "hello".getBytes();
        Holder<byte[]> y = new Holder<byte[]>("goodbye".getBytes());
        Holder<byte[]> yOriginal = new Holder<byte[]>("goodbye".getBytes());
        Holder<byte[]> z = new Holder<byte[]>();
        byte[] ret;
        if (testDocLiteral) {
            ret = docClient.testHexBinary(x, y, z);
        } else {
            ret = rpcClient.testHexBinary(x, y, z);
        }
        if (!perfTestOnly) {
            assertTrue("testHexBinary(): Incorrect value for inout param",
                       Arrays.equals(x, y.value));
            assertTrue("testHexBinary(): Incorrect value for out param",
                       Arrays.equals(yOriginal.value, z.value));
            assertTrue("testHexBinary(): Incorrect return value",
                       Arrays.equals(x, ret));
        }
    }

    public void testBase64Binary() throws Exception {
        byte[] x = "hello".getBytes();
        Holder<byte[]> y = new Holder<byte[]>("goodbye".getBytes());
        Holder<byte[]> yOriginal = new Holder<byte[]>("goodbye".getBytes());
        Holder<byte[]> z = new Holder<byte[]>();
        byte[] ret;
        if (testDocLiteral) {
            ret = docClient.testBase64Binary(x, y, z);
        } else {
            ret = rpcClient.testBase64Binary(x, y, z);
        }
        if (!perfTestOnly) {
            assertTrue("testBase64Binary(): Incorrect value for inout param",
                       Arrays.equals(x, y.value));
            assertTrue("testBase64Binary(): Incorrect value for out param",
                       Arrays.equals(yOriginal.value, z.value));
            assertTrue("testBase64Binary(): Incorrect return value",
                       Arrays.equals(x, ret));
        }

        // Test uninitialized holder value
        try {
            y = new Holder<byte[]>();
            z = new Holder<byte[]>();
            if (testDocLiteral) {
                docClient.testBase64Binary(x, y, z);
            } else {
                rpcClient.testBase64Binary(x, y, z);
            }
            fail("Uninitialized Holder for inout parameter should have thrown an error.");
        } catch (Exception e) {
            // Ignore expected failure.
        }
    }

    /*
     * XXX - TODO
     *
    public void testanyURI() throws Exception {
        String valueSets[][] = {
            {"file:///root%20%20/-;?&+", "file:///w:/test!artix~java*"},
            {"http://iona.com/", "file:///z:/mail_iona=com,\'xmlbus\'"},
            {"mailto:windows@systems", "file:///"}
        };

        for (int i = 0; i < valueSets.length; i++) {
            URI x = new URI(valueSets[i][0]);
            Holder<URI> yOrig = new Holder<URI>(new URI(valueSets[i][1]));
            Holder<URI> y = new Holder<URI>(new URI(valueSets[i][1]));
            Holder<URI> z = new Holder<URI>();

            URI ret;
            if (testDocLiteral) {
                ret = docClient.testanyURI(x, y, z);
            } else {
                ret = rpcClient.testanyURI(x, y, z);
            }
            if (!perfTestOnly) {
                assertEquals("testanyURI(): Incorrect value for inout param",
                    x.toString(), y.value.toString());
                assertEquals("testanyURI(): Incorrect value for out param",
                    yOrig.value.toString(), z.value.toString());
                assertEquals("testanyURI(): Incorrect return value",
                    x.toString(), ret.toString());
            }
        }
    }
    */
    
}

