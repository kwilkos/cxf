package org.objectweb.celtix.bindings.soap2;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Arrays;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.objectweb.celtix.message.Message;
import org.objectweb.celtix.service.model.BindingInfo;
import org.objectweb.celtix.service.model.BindingOperationInfo;
import org.objectweb.celtix.service.model.OperationInfo;
import org.objectweb.celtix.staxutils.DepthXMLStreamReader;
import org.objectweb.celtix.staxutils.StaxUtils;
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

        soapMessage.getExchange().put(SoapMessage.DATAWRITER_FACTORY_KEY, "test.writer.factory");
        soapMessage.put(SoapMessage.BINDING_INFO,
                        getTestService("resources/wsdl/hello_world_rpc_lit.wsdl", "SoapPortRPCLit"));

        soapMessage.setContent(XMLStreamWriter.class, getXMLStreamWriter(baos));
        soapMessage.put(Message.INVOCATION_OPERATION, "sendReceiveData");
    }

    public void tearDown() throws Exception {
        baos.close();
    }

    public void testWriteOutbound() throws Exception {
        soapMessage.put(Message.INVOCATION_OBJECTS, Arrays.asList(getTestObject()));

        soapMessage.getInterceptorChain().doIntercept(soapMessage);

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        XMLStreamReader xr = StaxUtils.createXMLStreamReader(bais);
        DepthXMLStreamReader reader = new DepthXMLStreamReader(xr);
        StaxUtils.toNextElement(reader);
        assertEquals(new QName("http://objectweb.org/hello_world_rpclit", "sendReceiveData"),
                     reader.getName());
        
        StaxUtils.nextEvent(reader);
        StaxUtils.toNextElement(reader);
        assertEquals(new QName("http://objectweb.org/hello_world_rpclit/types", "in"),
                     reader.getName());

        StaxUtils.toNextText(reader);
        assertEquals("this is elem1", reader.getText());
    }

    public void testWriteInbound() throws Exception {
        soapMessage.put(Message.INVOCATION_OBJECTS, Arrays.asList(getTestObject()));
        soapMessage.put(Message.INBOUND_MESSAGE, Message.INBOUND_MESSAGE);
        
        soapMessage.getInterceptorChain().doIntercept(soapMessage);

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        XMLStreamReader xr = StaxUtils.createXMLStreamReader(bais);
        DepthXMLStreamReader reader = new DepthXMLStreamReader(xr);
        StaxUtils.toNextElement(reader);
        assertEquals(new QName("http://objectweb.org/hello_world_rpclit", "sendReceiveDataResponse"),
                     reader.getName());
        
        StaxUtils.nextEvent(reader);
        StaxUtils.toNextElement(reader);
        assertEquals(new QName("http://objectweb.org/hello_world_rpclit/types", "out"), reader.getName());

        StaxUtils.nextEvent(reader);
        StaxUtils.toNextElement(reader);

        assertEquals(new QName("http://objectweb.org/hello_world_rpclit/types", "elem1"), reader.getName());

        StaxUtils.nextEvent(reader);
        StaxUtils.toNextText(reader);
        assertEquals("this is elem1", reader.getText());
    }


        

    public BindingInfo getTestService(String wsdlUrl, String port) throws Exception {
        BindingInfo binding = super.getTestService(wsdlUrl, port);
        BindingOperationInfo op = binding.getOperation(new QName("http://objectweb.org/hello_world_rpclit",
                                                                 "sendReceiveData"));
        OperationInfo oi = op.getOperationInfo();
        oi.setProperty("test.writer.factory", getTestWriterFactory(GreeterRPCLit.class));
        return binding;
    }

    public MyComplexStruct getTestObject() {
        MyComplexStruct obj = new MyComplexStruct();
        obj.setElem1("this is elem1");
        obj.setElem2("this is elem2");
        obj.setElem3(1982);
        return obj;
    }
}
