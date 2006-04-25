package demo.hw.server;


import org.objectweb.celtix.bus.management.jmx.export.annotation.ManagedAttribute;
import org.objectweb.celtix.bus.management.jmx.export.annotation.ManagedOperation;
import org.objectweb.celtix.bus.management.jmx.export.annotation.ManagedResource;
import org.objectweb.celtix.management.Instrumentation;

@ManagedResource(componentName = "GreeterInstrumentationName",
                 description = "The Celtix Service instrumentation demo component ",
                 currencyTimeLimit = 15, persistPolicy = "OnUpdate")
public class GreeterInstrumentation implements Instrumentation {
    
    private GreeterImpl greeter;

    public GreeterInstrumentation(GreeterImpl gi) {
        greeter = gi;
    }

    public String getInstrumentationName() {
        return "GreeterInstrumentation";
    }

    public Object getComponent() {
        return this;
    }

    public String getUniqueInstrumentationName() {
        return ",name=Demo.Management";
    }
    
    @ManagedAttribute(description = "Get the GreetMe call counter")
    public Integer getGreetMeCounter() {
        return greeter.requestCounters[0];
    }

    @ManagedAttribute(description = "Get the GreetMeOneWay call counter")
    public Integer getGreetMeOneWayCounter() {
        return greeter.requestCounters[1];
    }

    @ManagedAttribute(description = "Get the SayHi call counter")
    public Integer getSayHiCounter() {
        return greeter.requestCounters[2];
    }

    @ManagedAttribute(description = "Get the Ping me call counter")
    public Integer getPingMeCounter() {
        return greeter.requestCounters[3];
    }

    @ManagedAttribute(description = "Set the Ping me call counter")
    public void setPingMeCounter(Integer value) {
        greeter.requestCounters[3] = value;
    }

    @ManagedOperation(description = "set the SayHi return name",
                      currencyTimeLimit = -1)
    public void setSayHiReturnName(String name) {
        greeter.returnName = name;
    }
}


    
                      
