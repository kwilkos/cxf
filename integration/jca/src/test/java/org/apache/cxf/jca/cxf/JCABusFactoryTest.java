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


import java.io.File;
import java.io.FileNotFoundException;
//import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;
// import java.util.ResourceBundle;

import javax.resource.ResourceException;
import javax.xml.namespace.QName;

import org.w3c.dom.Node;

import org.apache.cxf.Bus;
// import org.apache.cxf.BusException;
// import org.apache.cxf.common.i18n.Message;
import org.apache.cxf.binding.BindingFactoryManager;
import org.apache.cxf.binding.soap.SoapBindingFactory;
import org.apache.cxf.binding.soap.SoapTransportFactory;
import org.apache.cxf.bus.spring.SpringBusFactory;
import org.apache.cxf.endpoint.Server;
import org.apache.cxf.frontend.ServerFactoryBean;
import org.apache.cxf.jaxws.JAXWSMethodInvoker;
import org.apache.cxf.jaxws.support.JaxWsServiceFactoryBean;
import org.apache.cxf.jca.core.resourceadapter.ResourceAdapterInternalException;
import org.apache.cxf.service.Service;
import org.apache.cxf.service.factory.ReflectionServiceFactoryBean;
import org.apache.cxf.test.AbstractCXFTest;
import org.apache.cxf.transport.ConduitInitiatorManager;
import org.apache.cxf.transport.DestinationFactoryManager;
import org.apache.cxf.transport.local.LocalTransportFactory;
import org.apache.cxf.wsdl.WSDLManager;
import org.apache.cxf.wsdl11.WSDLManagerImpl;
// import org.apache.cxf.jca.cxf.test.DummyBus;
import org.easymock.classextension.EasyMock;
import org.junit.Test;

public class JCABusFactoryTest extends AbstractCXFTest {
   
    
    @Test
    public void testSetAppserverClassLoader() {
        ClassLoader loader = new DummyClassLoader();
        JCABusFactory bf = new JCABusFactory(new ManagedConnectionFactoryImpl());
        bf.setAppserverClassLoader(loader);
        assertSame("Checking appserverClassLoader.", loader, bf.getAppserverClassLoader());
    } 

     
    @Test
    public void testModifiedBusArguments() throws Exception {
        ManagedConnectionFactoryImpl mcf = new ManagedConnectionFactoryImpl();
        mcf.setConfigurationScope("a.b.c");

        JCABusFactory jcaBusFactory = new JCABusFactory(mcf);
        String[] args = jcaBusFactory.getBusArgs();

        assertTrue("Wrong arguments length: " + args.length, args.length == 2);
        assertEquals("Wrong Argument. ", args[0], "-BUSid");
        assertEquals("Wrong Argument. ", args[1],  "a.b.c");        
    }


//     public void testBusInitGetProps() throws Exception {
//         DummyBus.reset();
//         System.setProperty("test.bus.class", DummyBus.class.getName());
//         ManagedConnectionFactoryImpl mcf = new ManagedConnectionFactoryImpl();
       
//         assertEquals("bus not yet initialized", DummyBus.getInitializeCount(), 0);

        
//         JCABusFactory jcaBusFactory = new JCABusFactory(mcf);
//         jcaBusFactory.create(null, null);

//         assertEquals("bus initialized ", DummyBus.getInitializeCount(), 1);
       
//     }

//     public void testInitBusThrowsException() throws Exception {
//         DummyBus.reset();
//         System.setProperty("test.bus.class", DummyBus.class.getName());
//         final Exception thrown = new BusException(new Message("tested bus exception!", 
//                                                               (ResourceBundle)null, new Object[]{}));

//         ClassLoader originalCl = Thread.currentThread().getContextClassLoader();
//         try {
//             ManagedConnectionFactoryImpl mcf = new ManagedConnectionFactoryImpl();
           
//             JCABusFactory jcaBusFactory = new JCABusFactory(mcf);

//             // do this for MockObject creation
//             Thread.currentThread().setContextClassLoader(jcaBusFactory.getClass().getClassLoader());
//             // DummyBus.setThrowException(true);
            
                       
//             try {
//                 jcaBusFactory.create(null, null);
//                 fail("did not get expected resource exception");
//             } catch (ResourceException re) {
//                 System.out.println("*************************");
//                 System.out.println(" exception: " + re.getMessage());
//                 System.out.println("*************************");
//                 assertTrue("cause is set", re.getCause() != null);
//                 assertEquals("cause is expected type", thrown.getClass(), re.getCause().getClass());
//             }

//             assertEquals("init was called once", 1, DummyBus.getInitializeCount());

//         } finally {
//             Thread.currentThread().setContextClassLoader(originalCl);
//         }
//     }

//     public void testInitBusSetsThreadContextClassLoader() throws Exception {
//         DummyBus.reset();
//         System.setProperty("test.bus.class", DummyBus.class.getName());
//         ClassLoader originalCl = Thread.currentThread().getContextClassLoader();
//         try {
//             ManagedConnectionFactoryImpl mcf = new ManagedConnectionFactoryImpl();
          
//             JCABusFactory jcaBusFactory = new JCABusFactory(mcf);

//             // do this for MockObject creation
//             Thread.currentThread().setContextClassLoader(jcaBusFactory.getClass().getClassLoader());

//             Class dummyBusClass = Class.forName(DummyBus.class.getName(), true, jcaBusFactory.getClass()
//                 .getClassLoader());
//             // initialise here while thread context classloader is correct
//             dummyBusClass.newInstance();

//             jcaBusFactory.create(null, null);

         
//             assertEquals("init was called once", 1, DummyBus.getInitializeCount());
//             assertTrue("loader is correct in init", DummyBus.isCorrectThreadContextClassLoader());

//         } finally {
//             Thread.currentThread().setContextClassLoader(originalCl);
//         }
//     }

    // service strings to qname localparts
   

    @Test
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
        JCABusFactory jcaBusFactory = new JCABusFactory(mcf);
        for (int i = 0; i < ejbServantServicePorpsTestStrings.length; i++) {
            String val = (String)ejbServantServicePorpsTestStrings[i][0];
            QName expected = (QName)ejbServantServicePorpsTestStrings[i][1];

            assertEquals("correct qname from mapping for " + val, expected, jcaBusFactory
                .serviceQNameFromString(val));
        }
    }

    @Test
    public void testInvalidQNameFromString() throws Exception {
        ManagedConnectionFactoryImpl mcf = new ManagedConnectionFactoryImpl();
        JCABusFactory jcaBusFactory = new JCABusFactory(mcf);
        try {
            jcaBusFactory.serviceQNameFromString("a@");
            fail("expecte ex on invalid format");
        } catch (ResourceException expected) {
            assertTrue(expected.getCause() instanceof java.util.NoSuchElementException);
        }
    }

    

    @Test
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
        JCABusFactory jcaBusFactory = new JCABusFactory(mcf);
        for (int i = 0; i < ejbServantServicePropsTestStringsWsdlLoc.length; i++) {
            String val = ejbServantServicePropsTestStringsWsdlLoc[i][0];
            String expectedUrl = ejbServantServicePropsTestStringsWsdlLoc[i][1];

            assertEquals("correct wsdlLocation from mapping for " + val, expectedUrl, jcaBusFactory
                .wsdlLocFromString(val));
        }
    }

    
    @Test
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
        JCABusFactory jcaBusFactory = new JCABusFactory(mcf);
        for (int i = 0; i < ejbServantServicePropsTestStringsPortName.length; i++) {
            String val = ejbServantServicePropsTestStringsPortName[i][0];
            String expectedUrl = ejbServantServicePropsTestStringsPortName[i][1];

            assertEquals("correct wsdlLocation from mapping for " + val, expectedUrl, jcaBusFactory
                .portNameFromString(val));
        }
    }

    @Test
    public void testInvalidPortNameFromString() throws Exception {
        ManagedConnectionFactoryImpl mcf = new ManagedConnectionFactoryImpl();
        JCABusFactory jcaBusFactory = new JCABusFactory(mcf);
        try {
            jcaBusFactory.portNameFromString("serviceName,");
            fail("expect ex on invalid format");
        } catch (ResourceException expected) {
            assertTrue(expected.getCause() instanceof java.util.NoSuchElementException);
        }

        try {
            jcaBusFactory.portNameFromString("serviceName,@abc");
            fail("expect ex on invalid format");
        } catch (ResourceException expected) {
            assertTrue("Exception message starts with Empty portName", expected.getMessage()
                .startsWith("Empty portName"));
        }

        try {
            jcaBusFactory.portNameFromString("serviceName,abc,uuu");
            fail("expect ex on invalid format");
        } catch (ResourceException expected) {
            assertTrue("Exception message starts with portName already set", expected.getMessage()
                .startsWith("portName already set"));
        }
    }

    @Test
    public void testLoadNonexistentProperties() throws Exception {
        ManagedConnectionFactoryImpl mcf = new ManagedConnectionFactoryImpl();
        JCABusFactory jcaBusFactory = new JCABusFactory(mcf);
        try {
            jcaBusFactory.loadProperties(new File("/rubbish_name.properties").toURI().toURL());
            fail("expect an exception .");
        } catch (ResourceException re) {
            assertTrue("Cause is FileNotFoundException, cause: " + re.getCause(),
                       re.getCause() instanceof FileNotFoundException);
        }
    }

    public void testInvalidMonitorConfigNoPropsURL() throws Exception {
        ManagedConnectionFactoryImpl mcf = new ManagedConnectionFactoryImpl();
        mcf.setMonitorEJBServiceProperties(Boolean.TRUE);
        JCABusFactory jcaBusFactory = new JCABusFactory(mcf);
        try {
            Bus mockBus = EasyMock.createMock(Bus.class);
            jcaBusFactory.setBus(mockBus);
            jcaBusFactory.initialiseServants();
            fail("exception expected");
        } catch (ResourceAdapterInternalException re) {
            assertTrue("EJBServiceProperties is not set.", re.getMessage()
                .indexOf("EJBServicePropertiesURL is not set") != -1);
        }
    }
    
    
    @Test
    public void testInitServants() throws Exception {
        ManagedConnectionFactoryImpl mcf = new ManagedConnectionFactoryImpl();
        //get resource 
        URL propFile = getClass().getResource("resources/ejb_servants.properties");
        mcf.setEJBServicePropertiesURL(propFile.toString());
        JCABusFactory jcaBusFactory = new JCABusFactory(mcf);
        Bus mockBus = EasyMock.createMock(Bus.class);

        jcaBusFactory.setBus((Bus)mockBus);
        jcaBusFactory.initialiseServants();
        
    }
    
    @Test
    public void testCreateService() throws Exception {
        Bus springBus = new SpringBusFactory().createBus();
        
        JCABusFactory jcaBusFactory = new JCABusFactory(null);
        jcaBusFactory.setBus(springBus);
        jcaBusFactory.initBus();
        
        ReflectionServiceFactoryBean bean = new JaxWsServiceFactoryBean();
        Service service = jcaBusFactory.createService(HelloInterface.class, bean);
        assertEquals("test", service.get("test"));
    }

    @Test
    public void testCreateServer() throws Exception {
        //Bus springBus = new SpringBusFactory().createBus();
        
        SoapBindingFactory bindingFactory = new SoapBindingFactory();

        bus.getExtension(BindingFactoryManager.class)
            .registerBindingFactory("http://schemas.xmlsoap.org/wsdl/soap/", bindingFactory);

        DestinationFactoryManager dfm = bus.getExtension(DestinationFactoryManager.class);

        SoapTransportFactory soapDF = new SoapTransportFactory();
        soapDF.setBus(bus);
        dfm.registerDestinationFactory("http://schemas.xmlsoap.org/wsdl/soap/", soapDF);
        dfm.registerDestinationFactory("http://schemas.xmlsoap.org/soap/", soapDF);
        
        LocalTransportFactory localTransport = new LocalTransportFactory();
        dfm.registerDestinationFactory("http://schemas.xmlsoap.org/soap/http", localTransport);
        dfm.registerDestinationFactory("http://schemas.xmlsoap.org/wsdl/soap/http", localTransport);
        dfm.registerDestinationFactory("http://cxf.apache.org/bindings/xformat", localTransport);

        ConduitInitiatorManager extension = bus.getExtension(ConduitInitiatorManager.class);
        extension.registerConduitInitiator(LocalTransportFactory.TRANSPORT_ID, localTransport);
        extension.registerConduitInitiator("http://schemas.xmlsoap.org/wsdl/soap/", localTransport);
        extension.registerConduitInitiator("http://schemas.xmlsoap.org/soap/http", localTransport);
        extension.registerConduitInitiator("http://schemas.xmlsoap.org/soap/", localTransport);
        
        bus.setExtension(new WSDLManagerImpl(), WSDLManager.class);

        
        JCABusFactory jcaBusFactory = new JCABusFactory(null);
        jcaBusFactory.setBus(bus);
        jcaBusFactory.initBus();
        
        ReflectionServiceFactoryBean bean = new JaxWsServiceFactoryBean();
        Service service = jcaBusFactory.createService(HelloInterface.class, bean);
        assertEquals("test", service.get("test"));
        
        Imple im = new Imple();
        
        service.setInvoker(new JAXWSMethodInvoker(im));

        ServerFactoryBean svrFactory = new ServerFactoryBean();

        String address = "http://localhost:9999/Hello";
        Server server = jcaBusFactory.createServer(svrFactory, bean, address);
        assertNotNull("The server should not be null", server);
        
        Node res = invoke("http://localhost:9999/Hello", 
                          LocalTransportFactory.TRANSPORT_ID,
                          "sayHi.xml");
        assertNotNull("We should get the result ", res);
    }
  
    /*
    public void testAddServantsCache() throws Exception {
        ManagedConnectionFactoryImpl mcf = new ManagedConnectionFactoryImpl();
        JCABusFactory jcaBusFactory = new JCABusFactory(mcf);
        ClassLoader cl = this.getClass().getClassLoader();
        Bus bus = jcaBusFactory.initBus(cl);

        Properties props = new Properties();
        String wsdlLocation =
            this.getClass().getResource("resources/hello_world.wsdl").toString();

        System.out.println("  in test wsdlLocation: " + wsdlLocation);
        props.put("jndiName", "{http://apache.org/hello_world_soap_http}SOAPService@"
                  + wsdlLocation);

        assertTrue("there's no registered servants at beginning", jcaBusFactory.getRegisteredServants()
            .isEmpty());
        jcaBusFactory.setBus(bus);
        jcaBusFactory.initialiseServantsFromProperties(props, true);
       
        javax.xml.ws.Endpoint ep = (javax.xml.ws.Endpoint) jcaBusFactory.getRegisteredServants().get(0);
              
        assertTrue("registered servant with the expected service name", ((javax.xml.ws.Endpoint)jcaBusFactory
            .getRegisteredServants().get(0)).isPublished());
        ep.stop();
        jcaBusFactory.deregisterServants(bus);

        assertTrue("servants should be deregistered", jcaBusFactory.getRegisteredServants().isEmpty());
        bus.shutdown(true);
    }
    
    */
//     public void testInitServantsFromPropertiesWithPortName() throws Exception {
//         ManagedConnectionFactoryImpl mcf = new ManagedConnectionFactoryImpl();
//         JCABusFactory jcaBusFactory = new JCABusFactory(mcf);
//         Bus mockBus = EasyMock.createMock(Bus.class);
//         jcaBusFactory.setBus(mockBus);
//         Properties props = new Properties();
//         props.put("jndiName", "{http://objectweb.org/hello_world_soap_http}SOAPService,SoapPort@file:///");
//         try {
//             jcaBusFactory.initialiseServantsFromProperties(props, true);
//         } catch (ResourceException expected) {
//             assertTrue("reasonable message", expected.toString().indexOf("jndiName") != -1);
//             assertTrue(expected instanceof ResourceAdapterInternalException);            
//         }
//     }

//     public void testInitServantsFromPropertiesWithMissingWsdlLocInPropertiesAndConfig() throws Exception {
//         ManagedConnectionFactoryImpl mcf = new ManagedConnectionFactoryImpl();
//         JCABusFactory jcaBusFactory = new JCABusFactory(mcf);
//         Bus mockBus = EasyMock.createMock(Bus.class);
//         jcaBusFactory.setBus(mockBus);
//         final String jndiName = "/a/b";
//         try {
//             Properties props = new Properties();
//             props.put(jndiName, "{http://ns}ServiceA");
//             jcaBusFactory.initialiseServantsFromProperties(props, true);
//             fail("expect ex on missing wsdl loc");
//         } catch (ResourceException expected) {
//             assertTrue("reasonable message", expected.toString().indexOf(jndiName) != -1);
//             assertTrue(expected instanceof ResourceAdapterInternalException);
//             assertTrue(expected.getMessage().indexOf("ServiceA") != -1);
//         }
//     }
     
    @Test
    public void testInitServantsFromPropertiesWithNoServiceQName() throws Exception {
        ManagedConnectionFactoryImpl mcf = new ManagedConnectionFactoryImpl();
        JCABusFactory jcaBusFactory = new JCABusFactory(mcf);
        Bus mockBus = EasyMock.createMock(Bus.class);
        jcaBusFactory.setBus(mockBus);
        final String jndiName = "/a/b";
        try {
            Properties props = new Properties();
            props.put(jndiName, "");
            jcaBusFactory.initialiseServantsFromProperties(props, true);
            fail("expect ex on missing service QName value");
        } catch (ResourceException expected) {
            assertTrue("reasonable message", expected.toString().indexOf(jndiName) != -1);
        }
    }

/*  so far doesn't support wsdl file  
    public void testInitFromPropsWithInvalidWsdlLocUrls() throws Exception {
        ManagedConnectionFactoryImpl mcf = new ManagedConnectionFactoryImpl();
        JCABusFactory jcaBusFactory = new JCABusFactory(mcf);

      
        try {
            Properties props = new Properties();
            props.put("/a/b", "{http://ns}ServiceA@unknownprotocol:/a");
            jcaBusFactory.initialiseServantsFromProperties(props, true);
            fail("expect ex on unknown protocol");
        } catch (ResourceException expected) {
            assertTrue("have a jcaBusFactorye " + expected.getCause(),
                       expected.getCause() instanceof MalformedURLException);
        }

        try {
            Properties props = new Properties();
            props.put("/a/b", "{http://ns}ServiceA@a/b");
            jcaBusFactory.initialiseServantsFromProperties(props, true);

            fail("expect ex on invalid format, no scheme");
        } catch (ResourceException expected) {
            assertTrue("have a mue " + expected.getCause(),
                       expected.getCause() instanceof MalformedURLException);
        }

        try {
            Properties props = new Properties();
            props.put("/a/b", "{http://ns}ServiceA@http://nowhere.plannetx.cupoftea:9090/NoWhere");
            jcaBusFactory.initialiseServantsFromProperties(props, true);

            fail("expect ex on invalid url, dud host name");
        } catch (ResourceException expected) {
            // resolving the URL above results in an IOException which
            // may be UnknowHostException or something different
            // depending on the platoform
            assertTrue("unexpected exception received: " + expected.getCause(),
                       expected.getCause() instanceof java.io.IOException);       

        }
          
    }
*/
    
    @Test
    public void testInitFromPropsDoesNotThrowExceptionWhenSomethingGoesWrong() throws Exception {
        ManagedConnectionFactoryImpl mcf = new ManagedConnectionFactoryImpl();
        JCABusFactory jcaBusFactory = new JCABusFactory(mcf);
        Bus mockBus = EasyMock.createMock(Bus.class);
        jcaBusFactory.setBus(mockBus);
        Properties props = new Properties();
        props.put("/a/b", "{http://ns}ServiceA@unknownprotocol:/a");
        
        jcaBusFactory.initialiseServantsFromProperties(props, false);
           
    }

    /*
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
        JCABusFactory jcaBusFactory = new JCABusFactory(mcf);
        Bus mockBus = EasyMock.createMock(Bus.class);
        jcaBusFactory.setBus((Bus)mockBus);

        JCABusFactory.EJBServicePropertiesMonitorRunnable propsRunnable =
            jcaBusFactory.new EJBServicePropertiesMonitorRunnable(5);
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

        JCABusFactory jcaBusFactory = new JCABusFactory(mcf);
        BootstrapContext bc = (BootstrapContext)MockObjectFactory.create(BootstrapContext.class);
        assertNotNull("BootstrapContext is not null", bc);
        jcaBusFactory.create(null, bc);
        assertEquals("BoostrapContext set", jcaBusFactory.getBootstrapContext(), bc);
        assertEquals("bus initialized ", DummyBus.initializeCount, 1);
    }
*/
   
}


class DummyClassLoader extends ClassLoader {
    public DummyClassLoader() {
        super();
    }
}
