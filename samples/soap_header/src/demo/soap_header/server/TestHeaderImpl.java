
package demo.soap_header.server;

import javax.xml.ws.Holder;
import org.objectweb.header_test.TestHeader;
import org.objectweb.header_test.types.TestHeader1;
import org.objectweb.header_test.types.TestHeader1Response;
import org.objectweb.header_test.types.TestHeader2;
import org.objectweb.header_test.types.TestHeader2Response;
import org.objectweb.header_test.types.TestHeader3;
import org.objectweb.header_test.types.TestHeader3Response;
import org.objectweb.header_test.types.TestHeader5;


public class TestHeaderImpl implements TestHeader {


    /**
     * 
     * @param inHeader
     * @param in
     * @return
     *     returns org.objectweb.header_test.types.TestHeader1Response
     */
    public TestHeader1Response testHeader1(
        TestHeader1 in,
        TestHeader1 inHeader) {
        if (in == null || inHeader == null) {
            throw new IllegalArgumentException("TestHeader1 part not found.");
        }

        System.out.println("testHeader1 operation invoked:");
        System.out.println("\tin parameter class: " + in.getClass().getSimpleName());
        System.out.println("\tinHeader class: " + inHeader.getClass().getSimpleName() + "\n");
        TestHeader1Response returnVal = new TestHeader1Response();
        
        returnVal.setResponseType("The header class is of type " + inHeader.getClass().getSimpleName());
        return returnVal;        
    }

    /**
     * 
     * @param out
     * @param outHeader
     * @param in
     */
    public void testHeader2(
        TestHeader2 in,
        Holder<TestHeader2Response> out,
        Holder<TestHeader2Response> outHeader) {

        System.out.println("testHeader2 operation invoked:");
        System.out.println("\tin parameter class: " + in.getClass().getSimpleName());
        System.out.println("\tout parameter class: " + out.getClass().getSimpleName());
        System.out.println("\toutHeader parameter class: " + outHeader.getClass().getSimpleName() + "\n");
        
        TestHeader2Response outVal = new TestHeader2Response();
        outVal.setResponseType(in.getRequestType());
        out.value = outVal;
        
        TestHeader2Response outHeaderVal = new TestHeader2Response();
        outHeaderVal.setResponseType("The header class is of type " + in.getRequestType());
        outHeader.value = outHeaderVal;        
    }

    /**
     * 
     * @param inoutHeader
     * @param in
     * @return
     *     returns org.objectweb.header_test.types.TestHeader3Response
     */
    public TestHeader3Response testHeader3(
        TestHeader3 in,
        Holder<TestHeader3> inoutHeader) {

        System.out.println("testHeader3 operation invoked:");
        System.out.println("\tin parameter class: " + in.getClass().getSimpleName());
        System.out.println("\tinoutHeader class: " + inoutHeader.getClass().getSimpleName() + "\n");
        
        if (inoutHeader.value == null) {
            throw new IllegalArgumentException("TestHeader3 part not found.");
        }
        TestHeader3Response returnVal = new TestHeader3Response();
        returnVal.setResponseType(inoutHeader.value.getRequestType());
        
        inoutHeader.value.setRequestType(in.getRequestType());
        return returnVal;
    }

    /**
     * 
     * @param requestType
     */
    public void testHeader4(
        String requestType) {
        
    }

    /**
     * 
     * @param in
     * @return
     *     returns org.objectweb.header_test.types.TestHeader5
     */
    public TestHeader5 testHeader5(
        TestHeader5 in) {
        System.out.println("testHeader5 operation invoked");
        System.out.println("\tin parameter class: " + in.getClass().getSimpleName() + "\n");

        return in;
    }
    
}
