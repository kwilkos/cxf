package demo.soap_header.client;

import java.io.File;
import javax.xml.namespace.QName;
import javax.xml.ws.Holder;
import org.objectweb.header_test.SOAPHeaderService;
import org.objectweb.header_test.TestHeader;

import org.objectweb.header_test.types.TestHeader1;
import org.objectweb.header_test.types.TestHeader1Response;
import org.objectweb.header_test.types.TestHeader2;
import org.objectweb.header_test.types.TestHeader2Response;
import org.objectweb.header_test.types.TestHeader3;
import org.objectweb.header_test.types.TestHeader3Response;
import org.objectweb.header_test.types.TestHeader5;

public final class Client {

    private static final QName SERVICE_NAME 
        = new QName("http://objectweb.org/header_test", "SOAPHeaderService");


    private Client() {
    } 

    public static void main(String args[]) throws Exception {
        
        if (args.length == 0) { 
            System.out.println("please specify wsdl");
            System.exit(1); 
        }

        File wsdl = new File(args[0]);
        
        SOAPHeaderService ss = new SOAPHeaderService(wsdl.toURL(), SERVICE_NAME);
        TestHeader proxy = ss.getSoapHeaderPort();

        // Invoke testHeader1 operation
        System.out.println("Invoking testHeader1 operation");
        TestHeader1 in1 = new TestHeader1();
        TestHeader1 inHeader1 = in1;
        TestHeader1Response response1 = proxy.testHeader1(in1, inHeader1);
        System.out.println("\ttestHeader1 operation: returned " + response1.getResponseType());

        // Invoke testHeader2 operation
        System.out.println("Invoking testHeader2 operation");
        TestHeader2 in2 = new TestHeader2();
        Holder<TestHeader2Response> out2 = new Holder<TestHeader2Response>();
        Holder<TestHeader2Response> outHeader2 = new Holder<TestHeader2Response>();

        in2.setRequestType(TestHeader2Response.class.getSimpleName());
        proxy.testHeader2(in2, out2, outHeader2);
        System.out.println("\ttestHeader2 operation: header type " + outHeader2.value.getResponseType());

        // Invoke testHeader3
        System.out.println("Invoking testHeader3 operation");
        TestHeader3 in3 = new TestHeader3();
        Holder<TestHeader3> inoutHeader3 = new Holder<TestHeader3>();

        in3.setRequestType(TestHeader3.class.getSimpleName());
        inoutHeader3.value = new TestHeader3();
        inoutHeader3.value.setRequestType(TestHeader3.class.getSimpleName());
        TestHeader3Response response3 = proxy.testHeader3(in3, inoutHeader3);
        System.out.println("\ttestHeader3 operation: header type " + inoutHeader3.value.getRequestType());
        System.out.println("\treturn type " + response3.getResponseType());

        // Invoke testHeader5
        System.out.println("Invoking testHeader5 operation");
        TestHeader5 in5 = new TestHeader5();
        in5.setRequestType(TestHeader5.class.getSimpleName());
        TestHeader5 response5 = proxy.testHeader5(in5);
        System.out.println("\ttestHeader5 operation: return type " + response5.getRequestType());
 
        System.exit(0); 
    }

}
