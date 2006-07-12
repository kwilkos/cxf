package org.objectweb.celtix.jca.celtix;


import java.io.File;
import java.io.FileNotFoundException;
import java.util.ResourceBundle;


import javax.resource.ResourceException;
import javax.xml.namespace.QName;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

import org.easymock.classextension.EasyMock;
import org.objectweb.celtix.Bus;
import org.objectweb.celtix.BusException;
import org.objectweb.celtix.common.i18n.Message;
import org.objectweb.celtix.jca.celtix.test.DummyBus;
import org.objectweb.celtix.jca.core.resourceadapter.ResourceAdapterInternalException;


public class BusFactoryTest extends TestCase {

    //private String urlPrefix;

    public BusFactoryTest(String name) {
        super(name);
    }

    /*
    public void setUp() throws Exception {
       
        urlPrefix = "file://";
        if (System.getProperty("os.name").startsWith("Win")) {
            urlPrefix = "file:/";
        }
    }
    */

    public void testSetAppserverClassLoader() {
        ClassLoader loader = new DummyClassLoader();
        BusFactory bf = new BusFactory(new ManagedConnectionFactoryImpl());
        bf.setAppserverClassLoader(loader);
        assertSame("Checking appserverClassLoader.", loader, bf.getAppserverClassLoader());
    }

    // Not a real test, just exercise the code, if classpath contains a vaild
    // license then fine, otherwise expect an exception to indicate it cannot be
    // found
    // in the RAR
      

    public void testNoDefaultValueForJAASConfigName() throws Exception {

        ManagedConnectionFactoryImpl mcf = new ManagedConnectionFactoryImpl();
        BusFactory busFactory = new BusFactory(mcf);
        assertNull("checking JAASConfigName default value", busFactory.getJAASLoginConfigName());
    }
  
    
    public void testModifiedBusArguments() throws Exception {
        ManagedConnectionFactoryImpl mcf = new ManagedConnectionFactoryImpl();
        mcf.setConfigurationScope("a.b.c");

        BusFactory busFactory = new BusFactory(mcf);
        String[] args = busFactory.getBusArgs();

        assertTrue("Wrong arguments length: " + args.length, args.length == 2);
        assertEquals("Wrong Argument. ", args[0], "-BUSid");
        assertEquals("Wrong Argument. ", args[1],  "a.b.c");        
    }

    public void testBusInitGetProps() throws Exception {
        DummyBus.reset();
        System.setProperty("test.bus.class", DummyBus.class.getName());
        ManagedConnectionFactoryImpl mcf = new ManagedConnectionFactoryImpl();
        mcf.setCeltixInstallDir(DummyBus.getCeltixHome());
        //mcf.setCeltixCEURL(DummyBus.CeltixCEURL);

        assertEquals("bus not yet initialized", DummyBus.getInitializeCount(), 0);

        
        BusFactory busFactory = new BusFactory(mcf);
        busFactory.create(null, null);

        assertEquals("bus initialized ", DummyBus.getInitializeCount(), 1);
       
    }

    public void testInitBusThrowsException() throws Exception {
        DummyBus.reset();
        System.setProperty("test.bus.class", DummyBus.class.getName());
        final Exception thrown = new BusException(new Message("tested bus exception!", 
                                                              (ResourceBundle)null, new Object[]{}));

        ClassLoader originalCl = Thread.currentThread().getContextClassLoader();
        try {
            ManagedConnectionFactoryImpl mcf = new ManagedConnectionFactoryImpl();
            mcf.setCeltixInstallDir(DummyBus.getCeltixHome());
           // mcf.setCeltixCEURL(DummyBus.CeltixCEURL);
            BusFactory busFactory = new BusFactory(mcf);

            // do this for MockObject creation
            Thread.currentThread().setContextClassLoader(busFactory.getClass().getClassLoader());
            DummyBus.setThrowException(true);
            
                       
            try {
                busFactory.create(null, null);
                fail("did not get expected resource exception");
            } catch (ResourceException re) {
                assertTrue("cause is set", re.getCause() != null);
                assertEquals("cause is expected type", thrown.getClass(), re.getCause().getClass());
            }

            assertEquals("init was called once", 1, DummyBus.getInitializeCount());

        } finally {
            Thread.currentThread().setContextClassLoader(originalCl);
        }
    }

    public void testInitBusSetsThreadContextClassLoader() throws Exception {
        DummyBus.reset();
        System.setProperty("test.bus.class", DummyBus.class.getName());
        ClassLoader originalCl = Thread.currentThread().getContextClassLoader();
        try {
            ManagedConnectionFactoryImpl mcf = new ManagedConnectionFactoryImpl();
            mcf.setCeltixInstallDir(DummyBus.getCeltixHome());
            //mcf.setCeltixCEURL(DummyBus.CeltixCEURL);
            BusFactory busFactory = new BusFactory(mcf);

            // do this for MockObject creation
            Thread.currentThread().setContextClassLoader(busFactory.getClass().getClassLoader());

            Class dummyBusClass = Class.forName(DummyBus.class.getName(), true, busFactory.getClass()
                .getClassLoader());
            // initialise here while thread context classloader is correct
            dummyBusClass.newInstance();

            busFactory.create(null, null);

         
            assertEquals("init was called once", 1, DummyBus.getInitializeCount());
            assertTrue("loader is correct in init", DummyBus.isCorrectThreadContextClassLoader());

        } finally {
            Thread.currentThread().setContextClassLoader(originalCl);
        }
    }

    // service strings to qname localparts
   

    public void testValidQNameFromString() throws Exception {
        final Object[][] ejbServantServicePorpsTestStrings =
            new Object[][] {{"serviceName", new QName("serviceName")},
                            {"a/b", new QName("a/b")},
                            {"{http://somenamespace}serviceName,portName",
                                new QName("http://somenamespace", "serviceName")},
                            {"{http://addd}n,portName@http://a",
                                new QName("http://addd", "n")},
                            {"{http://somenamespace}serviceName@file:/a/b/wsdl.wsdl",
                                new QName("http://somenamespace", "serviceName")},
                            {"{http://somenamespace}serviceName@http://a?param=1",
                                new QName("http://somenamespace", "serviceName")}};
        
        ManagedConnectionFactoryImpl mcf = new ManagedConnectionFactoryImpl();
        BusFactory busFactory = new BusFactory(mcf);
        for (int i = 0; i < ejbServantServicePorpsTestStrings.length; i++) {
            String val = (String)ejbServantServicePorpsTestStrings[i][0];
            QName expected = (QName)ejbServantServicePorpsTestStrings[i][1];

            assertEquals("correct qname from mapping for " + val, expected, busFactory
                .serviceQNameFromString(val));
        }
    }

    public void testInvalidQNameFromString() throws Exception {
        ManagedConnectionFactoryImpl mcf = new ManagedConnectionFactoryImpl();
        BusFactory busFactory = new BusFactory(mcf);
        try {
            busFactory.serviceQNameFromString("a@");
            fail("expecte ex on invalid format");
        } catch (ResourceException expected) {
            assertTrue(expected.getCause() instanceof java.util.NoSuchElementException);
        }
    }

    

    public void testWsdlLocFromString() throws Exception {
        //service strings to wsdl urls
        final String[][] ejbServantServicePropsTestStringsWsdlLoc = 
            new String[][] {{"serviceName", null},
                            {"a/b", null},
                            {"{http://somenamespace}serviceName", null},
                            {"{http://somenamespace}serviceName,portName@file:/a/b/wsdl.wsdl", 
                                "file:/a/b/wsdl.wsdl"},
                            {"{http://somenamespace}serviceName,portName@http://a?param=1",
                                "http://a?param=1"}};
        ManagedConnectionFactoryImpl mcf = new ManagedConnectionFactoryImpl();
        BusFactory busFactory = new BusFactory(mcf);
        for (int i = 0; i < ejbServantServicePropsTestStringsWsdlLoc.length; i++) {
            String val = ejbServantServicePropsTestStringsWsdlLoc[i][0];
            String expectedUrl = ejbServantServicePropsTestStringsWsdlLoc[i][1];

            assertEquals("correct wsdlLocation from mapping for " + val, expectedUrl, busFactory
                .wsdlLocFromString(val));
        }
    }

    
    public void testPortNameFromString() throws Exception {
        //service strings to portName
        final String[][] ejbServantServicePropsTestStringsPortName = 
            new String[][] {{"serviceName", null},
                            {"a/b", null},
                            {"{http://somenamespace}serviceName", null},
                            {"{http://somenamespace}serviceName,portName1@file:/a/b/wsdl.wsdl",
                                "portName1"},
                            {"{http://somenamespace}serviceName,portName2@http://a?param=1", 
                                "portName2"}};

        ManagedConnectionFactoryImpl mcf = new ManagedConnectionFactoryImpl();
        BusFactory busFactory = new BusFactory(mcf);
        for (int i = 0; i < ejbServantServicePropsTestStringsPortName.length; i++) {
            String val = ejbServantServicePropsTestStringsPortName[i][0];
            String expectedUrl = ejbServantServicePropsTestStringsPortName[i][1];

            assertEquals("correct wsdlLocation from mapping for " + val, expectedUrl, busFactory
                .portNameFromString(val));
        }
    }

    public void testInvalidPortNameFromString() throws Exception {
        ManagedConnectionFactoryImpl mcf = new ManagedConnectionFactoryImpl();
        BusFactory busFactory = new BusFactory(mcf);
        try {
            busFactory.portNameFromString("serviceName,");
            fail("expect ex on invalid format");
        } catch (ResourceException expected) {
            assertTrue(expected.getCause() instanceof java.util.NoSuchElementException);
        }

        try {
            busFactory.portNameFromString("serviceName,@abc");
            fail("expect ex on invalid format");
        } catch (ResourceException expected) {
            assertTrue("Exception message starts with Empty portName", expected.getMessage()
                .startsWith("Empty portName"));
        }

        try {
            busFactory.portNameFromString("serviceName,abc,uuu");
            fail("expect ex on invalid format");
        } catch (ResourceException expected) {
            assertTrue("Exception message starts with portName already set", expected.getMessage()
                .startsWith("portName already set"));
        }
    }

    public void testLoadNonexistentProperties() throws Exception {
        ManagedConnectionFactoryImpl mcf = new ManagedConnectionFactoryImpl();
        BusFactory busFactory = new BusFactory(mcf);
        try {
            busFactory.loadProperties(new File("/rubbish_name.properties").toURL());
            fail("expect an exception .");
        } catch (ResourceException re) {
            assertTrue("Cause is FileNotFoundException, cause: " + re.getCause(),
                       re.getCause() instanceof FileNotFoundException);
        }
    }

    public void testInvalidMonitorConfigNoPropsURL() throws Exception {
        ManagedConnectionFactoryImpl mcf = new ManagedConnectionFactoryImpl();
        mcf.setMonitorEJBServiceProperties(Boolean.TRUE);
        BusFactory busFactory = new BusFactory(mcf);
        try {
            Bus mockBus = EasyMock.createMock(Bus.class);
            busFactory.setBus(mockBus);
            busFactory.initialiseServants();
            fail("exception expected");
        } catch (ResourceAdapterInternalException re) {
            assertTrue("EJBServiceProperties is not set.", re.getMessage()
                .indexOf("EJBServicePropertiesURL is not set") != -1);
        }
    }
    
    /*
    public void testInitServants() throws Exception {
        ManagedConnectionFactoryImpl mcf = new ManagedConnectionFactoryImpl();
        //get resource 
        URL propFile = getClass().getResource("ejb_servants.properties");
        mcf.setEJBServicePropertiesURL(propFile.toString());
        BusFactory busFactory = new BusFactory(mcf);
        Bus mockBus = EasyMock.createMock(Bus.class);

        busFactory.setBus((Bus)mockBus);
        busFactory.initialiseServants();
        
    }

    public void testAddServantsCache() throws Exception {
        ManagedConnectionFactoryImpl mcf = new ManagedConnectionFactoryImpl();
        BusFactory busFactory = new BusFactory(mcf);
        Bus mockBus = EasyMock.createMock(Bus.class);
        Properties props = new Properties();
        props.put("jndiName", "{http://com.iona}serviceName,portName@file:///");
        assertTrue("there's no registered servants at beginning", busFactory.getRegisteredServants()
            .isEmpty());
        busFactory.setBus(mockBus);
        busFactory.initialiseServantsFromProperties(props, true);
        assertEquals("registered servant with the expected service name", ((QName)busFactory
            .getRegisteredServants().get(0)).toString(), "{http://com.iona}serviceName");
        busFactory.deregisterServants(mockBus);
        assertTrue("servants should be deregistered", busFactory.getRegisteredServants().isEmpty());
    }

    public void testInitServantsFromPropertiesWithPortName() throws Exception {
        ManagedConnectionFactoryImpl mcf = new ManagedConnectionFactoryImpl();
        BusFactory busFactory = new BusFactory(mcf);
        MockObject mockBus = MockObjectFactory.create(Bus.class);
        busFactory.setBus((Bus)mockBus);
        Properties props = new Properties();
        props.put("jndiName", "{http://com.iona}serviceName,portName@file:///");
        busFactory.initialiseServantsFromProperties(props, true);
        assertEquals("called registerServant(servant, service) once", 1, mockBus
            .getCallCount("registerServant", new Class[] {Servant.class, QName.class, String.class}));
    }

    public void testInitServantsFromPropertiesWithMissingWsdlLocInPropertiesAndConfig() throws Exception {
        ManagedConnectionFactoryImpl mcf = new ManagedConnectionFactoryImpl();
        BusFactory busFactory = new BusFactory(mcf);
        MockObject mockBus = MockObjectFactory.create(Bus.class);
        mockBus.setResult("getServiceWSDL", new MockInvoke() {
            public Object invoke(Object[] args) throws Exception {
                throw new BusException("ServiceA");
            }
        });
        busFactory.setBus((Bus)mockBus);
        final String jndiName = "/a/b";
        try {
            Properties props = new Properties();
            props.put(jndiName, "{http://ns}ServiceA");
            busFactory.initialiseServantsFromProperties(props, true);
            fail("expect ex on missing wsdl loc");
        } catch (ResourceException expected) {
            assertTrue("reasonable message", expected.toString().indexOf(jndiName) != -1);
            assertTrue(expected.getCause() instanceof BusException);
            assertTrue(expected.getCause().getMessage().indexOf("ServiceA") != -1);
        }
        assertTrue("not called on missing wsdl loc", mockBus.getCallCount("registerServant",
                                                                          new Class[] {Servant.class,
                                                                                       QName.class}) == 0);
    }

    public void testInitServantsFromPropertiesWithNoWsdlLocInPropertiesAndNoPortName() throws Exception {
        ManagedConnectionFactoryImpl mcf = new ManagedConnectionFactoryImpl();
        BusFactory busFactory = new BusFactory(mcf);
        MockObject mockBus = MockObjectFactory.create(Bus.class);
        busFactory.setBus((Bus)mockBus);
        mockBus.setResult("getServiceWSDL", new MockInvoke() {
            public Object invoke(Object[] args) throws Exception {
                assertTrue(args[0] instanceof QName);
                QName service = (QName)args[0];
                assertEquals("{http://ns}ServiceA", service.toString());
                return "file:///";
            }

        });
        final String jndiName = "/a/b";
        Properties props = new Properties();
        props.put(jndiName, "{http://ns}ServiceA");
        busFactory.initialiseServantsFromProperties(props, true);
        assertEquals("called registerServant(servant, service) once", 1, mockBus
            .getCallCount("registerServant", new Class[] {Servant.class, QName.class}));
    }

    public void testInitServantsFromPropertiesWithNoWsdlLocInProperties() throws Exception {
        ManagedConnectionFactoryImpl mcf = new ManagedConnectionFactoryImpl();
        BusFactory busFactory = new BusFactory(mcf);
        MockObject mockBus = MockObjectFactory.create(Bus.class);
        busFactory.setBus((Bus)mockBus);
        mockBus.setResult("getServiceWSDL", new MockInvoke() {
            public Object invoke(Object[] args) throws Exception {
                assertTrue(args[0] instanceof QName);
                QName service = (QName)args[0];
                assertEquals("{http://ns}ServiceA", service.toString());
                return "file:///";
            }

        });
        final String jndiName = "/a/b";
        Properties props = new Properties();
        props.put(jndiName, "{http://ns}ServiceA,portName");
        busFactory.initialiseServantsFromProperties(props, true);
        assertEquals("called registerServant(servant, service) once", 1, mockBus
            .getCallCount("registerServant", new Class[] {Servant.class, QName.class, String.class}));
    }

    public void testInitServantsFromPropertiesWithNoServiceQName() throws Exception {
        ManagedConnectionFactoryImpl mcf = new ManagedConnectionFactoryImpl();
        BusFactory busFactory = new BusFactory(mcf);
        MockObject mockBus = MockObjectFactory.create(Bus.class);
        busFactory.setBus((Bus)mockBus);
        final String jndiName = "/a/b";
        try {
            Properties props = new Properties();
            props.put(jndiName, "");
            busFactory.initialiseServantsFromProperties(props, true);
            fail("expect ex on missing service QName value");
        } catch (ResourceException expected) {
            assertTrue("reasonable message", expected.toString().indexOf(jndiName) != -1);
        }
        assertTrue("not called on missing wsdl loc", mockBus.getCallCount("registerServant",
                                                                          new Class[] {Servant.class,
                                                                                       QName.class}) == 0);
    }

    public void testInitFromPropsWithInvalidWsdlLocUrls() throws Exception {
        ManagedConnectionFactoryImpl mcf = new ManagedConnectionFactoryImpl();
        BusFactory busFactory = new BusFactory(mcf);

        try {
            Properties props = new Properties();
            props.put("/a/b", "{http://ns}ServiceA@unknownprotocol:/a");
            busFactory.initialiseServantsFromProperties(props, true);
            fail("expect ex on unknown protocol");
        } catch (ResourceException expected) {
            assertTrue("have a busFactorye " + expected.getCause(),
                       expected.getCause() instanceof MalformedURLException);
        }

        try {
            Properties props = new Properties();
            props.put("/a/b", "{http://ns}ServiceA@a/b");
            busFactory.initialiseServantsFromProperties(props, true);

            fail("expect ex on invalid format, no scheme");
        } catch (ResourceException expected) {
            assertTrue("have a mue " + expected.getCause(),
                       expected.getCause() instanceof MalformedURLException);
        }

        try {
            Properties props = new Properties();
            props.put("/a/b", "{http://ns}ServiceA@http://nowhere.plannetx.cupoftea:9090/NoWhere");
            busFactory.initialiseServantsFromProperties(props, true);

            fail("expect ex on invalid url, dud host name");
        } catch (ResourceException expected) {
            assertTrue("have a uhe " + expected.getCause(),
                       expected.getCause() instanceof java.net.UnknownHostException);
        }

    }

    public void testInitFromPropsDoesNotThrowExceptionWhenSomethingGoesWrong() throws Exception {
        ManagedConnectionFactoryImpl mcf = new ManagedConnectionFactoryImpl();
        BusFactory busFactory = new BusFactory(mcf);
        MockObject mockBus = MockObjectFactory.create(Bus.class);
        busFactory.setBus((Bus)mockBus);
        Properties props = new Properties();
        props.put("/a/b", "{http://ns}ServiceA@unknownprotocol:/a");
        busFactory.initialiseServantsFromProperties(props, false);
        assertEquals(" registerServant(servant, service) not called ", 0, mockBus
            .getCallCount("registerServant", new Class[] {Servant.class, QName.class}));
    }

    public void testDeregisterServants() throws Exception {
        ManagedConnectionFactoryImpl mcf = new ManagedConnectionFactoryImpl();
        BusFactory busFactory = new BusFactory(mcf);
        MockObject mockBus = MockObjectFactory.create(Bus.class);
        busFactory.setBus((Bus)mockBus);
        Properties props = new Properties();
        props.put("jndiName", "{http://com.iona}serviceName,portName@file:///");
        busFactory.initialiseServantsFromProperties(props, true);
        assertEquals("servantsCache is empty , removeServant not called", 0, mockBus
            .getCallCount("removeServant", new Class[] {QName.class}));
        busFactory.initialiseServantsFromProperties(props, true);
        assertEquals("removeServant called once, before register servant", 1, mockBus
            .getCallCount("removeServant", new Class[] {QName.class}));
        busFactory.deregisterServants((Bus)mockBus);
        assertEquals("removeServant called again when deregister", 2, mockBus
            .getCallCount("removeServant", new Class[] {QName.class}));
    }

    public void testPropertiesMonitorThread() throws Exception {
        FileChannel in, out;
        URL propFile = getClass().getResource("ejb_servants.properties");
        File origFile = new File(propFile.toString());
        File tmpFile = File.createTempFile("anyname", "properties");
        File tmpDir = tmpFile.getParentFile();

        File testFile = new File(tmpDir, "cubaunittest.properties");
        if (testFile.exists()) {
            testFile.delete();
        }
        assertTrue("file: " + testFile.getAbsolutePath() + " does not exist", !(testFile.exists()));

        ManagedConnectionFactoryImpl mcf = new ManagedConnectionFactoryImpl();
        mcf.setEJBServicePropertiesURL(testFile.toURI().toURL().toString());
        BusFactory busFactory = new BusFactory(mcf);
        Bus mockBus = EasyMock.createMock(Bus.class);
        busFactory.setBus((Bus)mockBus);

        BusFactory.EJBServicePropertiesMonitorRunnable propsRunnable = 
            busFactory.new EJBServicePropertiesMonitorRunnable(10);
        propsRunnable.setContinue(false);
        propsRunnable.run();
        //need to rewrite
        //assertEquals("not called ", 0, mockBus.getCallCount("registerServant", new Class[] {Servant.class,
                                                                                           // QName.class}));

        testFile.createNewFile();
        assertTrue("file exist", testFile.exists());

        
        try {
            in = new FileInputStream(origFile).getChannel();
            out = new FileOutputStream(testFile).getChannel();
            long size = in.size();
            MappedByteBuffer buf = in.map(FileChannel.MapMode.READ_ONLY, 0, size);
            out.write(buf);
        } finally {
            if (in != null) {
                in.close();
            }

            if (out != null) {
                out.close();
            }
        }

        propsRunnable.run();
        testFile.delete();
        assertEquals("called twice", 2, mockBus.getCallCount("registerServant", new Class[] {Servant.class,
                                                                                             QName.class}));
    }

    public void testPropertiesMonitorThreadCausesSomeFailures() throws Exception {
        File origFile = new File(DummyBus.vobRoot()
                                 + "/test/unit/com/iona/jca/Celtix/ejb_servants_one_wrong.properties");
        File tmpFile = File.createTempFile("anyname", "properties");
        File tmpDir = tmpFile.getParentFile();

        File testFile = new File(tmpDir, "otherunittest.properties");
        if (testFile.exists()) {
            testFile.delete();
        }
        assertTrue("file: " + testFile.getAbsolutePath() + " does not exist", !(testFile.exists()));

        ManagedConnectionFactoryImpl mcf = new ManagedConnectionFactoryImpl();
        mcf.setEJBServicePropertiesURL(testFile.toURI().toURL().toString());
        BusFactory busFactory = new BusFactory(mcf);
        MockObject mockBus = MockObjectFactory.create(Bus.class);
        busFactory.setBus((Bus)mockBus);

        BusFactory.EJBServicePropertiesMonitorRunnable propsRunnable =
            busFactory.new EJBServicePropertiesMonitorRunnable(10);
        propsRunnable.setContinue(false);
        propsRunnable.run();
        assertEquals("not called ", 0, mockBus.getCallCount("registerServant", new Class[] {Servant.class,
                                                                                            QName.class}));

        testFile.createNewFile();
        assertTrue("file exist", testFile.exists());

        FileChannel in, out;
        try {
            in = new FileInputStream(origFile).getChannel();
            out = new FileOutputStream(testFile).getChannel();
            long size = in.size();
            MappedByteBuffer buf = in.map(FileChannel.MapMode.READ_ONLY, 0, size);
            out.write(buf);
        } finally {
            if (in != null) {
                in.close();
            }

            if (out != null) {
                out.close();
            }
        }

        propsRunnable.run();
        testFile.delete();
        assertEquals("called once", 1, mockBus.getCallCount("registerServant", new Class[] {Servant.class,
                                                                                            QName.class}));
    }

   public void testInitServantsWithBootstrapContextNotNull() throws Exception {
        System.setProperty("test.bus.class", DummyBus.class.getName());
        ManagedConnectionFactoryImpl mcf = new ManagedConnectionFactoryImpl();
        mcf.setCeltixInstallDir(DummyBus.vobRoot());
        mcf.setCeltixCEURL(DummyBus.CeltixCEURL);

        assertEquals("bus not yet initialized", DummyBus.initializeCount, 0);

        BusFactory busFactory = new BusFactory(mcf);
        BootstrapContext bc = (BootstrapContext)MockObjectFactory.create(BootstrapContext.class);
        assertNotNull("BootstrapContext is not null", bc);
        busFactory.create(null, bc);
        assertEquals("BoostrapContext set", busFactory.getBootstrapContext(), bc);
        assertEquals("bus initialized ", DummyBus.initializeCount, 1);
    }

    public void testNotRegisterXAResourceIfBootstrapContextIsNull() throws Exception {
        ManagedConnectionFactoryImpl mcf = new ManagedConnectionFactoryImpl();
        mcf.setCeltixInstallDir(DummyBus.vobRoot());
        mcf.setCeltixCEURL(DummyBus.CeltixCEURL);
        BusFactory busFactory = new BusFactory(mcf);
        busFactory.setBootstrapContext(null);
        busFactory.setResourceRegistered(false);
        assertTrue("XAResource not registered", !BusFactory.resourceRegistered);
        busFactory.registerXAResource();
        assertTrue("XAResource not registered", !BusFactory.resourceRegistered);
    }*/

  
   
    public static Test suite() {
        return new TestSuite(BusFactoryTest.class);
    }

    public static void main(String[] args) {
        TestRunner.main(new String[] {BusFactoryTest.class.getName()});
    }
}


class DummyClassLoader extends ClassLoader {
    public DummyClassLoader() {
        super();
    }
}
