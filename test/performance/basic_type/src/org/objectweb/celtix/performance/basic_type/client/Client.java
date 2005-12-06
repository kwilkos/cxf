package org.objectweb.celtix.performance.basic_type.client;

import java.io.File;
import java.net.MalformedURLException;
import java.util.Iterator;
import java.util.List;

import javax.xml.namespace.QName;

import org.objectweb.celtix.pat.internal.TestCaseBase;
import org.objectweb.celtix.pat.internal.TestResult;
import org.objectweb.celtix.performance.basic_type.BasicPortType;
import org.objectweb.celtix.performance.basic_type.BasicService;
 
public final class Client extends TestCaseBase {
    
    private static final QName SERVICE_NAME 
        = new QName("http://celtix.objectweb.org/performance/basic_type", "BasicService");

    private static int opid;
    
    private  byte[] inputBase64 = new byte[100 * 1024];
    private  String inputString = new String();

    private final int asciiCount = 1 * 1024;
    
    private BasicService ss;
    private BasicPortType port;

    public Client(String[] args) {
        super("Base TestCase", args);
        serviceName = "BasicService";
        portName = "BasicPortType";
        operationName = "echoString";
        amount = 30;
        wsdlNameSpace = "http://celtix.objectweb.org/performance/basic_type";
    }

    public void initTestData() {
        String temp = "abcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*()_+?><[]/0123456789";
        for (int idx = 0; idx < 4 * packetSize; idx++) {
            for (int jdx = 0; jdx < 256; jdx++) {
                inputBase64[idx * 256 + jdx] = (byte)(jdx - 128);
            }
        }
        for (int i = 0; i < asciiCount / temp.length() * packetSize; i++) {
            inputString = inputString + temp;
        }
    }

    public void printUsage() {
        System.out.println("Syntax is: Client [-WSDL wsdllocation] operation [-operation args...]");
        System.out.println("   operation is one of: ");
        System.out.println("      echoBase64");
        System.out.println("      echoString");
    }

    public static void main(String args[]) throws Exception {
        Client client = new Client(args);
        client.initialize();
        if (client.getOperationName().equals("echoString")) {
            opid = 0;
        } else {
            opid = 1;
        }
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

    public void doJob() {
        try {
            switch(opid) {
            case 0:
                port.echoString(inputString);
                break;
            case 1:
                port.echoBase64(inputBase64);
                break;
            default:
                port.echoString(inputString);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void getPort() {
        File wsdl = new File(wsdlPath);
        try {
            ss = new BasicService(wsdl.toURL(), SERVICE_NAME);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        port = ss.getSoapHttpPort();
    }
} 
 


