package org.objectweb.celtix.bus.transports.https;

import java.util.logging.Handler;
import java.util.logging.LogRecord;


class TestHandler extends Handler {
    String log;
    
    public TestHandler() {
        log = "";
    }
    
    
    public void publish(LogRecord record) {
        log += record.getMessage();
        
    }

    
    public void flush() {
        
    }

    public void close() throws SecurityException {
        
    }
    
    boolean checkLogContainsString(String str) {
        if (log.indexOf(str) == -1) {
            return false;
        }
        return true;
    }
    
}