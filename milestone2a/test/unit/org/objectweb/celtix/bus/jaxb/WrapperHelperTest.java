package org.objectweb.celtix.bus.jaxb;

import java.util.ArrayList;
import java.util.List;
import junit.framework.TestCase;
import org.objectweb.handler_test.types.PingResponse;
import org.objectweb.hello_world_soap_http.types.AddNumbers;
import org.objectweb.hello_world_soap_http.types.AddNumbersResponse;
import org.objectweb.type_test.TestBoolean;
import org.objectweb.type_test.TestByte;
import org.objectweb.type_test.TestByteResponse;

public class WrapperHelperTest extends TestCase {

    public WrapperHelperTest(String arg0) {
        super(arg0);
    }

    public static void main(String[] args) {
        junit.textui.TestRunner.run(WrapperHelperTest.class);
    }

    public void testSetWrappedPart() throws Exception {
        AddNumbers wrapper = new AddNumbers();
        int arg0 = 10;
        int arg1 = 20;
        
        WrapperHelper.setWrappedPart("arg0", wrapper, arg0);
        WrapperHelper.setWrappedPart("arg1", wrapper, arg1);
        assertEquals(arg0, wrapper.getArg0());
        assertEquals(arg1, wrapper.getArg1());
        
        AddNumbersResponse wrapper1 = new AddNumbersResponse();
        int arg2 = 30;
        WrapperHelper.setWrappedPart("return", wrapper1, arg2);
        assertEquals(arg2, wrapper1.getReturn());
        
        TestBoolean wrapper2 = new TestBoolean();
        WrapperHelper.setWrappedPart("x", wrapper2, true);
        assertEquals(true, wrapper2.isX());
        
        TestByte wrapper3 = new TestByte();
        byte arg3 = 1;
        WrapperHelper.setWrappedPart("x", wrapper3, arg3);
        assertEquals(arg3, wrapper3.getX());
        
        try {
            WrapperHelper.setWrappedPart("x", wrapper3, null);
        } catch (IllegalArgumentException ex) {
            //Expected Exception
        }
    }

    public void testSetWrappedPartList() throws Exception {
        List<String> list = new ArrayList<String>();
        list.add("arg0");
        list.add("arg1");
        
        PingResponse wrapper = new PingResponse();
        assertNotNull(wrapper.getHandlersInfo());
        assertEquals(0, wrapper.getHandlersInfo().size());
        WrapperHelper.setWrappedPart("handlersInfo", wrapper, list);
        assertEquals(2, wrapper.getHandlersInfo().size());
        assertEquals(list, wrapper.getHandlersInfo());
    }
    
    public void testGetWrappedPart() throws Exception {
        AddNumbers wrapper = new AddNumbers();
        int arg0 = 10;
        int arg1 = 20;
        wrapper.setArg0(arg0);
        wrapper.setArg1(arg1);
        
        Object x = WrapperHelper.getWrappedPart("arg0", wrapper, int.class);
        Object y = WrapperHelper.getWrappedPart("arg1", wrapper, int.class);
        assertEquals(arg0, x);
        assertEquals(arg1, y);

        TestBoolean wrapper1 = new TestBoolean();
        boolean arg2 = true;
        wrapper1.setX(arg2);
        WrapperHelper.getWrappedPart("x", wrapper1, boolean.class);
        assertEquals(arg2, wrapper1.isX());

        TestByte wrapper3 = new TestByte();
        byte arg3 = 1;
        wrapper3.setX(arg3);
        WrapperHelper.getWrappedPart("x", wrapper3, Byte.class);
        assertEquals(arg3, wrapper3.getX());

        TestByteResponse wrapper4 = new TestByteResponse();
        byte arg4 = 1;
        wrapper4.setReturn(arg4);
        WrapperHelper.getWrappedPart("return", wrapper4, Byte.class);
        assertEquals(arg4, wrapper4.getReturn());        
    }
    
}
