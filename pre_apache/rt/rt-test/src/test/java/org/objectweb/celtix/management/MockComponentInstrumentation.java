package org.objectweb.celtix.management;

public class MockComponentInstrumentation implements Instrumentation {
    private static final String INSTRUMENTATION_NAME = "Mock.Component";

    private static int instanceNumber;
    
    MockComponent mockComponent;
    String objectName;
    
    public MockComponentInstrumentation(MockComponent moc) {
        mockComponent = moc;
        objectName = "MockComponent" + instanceNumber;
        instanceNumber++;
    }

    public Object getComponent() {        
        return mockComponent;
    }

    public String getInstrumentationName() {
        return INSTRUMENTATION_NAME;
    }

    public String getUniqueInstrumentationName() {
        return objectName;
    }

}
