package org.objectweb.celtix.bus.management;

import org.objectweb.celtix.Bus;

public class MBServerStart {
    protected MBServerStart() {        
    }
    
    public static void main(String[] args) {
        // Create the InstrumentationsManager
        Bus bus = Bus.getCurrent();
        bus.getWorkQueueManager().getAutomaticWorkQueue();   
        System.out.println("Waiting forever...");
        try {
            Thread.sleep(Long.MAX_VALUE);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}

