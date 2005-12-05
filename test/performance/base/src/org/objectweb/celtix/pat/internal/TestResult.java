package org.objectweb.celtix.pat.internal;

public class TestResult {

    private String name;
    private TestCase testCase;
  
    private double avgResponseTime;
    private double throughput;
  
    public static final String AVG_UNIT = "(ms)";
    private static final String THROUGHPUT_UNIT = "(invocations/sec)";
    
    public TestResult() {
        this("Default Result");
    }

    public TestResult(String name) {
        this(name,null);
    }

    public TestResult(String name, TestCase testCase) {
        this.name = name;
        this.testCase = testCase;
    }

    public void compute(long startTime, long endTime, int numberOfInvocations) {
        double numOfInvocations = (double)numberOfInvocations;
        double duration = convertToSeconds(endTime - startTime);
      
        throughput = numOfInvocations / duration ;
        avgResponseTime  = duration / numOfInvocations ;
    
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





