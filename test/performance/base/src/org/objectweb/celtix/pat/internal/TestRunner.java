package org.objectweb.celtix.pat.internal;

import java.util.logging.Logger;

public class TestRunner implements Runnable{

    private String name;
    protected TestCase testCase;

    public TestRunner() {
        this("Default runner");
    }

    public TestRunner(String name) {
        this(name, null);
    }

    public TestRunner(String name, TestCase testCase) {
        this.name = name;
        this.testCase = testCase;
    }

    public void run() {     
        System.out.println("TestRunner " + name + " is running");
        try {
            testCase.internalTestRun(name);
        } catch(Exception e) {
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
