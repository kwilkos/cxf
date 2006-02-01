package org.objectweb.celtix.bus.bindings.soap;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import javax.xml.namespace.QName;

import javax.xml.soap.Detail;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPFault;
import javax.xml.soap.SOAPMessage;

import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;
import javax.xml.ws.Binding;
import javax.xml.ws.Endpoint;
import javax.xml.ws.WebFault;
import javax.xml.ws.WebServiceProvider;
import javax.xml.ws.handler.Handler;
import javax.xml.ws.handler.MessageContext;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import junit.framework.TestCase;

import org.objectweb.celtix.Bus;
import org.objectweb.celtix.bindings.DataBindingCallback;
import org.objectweb.celtix.bindings.ServerBindingEndpointCallback;
import org.objectweb.celtix.bus.jaxws.DynamicDataBindingCallback;
import org.objectweb.celtix.bus.jaxws.EndpointUtils;
import org.objectweb.celtix.bus.jaxws.JAXBDataBindingCallback;
import org.objectweb.celtix.context.InputStreamMessageContext;
import org.objectweb.celtix.context.ObjectMessageContext;
import org.objectweb.celtix.context.OutputStreamMessageContext;
import org.objectweb.celtix.transports.ServerTransport;
import org.objectweb.celtix.transports.ServerTransportCallback;
import org.objectweb.celtix.ws.addressing.EndpointReferenceType;
import org.objectweb.celtix.wsdl.EndpointReferenceUtils;

import org.objectweb.hello_world_soap_http.HWSoapMessageProvider;
import org.objectweb.hello_world_soap_http.HWSourcePayloadProvider;
import org.objectweb.hello_world_soap_http.HelloWorldServiceProvider;
import org.objectweb.hello_world_soap_http.NoSuchCodeLitFault;
import org.objectweb.hello_world_soap_http.NotAnnotatedGreeterImpl;
import org.objectweb.hello_world_soap_http.NotAnnotatedGreeterImplRPCLit;


public class SOAPServerBindingTest extends TestCase {
    Bus bus;
    EndpointReferenceType epr;
    
    public SOAPServerBindingTest(String arg0) {
        super(arg0);
    }

    public static void main(String[] args) {
        junit.textui.TestRunner.run(SOAPServerBindingTest.class);
    }
    
    protected void setUp() throws Exception {
        super.setUp();
        bus = Bus.init();
        epr = new EndpointReferenceType();
        
        URL wsdlUrl = getClass().getResource("/wsdl/hello_world.wsdl");
        QName serviceName = new QName("http://objectweb.org/hello_world_soap_http", "SOAPService");
        epr = EndpointReferenceUtils.getEndpointReference(wsdlUrl, serviceName, "SoapPort");
    }

    public void testGetBinding() throws Exception {
        SOAPServerBinding serverBinding = new SOAPServerBinding(bus, epr, null, null);
        assertNotNull(serverBinding.getBinding());
    }

    /*
    public void testCreateObjectContext() throws Exception {
        SOAPServerBinding serverBinding = new SOAPServerBinding(bus, epr, null, null);
        byte[] bArray = new byte[0];
        TestInputStreamContext inCtx = new TestInputStreamContext(bArray);
        assertNotNull(serverBinding.createBindingMessageContext(inCtx));
    }
    */

    /*
    public void testCreateTransport() throws Exception {
        TestServerBinding serverBinding = new TestServerBinding(bus, epr, null, null);
        assertNotNull(serverBinding.getTransport(epr));
    }
    */

    public void testProviderDispatchMessageModeSourceData() throws Exception {
        HelloWorldServiceProvider provider = new HelloWorldServiceProvider();
        TestEndpointImpl testEndpoint = new TestEndpointImpl(
                                                  provider,
                                                  DataBindingCallback.Mode.MESSAGE,
                                                  DOMSource.class);
        TestServerBinding serverBinding = new TestServerBinding(bus, epr, testEndpoint, testEndpoint); 
        TestServerTransport serverTransport = new TestServerTransport(bus, epr);        
        TestInputStreamContext inCtx = new TestInputStreamContext(null);
        
        InputStream is = getClass().getResourceAsStream("resources/sayHiDocLiteralReq.xml");
        inCtx.setInputStream(is);

        serverBinding.testDispatch(inCtx, serverTransport);

        assertEquals(1, provider.getInvokeCount());
        assertNotNull(serverTransport.getOutputStreamContext());
        assertFalse(serverTransport.getOutputStreamContext().isFault());
        OutputStream os = serverTransport.getOutputStreamContext().getOutputStream();
        assertNotNull(os);
    }

    public void testProviderDispatchMessageModeSOAPMessageData() throws Exception {
        HWSoapMessageProvider provider = new HWSoapMessageProvider();
        TestEndpointImpl testEndpoint = new TestEndpointImpl(
                                                  provider,
                                                  DataBindingCallback.Mode.MESSAGE,
                                                  SOAPMessage.class);
        TestServerBinding serverBinding = new TestServerBinding(bus, epr, testEndpoint, testEndpoint); 
        TestServerTransport serverTransport = new TestServerTransport(bus, epr);        
        TestInputStreamContext inCtx = new TestInputStreamContext(null);
        
        InputStream is = getClass().getResourceAsStream("resources/sayHiDocLiteralReq.xml");
        inCtx.setInputStream(is);

        serverBinding.testDispatch(inCtx, serverTransport);

        assertEquals(1, provider.getInvokeCount());
        assertNotNull(serverTransport.getOutputStreamContext());
        assertFalse(serverTransport.getOutputStreamContext().isFault());
        OutputStream os = serverTransport.getOutputStreamContext().getOutputStream();
        assertNotNull(os);
    }

    public void testProviderDispatchPayloadModeSourceData() throws Exception {
        HWSourcePayloadProvider provider = new HWSourcePayloadProvider();
        TestEndpointImpl testEndpoint = new TestEndpointImpl(
                                                  provider,
                                                  DataBindingCallback.Mode.PAYLOAD,
                                                  DOMSource.class);
        TestServerBinding serverBinding = new TestServerBinding(bus, epr, testEndpoint, testEndpoint); 
        TestServerTransport serverTransport = new TestServerTransport(bus, epr);        
        TestInputStreamContext inCtx = new TestInputStreamContext(null);
        
        InputStream is = getClass().getResourceAsStream("resources/sayHiDocLiteralReq.xml");
        inCtx.setInputStream(is);

        serverBinding.testDispatch(inCtx, serverTransport);

        assertEquals(1, provider.getInvokeCount());
        assertNotNull(serverTransport.getOutputStreamContext());
        assertFalse(serverTransport.getOutputStreamContext().isFault());
        OutputStream os = serverTransport.getOutputStreamContext().getOutputStream();
        assertNotNull(os);
    }
    
    public void testDocLitDispatch() throws Exception {
        TestEndpointImpl testEndpoint = new TestEndpointImpl(new NotAnnotatedGreeterImpl());
        TestServerBinding serverBinding = new TestServerBinding(bus, epr, testEndpoint, testEndpoint); 

        TestServerTransport serverTransport = new TestServerTransport(bus, epr);
        
        QName wrapName = new QName("http://objectweb.org/hello_world_soap_http/types", "greetMe");
        QName elName = new QName("http://objectweb.org/hello_world_soap_http/types", "requestType");
        String data = new String("TestSOAPInputMessage");

        byte[] bArray = SOAPMessageUtil.createWrapDocLitSOAPMessage(wrapName, elName, data).getBytes();
        TestInputStreamContext inCtx = new TestInputStreamContext(bArray);

        serverBinding.testDispatch(inCtx, serverTransport);

        assertNotNull(serverTransport.getOutputStreamContext());
        assertFalse(serverTransport.getOutputStreamContext().isFault());
        OutputStream os = serverTransport.getOutputStreamContext().getOutputStream();
        assertNotNull(os);

        wrapName = new QName("http://objectweb.org/hello_world_soap_http/types", "greetMeResponse");
        elName = new QName("http://objectweb.org/hello_world_soap_http/types", "responseType");
       
        String ref = SOAPMessageUtil.createWrapDocLitSOAPMessage(wrapName, elName, data);
       
        assertEquals(ref, os.toString());
        
        //Doc Literal Case
        InputStream is = getClass().getResourceAsStream("resources/sayHiDocLiteralReq.xml");
        inCtx.setInputStream(is);
        serverBinding.testDispatch(inCtx, serverTransport);
        assertNotNull(serverTransport.getOutputStreamContext());
        assertFalse(serverTransport.getOutputStreamContext().isFault());
        is.close();
    }

    public void testRPCLitDispatch() throws Exception {
        TestEndpointImpl testEndpoint = new TestEndpointImpl(new NotAnnotatedGreeterImplRPCLit());
        TestServerBinding serverBinding = new TestServerBinding(bus, epr, testEndpoint, testEndpoint); 
        TestServerTransport serverTransport = new TestServerTransport(bus, epr);
        TestInputStreamContext inCtx = new TestInputStreamContext(null);
        
        InputStream is = getClass().getResourceAsStream("resources/sayHiRpcLiteralReq.xml");
        inCtx.setInputStream(is);
        serverBinding.testDispatch(inCtx, serverTransport);
        is.close();
        
        TestOutputStreamContext osc = 
            (TestOutputStreamContext) serverTransport.getOutputStreamContext();        
        assertNotNull(osc);
        assertFalse(osc.isFault());
        
        ByteArrayInputStream bais = new ByteArrayInputStream(osc.getOutputStreamBytes());        
        SOAPMessage msg = MessageFactory.newInstance().createMessage(null,  bais);
        assertNotNull(msg);
        assertFalse(msg.getSOAPBody().hasFault());        
        Node xmlNode = msg.getSOAPBody();
        assertNotNull(xmlNode);
        assertEquals(1, xmlNode.getChildNodes().getLength());
        //Check if the Response Node is "sayHiResponse"
        xmlNode = xmlNode.getFirstChild();
        assertEquals("sayHiResponse", xmlNode.getLocalName());
    }
    
    public void testDispatchOneway() throws Exception {
        TestEndpointImpl testEndpoint = new TestEndpointImpl(new NotAnnotatedGreeterImpl());
        TestServerBinding serverBinding = new TestServerBinding(bus, epr, testEndpoint, testEndpoint);        
        TestServerTransport serverTransport = new TestServerTransport(bus, epr);

        QName wrapName = new QName("http://objectweb.org/hello_world_soap_http/types", "greetMeOneWay");
        QName elName = new QName("http://objectweb.org/hello_world_soap_http/types", "requestType");
        String data = new String("TestSOAPInputMessage");
       
        byte[] bArray = SOAPMessageUtil.createWrapDocLitSOAPMessage(wrapName, elName, data).getBytes();
        
        TestInputStreamContext inCtx = new TestInputStreamContext(bArray);
        serverBinding.testDispatch(inCtx, serverTransport);

        assertNotNull(serverTransport.getOutputStreamContext());  
        OutputStream os = serverTransport.getOutputStreamContext().getOutputStream();        
        assertNotNull(os);
        
        assertEquals("", os.toString());

    }

    public void testUserFaultDispatch() throws Exception {
        TestEndpointImpl testEndpoint = new TestEndpointImpl(new NotAnnotatedGreeterImpl());
        TestServerBinding serverBinding = new TestServerBinding(bus, epr, testEndpoint, testEndpoint);        
        TestServerTransport serverTransport = new TestServerTransport(bus, epr);
        InputStream is =  getClass().getResourceAsStream("resources/TestDocLitFaultReq.xml");
        
        TestInputStreamContext inCtx = new TestInputStreamContext(null);
        inCtx.setInputStream(is);
        serverBinding.testDispatch(inCtx, serverTransport);

        assertNotNull(serverTransport.getOutputStreamContext());
        assertTrue(serverTransport.getOutputStreamContext().isFault());

        TestOutputStreamContext osc = (TestOutputStreamContext) serverTransport.getOutputStreamContext();
        ByteArrayInputStream bais = new ByteArrayInputStream(osc.getOutputStreamBytes());
        checkUserFaultMessage(bais, NoSuchCodeLitFault.class, "TestException");
    }

    public void testSystemFaultDispatch() throws Exception {
        TestEndpointImpl testEndpoint = new TestEndpointImpl(new NotAnnotatedGreeterImpl());
        TestServerBinding serverBinding = new TestServerBinding(bus, epr, testEndpoint, testEndpoint);        
        TestServerTransport serverTransport = new TestServerTransport(bus, epr);
        InputStream is =  getClass().getResourceAsStream("resources/BadSoapMessage.xml");
        
        TestInputStreamContext inCtx = new TestInputStreamContext(null);
        inCtx.setInputStream(is);
        serverBinding.testDispatch(inCtx, serverTransport);

        assertNotNull(serverTransport.getOutputStreamContext());
        assertTrue(serverTransport.getOutputStreamContext().isFault());

        TestOutputStreamContext osc = (TestOutputStreamContext) serverTransport.getOutputStreamContext();
        ByteArrayInputStream bais = new ByteArrayInputStream(osc.getOutputStreamBytes());
        checkSystemFaultMessage(bais);
    }

    private void checkSystemFaultMessage(ByteArrayInputStream bais) throws Exception {
        SOAPMessage msg = MessageFactory.newInstance().createMessage(null,  bais);
        assertNotNull(msg);
        Node xmlNode = msg.getSOAPBody();
        assertNotNull(xmlNode);
        assertEquals(1, xmlNode.getChildNodes().getLength());
        
        assertTrue(msg.getSOAPBody().hasFault());
        
        SOAPFault fault = msg.getSOAPBody().getFault();
        assertNotNull(fault);
        assertTrue(fault.hasChildNodes());
        
        //For Celtix Runtime Exceptions - SOAPFault will not have a Detail Node
        Detail detail = fault.getDetail();
        assertNull(detail);
    }
    
    private void checkUserFaultMessage(ByteArrayInputStream bais, 
                              Class<? extends Exception> clazz,
                              String faultString) throws Exception {
        
        SOAPMessage msg = MessageFactory.newInstance().createMessage(null,  bais);
        assertNotNull(msg);
        assertTrue(msg.getSOAPBody().hasFault());
        SOAPFault fault = msg.getSOAPBody().getFault();
        assertNotNull(fault);
        
        StringBuffer str = new StringBuffer(clazz.getName());
        str.append(": ");
        str.append(faultString);
        assertEquals(str.toString(), fault.getFaultString());
        assertTrue(fault.hasChildNodes());
        Detail detail = fault.getDetail();
        assertNotNull(detail);
        
        NodeList list = detail.getChildNodes();
        assertEquals(1, list.getLength()); 
        
        WebFault wfAnnotation = clazz.getAnnotation(WebFault.class);
        assertNotNull(wfAnnotation);
        assertEquals(wfAnnotation.targetNamespace(), list.item(0).getNamespaceURI());
        assertEquals(wfAnnotation.name(), list.item(0).getLocalName());
    }
    
    class TestServerBinding extends SOAPServerBinding {

        public TestServerBinding(Bus b, EndpointReferenceType ref, Endpoint ep,
                                 ServerBindingEndpointCallback cbFactory) {
            super(b, ref, ep, cbFactory);
        }

        public ServerTransport getTransport(EndpointReferenceType ref) throws Exception {            
            return createTransport(ref);
        }

        public void testDispatch(InputStreamMessageContext inCtx, ServerTransport t) {
            super.dispatch(inCtx, t);
        }
    }

    class TestServerTransport implements ServerTransport {
        private OutputStreamMessageContext osmc;
        public TestServerTransport(Bus b, EndpointReferenceType ref) {
        }

        public void shutdown() {
            //nothing to do
        }

        public void rebase(MessageContext context, EndpointReferenceType decoupledResponseEndpoint)
            throws IOException {
        }

        public OutputStreamMessageContext createOutputStreamContext(MessageContext context) 
            throws IOException {
            osmc = new TestOutputStreamContext(null, context);
            return osmc;
        }

        public void finalPrepareOutputStreamContext(OutputStreamMessageContext context) throws IOException {
        }
        
        public void postDispatch(MessageContext bindingContext, 
                                           OutputStreamMessageContext context) throws IOException {
        }
        
        public void activate(ServerTransportCallback callback) throws IOException { }

        public void deactivate() throws IOException { }   
        
        public OutputStreamMessageContext getOutputStreamContext() {
            return osmc;
        }
    }
    
    class TestEndpointImpl extends javax.xml.ws.Endpoint implements ServerBindingEndpointCallback {

        private final Object implementor;
        private WebServiceProvider wsProvider;
        private DataBindingCallback.Mode mode;
        private Class<?> dataClazz;
        
        TestEndpointImpl(Object impl) {
            implementor = impl;
            mode = DataBindingCallback.Mode.PARTS;
        }

        TestEndpointImpl(Object impl, 
                         DataBindingCallback.Mode dataMode,
                         Class<?> clazz) {
            implementor = impl;
            mode = dataMode;
            dataClazz = clazz;
        }
        
        public Binding getBinding() {
            return null;
        }

        public List<Handler> getHandlerChain() {
            return null;
        }

        public Object getImplementor() {
            return implementor;
        }

        public List<Source> getMetadata() {
            return null;
        }

        public Executor getExecutor() {
            return null;
        }

        public boolean isPublished() {
            return false;
        }

        public void publish(Object serverContext) { }

        public void publish(String address) { }

        public void setHandlerChain(List<Handler> h) {            
        }

        public void setMetadata(List<Source> m) {         
        }

        public void setExecutor(Executor ex) {            
        }

        public void stop() { }

        @Override
        public Map<String, Object> getProperties() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public void setProperties(Map<String, Object> arg0) {
            // TODO Auto-generated method stub
            
        }

        public DataBindingCallback createDataBindingCallback(ObjectMessageContext objContext,
                                                             DataBindingCallback.Mode dataMode) {
            if (dataMode == DataBindingCallback.Mode.PARTS) {
                return new JAXBDataBindingCallback(objContext.getMethod(),
                                               mode,
                                               null);
            }

            return new DynamicDataBindingCallback(dataClazz, mode);
        }
        
        public Method getMethod(Endpoint endpoint, QName operationName) {
            if (wsProvider != null) {
                try {
                    return implementor.getClass().getDeclaredMethod("invoke", dataClazz);
                } catch (Exception ex) {
                    //Ignore
                }
            }
            return EndpointUtils.getMethod(endpoint, operationName);
        }

        public DataBindingCallback.Mode getServiceMode() {
            return mode;
        } 

        public WebServiceProvider getWebServiceProvider() {
            if (wsProvider == null) {
                wsProvider = this.getImplementor().getClass().getAnnotation(WebServiceProvider.class);
            }
            return wsProvider;
        } 
        
    }
    
}
