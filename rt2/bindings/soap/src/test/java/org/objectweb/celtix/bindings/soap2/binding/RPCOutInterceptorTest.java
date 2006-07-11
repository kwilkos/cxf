package org.objectweb.celtix.bindings.soap2.binding;

import java.io.*;
import java.util.*;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.ws.handler.MessageContext;
import org.objectweb.celtix.bindings.soap2.TestBase;
import org.objectweb.celtix.jaxb.utils.DepthXMLStreamReader;
import org.objectweb.celtix.jaxb.utils.StaxUtils;
import org.objectweb.hello_world_rpclit.GreeterRPCLit;
import org.objectweb.hello_world_rpclit.types.MyComplexStruct;


public class RPCOutInterceptorTest extends TestBase {

    private ByteArrayOutputStream baos;
    
    public void setUp() throws Exception {
        super.setUp();
        
        RPCOutInterceptor interceptor = new RPCOutInterceptor();
        interceptor.setPhase("phase1");
        chain.add(interceptor);

        baos =  new ByteArrayOutputStream();

        soapMessage.put("service.model", getTestService(GreeterRPCLit.class));
        soapMessage.setResult(XMLStreamWriter.class, getXMLStreamWriter(baos));
        soapMessage.put(MessageContext.WSDL_OPERATION, "sendReceiveData");
        soapMessage.put("JAXB_CALLBACK", getTestCallback(GreeterRPCLit.class, "sendReceiveData"));
    }

    public void tearDown() throws Exception {
        baos.close();
    }

    public void testWriteOutbound() throws Exception {
        soapMessage.put("PARAMETERS", Arrays.asList(getTestObject()));

        soapMessage.getInterceptorChain().doIntercept(soapMessage);

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        XMLStreamReader xr = StaxUtils.createXMLStreamReader(bais);
        DepthXMLStreamReader reader = new DepthXMLStreamReader(xr);
        StaxUtils.toNextElement(reader);
        assertEquals(new QName("http://objectweb.org/hello_world_rpclit", "sendReceiveData"),
                     reader.getName());
        
        StaxUtils.nextEvent(reader);
        StaxUtils.toNextElement(reader);
        assertEquals(new QName("", "in"), reader.getName());

        StaxUtils.toNextText(reader);
        assertEquals("this is elem1", reader.getText());
    }

    public void testWriteInbound() throws Exception {
        soapMessage.put("RETURN", getTestObject());
        soapMessage.put("message.inbound", "message.inbound");
        
        soapMessage.getInterceptorChain().doIntercept(soapMessage);

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        XMLStreamReader xr = StaxUtils.createXMLStreamReader(bais);
        DepthXMLStreamReader reader = new DepthXMLStreamReader(xr);
        StaxUtils.toNextElement(reader);
        assertEquals(new QName("http://objectweb.org/hello_world_rpclit", "sendReceiveDataResponse"),
                     reader.getName());
        
        StaxUtils.nextEvent(reader);
        StaxUtils.toNextElement(reader);
        assertEquals(new QName("http://objectweb.org/hello_world_rpclit", "out"), reader.getName());

        StaxUtils.nextEvent(reader);
        StaxUtils.toNextElement(reader);

        assertEquals(new QName("http://objectweb.org/hello_world_rpclit/types", "elem1"), reader.getName());

        StaxUtils.nextEvent(reader);
        StaxUtils.toNextText(reader);
        assertEquals("this is elem1", reader.getText());
    }

    public MyComplexStruct getTestObject() {
        MyComplexStruct obj = new MyComplexStruct();
        obj.setElem1("this is elem1");
        obj.setElem2("this is elem2");
        obj.setElem3(1982);
        return obj;
    }
}
