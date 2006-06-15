package org.objectweb.celtix.bus.transports.https;

import java.net.InetAddress;
import java.net.Socket;
import java.net.URL;
import java.util.Properties;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.easymock.classextension.EasyMock;
import org.objectweb.celtix.Bus;
import org.objectweb.celtix.BusException;

public class SSLSocketFactoryWrapperTest extends TestCase {

    
    private static final String DROP_BACK_SRC_DIR = 
        "../../../../../../../../src/test/java/org/objectweb/celtix/bus/transports/https/";

    Bus bus;
    
    private SSLSocketFactory sslSocketFactory;
    private SSLSocket mockSocket;

    


    public SSLSocketFactoryWrapperTest(String arg0) {
        super(arg0);
    }

    public static Test suite() throws Exception {
        TestSuite suite = new TestSuite(SSLSocketFactoryWrapperTest.class);
        return new TestSetup(suite) {
            protected void tearDown() throws Exception {
                super.tearDown();
            }
        };
    }


    public static void main(String[] args) {
        junit.textui.TestRunner.run(SSLSocketFactoryWrapperTest.class);
    }

    public void setUp() throws BusException {
        bus = EasyMock.createMock(Bus.class);
        sslSocketFactory = EasyMock.createMock(SSLSocketFactory.class);
        mockSocket = EasyMock.createMock(SSLSocket.class);
        
    }

    public void tearDown() throws Exception {
        EasyMock.reset(bus);
        EasyMock.reset(sslSocketFactory);
        EasyMock.reset(mockSocket);   
        Properties props = System.getProperties();
        props.remove("javax.net.ssl.trustStore");
        props.remove("javax.net.ssl.keyStore");
        props.remove("javax.net.ssl.keyPassword");
        props.remove("javax.net.ssl.keyStorePassword");
    }
    
    public void testConstructor1() {
        TestHandler handler = new TestHandler();
   
        try {
            String[] ciphs = new String[]{"a", "b", "c"};
            SSLSocketFactoryWrapper sslSocketFactoryWrapper =
                createSSLSocketFactoryWrapper(ciphs,
                                              handler);
            EasyMock.expect(sslSocketFactory.createSocket((String)EasyMock.anyObject(), 
                                                          EasyMock.anyInt())).andReturn(mockSocket);
            EasyMock.expect(sslSocketFactory.getDefaultCipherSuites()).andReturn(ciphs);
            EasyMock.expect(sslSocketFactory.getSupportedCipherSuites()).andReturn(ciphs);
            EasyMock.replay(sslSocketFactory);
            SSLSocket socket =  
                (SSLSocket)sslSocketFactoryWrapper.createSocket("localhost", 9001);
            assertTrue("Expected socket != null", socket != null);
            String[] defCiphers = sslSocketFactoryWrapper.getDefaultCipherSuites();
            assertTrue("Default Cipher suite not as expected", defCiphers[1].equals("b"));
            String[] suppCiphers = sslSocketFactoryWrapper.getSupportedCipherSuites();
            assertTrue("Supported Cipher suite not as expected", suppCiphers[1].equals("b"));
            
            EasyMock.reset(sslSocketFactory);
            EasyMock.reset(mockSocket);  
            EasyMock.expect(sslSocketFactory.createSocket((String)EasyMock.anyObject(), 
                                                          EasyMock.anyInt())).andReturn(null);
            EasyMock.expect(sslSocketFactory.getDefaultCipherSuites()).andReturn(null);
            EasyMock.expect(sslSocketFactory.getSupportedCipherSuites()).andReturn(null);
            EasyMock.replay(sslSocketFactory);
            socket =  
                (SSLSocket)sslSocketFactoryWrapper.createSocket("localhost", 9001);
            assertTrue("Expected socket == null", socket == null);
            
            
        }  catch (Exception e) {
            assertTrue("Caught an unexpected exception e = " + e, false);
        }
    }
    
    public void testConstructor2() {
        TestHandler handler = new TestHandler();

        try {
            String[] ciphs = new String[]{};
            SSLSocketFactoryWrapper sslSocketFactoryWrapper =
                createSSLSocketFactoryWrapper(ciphs,
                                              handler);
            EasyMock.expect(sslSocketFactory.createSocket((Socket)EasyMock.anyObject(),
                                                          (String)EasyMock.anyObject(), 
                                                          EasyMock.anyInt(),
                                                          EasyMock.anyBoolean())).andReturn(mockSocket);
            EasyMock.expect(sslSocketFactory.getDefaultCipherSuites()).andReturn(ciphs);
            EasyMock.expect(sslSocketFactory.getSupportedCipherSuites()).andReturn(ciphs);
            EasyMock.replay(sslSocketFactory);
            Socket socket =  
                sslSocketFactoryWrapper.createSocket(null, "localhost", 9001, true);
            assertTrue("Expected socket != null", socket != null);
            
            EasyMock.reset(sslSocketFactory);
            EasyMock.reset(mockSocket);  
            EasyMock.expect(sslSocketFactory.createSocket((Socket)EasyMock.anyObject(),
                                                          (String)EasyMock.anyObject(), 
                                                          EasyMock.anyInt(),
                                                          EasyMock.anyBoolean())).andReturn(null);
            EasyMock.expect(sslSocketFactory.getDefaultCipherSuites()).andReturn(null);
            EasyMock.expect(sslSocketFactory.getSupportedCipherSuites()).andReturn(null);
            EasyMock.replay(sslSocketFactory);
            socket =  
                sslSocketFactoryWrapper.createSocket(null, "localhost", 9001, true);
            assertTrue("Expected socket == null", socket == null);
        }  catch (Exception e) {
            assertTrue("Caught an unexpected exception e = " + e, false);
        }
    }
    
    public void testConstructor3() {
        TestHandler handler = new TestHandler();
        try {
            String[] ciphs = new String[]{"a", "b", "c"};
            SSLSocketFactoryWrapper sslSocketFactoryWrapper =
                createSSLSocketFactoryWrapper(ciphs,
                                              handler);
            EasyMock.expect(sslSocketFactory.createSocket((String)EasyMock.anyObject(), 
                                                          EasyMock.anyInt(),
                                                          (InetAddress)EasyMock.anyObject(), 
                                                          EasyMock.anyInt())).andReturn(mockSocket);
            EasyMock.expect(sslSocketFactory.getDefaultCipherSuites()).andReturn(ciphs);
            EasyMock.expect(sslSocketFactory.getSupportedCipherSuites()).andReturn(ciphs);
            EasyMock.replay(sslSocketFactory);
            Socket socket =  
                sslSocketFactoryWrapper.createSocket("localhost", 9001, null, 9001);
            assertTrue("Expected socket != null", socket != null);
            
            EasyMock.reset(sslSocketFactory);
            EasyMock.reset(mockSocket);  
            EasyMock.expect(sslSocketFactory.createSocket((String)EasyMock.anyObject(), 
                                                          EasyMock.anyInt(),
                                                          (InetAddress)EasyMock.anyObject(), 
                                                          EasyMock.anyInt())).andReturn(null);
            EasyMock.expect(sslSocketFactory.getDefaultCipherSuites()).andReturn(null);
            EasyMock.expect(sslSocketFactory.getSupportedCipherSuites()).andReturn(null);
            EasyMock.replay(sslSocketFactory);
            socket =  
                sslSocketFactoryWrapper.createSocket("localhost", 9001, null, 9001);
            assertTrue("Expected socket == null", socket == null);
            
        }  catch (Exception e) {
            assertTrue("Caught an unexpected exception e = " + e, false);
        }
    }
    
    
    public void testConstructor4() {
        TestHandler handler = new TestHandler();  

        try {
            String[] ciphs = new String[]{"a", "b", "c"};
            SSLSocketFactoryWrapper sslSocketFactoryWrapper =
                createSSLSocketFactoryWrapper(ciphs,
                                              handler);
            EasyMock.expect(sslSocketFactory.createSocket((InetAddress)EasyMock.anyObject(), 
                                                          EasyMock.anyInt())).andReturn(mockSocket);
            EasyMock.expect(sslSocketFactory.getDefaultCipherSuites()).andReturn(ciphs);
            EasyMock.expect(sslSocketFactory.getSupportedCipherSuites()).andReturn(ciphs);
            EasyMock.replay(sslSocketFactory);
            InetAddress addr = InetAddress.getLocalHost();
            Socket socket =  
                sslSocketFactoryWrapper.createSocket(addr, 9001);
            assertTrue("Expected socket != null", socket != null);
            
            EasyMock.reset(sslSocketFactory);
            EasyMock.reset(mockSocket);  
            EasyMock.expect(sslSocketFactory.createSocket((InetAddress)EasyMock.anyObject(), 
                                                          EasyMock.anyInt())).andReturn(null);
            EasyMock.expect(sslSocketFactory.getDefaultCipherSuites()).andReturn(null);
            EasyMock.expect(sslSocketFactory.getSupportedCipherSuites()).andReturn(null);
            EasyMock.replay(sslSocketFactory);
            
            socket =  
                sslSocketFactoryWrapper.createSocket(addr, 9001);
            assertTrue("Expected socket == null", socket == null);
            
        }  catch (Exception e) {
            assertTrue("Caught an unexpected exception e = " + e, false);
        }
    }
    
    public void testConstructor5() {
        TestHandler handler = new TestHandler();  

        try {
            String[] ciphs = new String[]{"a", "b", "c"};
            SSLSocketFactoryWrapper sslSocketFactoryWrapper =
                createSSLSocketFactoryWrapper(ciphs,
                                              handler);
            EasyMock.expect(sslSocketFactory.createSocket((InetAddress)EasyMock.anyObject(), 
                                                          EasyMock.anyInt(),
                                                          (InetAddress)EasyMock.anyObject(), 
                                                          EasyMock.anyInt())).andReturn(mockSocket);
            EasyMock.expect(sslSocketFactory.getDefaultCipherSuites()).andReturn(ciphs);
            EasyMock.expect(sslSocketFactory.getSupportedCipherSuites()).andReturn(ciphs);
            EasyMock.replay(sslSocketFactory);
            InetAddress addr = InetAddress.getLocalHost();
            Socket socket =  
                sslSocketFactoryWrapper.createSocket(addr, 9001, addr, 9001);
            assertTrue("Expected socket != null", socket != null);
            
        }  catch (Exception e) {
            assertTrue("Caught an unexpected exception e = " + e, false);
        }
    }
    
    public void testConstructor6() {
        TestHandler handler = new TestHandler();  

        try {
            String[] ciphs = null;
            SSLSocketFactoryWrapper sslSocketFactoryWrapper =
                createSSLSocketFactoryWrapper(ciphs,
                                              handler);
            EasyMock.expect(sslSocketFactory.createSocket((InetAddress)EasyMock.anyObject(), 
                                                          EasyMock.anyInt(),
                                                          (InetAddress)EasyMock.anyObject(), 
                                                          EasyMock.anyInt())).andReturn(mockSocket);
            EasyMock.expect(sslSocketFactory.getDefaultCipherSuites()).andReturn(ciphs);
            EasyMock.expect(sslSocketFactory.getSupportedCipherSuites()).andReturn(ciphs);
            EasyMock.replay(sslSocketFactory);
            InetAddress addr = InetAddress.getLocalHost();
            Socket socket =  
                sslSocketFactoryWrapper.createSocket(addr, 9001, addr, 9001);
            assertTrue("Expected socket != null", socket != null);
            
        }  catch (Exception e) {
            assertTrue("Caught an unexpected exception e = " + e, false);
        }
    }
    
    
    
    private SSLSocketFactoryWrapper createSSLSocketFactoryWrapper(
                                             String[] cipherSuite, 
                                             TestHandler handler) {
        try {
            
            SSLSocketFactoryWrapper sslSocketFactoryWrapper = 
                new SSLSocketFactoryWrapper(sslSocketFactory,
                                            cipherSuite);
            
            sslSocketFactoryWrapper.addLogHandler(handler);
            return sslSocketFactoryWrapper;
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    
    protected static String getPath(String fileName) {
        URL keystoreURL = SSLSocketFactoryWrapperTest.class.getResource(".");
        String str = keystoreURL.getFile(); 
        str += DROP_BACK_SRC_DIR  + fileName;
        return str;
    }
}


