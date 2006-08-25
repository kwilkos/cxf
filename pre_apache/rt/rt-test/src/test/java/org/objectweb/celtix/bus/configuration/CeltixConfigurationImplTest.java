package org.objectweb.celtix.bus.configuration;

import junit.framework.TestCase;

import org.objectweb.celtix.Bus;
import org.objectweb.celtix.bus.busimpl.CeltixBus;
import org.objectweb.celtix.configuration.Configuration;

public class CeltixConfigurationImplTest extends TestCase {
    Bus bus;

    public void setUp() throws Exception {
        bus = Bus.getCurrent();
    }

    public void tearDown() {
        
    }

    public void testServicesMonitoring() {
        Configuration busConfig = bus.getConfiguration();
        boolean servicesMonitoring = busConfig.getBoolean("servicesMonitoring");
        assertEquals("servicesMonitoring is wrong", false, servicesMonitoring);
        assertEquals("bus.servicesMonitoring is wrong", false, ((CeltixBus)bus).isServicesMonitoring());

        busConfig.setObject("servicesMonitoring", Boolean.TRUE);
        Configuration busConfigNew = bus.getConfiguration();
        boolean servicesMonitoringNew = busConfigNew.getBoolean("servicesMonitoring");
        assertEquals("servicesMonitoring is wrong", true, servicesMonitoringNew);
        assertEquals("bus.servicesMonitoring is wrong", true, ((CeltixBus)bus).isServicesMonitoring());
    }

}
