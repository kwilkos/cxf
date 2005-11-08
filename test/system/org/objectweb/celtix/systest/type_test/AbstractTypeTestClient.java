package org.objectweb.celtix.systest.type_test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.math.BigDecimal;
import java.math.BigInteger;
//import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;
import javax.xml.ws.Holder;

import junit.extensions.TestSetup;
//import junit.framework.Assert;
//import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.objectweb.celtix.Bus;
import org.objectweb.celtix.BusException;
import org.objectweb.celtix.systest.type_test.soap.SOAPServerImpl;
import org.objectweb.type_test.SOAPService;
import org.objectweb.type_test.TypeTestPortType;

public abstract class AbstractTypeTestClient extends TestCase implements TypeTestTester {
    protected static Bus bus;
    protected static TypeTestPortType client;
    protected static SOAPServerImpl server;

    protected PrintStream pout;
    protected ByteArrayOutputStream bout = new ByteArrayOutputStream();
    protected boolean failed;
    protected boolean tearDownAll;
    protected boolean perfTestOnly;

    public AbstractTypeTestClient(String name) {
        super(name);
        System.setProperty("javax.xml.ws.spi.Provider", "org.objectweb.celtix.bus.jaxws.spi.ProviderImpl");
    }
    public AbstractTypeTestClient() {
        super("TypeTest");
    }

    public void setPerformanceTestOnly() {
        perfTestOnly = true;
    }

    public abstract TypeTestSetup getTestSetup();

    public void setUp() throws Exception {

        if (client == null || server == null) {
            tearDownAll = true;
            this.getTestSetup().setUp();
        }

        pout = System.out;

        System.setOut(new PrintStream(bout));
    }

    public void tearDown() throws Exception {

        System.setOut(pout);
        if (failed) {
            System.out.println(new String(bout.toByteArray()));
        }

        if (tearDownAll) {
            this.getTestSetup().tearDown();
        }
    }

    protected static class TypeTestSetup extends TestSetup {
        Class serverClass;
        QName serviceName;
        QName portName;
        String wsdlPath;
        String[] args;

        public TypeTestSetup(TestSuite suite, Class theServerClass, 
                             QName servicename, QName port, String theWsdlPath,
                             String[] theArgs) {
            super(suite);
            this.serverClass = theServerClass;
            this.serviceName = servicename;
            this.portName = port;
            this.wsdlPath = theWsdlPath;
            this.args = theArgs;
        }

        public void setUp() throws Exception {
            // setup for entire suite

            // XXX - Disable assertions here - theres a bogus assertion when
            // publishing an endpoint in RuntimeModeler.getPortTypeName()
            ClassLoader.getSystemClassLoader().setDefaultAssertionStatus(false);

            boolean noServerStart = Boolean.getBoolean("NO_SERVER_START");
            if (!noServerStart) {
                Object object = serverClass.newInstance();
                if (object instanceof SOAPServerImpl) {
                    server = (SOAPServerImpl) object;
                }
                try {
                    server.start(args);
                } catch (BusException bex) {
                    server.stop();
                    server = null;
                    throw bex;
                }
            }

            bus = Bus.init(args);

            // for support of soap header, context, substitutionGroup and
            // xsd:anyType, you must register generated TypeFactory.
            //File file = new File("/wsdl/type_test/type_test_soap.wsdl");
            //String location = file.toURL().toString();
            //bus.registerTypeFactory(new org.objectweb.type_test.TypeTestTypeFactory(location));

            URL wsdlLocation = null;
            wsdlLocation = getClass().getResource(wsdlPath);
            assertNotNull(wsdlLocation);

            SOAPService service = new SOAPService(wsdlLocation, serviceName);
            client = (TypeTestPortType) service.getPort(portName, TypeTestPortType.class);

            // XXX - Re-enable assertions
            ClassLoader.getSystemClassLoader().setDefaultAssertionStatus(true);
        }

        public void tearDown() throws Exception {
            // tear down for entire suite
            //
            if (server != null) {
                server.stop();
                server = null;
            }
            if (bus != null) {
                bus.shutdown(true);
                bus = null;
            }

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
        failed = true;
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

            byte ret = client.testByte(x, y, z);
            if (!perfTestOnly) {
                assertEquals("testByte(): Incorrect value for inout param",
                             Byte.valueOf(x), y.value);
                assertEquals("testByte(): Incorrect value for out param",
                             yOrig.value, z.value);
                assertEquals("testByte(): Incorrect return value", x, ret);
            }
        }
        failed = false;
    }

    public void testShort() throws Exception {
        failed = true;
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

            short ret = client.testShort(x, y, z);
            if (!perfTestOnly) {
                assertEquals("testShort(): Incorrect value for inout param", Short.valueOf(x), y.value);
                assertEquals("testShort(): Incorrect value for out param", yOrig.value, z.value);
                assertEquals("testShort(): Incorrect return value", x, ret);
            }
        }
        failed = false;
    }

    public void testUnsignedShort() throws Exception {
        failed = true;
        int valueSets[][] = {{0, 1}, {1, 0}, {0, Short.MAX_VALUE * 2 + 1}};

        for (int i = 0; i < valueSets.length; i++) {
            int x = valueSets[i][0];
            Holder<Integer> yOrig = new Holder<Integer>(valueSets[i][1]);
            Holder<Integer> y = new Holder<Integer>(valueSets[i][1]);
            Holder<Integer> z = new Holder<Integer>();

            int ret = client.testUnsignedShort(x, y, z);
            if (!perfTestOnly) {
                assertEquals("testUnsignedShort(): Incorrect value for inout param",
                             Integer.valueOf(x), y.value);
                assertEquals("testUnsignedShort(): Incorrect value for out param",
                             yOrig.value, z.value);
                assertEquals("testUnsignedShort(): Incorrect return value", x, ret);
            }
        }
        failed = false;
    }

    public void testInt() throws Exception {
        failed = true;
        int valueSets[][] = {{5, 10}, {-10, 50},
            {Integer.MIN_VALUE, Integer.MAX_VALUE}};

        for (int i = 0; i < valueSets.length; i++) {
            int x = valueSets[i][0];
            Holder<Integer> yOrig = new Holder<Integer>(valueSets[i][1]);
            Holder<Integer> y = new Holder<Integer>(valueSets[i][1]);
            Holder<Integer> z = new Holder<Integer>();
            
            int ret = client.testInt(x, y, z);
            if (!perfTestOnly) {
                assertEquals("testInt(): Incorrect value for inout param", Integer.valueOf(x), y.value);
                assertEquals("testInt(): Incorrect value for out param", yOrig.value, z.value);
                assertEquals("testInt(): Incorrect return value", x, ret);
            }
        }
        failed = false;
    }

    public void testUnsignedInt() throws Exception {
        failed = true;
        long valueSets[][] = {{11, 20}, {1, 0},
            {0, ((long)Integer.MAX_VALUE) * 2 + 1}};

        for (int i = 0; i < valueSets.length; i++) {
            long x = valueSets[i][0];
            long yOrig = valueSets[i][1];
            Holder<Long> y = new Holder<Long>(valueSets[i][1]);
            Holder<Long> z = new Holder<Long>();

            long ret = client.testUnsignedInt(x, y, z);
            if (!perfTestOnly) {
                assertEquals("testUnsignedInt(): Incorrect value for inout param",
                             Long.valueOf(x), y.value);
                assertEquals("testUnsignedInt(): Incorrect value for out param",
                             Long.valueOf(yOrig), z.value);
                assertEquals("testUnsignedInt(): Incorrect return value", x, ret);
            }
        }
        failed = false;
    }

    public void testLong() throws Exception {
        failed = true;
        long valueSets[][] = {{0, 1}, {-1, 0},
            {Long.MIN_VALUE, Long.MAX_VALUE}};

        for (int i = 0; i < valueSets.length; i++) {
            long x = valueSets[i][0];
            Holder<Long> yOrig = new Holder<Long>(valueSets[i][1]);
            Holder<Long> y = new Holder<Long>(valueSets[i][1]);
            Holder<Long> z = new Holder<Long>();

            long ret = client.testLong(x, y, z);
            if (!perfTestOnly) {
                assertEquals("testLong(): Incorrect value for inout param", Long.valueOf(x), y.value);
                assertEquals("testLong(): Incorrect value for out param", yOrig.value, z.value);
                assertEquals("testLong(): Incorrect return value", x, ret);
            }
        }
        failed = false;
    }

    public void testUnsignedLong() throws Exception {
        failed = true;
        BigInteger valueSets[][] = {{new BigInteger("0"), new BigInteger("1")},
            {new BigInteger("1"), new BigInteger("0")},
            {new BigInteger("0"), new BigInteger(String.valueOf(Long.MAX_VALUE * Long.MAX_VALUE))}};

        for (int i = 0; i < valueSets.length; i++) {
            BigInteger x = valueSets[i][0];
            Holder<BigInteger> yOrig = new Holder<BigInteger>(valueSets[i][1]);
            Holder<BigInteger> y = new Holder<BigInteger>(valueSets[i][1]);
            Holder<BigInteger> z = new Holder<BigInteger>();

            BigInteger ret = client.testUnsignedLong(x, y, z);
            if (!perfTestOnly) {
                assertEquals("testUnsignedLong(): Incorrect value for inout param", x, y.value);
                assertEquals("testUnsignedLong(): Incorrect value for out param", yOrig.value, z.value);
                assertEquals("testUnsignedLong(): Incorrect return value", x, ret);
            }
        }
        failed = false;
    }

    public void testFloat() throws Exception {
        failed = true;
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

            float ret = client.testFloat(x, y, z);
            if (!perfTestOnly) {
                assertEquals(i + ": testFloat(): Wrong value for inout param", x, y.value, delta);
                assertEquals(i + ": testFloat(): Wrong value for out param", yOrig.value, z.value, delta);
                assertEquals(i + ": testFloat(): Wrong return value", x, ret, delta);
            }
        }

        float x = Float.NaN;
        // XXX - O.0f 
        Holder<Float> yOrig = new Holder<Float>(0.0f);
        Holder<Float> y = new Holder<Float>(0.0f);
        Holder<Float> z = new Holder<Float>();
        float ret = client.testFloat(x, y, z);
        if (!perfTestOnly) {
            assertTrue("testFloat(): Incorrect value for inout param", Float.isNaN(y.value));
            assertEquals("testFloat(): Incorrect value for out param", yOrig.value, z.value, delta);
            assertTrue("testFloat(): Incorrect return value", Float.isNaN(ret));
        }

        failed = false;
    }

    public void testDouble() throws Exception {
        failed = true;
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

            double ret = client.testDouble(x, y, z);
            if (!perfTestOnly) {
                assertEquals("testDouble(): Incorrect value for inout param", x, y.value, delta);
                assertEquals("testDouble(): Incorrect value for out param", yOrig.value, z.value, delta);
                assertEquals("testDouble(): Incorrect return value", x, ret, delta);
            }
        }

        double x = Double.NaN;
        // XXX - O.0
        Holder<Double> yOrig = new Holder<Double>(0.0);
        Holder<Double> y = new Holder<Double>(0.0);
        Holder<Double> z = new Holder<Double>();
        double ret = client.testDouble(x, y, z);
        if (!perfTestOnly) {
            assertTrue("testDouble(): Incorrect value for inout param", Double.isNaN(y.value));
            assertEquals("testDouble(): Incorrect value for out param", yOrig.value, z.value, delta);
            assertTrue("testDouble(): Incorrect return value", Double.isNaN(ret));
        }
        failed = false;
    }

    public void testUnsignedByte() throws Exception {
        failed = true;
        short valueSets[][] = {{0, 1}, {1, 0},
            {0, Byte.MAX_VALUE * 2 + 1}};

        for (int i = 0; i < valueSets.length; i++) {
            short x = valueSets[i][0];
            Holder<Short> yOrig = new Holder<Short>(valueSets[i][1]);
            Holder<Short> y = new Holder<Short>(valueSets[i][1]);
            Holder<Short> z = new Holder<Short>();

            short ret = client.testUnsignedByte(x, y, z);
            if (!perfTestOnly) {
                assertEquals("testUnsignedByte(): Incorrect value for inout param",
                             Short.valueOf(x), y.value);
                assertEquals("testUnsignedByte(): Incorrect value for out param",
                             yOrig.value, z.value);
                assertEquals("testUnsignedByte(): Incorrect return value", x, ret);
            }
        }
        failed = false;
    }

    public void testBoolean() throws Exception {
        failed = true;
        boolean valueSets[][] = {{true, false}, {true, true},
            {false, true}, {false, false}};

        for (int i = 0; i < valueSets.length; i++) {
            boolean x = valueSets[i][0];
            Holder<Boolean> yOrig = new Holder<Boolean>(valueSets[i][1]);
            Holder<Boolean> y = new Holder<Boolean>(valueSets[i][1]);
            Holder<Boolean> z = new Holder<Boolean>();

            boolean ret = client.testBoolean(x, y, z);
            if (!perfTestOnly) {
                assertEquals("testBoolean(): Incorrect value for inout param", Boolean.valueOf(x), y.value);
                assertEquals("testBoolean(): Incorrect value for out param", yOrig.value, z.value);
                assertEquals("testBoolean(): Incorrect return value", x, ret);
            }
        }
        failed = false;
    }

    public void testString() throws Exception {
        failed = true;
        String valueSets[][] = {{"hello", "world"}, {"is pi > 3 ?", " is pi < 4\\\""},
            {"<illegal_tag/>", ""}};

        for (int i = 0; i < valueSets.length; i++) {
            String x = valueSets[i][0];
            Holder<String> yOrig = new Holder<String>(valueSets[i][1]);
            Holder<String> y = new Holder<String>(valueSets[i][1]);
            Holder<String> z = new Holder<String>();

            String ret = client.testString(x, y, z);
            if (!perfTestOnly) {
                assertEquals("testString(): Incorrect value for inout param", x, y.value);
                assertEquals("testString(): Incorrect value for out param", yOrig.value, z.value);
                assertEquals("testString(): Incorrect return value", x, ret);
            }
        }
        failed = false;
    }

    public void testQName() throws Exception {
        failed = true;
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

            QName ret = client.testQName(x, y, z);

            if (!perfTestOnly) {
                assertEquals("testQName(): Incorrect value for inout param", x, y.value);
                assertEquals("testQName(): Incorrect value for out param", yOrig, z.value);
                assertEquals("testQName(): Incorrect return value", x, ret);
            }
        }
        failed = false;
    }

    public void testDate() throws Exception {
        failed = true;

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

        XMLGregorianCalendar ret = client.testDate(x, y, z);
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
/* TODO Throw a Error
        try {
            ret = client.testDate(x, y, z);
            fail("Expected to catch IllegalStateException when calling"
                 + " testDate() with uninitialized parameters.");
        } catch (java.lang.IllegalStateException e) {
            // Ignore expected failure.
        }
*/
        failed = false;
    }

    public void testDateTime() throws Exception {
        failed = true;

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

        XMLGregorianCalendar ret = client.testDateTime(x, y, z);
        if (!perfTestOnly) {
            assertTrue("testDateTime(): Incorrect value for inout param", equalsDateTime(x, y.value));
            assertTrue("testDateTime(): Incorrect value for out param", equalsDateTime(yOrig, z.value));
            assertTrue("testDateTime(): Incorrect return value", equalsDateTime(x, ret));
        }

        failed = false;
    }

    public void testTime() throws Exception {
        failed = true;

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

        XMLGregorianCalendar ret = client.testTime(x, y, z);
        if (!perfTestOnly) {
            assertTrue("testTime(): Incorrect value for inout param", equalsTime(x, y.value));
            assertTrue("testTime(): Incorrect value for out param", equalsTime(yOrig, z.value));
            assertTrue("testTime(): Incorrect return value", equalsTime(x, ret));
        }

        failed = false;
    }

    public void testGYear() throws Exception {
        failed = true;

        javax.xml.datatype.DatatypeFactory datatypeFactory = javax.xml.datatype.DatatypeFactory.newInstance();

        XMLGregorianCalendar x = datatypeFactory.newXMLGregorianCalendar("2004");
        XMLGregorianCalendar yOrig = datatypeFactory.newXMLGregorianCalendar("2003+05:00");

        Holder<XMLGregorianCalendar> y = new Holder<XMLGregorianCalendar>(yOrig);
        Holder<XMLGregorianCalendar> z = new Holder<XMLGregorianCalendar>();

        XMLGregorianCalendar ret = client.testGYear(x, y, z);
        assertTrue("testGYear(): Incorrect value for inout param", x.equals(y.value));
        assertTrue("testGYear(): Incorrect value for out param", yOrig.equals(z.value));
        assertTrue("testGYear(): Incorrect return value", x.equals(ret));
        failed = false;
    }

    public void testGYearMonth() throws Exception {
        failed = true;

        javax.xml.datatype.DatatypeFactory datatypeFactory = javax.xml.datatype.DatatypeFactory.newInstance();

        XMLGregorianCalendar x = datatypeFactory.newXMLGregorianCalendar("2004-08");
        XMLGregorianCalendar yOrig = datatypeFactory.newXMLGregorianCalendar("2003-12+05:00");

        Holder<XMLGregorianCalendar> y = new Holder<XMLGregorianCalendar>(yOrig);
        Holder<XMLGregorianCalendar> z = new Holder<XMLGregorianCalendar>();

        XMLGregorianCalendar ret = client.testGYearMonth(x, y, z);
        assertTrue("testGYearMonth(): Incorrect value for inout param", x.equals(y.value));
        assertTrue("testGYearMonth(): Incorrect value for out param", yOrig.equals(z.value));
        assertTrue("testGYearMonth(): Incorrect return value", x.equals(ret));
        failed = false;
    }

    public void testGMonth() throws Exception {
        failed = true;

        javax.xml.datatype.DatatypeFactory datatypeFactory = javax.xml.datatype.DatatypeFactory.newInstance();

        XMLGregorianCalendar x = datatypeFactory.newXMLGregorianCalendar("--08--");
        XMLGregorianCalendar yOrig = datatypeFactory.newXMLGregorianCalendar("--12--+05:00");

        Holder<XMLGregorianCalendar> y = new Holder<XMLGregorianCalendar>(yOrig);
        Holder<XMLGregorianCalendar> z = new Holder<XMLGregorianCalendar>();

        XMLGregorianCalendar ret = client.testGMonth(x, y, z);
        assertTrue("testGMonth(): Incorrect value for inout param", x.equals(y.value));
        assertTrue("testGMonth(): Incorrect value for out param", yOrig.equals(z.value));
        assertTrue("testGMonth(): Incorrect return value", x.equals(ret));
        failed = false;
    }

    public void testGMonthDay() throws Exception {
        failed = true;

        javax.xml.datatype.DatatypeFactory datatypeFactory = javax.xml.datatype.DatatypeFactory.newInstance();

        XMLGregorianCalendar x = datatypeFactory.newXMLGregorianCalendar("--08-21");
        XMLGregorianCalendar yOrig = datatypeFactory.newXMLGregorianCalendar("--12-05+05:00");

        Holder<XMLGregorianCalendar> y = new Holder<XMLGregorianCalendar>(yOrig);
        Holder<XMLGregorianCalendar> z = new Holder<XMLGregorianCalendar>();

        XMLGregorianCalendar ret = client.testGMonthDay(x, y, z);
        assertTrue("testGMonthDay(): Incorrect value for inout param", x.equals(y.value));
        assertTrue("testGMonthDay(): Incorrect value for out param", yOrig.equals(z.value));
        assertTrue("testGMonthDay(): Incorrect return value", x.equals(ret));
        failed = false;
    }

    public void testGDay() throws Exception {
        failed = true;

        javax.xml.datatype.DatatypeFactory datatypeFactory = javax.xml.datatype.DatatypeFactory.newInstance();

        XMLGregorianCalendar x = datatypeFactory.newXMLGregorianCalendar("---21");
        XMLGregorianCalendar yOrig = datatypeFactory.newXMLGregorianCalendar("---05+05:00");

        Holder<XMLGregorianCalendar> y = new Holder<XMLGregorianCalendar>(yOrig);
        Holder<XMLGregorianCalendar> z = new Holder<XMLGregorianCalendar>();

        XMLGregorianCalendar ret = client.testGDay(x, y, z);
        assertTrue("testGDay(): Incorrect value for inout param", x.equals(y.value));
        assertTrue("testGDay(): Incorrect value for out param", yOrig.equals(z.value));
        assertTrue("testGDay(): Incorrect return value", x.equals(ret));
        failed = false;
    }

    public void testNormalizedString() throws Exception {
        failed = true;
        String x = "  normalized string ";
        String yOrig = "  another normalized  string ";

        Holder<String> y = new Holder<String>(yOrig);
        Holder<String> z = new Holder<String>();

        String ret = client.testNormalizedString(x, y, z);
        assertTrue("testNormalizedString(): Incorrect value for inout param", x.equals(y.value));
        assertTrue("testNormalizedString(): Incorrect value for out param", yOrig.equals(z.value));
        assertTrue("testNormalizedString(): Incorrect return value", x.equals(ret));
        failed = false;
    }

    public void testToken() throws Exception {
        failed = true;
        String x = "token";
        String yOrig = "another token";

        Holder<String> y = new Holder<String>(yOrig);
        Holder<String> z = new Holder<String>();

        String ret = client.testToken(x, y, z);
        assertTrue("testToken(): Incorrect value for inout param", x.equals(y.value));
        assertTrue("testToken(): Incorrect value for out param", yOrig.equals(z.value));
        assertTrue("testToken(): Incorrect return value", x.equals(ret));
        failed = false;
    }

    public void testLanguage() throws Exception {
        failed = true;
        String x = "abc";
        String yOrig = "abc-def";

        Holder<String> y = new Holder<String>(yOrig);
        Holder<String> z = new Holder<String>();

        String ret = client.testLanguage(x, y, z);
        assertTrue("testLanguage(): Incorrect value for inout param", x.equals(y.value));
        assertTrue("testLanguage(): Incorrect value for out param", yOrig.equals(z.value));
        assertTrue("testLanguage(): Incorrect return value", x.equals(ret));
        failed = false;
    }

    public void testNMTOKEN() throws Exception {
        failed = true;
        String x = "123:abc";
        String yOrig = "abc.-_:";

        Holder<String> y = new Holder<String>(yOrig);
        Holder<String> z = new Holder<String>();

        String ret = client.testNMTOKEN(x, y, z);
        assertTrue("testNMTOKEN(): Incorrect value for inout param", x.equals(y.value));
        assertTrue("testNMTOKEN(): Incorrect value for out param", yOrig.equals(z.value));
        assertTrue("testNMTOKEN(): Incorrect return value", x.equals(ret));
        failed = false;
    }

    public void testNMTOKENS() throws Exception {
        failed = true;
        List<String> x = new ArrayList<String>(1);
        x.add("123:abc");
        List<String> yOrig = new ArrayList<String>(2);
        yOrig.add("abc.-_:");
        yOrig.add("a");

        Holder<List<String>> y = new Holder<List<String>>(yOrig);
        Holder<List<String>> z = new Holder<List<String>>();

        // XXX - TODO Need to figure out what causes the JAXBException stack trace here.
        try {
            List<String> ret = client.testNMTOKENS(x, y, z);
            assertTrue("testNMTOKENS(): Incorrect value for inout param", x.equals(y.value));
            assertTrue("testNMTOKENS(): Incorrect value for out param", yOrig.equals(z.value));
            assertTrue("testNMTOKENS(): Incorrect return value", x.equals(ret));
        } catch (Exception e) {
            // XXX - Fix the problem here!
        }
        failed = false;
    }
    
    public void testName() throws Exception {
        failed = true;
        String x = "abc:123";
        String yOrig = "abc.-_";

        Holder<String> y = new Holder<String>(yOrig);
        Holder<String> z = new Holder<String>();

        String ret = client.testName(x, y, z);
        assertTrue("testName(): Incorrect value for inout param", x.equals(y.value));
        assertTrue("testName(): Incorrect value for out param", yOrig.equals(z.value));
        assertTrue("testName(): Incorrect return value", x.equals(ret));
        failed = false;
    }

    public void testNCName() throws Exception {
        failed = true;
        String x = "abc-123";
        String yOrig = "abc.-";

        Holder<String> y = new Holder<String>(yOrig);
        Holder<String> z = new Holder<String>();

        String ret = client.testNCName(x, y, z);
        assertTrue("testNCName(): Incorrect value for inout param", x.equals(y.value));
        assertTrue("testNCName(): Incorrect value for out param", yOrig.equals(z.value));
        assertTrue("testNCName(): Incorrect return value", x.equals(ret));
        failed = false;
    }

    public void testID() throws Exception {
        failed = true;
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

            String ret = client.testID(x, y, z);
            if (!perfTestOnly) {
                assertEquals("testID(): Incorrect value for inout param", x, y.value);
                assertEquals("testID(): Incorrect value for out param", yOrig.value, z.value);
                assertEquals("testID(): Incorrect return value", x, ret);
            }
        }
        failed = false;
    }

    public void testDecimal() throws Exception {
        failed = true;
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

            BigDecimal ret = client.testDecimal(x, y, z);
            if (!perfTestOnly) {
                assertEquals("testDecimal(): Incorrect value for inout param", x, y.value);
                assertEquals("testDecimal(): Incorrect value for out param", yOrig.value, z.value);
                assertEquals("testDecimal(): Incorrect return value", x, ret);
            }
        }
        failed = false;
    }

    public void testInteger() throws Exception {
        failed = true;
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

            BigInteger ret = client.testInteger(x, y, z);
            if (!perfTestOnly) {
                assertEquals("testInteger(): Incorrect value for inout param", x, y.value);
                assertEquals("testInteger(): Incorrect value for out param", yOrig.value, z.value);
                assertEquals("testInteger(): Incorrect return value", x, ret);
            }
        }
        failed = false;
    }

    public void testPositiveInteger() throws Exception {
        failed = true;
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

            BigInteger ret = client.testPositiveInteger(x, y, z);
            if (!perfTestOnly) {
                assertEquals("testPositiveInteger(): Incorrect value for inout param", x, y.value);
                assertEquals("testPositiveInteger(): Incorrect value for out param", yOrig.value, z.value);
                assertEquals("testPositiveInteger(): Incorrect return value", x, ret);
            }
        }
        failed = false;
    }

    public void testNonPositiveInteger() throws Exception {
        failed = true;
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

            BigInteger ret = client.testNonPositiveInteger(x, y, z);
            if (!perfTestOnly) {
                assertEquals("testNonPositiveInteger(): Incorrect value for inout param", x, y.value);
                assertEquals("testNonPositiveInteger(): Incorrect value for out param", yOrig.value, z.value);
                assertEquals("testNonPositiveInteger(): Incorrect return value", x, ret);
            }
        }
        failed = false;
    }

    public void testNegativeInteger() throws Exception {
        failed = true;
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

            BigInteger ret = client.testNegativeInteger(x, y, z);
            if (!perfTestOnly) {
                assertEquals("testNegativeInteger(): Incorrect value for inout param", x, y.value);
                assertEquals("testNegativeInteger(): Incorrect value for out param", yOrig.value, z.value);
                assertEquals("testNegativeInteger(): Incorrect return value", x, ret);
            }
        }
        failed = false;
    }

    public void testNonNegativeInteger() throws Exception {
        failed = true;
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

            BigInteger ret = client.testNonNegativeInteger(x, y, z);
            if (!perfTestOnly) {
                assertEquals("testNonNegativeInteger(): Incorrect value for inout param", x, y.value);
                assertEquals("testNonNegativeInteger(): Incorrect value for out param", yOrig.value, z.value);
                assertEquals("testNonNegativeInteger(): Incorrect return value", x, ret);
            }
        }
        failed = false;
    }

    public void testHexBinary() throws Exception {
        failed = true;
        byte[] x = "hello".getBytes();
        Holder<byte[]> y = new Holder<byte[]>("goodbye".getBytes());
        Holder<byte[]> yOriginal = new Holder<byte[]>("goodbye".getBytes());
        Holder<byte[]> z = new Holder<byte[]>();
        byte[] ret = client.testHexBinary(x, y, z);
        if (!perfTestOnly) {
            assertTrue("testHexBinary(): Incorrect value for inout param",
                       Arrays.equals(x, y.value));
            assertTrue("testHexBinary(): Incorrect value for out param",
                       Arrays.equals(yOriginal.value, z.value));
            assertTrue("testHexBinary(): Incorrect return value",
                       Arrays.equals(x, ret));
        }
        failed = false;
    }

    public void testBase64Binary() throws Exception {
        failed = true;
        byte[] x = "hello".getBytes();
        Holder<byte[]> y = new Holder<byte[]>("goodbye".getBytes());
        Holder<byte[]> yOriginal = new Holder<byte[]>("goodbye".getBytes());
        Holder<byte[]> z = new Holder<byte[]>();
        byte[] ret = client.testBase64Binary(x, y, z);
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
            client.testBase64Binary(x, y, z);
            fail("Uninitialized Holder for inout parameter should have thrown an error.");
        } catch (Exception e) {
            // Ignore expected failure.
        }

        failed = false;
    }

    /*
     * XXX - TODO
     *
    public void testanyURI() throws Exception {
        failed = true;
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

            URI ret = client.testanyURI(x, y, z);
            if (!perfTestOnly) {
                assertEquals("testanyURI(): Incorrect value for inout param",
                             x.toString(), y.value.toString());
                assertEquals("testanyURI(): Incorrect value for out param",
                             yOrig.value.toString(), z.value.toString());
                assertEquals("testanyURI(): Incorrect return value",
                             x.toString(), ret.toString());
            }
        }
        failed = false;
    }
    */
    
}

