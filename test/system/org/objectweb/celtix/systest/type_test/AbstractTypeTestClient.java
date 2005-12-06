package org.objectweb.celtix.systest.type_test;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;
import javax.xml.ws.Holder;
import javax.xml.ws.WebServiceException;

import org.objectweb.celtix.BusException;
import org.objectweb.celtix.systest.common.ClientServerTestBase;
//import org.objectweb.type_test.types.ColourEnum;
import org.objectweb.type_test.types.DecimalEnum;
import org.objectweb.type_test.types.NMTokenEnum;
import org.objectweb.type_test.types.NumberEnum;
import org.objectweb.type_test.types.StringEnum;

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
            docClient = docService.getPort(portName, org.objectweb.type_test.doc.TypeTestPortType.class);
            assertNotNull(docClient);
        } else {
            org.objectweb.type_test.rpc.SOAPService rpcService =
                new org.objectweb.type_test.rpc.SOAPService(wsdlLocation, serviceName);
            rpcClient = rpcService.getPort(portName, org.objectweb.type_test.rpc.TypeTestPortType.class);
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
            // xsd:date tests (ClassCastException in jaxb).
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
            // xsd:date tests (ClassCastException in jaxb).
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
            // xsd:date tests (ClassCastException in jaxb).
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
            // xsd:date tests (ClassCastException in jaxb).
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
            // xsd:date tests (ClassCastException in jaxb).
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
            // xsd:date tests (ClassCastException in jaxb).
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
            // xsd:date tests (ClassCastException in jaxb).
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
        // XXX - The jaxb ri code generation produces different method
        // signatures for the NMTOKENS type between using rpc literal
        // and doc literal styles.
        //
        if (testDocLiteral) {
            List<String> x = Arrays.asList("123:abc");
            List<String> yOrig = Arrays.asList("abc.-_:", "a");

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
     * XXX - TODO - need to customize anyURI type.
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
    
    /**
     * XXX - In the generated code for ColourEnum, the fromValue()
     * method is not declared static - not so easy to create a
     * ColourEnum instance!
     */
    public void testColourEnum() throws Exception {
        /*
        String[] xx = {"RED", "GREEN", "BLUE"};
        String[] yy = {"GREEN", "BLUE", "RED"};

        Holder<ColourEnum> z = new Holder<ColourEnum>();

        for (int i = 0; i < 3; i++) {
            ColourEnum x = ColourEnum.fromValue(xx[i]);
            ColourEnum yOrig = ColourEnum.fromValue(yy[i]);
            Holder<ColourEnum> y = new Holder<ColourEnum>(yOrig);

            ColourEnum ret;
                ret = rpcClient.testColourEnum(x, y, z);
                ret = docClient.testColourEnum(x, y, z);
            if (!perfTestOnly) {
                assertEquals("testColourEnum(): Incorrect value for inout param",
                             x.value(), y.value.value());
                assertEquals("testColourEnum(): Incorrect value for out param",
                             yOrig.value(), z.value.value());
                assertEquals("testColourEnum(): Incorrect return value",
                             x.value(), ret.value());
            }
        }
        */
    }
    
    public void testNumberEnum() throws Exception {
        int[] xx = {1, 2, 3};
        int[] yy = {3, 1, 2};

        Holder<NumberEnum> z = new Holder<NumberEnum>();

        for (int i = 0; i < 3; i++) {
            NumberEnum x = NumberEnum.fromValue(xx[i]);
            NumberEnum yOrig = NumberEnum.fromValue(yy[i]);
            Holder<NumberEnum> y = new Holder<NumberEnum>(yOrig);

            NumberEnum ret;
            if (testDocLiteral) {
                ret = docClient.testNumberEnum(x, y, z);
            } else {
                ret = rpcClient.testNumberEnum(x, y, z);
            }
            if (!perfTestOnly) {
                assertEquals("testNumberEnum(): Incorrect value for inout param",
                             x.value(), y.value.value());
                assertEquals("testNumberEnum(): Incorrect value for out param",
                             yOrig.value(), z.value.value());
                assertEquals("testNumberEnum(): Incorrect return value",
                             x.value(), ret.value());
            }
        }
    }
    
    public void testStringEnum() throws Exception {
        String[] xx = {"a b c", "d e f", "g h i"};
        String[] yy = {"g h i", "a b c", "d e f"};

        Holder<StringEnum> z = new Holder<StringEnum>();
        for (int i = 0; i < 3; i++) {
            StringEnum x = StringEnum.fromValue(xx[i]);
            StringEnum yOrig = StringEnum.fromValue(yy[i]);
            Holder<StringEnum> y = new Holder<StringEnum>(yOrig);

            StringEnum ret;
            if (testDocLiteral) {
                ret = docClient.testStringEnum(x, y, z);
            } else {
                ret = rpcClient.testStringEnum(x, y, z);
            }
            if (!perfTestOnly) {
                assertEquals("testStringEnum(): Incorrect value for inout param",
                             x.value(), y.value.value());
                assertEquals("testStringEnum(): Incorrect value for out param",
                             yOrig.value(), z.value.value());
                assertEquals("testStringEnum(): Incorrect return value",
                             x.value(), ret.value());
            }
        }
    }
    
    public void testDecimalEnum() throws Exception {
        BigDecimal[] xx = {new BigDecimal("-10.34"),
                           new BigDecimal("11.22"),
                           new BigDecimal("14.55")};
        BigDecimal[] yy = {new BigDecimal("14.55"),
                           new BigDecimal("-10.34"),
                           new BigDecimal("11.22")};

        Holder<DecimalEnum> z = new Holder<DecimalEnum>();

        for (int i = 0; i < 3; i++) {
            DecimalEnum x = DecimalEnum.fromValue(xx[i]);
            DecimalEnum yOrig = DecimalEnum.fromValue(yy[i]);
            Holder<DecimalEnum> y = new Holder<DecimalEnum>(yOrig);

            DecimalEnum ret;
            if (testDocLiteral) {
                ret = docClient.testDecimalEnum(x, y, z);
            } else {
                ret = rpcClient.testDecimalEnum(x, y, z);
            }
            if (!perfTestOnly) {
                assertEquals("testDecimalEnum(): Incorrect value for inout param",
                             x.value(), y.value.value());
                assertEquals("testDecimalEnum(): Incorrect value for out param",
                             yOrig.value(), z.value.value());
                assertEquals("testDecimalEnum(): Incorrect return value",
                             x.value(), ret.value());
            }
        }
    }
    
    public void testNMTokenEnum() throws Exception {
        String[] xx = {"hello", "there"};
        String[] yy = {"there", "hello"};

        Holder<NMTokenEnum> z = new Holder<NMTokenEnum>();

        for (int i = 0; i < 2; i++) {
            NMTokenEnum x = NMTokenEnum.fromValue(xx[i]);
            NMTokenEnum yOrig = NMTokenEnum.fromValue(yy[i]);
            Holder<NMTokenEnum> y = new Holder<NMTokenEnum>(yOrig);

            NMTokenEnum ret;
            if (testDocLiteral) {
                ret = docClient.testNMTokenEnum(x, y, z);
            } else {
                ret = rpcClient.testNMTokenEnum(x, y, z);
            }
            if (!perfTestOnly) {
                assertEquals("testNMTokenEnum(): Incorrect value for inout param",
                             x.value(), y.value.value());
                assertEquals("testNMTokenEnum(): Incorrect value for out param",
                             yOrig.value(), z.value.value());
                assertEquals("testNMTokenEnum(): Incorrect return value",
                             x.value(), ret.value());
            }
        }
    }
    
    public void testSimpleRestriction() throws Exception {
        // normal case, maxLength=10
        String x = "string_x";
        String yOrig = "string_y";
        Holder<String> y = new Holder<String>(yOrig);
        Holder<String> z = new Holder<String>();
        String ret;
        if (testDocLiteral) {
            ret = docClient.testSimpleRestriction(x, y, z);
        } else {
            ret = rpcClient.testSimpleRestriction(x, y, z);
        }
        if (!perfTestOnly) {
            assertEquals("testSimpleRestriction(): Incorrect value for inout param", x, y.value);
            assertEquals("testSimpleRestriction(): Incorrect value for out param", yOrig, z.value);
            assertEquals("testSimpleRestriction(): Incorrect return value", x, ret);
        }
        
        // abnormal case
        /* XXX - TODO - restrictions are not enforced.
        x = "string_xxxxx";
        y = new Holder<String>(yOrig);
        z = new Holder<String>();
        try {
            if (testDocLiteral) {
                ret = docClient.testSimpleRestriction(x, y, z);
            } else {
                ret = rpcClient.testSimpleRestriction(x, y, z);
            }
            fail("maxLength=10 restriction is violated.");
        } catch (Exception ex) {
            //ex.printStackTrace();
        }

        // abnormal case
        x = "string_x";
        yOrig = "string_yyyyyy";
        y = new Holder<String>(yOrig);
        z = new Holder<String>();
        try {
            if (testDocLiteral) {
                ret = docClient.testSimpleRestriction(x, y, z);
            } else {
                ret = rpcClient.testSimpleRestriction(x, y, z);
            }
            fail("maxLength=10 restriction is violated.");
        } catch (Exception ex) {
            //ex.printStackTrace();
        }
        */
    }

    public void testSimpleRestriction2() throws Exception {
        // normal case, minLength=5
        String x = "str_x";
        String yOrig = "string_yyy";
        Holder<String> y = new Holder<String>(yOrig);
        Holder<String> z = new Holder<String>();

        String ret;
        if (testDocLiteral) {
            ret = docClient.testSimpleRestriction2(x, y, z);
        } else {
            ret = rpcClient.testSimpleRestriction2(x, y, z);
        }
        if (!perfTestOnly) {
            assertEquals("testSimpleRestriction2(): Incorrect value for inout param", x, y.value);
            assertEquals("testSimpleRestriction2(): Incorrect value for out param", yOrig, z.value);
            assertEquals("testSimpleRestriction2(): Incorrect return value", x, ret);
        }
        
        // abnormal case
        /* XXX - TODO - restrictions are not enforced.
        x = "str";
        y = new Holder<String>(yOrig);
        z = new Holder<String>();
        try {
            if (testDocLiteral) {
                ret = docClient.testSimpleRestriction2(x, y, z);
            } else {
                ret = rpcClient.testSimpleRestriction2(x, y, z);
            }
            fail("minLength=5 restriction is violated.");
        } catch (Exception ex) {
            //ex.printStackTrace();
        }
        */
    }

    public void testSimpleRestriction3() throws Exception {
        // normal case, maxLength=10 && minLength=5
        String x = "str_x";
        String yOrig = "string_yyy";
        Holder<String> y = new Holder<String>(yOrig);
        Holder<String> z = new Holder<String>();

        String ret;
        if (testDocLiteral) {
            ret = docClient.testSimpleRestriction3(x, y, z);
        } else {
            ret = rpcClient.testSimpleRestriction3(x, y, z);
        }
        if (!perfTestOnly) {
            assertEquals("testSimpleRestriction3(): Incorrect value for inout param", x, y.value);
            assertEquals("testSimpleRestriction3(): Incorrect value for out param", yOrig, z.value);
            assertEquals("testSimpleRestriction3(): Incorrect return value", x, ret);
        }
        
        // abnormal case
        /* XXX - TODO - restrictions are not enforced.
        x = "str";
        y = new Holder<String>(yOrig);
        z = new Holder<String>();
        try {
            if (testDocLiteral) {
                ret = docClient.testSimpleRestriction3(x, y, z);
            } else {
                ret = rpcClient.testSimpleRestriction3(x, y, z);
            }
            fail("maxLength=10 && minLength=5 restriction is violated.");
        } catch (Exception ex) {
            //ex.printStackTrace();
        }

        // abnormal case
        x = "string_x";
        yOrig = "string_yyyyyy";
        y = new Holder<String>(yOrig);
        z = new Holder<String>();
        try {
            ret = rpcClient.testSimpleRestriction3(x, y, z);
            fail("maxLength=10 && minLength=5 restriction is violated.");
        } catch (Exception ex) {
            //ex.printStackTrace();
        }
        */
    }

    public void testSimpleRestriction4() throws Exception {
        // normal case, length=1
        String x = "x";
        String yOrig = "y";
        Holder<String> y = new Holder<String>(yOrig);
        Holder<String> z = new Holder<String>();

        String ret;
        if (testDocLiteral) {
            ret = docClient.testSimpleRestriction4(x, y, z);
        } else {
            ret = rpcClient.testSimpleRestriction4(x, y, z);
        }
        if (!perfTestOnly) {
            assertEquals("testSimpleRestriction4(): Incorrect value for inout param", x, y.value);
            assertEquals("testSimpleRestriction4(): Incorrect value for out param", yOrig, z.value);
            assertEquals("testSimpleRestriction4(): Incorrect return value", x, ret);
        }
        
        // abnormal case
        /* XXX - TODO - restrictions are not enforced.
        x = "str";
        y = new Holder<String>(yOrig);
        z = new Holder<String>();
        try {
            if (testDocLiteral) {
                ret = docClient.testSimpleRestriction4(x, y, z);
            } else {
                ret = rpcClient.testSimpleRestriction4(x, y, z);
            }
            fail("minLength=5 restriction is violated.");
        } catch (Exception ex) {
            //ex.printStackTrace();
        }
        */
    }

    public void testSimpleRestriction5() throws Exception {
        // normal case, maxLength=10 for SimpleRestrction
        // && minLength=5 for SimpleRestriction5
        String x = "str_x";
        String yOrig = "string_yyy";
        Holder<String> y = new Holder<String>(yOrig);
        Holder<String> z = new Holder<String>();

        String ret;
        if (testDocLiteral) {
            ret = docClient.testSimpleRestriction5(x, y, z);
        } else {
            ret = rpcClient.testSimpleRestriction5(x, y, z);
        }
        if (!perfTestOnly) {
            assertEquals("testSimpleRestriction5(): Incorrect value for inout param", x, y.value);
            assertEquals("testSimpleRestriction5(): Incorrect value for out param", yOrig, z.value);
            assertEquals("testSimpleRestriction5(): Incorrect return value", x, ret);
        }
        
        // abnormal case
        /* XXX - TODO - restrictions are not enforced.
        x = "str";
        y = new Holder<String>(yOrig);
        z = new Holder<String>();
        try {
            if (testDocLiteral) {
                ret = docClient.testSimpleRestriction5(x, y, z);
            } else {
                ret = rpcClient.testSimpleRestriction5(x, y, z);
            }
            fail("maxLength=10 && minLength=5 restriction is violated.");
        } catch (Exception ex) {
            //ex.printStackTrace();
        }

        // abnormal case
        x = "string_x";
        yOrig = "string_yyyyyy";
        y = new Holder<String>(yOrig);
        z = new Holder<String>();
        try {
            if (testDocLiteral) {
                ret = docClient.testSimpleRestriction5(x, y, z);
            } else {
                ret = rpcClient.testSimpleRestriction5(x, y, z);
            }
            fail("maxLength=10 && minLength=5 restriction is violated.");
        } catch (Exception ex) {
            //ex.printStackTrace();
        }
        */
    }

    public void testSimpleRestriction6() throws Exception {
        // normal case, maxLength=10 for SimpleRestrction
        // && maxLength=5 for SimpleRestriction6
        String x = "str_x";
        String yOrig = "y";
        Holder<String> y = new Holder<String>(yOrig);
        Holder<String> z = new Holder<String>();

        String ret;
        if (testDocLiteral) {
            ret = docClient.testSimpleRestriction6(x, y, z);
        } else {
            ret = rpcClient.testSimpleRestriction6(x, y, z);
        }
        if (!perfTestOnly) {
            assertEquals("testSimpleRestriction6(): Incorrect value for inout param", x, y.value);
            assertEquals("testSimpleRestriction6(): Incorrect value for out param", yOrig, z.value);
            assertEquals("testSimpleRestriction6(): Incorrect return value", x, ret);
        }
        
        // abnormal case
        /* XXX - TODO - restrictions are not enforced.
        x = "string_x";
        yOrig = "string_y";
        y = new Holder<String>(yOrig);
        z = new Holder<String>();
        try {
            if (testDocLiteral) {
                ret = docClient.testSimpleRestriction6(x, y, z);
            } else {
                ret = rpcClient.testSimpleRestriction6(x, y, z);
            }
            fail("maxLength=10 && minLength=5 restriction is violated.");
        } catch (Exception ex) {
            //ex.printStackTrace();
        }
        */
    }

    public void testHexBinaryRestriction() throws Exception {
        // normal case, maxLength=10 && minLength=1
        byte[] x = "x".getBytes();
        byte[] yOrig = "string_yyy".getBytes();
        Holder<byte[]> y = new Holder<byte[]>(yOrig);
        Holder<byte[]> z = new Holder<byte[]>();

        byte[] ret;
        if (testDocLiteral) {
            ret = docClient.testHexBinaryRestriction(x, y, z);
        } else {
            ret = rpcClient.testHexBinaryRestriction(x, y, z);
        }
        if (!perfTestOnly) {
            assertTrue("testHexBinaryRestriction(): Incorrect value for inout param",
                       equals(x, y.value));
            assertTrue("testHexBinaryRestriction(): Incorrect value for out param",
                       equals(yOrig, z.value));
            assertTrue("testHexBinaryRestriction(): Incorrect return value", equals(x, ret));
        }

        // abnormal case
        /* XXX - TODO - restrictions are not enforced.
        x = "".getBytes();
        y = new Holder<byte[]>(yOrig);
        z = new Holder<byte[]>();
        try {
            if (testDocLiteral) {
                ret = docClient.testHexBinaryRestriction(x, y, z);
            } else {
                ret = rpcClient.testHexBinaryRestriction(x, y, z);
            }
            fail("maxLength=10 && minLength=1 restriction is violated.");
        } catch (Exception ex) {
            //ex.printStackTrace();
        }

        // abnormal case
        x = "string_x".getBytes();
        yOrig = "string_yyyyyy".getBytes();
        y = new Holder<byte[]>(yOrig);
        z = new Holder<byte[]>();
        try {
            if (testDocLiteral) {
                ret = docClient.testHexBinaryRestriction(x, y, z);
            } else {
                ret = rpcClient.testHexBinaryRestriction(x, y, z);
            }
            fail("maxLength=10 && minLength=1 restriction is violated.");
        } catch (Exception ex) {
            //ex.printStackTrace();
        }
        */
    }
    
    protected boolean equals(byte[] x, byte[] y) {
        String xx = new String(x);
        String yy = new String(y);
        return xx.equals(yy);
    }
    
    public void testBase64BinaryRestriction() throws Exception {
        //      normal case, length=10
        byte[] x = "string_xxx".getBytes();
        byte[] yOrig = "string_yyy".getBytes();
        Holder<byte[]> y = new Holder<byte[]>(yOrig);
        Holder<byte[]> z = new Holder<byte[]>();

        byte[] ret;
        if (testDocLiteral) {
            ret = docClient.testBase64BinaryRestriction(x, y, z);
        } else {
            ret = rpcClient.testBase64BinaryRestriction(x, y, z);
        }
        if (!perfTestOnly) {
            assertTrue("testBase64BinaryRestriction(): Incorrect value for inout param",
                       equals(x, y.value));
            assertTrue("testBase64BinaryRestriction(): Incorrect value for out param",
                       equals(yOrig, z.value));
            assertTrue("testBase64BinaryRestriction(): Incorrect return value", equals(x, ret));
        }

        // abnormal case
        /* XXX - TODO - restrictions are not enforced.
        x = "string_xxxxx".getBytes();
        y = new Holder<byte[]>(yOrig);
        z = new Holder<byte[]>();
        try {
            if (testDocLiteral) {
                ret = docClient.testBase64BinaryRestriction(x, y, z);
            } else {
                ret = rpcClient.testBase64BinaryRestriction(x, y, z);
            }
            fail("length=10 restriction is violated.");
        } catch (Exception ex) {
            //ex.printStackTrace();
        }
        */
    }
    
    public void testSimpleListRestriction2() throws Exception {
        // XXX - jaxb ri generated code has different method signature
        //       between doc-literal and rpc-literal styles.
        if (testDocLiteral) {
            List<String> x = Arrays.asList("I", "am", "SimpleList");
            List<String> yOrig = Arrays.asList("Does", "SimpleList", "Work");
            Holder< List<String> > y = new Holder< List<String> >(yOrig);
            Holder< List<String> > z = new Holder< List<String> >();

            // normal case, maxLength=10 && minLength=1
            List<String> ret = docClient.testSimpleListRestriction2(x, y, z);
            if (!perfTestOnly) {
                assertTrue("testStringList(): Incorrect value for inout param", x.equals(y.value));
                assertTrue("testStringList(): Incorrect value for out param", yOrig.equals(z.value));
                assertTrue("testStringList(): Incorrect return value", x.equals(ret));
            }

            // abnormal case
            /* XXX - TODO - restrictions are not enforced.
            x = Arrays.asList("");
            y = new Holder< List<String> >(yOrig);
            z = new Holder< List<String> >();
            try {
                ret = docClient.testSimpleListRestriction2(x, y, z);
                fail("length=10 restriction is violated.");
            } catch (Exception ex) {
                //ex.printStackTrace();
            }
            */
        } else {
            String[] x = {"I", "am", "SimpleList"};
            String[] yOrig = {"Does", "SimpleList", "Work"};
            Holder<String[]> y = new Holder<String[]>(yOrig);
            Holder<String[]> z = new Holder<String[]>();

            // normal case, maxLength=10 && minLength=1
            String[] ret = rpcClient.testSimpleListRestriction2(x, y, z);

            assertTrue(y.value.length == 3);
            assertTrue(z.value.length == 3);
            assertTrue(ret.length == 3);
            if (!perfTestOnly) {
                for (int i = 0; i < 3; i++) {
                    assertEquals("testStringList(): Incorrect value for inout param", x[i], y.value[i]);
                    assertEquals("testStringList(): Incorrect value for out param", yOrig[i], z.value[i]);
                    assertEquals("testStringList(): Incorrect return value", x[i], ret[i]);
                }
            }
            
            // abnormal case
            /* XXX - TODO - restrictions are not enforced.
            x = new String[0];
            y = new Holder<String[]>(yOrig);
            z = new Holder<String[]>();
            try {
                ret = rpcClient.testSimpleListRestriction2(x, y, z);
                fail("length=10 restriction is violated.");
            } catch (Exception ex) {
                //ex.printStackTrace();
            }
            */
        }
    }
    
    public void testStringList() throws Exception {
        if (testDocLiteral) {
            List<String> x = Arrays.asList("I", "am", "SimpleList");
            List<String> yOrig = Arrays.asList("Does", "SimpleList", "Work");
            Holder< List<String> > y = new Holder< List<String> >(yOrig);
            Holder< List<String> > z = new Holder< List<String> >();

            List<String> ret = docClient.testStringList(x, y, z);
            if (!perfTestOnly) {
                assertTrue("testStringList(): Incorrect value for inout param", x.equals(y.value));
                assertTrue("testStringList(): Incorrect value for out param", yOrig.equals(z.value));
                assertTrue("testStringList(): Incorrect return value", x.equals(ret));
            }
        } else {
            String[] x = {"I", "am", "SimpleList"};
            String[] yOrig = {"Does", "SimpleList", "Work"};
            Holder<String[]> y = new Holder<String[]>(yOrig);
            Holder<String[]> z = new Holder<String[]>();

            String[] ret = rpcClient.testStringList(x, y, z);

            assertTrue(y.value.length == 3);
            assertTrue(z.value.length == 3);
            assertTrue(ret.length == 3);
            if (!perfTestOnly) {
                for (int i = 0; i < 3; i++) {
                    assertEquals("testStringList(): Incorrect value for inout param", x[i], y.value[i]);
                    assertEquals("testStringList(): Incorrect value for out param", yOrig[i], z.value[i]);
                    assertEquals("testStringList(): Incorrect return value", x[i], ret[i]);
                }
            }
        }
    }

    public void testNumberList() throws Exception {
        if (testDocLiteral) {
            List<Integer> x = Arrays.asList(1, 2, 3);
            List<Integer> yOrig = Arrays.asList(10, 100, 1000);
            Holder< List<Integer> > y = new Holder< List<Integer> >(yOrig);
            Holder< List<Integer> > z = new Holder< List<Integer> >();

            List<Integer> ret = docClient.testNumberList(x, y, z);
            if (!perfTestOnly) {
                assertTrue("testNumberList(): Incorrect value for inout param", x.equals(y.value));
                assertTrue("testNumberList(): Incorrect value for out param", yOrig.equals(z.value));
                assertTrue("testNumberList(): Incorrect return value", x.equals(ret));
            }
        } else {
            Integer[] x = {1, 2, 3};
            Integer[] yOrig = {10, 100, 1000};
            Holder<Integer[]> y = new Holder<Integer[]>(yOrig);
            Holder<Integer[]> z = new Holder<Integer[]>();

            Integer[] ret = rpcClient.testNumberList(x, y, z);

            assertTrue(y.value.length == 3);
            assertTrue(z.value.length == 3);
            assertTrue(ret.length == 3);
            if (!perfTestOnly) {
                for (int i = 0; i < 3; i++) {
                    assertEquals("testNumberList(): Incorrect value for inout param", x[i], y.value[i]);
                    assertEquals("testNumberList(): Incorrect value for out param", yOrig[i], z.value[i]);
                    assertEquals("testNumberList(): Incorrect return value", x[i], ret[i]);
                }
            }
        }
    }
    
    public void testQNameList() throws Exception {
        if (testDocLiteral) {
            List<QName> x = Arrays.asList(
                new QName("http://schemas.iona.com/type_test", "testqname1"),
                new QName("http://schemas.iona.com/type_test", "testqname2"),
                new QName("http://schemas.iona.com/type_test", "testqname3")
            );
            List<QName> yOrig = Arrays.asList(
                new QName("http://schemas.iona.com/type_test", "testqname4"),
                new QName("http://schemas.iona.com/type_test", "testqname5"),
                new QName("http://schemas.iona.com/type_test", "testqname6")
            );
            Holder< List<QName> > y = new Holder< List<QName> >(yOrig);
            Holder< List<QName> > z = new Holder< List<QName> >();

            List<QName> ret = docClient.testQNameList(x, y, z);
            if (!perfTestOnly) {
                assertTrue("testQNameList(): Incorrect value for inout param", x.equals(y.value));
                assertTrue("testQNameList(): Incorrect value for out param", yOrig.equals(z.value));
                assertTrue("testQNameList(): Incorrect return value", x.equals(ret));
            }
        } else {
            QName[] x = {
                new QName("http://schemas.iona.com/type_test", "testqname1"),
                new QName("http://schemas.iona.com/type_test", "testqname2"),
                new QName("http://schemas.iona.com/type_test", "testqname3")
            };
            QName[] yOrig = {
                new QName("http://schemas.iona.com/type_test", "testqname4"),
                new QName("http://schemas.iona.com/type_test", "testqname5"),
                new QName("http://schemas.iona.com/type_test", "testqname6")
            };
            Holder<QName[]> y = new Holder<QName[]>(yOrig);
            Holder<QName[]> z = new Holder<QName[]>();

            QName[] ret = rpcClient.testQNameList(x, y, z);

            assertTrue(y.value.length == 3);
            assertTrue(z.value.length == 3);
            assertTrue(ret.length == 3);
            if (!perfTestOnly) {
                for (int i = 0; i < 3; i++) {
                    assertEquals("testQNameList(): Incorrect value for inout param", x[i], y.value[i]);
                    assertEquals("testQNameList(): Incorrect value for out param", yOrig[i], z.value[i]);
                    assertEquals("testQNameList(): Incorrect return value", x[i], ret[i]);
                }
            }
        }
    }

    public void testSimpleUnionList() throws Exception {
        if (testDocLiteral) {
            List<String> x = Arrays.asList("5", "-7");
            List<String> yOrig = Arrays.asList("-9", "7");

            Holder< List<String> > y = new Holder< List<String> >(yOrig);
            Holder< List<String> > z = new Holder< List<String> >();

            List<String> ret = docClient.testSimpleUnionList(x, y, z);
            if (!perfTestOnly) {
                assertTrue("testSimpleUnionList(): Incorrect value for inout param", x.equals(y.value));
                assertTrue("testSimpleUnionList(): Incorrect value for out param", yOrig.equals(z.value));
                assertTrue("testSimpleUnionList(): Incorrect return value", x.equals(ret));
            }
        } else {
            String[] x = {"5", "-7"};
            String[] yOrig = {"-9", "7"};

            Holder<String[]> y = new Holder<String[]>(yOrig);
            Holder<String[]> z = new Holder<String[]>();

            String[] ret = rpcClient.testSimpleUnionList(x, y, z);

            assertTrue(y.value.length == 2);
            assertTrue(z.value.length == 2);
            assertTrue(ret.length == 2);
            if (!perfTestOnly) {
                for (int i = 0; i < 2; i++) {
                    assertEquals("testSimpleUnionList(): Incorrect value for inout param",
                                 x[i], y.value[i]);
                    assertEquals("testSimpleUnionList(): Incorrect value for out param",
                                 yOrig[i], z.value[i]);
                    assertEquals("testSimpleUnionList(): Incorrect return value",
                                 x[i], ret[i]);
                }
            }
        }
    }

    public void testAnonEnumList() throws Exception {
        if (testDocLiteral) {
            List<Short> x = Arrays.asList((short)10, (short)100);
            List<Short> yOrig = Arrays.asList((short)1000, (short)10);

            Holder< List<Short> > y = new Holder< List<Short> >(yOrig);
            Holder< List<Short> > z = new Holder< List<Short> >();

            List<Short> ret = docClient.testAnonEnumList(x, y, z);
            assertTrue("testNMTOKENS(): Incorrect value for inout param", x.equals(y.value));
            assertTrue("testNMTOKENS(): Incorrect value for out param", yOrig.equals(z.value));
            assertTrue("testNMTOKENS(): Incorrect return value", x.equals(ret));
        } else {
            Short[] x = {(short)10, (short)100};
            Short[] yOrig = {(short)1000, (short)10};

            Holder<Short[]> y = new Holder<Short[]>(yOrig);
            Holder<Short[]> z = new Holder<Short[]>();

            Short[] ret = rpcClient.testAnonEnumList(x, y, z);

            assertTrue(y.value.length == 2);
            assertTrue(z.value.length == 2);
            assertTrue(ret.length == 2);
            if (!perfTestOnly) {
                for (int i = 0; i < 2; i++) {
                    assertEquals("testAnonEnumList(): Incorrect value for inout param",
                                 x[i].shortValue(), y.value[i].shortValue());
                    assertEquals("testAnonEnumList(): Incorrect value for out param",
                                 yOrig[i].shortValue(), z.value[i].shortValue());
                    assertEquals("testAnonEnumList(): Incorrect return value",
                                 x[i].shortValue(), ret[i].shortValue());
                }
            }
        }
    }

    public void testUnionWithAnonEnum() throws Exception {
        String x = "5";
        String yOrig = "n/a";

        Holder<String> y = new Holder<String>(yOrig);
        Holder<String> z = new Holder<String>();
        String ret;
        if (testDocLiteral) {
            ret = docClient.testUnionWithAnonEnum(x, y, z);
        } else {
            ret = rpcClient.testUnionWithAnonEnum(x, y, z);
        }
        assertEquals("testUnionWithAnonEnum(): Incorrect value for inout param", x, y.value);
        assertEquals("testUnionWithAnonEnum(): Incorrect value for out param", yOrig, z.value);
        assertEquals("testUnionWithAnonEnum(): Incorrect return value", x, ret);
    }

}
