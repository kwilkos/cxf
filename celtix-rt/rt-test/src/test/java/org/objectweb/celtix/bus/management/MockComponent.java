package org.objectweb.celtix.bus.management;

import org.objectweb.celtix.management.Instrumentation;
import org.objectweb.celtix.management.InstrumentationFactory;

public class MockComponent implements InstrumentationFactory {
    public MockComponent() {
    }

    public Instrumentation createInstrumentation() {
        return  new MockComponentInstrumentation(this);
    }
}
