package org.objectweb.celtix.pat.internal;

public class TestRunner implements Runnable {

    protected TestCaseBase testCase;
    private String name;
    

    public TestRunner() {
        this("Default runner");
    }

    public TestRunner(String cname) {
        this(cname, null);
    }

    public TestRunner(String cname, TestCaseBase test) {
        this.name = cname;
        this.testCase = test;
    }

    public void run() {     
        System.out.println("TestRunner " + name + " is running");
        try {
            testCase.internalTestRun(name);
        } catch (Exception e) {
            e.printStackTrace();
        }    
        System.out.println("TestRunner " + name + " is finished");
    }
  
    public void start() {
        Thread thread = new Thread(this);
        thread.start();
    }

    public String getName() {
        return name;
    }
}
