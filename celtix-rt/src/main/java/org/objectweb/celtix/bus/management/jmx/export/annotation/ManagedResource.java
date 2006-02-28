package org.objectweb.celtix.bus.management.jmx.export.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ManagedResource {

    String objectName() default "";

    String description() default "";

    int currencyTimeLimit() default -1;

    boolean log() default false;

    String logFile() default "";

    String persistPolicy() default "";

    int persistPeriod() default -1;

    String persistName() default "";

    String persistLocation() default "";        

}
