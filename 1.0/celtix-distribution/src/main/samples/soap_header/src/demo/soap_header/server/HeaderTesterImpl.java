package demo.soap_header.server;

import javax.xml.ws.Holder;
import org.objectweb.headers.HeaderTester;
import org.objectweb.headers.InHeader;
import org.objectweb.headers.InHeaderResponse;
import org.objectweb.headers.InoutHeader;
import org.objectweb.headers.InoutHeaderResponse;
import org.objectweb.headers.OutHeader;
import org.objectweb.headers.OutHeaderResponse;
import org.objectweb.headers.SOAPHeaderData;


@javax.jws.WebService(name = "HeaderTester", serviceName = "HeaderService",
                      targetNamespace = "http://objectweb.org/headers",
                      wsdlLocation = "file:./wsdl/soap_header.wsdl")


public class HeaderTesterImpl implements HeaderTester {

    public InHeaderResponse inHeader(InHeader me,
                                     SOAPHeaderData headerInfo) {
        System.out.println("inHeader invoked");

        System.out.println("\tGetting Originator: " + headerInfo.getOriginator());
        System.out.println("\tGetting Message: " + headerInfo.getMessage());

        InHeaderResponse ihr = new InHeaderResponse();
        ihr.setResponseType("Hello " + me.getRequestType());
        return ihr;
    }

    public void outHeader(OutHeader me, 
                          Holder<OutHeaderResponse> theResponse,
                          Holder<SOAPHeaderData> headerInfo) {
        System.out.println("outHeader invoked");

        System.out.println("\tSetting originator: Celtix server");
        System.out.println("\tSetting message: outHeader invocation succeeded");

        SOAPHeaderData sh = new SOAPHeaderData();
        sh.setOriginator("Celtix server");
        sh.setMessage("outHeader invocation succeeded");
        headerInfo.value = sh;

        OutHeaderResponse ohr = new OutHeaderResponse();
        ohr.setResponseType("Hello " + me.getRequestType());
        theResponse.value = ohr;
    }

    public InoutHeaderResponse inoutHeader(InoutHeader me,
                                           Holder<SOAPHeaderData> headerInfo) {
        System.out.println("inoutHeader invoked");

        System.out.println("\tGetting Originator: " + headerInfo.value.getOriginator());
        System.out.println("\tGetting Message: " + headerInfo.value.getMessage());

        System.out.println("\tSetting originator: Celtix server");
        System.out.println("\tSetting message: inoutHeader invocation succeeded");

        headerInfo.value.setOriginator("Celtix server");
        headerInfo.value.setMessage("inoutHeader invocation succeeded");

        InoutHeaderResponse iohr = new InoutHeaderResponse();
        iohr.setResponseType("Hello " + me.getRequestType());

        return iohr;
    }    
}
