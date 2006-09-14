/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.cxf.jca.cxf;


// import java.io.File;
// import java.io.FileNotFoundException;
// import java.net.MalformedURLException;

// import java.util.Properties;
//import java.util.ResourceBundle;

//import javax.resource.ResourceException;
//import javax.xml.namespace.QName;


import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;


//import org.apache.cxf.Bus;
//import org.apache.cxf.BusException;
//import org.apache.cxf.common.i18n.Message;
//import org.apache.cxf.jca.core.resourceadapter.ResourceAdapterInternalException;
//import org.apache.cxf.jca.cxf.test.DummyBus;
//import org.easymock.classextension.EasyMock;

public class JCABusFactoryTest extends TestCase {
   
    public JCABusFactoryTest(String name) {
        super(name);
    }

   
    public void testSetAppserverClassLoader() {
        ClassLoader loader = new DummyClassLoader();
        JCABusFactory bf = new JCABusFactory(new ManagedConnectionFactoryImpl());
        bf.setAppserverClassLoader(loader);
        assertSame("Checking appserverClassLoader.", loader, bf.getAppserverClassLoader());
    } 

     
    public void testModifiedBusArguments() throws Exception {
        ManagedConnectionFactoryImpl mcf = new ManagedConnectionFactoryImpl();
        mcf.setConfigurationScope("a.b.c");

        JCABusFactory jcaBusFactory = new JCABusFactory(mcf);
        String[] args = jcaBusFactory.getBusArgs();

        assertTrue("Wrong arguments length: " + args.length, args.length == 2);
        assertEquals("Wrong Argument. ", args[0], "-BUSid");
        assertEquals("Wrong Argument. ", args[1],  "a.b.c");        
    }

    /*
    public void testBusInitGetProps() throws Exception {
        DummyBus.reset();
        System.setProperty("test.bus.class", DummyBus.class.getName());
        ManagedConnectionFactoryImpl mcf = new ManagedConnectionFactoryImpl();
       
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
           
            BusFactory busFactory = new BusFactory(mcf);

            // do this for MockObject creation
            Thread.currentThread().setContextClassLoader(busFactory.getClass().getClassLoader());
            //            DummyBus.setThrowException(true);
            
                       
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
    */
    // service strings to qname localparts
   
/*
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
        Bus bus = Bus.init();

        Properties props = new Properties();
        String wsdlLocation =
            this.getClass().getResource("resources/hello_world.wsdl").toString();
       
        props.put("jndiName", "{http://objectweb.org/hello_world_soap_http}SOAPService@"
                  + wsdlLocation);

        assertTrue("there's no registered servants at beginning", busFactory.getRegisteredServants()
            .isEmpty());
        busFactory.setBus(bus);
        busFactory.initialiseServantsFromProperties(props, true);
       
        javax.xml.ws.Endpoint ep = (javax.xml.ws.Endpoint) busFactory.getRegisteredServants().get(0);
              
        assertTrue("registered servant with the expected service name", ((Endpoint)busFactory
            .getRegisteredServants().get(0)).isPublished());
        ep.stop();
        busFactory.deregisterServants(bus);

        assertTrue("servants should be deregistered", busFactory.getRegisteredServants().isEmpty());
        bus.shutdown(true);
    }
    
    
//     public void testInitServantsFromPropertiesWithPortName() throws Exception {
//         ManagedConnectionFactoryImpl mcf = new ManagedConnectionFactoryImpl();
//         BusFactory busFactory = new BusFactory(mcf);
//         Bus mockBus = EasyMock.createMock(Bus.class);
//         busFactory.setBus(mockBus);
//         Properties props = new Properties();
//         props.put("jndiName", "{http://objectweb.org/hello_world_soap_http}SOAPService,SoapPort@file:///");
//         try {
//             busFactory.initialiseServantsFromProperties(props, true);
//         } catch (ResourceException expected) {
//             assertTrue("reasonable message", expected.toString().indexOf("jndiName") != -1);
//             assertTrue(expected instanceof ResourceAdapterInternalException);            
//         }
//     }

    public void testInitServantsFromPropertiesWithMissingWsdlLocInPropertiesAndConfig() throws Exception {
        ManagedConnectionFactoryImpl mcf = new ManagedConnectionFactoryImpl();
        BusFactory busFactory = new BusFactory(mcf);
        Bus mockBus = EasyMock.createMock(Bus.class);
        busFactory.setBus(mockBus);
        final String jndiName = "/a/b";
        try {
            Properties props = new Properties();
            props.put(jndiName, "{http://ns}ServiceA");
            busFactory.initialiseServantsFromProperties(props, true);
            fail("expect ex on missing wsdl loc");
        } catch (ResourceException expected) {
            assertTrue("reasonable message", expected.toString().indexOf(jndiName) != -1);
            assertTrue(expected instanceof ResourceAdapterInternalException);
            assertTrue(expected.getMessage().indexOf("ServiceA") != -1);
        }
    }
     
    public void testInitServantsFromPropertiesWithNoServiceQName() throws Exception {
        ManagedConnectionFactoryImpl mcf = new ManagedConnectionFactoryImpl();
        BusFactory busFactory = new BusFactory(mcf);
        Bus bus = EasyMock.createMock(Bus.class);
        busFactory.setBus(bus);
        final String jndiName = "/a/b";
        try {
            Properties props = new Properties();
            props.put(jndiName, "");
            busFactory.initialiseServantsFromProperties(props, true);
            fail("expect ex on missing service QName value");
        } catch (ResourceException expected) {
            assertTrue("reasonable message", expected.toString().indexOf(jndiName) != -1);
        }
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
        Bus mockBus = EasyMock.createMock(Bus.class);
        busFactory.setBus(mockBus);
        Properties props = new Properties();
        props.put("/a/b", "{http://ns}ServiceA@unknownprotocol:/a");
        
        busFactory.initialiseServantsFromProperties(props, false);
           
    }
    
    public void testPropertiesMonitorThreadCausesSomeFailures() throws Exception {
        FileChannel in = null;
        FileChannel out = null;
        URL propFile = getClass().getResource("resources/ejb_servants_one_wrong.properties");
        
        File origFile = new File(propFile.toString().substring(5));
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
        Bus mockBus = EasyMock.createMock(Bus.class);
        busFactory.setBus((Bus)mockBus);

        BusFactory.EJBServicePropertiesMonitorRunnable propsRunnable =
            busFactory.new EJBServicePropertiesMonitorRunnable(5);
        propsRunnable.setContinue(false);        
        propsRunnable.run();
        //do nothing here 
        
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
        
    }
    
   public void testInitServantsWithBootstrapContextNotNull() throws Exception {
        System.setProperty("test.bus.class", DummyBus.class.getName());
        ManagedConnectionFactoryImpl mcf = new ManagedConnectionFactoryImpl();
        mcf.setCXFInstallDir(DummyBus.vobRoot());
        mcf.setCXFCEURL(DummyBus.CXFCEURL);

        assertEquals("bus not yet initialized", DummyBus.initializeCount, 0);

        BusFactory busFactory = new BusFactory(mcf);
        BootstrapContext bc = (BootstrapContext)MockObjectFactory.create(BootstrapContext.class);
        assertNotNull("BootstrapContext is not null", bc);
        busFactory.create(null, bc);
        assertEquals("BoostrapContext set", busFactory.getBootstrapContext(), bc);
        assertEquals("bus initialized ", DummyBus.initializeCount, 1);
    }

   */

  
   
    public static Test suite() {
        return new TestSuite(JCABusFactoryTest.class);
    }

    public static void main(String[] args) {
        TestRunner.main(new String[] {JCABusFactoryTest.class.getName()});
    }
}


class DummyClassLoader extends ClassLoader {
    public DummyClassLoader() {
        super();
    }
}
