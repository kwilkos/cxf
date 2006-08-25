package org.objectweb.celtix.management;


public class MockComponent implements InstrumentationFactory {
    public MockComponent() {
    }

    public Instrumentation createInstrumentation() {
        return  new MockComponentInstrumentation(this);
    }
}
