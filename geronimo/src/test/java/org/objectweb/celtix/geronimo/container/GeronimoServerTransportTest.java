package org.objectweb.celtix.geronimo.container;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;

import javax.xml.namespace.QName;
import javax.xml.ws.Holder;
import javax.xml.ws.handler.MessageContext;

import junit.framework.TestCase;

import org.apache.geronimo.webservices.WebServiceContainer.Request;
import org.apache.geronimo.webservices.WebServiceContainer.Response;
import org.easymock.classextension.EasyMock;
import org.objectweb.celtix.Bus;
import org.objectweb.celtix.configuration.Configuration;
import org.objectweb.celtix.context.InputStreamMessageContext;
import org.objectweb.celtix.context.OutputStreamMessageContext;
import org.objectweb.celtix.geronimo.MockBusFactory;
import org.objectweb.celtix.transports.ServerTransport;
import org.objectweb.celtix.transports.ServerTransportCallback;
import org.objectweb.celtix.ws.addressing.AttributedURIType;
import org.objectweb.celtix.ws.addressing.EndpointReferenceType;
import org.objectweb.celtix.wsdl.EndpointReferenceUtils;

public class GeronimoServerTransportTest extends TestCase {

    private Bus mockBus; 
    private EndpointReferenceType address; 
    private Holder<InputStreamMessageContext> ctxHolder = new Holder<InputStreamMessageContext>();
    private Holder<ServerTransport> transportHolder = new Holder<ServerTransport>();
    private Holder<Boolean> invokedHolder = new Holder<Boolean>();
    private MockBusFactory busFactory = new MockBusFactory();
    private GeronimoServerTransport transport;
    
    
    public void setUp() throws Exception {
        
        mockBus = busFactory.createMockBus();
        
        address = new EndpointReferenceType();
        AttributedURIType uri = new AttributedURIType();
        uri.setValue("http://foo.bar.iona.com/wibbly/wobbly/wonder");
        address.setAddress(uri);
        QName serviceName = new QName("http://www.w3.org/2004/08/wsdl", "testServiceName");
        EndpointReferenceUtils.setServiceAndPortName(address, serviceName, "");

        Configuration child = 
            busFactory.addChildConfig("http://celtix.objectweb.org/bus/jaxws/endpoint-config", null, null);
        Configuration httpServerCfg = 
            busFactory.addChildConfig("http://celtix.objectweb.org/bus/transports/http/http-server-config",
                null, null, child);
        EasyMock.replay(child);
        EasyMock.replay(httpServerCfg);
        busFactory.replay(); 
        
        transport = new GeronimoServerTransport(mockBus, address);
        
    }
    
    public void testTransportIsSerializable() {
        
        assertTrue("transport must be serializable",
                   Serializable.class.isAssignableFrom(GeronimoServerTransport.class));
    }
    
    
    public void testCopyHeaders() { 

  
        DummyRequest req = new DummyRequest(); 
        req.setHeader("Accept", "text/plain, text/html"); 
        req.setHeader("Host", "localhost"); 
        req.setHeader("Content-Type", "text/html");
        
        GeronimoInputStreamMessageContext ctx = transport.createInputStreamMessageContext();
        ctx.setRequest(req);
        
        Map<String, List<String>> headerMap = new HashMap<String, List<String>>();
        assertTrue(ctx instanceof MessageContext);
        transport.copyRequestHeaders(ctx, headerMap);
                
        assertEquals("unexpected number of headers", 3, headerMap.size());
        checkHeader(headerMap, "Host", "localhost");
        checkHeader(headerMap, "Content-Type", "text/html");
        checkHeader(headerMap, "Accept", "text/plain", "text/html");
    }
    
    
    private void checkHeader(Map<String, List<String>> headers, String name, String... expectedValues) {
        
        List<String> values = headers.get(name);
        
        int valueCount = 0;

        for (String expectedValue : expectedValues) {
            assertTrue("unexpected header value: [" + expectedValue + "] " + values, 
                       values.contains(expectedValue));
            ++valueCount;
        }
        assertEquals("unexpected number of header values", valueCount, values.size());
    }
    
    public void testInvoke() throws Exception {
        
        final Request request = EasyMock.createMock(Request.class);
        final Response response = EasyMock.createMock(Response.class);
        

        transport.activate(new ServerTransportCallback() {

            public void dispatch(InputStreamMessageContext ctx, ServerTransport tport) {
                invokedHolder.value = Boolean.TRUE;
                ctxHolder.value = ctx; 
                transportHolder.value = tport;
            }

            public Executor getExecutor() {
                return null;
            }
            
        });
        
        transport.invoke(request, response);
        
        assertTrue("ServerTransportCallback.dispatch was not called", invokedHolder.value);
        assertSame("transport not passed to ServerTransportCallback", transport, transportHolder.value);
        
        assertNotNull("no context passed from transport", ctxHolder.value);
        InputStreamMessageContext ctx = ctxHolder.value;
        assertTrue("context is of incorrect type: " + ctx.getClass(), 
                   ctx instanceof GeronimoInputStreamMessageContext);
        GeronimoInputStreamMessageContext inctx = (GeronimoInputStreamMessageContext)ctx;
        assertNotNull("request not available in context", inctx.getRequest());
        
        assertNotNull("response not available in context", inctx.getResponse());
    }

    public void testCreateOutputStreamContext() throws IOException {

        MessageContext mc = EasyMock.createMock(MessageContext.class);
        Response resp = EasyMock.createNiceMock(Response.class);
        resp.setStatusCode(200);
        EasyMock.expect(resp.getOutputStream()).andReturn(EasyMock.createNiceMock(OutputStream.class)); 
        
        EasyMock.expect(mc.get(GeronimoInputStreamMessageContext.RESPONSE))
                .andReturn(resp);
        
        EasyMock.expect(mc.get("foo")).andReturn("ret");
        EasyMock.replay(mc);
        EasyMock.replay(resp);
        
        OutputStreamMessageContext outctx = transport.createOutputStreamContext(mc);
        
        assertNotNull("received null context from the transoprt", outctx);
        assertTrue("incorrect type for context" + outctx.getClass(), 
                   outctx instanceof GeronimoOutputStreamServerMessageContext);

        assertNotNull("no outputstream in context", outctx.getOutputStream());
        
        // check that context passed in has been wrapped.
        
        outctx.get("foo");
        EasyMock.verify(mc);
        
    }
    
    
    static class DummyRequest implements Request {

        private Map<String, String> headers = new HashMap<String, String>();

        public void setHeader(String header, String value) {
            headers.put(header, value);
        }
        public String getHeader(String header) {
            return headers.get(header);
        }

        public URI getURI() {
            // TODO Auto-generated method stub
            return null;
        }

        public int getContentLength() {
            // TODO Auto-generated method stub
            return 0;
        }

        public String getContentType() {
            // TODO Auto-generated method stub
            return null;
        }

        public InputStream getInputStream() throws IOException {
            // TODO Auto-generated method stub
            return null;
        }

        public int getMethod() {
            // TODO Auto-generated method stub
            return 0;
        }

        public String getParameter(String arg0) {
            // TODO Auto-generated method stub
            return null;
        }

        public Map getParameters() {
            // TODO Auto-generated method stub
            return null;
        }

        public Object getAttribute(String arg0) {
            // TODO Auto-generated method stub
            return null;
        }

        public void setAttribute(String arg0, Object arg1) {
            // TODO Auto-generated method stub
            
        }
        
    }
    
}
