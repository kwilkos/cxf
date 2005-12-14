package org.objectweb.celtix.systest.basic;

import java.lang.reflect.UndeclaredThrowableException;
import java.net.URL;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.xml.namespace.QName;
//import javax.xml.ws.AsyncHandler;
import javax.xml.ws.AsyncHandler;
import javax.xml.ws.Response;

import org.objectweb.celtix.systest.common.ClientServerTestBase;
import org.objectweb.hello_world_soap_http.BadRecordLitFault;
import org.objectweb.hello_world_soap_http.Greeter;
import org.objectweb.hello_world_soap_http.NoSuchCodeLitFault;
import org.objectweb.hello_world_soap_http.SOAPService;
import org.objectweb.hello_world_soap_http.types.BareDocumentResponse;
import org.objectweb.hello_world_soap_http.types.GreetMeSometimeResponse;

public class ClientServerTest extends ClientServerTestBase {

    private final QName serviceName = new QName("http://objectweb.org/hello_world_soap_http",
                                                "SOAPService");    
    private final QName portName = new QName("http://objectweb.org/hello_world_soap_http",
                                             "SoapPort");

    
    public void onetimeSetUp() { 
        assertTrue("server did not launch correctly", launchServer(Server.class));
    }
    
    public void testBasicConnection() throws Exception {
        URL wsdl = getClass().getResource("/wsdl/hello_world.wsdl");
        assertNotNull(wsdl);
        
        SOAPService service = new SOAPService(wsdl, serviceName);
        assertNotNull(service);
        
        Greeter greeter = service.getPort(portName, Greeter.class);
        
        String response1 = new String("Hello Milestone-");
        String response2 = new String("Bonjour");
        try {                        
            for (int idx = 0; idx < 5; idx++) {
                String greeting = greeter.greetMe("Milestone-" + idx);
                assertNotNull("no response received from service", greeting);
                String exResponse = response1 + idx;
                assertEquals(exResponse, greeting);
                
                String reply = greeter.sayHi();
                assertNotNull("no response received from service", reply);
                assertEquals(response2, reply);

                greeter.greetMeOneWay("Milestone-" + idx);
            }            
        } catch (UndeclaredThrowableException ex) {
            throw (Exception)ex.getCause();
        }
        
        try {
            BareDocumentResponse bareres = greeter.testDocLitBare("MySimpleDocument");
            fail("Should have thrown Exception as SOAP Doc/Lit Bare Style is not yet supported");
            assertNotNull("no response for operation testDocLitBare", bareres);
            assertEquals("Celtix", bareres.getCompany());
            assertTrue(bareres.getId() == 1);
        } catch (Exception e) {
            //e.printStackTrace();
            //Ignore as exception is expected.
        }
    } 

    public void testAsyncPollingCall() throws Exception {
        URL wsdl = getClass().getResource("/wsdl/hello_world.wsdl");
        assertNotNull(wsdl);
        
        SOAPService service = new SOAPService(wsdl, serviceName);
        assertNotNull(service);
        ExecutorService executor = Executors.newFixedThreadPool(5);
        service.setExecutor(executor);
        assertNotNull(service);

        String expectedString = new String("How are you Joe");
        try {
            Greeter greeter = (Greeter) service.getPort(portName, Greeter.class);
            
            Response<GreetMeSometimeResponse> response = greeter.greetMeSometimeAsync("Joe");
            while (!response.isDone()) {
                Thread.sleep(100);
            }
            GreetMeSometimeResponse reply = response.get();
            assertNotNull("no response received from service", reply);
            String s = reply.getResponseType();
            assertEquals(expectedString, s);   
        } catch (UndeclaredThrowableException ex) {
            throw (Exception)ex.getCause();
        }
        executor.shutdown();
    }
    
    public void testAsyncSynchronousPolling() throws Exception {
        URL wsdl = getClass().getResource("/wsdl/hello_world.wsdl");
        assertNotNull(wsdl);
        
        SOAPService service = new SOAPService(wsdl, serviceName);
        assertNotNull(service);
        ExecutorService executor = Executors.newFixedThreadPool(5);
        service.setExecutor(executor);
        assertNotNull(service);

        final String expectedString = new String("How are you Joe");
          
        class Poller extends Thread {
            Response<GreetMeSometimeResponse> response;
            int tid;
            
            Poller(Response<GreetMeSometimeResponse> r, int t) {
                response = r;
                tid = t;
            }
            public void run() {
                if (tid % 2 > 0) {
                    while (!response.isDone()) {
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException ex) {
                            // ignore
                        }
                    }
                }
                GreetMeSometimeResponse reply = null;
                try {
                    reply = response.get();
                } catch (Exception ex) {
                    fail("Poller " + tid + " failed with " + ex);
                }
                assertNotNull("Poller " + tid + ": no response received from service", reply);
                String s = reply.getResponseType();
                assertEquals(expectedString, s);   
            }
        }
        
        Greeter greeter = (Greeter) service.getPort(portName, Greeter.class);
        Response<GreetMeSometimeResponse> response = greeter.greetMeSometimeAsync("Joe");
        
        Poller[] pollers = new Poller[4];
        for (int i = 0; i < pollers.length; i++) {
            pollers[i] = new Poller(response, i);
        }
        for (Poller p : pollers) {            
            p.start();
        }
        
        for (Poller p : pollers) {
            p.join();
        }
        
        executor.shutdown();    
    }
    
    static class MyHandler implements AsyncHandler<GreetMeSometimeResponse> {        
        static int invocationCount;
        private String replyBuffer;
        
        public void handleResponse(Response<GreetMeSometimeResponse> response) {
            invocationCount++;
            try {
                GreetMeSometimeResponse reply = response.get();
                replyBuffer = reply.getResponseType();
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            } catch (ExecutionException ex) {
                ex.printStackTrace();
            }            
        } 
        
        String getReplyBuffer() {
            return replyBuffer;
        }
    }
    
    public void testAsyncCallWithHandler() throws Exception {
        URL wsdl = getClass().getResource("/wsdl/hello_world.wsdl");
        assertNotNull(wsdl);
        
        SOAPService service = new SOAPService(wsdl, serviceName);
        ExecutorService executor = Executors.newFixedThreadPool(5);
        service.setExecutor(executor);
        assertNotNull(service);
        
        MyHandler h = new MyHandler();
        MyHandler.invocationCount = 0;

        String expectedString = new String("How are you Joe");
        try {
            Greeter greeter = (Greeter) service.getPort(portName, Greeter.class);
            Future<?> f = greeter.greetMeSometimeAsync("Joe", h);
            int i = 0;
            while (!f.isDone() && i < 10) {
                Thread.sleep(100);
                i++;
            }
            assertEquals("callback was not executed or did not return the expected result",
                         expectedString, h.getReplyBuffer());
        } catch (UndeclaredThrowableException ex) {
            throw (Exception)ex.getCause();
        }
        assertEquals(1, MyHandler.invocationCount);       
        executor.shutdown();
    }
    
    public void testAsyncCallWithHandlerAndMultipleClients() throws Exception {
        URL wsdl = getClass().getResource("/wsdl/hello_world.wsdl");
        assertNotNull(wsdl);
        
        SOAPService service = new SOAPService(wsdl, serviceName);
        ExecutorService executor = Executors.newFixedThreadPool(5);
        service.setExecutor(executor);
        assertNotNull(service);
        
        final MyHandler h = new MyHandler();
        MyHandler.invocationCount = 0;

        final String expectedString = new String("How are you Joe");
        
        class Poller extends Thread {
            Future<?> future;
            int tid;
            
            Poller(Future<?> f, int t) {
                future = f;
                tid = t;
            }
            public void run() {
                if (tid % 2 > 0) {
                    while (!future.isDone()) {
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException ex) {
                            // ignore
                        }
                    }
                }
                try {
                    future.get();
                } catch (Exception ex) {
                    fail("Poller " + tid + " failed with " + ex);
                }
                assertEquals("callback was not executed or did not return the expected result",
                             expectedString, h.getReplyBuffer());
            }
        }
        
        Greeter greeter = (Greeter) service.getPort(portName, Greeter.class);
        Future<?> f = greeter.greetMeSometimeAsync("Joe", h);
        
        Poller[] pollers = new Poller[4];
        for (int i = 0; i < pollers.length; i++) {
            pollers[i] = new Poller(f, i);
        }
        for (Poller p : pollers) {            
            p.start();
        }
        
        for (Poller p : pollers) {
            p.join();
        }
        assertEquals(1, MyHandler.invocationCount);   
        executor.shutdown();    
    }
    
    
 
    public void testFaults() throws Exception {
        URL wsdl = getClass().getResource("/wsdl/hello_world.wsdl");
        assertNotNull(wsdl);
        
        SOAPService service = new SOAPService(wsdl, serviceName);
        ExecutorService ex = Executors.newFixedThreadPool(1);
        service.setExecutor(ex);
        assertNotNull(service);

        String noSuchCodeFault = "NoSuchCodeLitFault";
        String badRecordFault = "BadRecordLitFault";

        Greeter greeter = service.getPort(portName, Greeter.class);
        for (int idx = 0; idx < 2; idx++) {
            try {
                greeter.testDocLitFault(noSuchCodeFault);
                fail("Should have thrown NoSuchCodeLitFault exception");
            } catch (NoSuchCodeLitFault nslf) {
                assertNotNull(nslf.getFaultInfo());
                assertNotNull(nslf.getFaultInfo().getCode());
            } 
            
            try {
                greeter.testDocLitFault(badRecordFault);
                fail("Should have thrown BadRecordLitFault exception");
            } catch (BadRecordLitFault brlf) {
                assertNotNull(brlf.getFaultInfo());
            }
        }

    } 

    public static void main(String[] args) {
        junit.textui.TestRunner.run(ClientServerTest.class);
    }
    


    /*
    
    public static void main(String[] args) {
        ClientServerTest cst = new ClientServerTest();
        
        if ("client".equals(args[0])) {
            try {
                cst.testAsyncPollingCall();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            System.err.println("Exiting...........");
            System.exit(0);
        } else if ("server".equals(args[0])) {
            try {
               // cst.setUp();
                cst.onetimeSetUp();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        } else {
            System.out.println("Invaid arg");
        }

    }
    
    */
    
    
    
    
}
