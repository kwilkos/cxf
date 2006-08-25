package org.objectweb.celtix.jca.celtix.handlers;

import java.lang.reflect.Method;


public class TestTarget implements TestInterface { 
    boolean methodInvoked; 

    Method lastMethod; 

    public void testMethod() { 
        try { 
            methodInvoked = true; 
            lastMethod = getClass().getMethod("testMethod", new Class[0]); 
        } catch (NoSuchMethodException ex) { 
            throw new RuntimeException(ex); 
        } 
    } 

    public String toString() { 
        try { 
            methodInvoked = true; 
            lastMethod = getClass().getMethod("toString", new Class[0]); 
            return "TestTarget"; 
            // don't delegate to super as this
            // calls hashCode which messes up the
            // test 
        } catch (NoSuchMethodException ex) { 
            throw new RuntimeException(ex); 
        } 
    } 


    public int hashCode() { 
        try { 
            methodInvoked = true;  
            lastMethod = getClass().getMethod("hashCode", new Class[0]); 
            return super.hashCode(); 
        } catch (NoSuchMethodException ex) { 
            throw new RuntimeException(ex); 
        } 

    } 

    public boolean equals(Object obj) { 
        try { 
            methodInvoked = true; 
            lastMethod = getClass().getMethod("equals", new Class[] {Object.class}); 
            return super.equals(obj); 
        } catch (NoSuchMethodException ex) { 
            throw new RuntimeException(ex); 
        } 

    } 

} 
