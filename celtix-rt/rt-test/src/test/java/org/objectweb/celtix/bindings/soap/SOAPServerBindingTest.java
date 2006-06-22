package org.objectweb.celtix.bindings.soap;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;

import javax.jws.WebMethod;
import javax.jws.soap.SOAPBinding.Style;
import javax.xml.namespace.QName;

import javax.xml.soap.Detail;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPFault;
import javax.xml.soap.SOAPMessage;

import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;
import javax.xml.ws.Binding;
import javax.xml.ws.Endpoint;
import javax.xml.ws.Provider;
import javax.xml.ws.WebFault;
import javax.xml.ws.WebServiceProvider;
import javax.xml.ws.handler.Handler;
import javax.xml.ws.handler.MessageContext;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import junit.framework.TestCase;

import org.objectweb.celtix.Bus;
import org.objectweb.celtix.bindings.DataBindingCallback;
import org.objectweb.celtix.bindings.DataBindingCallback.Mode;
import org.objectweb.celtix.bindings.ServerBindingEndpointCallback;
import org.objectweb.celtix.bindings.ServerDataBindingCallback;
import org.objectweb.celtix.bindings.TestInputStreamContext;
import org.objectweb.celtix.bindings.TestOutputStreamContext;
import org.objectweb.celtix.bus.jaxws.EndpointUtils;
import org.objectweb.celtix.context.InputStreamMessageContext;
import org.objectweb.celtix.context.ObjectMessageContext;
import org.objectweb.celtix.context.OutputStreamMessageContext;
import org.objectweb.celtix.jaxb.JAXBDataBindingCallback;
import org.objectweb.celtix.jaxb.ServerDynamicDataBindingCallback;
import org.objectweb.celtix.transports.ServerTransport;
import org.objectweb.celtix.transports.ServerTransportCallback;
import org.objectweb.celtix.ws.addressing.EndpointReferenceType;
import org.objectweb.celtix.wsdl.EndpointReferenceUtils;

import org.objectweb.hello_world_soap_http.DocLitBareImpl;
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
        SOAPServerBinding serverBinding = new SOAPServerBinding(bus, epr, null);
        assertNotNull(serverBinding.getBinding());
    }

    public void testProviderDispatchMessageModeSourceData() throws Exception {
        HelloWorldServiceProvider provider = new HelloWorldServiceProvider();
        TestEndpointImpl testEndpoint
            = new TestEndpointImpl(
                                  provider,
                                  DataBindingCallback.Mode.MESSAGE,
                                  DOMSource.class,
                                  new QName[]{
                                      new QName("http://objectweb.org/hello_world_soap_http/types",
                                                "sayHi")
                                  });
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
                                  SOAPMessage.class,
                                  new QName[]{
                                      new QName("http://objectweb.org/hello_world_soap_http/types",
                                                "sayHi")
                                  });
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
                          DOMSource.class,
                          new QName[]{
                              new QName("http://objectweb.org/hello_world_soap_http/types",
                                        "sayHi")
                          });
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
        QName wrapName = new QName("http://objectweb.org/hello_world_soap_http/types", "greetMe");
        QName wrapName2 = new QName("http://objectweb.org/hello_world_soap_http/types", "sayHi");
        
        TestEndpointImpl testEndpoint = new TestEndpointImpl(new NotAnnotatedGreeterImpl(),
                                                             new QName[]{wrapName, wrapName2});
        TestServerBinding serverBinding = new TestServerBinding(bus, epr, testEndpoint, testEndpoint); 

        TestServerTransport serverTransport = new TestServerTransport(bus, epr);
        
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
        inCtx.clear();
        InputStream is = getClass().getResourceAsStream("resources/sayHiDocLiteralReq.xml");
        inCtx.setInputStream(is);
        serverBinding.testDispatch(inCtx, serverTransport);
        assertNotNull(serverTransport.getOutputStreamContext());
        assertFalse(serverTransport.getOutputStreamContext().isFault());
        is.close();
    }

    public void testRPCLitDispatch() throws Exception {
        QName qn = new QName("http://objectweb.org/hello_world_rpclit", "sayHi");
        TestEndpointImpl testEndpoint = new TestEndpointImpl(new NotAnnotatedGreeterImplRPCLit(),
                                                             new QName[]{qn});
        TestServerBinding serverBinding = new TestServerBinding(bus, epr, testEndpoint, testEndpoint); 
        TestServerTransport serverTransport = new TestServerTransport(bus, epr);
        TestInputStreamContext inCtx = new TestInputStreamContext(null);
        
        InputStream is = getClass().getResourceAsStream("resources/sayHiRpcLiteralReq.xml");
        inCtx.setInputStream(is);
        serverBinding.testDispatch(inCtx, serverTransport);
        is.close();
        
        TestOutputStreamContext osc = 
            (TestOutputStreamContext)serverTransport.getOutputStreamContext();        
        assertNotNull(osc);
        assertFalse(osc.isFault());
        
        ByteArrayInputStream bais = new ByteArrayInputStream(osc.getOutputStreamBytes());
        
        //System.out.println(new String(osc.getOutputStreamBytes()));
        
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
        QName wrapName = new QName("http://objectweb.org/hello_world_soap_http/types", "greetMeOneWay");
        TestEndpointImpl testEndpoint = new TestEndpointImpl(new NotAnnotatedGreeterImpl(),
                                                             new QName[]{wrapName});
        TestServerBinding serverBinding = new TestServerBinding(bus, epr, testEndpoint, testEndpoint);        
        TestServerTransport serverTransport = new TestServerTransport(bus, epr);

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

    public void testDocLitBareDispatch() throws Exception {
        DocLitBareImpl dc = new DocLitBareImpl();
        TestEndpointImpl testEndpoint = new TestEndpointImpl(dc, null);
        TestServerBinding serverBinding = new TestServerBinding(bus, epr, testEndpoint, testEndpoint); 
        TestServerTransport serverTransport = new TestServerTransport(bus, epr);
        TestInputStreamContext inCtx = new TestInputStreamContext(null);
        
        InputStream is = getClass().getResourceAsStream("resources/sayHiDocLiteralBareReq.xml");
        inCtx.setInputStream(is);
        serverBinding.testDispatch(inCtx, serverTransport);
        is.close();
        
        assertEquals(1, dc.getSayHiInvocationCount());
        assertNotNull(serverTransport.getOutputStreamContext());
        assertFalse("Should not have a SOAP Fault", serverTransport.getOutputStreamContext().isFault());
    }
    public void testDocLitBareNoParamDispatch() throws Exception {
        DocLitBareImpl dc = new DocLitBareImpl();
        TestEndpointImpl testEndpoint = new TestEndpointImpl(dc, null);
        TestServerBinding serverBinding = new TestServerBinding(bus, epr, testEndpoint, testEndpoint); 
        TestServerTransport serverTransport = new TestServerTransport(bus, epr);
        TestInputStreamContext inCtx = new TestInputStreamContext(null);
        
        InputStream is = getClass().getResourceAsStream("resources/EmptyBody.xml");
        inCtx.setInputStream(is);
        serverBinding.testDispatch(inCtx, serverTransport);
        is.close();
        
        assertEquals(1, dc.getBareNoParamCount());
        assertNotNull(serverTransport.getOutputStreamContext());
        assertFalse("Should not have a SOAP Fault", serverTransport.getOutputStreamContext().isFault());
    }   
    public void testUserFaultDispatch() throws Exception {
        QName qn = new QName("http://objectweb.org/hello_world_soap_http/types", "testDocLitFault");
        TestEndpointImpl testEndpoint = new TestEndpointImpl(new NotAnnotatedGreeterImpl(),
                                                             new QName[]{qn});
        TestServerBinding serverBinding = new TestServerBinding(bus, epr, testEndpoint, testEndpoint);        
        TestServerTransport serverTransport = new TestServerTransport(bus, epr);
        InputStream is =  getClass().getResourceAsStream("resources/TestDocLitFaultReq.xml");
        
        TestInputStreamContext inCtx = new TestInputStreamContext(null);
        inCtx.setInputStream(is);
        serverBinding.testDispatch(inCtx, serverTransport);

        assertNotNull(serverTransport.getOutputStreamContext());
        assertTrue("Expecting a SOAP Fault", serverTransport.getOutputStreamContext().isFault());

        TestOutputStreamContext osc = (TestOutputStreamContext)serverTransport.getOutputStreamContext();
        ByteArrayInputStream bais = new ByteArrayInputStream(osc.getOutputStreamBytes());
        checkUserFaultMessage(bais, NoSuchCodeLitFault.class, "TestException");
    }
    
    public void testSystemFaultDispatch() throws Exception {
        TestEndpointImpl testEndpoint = new TestEndpointImpl(new NotAnnotatedGreeterImpl(),
                                                             new QName[]{});
        TestServerBinding serverBinding = new TestServerBinding(bus, epr, testEndpoint, testEndpoint);        
        TestServerTransport serverTransport = new TestServerTransport(bus, epr);
        InputStream is =  getClass().getResourceAsStream("resources/BadSoapMessage.xml");
        
        TestInputStreamContext inCtx = new TestInputStreamContext(null);
        inCtx.setInputStream(is);
        serverBinding.testDispatch(inCtx, serverTransport);

        assertNotNull(serverTransport.getOutputStreamContext());
        assertTrue("Expecting a SOAP Fault", serverTransport.getOutputStreamContext().isFault());

        TestOutputStreamContext osc = (TestOutputStreamContext)serverTransport.getOutputStreamContext();
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
        if (detail != null) {
            assertFalse("Detail should be non-existent or empty", detail.hasChildNodes());
        }
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

        private QName qn;
        
        public TestServerBinding(Bus b, EndpointReferenceType ref, Endpoint ep,
                                 ServerBindingEndpointCallback cbFactory) {
            super(b, ref, cbFactory);
        }

        public ServerTransport getTransport(EndpointReferenceType ref) throws Exception {            
            return createTransport(ref);
        }

        public void testDispatch(InputStreamMessageContext inCtx, ServerTransport t) {
            super.dispatch(inCtx, t);
        }
        
        public QName getOperationName(MessageContext ctx) {
            qn = super.getOperationName(ctx);
            return qn;
        }
        
        public QName getInvokedMethod() {
            return qn;
        }
    }

    class TestServerTransport implements ServerTransport {
        private OutputStreamMessageContext osmc;
        public TestServerTransport(Bus b, EndpointReferenceType ref) {
        }

        public void shutdown() {
            //nothing to do
        }

        public OutputStreamMessageContext rebase(MessageContext context,
                                                 EndpointReferenceType decoupledResponseEndpoint)
            throws IOException {
            return null;
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
    
    final class TestEndpointImpl extends javax.xml.ws.Endpoint implements ServerBindingEndpointCallback {

        private final Object implementor;
        private WebServiceProvider wsProvider;
        private DataBindingCallback.Mode mode;
        private Class<?> dataClazz;
        private Map<QName, ServerDataBindingCallback> callbackMap
            = new ConcurrentHashMap<QName, ServerDataBindingCallback>();
        
        TestEndpointImpl(Object impl, QName ops[]) {
            implementor = impl;
            mode = DataBindingCallback.Mode.PARTS;
            initOpMap(ops);
        }

        TestEndpointImpl(Object impl, 
                         DataBindingCallback.Mode dataMode,
                         Class<?> clazz,
                         QName ops[]) {
            implementor = impl;
            mode = dataMode;
            dataClazz = clazz;
            initOpMap(ops);
        }
        
        
        private void initOpMap(QName ops[]) {
            if (ops != null) {
                for (QName op : ops) {
                    callbackMap.put(op, getDataBindingCallback(op, mode));                
                }
            } else {
                addMethods(implementor.getClass());
            }
        }     
        private void addMethods(Class<?> cls) {
            if (cls == null) {
                return;
            }
            for (Method meth : cls.getMethods()) {
                WebMethod wm = meth.getAnnotation(WebMethod.class);
                if (wm != null) {
                    QName op = new QName("", wm.operationName());
                    ServerDataBindingCallback cb = getDataBindingCallback(op, mode);
                    callbackMap.put(op, cb);
                }
            }
            for (Class<?> cls2 : cls.getInterfaces()) {
                addMethods(cls2);
            }
            addMethods(cls.getSuperclass());
        }
        
        @SuppressWarnings("unchecked")
        private ServerDataBindingCallback getDataBindingCallback(QName operationName,
                                                                 Mode dataMode) {
            if (dataMode == DataBindingCallback.Mode.PARTS) {
                return new JAXBDataBindingCallback(getMethod(operationName),
                                               mode,
                                               null,
                                               null,
                                               implementor);
            }

            return new ServerDynamicDataBindingCallback(dataClazz, mode, (Provider<?>)implementor);
        }

        public ServerDataBindingCallback getDataBindingCallback(QName operationName,
                                                                ObjectMessageContext objContext,
                                                                Mode dataMode) {
            if (operationName == null) {
                return null;
            }
            return callbackMap.get(operationName);
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

        public Method getMethod(QName operationName) {
            if (wsProvider != null) {
                try {
                    return implementor.getClass().getDeclaredMethod(
                                                     operationName.getLocalPart(), dataClazz);
                } catch (Exception ex) {
                    //Ignore
                }
            }
            return EndpointUtils.getMethod(implementor.getClass(), operationName);
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


        public Map<QName, ? extends DataBindingCallback> getOperations() {
            return callbackMap;
        }

        public Style getStyle() {
            // TODO Auto-generated method stub
            return Style.DOCUMENT;
        }

        public DataBindingCallback getFaultDataBindingCallback(ObjectMessageContext objContext) {
            return new JAXBDataBindingCallback(null, DataBindingCallback.Mode.PARTS, null);
        }

    }
    
}
