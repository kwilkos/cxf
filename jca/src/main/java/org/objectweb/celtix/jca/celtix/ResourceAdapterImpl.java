package org.objectweb.celtix.jca.celtix;


import java.util.HashSet;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Logger;

import javax.resource.NotSupportedException;
import javax.resource.ResourceException;
import javax.resource.spi.ActivationSpec;
import javax.resource.spi.BootstrapContext;
import javax.resource.spi.ResourceAdapter;
import javax.resource.spi.ResourceAdapterInternalException;
import javax.resource.spi.endpoint.MessageEndpointFactory;
import javax.transaction.xa.XAResource;

import org.objectweb.celtix.Bus;
import org.objectweb.celtix.BusException;
import org.objectweb.celtix.jca.core.resourceadapter.ResourceBean;

public class ResourceAdapterImpl extends ResourceBean implements ResourceAdapter {

    private static final Logger LOG = Logger.getLogger(ResourceAdapterImpl.class.getName());
    private BootstrapContext ctx;
    private Set <Bus> busCache = new HashSet<Bus>();
   
    public ResourceAdapterImpl() {
        super();
        LOG.fine("Resource Adapter is constructed without props");
    }

    public ResourceAdapterImpl(Properties props) {
        super(props);
        LOG.fine("Resource Adapter is constructed with props");
    }
    
    public void registerBus(Bus bus) {
        LOG.fine("Bus " + bus + " initialized and added to ResourceAdapter busCache");
        busCache.add(bus);
    }

    protected Set getBusCache() {
        return busCache;
    }

    protected void setBusCache(Set<Bus> cache) {
        this.busCache = cache;
    } 
   
    public void start(BootstrapContext aCtx) throws ResourceAdapterInternalException {
        LOG.fine("Resource Adapter is starting by appserver...");
        if (aCtx == null) {
            throw new ResourceAdapterInternalException("BootstrapContext can not be null");
        }
        this.ctx = aCtx;
    }

    public void stop() {
        LOG.fine("Resource Adapter is stopping by appserver...");
        if (!busCache.isEmpty()) {

            Iterator busIterator = busCache.iterator();
            Bus bus = null;
            int busCounter = 0;

            while (busIterator.hasNext()) {

                busCounter++;
                bus = (Bus)busIterator.next();

                try {
                    if (bus != null) {
                        bus.shutdown(true);
                        LOG.fine("Number " + busCounter + " Bus: " + bus + " has been shut down");
                    } else {
                        LOG.fine("Number " + busCounter + " Bus is null");
                    }
                } catch (BusException be) {
                    LOG.fine("Failed to shutdown bus when stop ResourceAdapter, reason: " + be);
                }
            }
        }   
    }

    public XAResource[] getXAResources(ActivationSpec as[])
        throws ResourceException {
        throw new NotSupportedException();
    }

    public void endpointActivation(MessageEndpointFactory mef, ActivationSpec as)
        throws ResourceException {
        throw new NotSupportedException();
    }

    public void endpointDeactivation(MessageEndpointFactory mef, ActivationSpec as) {
    }

    public BootstrapContext getBootstrapContext() {
        return ctx;
    }
}















