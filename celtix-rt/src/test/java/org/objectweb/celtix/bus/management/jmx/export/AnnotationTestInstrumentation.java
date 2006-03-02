
package org.objectweb.celtix.bus.management.jmx.export;

import org.objectweb.celtix.bus.management.jmx.export.annotation.ManagedAttribute;
import org.objectweb.celtix.bus.management.jmx.export.annotation.ManagedNotification;
import org.objectweb.celtix.bus.management.jmx.export.annotation.ManagedNotifications;
import org.objectweb.celtix.bus.management.jmx.export.annotation.ManagedOperation;
import org.objectweb.celtix.bus.management.jmx.export.annotation.ManagedOperationParameter;
import org.objectweb.celtix.bus.management.jmx.export.annotation.ManagedOperationParameters;
import org.objectweb.celtix.bus.management.jmx.export.annotation.ManagedResource;


@ManagedResource(objectName = "bean:name=testBean4", description = "My Managed Bean",
                 persistPolicy = "OnUpdate", currencyTimeLimit = 15 , 
                 log = false ,
                 logFile = "jmx.log", persistPeriod = 200,
                 persistLocation = "/local/work", persistName = "bar.jmx")
@ManagedNotifications({@ManagedNotification(name = "My Notification",
                                            notificationTypes = {"type.foo", "type.bar" }) })
public class AnnotationTestInstrumentation {

    private String name; 

    private String nickName;

    private int age;

    private boolean isSuperman;


    @ManagedAttribute(description = "The Age Attribute", currencyTimeLimit = 15)
    public int getAge() {
        return age;
    }
        
    public void setAge(int a) {
        this.age = a;
    }

    @ManagedOperation(currencyTimeLimit = 30)
    public long myOperation() {
        return 1L;
    }

    @ManagedAttribute(description = "The Name Attribute",
                      currencyTimeLimit = 20,
                      defaultValue = "bar",
                      persistPolicy = "OnUpdate")
    public void setName(String n) {
        this.name = n;
    }

    @ManagedAttribute(defaultValue = "bar", persistPeriod = 300)
    public String getName() {
        return name;
    }

    @ManagedAttribute(defaultValue = "barasd", description = "The Nick Name Attribute")
    public String getNickName() {
        return this.nickName;
    }

    public void setNickName(String n) {
        this.nickName = n;
    }

    @ManagedAttribute(description = "The Is Superman Attribute")
    public void setSuperman(boolean superman) {
        this.isSuperman = superman;
    }

    public boolean isSuperman() {
        return isSuperman;
    }

    @ManagedOperation(description = "Add Two Numbers Together")
    @ManagedOperationParameters({@ManagedOperationParameter(
                                 name = "x", description = "Left operand"),
                                 @ManagedOperationParameter(
                                 name = "y", description = "Right operand") })
    public int add(int x, int y) {
        return x + y;
    }

}
