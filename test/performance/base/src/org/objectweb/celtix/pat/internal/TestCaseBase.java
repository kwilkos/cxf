package org.objectweb.celtix.pat.internal;

import java.util.*;

import org.objectweb.celtix.Bus;
import org.objectweb.celtix.BusException;

public abstract class TestCaseBase {
    private static boolean initialized;
    
    protected String wsdlPath;

    protected String serviceName;

    protected String portName;

    protected String operationName;

    protected String hostname;

    protected String hostport;

    protected int packetSize = 1;

    protected boolean usingTime;

    protected int amount = 1;

    protected String wsdlNameSpace;

    protected Bus bus;

    protected List<TestResult> results = new ArrayList<TestResult>();

    private int numberOfThreads;
    
    private String name;

    private String[] args;

    private String faultReason = "no error";

    
    public TestCaseBase() {
        this("DEFAULT TESTCASE", null);
    }

    public TestCaseBase(String cname) {
        this(cname, null);
    }

    public TestCaseBase(String cname, String[] arg) {
        this.name = cname;
        this.args = arg;
    }

    public abstract void initTestData();

    public void init() throws Exception {
        initBus();
        initTestData();
    }

    private void processArgs() {
        int count = 0;
        int argc = args.length; 
        while (true) {
            if (count >= argc) {
                break;
            }
            if ("-WSDL".equals(args[count])) {
                wsdlPath = args[count + 1];
                count += 2;
            } else if ("-Service".equals(args[count])) {
                serviceName = args[count + 1];
                count += 2;
            } else if ("-Port".equals(args[count])) {
                portName = args[count + 1];
                count += 2;
            } else if ("-Operation".equals(args[count])) {
                operationName = args[count + 1];
                count += 2;
            } else if ("-BasedOn".equals(args[count])) {
                if ("Time".equals(args[count + 1])) {
                    usingTime = true;
                }
                count += 2;
            } else if ("-Amount".equals(args[count])) {
                amount = Integer.parseInt(args[count + 1]);
                count += 2;
            } else if ("-Threads".equals(args[count])) {
                numberOfThreads = Integer.parseInt(args[count + 1]);
                count += 2;
            } else if ("-HostName".equals(args[count])) {
                hostname = args[count + 1];
                count += 2;
            } else if ("-HostPort".equals(args[count])) {
                hostport = args[count + 1];
                count += 2;
            } else if ("-PacketSize".equals(args[count])) {
                packetSize = Integer.parseInt(args[count + 1]);
                count += 2;
            } else {
                count++;
            }
        }
    }

    private boolean validate() {
        if (wsdlNameSpace == null || wsdlNameSpace.trim().equals("")) {
            System.out.println("WSDL name space is not specified");
            faultReason = "Missing WSDL name space";
            return false;
        }
        if (serviceName == null || serviceName.trim().equals("")) {
            System.out.println("Service name is not specified");
            faultReason = "Missing Service name";
            return false;
        }
        if (portName == null || portName.trim().equals("")) {
            System.out.println("Port name is not specified");
            faultReason = "Missing Port name";
            return false;
        }
        if (wsdlPath == null || wsdlPath.trim().equals("")) {
            System.out.println("WSDL path is not specifed");
            faultReason = "Missing WSDL path";
            return false;
        }
        return true;
    }

    // for the celtix init , here do nothing
    private void initBus() throws BusException {
        bus = Bus.init();
    }

    public void tearDown() throws BusException {
        System.out.println("Bus is going to shutdown");
        if (bus != null) {
            bus.shutdown(true);
        }
    }

    protected void setUp() throws Exception {
       
        clearTestResults();
        printTitle();
        printSetting("Default Setting: ");
        processArgs();
        if (!validate()) {
            System.out.println("Configure Exception!" + faultReason);
            System.exit(1);
        }
        init();
        printSetting("Runtime Setting: ");
    }

    public void initialize() {
        try {
            if (!initialized) {
                setUp();
            }
            initialized = true;


            System.out.println("TestCase " + name + " is warming up the jit. (5 sec/200 iterations)");
            long endTime = System.currentTimeMillis() + 5000;
            getPort();
            int count = 0;
            while (System.currentTimeMillis() < endTime || count < 200) {
                count++;
                doJob();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public abstract void doJob();

    public abstract void getPort();

    protected void internalTestRun(String caseName) throws Exception {
        int numberOfInvocations = 0;
        long startTime = System.currentTimeMillis();
        long endTime = startTime + amount * 1000;
        getPort();
        if (usingTime) {
            while (System.currentTimeMillis() < endTime) {
                doJob();
                numberOfInvocations++;
            }
        } else {
            for (int i = 0; i < amount; i++) {
                doJob();
                numberOfInvocations++;
            }
        }
        endTime = System.currentTimeMillis();
        TestResult testResult = new TestResult(caseName, this);
        testResult.compute(startTime, endTime, numberOfInvocations);
        addTestResult(testResult);
    }

    public void testRun() throws Exception {
        if (numberOfThreads == 0) {
            internalTestRun(name);
        }
        List<Thread> threadList = new ArrayList<Thread>();
        for (int i = 0; i < numberOfThreads; i++) {
            TestRunner runner = new TestRunner("No." + i + " TestRunner", this);
            Thread thread = new Thread(runner, "RunnerThread No." + i);
            thread.start();
            threadList.add(thread);
        }

        for (Iterator iter = threadList.iterator(); iter.hasNext();) {
            Thread thread = (Thread) iter.next();
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void run() {
        try {
            System.out.println("TestCase " + name + " is running");
            testRun();
            tearDown();
            System.out.println("TestCase " + name + " is finished");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void clearTestResults() {
        results.clear();
    }

    protected void addTestResult(TestResult result) {
        results.add(result);
    }

    public List getTestResults() {
        return results;
    }

    public abstract void printUsage();

    public void printSetting(String settingType) {
        System.out.println(settingType + "  [Service] -- > " + serviceName);
        System.out.println(settingType + "  [Port] -- > " + portName);
        System.out.println(settingType + "  [Operation] -- > " + operationName);
        System.out.println(settingType + "  [Threads] -- > " + numberOfThreads);
        System.out.println(settingType + "  [Packet Size] -- > " + packetSize
                           + " packet(s) ");
        if (usingTime) {
            System.out.println(settingType + "  [Running] -->  " + amount + " (secs)");
        } else {
            System.out.println(settingType + "  [Running] -->  " + amount
                               + " (invocations)");
        }
    }

    public void printTitle() {
        System.out.println(" ---------------------------------");
        System.out.println(name + "  Client (JAVA Version)");
        System.out.println(" ---------------------------------");
    }

    public void setWSDLNameSpace(String nameSpace) {
        this.wsdlNameSpace = nameSpace;
    }

    public void setWSDLPath(String wpath) {
        this.wsdlPath = wsdlPath;
    }

    public void setServiceName(String sname) {
        this.serviceName = sname;
    }

    public void setPortName(String pname) {
        this.portName = pname;
    }

    public void setOperationName(String oname) {
        this.operationName = oname;
    }

    public String getServiceName() {
        return this.serviceName;
    }

    public String getPortName() {
        return this.portName;
    }

    public String getOperationName() {
        return this.operationName;
    }

    public String getName() {
        return this.name;
    }

    public Bus getBus() {
        return this.bus;
    }


}
