package org.objectweb.celtix.ws.rm;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.objectweb.celtix.common.logging.LogUtils;

public class SequenceMonitor {

    private static final long DEFAULT_MONITOR_INTERVAL = 60000L;
    private static final Logger LOG = LogUtils.getL7dLogger(SequenceMonitor.class);
    private long monitorInterval = DEFAULT_MONITOR_INTERVAL;
    private long firstCheck;
    private List<Long> receiveTimes = new ArrayList<Long>();

    public void acknowledgeMessage() {
        long now = System.currentTimeMillis();
        if (0 == firstCheck) {
            firstCheck = now + monitorInterval;
        }
        receiveTimes.add(new Long(now));
    }

    public int getMPM() {
        long now = System.currentTimeMillis();
        int mpm = 0;
        if (firstCheck > 0 && now >= firstCheck) {
            long threshold = now - monitorInterval;
            while (!receiveTimes.isEmpty()) {
                if (receiveTimes.get(0).longValue() <= threshold) {
                    receiveTimes.remove(0);
                } else {
                    break;
                }
            }
            mpm = receiveTimes.size();
        } 
        
        return mpm;
    }
    
    protected void setMonitorInterval(long i) {
        if (receiveTimes.size() == 0) {
            firstCheck = 0;
            monitorInterval = i;
        } else {
            LOG.warning("Cannot change monitor interval at this point.");
        }
    }
}
