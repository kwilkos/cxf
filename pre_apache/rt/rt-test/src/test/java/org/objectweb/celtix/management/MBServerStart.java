package org.objectweb.celtix.management;

import org.objectweb.celtix.Bus;
import org.objectweb.celtix.BusException;


public class MBServerStart {
    protected MBServerStart() {        
    }
    
    public static void main(String[] args) throws BusException {
        // Create the InstrumentationsManager
        Bus bus = Bus.init(args);
        System.out.println("BusID is " + bus.getBusID());
        //AutomaticWorkQueue awq = 
        bus.getWorkQueueManager().getAutomaticWorkQueue();
        //need to use awq to run the programe 
        System.out.println("Waiting forever...");
        try {
            Thread.sleep(Long.MAX_VALUE);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
       
        /*MBeanServer server;
        
        ObjectName ton = null;
        
        ModelMBeanAssembler mbAssembler = new ModelMBeanAssembler();
        
        server = ManagementFactory.getPlatformMBeanServer(); 
        
        try {
            ton = new ObjectName("org.objectweb.celtix:Type=testInstrumentation");
        } catch (MalformedObjectNameException e) {            
            e.printStackTrace();
        } catch (NullPointerException e) {            
            e.printStackTrace();
        }
       
        AnnotationTestInstrumentation ati = new AnnotationTestInstrumentation();
        
        ModelMBeanInfo mbi = mbAssembler.getModelMbeanInfo(ati.getClass());
                
        RunTimeModelMBean rtMBean;
        
        try {
            rtMBean = (RunTimeModelMBean)server.instantiate(
                "org.objectweb.celtix.bus.management.jmx.export.runtime.RunTimeModelMBean");
        
                       
            rtMBean.setModelMBeanInfo(mbi);
                            
            rtMBean.setManagedResource(ati, "ObjectReference");
                           
            server.registerMBean(rtMBean, ton);
            
        } catch (ReflectionException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (MBeanException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InstanceNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InvalidTargetObjectTypeException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InstanceAlreadyExistsException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (NotCompliantMBeanException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
               
        System.out.println("Start the MBServer");
        try {
            Thread.sleep(Long.MAX_VALUE);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }*/
        
    }
}

