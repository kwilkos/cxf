package org.objectweb.celtix.bindings.soap2.binding;

import java.io.*;
import java.util.*;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.ws.handler.MessageContext;
import org.objectweb.celtix.bindings.soap2.TestBase;
import org.objectweb.celtix.context.ObjectMessageContextImpl;
import org.objectweb.celtix.staxutils.DepthXMLStreamReader;
import org.objectweb.celtix.staxutils.StaxUtils;
import org.objectweb.hello_world_soap_http.Greeter;

public class WrapperOutInterceptorTest extends TestBase {

    private ByteArrayOutputStream baos;
    private ObjectMessageContextImpl objCtx;
    
    public void setUp() throws Exception {
        super.setUp();
        
        WrapperOutInterceptor interceptor = new WrapperOutInterceptor();
        interceptor.setPhase("phase1");
        chain.add(interceptor);

        baos =  new ByteArrayOutputStream();

//         ServiceInfo service = getTestService(Greeter.class);
//         BindingInfo binfo = service.getPort("GreeterPort").getBinding();
        
//         soapMessage.put("service.model", service);
//         soapMessage.put("service.model.binding", binfo);
        soapMessage.setResult(XMLStreamWriter.class, getXMLStreamWriter(baos));
        soapMessage.put(MessageContext.WSDL_OPERATION, "greetMe");
        soapMessage.put("JAXB_CALLBACK", getTestCallback(Greeter.class, "greetMe"));

        String val = new String("TESTOUTPUTMESSAGE");
        objCtx = new ObjectMessageContextImpl();
        objCtx.setMessageObjects(new Object[] {val});
        objCtx.setReturn(val);
        soapMessage.put("OBJECTCONTEXT", objCtx);
    }

    public void tearDown() throws Exception {
        baos.close();
    }

    public void testWriteOutbound() throws Exception {
        soapMessage.getInterceptorChain().doIntercept(soapMessage);

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        XMLStreamReader xr = StaxUtils.createXMLStreamReader(bais);
        DepthXMLStreamReader reader = new DepthXMLStreamReader(xr);
        StaxUtils.toNextElement(reader);
        assertEquals(new QName("http://objectweb.org/hello_world_soap_http/types", "greetMe"),
                     reader.getName());
        
        StaxUtils.nextEvent(reader);
        StaxUtils.toNextElement(reader);
        assertEquals(new QName("http://objectweb.org/hello_world_soap_http/types", "requestType"),
                     reader.getName());

        StaxUtils.nextEvent(reader);
        StaxUtils.toNextText(reader);
        assertEquals("TESTOUTPUTMESSAGE", reader.getText());
    }

    public void testWriteInbound() throws Exception {
        soapMessage.put("message.inbound", "message.inbound");
        
        soapMessage.getInterceptorChain().doIntercept(soapMessage);

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        XMLStreamReader xr = StaxUtils.createXMLStreamReader(bais);
        DepthXMLStreamReader reader = new DepthXMLStreamReader(xr);
        StaxUtils.toNextElement(reader);
        assertEquals(new QName("http://objectweb.org/hello_world_soap_http/types", "greetMeResponse"),
                     reader.getName());
        
        StaxUtils.nextEvent(reader);
        StaxUtils.toNextElement(reader);
        assertEquals(new QName("http://objectweb.org/hello_world_soap_http/types", "responseType"),
                     reader.getName());

        StaxUtils.nextEvent(reader);
        StaxUtils.toNextText(reader);
        assertEquals("TESTOUTPUTMESSAGE", reader.getText());        
    }
}
