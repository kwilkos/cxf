package demo.soap_header.client;

import java.io.File;
import javax.xml.namespace.QName;
import javax.xml.ws.Holder;
import org.objectweb.headers.HeaderService;
import org.objectweb.headers.HeaderTester;
import org.objectweb.headers.InHeader;
import org.objectweb.headers.InHeaderResponse;
import org.objectweb.headers.InoutHeader;
import org.objectweb.headers.InoutHeaderResponse;
import org.objectweb.headers.OutHeader;
import org.objectweb.headers.OutHeaderResponse;
import org.objectweb.headers.SOAPHeaderData;

public final class Client {

    private static final QName SERVICE_NAME
        = new QName("http://objectweb.org/headers", "HeaderService");


    private Client() {
    }

    public static void main(String args[]) throws Exception {

        if (args.length == 0) {
            System.out.println("please specify wsdl");
            System.exit(1);
        }

        File wsdl = new File(args[0]);

        HeaderService hs = new HeaderService(wsdl.toURL(), SERVICE_NAME);
        HeaderTester proxy = hs.getSoapPort();

        invokeInHeader(proxy);
        invokeOutHeader(proxy);  
        invokeInOutHeader(proxy);  
    }
     
    private static void invokeInHeader(HeaderTester proxy) {
        // invoke inHeader operation
        System.out.println("Invoking inHeader operation");
        InHeader me = new InHeader();
        me.setRequestType("Celtix user");
        SOAPHeaderData headerInfo = new SOAPHeaderData();
        headerInfo.setOriginator("Celtix client");
        headerInfo.setMessage("Invoking inHeader operation");
        InHeaderResponse response = proxy.inHeader(me, headerInfo);
        System.out.println("\tinHeader invocation returned: ");
        System.out.println("\t\tResult: " + response.getResponseType());
    }

    private static void invokeOutHeader(HeaderTester proxy) {    
        // invoke outHeaderoperation
        System.out.println("Invoking outHeader operation");
        OutHeader me = new OutHeader();
        me.setRequestType("Celtix user");
        Holder<OutHeaderResponse> theResponse = new Holder<OutHeaderResponse>();
        Holder<SOAPHeaderData> headerInfo = new Holder<SOAPHeaderData>();
        proxy.outHeader(me, theResponse, headerInfo);
        System.out.println("\toutHeader invocation returned: ");
        System.out.println("\t\tOut parameter: " + theResponse.value.getResponseType());
        System.out.println("\t\tHeader content:");
        System.out.println("\t\t\tOriginator: " + headerInfo.value.getOriginator());
        System.out.println("\t\t\tMessage: " + headerInfo.value.getMessage());
    }

    private static void invokeInOutHeader(HeaderTester proxy) {
        System.out.println("Inovking inoutHeader operation");
        InoutHeader me = new InoutHeader();
        me.setRequestType("Celtix user");
        Holder<SOAPHeaderData> headerInfo = new Holder<SOAPHeaderData>();
        SOAPHeaderData shd = new SOAPHeaderData();
        shd.setOriginator("Celtix client");
        shd.setMessage("Inovking inoutHeader operation");
        headerInfo.value = shd;
        InoutHeaderResponse response = proxy.inoutHeader(me, headerInfo);
        System.out.println("\tinoutHeader invocation returned: ");
        System.out.println("\t\tResult: " + response.getResponseType());
        System.out.println("\t\tHeader content:");
        System.out.println("\t\t\tOriginator: " + headerInfo.value.getOriginator());
        System.out.println("\t\t\tMessage: " + headerInfo.value.getMessage());

        System.exit(0);
    }
}

