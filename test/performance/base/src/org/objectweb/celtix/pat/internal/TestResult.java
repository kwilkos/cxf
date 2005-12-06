package org.objectweb.celtix.pat.internal;

public class TestResult {
    public static final String AVG_UNIT = "(ms)";
    private static final String THROUGHPUT_UNIT = "(invocations/sec)";
    
    private String name;
    private TestCaseBase testCase;
  
    private double avgResponseTime;
    private double throughput;
  
    public TestResult() {
        this("Default Result");
    }

    public TestResult(String cname) {
        this(cname, null);
    }

    public TestResult(String cname, TestCaseBase test) {
        this.name = cname;
        this.testCase = test;
    }

    public void compute(long startTime, long endTime, int numberOfInvocations) {
        double numOfInvocations = (double)numberOfInvocations;
        double duration = convertToSeconds(endTime - startTime);
      
        throughput = numOfInvocations / duration;
        avgResponseTime  = duration / numOfInvocations;
    
        System.out.println("Throughput: " + testCase.getOperationName() + " " + throughput + THROUGHPUT_UNIT);
        System.out.println("AVG. response time: " + avgResponseTime * 1000 + AVG_UNIT);
        System.out.println(numOfInvocations + " (invocations), running " + duration  + " (sec) ");

    }

    private double convertToSeconds(double ms) {
        return ms / 1000;
    }

    public String getName() {
        return this.name;
    }

    public double getAvgResponseTime() {
        return avgResponseTime;
    }

    public double getThroughput() {
        return throughput;
    }
}
