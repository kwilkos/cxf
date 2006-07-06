package org.objectweb.celtix.jca.celtix;


import java.util.Properties;
import java.util.logging.Logger;

import javax.resource.NotSupportedException;
import javax.resource.ResourceException;
import javax.resource.spi.ActivationSpec;
import javax.resource.spi.BootstrapContext;
import javax.resource.spi.ResourceAdapter;
import javax.resource.spi.ResourceAdapterInternalException;
import javax.resource.spi.endpoint.MessageEndpointFactory;
import javax.transaction.xa.XAResource;

import org.objectweb.celtix.common.logging.LogUtils;
import org.objectweb.celtix.jca.core.resourceadapter.ResourceBean;

public class ResourceAdapterImpl extends ResourceBean implements ResourceAdapter {

    private static final Logger LOG = LogUtils.getL7dLogger(ResourceAdapterImpl.class);
    private BootstrapContext ctx;
   
    public ResourceAdapterImpl() {
        super();
        LOG.info("Resource Adapter is constructed without props");
    }

    public ResourceAdapterImpl(Properties props) {
        super(props);
        LOG.info("Resource Adapter is constructed with props");
    }

     
   
    public void start(BootstrapContext aCtx) throws ResourceAdapterInternalException {
        LOG.info("Resource Adapter is starting by appserver...");
        if (ctx == null) {
            throw new ResourceAdapterInternalException("BootstrapContext can not be null");
        }
        this.ctx = aCtx;
    }

    public void stop() {
        LOG.info("Resource Adapter is stopping by appserver...");

        //shutdown the bus
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















