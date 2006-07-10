package org.objectweb.celtix.bindings.soap2.binding;

import java.io.*;
import java.lang.reflect.Method;
import java.util.*;

import javax.xml.bind.JAXBContext;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamReader;
import javax.xml.ws.handler.MessageContext;

import org.objectweb.celtix.bindings.DataBindingCallback.Mode;
import org.objectweb.celtix.bindings.soap2.TestUtil;
import org.objectweb.celtix.jaxb.JAXBDataBindingCallback;
import org.objectweb.celtix.jaxb.JAXBEncoderDecoder;
import org.objectweb.celtix.jaxb.utils.DepthXMLStreamReader;
import org.objectweb.celtix.jaxb.utils.StaxStreamFilter;
import org.objectweb.celtix.jaxb.utils.StaxUtils;
import org.objectweb.celtix.rio.soap.Soap11;
import org.objectweb.celtix.rio.soap.SoapMessage;
import org.objectweb.celtix.servicemodel.JAXWSClassServiceBuilder;
import org.objectweb.celtix.servicemodel.MessageInfo;
import org.objectweb.celtix.servicemodel.MessagePartInfo;
import org.objectweb.celtix.servicemodel.OperationInfo;
import org.objectweb.celtix.servicemodel.Service;

import org.objectweb.hello_world_rpclit.GreeterRPCLit;
import org.objectweb.hello_world_rpclit.types.MyComplexStruct;

import static org.objectweb.celtix.datamodel.soap.SOAPConstants.SOAP_BODY;
import static org.objectweb.celtix.datamodel.soap.SOAPConstants.SOAP_ENV;

public class RPCInterceptorTest extends TestBase {

    private DepthXMLStreamReader reader;
    
    public void setUp() throws Exception {
        super.setUp();
        XMLStreamReader xr = getXMLStreamReader(getTestStream(getClass(), "resources/greetMeRpcLitReq.xml"));
        StaxStreamFilter filter = new StaxStreamFilter(new QName[]{SOAP_ENV, SOAP_BODY});
        xr = StaxUtils.createFilteredReader(xr, filter);
        reader = new DepthXMLStreamReader(xr);

        RPCInterceptor interceptor = new RPCInterceptor();
        interceptor.setPhase("phase1");
        chain.add(interceptor);
    }

    public void testGetMethodArgument() throws Exception {
        StaxUtils.toNextElement(reader);

        assertEquals("sendReceiveData", reader.getLocalName());

        StaxUtils.nextEvent(reader);
        StaxUtils.toNextElement(reader);

        assertEquals("in", reader.getLocalName());

        JAXBContext context = JAXBEncoderDecoder.createJAXBContextForClass(GreeterRPCLit.class);
        Object o = JAXBEncoderDecoder.unmarshall(context,
                                                 null,
                                                 reader,
                                                 new QName("in"),
                                                 MyComplexStruct.class);

        assertNotNull(o);
        assertTrue(o instanceof MyComplexStruct);
        MyComplexStruct s = (MyComplexStruct) o;
        assertEquals("this is element 2", s.getElem2());
    }

    public void testInterceptorRPCLitInbound() throws Exception {
        SoapMessage message = TestUtil.createEmptySoapMessage(new Soap11(), chain);
        message.setSource(InputStream.class, getTestStream(getClass(), "resources/greetMeRpcLitReq.xml"));
        message.put("service.model", getTestService());
        message.put("message.inbound", "message.inbound");
        message.put("JAXB_CALLBACK", getTestCallback());

        message.getInterceptorChain().doIntercept(message);

        List<?> parameters = (List<?>) message.get("PARAMETERS");
        assertEquals(1, parameters.size());

        assertEquals("sendReceiveData", (String) message.get(MessageContext.WSDL_OPERATION));
        
        Object obj = parameters.get(0);
        assertTrue(obj instanceof MyComplexStruct);
        MyComplexStruct s = (MyComplexStruct) obj;
        assertEquals("this is element 2", s.getElem2());
    }

    public void testInterceptorRPCLitOutbound() throws Exception {
        SoapMessage message = TestUtil.createEmptySoapMessage(new Soap11(), chain);
        message.setSource(InputStream.class, getTestStream(getClass(), "resources/greetMeRpcLitResp.xml"));
        message.put("service.model", getTestService());
        message.put("JAXB_CALLBACK", getTestCallback());

        message.getInterceptorChain().doIntercept(message);
        Object retValue = message.get("RETURN");
        assertNotNull(retValue);
        assertTrue(retValue instanceof MyComplexStruct);
        MyComplexStruct s = (MyComplexStruct) retValue;
        assertEquals("return is element 2", s.getElem2());
    }

    protected JAXBDataBindingCallback getTestCallback() throws Exception {
        JAXBContext ctx = JAXBEncoderDecoder.createJAXBContextForClass(GreeterRPCLit.class);
        Method m = org.objectweb.celtix.testutil.common.TestUtil.getMethod(GreeterRPCLit.class,
                                                                           "sendReceiveData");
        return new JAXBDataBindingCallback(m, Mode.PARTS, ctx);
    }

    public Service getTestService() {
        Service service = JAXWSClassServiceBuilder.buildService(GreeterRPCLit.class);
        assertNotNull(service);
        OperationInfo op = service.getOperation("sendReceiveData");
        assertNotNull(op);
        assertEquals(1, op.getInput().size());
        assertEquals(1, op.getOutput().size());
        MessageInfo msg = op.getInput();
        assertEquals(1, msg.getMessageParts().size());
        MessagePartInfo part = msg.getMessageParts().get(0);
        assertEquals(new QName("in"), part.getName());
        return service;
    }
}
