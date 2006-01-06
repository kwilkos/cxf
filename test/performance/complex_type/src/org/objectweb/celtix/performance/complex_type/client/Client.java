package org.objectweb.celtix.performance.complex_type.client;

import java.io.File;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.util.Iterator;
import java.util.List;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.namespace.QName;

import org.objectweb.celtix.pat.internal.TestCaseBase;
import org.objectweb.celtix.pat.internal.TestResult;
import org.objectweb.celtix.performance.complex_type.ComplexPortType;
import org.objectweb.celtix.performance.complex_type.ComplexService;
import org.objectweb.celtix.performance.complex_type.types.ColourEnum;
import org.objectweb.celtix.performance.complex_type.types.NestedComplexType;
import org.objectweb.celtix.performance.complex_type.types.NestedComplexTypeSeq;
import org.objectweb.celtix.performance.complex_type.types.SimpleStruct;

public final class Client extends TestCaseBase {
    private static final QName SERVICE_NAME = new QName(
                                             "http://celtix.objectweb.org/performance/complex_type",
                                             "ComplexService");          
    private static final QName PORT_NAME = new QName(
                                          "http://celtix.objectweb.org/performance/complex_type",
                                          "ComplexPortType");
    private ComplexService cs;
    private ComplexPortType port;
    private final NestedComplexTypeSeq complexTypeSeq = new NestedComplexTypeSeq();
       
    public Client(String[] args) {
        super("Base TestCase", args);
        serviceName = "ComplexService";
        portName = "ComplexPortType";
        operationName = "sendReceiveData";
        wsdlNameSpace = "http://celtix.objectweb.org/performance/complex_type";
        amount = 30;
        packetSize = 1;
    }

    public static void main(String args[]) throws Exception {
                       
        Client client = new Client(args);
        
        client.initialize(); 
        
        client.run();
        
        List results = client.getTestResults();
        TestResult testResult = null;
        for (Iterator iter = results.iterator(); iter.hasNext();) {
            testResult = (TestResult)iter.next();
            System.out.println("Throughput " + testResult.getThroughput());
            System.out.println("AVG Response Time " + testResult.getAvgResponseTime());
        }
        System.out.println("Celtix client is going to shutdown!");
    }
    
    private SimpleStruct getSimpleStruct() throws DatatypeConfigurationException {
        SimpleStruct ss = new SimpleStruct();
        ss.setVarFloat(Float.MAX_VALUE);
        ss.setVarShort(Short.MAX_VALUE);
        ss.setVarByte(Byte.MAX_VALUE);
        ss.setVarDecimal(new BigDecimal("3.1415926"));
        ss.setVarDouble(Double.MAX_VALUE);
        ss.setVarString("1234567890!@#$%^&*()abcdefghijk");
        ss.setVarAttrString("1234567890!@#$%^&*()abcdefghijk");
        ss.setVarDateTime(DatatypeFactory.newInstance().newXMLGregorianCalendar(2005, 12, 3, 0, 0, 9, 0, 0));
        return ss;
    }

    
    public void initTestData() {
        NestedComplexType  complexType  = new NestedComplexType();
        complexType.setVarString("#12345ABc");
        complexType.setVarUByte(Short.MAX_VALUE);
        complexType.setVarUnsignedLong(new BigInteger("13691056728"));
        complexType.setVarFloat(Float.MAX_VALUE);
        complexType.setVarQName(new QName("return", "return"));
        try {
            complexType.setVarStruct(getSimpleStruct());
        } catch (DatatypeConfigurationException e) {
            e.printStackTrace();
        }
         
        complexType.setVarEnum(ColourEnum.RED);
        byte[] binary = new byte[1024];
        for (int idx = 0; idx < 4; idx++) {
            for (int jdx = 0; jdx < 256; jdx++) {
                binary[idx * 256 + jdx] = (byte)(jdx - 128);
            }
        }
        complexType.setVarBase64Binary(binary);
        complexType.setVarHexBinary(binary);

        for (int i = 0; i < packetSize; i++) {
            complexTypeSeq.getItem().add(complexType);
        }            
    }
    
    public void doJob() {
        port.sendReceiveData(complexTypeSeq);
    }

    public void getPort() {
        File wsdl = new File(wsdlPath);
        try {
            cs = new ComplexService(wsdl.toURL(), SERVICE_NAME);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        port = (ComplexPortType)cs.getPort(PORT_NAME, ComplexPortType.class);
    }

    public void printUsage() {
        System.out.println("Syntax is: Client [-WSDL wsdllocation] [-PacketSize ]");
    }
}
